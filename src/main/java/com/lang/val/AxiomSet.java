package com.lang.val;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

public class AxiomSet {
	private final List<Prop> axioms;
	private final ImmutableList<Prop> baseAxioms;
	private final List<Prop> conclusions;

	public AxiomSet(List<Prop> a) {
		this.axioms = a;
		Builder<Prop> b = ImmutableList.builder();
		for (Prop ax : a) {
			b.add(ax);
		}
		this.baseAxioms = b.build();
		List<Prop> conclusions = Lists.newArrayList();
		for (Prop ax : a) {
			conclusions.add(ax);
		}
		this.conclusions = conclusions;
	}

	public void enumerateFirstNConclusions(int i) {
		while (i < conclusions.size()) {
			int n = multiplyAxiomsAndAddToConclusions();
			if (n >= i) {
				break;
			}
			for (Prop ax : axioms) {
				n += deriveConclusions(ax);
				if (n >= i) {
					break;
				}
			}
		}
	}

	private int deriveConclusions(Prop ax) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int multiplyAxiomsAndAddToConclusions() {
		List<Prop> newAxioms = Lists.newArrayList();
		for (int i = 0, ii = axioms.size(); i < ii; i++) {
			Prop ax = axioms.get(i);
			for (int l = i + 1, ll = axioms.size(); l < ll; l++) {
				Prop aax = axioms.get(l);
				Prop na = ax.multiply(aax);
				newAxioms.add(na);
			}
		}
		axioms.addAll(newAxioms);
		conclusions.addAll(newAxioms);
		return conclusions.size();
	}
}
