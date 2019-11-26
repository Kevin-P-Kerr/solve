package com.lang.val;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

	public List<Prop> getConclusionsOfOrderN(int n, int limit) {
		n--;
		if (n < colligatedConclusions.size()) {
			List<Prop> ret = Lists.newArrayList();
			ret.add(colligatedConclusions.get(n));
			ret.addAll(getConclusions(colligatedConclusions.get(n), limit));
			return ret;
		}
		Prop x = colligatedConclusions.get(colligatedConclusions.size() - 1);
		n = n - colligatedConclusions.size();
		Prop premier = colligatedConclusions.get(0);
		while (n >= 0) {
			n--;
			x = x.multiply(premier);
			colligatedConclusions.add(x);
		}
		List<Prop> ret = Lists.newArrayList();
		ret.add(x);
		ret.addAll(getConclusions(x, limit));
		return ret;
	}

	private Collection<Prop> getConclusions(Prop p, int limit) {
		Set<Prop> ret = Sets.newHashSet();
		if (ret.size() >= limit) {
			return ret;
		}
		List<Prop> bases = p.transmitLastUniveral();
		ret.addAll(bases);
		for (Prop b : bases) {
			Collection<Prop> m = getConclusions(b, limit - ret.size());
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

}
