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
 * Interface for NestedSet events.
 * 
 * @author gabriele
 */
public interface NestedSetListenerProvider {
    /**
     * Notifies that the NestedSet has changed.
     *
     * @param event event object describing the change
     */
    void fireEvent(NestedSetEvent event);
    
    /**
     * Adds a listener for NestedSet changes in this provider.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener the listener to be added
     */
    void addListener(NestedSetListener listener);
    
    /**
     * Removes the given NestedSet change listener from this provider.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the listener to be removed
     */
    void removeListener(NestedSetListener listener);
}
