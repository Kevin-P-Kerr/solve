package com.lang.val.prop;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lang.val.LambdaHeccitySet;

public class BooleanPart {
	List<ConjunctProp> conjunctions;

	public BooleanPart(List<ConjunctProp> conjunctions) {
		this.conjunctions = conjunctions;
	}

	@Override
	public boolean equals(Object a) {
		if (super.equals(a)) {
			return true;
		}
		if (!(a instanceof BooleanPart)) {
			return false;
		}
		BooleanPart bp = (BooleanPart) a;
		if (bp.conjunctions.size() != conjunctions.size()) {
			return false;
		}
		for (int i = 0, ii = conjunctions.size(); i < ii; i++) {
			if (!(bp.conjunctions.get(i).equals(conjunctions.get(i)))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, ii = conjunctions.size(); i < ii; i++) {
			sb.append(conjunctions.get(i).toString());
			if (ii - i > 1) {
				sb.append(" + ");
			}
		}
		return sb.toString();
	}

	public BooleanPart copy() {
		List<ConjunctProp> conjs = Lists.newArrayList();
		for (ConjunctProp cj : conjunctions) {
			conjs.add(cj.copy());
		}
		return new BooleanPart(conjs);
	}

	// mutates the object
	public void add(BooleanPart booleanPart) {
		for (ConjunctProp cj : booleanPart.conjunctions) {
			conjunctions.add(cj);
		}
	}

	// multply two boolean parts. this does not copy
	// (a+b)(c+d) = ac + bc + ad + bd.
	public void multiply(BooleanPart b) {
		List<ConjunctProp> ncp = Lists.newArrayList();
		for (ConjunctProp cp : b.conjunctions) {
			for (ConjunctProp ccp : conjunctions) {
				ConjunctProp nccp = ccp.copy();
				nccp.atoms.addAll(cp.atoms);
				ncp.add(nccp);
			}
		}
		this.conjunctions = ncp;
	}

	public void simplify() {
		for (ConjunctProp cp : conjunctions) {
			cp.simplify();
		}
		Set<ConjunctProp> markedForRemoval = Sets.newHashSet();

		for (int i = 0, ii = conjunctions.size(); i < ii; i++) {
			ConjunctProp cp = conjunctions.get(i);
			for (int l = i + 1, ll = conjunctions.size(); l < ll; l++) {
				ConjunctProp ccp = conjunctions.get(l);
				if (ccp.equals(cp)) {
					Prop.d("removing " + cp.toString());
					Prop.d("eq to " + ccp.toString());
					markedForRemoval.add(cp);
				}
			}
		}
		for (ConjunctProp i : markedForRemoval) {
			conjunctions.remove(i);
		}
	}

	public void removeContradictions() {
		List<ConjunctProp> markedForRemoval = Lists.newArrayList();
		for (ConjunctProp cp : conjunctions) {
			if (cp.isContradiction()) {
				markedForRemoval.add(cp);
			}
		}
		for (ConjunctProp cp : markedForRemoval) {
			conjunctions.remove(cp);
		}
	}

	public void replaceHeccity(int to, int from, String name) {
		for (ConjunctProp cp : conjunctions) {
			cp.replaceHecceities(from, to, name);
		}

	}

	public void negate() {
		// ~(a+b) == ~a*~b
		// ~(a*b+c) == ~a*~c + ~b*~c
		List<BooleanPart> c = negateConjucntions();
		BooleanPart x = c.get(0);
		for (int i = 1, ii = c.size(); i < ii; i++) {
			x.multiply(c.get(i));
		}
		this.conjunctions = x.conjunctions;
	}

	private List<BooleanPart> negateConjucntions() {
		List<BooleanPart> ret = Lists.newArrayList();
		for (ConjunctProp cp : conjunctions) {
			List<ConjunctProp> negated = cp.negate();
			ret.add(new BooleanPart(negated));
		}
		return ret;
	}

	void setUpIndex(String name, int i) {
		for (ConjunctProp cp : conjunctions) {
			cp.setUpIndex(name, i);
		}
	}

	void transmitHecName(int index, String s) {
		for (ConjunctProp cp : conjunctions) {
			cp.transmitHecName(index, s);
		}
	}

	public boolean couldContradict(BooleanPart booleanPart, QuantifierPart quantifierPart) {
		for (ConjunctProp cp : conjunctions) {
			for (ConjunctProp ccp : booleanPart.conjunctions) {
				if (cp.couldContradict(ccp, quantifierPart)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasPotentialContradictions(QuantifierPart quantifierPart) {
		int i = 0;
		for (ConjunctProp cp : conjunctions) {
			if (cp.hasPotentialContradictions(quantifierPart)) {
				return true;
			}
			i++;
		}
		return false;
	}

	public void registerLambdaRelations(LambdaHeccitySet lhs) {
		for (ConjunctProp cp : conjunctions) {
			lhs.registerConjunction(cp);
		}

	}

}