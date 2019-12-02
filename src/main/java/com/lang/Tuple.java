package com.lang;

public class Tuple<L, R> {
	private final L l;
	private final R r;

	public Tuple(L l, R r) {
		this.l = l;
		this.r = r;
	}

	public L getLeft() {
		return l;
	}

	public R getRight() {
		return r;
	}
}
