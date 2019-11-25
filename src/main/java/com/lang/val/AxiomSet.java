package com.lang.val;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

public class AxiomSet {
	private final List<Prop> axioms;
	private final ImmutableList<Prop> baseAxioms;
	private final List<Prop> conclusions;
	private List<Prop> colligatedConclusions = Lists.newArrayList();

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
		Prop ax = a.get(0);
		for (int i = 1, ii = a.size(); i < ii; i++) {
			ax = ax.multiply(a.get(i));
		}
		colligatedConclusions.add(ax);
		this.conclusions = conclusions;
	}

	// TODO: incomplete
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

	public List<Prop> getConclusionsOfOrderN(int n, int limit) {
		n--;
		if (n < colligatedConclusions.size()) {
			return getConclusions(colligatedConclusions.get(n), limit);
		}
		Prop x = colligatedConclusions.get(colligatedConclusions.size() - 1);
		n = n - colligatedConclusions.size();
		while (n >= 0) {
			n--;
			x = x.multiply(x);
		}
		colligatedConclusions.add(x);
		return getConclusions(x, limit);
	}

	private List<Prop> getConclusions(Prop p, int limit) {
		List<Prop> ret = Lists.newArrayList();
		if (ret.size() >= limit) {
			return ret;
		}
		List<Prop> bases = p.transmitLastUniveral();
		ret.addAll(bases);
		for (Prop b : bases) {
			List<Prop> m = getConclusions(b, limit - ret.size());
			ret.addAll(m);
			if (ret.size() > limit) {
				break;
			}
		}
		return ret;

	}

	private int deriveConclusions(Prop ax) {
		// TODO Auto-generated method stub
		return 0;
	}

	// TODO: there is a bug in this code
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
