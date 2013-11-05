/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */
package org.code_factory.jpa.nestedset.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link EventNode} representing an event node within the Nested Set.
 */
public class EventNode implements Serializable {
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkedId == null) ? 0 : linkedId.hashCode());
		result = prime * result
				+ ((linkedTypeClass == null) ? 0 : linkedTypeClass.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventNode other = (EventNode) obj;
		if (linkedId == null) {
			if (other.linkedId != null)
				return false;
		} else if (!linkedId.equals(other.linkedId))
			return false;
		if (linkedTypeClass == null) {
			if (other.linkedTypeClass != null)
				return false;
		} else if (!linkedTypeClass.equals(other.linkedTypeClass))
			return false;
		return true;
	}

	private static final long serialVersionUID = 6993373022392922587L;
    
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int CHANGE = 3;
    public static final int ADD_MOVE = 4;
    public static final int DELETE_MOVE = 5;

    private int operation;
    private int nodeId;
    private String linkedId; 
    private Class<?> linkedTypeClass;
    private List<EventNode> children;
    
    public EventNode(int operation, int id, Class<?> refTypeClass, String refId) {
        super();
        this.nodeId = id;
        this.operation = operation;
        this.linkedId = refId; 
    }
    
    /**
     * Returns the id of the node
     * @return node ID
     */
    public int getId() {
        return nodeId;
    }

    /**
     * returns the type of operation
     * @see EventNode 
     * 
     * 
     * @return operation type
     */
    public int getOperation() {
        return operation;
    }

    /**
     * returns the id of the linked entity
     * @return  linked entity ID
     */
    public String getLinkedId() {
        return linkedId;
    }
    
    /**
     * returns the class of the linked entity
     * @return  linked entity class
     */
    public Class<?> getLinkedTypeClass() {
        return linkedTypeClass;
    }

    
    /**
     * @param operation
     *            the operation to set
     */
    public void setOperation(int operation) {
        this.operation = operation;
    }
    
    /**
     * Returns a list of event node 
     * @return
     */
    public List<EventNode> getChildren(){
        if (children == null){
            return Collections.emptyList();
        }
        return children;
    }

    /**
     * adds a event node 
     * @return
     */
    public void addChildren(EventNode child){
        if (children == null){
            children = new ArrayList<EventNode>();
        }
        children.add(child);
    }
    
    public String toString(){
        return toString(0);
    }

    protected String toString(int level){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<level; i++) {
            sb.append('\t');
        }
        switch (operation) {
            case ADD:
                sb.append("+");
                break;
            case ADD_MOVE:
                sb.append("+M");
                break;
            case DELETE_MOVE:
                sb.append("-M");
                break;                
            case DELETE:
                sb.append("-");
                break;
            case CHANGE:
                sb.append("*");
                break;
        }
        sb.append(" " + nodeId);
        sb.append(" ["  + linkedId + "]\n");
        
        for (EventNode eventNode : getChildren()) {
            sb.append(eventNode.toString(level+1));
        }
        return sb.toString();
    }
    
    public void accept(EventNodeVisitor visitor) {
        if (visitor.inNode(this)){
            for (EventNode child : getChildren()) {
                child.accept(visitor);
            }
        }
        visitor.outNode(this);
    }
    
}
