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

	}

	private final List<Quantifier> prefix = Lists.newArrayList();
	private List<CompoundProp> matrix = Lists.newArrayList();
	private Map<Hecceity, String> h2s = Maps.newHashMap();
	private Map<String, Hecceity> s2h = Maps.newHashMap();

	public List<Quantifier> getPrefix() {
		return prefix;
	}

	public List<CompoundProp> getMatrix() {
		return matrix;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Quantifier q : prefix) {
			if (q.getType() == QuantifierType.FORALL) {
				sb.append("forall ");
			} else {
				sb.append("thereis ");
			}
			Hecceity h = q.getHecceity();
			sb.append(h2s.get(h));
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
	 */
	public void addQuantifierUnique(Quantifier q) {
		// don't add duplicate quantifiers
		if (getHecceties().indexOf(q.getHecceity()) >= 0) {
			return;
		}
		if (!h2s.containsKey(q.hecceity)) {
			String s = uniqueString.getString();
			h2s.put(q.hecceity, s);
			s2h.put(s, q.hecceity);
		}
		addQuantifier(q);
	}

	public void addCompoundProp(CompoundProp p) {
		this.matrix.add(p);
	}

	public void addQuantifier(QuantifierType qt, String lit) {
		Hecceity h = s2h.get(lit);
		if (h == null) {
			h = new Hecceity();
			h2s.put(h, lit);
			s2h.put(lit, h);
		}
		addQuantifier(new Quantifier(qt, h));
	}

	public void addQuantifier(QuantifierType qt) {
		String name = uniqueString.getString();
		addQuantifier(qt, name);
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
		return ret;
	}

	public Prop copy() {
		Prop ret = new Prop();
		for (Quantifier q : getPrefix()) {
			ret.addQuantifier(q.getType());
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

	private static class ReplaceOp implements UnaryOperator<Hecceity> {

		private final Hecceity from;
		private final Hecceity to;

		public ReplaceOp(Hecceity from, Hecceity to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public Hecceity apply(Hecceity t) {
			if (t == from) {
				return to;
			}
			return t;
		}

	}

	public void replace(Quantifier from, Quantifier to) {
		List<Quantifier> pre = getPrefix();
		Hecceity fh = from.getHecceity();
		Hecceity th = to.getHecceity();
		ReplaceOp op = new ReplaceOp(fh, th);
		pre.remove(from);
		List<CompoundProp> m = getMatrix();
		for (CompoundProp cp : m) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				ap.getHecceities().replaceAll(op);
			}
		}

	}

	public void addAllQuants(List<Quantifier> quants) {
		for (Quantifier q : quants) {
			addQuantifierUnique(q);
		}
	}

	public Map<Hecceity, List<AtomicProp>> hecceties2atomicProps() {
		Map<Hecceity, List<AtomicProp>> ret = Maps.newHashMap();
		for (Quantifier q : getPrefix()) {
			Hecceity h = q.getHecceity();
			List<AtomicProp> apl = Lists.newArrayList();
			ret.put(h, apl);
			for (CompoundProp cp : getMatrix()) {
				for (AtomicProp ap : cp.getAtomicProps()) {
					if (ap.getHecceities().contains(h)) {
						apl.add(ap);
					}
				}
			}
		}
		return ret;
	}

	public List<AtomicProp> getAtomicPropsForQuants(List<Quantifier> quants) {
		List<AtomicProp> ret = Lists.newArrayList();
		Map<Hecceity, List<AtomicProp>> m = hecceties2atomicProps();
		for (Quantifier q : quants) {
			ret.addAll(m.get(q.getHecceity()));
		}
		return ret;
	}
}
