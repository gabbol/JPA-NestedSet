/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */
package org.code_factory.jpa.nestedset.events;

/**
 * The interface is useful for implementing visitor design pattern
 * 
 * @author gabbol
 * 
 */
public interface EventNodeVisitor {
    
    /**
     * The method is called from {@link EventNode} before inspecting the children
     * 
     * @param node: the node to visit
     * 
     * @return true to go down and visit the children of the node
     */
    boolean inNode(EventNode node);
    
    /**
     * The method is called from {@link EventNode} after inspected the children
     * 
     * @param node: the node to visit
     * 
     * @return
     */
    boolean outNode(EventNode node);

}
