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

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof Tuple<?, ?>)) {
			return false;
		}
		Tuple<?, ?> t = (Tuple<?, ?>) o;
		return t.getLeft().equals(getLeft()) && t.getRight().equals(getRight());
	}
}
