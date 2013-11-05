package org.code_factory.jpa.nestedset;

import org.code_factory.jpa.nestedset.model.Member;

public class JPANodeBuilderCustom extends JPANodeBuilder {
	private static int id= 0;
	
	protected <T extends NodeInfo> T createNodeInfo(Object linkedEntity) {
		 T info = super.createNodeInfo(linkedEntity);
		 Member m = (Member) info;
		 id++;
		 m.setId(id);
		 return info;
	}

	public JPANodeBuilderCustom(JpaNestedSetManager manager) {
		super(manager);
		id = 0;
	}
}
