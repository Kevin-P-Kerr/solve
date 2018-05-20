package com.lang.val;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

	private final UniqueString uniqueString = new UniqueString();

	public static class Hecceity {
	}

	public static enum QuantifierType {
		FORALL, THEREIS;
	}

	public static class Quantifier {
		private final QuantifierType type;
		private final Hecceity hecceity;
		private AtomicProp constraint;

		public Quantifier(QuantifierType type, Hecceity hecceity) {
			this.type = type;
			this.hecceity = hecceity;
		}

		public QuantifierType getType() {
			return type;
		}

		public Hecceity getHecceity() {
			return hecceity;
		}

		public void addConstraint(AtomicProp ap) {
			this.constraint = ap;
		}

		public AtomicProp getConstraint() {
			return constraint;
		}

	}

	public static class AtomicProp {
		private final String name;
		private final List<Hecceity> hecceities;
		private final boolean truthValue;

		public AtomicProp(String name, List<Hecceity> ents, boolean tv) {
			this.name = name;
			this.hecceities = ents;
			this.truthValue = tv;
		}

		public String getName() {
			return name;
		}

		public List<Hecceity> getHecceities() {

			return hecceities;
		}

		public boolean getTruthValue() {
			return truthValue;
		}

		private Integer hashCode = null;

		@Override
		public int hashCode() {
			if (hashCode != null) {
				return hashCode;
			}
			int seed = 31;
			seed *= name.hashCode();
			for (Hecceity h : hecceities) {
				seed *= h.hashCode();
			}
			hashCode = seed * new Boolean(truthValue).hashCode();
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AtomicProp)) {
				return false;
			}
			AtomicProp p = (AtomicProp) obj;
			if (p.getTruthValue() != truthValue) {
				return false;
			}
			boolean f = p.getName().equals(getName());
			if (!f) {
				return false;
			}
			List<Hecceity> hecs = p.getHecceities();
			if (hecceities.size() != hecs.size()) {
				return false;
			}
			for (int i = 0, ii = hecceities.size(); i < ii; i++) {
				if (hecceities.get(i) != hecs.get(i)) {
					return false;
				}
			}
			return true;
		}
	}

	public class CompoundProp {
		private final List<AtomicProp> atomicProps;

		private CompoundProp() {
			this.atomicProps = Lists.newArrayList();
		}

		public List<AtomicProp> getAtomicProps() {
			return atomicProps;
		}

		public void addAtomicProp(String name, List<String> hecceities, boolean tv) {
			List<Hecceity> ents = Lists.newArrayList();
			for (String s : hecceities) {
				ents.add(s2h.get(s));
			}
			addAtomicProp(new AtomicProp(name, ents, tv));
		}

		public void addAtomicProp(AtomicProp atomicProp) {
			this.atomicProps.add(atomicProp);
		}

		private Integer hashCode = null;

		@Override
		public int hashCode() {
			if (hashCode != null) {
				return hashCode;
			}
			int seed = 31;
			for (AtomicProp ap : atomicProps) {
				seed *= ap.hashCode();
			}
			hashCode = seed;
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CompoundProp)) {
				return false;
			}
			CompoundProp cp = (CompoundProp) obj;
			if (cp.getAtomicProps().size() != getAtomicProps().size()) {
				return false;
			}
			for (AtomicProp ap : cp.getAtomicProps()) {
				if (atomicProps.indexOf(ap) < 0) {
					return false;
				}
			}
			return true;

		}

		public List<AtomicPropInfo> getAtomicPropInfo() {
			List<AtomicPropInfo> ret = Lists.newArrayList();
			for (AtomicProp ap : atomicProps) {
				String str = ap.getName() + "(";
				for (Hecceity h : ap.getHecceities()) {
					str += h2s.get(h);
				}
				str += ")";
				ret.add(new AtomicPropInfo(str, ap.getTruthValue()));
			}
			return ret;

		}

		public void addAllAtomicProp(List<AtomicProp> atomicProps2) {
			this.atomicProps.addAll(atomicProps2);
		}

	}

	public static class AtomicPropInfo {
		private final String str;
		private final boolean tv;

		public AtomicPropInfo(String str, boolean tv) {
			this.str = str;
			this.tv = tv;
		}

		public String getString() {
			return str;
		}

		public boolean getTruthValue() {
			return tv;
		}
	}

	private final List<Quantifier> prefix = Lists.newArrayList();
	private final List<List<Quantifier>> quantifierContraints = Lists.newArrayList();
	private List<CompoundProp> matrix = Lists.newArrayList();
	private Map<Hecceity, String> h2s = Maps.newHashMap();
	private Map<String, Hecceity> s2h = Maps.newHashMap();

	public void addQuantifierConstraint(List<Quantifier> quants) {
		this.quantifierContraints.add(quants);
	}
	
	private static class SwapOp <T> implements UnaryOperator <T> {

		private final T from;
		private final T to;
		
		public SwapOp(T from, T to) {
			this.from = from;
			this.to= to;
		}
		@Override
		public T apply(T t) {
			if (t == from) {
				return to;
			}
			if (t == to) {
				return from;
			}
			return t;
		}
		
	}
	
	public Prop swapQuantifiers (int from, int to) throws LogicException {
		int n = to;
		if (from == to) {
			throw new LogicException();
		}
		if (from > to) {
			to = from;
			from = n;
		}
		Quantifier fromQ = prefix.get(from);
		Quantifier toQ = prefix.get(to);
		if (quantifierContraints.size() == 0) {
			throw new LogicException();
		}
		for (List<Quantifier> quants:quantifierContraints) {
			if (quants.indexOf(fromQ) >= 0 && quants.indexOf(toQ) >= 0) {
				throw new LogicException();
			}
			if (quants.indexOf(fromQ) >= 0) {
				int dex = quants.indexOf(fromQ);
				if (dex == quants.size()-1) {
					continue;
				}
				int next = prefix.indexOf(quants.get(dex+1));
				if (next <= to) {
					throw new LogicException();
				}
			}
			if (quants.indexOf(toQ) >= 0) {
				int dex = quants.indexOf(toQ);
				if (dex == 0) {
					continue;
				}
				int next = prefix.indexOf(quants.get(dex-1));
				if (next >= from) {
					throw new LogicException();
				}
			}
			
		}
		prefix.replaceAll(new SwapOp<Quantifier>(fromQ,toQ));
		return this;
	}
	
	public List<Quantifier> getPrefix() {
		return prefix;
	}

	public List<CompoundProp> getMatrix() {
		return matrix;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<AtomicProp> constraints = Lists.newArrayList();
		for (Quantifier q : prefix) {
			if (q.getType() == QuantifierType.FORALL) {
				sb.append("forall ");
			} else {
				sb.append("thereis ");
			}
			Hecceity h = q.getHecceity();
			sb.append(h2s.get(h));

			AtomicProp constraint = q.getConstraint();
			if (constraint != null) {
				constraints.add(constraint);
				sb.append(" in ");
				sb.append(constraint.getName());
			}
			sb.append(" ");

		}
		sb.append(":");
		boolean firstCP = true;
		for (CompoundProp cp : matrix) {
			if (!firstCP) {
				sb.append(" + ");
			} else {
				firstCP = false;
			}
			boolean firstAtomicProp = true;
			for (AtomicProp prop : cp.getAtomicProps()) {
				if (constraints.indexOf(prop) >= 0) {
					continue;
				}
				if (!firstAtomicProp) {
					sb.append("*");
				} else {
					firstAtomicProp = false;
				}
				String name = prop.getName();
				if (!prop.getTruthValue()) {
					name = "~" + name;
				}
				sb.append(name + "(");
				boolean first = true;
				for (Hecceity h : prop.getHecceities()) {
					if (!first) {
						sb.append(" ");
					} else {
						first = false;
					}
					String c = h2s.get(h);
					sb.append(c);
				}
				sb.append(")");

			}

		}
		return sb.toString();
	}

	private void addQuantifier(Quantifier q) {
		this.prefix.add(q);
	}

	/**
	 * add quantifier with unique hecceties
	 * @return 
	 */
	public Quantifier addQuantifierUnique(Quantifier q) {
		// don't add duplicate quantifiers
		if (getHecceties().indexOf(q.getHecceity()) >= 0) {
			return null;
		}
		if (!h2s.containsKey(q.hecceity)) {
			String s = uniqueString.getString();
			h2s.put(q.hecceity, s);
			s2h.put(s, q.hecceity);
		}
		addQuantifier(q);
		return q;
	}

	public void addCompoundProp(CompoundProp p) {
		this.matrix.add(p);
	}

	public Quantifier addQuantifier(QuantifierType qt, String lit) {
		Hecceity h = s2h.get(lit);
		if (h == null) {
			h = new Hecceity();
			h2s.put(h, lit);
			s2h.put(lit, h);
		}
		Quantifier q = new Quantifier(qt, h);
		addQuantifier(q);
		return q;
	}

	public void addQuantifier(QuantifierType qt, String lit, String constraint) {
		Quantifier q = addQuantifier(qt, lit);
		List<Hecceity> arg = Lists.newArrayList(q.getHecceity());
		AtomicProp ap = new AtomicProp(constraint, arg, true);
		q.addConstraint(ap);
	}

	public Quantifier addQuantifier(QuantifierType qt) {
		String name = uniqueString.getString();
		Quantifier q = addQuantifier(qt, name);
		return q;
	}

	public CompoundProp makeBlankCompoundProp() {
		return new CompoundProp();
	}

	public List<Hecceity> getHecceties() {

		List<Hecceity> ret = Lists.newArrayList();
		for (Quantifier q : getPrefix()) {
			ret.add(q.getHecceity());
		}
		return ret;
	}

	public Prop copyWithHecceities() {
		Prop ret = new Prop();
		for (Quantifier q : getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		for (CompoundProp cp : getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			for (AtomicProp ap : cp.getAtomicProps()) {
				List<Hecceity> args = Lists.newArrayList();
				for (Hecceity h : ap.getHecceities()) {
					args.add(h);
				}
				ncp.addAtomicProp(new AtomicProp(ap.getName(), args, ap.getTruthValue()));
			}
			ret.addCompoundProp(ncp);
		}
		for (List<Quantifier> quants: quantifierContraints) {
			ret.addQuantifierConstraint(quants);
		}
		return ret;
	}

	public Prop copy() {
		Prop ret = new Prop();
		for (Quantifier q : getPrefix()) {
			Quantifier nq = ret.addQuantifier(q.getType());
			AtomicProp constraint = q.getConstraint();
			if (constraint != null) {
				List<Hecceity> arg = Lists.newArrayList(nq.getHecceity());
				AtomicProp nc = new AtomicProp(constraint.getName(), arg, true);
				nq.addConstraint(nc);
			}
		}
		for (CompoundProp cp : getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			for (AtomicProp ap : cp.getAtomicProps()) {
				List<Hecceity> args = Lists.newArrayList();
				for (Hecceity h : ap.getHecceities()) {
					args.add(ret.getHecceties().get(getHecceties().indexOf(h)));
				}
				ncp.addAtomicProp(new AtomicProp(ap.getName(), args, ap.getTruthValue()));
			}
			ret.addCompoundProp(ncp);
		}
		return ret;
	}

	private static class ReplaceOp<T> implements UnaryOperator<T>

	{

		private final T from;
		private final T to;

		public ReplaceOp(T from, T to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public T apply(T t) {
			if (t == from) {
				return to;
			}
			return t;
		}

	}

	public static class LogicException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LogicException() {
			super();
		}
	}

	public Prop replace(Quantifier from, Quantifier to) throws LogicException {
		if (!from.getType().equals(QuantifierType.FORALL)) {
			throw new LogicException();
		}
		List<Quantifier> pre = getPrefix();
		Hecceity fh = from.getHecceity();
		Hecceity th = to.getHecceity();
		ReplaceOp<Hecceity> op = new ReplaceOp<Hecceity>(fh, th);
		ReplaceOp<Quantifier> qop = new ReplaceOp<Quantifier>(from, to);
		if (pre.indexOf(to) >= 0) {
			pre.remove(from);
		} else {
			String k = h2s.get(fh);
			s2h.put(k, th);
			h2s.put(th, k);
			pre.replaceAll(qop);
		}
		List<CompoundProp> m = getMatrix();
		for (CompoundProp cp : m) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				ap.getHecceities().replaceAll(op);
			}
		}
		return this;

	}

	public List<Quantifier> addAllQuants(List<Quantifier> quants) {
		List<Quantifier> ret = Lists.newArrayList();
		for (Quantifier q : quants) {
			Quantifier  r = (addQuantifierUnique(q));
			if (r != null) {
				ret.add(r);
			}
		}
		return ret;
	}

	public void removeQuantifier(Quantifier q) {
		getPrefix().remove(q);
		Hecceity h = q.getHecceity();
		List<CompoundProp> removals = Lists.newArrayList();

		for (CompoundProp cp : matrix) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				if (ap.getHecceities().indexOf(h) >= 0) {
					removals.add(cp);
				}
			}
		}
		for (CompoundProp cp : removals) {
			matrix.remove(cp);
		}
	}

	public List<Quantifier> getQuants(QuantifierType type) {
		List<Quantifier> ret = Lists.newArrayList();
		for (Quantifier q : getPrefix()) {
			if (q.getType().equals(type)) {
				ret.add(q);
			}
		}
		return ret;
	}

	public Prop copySpecificProp(int i) {
		Prop p = new Prop();
		CompoundProp cp = getMatrix().get(i);
		List<Quantifier> quants = Lists.newArrayList();
		List<Hecceity> hecs = Lists.newArrayList();
		for (AtomicProp ap : cp.getAtomicProps()) {
			hecs.addAll(ap.getHecceities());
		}
		for (Quantifier q : getPrefix()) {
			if (hecs.indexOf(q.getHecceity()) >= 0) {
				quants.add(q);
			}
		}
		for (Quantifier q : quants) {
			p.addQuantifierUnique(q);
		}
		CompoundProp ncp = p.makeBlankCompoundProp();
		for (AtomicProp ap : cp.getAtomicProps()) {
			ncp.addAtomicProp(ap);
		}
		p.addCompoundProp(ncp);
		return p.copy();

	}

	public List<Prop> getIndividualFacts() {
		List<Quantifier> marked = Lists.newArrayList();
		List<Prop> ret = Lists.newArrayList();
		for (Quantifier q : getPrefix()) {
			Prop p = new Prop();
			if (marked.indexOf(q) >= 0) {
				continue;
			}
			List<Quantifier> related = getRelatedQuantifiers(q);
			for (Quantifier r : related) {
				p.addQuantifierUnique(r);
				marked.add(r);
			}
			for (CompoundProp cp : getMatrix()) {
				CompoundProp compound = p.makeBlankCompoundProp();
				List<AtomicProp> atoms = cp.getAtomicProps();
				for (AtomicProp ap : atoms) {
					for (Quantifier qq : related) {
						if (ap.getHecceities().indexOf(qq.hecceity) >= 0) {
							compound.addAtomicProp(ap);
							break;
						}
					}

				}
				p.addCompoundProp(compound);
			}
			ret.add(p);
		}
		return ret;
	}

	private List<Quantifier> getRelatedQuantifiers(Quantifier q) {
		List<Quantifier> ret = Lists.newArrayList(q);
		Map<Hecceity, Quantifier> m = Maps.newHashMap();
		for (Quantifier qq : getPrefix()) {
			m.put(qq.hecceity, qq);
		}
		int s = ret.size();
		boolean first = true;
		while (s != ret.size() || first) {
			first = false;
			s = ret.size();
			for (CompoundProp cp : getMatrix()) {
				for (AtomicProp ap : cp.getAtomicProps()) {
					List<Hecceity> hecs = ap.getHecceities();
					List<Quantifier> additions = Lists.newArrayList();
					for (Quantifier qq: ret) {
						if (hecs.indexOf(qq.getHecceity()) >= 0) {
							for (Hecceity h : hecs) {
								Quantifier quant = m.get(h);
								additions.add(quant);
							}
						}
					}
					for (Quantifier qq:additions) {
						if (ret.indexOf(qq) < 0) {
							ret.add(qq);
						}
					}
				}
			}	
		}
		return ret;

	}

	@Deprecated
	public List<Prop> factor() {
		if (matrix.size() == 1) {
			if (matrix.get(0).getAtomicProps().size() == 1) {
				return Lists.newArrayList(this);
			}
			List<Prop> ret = Lists.newArrayList();
			for (AtomicProp ap : matrix.get(0).getAtomicProps()) {
				Prop p = new Prop();
				for (Quantifier q : prefix) {
					p.addQuantifierUnique(q);
				}
				CompoundProp cp = p.makeBlankCompoundProp();
				cp.addAtomicProp(ap);
				p.addCompoundProp(cp);
				List<Quantifier> removals = Lists.newArrayList();
				for (Quantifier q : p.getPrefix()) {
					if (ap.getHecceities().indexOf(q.getHecceity()) < 0) {
						removals.add(q);
					}
				}
				p.getPrefix().removeAll(removals);
				ret.add(p);
			}
			return ret;
		}
		List<AtomicProp> common = Lists.newArrayList();
		CompoundProp first = matrix.get(0);
		for (AtomicProp ap : first.getAtomicProps()) {
			common.add(ap);
		}
		for (int i = 1, ii = matrix.size(); i < ii; i++) {
			CompoundProp cp = matrix.get(i);
			List<AtomicProp> atoms = cp.getAtomicProps();
			List<AtomicProp> removals = Lists.newArrayList();
			for (AtomicProp ap : common) {
				if (atoms.indexOf(ap) < 0) {
					removals.add(ap);
				}
			}
			common.removeAll(removals);
		}
		if (common.size() == 0) {
			return Lists.newArrayList(this);
		}
		Prop factor1 = copyWithHecceities();
		Prop factor2 = copyWithHecceities();
		CompoundProp cp = factor1.makeBlankCompoundProp();
		for (AtomicProp ap : common) {
			cp.addAtomicProp(ap);
		}
		factor1.matrix = Lists.newArrayList(cp);
		List<Quantifier> removals = Lists.newArrayList();
		boolean removalFlag= true;
		for (Quantifier q : factor1.getPrefix()) {
			for (AtomicProp ap : common) {
				if (ap.getHecceities().indexOf(q.hecceity) >= 0) {
					removalFlag = false;
					break;
				}
			}
			if (removalFlag) {
				removals.add(q);
			}
			else {
				removalFlag = true;
			}
		}
		factor1.getPrefix().removeAll(removals);
		for (CompoundProp compound : factor2.getMatrix()) {
			compound.getAtomicProps().removeAll(common);
		}
		removals = Lists.newArrayList();
		for (Quantifier q : factor2.getPrefix()) {
			for (CompoundProp comp : factor2.getMatrix()) {
				for (AtomicProp atomic : comp.getAtomicProps()) {
					if (atomic.getHecceities().indexOf(q.hecceity) >= 0) {
						break;
					}
				}
			}
			removals.add(q);
		}
		factor2.getPrefix().removeAll(removals);
		List<Prop> ret = Lists.newArrayList();
		ret.add(factor1);
		ret.add(factor2);
		return ret;
	}

	public Prop extractPropFromQuant(int i) {
		Quantifier q = getPrefix().get(i);
		Prop p = new Prop();
		p.addQuantifierUnique(q);
		for (CompoundProp cp : getMatrix()) {
			CompoundProp ncp = p.makeBlankCompoundProp();
			for (AtomicProp ap : cp.getAtomicProps()) {
				if (ap.getHecceities().size() == 1) {
					if (ap.getHecceities().get(0).equals(q.getHecceity())) {
						ncp.addAtomicProp(ap);
					}
				}
			}
			p.addCompoundProp(ncp);
		}
		return p;
	}

}
