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
import java.util.List;

/**
 * Encapsulates information describing changes to an nestedSet model, 
 * and is used to notify nestedSet listeners of the change. 
 */

public class NestedSetEvent implements Serializable {
    private static final long serialVersionUID = -2601784350913848837L;
    private final String id;
    private final List<EventNode> roots;
    
    public NestedSetEvent(String id, List<EventNode> roots) {
        super();
        this.id = id;
        this.roots = roots;
    }

    /**
     * 
     * @return configuration ID
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @return a list of event node roots
     */
    public List<EventNode> getRoots() {
        return roots;
    }
}
