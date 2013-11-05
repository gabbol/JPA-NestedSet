/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.lang.reflect.Field;

import javax.persistence.Entity;

import org.code_factory.jpa.nestedset.annotations.LeftColumn;
import org.code_factory.jpa.nestedset.annotations.LevelColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedIdColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedType;
import org.code_factory.jpa.nestedset.annotations.LinkedTypeColumn;
import org.code_factory.jpa.nestedset.annotations.RightColumn;
import org.code_factory.jpa.nestedset.annotations.RootColumn;

/**
 * A configuration for a class managed by a NestedSetManager.
 * 
 * @author robo
 * @author gabbol
 */
public class Configuration {
    private String id; 
    private final Class<? extends NodeInfo> nodeInfoClass;
    private String leftFieldName;
    private String rightFieldName;
    private String levelFieldName;
    private String rootIdFieldName;
    private String entityName;
    private String linkedIdFieldName;
    private String linkedTypeCodeFieldName;
    private LinkedType[] linkedTypes; 
    private boolean hasManyRoots = false;
    
    
    public Configuration(String id, Class<? extends NodeInfo> nodeInfoClass) {
        super();
        this.id = id;
        this.nodeInfoClass = nodeInfoClass;
        init(nodeInfoClass);
    }
    
    
    /**
     * INTERNAL: Gets the nestedset configuration for the given class.
     * 
     * @param clazz
     * @return The configuration.
     * @throws IllegalArgumentException
     */
    private void init(Class<?> clazz) {
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        setEntityName((name != null && name.length() > 0) ? name : clazz.getSimpleName());
    
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(LeftColumn.class) != null) {
                    setLeftFieldName(field.getName());
                } else if (field.getAnnotation(RightColumn.class) != null) {
                    setRightFieldName(field.getName());
                } else if (field.getAnnotation(LevelColumn.class) != null) {
                    setLevelFieldName(field.getName());
                } else if (field.getAnnotation(RootColumn.class) != null) {
                    setRootIdFieldName(field.getName());
                } else if (field.getAnnotation(LinkedTypeColumn.class) != null) {
                    setLinkedTypeCodeFieldName(field.getName());
                    LinkedTypeColumn refTypeAnnotation = field.getAnnotation(LinkedTypeColumn.class);
                    setLinkedTypes(refTypeAnnotation.value());
                } else if (field.getAnnotation(LinkedIdColumn.class) != null) {
                    setLinkedIdFieldName(field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        if (getLinkedTypeCodeFieldName() == null){
           throw new IllegalArgumentException("Configuration wrong: missing linked type code field");  
        }
        if (getLinkedIdFieldName() == null){
            throw new IllegalArgumentException("Configuration wrong: missing linked id field");
        }        
    }
    
    /**
     * @return the leftFieldName
     */
    public String getLeftFieldName() {
        return leftFieldName;
    }

    /**
     * @param leftFieldName
     *            the leftFieldName to set
     */
    public void setLeftFieldName(String leftFieldName) {
        this.leftFieldName = leftFieldName;
    }

    /**
     * @return the rightFieldName
     */
    public String getRightFieldName() {
        return rightFieldName;
    }

    /**
     * @param rightFieldName
     *            the rightFieldName to set
     */
    public void setRightFieldName(String rightFieldName) {
        this.rightFieldName = rightFieldName;
    }

    /**
     * @return the levelFieldName
     */
    public String getLevelFieldName() {
        return levelFieldName;
    }

    /**
     * @param levelFieldName
     *            the levelFieldName to set
     */
    public void setLevelFieldName(String levelFieldName) {
        this.levelFieldName = levelFieldName;
    }

    /**
     * @return the rootIdFieldName
     */
    public String getRootIdFieldName() {
        return rootIdFieldName;
    }

    /**
     * @param rootIdFieldName
     *            the rootIdFieldName to set
     */
    public void setRootIdFieldName(String rootIdFieldName) {
        this.rootIdFieldName = rootIdFieldName;
        this.hasManyRoots = true;
    }

    public boolean hasManyRoots() {
        return this.hasManyRoots;
    }

    @Override
    public String toString() {
        return "Configuration: " + this.nodeInfoClass.getName();
    }

    /**
     * @return the entity name
     */
    public String getEntityName() {
        return entityName;
    }

    
    /**
     * @param tableName
     *            the tableName to set
     */
    public void setEntityName(String tableName) {
        this.entityName = tableName;
    }
    
    
    /**
     * @return the class given the linked entity code
     */
    public Class<?> getLinkedTypeClass(int code) {
        for ( LinkedType refType : linkedTypes ) {
            if (refType.code() == code) {
                return refType.entityClass();
            }
        }
        throw new IllegalArgumentException("Class Reference Type not found. Code '"+ code+ "' is invalid.");
    }
    
    /**
     * @return the code class given the linked entity class
     */
    public int getLinkedTypeCode(Class<?> entityClass) {
        for ( LinkedType refType : linkedTypes ) {
            if (refType.entityClass().equals(entityClass)) {
                return refType.code();
            }
        }
        throw new IllegalArgumentException("Code Reference Type not found. Class '" + entityClass + "' is invalid.");
    }
    
    
    /**
     * @param linkedTypes
     *            the linkedTypes to set
     */
	public void setLinkedTypes(LinkedType[] linkedTypes) {
		this.linkedTypes = linkedTypes;
	}

	 /**
     * @return the entity name
     */
	public String getLinkedIdFieldName() {
		return linkedIdFieldName;
	}
	
    /**
     * @param linkedIdFieldName
     *            the linkedIdFieldName to set
     */
	public void setLinkedIdFieldName(String linkedIdFieldName) {
		this.linkedIdFieldName = linkedIdFieldName;
	}

	
    /**
     * @return the LinkedTypeCodeFieldName
     */
	public String getLinkedTypeCodeFieldName() {
		return linkedTypeCodeFieldName;
	}
	
	   
    /**
     * @param linkedTypeCodeFieldName
     *            the linkedTypeCodeFieldName to set
     */
	public void setLinkedTypeCodeFieldName(String linkedTypeCodeFieldName) {
		this.linkedTypeCodeFieldName = linkedTypeCodeFieldName;
	}
    
    /**
    * @return the nodeInfo class
    */
    public Class<? extends NodeInfo> getNodeInfoClass() {
        return nodeInfoClass;
    }


    public String getId() {
        return id;
    }

}
