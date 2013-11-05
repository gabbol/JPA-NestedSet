/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.code_factory.jpa.nestedset.model.Member;
import org.testng.annotations.BeforeClass;

/**
 * 
 * @author robo
 * @author gabbol
 */
public abstract class FunctionalNestedSetTest extends TestCase {

	protected EntityManagerFactory emFactory;
	protected EntityManager em;
	private Map<String, Configuration> map = new HashMap<String, Configuration>();

	@BeforeClass(alwaysRun = true)
	protected void createEntityManagerFactory() {
		try {
			emFactory = Persistence.createEntityManagerFactory("TestPU");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected EntityManager getEntityManager(){
		return em;
	}

	@Override
	protected void setUp() throws Exception {
		createEntityManagerFactory();
		createEntityManager();
		Configuration categoryInfo = new Configuration("category", org.code_factory.jpa.nestedset.model.Category.class);
		map.put(categoryInfo.getId(), categoryInfo); 
		Configuration memberInfo = new Configuration(Member.HIERARCHY_ID,org.code_factory.jpa.nestedset.model.Member.class);
		map.put(memberInfo.getId(), memberInfo);
	}

	@Override
	protected void tearDown() throws Exception {
		closeEntityManager();
		closeEntityManagerFactory();
	}

	protected EntityManager createEntityManager() {
		em = emFactory.createEntityManager();
		return em;
	}

	protected JpaNestedSetManager getManager(String id) {
		Configuration configuration = map.get(id);
		JpaNestedSetManager manager = new JpaNestedSetManager(configuration, em);
		manager.setListenerProvider(new NestedSetListenerProviderImpl());
		manager.getListenerProvider().addListener(new PrintNestedListener());
		return manager;
	}

	protected void closeEntityManager() {
		if (em != null) {
			em.close();
		}
	}

	protected void closeEntityManagerFactory() {
		if (emFactory != null) {
			emFactory.close();
		}
	}

	protected void printTree(Node<?> node) {
		printNode(node);
		if (node.hasChildren()) {
			for (Node<?> child : node.getChildren()) {
				printTree(child);
			}
		}
	}

	protected void printNode(Node<?> node) {
		for (int i = 0; i < node.getLevel(); i++) {
			System.out.print("--");
		}
		System.out.println(node.toString());
	}

	/**
	 * Asserts that two objects are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	static public void assertEquals(String message, List<?> expected,
			Object[] actual) {
		if (expected == null && actual == null) {
			return;
		}
		if (expected != null && actual != null) {
			if (expected.size() == actual.length) {
				for (int i = 0; i < actual.length; i++) {
					Assert.assertEquals(message, expected.get(i), actual[i]);
				}
				return;
			}
		}
		throw new AssertionFailedError(message);
	}

	protected String readFile(String fileName) {
		StringBuilder sb = new StringBuilder();
		try {
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(getClass().getResourceAsStream(fileName));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				sb.append(strLine + "\n");
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return sb.toString();

	}

}
