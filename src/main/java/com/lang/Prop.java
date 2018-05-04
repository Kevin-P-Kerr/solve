package com.lang;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Prop {
	private static class IDMaker {
		private long c = 0;

		public long getID() {
			long i = c;
			c++;
			return i;
		}
	}

	private static final IDMaker idMaker = new IDMaker();

	public static class Hecceity {
		private final long id;

		public Hecceity(long id) {
			this.id = id;
		}
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

		public AtomicProp(String name, List<Hecceity> ents) {
			this.name = name;
			this.hecceities = ents;
		}

		public String getName() {
			return name;
		}

		public List<Hecceity> getHecceities() {
			return hecceities;
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

		public void addAtomicProp(String name, List<String> hecceities) {
			List<Hecceity> ents = Lists.newArrayList();
			for (String s : hecceities) {
				ents.add(s2h.get(s));
			}
			addAtomicProp(new AtomicProp(name, ents));
		}

		private void addAtomicProp(AtomicProp atomicProp) {
			this.atomicProps.add(atomicProp);

		}
	}

	private List<Quantifier> prefix = Lists.newArrayList();
	private List<CompoundProp> matrix = Lists.newArrayList();
	private Map<Hecceity, String> h2s = Maps.newHashMap();
	private Map<String, Hecceity> s2h = Maps.newHashMap();

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

	public void addQuantifier(Quantifier q) {
		this.prefix.add(q);
	}

	public void addCompoundProp(CompoundProp p) {
		this.matrix.add(p);
	}

	public void addQuantifier(QuantifierType qt, String lit) {
		Hecceity h = s2h.get(lit);
		if (h == null) {
			h = new Hecceity(idMaker.getID());
			h2s.put(h, lit);
			s2h.put(lit, h);
		}
		addQuantifier(new Quantifier(qt, h));
	}

	public CompoundProp makeBlankCompoundProp() {
		return new CompoundProp();
	}

}
