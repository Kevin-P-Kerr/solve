package com.lang.val;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.prop.AtomicProp;
import com.lang.val.prop.ConjunctProp;
import com.lang.val.prop.Heccity;

public class LambdaHeccitySet {
	List<LambdaHeccity> hecs;
	List<ConjunctProp> conjunctions = Lists.newArrayList();

	public LambdaHeccitySet(List<LambdaHeccity> l) {
		this.hecs = l;
	}

	public void registerConjunction(ConjunctProp cp) {
		if (containsOnlyHecs(cp)) {
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

	public LambdaHeccity getHeccity(int index) {
		for (LambdaHeccity lh : hecs) {
			if (lh.getId() == index) {
				return lh;
			}
		}
		return null;
	}
}
