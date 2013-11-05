package org.code_factory.jpa.nestedset;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.code_factory.jpa.nestedset.model.Group;
import org.code_factory.jpa.nestedset.model.Member;
import org.code_factory.jpa.nestedset.model.User;

public abstract class TestUtil implements Nodes {
	
	static void createLinksModel(EntityManager em) {
		Group g3 = new Group();
		g3.setName(G3);
		Group g4 = new Group();
		g4.setName(G4);
		Group g2 = new Group();
		g2.setName(G2);
		Group g1 = new Group();
		g1.setName(G1);
		em.persist(g4);
		em.persist(g3);
		em.persist(g2);
		em.persist(g1);
		User a = new User();
		a.setName(A);
		User b = new User();
		b.setName(B);
		User c = new User();
		c.setName(C);
		User d = new User();
		d.setName(D);
		User e = new User();
		e.setName(E);
		em.persist(a);
		em.persist(b);
		em.persist(c);
		em.persist(d);
		em.persist(e);
	}

	static void createNodesModel(EntityManager em , NodeBuilder nodeBuilder) {
		
		Group g3 = em.find(Group.class, G3);
		Group g4 = em.find(Group.class, G4);
		Group g2 = em.find(Group.class, G2);
		Group g1 = em.find(Group.class, G1);
		User a = em.find(User.class, A);
		User b = em.find(User.class, B);
		User c = em.find(User.class, C);
		User d = em.find(User.class, D);
		User e = em.find(User.class, E);
		
		Node<Member> g1Node = nodeBuilder.create(g1);
		Node<Member> g2Node = nodeBuilder.create(g1Node, g2);
		Node<Member> g3Node = nodeBuilder.create(g2Node, g3);
		Node<Member> g4Node = nodeBuilder.create(g1Node, g4);
		nodeBuilder.create(g2Node, a);
		nodeBuilder.create(g2Node, b);
		nodeBuilder.create(g2Node, c);
		nodeBuilder.create(g3Node, d);
		nodeBuilder.create(g4Node, d);
		nodeBuilder.create(g4Node, e);
	}

	static void deletaAll(EntityManager em) {
		em.createQuery("DELETE FROM junit_member").executeUpdate();
		em.createQuery("DELETE FROM junit_user").executeUpdate();
		em.createQuery("DELETE FROM junit_group").executeUpdate();
		em.createQuery("DELETE FROM junit_category").executeUpdate();
	}

	static String print(NestedSetManager manager) {
		StringBuilder sb = new StringBuilder();
		List<Node<?>> roots = new ArrayList<Node<?>>();
		roots.addAll(manager.getRoots());
		Collections.sort(roots, new Comparator<Node<?>>() {

			@Override
			public int compare(Node<?> o1, Node<?> o2) {
				return Integer.compare(o1.getRootValue(),o2.getRootValue());
			}
		} );
		print(roots,sb);
		return sb.toString();
	}
	
	private static void print(List<Node<?>> list, StringBuilder sb) {
		String pattern = "Id {0,number,#,00000000} - Root {1,number,#,000} level {2,number,#,000} -------{3} ({5,number,#,000},{6,number,#,000}) : value {4}\n";
		for (Node node : list) {
			StringBuffer levelBuffer = new StringBuffer();
			for (int i=0; i < node.getLevel(); i++) {
				levelBuffer.append("--");
			}
			String result = MessageFormat.format(pattern,node.getId(), node.getRootValue(),node.getLevel(),levelBuffer.toString(),node.getLinkedId(),node.getLeftValue(),node.getRightValue());
			sb.append(result);
			print(node.getChildren(),sb);
		}
		
	}


}
