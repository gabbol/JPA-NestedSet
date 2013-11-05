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
 *  {@link NodeBuilder} allows the creation of a Nested Set starting from the linked entity <br/>
 *  {@link NodeBuilder} is created using {@link NestedSetManager}.
 *  
 *  <pre>
 *  NestedSetManager manager = ...
 *  Group rootGroup = ...
 *  Group childGroup = ...
 *  
 *  INodeBuilder builder = manager.createNodeBuilder();
 *  Node rootNode = builder.create(rootGroup);
 *  builder.create(rootNode, childGroup);
 *  </pre>
 *  
 *  @author gabbol 
 */
public interface NodeBuilder {
    
    /**
     *  creates a root node given linked entity
     *  
     *  @param linkedEntity linked entity 
     *  @return a root node
     */
    public <T extends NodeInfo>  Node<T> create(Object linkedEntity);
    
    
    
    /**
     *  creates a child node given the linked entity
     *  
     *  @param linkedEntity linked entity 
     *  @param parent the parent node of the node to create
     *  @return a child node
     */
    public <T extends NodeInfo>  Node<T> create(Node<T> parent, Object linkedEntity);
   
    /**
     *  creates a child node given the linked entity
     *  
     *  @param linkedEntity linked entity 
     *  @param parentId the parent node ID of the node to create
     *  @return a child node
     */
    public <T extends NodeInfo>  Node<T> create(Integer parentId, Object linkedEntity);
  
    
    

}
