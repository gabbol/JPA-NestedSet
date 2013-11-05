package org.code_factory.jpa.nestedset;

import javax.persistence.EntityManager;

import org.code_factory.jpa.nestedset.model.Member;

public class MoveRootTest extends FunctionalNestedSetTest implements Nodes {

	private void initModel() {
		JpaNestedSetManager nsm = getManager(Member.HIERARCHY_ID);
		EntityManager em = nsm.getEntityManager();
		em.getTransaction().begin();
		TestUtil.deletaAll(em);
		JPANodeBuilderCustom jpaNodeBuilderCustom = new JPANodeBuilderCustom(nsm);
		TestUtil.createLinksModel(em);
		TestUtil.createNodesModel(em,jpaNodeBuilderCustom);
		TestUtil.createNodesModel(em,jpaNodeBuilderCustom);
		TestUtil.createNodesModel(em,jpaNodeBuilderCustom);
		em.flush();
		em.getTransaction().commit();
		em.clear();
		System.out.println(TestUtil.print(nsm));
	}

	protected Node<Member> getNode(JpaNestedSetManager  manager, int id){
		EntityManager em = manager.getEntityManager();
		Member m = em.find(Member.class, id);
		return manager.getNode(m);
	}	
	
	public void testMoveAsFirstChildOf() {
		initModel();
		JpaNestedSetManager nsm = getManager(Member.HIERARCHY_ID);
		EntityManager em = nsm.getEntityManager();
		em.getTransaction().begin();
		
		Node a = getNode(nsm,6); 
		Node b = getNode(nsm, 3);
		a.moveAsFirstChildOf(b);
		System.out.println(TestUtil.print(nsm));

		a = getNode(nsm,27); 
		b = getNode(nsm, 3);
		a.moveAsFirstChildOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,21); 
		b = getNode(nsm, 4);
		a.moveAsFirstChildOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,13); 
		b = getNode(nsm, 24);
		a.moveAsFirstChildOf(b);
		System.out.println(TestUtil.print(nsm));
		
		assertEquals(TestUtil.print(nsm).trim(),readFile("testMoveAsFirstChildOf.txt").trim());
		em.getTransaction().commit();		
	}

	public void testMoveAsLastChildOf() {
		initModel();
		JpaNestedSetManager nsm = getManager(Member.HIERARCHY_ID);
		EntityManager em = nsm.getEntityManager();
		em.getTransaction().begin();
		
		Node a = getNode(nsm,4); 
		Node b = getNode(nsm, 8);
		a.moveAsLastChildOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,13); 
		b = getNode(nsm, 22);
		a.moveAsLastChildOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,24); 
		b = getNode(nsm, 6);
		a.moveAsLastChildOf(b);
		System.out.println(TestUtil.print(nsm));

		assertEquals(TestUtil.print(nsm).trim(),readFile("testMoveAsLastChildOf.txt").trim());
		em.getTransaction().commit();		
	}

	public void testMoveAsNextSiblingOf() {
		initModel();
		JpaNestedSetManager nsm = getManager(Member.HIERARCHY_ID);
		EntityManager em = nsm.getEntityManager();
		em.getTransaction().begin();
		
		Node a = getNode(nsm,3); 
		Node b = getNode(nsm, 2);
		a.moveAsNextSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,9); 
		b = getNode(nsm, 25);
		a.moveAsNextSiblingOf(b);
		System.out.println(TestUtil.print(nsm));

		a = getNode(nsm,9); 
		b = getNode(nsm, 4);
		a.moveAsNextSiblingOf(b);
		System.out.println(TestUtil.print(nsm));

		a = getNode(nsm,12); 
		b = getNode(nsm, 21);
		a.moveAsNextSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,24); 
		b = getNode(nsm, 11);
		a.moveAsNextSiblingOf(b);
		System.out.println(TestUtil.print(nsm));

		assertEquals(TestUtil.print(nsm).trim(),readFile("testMoveAsNextSiblingOf.txt").trim());
		em.getTransaction().commit();
	}

	public void testMoveAsPrevSiblingOf() {
		initModel();
		JpaNestedSetManager nsm = getManager(Member.HIERARCHY_ID);
		EntityManager em = nsm.getEntityManager();
		em.getTransaction().begin();
		
		Node a = getNode(nsm,6); 
		Node b = getNode(nsm, 2);
		a.moveAsPrevSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,13); 
		b = getNode(nsm, 22);
		a.moveAsPrevSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		a = getNode(nsm,24); 
		b = getNode(nsm, 2);
		a.moveAsPrevSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		
		a = getNode(nsm,23); 
		b = getNode(nsm, 1);
		a.moveAsPrevSiblingOf(b);
		System.out.println(TestUtil.print(nsm));
		
		assertEquals(TestUtil.print(nsm).trim(),readFile("testMoveAsPrevSiblingOf.txt").trim());
		em.getTransaction().commit();
	}
}