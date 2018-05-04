package com.lang;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lang.parse.Tokenizer.Token;

public class Prop {
	private static class IDMaker {
		private long c = 0;
		public long getID () {
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
		
		public Quantifier (QuantifierType type, Hecceity hecceity) {
			this.type = type;
			this.hecceity = hecceity;
		}
		
		public QuantifierType getType () { 
			return type;
		}
		
		public Hecceity getHecceity () {
			return hecceity;
		}
		
	}
	public static class AtomicProp {
		private final String name;
		private final List<Hecceity> hecceities;
		
		public AtomicProp (String name, List<Hecceity> ents) {
			this.name = name;
			this.hecceities = ents;
		}
		
		public String getName () {
			return name;
		}
		
		public List<Hecceity> getHecceities () {
			return hecceities;
		}
	}
	
	private List<Quantifier> prefix = Lists.newArrayList();
	private List<AtomicProp> matrix = Lists.newArrayList();
	private Map<Hecceity,String> h2s = Maps.newHashMap();
	private Map<String,Hecceity> s2h = Maps.newHashMap();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Quantifier q: prefix) {
			if (q.getType() == QuantifierType.FORALL) {
				sb.append("forall ");
			}
			else {
				sb.append("thereis ");
			}
			Hecceity h = q.getHecceity();
			sb.append(h2s.get(h));
		}
		sb.append(":");
		for (AtomicProp prop: matrix) {
			String name = prop.getName();
			sb.append(name+"(");
			for (Hecceity h:prop.getHecceities()) {
				String c = h2s.get(h);
				sb.append(c);
				sb.append(" ");
			}
			sb.append(")");
		}
		return sb.toString();
	}
	
	public void addQuantifier(Quantifier q) {
		this.prefix.add(q);
	}
	
	public void addAtomicProp (AtomicProp p) {
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

	public void addAtomicProp(String name, List<String> hecceities) {
		List<Hecceity> ents = Lists.newArrayList();
		for (String s:hecceities) {
			ents.add(s2h.get(s));
		}
		addAtomicProp(new AtomicProp(name, ents));
	}

}
