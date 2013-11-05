/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * A <tt>NestedSetManager</tt> is used to read and manipulate the nested set tree structure of
 * classes that implement {@link NodeInfo} using and where each instance thus has a position in a
 * nested set tree.
 *
 * @author Roman Borschel <roman@code-factory.org>
 * @author gabbol
 */
public interface NestedSetManager {
    
    /**
     * Clears the NestedSetManager, removing all managed nodes from the <tt>NestedSetManager</tt>.
     * Any entities wrapped by such nodes are not detached from the underlying <tt>EntityManager</tt>.
     *
     * @return void
     */
    void clear();

    /**
     * Creates a root node for the given NodeInfo instance.
     *
     * @param <T>
     * @param root
     * @return The created node instance.
     */
    <T extends NodeInfo> Node<T> createRoot(T root);

    /**
     * Gets the EntityManager used by this NestedSetManager.
     *
     * @return The EntityManager.
     */
    EntityManager getEntityManager();
    
    /**
     * Sets the EntityManager used by this NestedSetManager.
     *
     * @param The EntityManager.
     */
    void setEntityManager(EntityManager entityManager);

    /**
     * Gets the node that represents the given NodeInfo instance in the tree.
     *
     * @param <T>
     * @param nodeInfo
     * @return The node.
     */
    <T extends NodeInfo> Node<T> getNode(T nodeInfo);

    /**
     * Gets a collection of all nodes currently managed by the NestedSetManager.
     *
     * @return The collection of nodes.
     */
    <T extends NodeInfo> Collection<Node<T>> getNodes();
    
    /**
     * remove all nodes
     */
    void deleteAll();
    
    /**
     * returns root nodes
     *
     * @return The collection of nodes.
     */
    <T extends NodeInfo> List<Node<T>> getRoots();
    
      
    /**
     * Unwraps node list, returning wrapped object list.
     *
     * @return The wrapped object list.
     */
    <T extends NodeInfo> List<T> unwrap(Collection<Node<T>> list);
    
    /**
     * Gets all ancestors which have the property linkedTypeClass and linkedId
     * 
     * @return list a list of nodes
     * @param linkedTypeClass Class type of the linked entity   
     * @param linkedId the ID of the entity linked  
     * 
     */
    <T extends NodeInfo> Collection<Node<T>> getAncestors(Class<?> linkedTypeClass, Object linkedId);

    /**
     * Gets all children (first level) which have the property linkedTypeClass and linkedId
     * 
     * @return list a node list
     * @param linkedTypeClass Class type of the entity linked  
     * @param linkedId the ID of the entity linked  
     * 
     */
    <T extends NodeInfo> Collection<Node<T>> getChildren(Class<?> linkedTypeClass, Object linkedId);

    /**
     * Gets all descendants which have the property linkedTypeClass and linkedId
     * 
     * @return list a node list
     * @param linkedTypeClass Class type of the entity linked  
     * @param linkedId the ID of the entity linked  
     * 
     */
    <T extends NodeInfo> Collection<Node<T>> getDescendants(Class<?> linkedTypeClass, Object linkedId);

    /**
     * Gets all parents which have the property linkedTypeClass and linkedId
     * 
     * @return list a node list
     * @param linkedTypeClass Class type of the entity linked  
     * @param linkedId the ID of the entity linked  
     * 
     */
    <T extends NodeInfo> Collection<Node<T>> getParents(Class<?> linkedTypeClass, Object linkedId);
    
    /**
     * Search all the nodes which have the property linkedTypeClass and linkedId
     * 
     * @return list a node list
     * @param filter to apply to the list of nodes
     * 
     */
    <T extends NodeInfo>  List<Node<T>> find(Class<?> linkedTypeClass, Object linkedId);

    /**
     * Gets a list of linked entities given the list of NodeInfo 
     * The list can be filtered
     * 
     * @param list a node list
     * @param filter to apply to the list of nodes
     * @return The collection of linked entities.
     * 
     */
    <T extends NodeInfo, E>  List<E> lookupLinkedObjects(Collection<Node<T>>  list, TypeFilter<E> filter);

    /**
     * Gets the linked entity give the node
     * 
     * @param a node
     * @return The linked entity.
     * 
     */
    <T extends NodeInfo, E> E lookupLinkedObject(Node<T> node);
    
    /**
     * Returns a node builder to create a Nested Set 
     * 
     * @return a node builder
     * 
     */
    NodeBuilder createNodeBuilder();
}
