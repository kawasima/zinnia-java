package net.unit8.zinnia;

import java.util.List;

public class FreeList<T> {
	private List<T> freeList;
	int pi;
	int li;
	int size;
	
	public void free() {
		li = pi = 0;
	}
	
	T alloc(int len) {
		T r = new T();
		return r;
	}
	
}
