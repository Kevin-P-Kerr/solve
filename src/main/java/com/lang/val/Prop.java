package com.lang.val;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
			seed *= Integer.hashCode(getHecceities().size());
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
			return true;
		}

		// assume equal hashCodes
		public boolean exactExactEquals(AtomicProp aap) {
			List<Hecceity> hecs = getHecceities();
			List<Hecceity> compHecs = aap.getHecceities();
			for (int i = 0, ii = hecs.size(); i < ii; i++) {
				if (hecs.get(i) != compHecs.get(i)) {
					return false;
				}
			}
			return true;
		}
	}

	public class CompoundProp {
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			boolean firstAtomicProp = true;
			for (AtomicProp prop : getAtomicProps()) {

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
			return sb.toString();

		}

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

		public boolean containsHecceity(Hecceity h) {
			for (AtomicProp ap : getAtomicProps()) {
				for (Hecceity comp : ap.getHecceities()) {
					if (comp == h) {
						return true;
					}
				}
			}
			return false;
		}

		public CompoundProp copy(CompoundProp cpy) {
			for (AtomicProp ap : getAtomicProps()) {
				cpy.addAtomicProp(new AtomicProp(ap.getName(), ap.getHecceities(), ap.getTruthValue()));
			}
			return cpy;
		}

		public String toString(List<AtomicProp> constraints) {
			List<String> names = Lists.newArrayList();
			for (AtomicProp ap : constraints) {
				names.add(ap.getName());
			}
			StringBuilder sb = new StringBuilder();
			boolean firstAtomicProp = true;
			int printed = 0;
			for (AtomicProp prop : getAtomicProps()) {

				String name = prop.getName();
				if (names.contains(name) && printed >= 1) {
					continue;
				}
				if (!firstAtomicProp) {
					sb.append("*");
				} else {
					firstAtomicProp = false;
				}

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
				printed++;
			}

			return sb.toString();

		}

		public boolean containsOnly(List<Hecceity> hecs) {
			for (AtomicProp ap : atomicProps) {
				for (Hecceity h : ap.getHecceities()) {
					if (hecs.indexOf(h) < 0) {
						return false;
					}
				}
			}
			return true;
		}

		public List<Hecceity> getAllHecceities() {
			List<Hecceity> ret = Lists.newArrayList();
			for (AtomicProp ap : getAtomicProps()) {
				ret.addAll(ap.getHecceities());
			}
			return ret;
		}

		public boolean evaluate(CompoundProp ccp) {
			List<AtomicProp> atoms = ccp.getAtomicProps();
			for (AtomicProp ap : getAtomicProps()) {
				boolean flag = false;
				for (AtomicProp comp : atoms) {
					if (ap.equals(comp) && ap.exactExactEquals(comp)) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					return false;
				}
			}
			return true;
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

	private static class SwapOp<T> implements UnaryOperator<T> {

		private final T from;
		private final T to;

		public SwapOp(T from, T to) {
			this.from = from;
			this.to = to;
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

	public Prop swapQuantifiers(int from, int to) throws LogicException {
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
			throw new LogicException("no quantifier constraints ");
		}
		try {
			for (List<Quantifier> quants : quantifierContraints) {
				if (quants.indexOf(fromQ) >= 0 && quants.indexOf(toQ) >= 0) {
					throw new LogicException();
				}
				if (quants.indexOf(fromQ) >= 0) {
					int dex = quants.indexOf(fromQ);
					if (dex == quants.size() - 1) {
						continue;
					}
					int next = prefix.indexOf(quants.get(dex + 1));
					if (next <= to) {
						throw new LogicException();
					}
				}
				if (quants.indexOf(toQ) >= 0) {
					int dex = quants.indexOf(toQ);
					if (dex == 0) {
						continue;
					}
					int next = prefix.indexOf(quants.get(dex - 1));
					if (next >= from) {
						throw new LogicException();
					}
				}

			}
		} catch (LogicException e) {
			StringBuilder s = new StringBuilder();
			for (List<Quantifier> quants : quantifierContraints) {
				for (Quantifier q : quants) {
					Hecceity h = q.getHecceity();
					String hn = h2s.get(h);
					s.append(hn).append(" ");
				}
				s.append("\n");
			}
			throw new LogicException(s.toString());
		}
		prefix.replaceAll(new SwapOp<Quantifier>(fromQ, toQ));
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
			sb.append(cp.toString(constraints));

		}
		return sb.toString();
	}

	private void addQuantifier(Quantifier q) {
		this.prefix.add(q);
	}

	/**
	 * add quantifier with unique hecceties
	 *
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
		ret.h2s = h2s;
		ret.s2h = s2h;
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
		for (List<Quantifier> quants : quantifierContraints) {
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
		private String message;
		private static final long serialVersionUID = 1L;

		public LogicException() {
			super();
		}

		public LogicException(String string) {
			this.message = string;
		}

		@Override
		public String toString() {
			return message == null ? "" : message;
		}
	}

	public boolean allowedForReplacement(Quantifier from) throws LogicException {
		if (from.getType().equals(QuantifierType.FORALL)) {
			return true;
		}
		for (List<Quantifier> quants : quantifierContraints) {
			for (Quantifier q : quants) {
				if (q == from || q.getHecceity() == from.getHecceity()) {
					return quants.size() == 1;
				}
			}
		}
		return false;
	}

	public Prop replace(Quantifier from, Quantifier to) throws LogicException {
		if (!allowedForReplacement(from)) {
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
		for (List<Quantifier> quants : quantifierContraints) {
			quants.replaceAll(qop);
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
			Quantifier r = (addQuantifierUnique(q));
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

	public Prop getCase(int index) throws LogicException {
		Prop p = this.copyWithHecceities();
		CompoundProp cp = p.matrix.get(index);
		p.matrix.clear();
		p.matrix.add(cp);
		List<Quantifier> removals = Lists.newArrayList();
		for (Quantifier q : p.prefix) {
			if (!cp.containsHecceity(q.hecceity)) {
				removals.add(q);
			}
		}
		p.prefix.removeAll(removals);
		return p;
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

	private Prop getIndividualFact(Quantifier q) {
		Prop p = new Prop();
		List<Quantifier> related = getRelatedQuantifiers(q);
		for (Quantifier r : related) {
			p.addQuantifierUnique(r);
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
		return p;
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
					for (Quantifier qq : ret) {
						if (hecs.indexOf(qq.getHecceity()) >= 0) {
							for (Hecceity h : hecs) {
								Quantifier quant = m.get(h);
								additions.add(quant);
							}
						}
					}
					for (Quantifier qq : additions) {
						if (ret.indexOf(qq) < 0) {
							ret.add(qq);
						}
					}
				}
			}
		}
		SwapOp<Quantifier> swap = new SwapOp<Prop.Quantifier>(ret.get(0), ret.get(ret.size() - 1));
		ret.replaceAll(swap);
		return ret;

	}

	public List<Prop> factor() {
		if (matrix.size() == 1) {
			if (matrix.get(0).getAtomicProps().size() == 1) {
				return Lists.newArrayList(this);
			}
			List<Prop> ret = Lists.newArrayList();
			label1: for (AtomicProp ap : matrix.get(0).getAtomicProps()) {
				Prop p = new Prop();
				for (Quantifier q : prefix) {
					AtomicProp constraint = q.getConstraint();
					if (constraint != null && ap.getHecceities().size() == 1
							&& ap.getHecceities().get(0) == constraint.getHecceities().get(0)) {
						continue label1;
					}
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
		// TODO: fix all this
		List<AtomicProp> common = Lists.newArrayList();
		CompoundProp first = matrix.get(0);
		List<AtomicProp> constraints = Lists.newArrayList();
		for (Quantifier q : getPrefix()) {
			AtomicProp cons = q.getConstraint();
			if (cons != null) {
				constraints.add(cons);
			}
		}
		label2: for (AtomicProp ap : first.getAtomicProps()) {
			if (ap.getHecceities().size() == 1 && ap.getTruthValue()) {
				for (AtomicProp con : constraints) {
					if (con.getHecceities().get(0) == ap.getHecceities().get(0) && con.getName() == ap.getName()) {
						continue label2;
					}
				}
				common.add(ap);
			}
		}
		for (int i = 1, ii = matrix.size(); i < ii; i++) {
			CompoundProp cp = matrix.get(i);
			List<AtomicProp> atoms = cp.getAtomicProps();
			List<AtomicProp> removals = Lists.newArrayList();
			for (AtomicProp ap : common) {
				if (atoms.indexOf(ap) < 0) {
					removals.add(ap);
				}
				if (ap.truthValue && ap.getHecceities().size() == 1) {

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
		boolean removalFlag = true;
		for (Quantifier q : factor1.getPrefix()) {
			for (AtomicProp ap : common) {
				if (ap.getHecceities().indexOf(q.hecceity) >= 0) {
					removalFlag = false;
					break;
				}
			}
			if (removalFlag) {
				removals.add(q);
			} else {
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

	public Value swapQuantifiers(String from, String to) throws LogicException {
		Hecceity fh = s2h.get(from);
		Hecceity th = s2h.get(to);
		int f = -1;
		int t = -1;
		for (int i = 0, ii = prefix.size(); i < ii; i++) {
			Quantifier q = prefix.get(i);
			if (q.getHecceity() == fh) {
				f = i;
			}
			if (q.getHecceity() == th) {
				t = i;
			}
			if (f >= 0 && t >= 0) {
				break;
			}

		}
		return swapQuantifiers(f, t);
	}

	public Prop replace(String from, String to) throws LogicException {
		Hecceity fh = s2h.get(from);
		Hecceity th = s2h.get(to);
		Quantifier f = null;
		Quantifier t = null;
		for (int i = 0, ii = prefix.size(); i < ii; i++) {
			Quantifier q = prefix.get(i);
			if (q.getHecceity() == fh) {
				f = q;
			}
			if (q.getHecceity() == th) {
				t = q;
			}
			if (f != null && t != null) {
				break;
			}

		}
		int ii = prefix.indexOf(f);
		int i = prefix.indexOf(t);
		if (ii < i) {
			throw new LogicException();
		}
		return replace(f, t);
	}

	// TODO: this is (probably) fine. but in the future use the exactEquals method on AtomicProp
	private static boolean contradiction(CompoundProp cp) {
		Map<String, Boolean> boolMap = Maps.newHashMap();

		for (AtomicPropInfo ap : cp.getAtomicPropInfo()) {
			Boolean b = boolMap.get(ap.getString());
			if (b == null) {
				b = ap.getTruthValue();
				boolMap.put(ap.getString(), b);
				continue;
			}
			if (b != ap.getTruthValue()) {
				return true;
			}
		}
		return false;

	}

	public Prop removeContradictions() {
		Prop ret = new Prop();
		for (Quantifier q : getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		for (CompoundProp cp : getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			if (!contradiction(cp)) {
				ncp.addAllAtomicProp(cp.getAtomicProps());

				ret.addCompoundProp(ncp);
			}
		}
		for (List<Quantifier> quants : quantifierContraints) {
			ret.addQuantifierConstraint(quants);
		}
		ret.h2s = h2s;
		ret.s2h = s2h;
		return ret;

	}

	// we can assume that p has no contradictions
	public Prop removeRedundant() {
		Prop ret = new Prop();
		for (Quantifier q : getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		List<CompoundProp> compounds = Lists.newArrayList();
		for (CompoundProp cp : getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			Set<AtomicProp> atoms = Sets.newHashSet();
			for (AtomicProp ap : cp.getAtomicProps()) {
				boolean flag = false;
				for (AtomicProp aap : atoms) {
					if (aap == ap) {
						flag = true;
						break;
					}
					if (aap.equals(ap) && aap.exactExactEquals(ap)) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					atoms.add(ap);
					ncp.addAtomicProp(ap);
				}
			}
			// TODO: do something like exact contains
			if (compounds.contains(ncp)) {
				continue;
			}
			compounds.add(ncp);
			ret.addCompoundProp(ncp);
		}
		for (List<Quantifier> quants : quantifierContraints) {
			ret.addQuantifierConstraint(quants);
		}
		ret.h2s = h2s;
		ret.s2h = s2h;
		return ret;
	}

	private static QuantifierType reverseType(Quantifier q) {
		return q.getType().equals(QuantifierType.FORALL) ? QuantifierType.THEREIS : QuantifierType.FORALL;
	}

	private Prop invertQuantifiers(int i) {
		CompoundProp cp = matrix.get(i);
		List<Hecceity> hecs = cp.getAllHecceities();
		List<Quantifier> invertedQuantifiers = Lists.newArrayList();
		for (Quantifier q : getPrefix()) {
			if (hecs.indexOf(q.getHecceity()) >= 0) {
				invertedQuantifiers.add(q);
			}
		}
		Prop p = copyWithHecceities();
		List<CompoundProp> antecedent = Lists.newArrayList(cp);

		antecedent = negateMatrix(antecedent, p);
		List<CompoundProp> consequent = Lists.newArrayList();
		for (CompoundProp ccp : getMatrix()) {
			if (ccp == cp) {
				continue;
			}
			consequent.add(ccp);
		}
		consequent = negateMatrix(consequent, p);
		List<Quantifier> consPrefix = Lists.newArrayList();
		for (Quantifier q : p.getPrefix()) {
			if (invertedQuantifiers.indexOf(q) < 0) {
				consPrefix.add(new Quantifier(reverseType(q), q.getHecceity()));
			}

		}
		p.prefix.clear();
		p.prefix.addAll(invertedQuantifiers);
		p.prefix.addAll(consPrefix);
		p.matrix.clear();
		p.matrix.addAll(antecedent);
		p.matrix.addAll(consequent);
		return p;
	}

	public Prop invertQuantifier(String from) throws LogicException {
		try {
			int i = Integer.parseInt(from);
			return invertQuantifiers(i);
		} catch (NumberFormatException e) {

		}
		Hecceity h = s2h.get(from);
		if (h == null) {
			throw new LogicException("no hecceity");
		}
		Quantifier q = null;
		for (Quantifier qq : getPrefix()) {
			if (qq.getHecceity() == h) {
				q = qq;
				break;
			}
		}
		if (q == null) {
			throw new LogicException("no quantifier");
		}
		Prop former = getIndividualFact(q);
		// check preconditions
		QuantifierType type = q.getType();
		if (type == QuantifierType.FORALL) {
			for (Quantifier quant : former.getPrefix()) {
				if (quant.type == QuantifierType.THEREIS) {
					throw new LogicException("invalid precondition for inversion");
				}
			}
		} else {
			List<Quantifier> formerPrefix = former.getPrefix();
			if (formerPrefix.indexOf(q) != formerPrefix.size() - 1) {
				throw new LogicException("invalid position for inversion");
			}
			for (Quantifier qq : formerPrefix) {
				if (q != qq && qq.getType() == QuantifierType.THEREIS) {
					throw new LogicException("invalid precondition for inversion");
				}
			}
		}

		List<CompoundProp> preconditions = Lists.newArrayList();
		List<CompoundProp> postconditions = Lists.newArrayList();
		for (CompoundProp cp : former.getMatrix()) {
			if (cp.containsHecceity(h)) {
				postconditions.add(cp);
			} else {
				preconditions.add(cp);
			}
		}
		Prop ret = new Prop();
		Quantifier nq = new Quantifier(type == QuantifierType.FORALL ? QuantifierType.THEREIS : QuantifierType.FORALL,
				q.getHecceity());
		nq.constraint = q.constraint;
		SwapOp<Quantifier> swap = new SwapOp<Prop.Quantifier>(q, nq);
		ret.prefix.addAll(former.prefix);
		ret.prefix.replaceAll(swap);

		ret.s2h = former.s2h;
		ret.h2s = former.h2s;
		ret.quantifierContraints.addAll(former.quantifierContraints);
		/* @formatter:off
		 * forall a thereis b : ~Man(a) + Man(b)*Mother(b a)
		 * forall a thereis b: Man(a) -> Man(b)*Mother(b a)
		 * forall a forall b : Man(b)*Mother(b a) -> Man(a)
		 * forall a forall b : ~Man(b) + ~Mother(b a) + Man(a)
		 * forall a forall b : ...
		 *
		 * forall a forall b forall c: ~Mother(a b) + ~Mother(b c) + Grandmother(a c)
		 * forall a forall b forall c : Mother(a b) -> ~Mother(b c) + Grandmother(a c)
		 * forall a forall b thereis c: ~Mother(b c) + Grandmother(a c) -> Mother(a b)
		 * forall a forall b thereis c: Mother(b c)*~Grandmother(a c) + Mother(a b)
		 *
		 * thereis a thereis b : Mother(b a)*Man(a)*Man(b)
		 * forall a forall b : ~Man(a) + ~Mother(b a) + Man(b)
		 *
		 * thereis a thereis b : Couch(a)*Me(b)*Sitting(b a)
		 *
		 * @formatter:on
		 */
		if (type == QuantifierType.THEREIS) {
			preconditions = negateMatrix(preconditions, former);
			postconditions = negateMatrix(postconditions, former);

		} else {
			// forall a b c : ~bar(a b c) + ac == forall a b thereis c ~a + ~c + bar(abc)
			postconditions = negateMatrix(postconditions, former);
			preconditions = negateMatrix(preconditions, former);
		}

		// doesn't really matter but looks nice
		if (type == QuantifierType.FORALL) {
			ret.matrix.addAll(preconditions);
			ret.matrix.addAll(postconditions);
		} else {
			ret.matrix.addAll(postconditions);
			ret.matrix.addAll(preconditions);
		}
		return ret;
	}

	public Prop negateMatrix() {
		Prop ret = copyWithHecceities();
		ret.matrix = negateMatrix(ret.matrix, ret);
		return ret;
	}

	private static List<CompoundProp> negateMatrix(CompoundProp cp, Prop p) {
		List<CompoundProp> ret = Lists.newArrayList();
		for (AtomicProp ap : cp.getAtomicProps()) {
			CompoundProp ncp = p.makeBlankCompoundProp();
			AtomicProp cpy = new AtomicProp(ap.getName(), ap.getHecceities(), !ap.getTruthValue());
			ncp.addAtomicProp(cpy);
			ret.add(ncp);
		}
		return ret;
	}

	private static CompoundProp combine(List<CompoundProp> m1, Prop p) {
		CompoundProp base = m1.get(0);
		base = base.copy(p.makeBlankCompoundProp());
		for (int i = 1, ii = m1.size(); i < ii; i++) {
			CompoundProp cp = m1.get(i);
			base.addAllAtomicProp(cp.getAtomicProps());
		}
		return base;
	}

	private static List<CompoundProp> combine(List<CompoundProp> m1, List<CompoundProp> m2, Prop p) {
		List<CompoundProp> ret = Lists.newArrayList();
		for (CompoundProp base : m1) {
			for (CompoundProp cp : m2) {
				base = base.copy(p.makeBlankCompoundProp());
				base.addAllAtomicProp(cp.getAtomicProps());
				ret.add(base);
			}
		}
		return ret;
	}

	private static List<CompoundProp> negateMatrix(List<CompoundProp> matrix, Prop p) {
		if (matrix.size() == 1) {
			return negateMatrix(matrix.get(0), p);
		}
		List<CompoundProp> base = negateMatrix(matrix.get(0), p);
		for (int i = 1; i < matrix.size(); i++) {
			base = combine(base, negateMatrix(matrix.get(i), p), p);
		}
		return base;
	}

	private List<CompoundProp> getSubsetOfMatrix(List<Hecceity> covered) {
		List<CompoundProp> ret = Lists.newArrayList();
		for (CompoundProp cp : getMatrix()) {
			if (cp.containsOnly(covered)) {
				ret.add(cp);
			}
		}
		return ret;
	}

	public Prop getSubset(List<Quantifier> coveredQuants) {
		List<Hecceity> hecs = Lists.newArrayList();
		for (Quantifier q : coveredQuants) {
			hecs.add(q.getHecceity());
		}
		Prop p = copyWithHecceities();
		p.prefix.clear();
		p.prefix.addAll(coveredQuants);
		p.matrix.clear();
		p.matrix.addAll(getSubsetOfMatrix(hecs));
		return p;
	}

	public boolean evaluate(Prop p) {
		for (CompoundProp cp : getMatrix()) {
			for (CompoundProp ccp : p.getMatrix()) {
				if (cp.evaluate(ccp)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean usesQuantifier(Quantifier q) {
		Hecceity h = q.getHecceity();
		for (CompoundProp cp : getMatrix()) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				if (ap.getHecceities().indexOf(h) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

}
