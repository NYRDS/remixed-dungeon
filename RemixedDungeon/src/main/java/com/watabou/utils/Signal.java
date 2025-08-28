package com.watabou.utils;

import java.util.LinkedList;

public class Signal<T> {

	private final LinkedList<Listener<T>> listeners = new LinkedList<>();
	
	private boolean canceled;
	
	private final boolean stackMode;
	
	public Signal() {
		this( false );
	}
	
	public Signal( boolean stackMode ) {
		this.stackMode = stackMode;
	}
	
	public void add( Listener<T> listener ) {
		if (!listeners.contains( listener )) {
			if (stackMode) {
				listeners.addFirst( listener );
			} else {
				listeners.addLast( listener );
			}
		}
	}
	
	public void remove( Listener<T> listener ) {
		listeners.remove( listener );
	}
	
	public void removeAll() {
		listeners.clear();
	}
	
	public void replace( Listener<T> listener ) {
		removeAll();
		add( listener );
	}
	
	public int numListeners() {
		return listeners.size();
	}
	
	public void dispatch( T t ) {
		
		@SuppressWarnings("unchecked")
		Listener<T>[] list = listeners.toArray(new Listener[0]);
		
		canceled = false;
		for (Listener<T> listener : list) {

			if (listeners.contains(listener)) {
				listener.onSignal(t);
				if (canceled) {
					return;
				}
			}

		}
	}
	
	public void cancel() {
		canceled = true;
	}
	
	public interface Listener<T> {
		void onSignal(T t);
	}
}
