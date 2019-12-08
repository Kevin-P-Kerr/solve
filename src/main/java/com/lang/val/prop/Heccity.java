package com.lang.val.prop;

public class Heccity {
	String name;
	int index;

	public Heccity(String n) {
		this.name = n;
	}

	private Heccity(String n, int i) {
		this.name = n;
		this.index = i;
	}

	public Heccity copy() {
		return new Heccity(name, index);
	}

	@Override
	public boolean equals(Object h) {
		if (super.equals(h)) {
			return true;
		}
		if (h instanceof Heccity) {
			Heccity k = (Heccity) h;
			return k.index == index;
		}
		return false;
	}
}