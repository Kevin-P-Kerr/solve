package com.lang.val.prop;

import java.util.List;
import com.google.common.collect.Lists;
import com.lang.Tuple;

public class ConjunctProp {
	final List<AtomicProp> atoms;

	public ConjunctProp(List<AtomicProp> atoms) {
		this.atoms = atoms;
	}

	public boolean hasPotentialContradictions(QuantifierPart quantifierPart) {
		for (int i = 0, ii = atoms.size(); i < ii; i++) {
			AtomicProp ap = atoms.get(i);
			for (int l = i + 1, ll = atoms.size(); l < ll; l++) {
				AtomicProp aap = atoms.get(l);
				if (ap.couldContradict(aap, quantifierPart)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Tuple<Integer, Integer>> fromToL = null;

	public boolean couldContradict(ConjunctProp ccp, QuantifierPart quantifierPart) {
		for (AtomicProp ap : atoms) {
			for (AtomicProp aap : ccp.atoms) {
				if (ap.couldContradict(aap, quantifierPart)) {
					fromToL = ap.getDiscoveredContradiction();
					return true;
				}
			}
		}
		return false;
	}

	public void transmitHecName(int index, String s) {
		for (AtomicProp ap : atoms) {
			ap.transmitHecName(index, s);
		}
	}

	public void setUpIndex(String name, int i) {
		for (AtomicProp ap : atoms) {
			ap.setUpIndex(name, i);
		}
	}

	public List<ConjunctProp> negate() {
		List<ConjunctProp> conjuncts = Lists.newArrayList();
		for (AtomicProp ap : atoms) {
			AtomicProp aap = ap.copy();
			aap.negate = !aap.negate;
			conjuncts.add(new ConjunctProp(Lists.newArrayList(aap)));
		}
		return conjuncts;
	}

	public boolean isContradiction() {
		for (int i = 0, ii = atoms.size(); i < ii; i++) {
			AtomicProp ap = atoms.get(i);
			for (int l = i + 1, ll = atoms.size(); l < ll; l++) {
				AtomicProp aap = atoms.get(l);
				if (ap.contradicts(aap)) {
					return true;
				}
			}
		}
		return false;
	}

	public void simplify() {
		List<AtomicProp> markedForRemoval = Lists.newArrayList();
		for (int i = 0, ii = atoms.size(); i < ii; i++) {
			AtomicProp ap = atoms.get(i);
			for (int l = i + 1, ll = atoms.size(); l < ll; l++) {
				AtomicProp aap = atoms.get(l);
				if (aap.equals(ap)) {
					markedForRemoval.add(aap);
				}
			}
		}
		for (AtomicProp i : markedForRemoval) {
			atoms.remove(i);
		}

	}

	@Override
	public boolean equals(Object anO) {
		if (anO == this) {
			return true;
		}
		if (!(anO instanceof ConjunctProp)) {
			return false;
		}
		ConjunctProp cp = (ConjunctProp) anO;
		if (cp.atoms.size() != atoms.size()) {
			return false;
		}
		for (AtomicProp atom : cp.atoms) {
			if (!atoms.contains(atom)) {
				return false;
			}
		}
		return true;
	}

	public ConjunctProp copy() {
		List<AtomicProp> a = Lists.newArrayList();
		for (AtomicProp at : atoms) {
			a.add(at.copy());
		}
		return new ConjunctProp(a);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, ii = atoms.size(); i < ii; i++) {
			sb.append(atoms.get(i).toString());
			if (ii - i > 1) {
				sb.append("*");
			}
		}
		return sb.toString();
	}

	public void replaceHecceities(int from, int to, String name) {
		for (AtomicProp ap : atoms) {
			ap.replaceHecceities(from, to, name);
		}
	}

	public List<Tuple<Integer, Integer>> getFirstContradiction(QuantifierPart quantifierPart) {
		if (!hasPotentialContradictions(quantifierPart)) {
			return null;
		}
		List<Tuple<Integer, Integer>> local = fromToL;
		fromToL = null;
		return local;
	}

}