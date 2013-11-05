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
 * Implements {@link NodeBuilder}
 * @author gabbol
 *
 */
public class JPANodeBuilder implements NodeBuilder{
    private JpaNestedSetManager manager;
    
    public JPANodeBuilder(JpaNestedSetManager manager){
        this.manager = manager;
    }
    
    protected <T extends NodeInfo> T createNodeInfo(Object linkedEntity) {
        try {
            Configuration configuration = manager.getConfiguration();
            T model = (T) configuration.getNodeInfoClass().newInstance();
            model.setLinkedTypeCode(configuration.getLinkedTypeCode(linkedEntity.getClass()));
            model.setLinkedId(manager.getPrimaryKeyValue(linkedEntity));
            return model;
        } catch (InstantiationException e) {
           throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public <T extends NodeInfo>  Node<T> create(Object linkedEntity) {
         T nodeInfo = (T) createNodeInfo(linkedEntity);
         return manager.createRoot(nodeInfo);
    } 
    
    public <T extends NodeInfo>  Node<T> create(Node<T> parent, Object linkedEntity) {
        T nodeInfo = (T) createNodeInfo(linkedEntity);
        return parent.addChild(nodeInfo);
    }

    @Override
    public <T extends NodeInfo> Node<T> create(Integer parentId, Object linkedEntity) {
        Class<T> clazz = (Class<T>) manager.getConfiguration().getNodeInfoClass();
        T nodeInfo = (T) manager.getEntityManager().find(clazz, parentId);
        return create(manager.getNode(nodeInfo), linkedEntity);
    }

	protected JpaNestedSetManager getManager() {
		return manager;
	} 
       
}
