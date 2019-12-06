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
import com.lang.val.prop.ProofTrace;
import com.lang.val.prop.Prop;

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
		this.conclusions = conclusions;
	}

	private void initColligated() {
		if (colligatedConclusions.size() > 0) {
			return;
		}
		Prop ax = baseAxioms.get(0);
		for (int i = 1, ii = baseAxioms.size(); i < ii; i++) {
			ax = ax.multiply(baseAxioms.get(i));
		}
		colligatedConclusions.add(ax);
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

	private ProofResult findContradiction(List<Prop> props, int order, ProofTrace pt) {
		if (order < 0) {
			ProofResult p = new ProofResult();
			p.setProofValue(PROOF_VALUE.PF_UNPROVED);
			return p;
		}
		List<Prop> collect = Lists.newArrayList();
		for (Prop p : props) {
			for (Prop axiom : baseAxioms) {
				Prop test = p.multiply(axiom);
				if (test.hasPotentialContradictions()) {
					List<Prop> replacements = test.getIterations();
					// to, from
					for (Prop rep : replacements) {
						if (rep.isContradiction()) {
							ProofResult found = new ProofResult();
							found.setProofValue(PROOF_VALUE.PF_PROVED_FALSE);
							return found;
						}
						collect.add(rep);
					}
				}
			}
		}
		return findContradiction(collect, order - 1, pt);
	}

	public ProofResult contradicts(Prop toBeProven, int order) {
		List<Prop> l = Lists.newArrayList();
		l.add(toBeProven);
		ProofTrace pt = new ProofTrace(toBeProven);
		return findContradiction(l, order, pt);
	}

}
