package com.lang.val.prop;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.prop.Prop.Quantifier.QuantifierType;

public class Quantifier {
	public enum QuantifierType {
		FORALL, THEREIS
	}

	Quantifier.QuantifierType type;
	String name;
	int index;
	final List<Integer> indicesForTransmission = Lists.newArrayList();

	private Quantifier(Quantifier.QuantifierType t, String name, int index) {
		this.type = t;
		this.name = name;
		this.index = index;
	}

	public static Quantifier newExistential(String name, int index) {
		return new Quantifier(QuantifierType.THEREIS, name, index);
	}

	public static Quantifier newUniversal(String name, int index) {
		return new Quantifier(QuantifierType.FORALL, name, index);
	}

	public static Quantifier newExistential(String name) {
		return new Quantifier(QuantifierType.THEREIS, name, -1);
	}

	public static Quantifier newUniversal(String name) {
		return new Quantifier(QuantifierType.FORALL, name, -1);
	}

	public void setIndex(int i) {
		this.index = i;
	}

	public boolean canTransmitInto(Quantifier q) {
		return indicesForTransmission.contains(q.index);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (type == QuantifierType.FORALL) {
			b.append("forall ");
		} else {
			b.append("thereis ");
		}
		b.append(name);
		return b.toString();
	}

	public Quantifier copy() {
		Quantifier q = new Quantifier(type, name, index);
		for (Integer i : indicesForTransmission) {
			q.indicesForTransmission.add(i);
		}
		return q;
	}

	@Override
	public boolean equals(Object a) {
		if (super.equals(a)) {
			return true;
		}
		if (!(a instanceof Quantifier)) {
			return false;
		}
		Quantifier q = (Quantifier) a;
		return q.index == index && q.type == type;
	}

	public void setIndicesPriorTo(int i) {
		if (type == QuantifierType.THEREIS) {
			return;
		}
		while (i-- >= 0) {
			indicesForTransmission.add(i);
		}

	}
}