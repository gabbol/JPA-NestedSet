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
 * A filter to select a collection of nodes based on the linked type class
 * 
 * @param <T> The linked entity type.
 * 
 * @author gabbol
 */

public class TypeFilter<T>  {
    
    public final static TypeFilter<Object> EMPTY = new TypeFilter<Object>(Object.class);
        
    public TypeFilter(Class<T> typeClass) {
        super();
        this.typeClass = typeClass;
    }

    private Class<T> typeClass;
    
    public boolean accept(Node<? extends NodeInfo> node) {
        return node.getLinkedTypeClass().equals(typeClass);
    }

}
