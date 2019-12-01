package com.lang.val;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lang.ProofResult;
import com.lang.ProofResult.PROOF_VALUE;

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

	private void generateColligated(int n) {
		Prop x = colligatedConclusions.get(colligatedConclusions.size() - 1);
		n = n - colligatedConclusions.size();
		Prop premier = colligatedConclusions.get(0);
		while (n >= 0) {
			n--;
			x = x.multiply(premier);
			colligatedConclusions.add(x);
		}
	}

	public List<Prop> getConclusionsOfOrderN(int n, int limit) {
		n--;
		if (n < colligatedConclusions.size()) {
			List<Prop> ret = Lists.newArrayList();
			ret.add(colligatedConclusions.get(n));
			ret.addAll(getConclusions(colligatedConclusions.get(n), limit));
			return ret;
		}
		generateColligated(n);
		Prop x = colligatedConclusions.get(n);

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
		List<Prop> bases = p.transmitLastUniveral(limit);
		ret.addAll(bases);
		for (Prop b : bases) {
			Collection<Prop> m = getConclusions(b, limit - ret.size());
			ret.addAll(m);
			if (ret.size() > limit) {
				break;
			}
			limit -= ret.size();
		}
		return ret;

	}

	private static class IntWrapper {
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

	private boolean checkForContradictions(Prop p, IntWrapper limit) {
		Set<Prop> ret = Sets.newHashSet();
		if (ret.size() >= limit.get()) {
			return false;
		}
		List<Prop> bases = p.transmitLastUniveral(limit.get());
		ret.addAll(bases);
		for (Prop x : bases) {
			if (x.isContradiction()) {
				return true;
			}
		}
		limit.decrement(ret.size());
		for (Prop b : bases) {
			boolean iscontra = checkForContradictions(b, limit);
			if (iscontra) {
				return true;
			}
			if (ret.size() > limit.get()) {
				break;
			}
		}
		return false;

	}

	public boolean contradicts(Prop p, int order, int i) {
		order--;
		generateColligated(order);
		Prop x = colligatedConclusions.get(order).multiply(p);
		return checkForContradictions(x, new IntWrapper(i));
	}

	public ProofResult contradicts(Prop toBeProven, int order) {
		if (order < 0) {
			return new ProofResult();
		}
		while (order-- >= 0) {
			for (Prop a : axioms) {
				if (a.couldContradict(toBeProven)) {
					toBeProven = toBeProven.multiply(a);
					toBeProven.simplifyViaContradictions();
					if (toBeProven.isContradiction()) {
						ProofResult pr = new ProofResult();
						pr.setProofValue(PROOF_VALUE.PF_PROVED_TRUE);
						return pr;
					}
				}
			}
		}
		return new ProofResult();
	}

}
