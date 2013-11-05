/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.code_factory.jpa.nestedset.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.code_factory.jpa.nestedset.NodeInfo;
import org.code_factory.jpa.nestedset.annotations.LeftColumn;
import org.code_factory.jpa.nestedset.annotations.LevelColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedIdColumn;
import org.code_factory.jpa.nestedset.annotations.LinkedType;
import org.code_factory.jpa.nestedset.annotations.LinkedTypeColumn;
import org.code_factory.jpa.nestedset.annotations.RightColumn;
import org.code_factory.jpa.nestedset.annotations.RootColumn;

/**
 * @author robo
 */
@Entity(name="junit_category")
public class Category implements NodeInfo {
	@Id
	@GeneratedValue
	private int id;
	private String name;

	@Column(updatable = false)
	@LeftColumn
	private int lft;
	@RightColumn
	@Column(updatable = false)
	private int rgt;
	@LevelColumn
	@Column(updatable = false)
	private int level;
	@RootColumn
	private int rootId;

	@LinkedIdColumn
	@Basic()
	private String linkedId = null;

	@LinkedTypeColumn({ @LinkedType(code = 0, entityClass = Group.class),
			@LinkedType(code = 1, entityClass = User.class) })
	@Basic()
	private int linkedTypeCode = 0;

	@Override
	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getLeftValue() {
		return this.lft;
	}

	@Override
	public int getRightValue() {
		return this.rgt;
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	@Override
	public void setLeftValue(int value) {
		this.lft = value;
	}

	@Override
	public void setRightValue(int value) {
		this.rgt = value;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getRootValue() {
		return this.rootId;
	}

	@Override
	public void setRootValue(int value) {
		this.rootId = value;
	}

	@Override
	public String toString() {
		return "[Category: id=" + this.id + ", name=" + this.name + "-"
				+ super.toString() + "]";
	}

	public String getLinkedId() {
		return linkedId;
	}

	public void setLinkedId(String linkedId) {
		this.linkedId = linkedId;
	}

	public int getLinkedTypeCode() {
		return linkedTypeCode;
	}

	public void setLinkedTypeCode(int linkedTypeCode) {
		this.linkedTypeCode = linkedTypeCode;
	}

}
