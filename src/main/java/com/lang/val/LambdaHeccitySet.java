package com.lang.val;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.prop.AtomicProp;
import com.lang.val.prop.ConjunctProp;
import com.lang.val.prop.Heccity;

public class LambdaHeccitySet {
	List<LambdaHeccity> hecs;
	List<ConjunctProp> conjunctions = Lists.newArrayList();

	public static enum Type {
		ABSTRACT, CONCRETE;
	}

	private final Type type;

	public LambdaHeccitySet(List<LambdaHeccity> l, Type type) {
		this.hecs = l;
		this.type = type;
	}

	public void registerConjunction(ConjunctProp cp) {
		if (!containsOnlyHecs(cp) && type == Type.CONCRETE) {
			return;
		} else if (type == Type.ABSTRACT && !containsAtLeastOne(cp)) {
			return;
		}
		conjunctions.add(cp.copy());
	}

	protected List<LambdaHeccity> getHecs() {
		return hecs;
	}

	private boolean containsOnlyHecs(ConjunctProp cp) {
		for (AtomicProp ap : cp.getAtoms()) {
			for (Heccity h : ap.getHeccesities()) {
				if (getHeccity(h.getIndex()) == null) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean containsAtLeastOne(ConjunctProp cp) {
		for (AtomicProp ap : cp.getAtoms()) {
			for (Heccity h : ap.getHeccesities()) {
				if (getHeccity(h.getIndex()) != null) {
					return true;
				}
			}
		}
		return false;
	}

	public LambdaHeccity getHeccity(int index) {
		for (LambdaHeccity lh : hecs) {
			if (lh.getId() == index) {
				return lh;
			}
		}
		return null;
	}
}
