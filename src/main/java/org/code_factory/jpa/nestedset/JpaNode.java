/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.code_factory.jpa.nestedset.events.EventBuilder;
import org.code_factory.jpa.nestedset.events.EventNode;

/**
 * Implements {@link Node}<br/>
 * A decorator for a {@link NodeInfo} implementation that enriches it with the
 * full API of a node in a nested set tree.
 * 
 * @param <T extends NodeInfo> The wrapped entity type.
 * @author Roman Borschel <roman@code-factory.org>
 * @author gabbol
 */

class JpaNode<T extends NodeInfo> implements Node<T> {
	private static final int PREV_SIBLING = 1;
	private static final int FIRST_CHILD = 2;
	private static final int NEXT_SIBLING = 3;
	private static final int LAST_CHILD = 4;

	/** The wrapped NodeInfo implementor. */
	private T node;
	/** The type of the wrapped instance. */
	private final Class<T> type;
	private CriteriaQuery<T> baseQuery;
	private Root<T> queryRoot;

	/** The JpaNestedSetManager that manages this node. */
	private final JpaNestedSetManager nsm;

	/*
	 * "Caches" of the tree state reachable from this node. These are cleared
	 * whenever the node is rendered invalid due to tree modifications.
	 */
	private List<Node<T>> children;
	private Node<T> parent;
	private List<Node<T>> ancestors;
	private List<Node<T>> descendants;
	private int descendantDepth = 0;

	@SuppressWarnings("unchecked")
	public JpaNode(T node, JpaNestedSetManager nsm) {
		this.node = node;
		this.nsm = nsm;
		this.type = (Class<T>) node.getClass();
	}

	@Override
	public int getId() {
		return this.node.getId();
	}

	@Override
	public int getLeftValue() {
		return this.node.getLeftValue();
	}

	@Override
	public int getRightValue() {
		return this.node.getRightValue();
	}

	@Override
	public int getLevel() {
		return this.node.getLevel();
	}

	@Override
	public int getRootValue() {
		return this.node.getRootValue();
	}

	@Override
	public void setRootValue(int value) {
		this.node.setRootValue(value);
	}

	@Override
	public void setLeftValue(int value) {
		this.node.setLeftValue(value);
	}

	@Override
	public void setRightValue(int value) {
		this.node.setRightValue(value);
	}

	@Override
	public void setLevel(int level) {
		this.node.setLevel(level);
	}

	@Override
	public String toString() {
		return "[Left: " + node.getLeftValue() + ", Right: "
				+ node.getRightValue() + ", Level: " + node.getLevel()
				+ ", NodeInfo: " + node.toString() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return (getRightValue() - getLeftValue()) > 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasParent() {
		return !isRoot();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid() {
		return isValidNode(this);
	}

	private boolean isValidNode(NodeInfo node) {
		return node != null && node.getRightValue() > node.getLeftValue();
	}

	/*
	 * public void setBaseQuery(CriteriaQuery<T> cq) { // The first root must be
	 * of the wrapped node type. this.queryRoot = (Root<T>)
	 * cq.getRoots().iterator().next(); this.baseQuery = cq; }
	 */

	/* public */private CriteriaQuery<T> getBaseQuery() {
		if (this.baseQuery == null) {
			this.baseQuery = nsm.getEntityManager().getCriteriaBuilder()
					.createQuery(type);
			this.queryRoot = this.baseQuery.from(type);
		}
		return this.baseQuery;
	}

	/**
	 * Gets the number of children (direct descendants) of this node.
	 * 
	 * @return The number of children of this node.
	 */
	public int getNumberOfChildren() {
		return getChildren().size();
	}

	/**
	 * Gets the number of descendants (children and their children etc.) of this
	 * node.
	 * 
	 * @return The number of descendants of this node.
	 */
	public int getNumberOfDescendants() {
		return (this.getRightValue() - this.getLeftValue() - 1) / 2;
	}

	/**
	 * Determines if this node is equal to another node.
	 * 
	 * @return bool
	 */
	/*
	 * public boolean isEqualTo(Node<T> node) { return ((this.getLeftValue() ==
	 * node.getLeftValue()) && (this.getRightValue() == node.getRightValue()) &&
	 * (this.getRootValue() == node.getRootValue())); }
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRoot() {
		return getLeftValue() == 1;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @todo Better return an unmodifiable list instead?
	 */
	@Override
	public List<Node<T>> getChildren() {
		if (children == null) {
			children = Collections.unmodifiableList(getDescendants(1));
		}
		return children;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node<T> getParent() {
		if (isRoot()) {
			return null;
		}
		if (this.parent != null) {
			return this.parent;
		}

		CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = getBaseQuery();
		cq.where(cb.lt(queryRoot.get(nsm.getConfiguration().getLeftFieldName())
				.as(Number.class), getLeftValue()), cb.gt(
				queryRoot.get(nsm.getConfiguration().getRightFieldName()).as(
						Number.class), getRightValue()));
		cq.orderBy(cb.asc(queryRoot.get(nsm.getConfiguration()
				.getRightFieldName())));
		nsm.applyRootId(this.type, cq, getRootValue());

		List<T> result = nsm.getEntityManager().createQuery(cq).getResultList();

		this.parent = nsm.getNode(result.get(0));

		return this.parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Node<T>> getDescendants() {
		return getDescendants(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Node<T>> getDescendants(int depth) {
		if (this.descendants != null
				&& (depth == 0 && this.descendantDepth == 0 || depth <= this.descendantDepth)) {
			return this.descendants;
		}

		// TODO: Fill this.children here also?
		CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = getBaseQuery();
		Predicate wherePredicate = cb.and(cb.gt(
				queryRoot.get(nsm.getConfiguration().getLeftFieldName()).as(
						Number.class), getLeftValue()), cb.lt(
				queryRoot.get(nsm.getConfiguration().getRightFieldName()).as(
						Number.class), getRightValue()));

		if (depth > 0) {
			wherePredicate = cb.and(
					wherePredicate,
					cb.le(queryRoot.get(
							nsm.getConfiguration().getLevelFieldName()).as(
							Number.class), getLevel() + depth));
		}
		cq.where(wherePredicate);
		cq.orderBy(cb.asc(queryRoot.get(nsm.getConfiguration()
				.getLeftFieldName())));

		nsm.applyRootId(this.type, cq, getRootValue());

		List<Node<T>> nodes = new ArrayList<Node<T>>();
		for (T n : nsm.getEntityManager().createQuery(cq).getResultList()) {
			nodes.add(nsm.getNode(n));
		}

		this.descendants = nodes;
		this.descendantDepth = depth;

		/*
		 * if (this.descendants.size() > 0) {
		 * this.nsm.buildTree(this.descendants, depth); }
		 */

		return this.descendants;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node<T> addChild(T child) {
		if (child == this.node) {
			throw new IllegalArgumentException(
					"Cannot add node as child of itself.");
		}
		int newLeft = getRightValue();
		int newRight = getRightValue() + 1;
		int newRoot = getRootValue();
		shiftRLValues(newLeft, 0, 2, newRoot);
		child.setLevel(getLevel() + 1);
		child.setLeftValue(newLeft);
		child.setRightValue(newRight);
		child.setRootValue(newRoot);
		// nsm.getEntityManager().refresh(this.node); // the current node is
		// changed in the shift method via sql code. It needs to be refreshed.
		nsm.getEntityManager().persist(child);
		Node<T> node = this.nsm.getNode(child);
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(node, EventNode.ADD);
		eb.fireEvent();
		return node;
	}

	/**
	 * Inserts this node as the previous sibling of the given node.
	 * 
	 * @return void
	 */
	/*private void insertAsPrevSiblingOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot add node as child of itself.");
		}

		int newLeft = dest.getLeftValue();
		int newRight = dest.getLeftValue() + 1;
		int newRoot = dest.getRootValue();

		shiftRLValues(newLeft, 0, 2, newRoot);
		setLevel(dest.getLevel());
		setLeftValue(newLeft);
		setRightValue(newRight);
		setRootValue(newRoot);
		nsm.getEntityManager().persist(this.node);
	}*/

	/**
	 * Inserts this node as the next sibling of the given node.
	 * 
	 * @return void
	 */
	/*private void insertAsNextSiblingOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot add node as child of itself.");
		}

		int newLeft = dest.getRightValue() + 1;
		int newRight = dest.getRightValue() + 2;
		int newRoot = dest.getRootValue();

		shiftRLValues(newLeft, 0, 2, newRoot);
		setLevel(dest.getLevel());
		setLeftValue(newLeft);
		setRightValue(newRight);
		setRootValue(newRoot);
		nsm.getEntityManager().persist(this.node);
	}*/

	/**
	 * Inserts this node as the last child of the given node.
	 * 
	 * @return void
	 */
	/*private void insertAsLastChildOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot add node as child of itself.");
		}

		int newLeft = dest.getRightValue();
		int newRight = dest.getRightValue() + 1;
		int newRoot = dest.getRootValue();

		shiftRLValues(newLeft, 0, 2, newRoot);
		// *setLevel(dest.getLevel() + 1);
		// *setLeftValue(newLeft);
		// *setRightValue(newRight);
		// *setRootValue(newRoot);
		// *nsm.getEntityManager().persist(this.node);
	}*/

	/**
	 * Inserts this node as the first child of the given node.
	 * 
	 * @return void
	 */
	/*private void insertAsFirstChildOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot add node as child of itself.");
		}

		int newLeft = dest.getLeftValue() + 1;
		// int newRight = dest.getLeftValue() + 2;
		int newRoot = dest.getRootValue();

		shiftRLValues(newLeft, 0, 2, newRoot);
		// setLevel(dest.getLevel()+1);
		// setLeftValue(newLeft);
		// setRightValue(newRight);
		// setRootValue(newRoot);
		// nsm.getEntityManager().persist(this.node);
	}*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete() {

		// TODO: Remove deleted nodes that are in-memory from
		// JpaNestedSetManager.
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(this, EventNode.DELETE);

		int oldRoot = getRootValue();
		Configuration cfg = nsm.getConfiguration();
		String rootIdFieldName = cfg.getRootIdFieldName();
		String leftFieldName = cfg.getLeftFieldName();
		String rightFieldName = cfg.getRightFieldName();
		String entityName = cfg.getEntityName();

		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(entityName).append(" n")
				.append(" where n.").append(leftFieldName).append(">= ?1")
				.append(" and n.").append(rightFieldName).append("<= ?2");

		if (rootIdFieldName != null) {
			sb.append(" and n.").append(rootIdFieldName).append("= ?3");
		}

		Query q = nsm.getEntityManager().createQuery(sb.toString());
		q.setParameter(1, getLeftValue());
		q.setParameter(2, getRightValue());
		if (rootIdFieldName != null) {
			q.setParameter(3, oldRoot);
		}
		q.executeUpdate();

		// Close gap in tree
		int first = getRightValue() + 1;
		int delta = getLeftValue() - getRightValue() - 1;
		shiftRLValues(first, 0, delta, oldRoot);
		nsm.removeNodes(getLeftValue(), getRightValue(), oldRoot);
		eb.fireEvent();
	}

	private void makeRoot(int type, Node<T> dest) {

		int delta = getLeftValue() - getRightValue() - 1;
		int first = getRightValue() + 1;
		int shiftRLTree = 0;
		int rootValue = 0;
		int newRootValue = 0;
		switch (type) {
		case NEXT_SIBLING:
			shiftRLTree = dest.getRootValue() + 1;
			rootValue = getRootValue();
			newRootValue = dest.getRootValue() + 1;
			break;
		case FIRST_CHILD:
			shiftRLTree = 1;
			rootValue = getRootValue() + 1;
			newRootValue = 1;
			break;
		case PREV_SIBLING:
			shiftRLTree = dest.getRootValue();
			newRootValue = dest.getRootValue();
			if (getRootValue() < dest.getRootValue()) {
				rootValue = getRootValue();
			} else {
				rootValue = getRootValue() + 1;
			}
			break;
		case LAST_CHILD:
			int nRoots = nsm.getRoots().size();
			rootValue = getRootValue();
			newRootValue = nRoots + 1;
			shiftRLTree = nRoots + 1;
			break;
		}

		Configuration cfg = nsm.getConfiguration();
		String rootIdFieldName = cfg.getRootIdFieldName();
		String leftFieldName = cfg.getLeftFieldName();
		String rightFieldName = cfg.getRightFieldName();
		String levelFieldName = cfg.getLevelFieldName();
		String entityName = cfg.getEntityName();

		// shifts the trees that have a precise value
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(entityName).append(" n").append(" set n.")
				.append(rootIdFieldName).append(" = n.")
				.append(rootIdFieldName).append("+1").append(" where n.")
				.append(rootIdFieldName).append(">= ?1");
		Query q = nsm.getEntityManager().createQuery(sb.toString());
		q.setParameter(1, shiftRLTree);
		q.executeUpdate();

		// extractTree
		sb = new StringBuilder();
		sb.append("update ").append(entityName).append(" n").append(" set")
				.append(" n.").append(leftFieldName).append(" = n.")
				.append(leftFieldName).append(" + ?4").append(",n.")
				.append(rightFieldName).append(" = n.").append(rightFieldName)
				.append(" + ?4").append(",n.").append(levelFieldName)
				.append(" = n.").append(levelFieldName).append(" + ?6")
				.append(",n.").append(rootIdFieldName).append(" = ?5")
				.append(" where n.").append(leftFieldName).append(">= ?1")
				.append(" and n.").append(rightFieldName).append("<= ?2")
				.append(" and n.").append(rootIdFieldName).append("= ?3");

		q = nsm.getEntityManager().createQuery(sb.toString());
		q.setParameter(1, getLeftValue());
		q.setParameter(2, getRightValue());
		q.setParameter(4, -(getLeftValue() - 1));
		q.setParameter(3, rootValue);
		q.setParameter(5, newRootValue);
		q.setParameter(6, -getLevel());
		q.executeUpdate();

		shiftRLValues(first, 0, delta, rootValue);

		nsm.getEntityManager().flush();
		nsm.getEntityManager().clear();
		nsm.clear();

	}

	/**
	 * Adds 'delta' to all Left and right values that are >= 'first' and <=
	 * 'last'. 'delta' can also be negative. If 'last' is 0 it is skipped and
	 * there is no upper bound.
	 * 
	 * @param first
	 *            The first left/right value (inclusive) of the nodes to shift.
	 * @param last
	 *            The last left/right value (inclusive) of the nodes to shift.
	 * @param delta
	 *            The offset by which to shift the left/right values (can be
	 *            negative).
	 * @param rootId
	 *            The root/tree ID of the nodes to shift.
	 */
	private void shiftRLValues(int first, int last, int delta, int rootId) {
		Configuration cfg = nsm.getConfiguration();
		String rootIdFieldName = cfg.getRootIdFieldName();
		String leftFieldName = cfg.getLeftFieldName();
		String rightFieldName = cfg.getRightFieldName();
		String entityName = cfg.getEntityName();

		// Shift left values
		StringBuilder sbLeft = new StringBuilder();
		sbLeft.append("update ").append(entityName).append(" n")
				.append(" set n.").append(leftFieldName).append(" = n.")
				.append(leftFieldName).append(" + ?1").append(" where n.")
				.append(leftFieldName).append(" >= ?2");

		if (last > 0) {
			sbLeft.append(" and n.").append(leftFieldName).append(" <= ?3");
		}

		if (rootIdFieldName != null) {
			sbLeft.append(" and n.").append(rootIdFieldName).append(" = ?4");
		}

		Query qLeft = nsm.getEntityManager().createQuery(sbLeft.toString());
		qLeft.setParameter(1, delta);
		qLeft.setParameter(2, first);
		if (last > 0) {
			qLeft.setParameter(3, last);
		}
		if (rootIdFieldName != null) {
			qLeft.setParameter(4, rootId);
		}
		qLeft.executeUpdate();
		this.nsm.updateLeftValues(first, last, delta, rootId);

		// Shift right values
		StringBuilder sbRight = new StringBuilder();
		sbRight.append("update ").append(entityName).append(" n")
				.append(" set n.").append(rightFieldName).append(" = n.")
				.append(rightFieldName).append(" + ?1").append(" where n.")
				.append(rightFieldName).append(" >= ?2");

		if (last > 0) {
			sbRight.append(" and n.").append(rightFieldName).append(" <= ?3");
		}

		if (rootIdFieldName != null) {
			sbRight.append(" and n.").append(rootIdFieldName).append(" = ?4");
		}

		Query qRight = nsm.getEntityManager().createQuery(sbRight.toString());
		qRight.setParameter(1, delta);
		qRight.setParameter(2, first);
		if (last > 0) {
			qRight.setParameter(3, last);
		}
		if (rootIdFieldName != null) {
			qRight.setParameter(4, rootId); // NO SONAR
		}
		qRight.executeUpdate();
		this.nsm.updateRightValues(first, last, delta, rootId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T unwrap() {
		return this.node;
	}

	/**
	 * Determines if the node is a leaf node.
	 * 
	 * @return TRUE if the node is a leaf, FALSE otherwise.
	 */
	public boolean isLeaf() {
		return (getRightValue() - getLeftValue()) == 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node<T> getFirstChild() {
		if (this.children != null) {
			return this.children.get(0);
		}

		CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = getBaseQuery();
		cq.where(cb.equal(
				queryRoot.get(nsm.getConfiguration().getLeftFieldName()),
				getLeftValue() + 1));

		nsm.applyRootId(this.type, cq, getRootValue());

		return nsm.getNode(nsm.getEntityManager().createQuery(cq)
				.getSingleResult());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node<T> getLastChild() {
		if (this.children != null) {
			return this.children.get(this.children.size() - 1);
		}

		CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = getBaseQuery();
		cq.where(cb.equal(
				queryRoot.get(nsm.getConfiguration().getRightFieldName()),
				getRightValue() - 1));

		nsm.applyRootId(this.type, cq, getRootValue());

		return nsm.getNode(nsm.getEntityManager().createQuery(cq)
				.getSingleResult());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Node<T>> getAncestors() {
		if (this.ancestors != null) {
			return this.ancestors;
		}

		CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<T> cq = getBaseQuery();
		Predicate wherePredicate = cb.and(cb.lt(
				queryRoot.get(nsm.getConfiguration().getLeftFieldName()).as(
						Number.class), getLeftValue()), cb.gt(
				queryRoot.get(nsm.getConfiguration().getRightFieldName()).as(
						Number.class), getRightValue()));

		cq.where(wherePredicate);
		cq.orderBy(cb.asc(queryRoot.get(nsm.getConfiguration()
				.getLeftFieldName())));

		nsm.applyRootId(this.type, cq, getRootValue());

		List<Node<T>> nodes = new ArrayList<Node<T>>();

		for (T n : nsm.getEntityManager().createQuery(cq).getResultList()) {
			nodes.add(nsm.getNode(n));
		}

		this.ancestors = nodes;

		return this.ancestors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDescendantOf(Node<T> subj) {
		return ((getLeftValue() > subj.getLeftValue())
				&& (getRightValue() < subj.getRightValue()) && (getRootValue() == subj
					.getRootValue()));
	}

	public String getPath(String seperator) {
		StringBuilder path = new StringBuilder();
		List<Node<T>> ancestors = getAncestors();
		for (Node<T> ancestor : ancestors) {
			path.append(ancestor.toString()).append(seperator);
		}

		return path.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveAsPrevSiblingOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot move node as previous sibling of itself");
		}
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(this, EventNode.DELETE_MOVE);

		if (dest.isRoot() || dest.getRootValue() != getRootValue()) {
			moveBetweenTrees2(dest, dest.getLeftValue(),  dest.getLevel() - getLevel(),  1);
		} else {
			// Move within the tree
			int oldLevel = getLevel();
			setLevel(dest.getLevel());
			updateNode(dest.getLeftValue(), getLevel() - oldLevel);

		}
		eb.add(this, EventNode.ADD_MOVE);
		eb.fireEvent();
	}

	/**
	 * move node's and its children to location 'destLeft' and update rest of
	 * tree.
	 * 
	 * @param int destLeft destination left value
	 * @param levelDiff
	 */
	private void updateNode(int destLeft, int levelDiff) {
		int left = getLeftValue();
		int right = getRightValue();
		int rootId = getRootValue();
		int treeSize = right - left + 1;

		// Make room in the new branch
		shiftRLValues(destLeft, 0, treeSize, rootId);

		if (left >= destLeft) { // src was shifted too?
			left += treeSize;
			right += treeSize;
		}

		String levelFieldName = nsm.getConfiguration().getLevelFieldName();
		String leftFieldName = nsm.getConfiguration().getLeftFieldName();
		String rightFieldName = nsm.getConfiguration().getRightFieldName();
		String rootIdFieldName = nsm.getConfiguration().getRootIdFieldName();
		String entityName = nsm.getConfiguration().getEntityName();

		// update level for descendants
		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("update ").append(entityName).append(" n")
				.append(" set n.").append(levelFieldName).append(" = n.")
				.append(levelFieldName).append(" + ?1").append(" where n.")
				.append(leftFieldName).append(" > ?2").append(" and n.")
				.append(rightFieldName).append(" < ?3");

		if (rootIdFieldName != null) {
			updateQuery.append(" and n.").append(rootIdFieldName)
					.append(" = ?4");
		}

		Query q = nsm.getEntityManager().createQuery(updateQuery.toString());
		q.setParameter(1, levelDiff);
		q.setParameter(2, left);
		q.setParameter(3, right);
		if (rootIdFieldName != null) {
			q.setParameter(4, rootId);
		}
		q.executeUpdate();
		this.nsm.updateLevels(left, right, levelDiff, rootId);

		// now there's enough room next to target to move the subtree
		shiftRLValues(left, right, destLeft - left, rootId);

		// correct values after source (close gap in old tree)
		shiftRLValues(right + 1, 0, -treeSize, rootId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveAsNextSiblingOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot move node as next sibling of itself");
		}
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(this, EventNode.DELETE_MOVE);
		if (dest.getRootValue() != getRootValue()) {
			moveBetweenTrees2(dest,  dest.isRoot() ? 1 : dest.getRightValue() + 1,  dest.getLevel() - getLevel(), NEXT_SIBLING);
		} else {
			// Move within tree
			int oldLevel = getLevel();
			setLevel(dest.getLevel());
			updateNode(dest.getRightValue() + 1, getLevel() - oldLevel);
		}
		eb.add(this, EventNode.ADD_MOVE);
		eb.fireEvent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveAsFirstChildOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException( "Cannot move node as first child of itself");
		}
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(this, EventNode.DELETE_MOVE);

		if (dest.getRootValue() != getRootValue()) {
			moveBetweenTrees2(dest, dest.getLeftValue() + 1,  dest.getLevel() - getLevel() + 1, JpaNode.FIRST_CHILD);
		} else {
			// Move within tree
			int oldLevel = getLevel();
			setLevel(dest.getLevel() + 1);
			updateNode(dest.getLeftValue() + 1, getLevel() - oldLevel);
			EntityManager em = nsm.getEntityManager();
			em.flush();
			em.clear();
		}
		eb.add(this, EventNode.ADD_MOVE);
		eb.fireEvent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveAsLastChildOf(Node<T> dest) {
		if (dest == this.node) {
			throw new IllegalArgumentException(
					"Cannot move node as first child of itself");
		}
		EventBuilder eb = nsm.createEventBuilder();
		eb.add(this, EventNode.DELETE_MOVE);
		if (dest.getRootValue() != getRootValue()) {
			moveBetweenTrees2(dest, dest.getRightValue(),  dest.getLevel() - getLevel() + 1, LAST_CHILD);
		} else {
			// Move within tree
			int oldLevel = getLevel();
			setLevel(dest.getLevel() + 1);
			updateNode(dest.getRightValue(), getLevel() - oldLevel);
		}
		eb.add(this, EventNode.ADD_MOVE);
		eb.fireEvent();
	}

	/**
	 * Accomplishes moving of nodes between different trees. Used by the move*
	 * methods if the root values of the two nodes are different.
	 * 
	 * @param dest
	 * @param newLeftValue
	 * @param moveType
	 */
	/*private void moveBetweenTrees(Node<T> dest, int newLeftValue, int moveType) {

		Configuration cfg = nsm.getConfiguration();
		String leftFieldName = cfg.getLeftFieldName();
		String rightFieldName = cfg.getRightFieldName();
		String levelFieldName = cfg.getLevelFieldName();
		String rootIdFieldName = cfg.getRootIdFieldName();
		String entityName = cfg.getEntityName();

		// Move between trees: Detach from old tree & insert into new tree
		int newRoot = dest.getRootValue();
		int oldRoot = getRootValue();
		int oldLft = getLeftValue();
		int oldRgt = getRightValue();
		int oldLevel = getLevel();

		// Prepare target tree for insertion, make room
		shiftRLValues(newLeftValue, 0, oldRgt - oldLft - 1, newRoot);

		// Set new root id for this node
		// *setRootValue(newRoot);
		// $this -> _node -> save();
		// Insert this node as a new node
		// *setRightValue(0);
		// *setLeftValue(0);

		switch (moveType) {
		case PREV_SIBLING:
			insertAsPrevSiblingOf(dest);
			break;
		case FIRST_CHILD:
			insertAsFirstChildOf(dest);
			break;
		case NEXT_SIBLING:
			insertAsNextSiblingOf(dest);
			break;
		case LAST_CHILD:
			insertAsLastChildOf(dest);
			break;
		default:
			throw new IllegalArgumentException("Unknown move operation: "
					+ moveType);
		}

		// int diff = oldRgt - oldLft;
		// setRightValue(getLeftValue() + (oldRgt - oldLft));

		int newLevel = getLevel();
		int levelDiff = newLevel - oldLevel;

		// Relocate descendants of the node
		int diff = getLeftValue() - oldLft;

		// Update lft/rgt/root/level for all descendants
		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("update ").append(entityName).append(" n")
				.append(" set n.").append(leftFieldName).append(" = n.")
				.append(leftFieldName).append(" + ?1").append(", n.")
				.append(rightFieldName).append(" = n.").append(rightFieldName)
				.append(" + ?2").append(", n.").append(levelFieldName)
				.append(" = n.").append(levelFieldName).append(" + ?3")
				.append(", n.").append(rootIdFieldName).append(" = ?4")
				.append(" where n.").append(leftFieldName).append(" > ?5")
				.append(" and n.").append(rightFieldName).append(" < ?6")
				.append(" and n.").append(rootIdFieldName).append(" = ?7");

		Query q = nsm.getEntityManager().createQuery(updateQuery.toString());
		q.setParameter(1, diff);
		q.setParameter(2, diff);
		q.setParameter(3, levelDiff);
		q.setParameter(4, newRoot);
		q.setParameter(5, oldLft);
		q.setParameter(6, oldRgt);
		q.setParameter(7, oldRoot);

		q.executeUpdate();

		// Close gap in old tree
		int first = oldRgt + 1;
		int delta = oldLft - oldRgt - 1;
		shiftRLValues(first, 0, delta, oldRoot);
	}*/

	/**
	 * Makes this node a root node. Only used in multiple-root trees.
	 * 
	 * @param newRootId
	 */
	private void moveBetweenTrees2(final Node<T> dest, final int newLeftValue, final int offsetLevel, final int moveType) {
		EntityManager em = nsm.getEntityManager();
		
		Configuration cfg = nsm.getConfiguration();
		String rootIdFieldName = cfg.getRootIdFieldName();
		String leftFieldName = cfg.getLeftFieldName();
		String rightFieldName = cfg.getRightFieldName();
		String levelFieldName = cfg.getLevelFieldName();
		String entityName = cfg.getEntityName();
	
		final int delta = getLeftValue() - getRightValue() - 1; 

		int rootValue = getRootValue();
		int offsetNode = newLeftValue - getLeftValue();
		int newRootValue = dest.getRootValue();
		
		if (dest.isRoot()) {
			if (rootValue > dest.getRootValue()) {
				rootValue++;
			}
			newRootValue = moveType ==  NEXT_SIBLING ?   dest.getRootValue() + 1 :  dest.getRootValue();
			
			StringBuilder sb = new StringBuilder();
			sb.append("update ").append(entityName).append(" n")
					.append(" set n.").append(rootIdFieldName).append(" = n.")
					.append(rootIdFieldName).append("+1").append(" where n.")
					.append(rootIdFieldName).append(">= ?1");
			Query q = nsm.getEntityManager().createQuery(sb.toString());
			q.setParameter(1, newRootValue);
			q.executeUpdate();
		} else {
			// Prepare target tree for insertion, make room
			shiftRLValues(newLeftValue, 0, -delta, dest.getRootValue());
		}
		em.flush();
		em.clear();
		// extractTree
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(entityName).append(" n").append(" set")
				.append(" n.").append(leftFieldName).append(" = n.")
				.append(leftFieldName).append(" + ?4").append(",n.")
				.append(rightFieldName).append(" = n.").append(rightFieldName)
				.append(" + ?4").append(",n.").append(levelFieldName)
				.append(" = n.").append(levelFieldName).append(" + ?6")
				.append(",n.").append(rootIdFieldName).append(" = ?5")
				.append(" where n.").append(leftFieldName).append(">= ?1")
				.append(" and n.").append(rightFieldName).append("<= ?2")
				.append(" and n.").append(rootIdFieldName).append("= ?3");

		Query q = nsm.getEntityManager().createQuery(sb.toString());
		q.setParameter(1, getLeftValue());
		q.setParameter(2, getRightValue());
		q.setParameter(4, offsetNode); // offset tree
		q.setParameter(3, rootValue);
		q.setParameter(5, newRootValue);
		q.setParameter(6, offsetLevel);
		q.executeUpdate();
		em.flush();
		em.clear();
		
		shiftRLValues(getRightValue() + 1, 0, delta, rootValue); // fix source tree
		
		em.flush();
		em.clear();
		/* clear internal cache */
		nsm.clear();
		/* update current node */
		invalidate();
		this.setLevel(getLevel()+offsetLevel);
		this.setLeftValue(getLeftValue()+offsetNode);
		this.setRightValue(getRightValue()+offsetNode);
		this.setRootValue(newRootValue);
		//this.node = (T) em.find(cfg.getNodeInfoClass(), this.unwrap().getId());
	}

	//
	// Internal tree management methods used for preconstructing and
	// invalidating the parts
	// of a tree reachable directly from this node.
	//

	void invalidate() {
		// Clear all local caches of other nodes, so that they're re-evaluated.
		this.children = null;
		this.parent = null;
		this.ancestors = null;
		this.descendants = null;
		this.descendantDepth = 0;
	}

	void internalAddChild(Node<T> child) {
		if (this.children == null) {
			this.children = new ArrayList<Node<T>>();
		}
		this.children.add(child);
	}

	void internalSetParent(Node<T> parent) {
		this.parent = parent;
	}

	void internalAddDescendant(Node<T> descendant) {
		if (this.descendants == null) {
			this.descendants = new ArrayList<Node<T>>();
		}
		this.descendants.add(descendant);
	}

	void internalSetAncestors(List<Node<T>> ancestors) {
		this.ancestors = ancestors;
	}

	@Override
	public int getLinkedTypeCode() {
		return node.getLinkedTypeCode();
	}

	@Override
	public String getLinkedId() {
		return node.getLinkedId();
	}

	@Override
	public void setLinkedTypeCode(int value) {
		node.setLinkedTypeCode(value);
	}

	@Override
	public void setLinkedId(String value) {
		node.setLinkedId(value);
	}

	public Class<?> getLinkedTypeClass() {
		return nsm.getConfiguration().getLinkedTypeClass(getLinkedTypeCode());
	}

	@Override
	public <T extends Object> T lookupLinkedObject() {
		return (T) this.nsm.lookupLinkedObject(this);
	}

	public void accept(NodeVisitor<T> visitor) {
		if (visitor.inNode(this)) {
			for (Node<T> child : getChildren()) {
				child.accept(visitor);
			}
		}
		visitor.outNode(this);
	}
}
