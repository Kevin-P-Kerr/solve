package com.lang.val;

import java.util.List;

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
		private final String name;

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

	}

	public static class BooleanPart {
		private final List<ConjunctProp> conjunctions;

		public BooleanPart(List<ConjunctProp> conjunctions) {
			this.conjunctions = conjunctions;
		}
	}

	public static class ConjunctProp {
		private final List<AtomicProp> atoms;

		public ConjunctProp(List<AtomicProp> atoms) {
			this.atoms = atoms;
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
	}

	public static class Heccity {
		private final String name;

		public Heccity(String n) {
			this.name = n;
		}
	}

	private final Quantifier quantifier;
	private final BooleanPart booleanPart;

	public Prop(Quantifier q, BooleanPart b) {
		this.quantifier = q;
		this.booleanPart = b;
	}

}
