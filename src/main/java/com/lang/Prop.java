package com.lang;

import java.util.List;

import com.google.common.collect.Lists;

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
		
	}
	public static class AtomicProp {
		private final String name;
		private final List<Hecceity> hecceities;
		
		public AtomicProp (String name, List<Hecceity> ents) {
			this.name = name;
			this.hecceities = ents;
		}
	}
	
	private List<Quantifier> prefix = Lists.newArrayList();
	private List<AtomicProp> matrix = Lists.newArrayList();

}
