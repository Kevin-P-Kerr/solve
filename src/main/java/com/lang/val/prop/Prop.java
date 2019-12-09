package com.lang.val.prop;

import java.util.List;
import com.google.common.collect.Lists;
import com.lang.Tuple;
import com.lang.val.Value;
import com.lang.val.prop.Quantifier.QuantifierType;

public class Prop extends Value {

	private final QuantifierPart quantifierPart;
	private final BooleanPart booleanPart;

	public Prop(QuantifierPart q, BooleanPart b) {
		this.quantifierPart = q;
		this.booleanPart = b;
		this.init();
	}

	private void init() {
		// set up the indices of the heccities
		for (int i = 0, ii = quantifierPart.quantifiers.size(); i < ii; i++) {
			Quantifier q = quantifierPart.quantifiers.get(i);
			String name = q.name;
			q.setIndex(i);
			q.setIndicesPriorTo(i);
			booleanPart.setUpIndex(name, i);
		}

	}

	@Override
	public String toString() {
		return quantifierPart.toString() + ":" + booleanPart.toString() + ".";
	}

	public Prop copy() {
		return new Prop(quantifierPart.copy(), booleanPart.copy());
	}

	// add a prop to this one--return a copy
	public Prop add(Prop b) {
		Prop a = this.copy();
		int offset = a.quantifierPart.getLargestOffset() + 1;
		for (int i = 0, ii = b.quantifierPart.quantifiers.size(); i < ii; i++) {
			Quantifier q = b.quantifierPart.quantifiers.get(i);
			int index = i + offset;
			b.transmitHecceity(index, q.index, q.name);
			q.index = index;
		}
		a.quantifierPart.add(b.quantifierPart);
		a.booleanPart.add(b.booleanPart);
		return a;
	}

	public Prop multiply(Prop b) {
		Prop a = this.copy();
		b = b.copy();

		int offset = a.quantifierPart.getLargestOffset() + 1;
		for (int i = b.quantifierPart.quantifiers.size() - 1, ii = 0; i >= ii; i--) {
			Quantifier q = b.quantifierPart.quantifiers.get(i);
			int index = i + offset;
			b.transmitHecceity(index, q.index, q.name);
			q.index = index;
		}
		a.quantifierPart.add(b.quantifierPart);
		a.booleanPart.multiply(b.booleanPart);
		a.resetHecNames();
		// a.simplify();
		// a.removeContradictions();
		return a;
	}

	private void resetHecNames() {
		UniqueString strMaker = new UniqueString();
		for (Quantifier q : quantifierPart.quantifiers) {
			String s = strMaker.getString();
			q.name = s;
			booleanPart.transmitHecName(q.index, s);
		}
	}

	public void simplify() {
		booleanPart.simplify();
	}

	public void removeContradictions() {
		booleanPart.removeContradictions();
	}

	private static boolean DEBUG = false;

	protected static void d(Object o) {
		if (DEBUG) {
			System.err.println(o.toString());
		}
	}

	public List<Prop> transmitLastUniveral(int limit) {
		d("** doing inference*");
		d("from (below line)");
		d(this);
		Prop base = copy();
		Quantifier q = base.getLastUniversal();
		List<Prop> ret = Lists.newArrayList();
		if (q == null) {
			return ret;
		}
		d("transmitting");
		d(q);
		int count = 0;
		for (Quantifier qq : quantifierPart.quantifiers) {
			if (qq.equals(q) || count++ >= limit) {
				break;
			}
			d("replacing");
			d(qq);
			Prop p = base.copy();
			p.replaceQuantifier(qq.index, q.index, qq.name);
			p.simplify();
			p.removeContradictions();
			d("inferred");
			d(p);
			ret.add(p);
		}
		d("done");
		return ret;
	}

	private void replaceQuantifier(int to, int from, String name) {
		Quantifier qq = null;
		for (Quantifier q : quantifierPart.quantifiers) {
			if (q.index == from) {
				qq = q;
			}
		}
		if (qq != null) {
			quantifierPart.quantifiers.remove(qq);
		}
		booleanPart.replaceHeccity(to, from, name);
	}

	private void transmitHecceity(int to, int from, String name) {
		booleanPart.replaceHeccity(to, from, name);
	}

	private Quantifier getLastUniversal() {
		for (int i = quantifierPart.quantifiers.size(); i > 0; i--) {
			Quantifier q = quantifierPart.quantifiers.get(i - 1);
			if (q.type == QuantifierType.FORALL) {
				return q;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object a) {
		if (super.equals(a)) {
			return true;
		}
		if (!(a instanceof Prop)) {
			return false;
		}
		Prop p = (Prop) a;
		return p.quantifierPart.equals(quantifierPart) && p.booleanPart.equals(booleanPart);
	}

	public Prop negate() {
		Prop r = copy();

		r.quantifierPart.negate();
		r.booleanPart.negate();
		r.init();
		return r;
	}

	// TODO: this doesn't really obey the hascode contract
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public boolean isContradiction() {
		return booleanPart.conjunctions.isEmpty();
	}

	public boolean isInteresting(int n) {
		return quantifierPart.quantifiers.size() <= n;
	}

	public boolean hasPotentialContradictions() {
		return booleanPart.hasPotentialContradictions(quantifierPart);
	}

	public void removeQuantifier(int index) {
		quantifierPart.removeQuantifier(index);
	}

	protected void replaceHeccity(int to, int from, String name) {
		booleanPart.replaceHeccity(to, from, name);
	}

	public Prop replaceHeccity(Quantifier to, Quantifier from) {
		replaceHeccity(to.index, from.index, to.name);
		return this;
	}

	private static List<Prop> getIterations(Prop p, List<Prop> cached) {
		if (p.quantifierPart.quantifiers.size() <= 1) {
			return Lists.newArrayList();
		}
		List<Prop> ret = Lists.newArrayList();
		for (int i = 0, ii = p.quantifierPart.quantifiers.size(); i < ii; i++) {
			Quantifier q = p.quantifierPart.quantifiers.get(i);
			for (int l = i + 1, ll = ii; l < ll; l++) {
				Quantifier qq = p.quantifierPart.quantifiers.get(l);
				if (q.canTransmitInto(qq)) {
					Prop n = p.copy();
					n.replaceHeccity(qq, q);
					n.removeQuantifier(q.index);
					n.removeContradictions();
					if (n.isContradiction()) {
						List<Prop> early = Lists.newArrayList();
						early.add(n);
						return early;
					}
					if (cached.indexOf(n) < 0) {
						ret.add(n);
						cached.add(n);
						ret.addAll(getIterations(n, cached));
					}
				}
				if (qq.canTransmitInto(q)) {
					Prop n = p.copy();
					n.replaceHeccity(q, qq);
					n.removeQuantifier(qq.index);
					n.removeContradictions();
					if (n.isContradiction()) {
						List<Prop> early = Lists.newArrayList();
						early.add(n);
						return early;
					}
					if (cached.indexOf(n) < 0) {
						ret.add(n);
						cached.add(n);
						ret.addAll(getIterations(n, cached));
					}
				}
			}
		}
		return ret;
	}

	public List<Prop> getIterations() {
		return getIterations(this, Lists.newArrayList());
	}

	public List<Integer> getNonContradictedConjunctIndices() {
		List<Integer> nonContradictedIndices = Lists.newArrayList();
		for (int i = 0, ii = booleanPart.conjunctions.size(); i < ii; i++) {
			ConjunctProp cj = booleanPart.conjunctions.get(i);
			if (cj.hasPotentialContradictions(quantifierPart, Lists.newArrayList())) {
				continue;
			}
			nonContradictedIndices.add(i);
		}
		return nonContradictedIndices;
	}

	public Prop produceContradictions(ProofTrace pt) {
		Prop c = copy();
		for (Tuple<Integer, Integer> e : c.getFirstContradictionsIndices()) {
			Quantifier fromQ = c.quantifierPart.getQuantifier(e.getLeft());
			Quantifier toQ = c.quantifierPart.getQuantifier(e.getRight());
			pt.replaceHeccity(toQ.index, fromQ.index, toQ.name, fromQ.name);
			c.replaceHeccity(toQ, fromQ);
			c.quantifierPart.removeQuantifier(fromQ);
		}
		pt.removeContradictions();

		c.removeContradictions();
		if (c.hasPotentialContradictions()) {
			return c.produceContradictions(pt);
		}
		return c;

	}

	private List<Tuple<Integer, Integer>> getFirstContradictionsIndices() {
		return booleanPart.getFirstContradiction(quantifierPart);
	}

	public boolean hasContradictionsAtIndices(List<Integer> unresolved, Prop ax) {
		for (Integer i : unresolved) {
			ConjunctProp cp = booleanPart.conjunctions.get(i);
			if (cp.couldContradictSimply(ax.booleanPart)) {
				return true;
			}
		}
		return false;
	}

	public int getNumberOfExistentials() {
		int i = 0;
		for (Quantifier q : quantifierPart.quantifiers) {
			if (q.type == QuantifierType.THEREIS) {
				i++;
			}
		}
		return i;
	}

	public boolean containsExistential() {
		for (Quantifier q : quantifierPart.quantifiers) {
			if (q.type == QuantifierType.THEREIS) {
				return true;
			}
		}
		return false;
	}
}
