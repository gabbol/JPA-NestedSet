/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.code_factory.jpa.nestedset.events.EventBuilder;
import org.code_factory.jpa.nestedset.events.EventNode;
import org.code_factory.jpa.nestedset.events.NestedSetListenerProvider;

/**
 * 
 * Implements {@link NestedSetManager}
 * 
 * 
 * @author Roman Borschel <roman@code-factory.org>
 * @author gabbol
 */
public class JpaNestedSetManager implements NestedSetManager {

    private EntityManager em;
    private final Map<Key, Node<?>> nodes;
    private Configuration configuration;
    protected static final int DEPTH_INFINITE = 0;
    protected static final int DEPTH_ONE = 1;
    private NestedSetListenerProvider listenerProvider;

    public JpaNestedSetManager(Configuration configuration, EntityManager em) {
        this.em = em;
        this.configuration = configuration;
        this.nodes = new HashMap<Key, Node<?>>();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        EntityManager em = getEntityManager();
        em.createQuery("DELETE FROM " + configuration.getEntityName() + " c").executeUpdate();
        clear();
    }

    /**
     * Returns the configuration
     *
     * @return a configuration
     */
    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.nodes.clear();
    }

    /**
     * Establishes all parent/child/ancestor/descendant relationships of all the
     * nodes in the given list. As a result, invocations on the corresponding
     * methods on these node instances will not trigger any database queries.
     * 
     * @param <T>
     * @param treeList
     * @param maxLevel
     * @return void
     */
    private <T extends NodeInfo> void buildTree(List<Node<T>> treeList, int maxLevel) {
        Node<T> rootNode = treeList.get(0);

        Stack<JpaNode<T>> stack = new Stack<JpaNode<T>>();
        int level = rootNode.getLevel();

        for (Node<T> n : treeList) {
            JpaNode<T> node = (JpaNode<T>) n;

            if (node.getLevel() < level) {
                stack.pop();
            }
            level = node.getLevel();

            if (node != rootNode) {
                JpaNode<T> parent = stack.peek();
                // set parent
                node.internalSetParent(parent);
                // add child to parent
                parent.internalAddChild(node);
                // set ancestors
                node.internalSetAncestors(new ArrayList<Node<T>>(stack));
                // add descendant to all ancestors
                for (JpaNode<T> anc : stack) {
                    anc.internalAddDescendant(node);
                }
            }

            if (node.hasChildren() && (maxLevel == 0 || maxLevel > level)) {
                stack.push(node);
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> List<Node<T>> getRoots() {
        EntityManager em = this.getEntityManager();
        Configuration config = getConfiguration();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Class<T> nodeInfoClass = (Class<T>) getConfiguration().getNodeInfoClass();
        CriteriaQuery<T> cq = cb.createQuery(nodeInfoClass);
        Root<T> queryRoot = cq.from(nodeInfoClass);
        cq.where(cb.equal(queryRoot.get(config.getLeftFieldName()).as(Number.class), 1));
        cq.orderBy(cb.asc(queryRoot.get(config.getRootIdFieldName())));
        List<Node<T>> nodes = new ArrayList<Node<T>>();
        Query query = em.createQuery(cq);
        //query.setHint("javax.persistence.cache.storeMode", "REFRESH");
        //em.getEntityManagerFactory().getCache().evictAll();
        //query.setHint(QueryHints.CACHE_USAGE, CacheUsage.ConformResultsInUnitOfWork);
        List<T> result = query.getResultList();
        for (T n : result) {
            nodes.add(getNode(n));
        }
        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Node<T> createRoot(T root) {
        Configuration config = getConfiguration();
        root.setRootValue(getRoots().size() + 1);
        int maximumRight;
        if (config.hasManyRoots()) {
            maximumRight = 0;
        } else {
            maximumRight = getMaximumRight(root.getClass());
        }
        root.setLeftValue(maximumRight + 1);
        root.setRightValue(maximumRight + 2);
        root.setLevel(0);
        em.persist(root);
        Node<T> node = getNode(root);
        EventBuilder eb = createEventBuilder();
        eb.add(node, EventNode.ADD);
        eb.fireEvent();
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Node<T> getNode(T nodeInfo) {
        Key key = new Key(nodeInfo.getClass(), nodeInfo.getId());
        if (this.nodes.containsKey(key)) {
            @SuppressWarnings("unchecked")
            Node<T> n = (Node<T>) this.nodes.get(key);
            return n;
        }
        Node<T> node = new JpaNode<T>(nodeInfo, this);
        if (!node.isValid()) {
            throw new IllegalArgumentException("The given NodeInfo instance has no position " + "in a tree and is thus not yet a node.");
        }
        this.nodes.put(key, node);
        return node;
    }
    
    /**
     * INTERNAL 
     */

    int getMaximumRight(Class<? extends NodeInfo> clazz) {
        Configuration config = getConfiguration();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<? extends NodeInfo> cq = cb.createQuery(clazz);
        Root<? extends NodeInfo> queryRoot = cq.from(clazz);
        cq.orderBy(cb.desc(queryRoot.get(config.getRightFieldName())));
        List<? extends NodeInfo> highestRows = em.createQuery(cq).setMaxResults(1).getResultList();
        if (highestRows.isEmpty()) {
            return 0;
        } else {
            return highestRows.get(0).getRightValue();
        }
    }

    /**
     * INTERNAL: Applies the root ID criteria to the given CriteriaQuery.
     * 
     * @param cq
     * @param rootId
     */
    void applyRootId(Class<?> clazz, CriteriaQuery<?> cq, int rootId) {
        Configuration config = getConfiguration();
        if (config.getRootIdFieldName() != null) {
            Root<?> root = cq.getRoots().iterator().next();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            Predicate p = cq.getRestriction();
            cq.where(cb.and(p, cb.equal(root.get(config.getRootIdFieldName()), rootId)));
        }
    }

    /**
     * INTERNAL: Updates the left values of all nodes currently known to the
     * manager.
     * 
     * @param minLeft
     *            The lower bound (inclusive) of the left values to update.
     * @param maxLeft
     *            The upper bound (inclusive) of the left values to update.
     * @param delta
     *            The delta to apply on the left values within the range.
     */
    void updateLeftValues(int minLeft, int maxLeft, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getLeftValue() >= minLeft && (maxLeft == 0 || node.getLeftValue() <= maxLeft)) {
                    node.setLeftValue(node.getLeftValue() + delta);
                    ((JpaNode<?>) node).invalidate();
                }
            }
        }
    }

    /**
     * INTERNAL: Updates the right values of all nodes currently known to the
     * manager.
     * 
     * @param minRight
     *            The lower bound (inclusive) of the right values to update.
     * @param maxRight
     *            The upper bound (inclusive) of the right values to update.
     * @param delta
     *            The delta to apply on the right values within the range.
     */
    void updateRightValues(int minRight, int maxRight, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getRightValue() >= minRight && (maxRight == 0 || node.getRightValue() <= maxRight)) {
                    node.setRightValue(node.getRightValue() + delta);
                    ((JpaNode<?>) node).invalidate();
                }
            }
        }
    }

    /**
     * INTERNAL: Updates the level values of all nodes currently known to the
     * manager.
     * 
     * @param left
     *            The lower bound left value.
     * @param right
     *            The upper bound right value.
     * @param delta
     *            The delta to apply on the level values of the nodes within the
     *            range.
     */
    void updateLevels(int left, int right, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getLeftValue() > left && node.getRightValue() < right) {
                    node.setLevel(node.getLevel() + delta);
                    ((JpaNode<?>) node).invalidate();
                }
            }
        }
    }

    void removeNodes(int left, int right, int rootId) {
        Set<Key> removed = new HashSet<Key>();
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getLeftValue() >= left && node.getRightValue() <= right) {
                    removed.add(new Key(node.unwrap().getClass(), node.getId()));
                }
            }
        }
        for (Key k : removed) {
            Node<?> n = this.nodes.remove(k);
            n.setLeftValue(0);
            n.setRightValue(0);
            n.setLevel(0);
            n.setRootValue(0);
            this.em.detach(n.unwrap());
        }
    }

    
    
    protected <T extends NodeInfo> void retrieveAncestors(int entityType, Object entityId, int deep, Set<Node<T>> history) {
        List<Node<T>> elements = find(entityType, entityId, history);

        if (elements.size() == 0 && history.size() == 0) {
            throw new IllegalArgumentException("The given NodeInfo instance has no position in a tree and is thus not yet a node.");
        }

        if (deep == DEPTH_ONE) {
            for (Node<T> e : elements) {
                Node<T> p = e.getParent();
                if (p != null) {
                    history.add(p);
                }
            }
        } else {
            for (Node<T> e : elements) {
                List<Node<T>> ancestors = e.getAncestors();
                for (Iterator<Node<T>> it = ancestors.iterator(); it.hasNext();) {
                    Node<T> a = it.next();
                    if (history.contains(a)) {
                        it.remove();
                    } else {
                        history.add(a);
                    }
                }

                for (Node<T> a : ancestors) {
                    T info = a.unwrap();
                    retrieveAncestors(info.getLinkedTypeCode(), info.getLinkedId(), deep, history);
                }

            }
        }
    }

    protected <T extends NodeInfo> void retrieveDescendants(int entityType, Object entityId, int deep, Set<Node<T>> history) {
        List<Node<T>> elements = find(entityType, entityId, history);

        if (elements.size() == 0 && history.size() == 0) {
            throw new IllegalArgumentException("The given NodeInfo instance has no position in a tree and is thus not yet a node.");
        }

        if (deep == DEPTH_ONE) {
            for (Node<T> e : elements) {
                history.addAll(e.getChildren());
            }
        } else {
            for (Node<T> e : elements) {
                List<Node<T>> descendants = e.getDescendants(deep);
                for (Iterator<Node<T>> it = descendants.iterator(); it.hasNext();) {
                    Node<T> a = it.next();
                    if (history.contains(a)) {
                        it.remove();
                    } else {
                        history.add(a);
                    }
                }
                for (Node<T> a : descendants) {
                    T info = a.unwrap();
                    retrieveDescendants(info.getLinkedTypeCode(), info.getId(), deep, history);
                }
            }
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> List<Node<T>> find(Class<?> refTypeClass, Object refId) {
        Configuration config = getConfiguration();
        return find(config.getLinkedTypeCode(refTypeClass), refId, new HashSet<Node<T>>());
    }

    protected <T extends NodeInfo> List<Node<T>> find(int refType, Object refId, Collection<Node<T>> excludedNodes) {
        Configuration config = getConfiguration();
        EntityManager em = this.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<?> cq = cb.createQuery(config.getNodeInfoClass());
        Root<?> queryRoot = cq.from(config.getNodeInfoClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(queryRoot.get(config.getLinkedTypeCodeFieldName()), refType));
        predicates.add(cb.equal(queryRoot.get(config.getLinkedIdFieldName()), refId));
        if (excludedNodes != null) {
            for (Node<?> n : excludedNodes) {
                predicates.add(cb.notEqual(queryRoot.get("id"), n.unwrap().getId()));
            }
        }
        cq = cq.where(predicates.toArray(new Predicate[predicates.size()]));
        cq = cq.orderBy(cb.asc(queryRoot.get(config.getLeftFieldName())));
        List<Node<T>> nodes = new ArrayList<Node<T>>();
        for (Object n : em.createQuery(cq).getResultList()) {
            Node<T> node = (Node<T>) getNode((T) n);
            if (!node.isRoot()) {
                node.getAncestors();  //fix 1156
            }
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Collection<Node<T>> getAncestors(Class<?> refTypeClass, Object refId) {
        Set<Node<T>> result = new HashSet<Node<T>>();
        retrieveAncestors(getConfiguration().getLinkedTypeCode(refTypeClass), refId, DEPTH_INFINITE, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Collection<Node<T>> getChildren(Class<?> refTypeClass, Object refId) {
        Set<Node<T>> result = new HashSet<Node<T>>();
        retrieveDescendants(getConfiguration().getLinkedTypeCode(refTypeClass), refId, DEPTH_ONE, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Collection<Node<T>> getDescendants(Class<?> refTypeClass, Object refId) {
        Set<Node<T>> result = new HashSet<Node<T>>();
        retrieveDescendants(getConfiguration().getLinkedTypeCode(refTypeClass), refId, DEPTH_INFINITE, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> Collection<Node<T>> getParents(Class<?> refTypeClass, Object refId) {
        Set<Node<T>> result = new HashSet<Node<T>>();
        retrieveAncestors(getConfiguration().getLinkedTypeCode(refTypeClass), refId, DEPTH_ONE, result);
        return result;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> List<T> unwrap(Collection<Node<T>> list) {
        List<T> result = new ArrayList<T>();
        for (Node<T> node : list) {
            result.add(node.unwrap());
        }
        return result;
    }


    protected EventBuilder createEventBuilder() {
        return new EventBuilder(getConfiguration().getId(), this.getListenerProvider());
    }

    public NestedSetListenerProvider getListenerProvider() {
        return listenerProvider;
    }

    public void setListenerProvider(NestedSetListenerProvider listenerProvider) {
        this.listenerProvider = listenerProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo> List<Node<T>> getNodes() {
        List<Node<T>> result = new ArrayList<Node<T>>();
        Configuration config = getConfiguration();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<?> cq = cb.createQuery(config.getNodeInfoClass());
        Root<?> queryRoot = cq.from(config.getNodeInfoClass());
        for (Object n : getEntityManager().createQuery(cq).getResultList()) {
            result.add((Node<T>) getNode((NodeInfo) n));
        }
        return result;
    }
    
    
    Iterator<Node<?>> getCachedNodes() {
    	return nodes.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo, E>  List<E> lookupLinkedObjects(Collection<Node<T>>  list, TypeFilter<E> filter){
        HashMap<String, Node<? extends NodeInfo>> refId2node = new HashMap<String, Node<? extends NodeInfo>>();
        for (Node<? extends NodeInfo> node: list){
            if (filter.accept(node)) {
                refId2node.put(node.getLinkedId(), node);
            }
        }
        List<E> result = new ArrayList<E>(refId2node.size());
        for (Node<? extends NodeInfo> node : refId2node.values()){
            result.add((E)node.lookupLinkedObject());
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NodeInfo, E> E lookupLinkedObject(Node<T> node) {
        Object objectId = convertPrimaryKey(node.getLinkedId(), node.getLinkedTypeClass()); 
        E refObject = (E) getEntityManager().find(node.getLinkedTypeClass(),objectId);
        return refObject;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntityManager(EntityManager entityManager) {
        clear();
        this.em = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBuilder createNodeBuilder() {
        return new JPANodeBuilder(this);
    }
    
    /**
     * Returns to the field that is the primary key, otherwise null
     * 
     * @param entityClass Entity Class 
     * @return Field Primary key field
     */
    public Field findPrimaryKeyField(Class<?> entityClass)  {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                return field;
            }
        }
        Class<?> parent = entityClass.getSuperclass();
        if (parent != null) {
            return findPrimaryKeyField(parent);
        }
        return null;
    }

    /**
     * Convert the primary key serialized into the correct type
     * @param primaryKeyValue primary key serialized
     * @param entityClass the entity class that contains the primary key
     * @return returns the primary key value converted
     */
    public Object convertPrimaryKey(String primaryKeyValue, Class<?> entityClass) {
        Field primaryKeyField = findPrimaryKeyField(entityClass);
        Class<?> typeId = primaryKeyField.getType(); 
        
        if (typeId == String.class) {
            return primaryKeyValue;
        }
        if (typeId == int.class || typeId == Integer.class) {
            return Integer.parseInt(primaryKeyValue);
        }
        if (typeId == long.class || typeId == Long.class){
            return Long.parseLong(primaryKeyValue);
        }
        throw new IllegalArgumentException("The type of the id entity is not supported");
    }

    /**
     * Gets primary key of the entity object
     * @param entity object
     * @return primary key of the entity object
     */
    public String getPrimaryKeyValue(Object entity) {
        Field field = findPrimaryKeyField(entity.getClass());
        if (field != null) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    return value.toString();
                }
            } catch (IllegalArgumentException e) {
                //nothing
            } catch (IllegalAccessException e) {
                //nothing
            }
        }
        return null;
    }
}
