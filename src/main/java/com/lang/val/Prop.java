package com.lang.val;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.Prop.Quantifier.QuantifierType;

public class Prop extends Value {

	private static class UniqueString {
		private int c = 0;
		private static String[] alpha = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
				"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z " };

		public String getString() {
			int mod = alpha.length;
			int i = c;
			c++;
			int digits = 1;
			int m = mod;
			while (m < i) {
				m = m * mod;
				if (m > i) {
					m = m / mod;
					digits++;
					break;
				}
				digits++;
			}
			String ret = "";
			while (digits > 0) {
				if (i > m) {
					int index = i / m;
					i = i % m;
					ret += alpha[index - 1];
				} else {
					int index = i % m;
					ret += alpha[index];
				}
				m = m / mod;
				digits--;
			}
			return ret;
		}

	}

	public static class Quantifier {
		public enum QuantifierType {
			FORALL, THEREIS
		}

		private final QuantifierType type;
		private String name;

		private Quantifier(QuantifierType t, String name) {
			this.type = t;
			this.name = name;
		}

		public static Quantifier newExistential(String name) {
			return new Quantifier(QuantifierType.THEREIS, name);
		}

		public static Quantifier newUniversal(String name) {
			return new Quantifier(QuantifierType.FORALL, name);
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			if (type == QuantifierType.FORALL) {
				b.append("forall ");
			} else {
				b.append("thereis ");
			}
			b.append(name);
			return b.toString();
		}

		public Quantifier copy() {
			return new Quantifier(type, name);
		}

	}

	public static class BooleanPart {
		private List<ConjunctProp> conjunctions;

		public BooleanPart(List<ConjunctProp> conjunctions) {
			this.conjunctions = conjunctions;
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
			List<Integer> markedForRemoval = Lists.newArrayList();
			for (int i = 0, ii = conjunctions.size(); i < ii; i++) {
				ConjunctProp cp = conjunctions.get(i);
				for (int l = 0, ll = conjunctions.size(); l < ll; l++) {
					if (l == i) {
						continue;
					}
					ConjunctProp ccp = conjunctions.get(l);
					if (ccp.equals(cp)) {
						markedForRemoval.add(l);
					}
				}
			}
			for (Integer i : markedForRemoval) {
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

		public void replaceHeccity(String to, String from) {
			for (ConjunctProp cp : conjunctions) {
				cp.replaceHecceities(from, to);
			}

		}
	}

	public static class ConjunctProp {
		private final List<AtomicProp> atoms;

		public ConjunctProp(List<AtomicProp> atoms) {
			this.atoms = atoms;
		}

		public boolean isContradiction() {
			for (int i = 0, ii = atoms.size(); i < ii; i++) {
				AtomicProp ap = atoms.get(i);
				for (int l = 0, ll = atoms.size(); l < ll; l++) {
					if (l == i) {
						continue;
					}
					AtomicProp aap = atoms.get(l);
					if (ap.contradicts(aap)) {
						return true;
					}
				}
			}
			return false;
		}

		public void simplify() {
			List<Integer> markedForRemoval = Lists.newArrayList();
			for (int i = 0, ii = atoms.size(); i < ii; i++) {
				AtomicProp ap = atoms.get(i);
				for (int l = 0, ll = atoms.size(); l < ll; l++) {
					if (l == i) {
						continue;
					}
					AtomicProp aap = atoms.get(l);
					if (aap.equals(ap)) {
						markedForRemoval.add(l);
					}
				}
			}
			for (Integer i : markedForRemoval) {
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

		public void replaceHecceities(String oldName, String name) {
			for (AtomicProp ap : atoms) {
				ap.replaceHecceities(oldName, name);
			}
		}

	}

	public static class AtomicProp {
		private final boolean negate;
		private final String name;
		private final List<Heccity> heccesities;

		public AtomicProp(boolean n, String name, List<Heccity> h) {
			this.negate = n;
			this.name = name;
			this.heccesities = h;
		}

		public boolean contradicts(AtomicProp ap) {
			if (ap == this) {
				return false;
			}
			if (ap.negate == negate) {
				return false;
			}
			for (Heccity h : heccesities) {
				if (!ap.heccesities.contains(h)) {
					return false;
				}
			}
			return true;
		}

		public void replaceHecceities(String oldName, String name2) {
			for (Heccity h : heccesities) {
				if (h.name.equals(oldName)) {
					h.name = name2;
				}
			}
		}

		@Override
		public boolean equals(Object anO) {
			if (anO == this) {
				return true;
			}
			if (!(anO instanceof AtomicProp)) {
				return false;
			}
			AtomicProp ap = (AtomicProp) anO;
			if (ap.negate != negate) {
				return false;
			}
			if (!ap.name.equals(name)) {
				return false;
			}
			for (Heccity h : heccesities) {
				if (!ap.heccesities.contains(h)) {
					return false;
				}
			}
			return true;
		}

		public AtomicProp copy() {
			List<Heccity> hh = Lists.newArrayList();
			for (Heccity h : heccesities) {
				hh.add(h.copy());
			}
			return new AtomicProp(negate, name, hh);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (negate) {
				sb.append("~");
			}
			sb.append(name);
			sb.append("(");
			for (int i = 0, ii = heccesities.size(); i < ii; i++) {
				sb.append(heccesities.get(i).name);
				if (ii - i > 1) {
					sb.append(" ");
				}
			}
			sb.append(")");
			return sb.toString();
		}
	}

	public static class Heccity {
		private String name;

		public Heccity(String n) {
			this.name = n;
		}

		public Heccity copy() {
			return new Heccity(name);
		}

		@Override
		public boolean equals(Object h) {
			if (super.equals(h)) {
				return true;
			}
			if (h instanceof Heccity) {
				Heccity k = (Heccity) h;
				return k.name.equals(name);
			}
			return false;
		}
	}

	public static class QuantifierPart {
		private List<Quantifier> quantifiers;

		public QuantifierPart(List<Quantifier> q) {
			this.quantifiers = q;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (Quantifier q : quantifiers) {
				b.append(q.toString());
				b.append(" ");
			}
			return b.toString();
		}

		public QuantifierPart copy() {
			List<Quantifier> qs = Lists.newArrayList();
			for (Quantifier q : quantifiers) {
				qs.add(q.copy());
			}
			return new QuantifierPart(qs);
		}

		// mutates the object
		public void add(QuantifierPart quantifierPart) {
			List<Quantifier> newQuants = Lists.newArrayList();
			int i = 0;
			int ii = quantifierPart.quantifiers.size();
			for (Quantifier q : quantifiers) {
				if (q.type == QuantifierType.THEREIS) {
					newQuants.add(q);
				} else {
					for (; i < ii; i++) {
						Quantifier qq = quantifierPart.quantifiers.get(i);
						if (qq.type == QuantifierType.THEREIS) {
							newQuants.add(qq);
						} else {
							break;
						}
					}
					newQuants.add(q);
				}
			}
			for (; i < ii; i++) {
				Quantifier qq = quantifierPart.quantifiers.get(i);
				newQuants.add(qq);
			}
			this.quantifiers = newQuants;
		}

		public void removeWithName(String name) {
			Quantifier r = null;
			for (Quantifier q : quantifiers) {
				if (q.name.equals(name)) {
					r = q;
					break;
				}
			}
			if (r != null) {
				quantifiers.remove(r);
			}
		}
	}

	private final QuantifierPart quantifierPart;
	private final BooleanPart booleanPart;

	public Prop(QuantifierPart q, BooleanPart b) {
		this.quantifierPart = q;
		this.booleanPart = b;
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
		UniqueString s = new UniqueString();

		for (Quantifier q : a.quantifierPart.quantifiers) {
			a.replaceHeccity(q, s.getString());
		}
		for (Quantifier q : b.quantifierPart.quantifiers) {
			b.replaceHeccity(q, s.getString());
		}
		a.quantifierPart.add(b.quantifierPart);
		a.booleanPart.add(b.booleanPart);
		return a;
	}

	public Prop multiply(Prop b) {
		Prop a = this.copy();
		UniqueString s = new UniqueString();

		for (Quantifier q : a.quantifierPart.quantifiers) {
			a.replaceHeccity(q, s.getString());
		}
		for (Quantifier q : b.quantifierPart.quantifiers) {
			b.replaceHeccity(q, s.getString());
		}
		a.quantifierPart.add(b.quantifierPart);
		a.booleanPart.multiply(b.booleanPart);
		a.simplify();
		a.removeContradictions();
		return a;
	}

	private void simplify() {
		booleanPart.simplify();
	}

	private void replaceHeccity(Quantifier q, String name) {
		String oldName = q.name;
		for (Quantifier qq : quantifierPart.quantifiers) {
			if (qq == q) {
				qq.name = name;
				break;
			}
		}
		for (ConjunctProp cj : booleanPart.conjunctions) {
			cj.replaceHecceities(oldName, name);
		}
	}

	private void removeContradictions() {
		booleanPart.removeContradictions();
	}

	public List<Prop> transmitLastUniveral() {
		Quantifier q = getLastUniversal();
		List<Prop> ret = Lists.newArrayList();
		if (q == null) {
			return ret;
		}
		for (Quantifier qq : quantifierPart.quantifiers) {
			if (qq == q) {
				break;
			}
			Prop p = this.copy();
			p.replaceHeccity(qq.name, q.name);
			p.quantifierPart.removeWithName(q.name);
			p.simplify();
			p.removeContradictions();
			ret.add(p);
		}
		return ret;

	}

	private void replaceHeccity(String to, String from) {
		Quantifier qq = null;
		for (Quantifier q : quantifierPart.quantifiers) {
			if (q.name.equals(from)) {
				qq = q;
			}
		}
		if (qq != null) {
			quantifierPart.quantifiers.remove(qq);
		}
		booleanPart.replaceHeccity(to, from);

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
}
