/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */
package org.code_factory.jpa.nestedset;

/**
 * The interface is useful for implementing visitor design pattern
 * 
 * @author gabbol
 * 
 */
public interface NodeVisitor<T extends NodeInfo> {
    
    /**
     * The method is called from {@link Node} before inspecting the children
     * 
     * @param node: the node to visit
     * 
     * @return true to go down and visit the children of the node
     */
    boolean inNode(Node<T> node);
    
    /**
     * The method is called from {@link Node} after inspected the children
     * 
     * @param node: the node to visit
     * 
     * @return
     */
    boolean outNode(Node<T> node);

}
