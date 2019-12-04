package com.lang.val;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lang.ProofTrace;
import com.lang.Tuple;
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

		private QuantifierType type;
		private String name;
		private int index;
		private final List<Integer> indicesForTransmission = Lists.newArrayList();

		private Quantifier(QuantifierType t, String name, int index) {
			this.type = t;
			this.name = name;
			this.index = index;
		}

		public static Quantifier newExistential(String name, int index) {
			return new Quantifier(QuantifierType.THEREIS, name, index);
		}

		public static Quantifier newUniversal(String name, int index) {
			return new Quantifier(QuantifierType.FORALL, name, index);
		}

		public static Quantifier newExistential(String name) {
			return new Quantifier(QuantifierType.THEREIS, name, -1);
		}

		public static Quantifier newUniversal(String name) {
			return new Quantifier(QuantifierType.FORALL, name, -1);
		}

		public void setIndex(int i) {
			this.index = i;
		}

		public boolean canTransmitInto(Quantifier q) {
			return indicesForTransmission.contains(q.index);
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
			Quantifier q = new Quantifier(type, name, index);
			for (Integer i : indicesForTransmission) {
				q.indicesForTransmission.add(i);
			}
			return q;
		}

		@Override
		public boolean equals(Object a) {
			if (super.equals(a)) {
				return true;
			}
			if (!(a instanceof Quantifier)) {
				return false;
			}
			Quantifier q = (Quantifier) a;
			return q.index == index && q.type == type;
		}

		public void setIndicesPriorTo(int i) {
			if (type == QuantifierType.THEREIS) {
				return;
			}
			while (i-- >= 0) {
				indicesForTransmission.add(i);
			}

		}
	}

	public static class BooleanPart {
		private List<ConjunctProp> conjunctions;

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
						d("removing " + cp.toString());
						d("eq to " + ccp.toString());
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

		private void setUpIndex(String name, int i) {
			for (ConjunctProp cp : conjunctions) {
				cp.setUpIndex(name, i);
			}
		}

		private void transmitHecName(int index, String s) {
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

		private int firstContradictionIndex = -1;

		public boolean hasPotentialContradictions(QuantifierPart quantifierPart) {
			int i = 0;
			for (ConjunctProp cp : conjunctions) {
				if (cp.hasPotentialContradictions(quantifierPart)) {
					this.firstContradictionIndex = i;
					return true;
				}
				i++;
			}
			return false;
		}

		public Tuple<List<Integer>, List<Integer>> getFirstContradiction() {
			ConjunctProp cp = conjunctions.get(firstContradictionIndex);
			firstContradictionIndex = -1;
			return cp.getFirstContradiction();
		}
	}

	public static class ConjunctProp {
		private final List<AtomicProp> atoms;

		public ConjunctProp(List<AtomicProp> atoms) {
			this.atoms = atoms;
		}

		private int firstContradictionIndex = -1;

		// must be called after hasPotentialContradictions()
		protected Tuple<List<Integer>, List<Integer>> getFirstContradiction() {
			AtomicProp ap = atoms.get(firstContradictionIndex);
			firstContradictionIndex = -1;
			List<Integer> from = ap.fromIndices;
			List<Integer> tol = ap.toIndices;
			ap.fromIndices = Lists.newArrayList();
			ap.toIndices = Lists.newArrayList();
			return new Tuple<List<Integer>, List<Integer>>(from, tol);

		}

		public boolean hasPotentialContradictions(QuantifierPart quantifierPart) {
			for (int i = 0, ii = atoms.size(); i < ii; i++) {
				AtomicProp ap = atoms.get(i);
				for (int l = i + 1, ll = atoms.size(); l < ll; l++) {
					AtomicProp aap = atoms.get(l);
					if (ap.couldContradict(aap, quantifierPart)) {
						firstContradictionIndex = i;
						return true;
					}
				}
			}
			return false;
		}

		public boolean couldContradict(ConjunctProp ccp, QuantifierPart quantifierPart) {
			for (AtomicProp ap : atoms) {
				for (AtomicProp aap : ccp.atoms) {
					if (ap.couldContradict(aap, quantifierPart)) {
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

	}

	public static class AtomicProp {
		private boolean negate;
		private final String name;
		private final List<Heccity> heccesities;

		public AtomicProp(boolean n, String name, List<Heccity> h) {
			this.negate = n;
			this.name = name;
			this.heccesities = h;
		}

		private List<Integer> fromIndices = Lists.newArrayList();
		private List<Integer> toIndices = Lists.newArrayList();

		public boolean couldContradict(AtomicProp aap, QuantifierPart quantifierPart) {
			if (!(name.equals(aap.name) && negate != aap.negate)) {
				return false;
			}
			List<Integer> toList = Lists.newArrayList();
			List<Integer> fromList = Lists.newArrayList();
			for (int i = 0, ii = heccesities.size(); i < ii; i++) {
				int from;
				int to;
				Heccity a = heccesities.get(i);
				Heccity b = aap.heccesities.get(i);
				if (a.equals(b)) {
					continue;
				}
				int indexA = a.index;
				int indexB = b.index;
				Quantifier qA = quantifierPart.getQuantifier(indexA);
				Quantifier qB = quantifierPart.getQuantifier(indexB);
				if (qA.canTransmitInto(qB)) {
					from = qA.index;
					to = qB.index;
				} else if (qB.canTransmitInto(qA)) {
					from = qB.index;
					to = qA.index;
				} else {
					return false;
				}
				if (fromList.contains(to)) {
					return false;
				}
				if (fromList.contains(from)) {
					continue;
				}
				toList.add(to);
				fromList.add(from);
			}
			fromIndices = fromList;
			toIndices = toList;
			return true;
		}

		public void transmitHecName(int index, String s) {
			for (Heccity h : heccesities) {
				if (h.index == index) {
					h.name = s;
				}
			}
		}

		public void setUpIndex(String n, int i) {
			for (Heccity h : heccesities) {
				if (h.name.equals(n)) {
					h.index = i;
				}
			}
		}

		public boolean contradicts(AtomicProp ap) {
			AtomicProp c = new AtomicProp(!ap.negate, ap.name, ap.heccesities);
			return equals(c);

		}

		public void replaceHecceities(int from, int to, String name) {
			for (Heccity h : heccesities) {
				if (h.index == from) {
					h.index = to;
					h.name = name;
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
			if (ap.heccesities.size() != heccesities.size()) {
				return false;
			}
			for (int i = 0, ii = heccesities.size(); i < ii; i++) {
				Heccity h = heccesities.get(i);
				Heccity hh = ap.heccesities.get(i);
				if (!(h.equals(hh))) {
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
		private int index;

		public Heccity(String n) {
			this.name = n;
		}

		private Heccity(String n, int i) {
			this.name = n;
			this.index = i;
		}

		public Heccity copy() {
			return new Heccity(name, index);
		}

		@Override
		public boolean equals(Object h) {
			if (super.equals(h)) {
				return true;
			}
			if (h instanceof Heccity) {
				Heccity k = (Heccity) h;
				return k.index == index;
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
		public boolean equals(Object a) {
			if (super.equals(a)) {
				return true;
			}
			if (!(a instanceof QuantifierPart)) {
				return false;
			}
			QuantifierPart qp = (QuantifierPart) a;
			if (qp.quantifiers.size() != quantifiers.size()) {
				return false;
			}
			for (int i = 0, ii = quantifiers.size(); i < ii; i++) {
				if (!(qp.quantifiers.get(i).equals(quantifiers.get(i)))) {
					return false;
				}
			}
			return true;
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
		// thereis a forall b forall c thereis d :Man(a)*~Mother(b a)*~Man(c) + Man(a)*~Mother(b a)*Man(d)*Mother(d c).
		public void add(QuantifierPart quantifierPart) {
			for (Quantifier q : quantifierPart.quantifiers) {
				boolean isForall = q.type == QuantifierType.FORALL;
				for (Quantifier qq : quantifiers) {
					boolean localForAll = qq.type == QuantifierType.FORALL;
					if (isForall) {
						q.indicesForTransmission.add(qq.index);
					}
					if (localForAll) {
						qq.indicesForTransmission.add(q.index);
					}
				}
			}
			quantifiers.addAll(quantifierPart.quantifiers);
		}

		public void removeQuantifier(Quantifier r) {

			quantifiers.remove(r);

		}

		public void negate() {
			// forall a thereis b
			// ~ exists a forall b
			for (Quantifier q : quantifiers) {
				if (q.type == QuantifierType.FORALL) {
					q.type = QuantifierType.THEREIS;
				} else {
					q.type = QuantifierType.FORALL;
				}
			}
		}

		public int getLargestOffset() {
			return quantifiers.get(quantifiers.size() - 1).index;
		}

		public Quantifier getQuantifier(int t) {
			for (Quantifier q : quantifiers) {
				if (q.index == t) {
					return q;
				}
			}
			return null;
		}

		public void removeQuantifier(int f) {
			Quantifier toRemove = null;
			for (Quantifier q : quantifiers) {
				if (q.index == f) {
					toRemove = q;
					break;
				}
			}
			if (toRemove != null) {
				quantifiers.remove(toRemove);
			}
		}
	}

	private final QuantifierPart quantifierPart;
	private final BooleanPart booleanPart;

	public Prop(QuantifierPart q, BooleanPart b) {
		this.quantifierPart = q;
		this.booleanPart = b;
		this.init();
	}

	private void init() {
		// set up the indices of the heccities
		List<Quantifier> foralls = Lists.newArrayList();
		for (int i = 0, ii = quantifierPart.quantifiers.size(); i < ii; i++) {
			Quantifier q = quantifierPart.quantifiers.get(i);
			String name = q.name;
			q.setIndex(i);
			q.setIndicesPriorTo(i);
			if (q.type == QuantifierType.FORALL) {
				foralls.add(q);
			} else {
				for (int l = 0, ll = foralls.size(); l < ll; l++) {
					Quantifier qq = foralls.get(l);
					for (int n = l + 1, nn = foralls.size(); n < nn; n++) {
						Quantifier qz = foralls.get(n);
						int dex = qz.index;
						if (qq.indicesForTransmission.contains(dex)) {
							continue;
						}
						qq.indicesForTransmission.add(dex);
					}
				}
				foralls = Lists.newArrayList();
			}
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
		a.simplify();
		a.removeContradictions();
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

	private void simplify() {
		booleanPart.simplify();
	}

	public void removeContradictions() {
		booleanPart.removeContradictions();
	}

	private static boolean DEBUG = false;

	private static void d(Object o) {
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

	// watch out, this mutates the object!
	public void simplifyViaContradictions(ProofTrace trace) {
		// pre-condition check--can we do this?
		if (!booleanPart.hasPotentialContradictions(quantifierPart)) {
			return;
		}
		Tuple<List<Integer>, List<Integer>> l = booleanPart.getFirstContradiction();
		List<Integer> from = l.getLeft();
		List<Integer> to = l.getRight();
		for (int i = 0, ii = from.size(); i < ii; i++) {
			int f = from.get(i);
			int t = to.get(i);
			Quantifier qq = quantifierPart.getQuantifier(t);
			Quantifier old = quantifierPart.getQuantifier(f);
			quantifierPart.removeQuantifier(f);
			trace.replaceHeccity(t, f, qq.name, old.name);
			booleanPart.replaceHeccity(t, f, qq.name);
		}
		trace.removeContradictions();
		simplify();
		booleanPart.removeContradictions();
		if (!booleanPart.hasPotentialContradictions(quantifierPart)) {
			return;
		}
		simplifyViaContradictions(trace);

	}

	public void removeQuantifier(int index) {
		quantifierPart.removeQuantifier(index);
	}

	public void replaceHeccity(int to, int from, String name) {
		booleanPart.replaceHeccity(to, from, name);
	}
}
