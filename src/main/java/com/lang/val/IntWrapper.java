package com.lang.val;

class IntWrapper {
	private int i;

	public IntWrapper(int x) {
		i = x;
	}

	public void decrement(int y) {
		i = i - y;
	}

	public void increment(int y) {
		i += y;
	}

	public int get() {
		return i;
	}
}