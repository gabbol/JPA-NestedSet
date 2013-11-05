package org.code_factory.jpa.nestedset.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A representation of the model object '<em><b>Group</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity(name="junit_group")
public class Group

{


	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Id()
	private String name = null;

	/**
	 * Returns the value of '<em><b>name</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>name</b></em>' feature
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the '{@link Group#getName() <em>name</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newName
	 *            the new value of the '{@link Group#getName() name}' feature.
	 * @generated
	 */
	public void setName(String newName) {
		name = newName;
	}

}
