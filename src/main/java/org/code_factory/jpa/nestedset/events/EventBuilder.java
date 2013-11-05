/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.code_factory.jpa.nestedset.Node;

/**
 * EventBuilder is a utility class to create the events of the changes in a nested Set
 * 
 * @author gabbol
 *
 */
public class EventBuilder  {
    private Map<Integer, EventNode> roots = new HashMap<Integer, EventNode>();
    private NestedSetListenerProvider provider; 
    private String id;
    
	public EventBuilder(String id, NestedSetListenerProvider provider){
	    this.provider = provider;
	    this.id = id;
	}
	
	/**
	 * create an event given node and the operation done
	 * The eventType should be one of 
	 * <code>@see {@link EventNode}.ADD</code>,
	 * <code>@see {@link EventNode}.DELETE</code>,
	 * <code>@see {@link EventNode}.CHANGE</code>,
	 * <code>@see {@link EventNode}.MOVE_ADD</code>,
	 * <code>@see {@link EventNode}.MOVE_DELETE</code>,
	 * 
	 * @param node the node involved
	 * @param eventType type of operation performed
	 * 
	 * 
	 */
	public void add(Node<?> node, int eventType) {
		EventNode eventNode = new EventNode(eventType, node.getId(), node.getLinkedTypeClass(), node.getLinkedId());
		createChildrenPaths(eventNode, node);
		EventNode newEventNode = createParentParent(eventNode, node);
		if (newEventNode == null) {
			newEventNode = eventNode;
		}
		EventNode currentEventNode = roots.get(newEventNode.getId());
		if (currentEventNode != null) {
			merge(currentEventNode, newEventNode);
		} else {
			roots.put(newEventNode.getId(), newEventNode);
		}
	}
    
	/**
	 * Merge two events
	 * @param currentEventNode the node already exists
	 * @param newEventNode the new node to be added
	 */
    protected void merge(EventNode currentEventNode, EventNode newEventNode) {
        if (currentEventNode.getOperation() != newEventNode.getOperation()) {
            if (currentEventNode.getOperation() == EventNode.ADD || currentEventNode.getOperation() == EventNode.DELETE) {
                currentEventNode.setOperation(EventNode.CHANGE);
            }
        }
        for (EventNode childAddNode : newEventNode.getChildren()) {
            int index = currentEventNode.getChildren().indexOf(childAddNode);
            if (index >= 0) {
                merge(currentEventNode.getChildren().get(index), childAddNode);
            } else {
                currentEventNode.addChildren(childAddNode);
            }
        }
    }

    
	protected void createChildrenPaths(EventNode eventNode, Node<?> node) {
		for (Node<?> childNode : node.getChildren()) {
			EventNode eventChildNode = new EventNode(eventNode.getOperation(),childNode.getId(), childNode.getLinkedTypeClass(), childNode.getLinkedId());
			eventNode.addChildren(eventChildNode);
			createChildrenPaths(eventChildNode, childNode);
		}
	}

	protected EventNode createParentParent(EventNode eventChildNode, Node<?> childNode) {
		EventNode currentNode = eventChildNode;
		List<?> ancestors = childNode.getAncestors();
		EventNode eventParentNode = null;
		Node<?> parentNode = null;
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			parentNode = (Node<?>) ancestors.get(i);
			eventParentNode = new EventNode(EventNode.CHANGE, parentNode.getId(), parentNode.getLinkedTypeClass(), parentNode.getLinkedId());
			eventParentNode.addChildren(currentNode);
			currentNode = eventParentNode;
		}
		return eventParentNode;
	}
	
	/**
	 *  Notify the event to the listeners 
	 * 
	 **/
	public void fireEvent() {
		if (roots.size() > 0 && provider  != null) {
			List<EventNode> result = new ArrayList<EventNode>(roots.size());
			result.addAll(roots.values());
			NestedSetEvent event = new NestedSetEvent(id, result);
			provider.fireEvent(event);
		}
	}



}
