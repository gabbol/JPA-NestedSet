package org.code_factory.jpa.nestedset.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.code_factory.jpa.nestedset.NodeInfo;
import org.code_factory.jpa.nestedset.annotations.LeftColumn;
import org.code_factory.jpa.nestedset.annotations.LevelColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedIdColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedType;
import org.code_factory.jpa.nestedset.annotations.LinkedTypeColumn;
import org.code_factory.jpa.nestedset.annotations.RightColumn;
import org.code_factory.jpa.nestedset.annotations.RootColumn;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

/**
 * A representation of the model object '<em><b>Member</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
@Entity(name = "junit_member")
//@Cache(type = CacheType.NONE, expiry = 0, alwaysRefresh = true)
public class Member implements NodeInfo

{
	public final static String HIERARCHY_ID = Member.class.getName();
	/**
	 * @generated
	 */
	private static final long serialVersionUID = 1L;

	public static final String CLASS_ID = "members";

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Id()
	@GeneratedValue(generator = "sample_member")
	@SequenceGenerator(name = "sample_member", sequenceName = "sample_member_seq")
	private int id = 0;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@RootColumn
	@Basic()
	private int rootValue = 0;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@LinkedTypeColumn({ @LinkedType(code = 0, entityClass = Group.class),
			@LinkedType(code = 1, entityClass = User.class) })
	@Basic()
	private int linkedTypeCode = 0;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@LinkedIdColumn
	@Basic()
	private String linkedId = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@LevelColumn
	@Basic()
	private int level = 0;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@LeftColumn
	@Basic()
	private int leftValue = 0;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@RightColumn
	@Basic()
	private int rightValue = 0;

	/**
	 * Returns the value of '<em><b>id</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>id</b></em>' feature
	 * @generated
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the '{@link Member#getId() <em>id</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newId
	 *            the new value of the '{@link Member#getId() id}' feature.
	 * @generated
	 */
	public void setId(int newId) {
		id = newId;
	}

	/**
	 * Returns the value of '<em><b>rootValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>rootValue</b></em>' feature
	 * @generated
	 */
	public int getRootValue() {
		return rootValue;
	}

	/**
	 * Sets the '{@link Member#getRootValue() <em>rootValue</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newRootValue
	 *            the new value of the '{@link Member#getRootValue() rootValue}'
	 *            feature.
	 * @generated
	 */
	public void setRootValue(int newRootValue) {
		rootValue = newRootValue;
	}

	/**
	 * Returns the value of '<em><b>linkedTypeCode</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>linkedTypeCode</b></em>' feature
	 * @generated
	 */
	public int getLinkedTypeCode() {
		return linkedTypeCode;
	}

	/**
	 * Sets the '{@link Member#getLinkedTypeCode() <em>linkedTypeCode</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newLinkedTypeCode
	 *            the new value of the '{@link Member#getLinkedTypeCode()
	 *            linkedTypeCode}' feature.
	 * @generated
	 */
	public void setLinkedTypeCode(int newLinkedTypeCode) {
		linkedTypeCode = newLinkedTypeCode;
	}

	/**
	 * Returns the value of '<em><b>linkedId</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>linkedId</b></em>' feature
	 * @generated
	 */
	public String getLinkedId() {
		return linkedId;
	}

	/**
	 * Sets the '{@link Member#getLinkedId() <em>linkedId</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newLinkedId
	 *            the new value of the '{@link Member#getLinkedId() linkedId}'
	 *            feature.
	 * @generated
	 */
	public void setLinkedId(String newLinkedId) {
		linkedId = newLinkedId;
	}

	/**
	 * Returns the value of '<em><b>level</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>level</b></em>' feature
	 * @generated
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the '{@link Member#getLevel() <em>level</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newLevel
	 *            the new value of the '{@link Member#getLevel() level}'
	 *            feature.
	 * @generated
	 */
	public void setLevel(int newLevel) {
		level = newLevel;
	}

	/**
	 * Returns the value of '<em><b>leftValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>leftValue</b></em>' feature
	 * @generated
	 */
	public int getLeftValue() {
		return leftValue;
	}

	/**
	 * Sets the '{@link Member#getLeftValue() <em>leftValue</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newLeftValue
	 *            the new value of the '{@link Member#getLeftValue() leftValue}'
	 *            feature.
	 * @generated
	 */
	public void setLeftValue(int newLeftValue) {
		leftValue = newLeftValue;
	}

	/**
	 * Returns the value of '<em><b>rightValue</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>rightValue</b></em>' feature
	 * @generated
	 */
	public int getRightValue() {
		return rightValue;
	}

	/**
	 * Sets the '{@link Member#getRightValue() <em>rightValue</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newRightValue
	 *            the new value of the '{@link Member#getRightValue()
	 *            rightValue}' feature.
	 * @generated
	 */
	public void setRightValue(int newRightValue) {
		rightValue = newRightValue;
	}

}
