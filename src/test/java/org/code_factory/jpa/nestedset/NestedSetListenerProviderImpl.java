package org.code_factory.jpa.nestedset;

import java.util.ArrayList;
import java.util.List;

import org.code_factory.jpa.nestedset.events.NestedSetEvent;
import org.code_factory.jpa.nestedset.events.NestedSetListener;
import org.code_factory.jpa.nestedset.events.NestedSetListenerProvider;

public class NestedSetListenerProviderImpl implements  NestedSetListenerProvider {
	private List<NestedSetListener> listeners = new ArrayList<NestedSetListener>();

	@Override
	public void fireEvent(NestedSetEvent event) {
		for (NestedSetListener  l : listeners) {
			l.nestedSetChanged(event);
		}
	}

	@Override
	public void addListener(NestedSetListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(NestedSetListener listener) {
		listeners.remove(listener); 
		
	}

}
