/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */
package org.code_factory.jpa.nestedset.events;

import java.util.EventListener;

/**
 * 
 * The listener interface for receiving model changed events. <br/>
 * The class that is interested in processing a nested Set changed event should implement this interface. 
 *  
 */
public interface NestedSetListener extends EventListener{
    /**
     * Called when the model has been changed.
     */
    void nestedSetChanged(NestedSetEvent e);
}
