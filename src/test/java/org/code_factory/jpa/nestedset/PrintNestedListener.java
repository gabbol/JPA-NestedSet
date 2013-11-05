package org.code_factory.jpa.nestedset;

import org.code_factory.jpa.nestedset.events.EventNode;
import org.code_factory.jpa.nestedset.events.NestedSetEvent;
import org.code_factory.jpa.nestedset.events.NestedSetListener;

public class PrintNestedListener implements NestedSetListener {
	int level;
	final StringBuffer sb = new StringBuffer();

	@Override
	public void nestedSetChanged(NestedSetEvent e) {
		for (EventNode node : e.getRoots()) {
			System.out.println(node.toString());
		}
	}

}
