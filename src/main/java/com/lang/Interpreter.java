package com.lang;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;
import com.lang.val.Prop;
import com.lang.val.Prop.AtomicProp;
import com.lang.val.Prop.AtomicPropInfo;
import com.lang.val.Prop.CompoundProp;
import com.lang.val.Prop.Hecceity;
import com.lang.val.Prop.Quantifier;
import com.lang.val.Prop.QuantifierType;
import com.lang.val.Undefined;
import com.lang.val.Value;

public class Interpreter {

	private final TokenStream tokens;
	private final static List<Prop> constructors = Lists.newArrayList();

	public Interpreter(TokenStream s) {
		this.tokens = s;
	}

	private void evalPrefix(Prop p) throws ParseException {
		Token t;
		while (tokens.hasToken()) {
			t = tokens.getNext();
			if (t.getType().equals(TokenType.TT_COLON)) {
				break;
			}
			if (!(t.getType().equals(TokenType.TT_FORALL) || t.getType().equals(TokenType.TT_THEREIS))) {
				throw new ParseException("expecting quantifier", 0);
			}
			QuantifierType qt = t.getType().equals(TokenType.TT_FORALL) ? QuantifierType.FORALL
					: QuantifierType.THEREIS;

			t = tokens.getNext();
			String lit = t.getLit();
			t = tokens.peek();
			if (t.getType().equals(TokenType.TT_IN)) {
				tokens.getNext();
				t = tokens.getNext();
				p.addQuantifier(qt, lit, t.getLit());

			} else {
				p.addQuantifier(qt, lit);
			}
		}
	}

	/**
	 * recursively add all atomic props to a compound prop
	 *
	 * @param cp
	 * @throws ParseException
	 */
	private void addAtomicProp(CompoundProp cp) throws ParseException {
		boolean truthValue = !tokens.peek().getType().equals(TokenType.TT_TILDE);
		if (!truthValue) {
			// seek past the tilde
			tokens.getNext();
		}
		Token t = tokens.getNext();
		String name = t.getLit();
		List<String> hecceities = Lists.newArrayList();
		t = tokens.getNext();
		if (!t.getType().equals(TokenType.TT_LPAREN)) {
			throw new ParseException("expecting a LPAREN, got " + t.getLit(), 0);
		}
		t = tokens.getNext();
		while (!t.getType().equals(TokenType.TT_RPAREN)) {
			hecceities.add(t.getLit());
			t = tokens.getNext();
		}
		cp.addAtomicProp(name, hecceities, truthValue);
		if (tokens.hasToken()) {
			t = tokens.peek();
			if (t.getType().equals(TokenType.TT_ASTER)) {
				// seek past the asterisk
				tokens.getNext();
				addAtomicProp(cp);
			}

		}
	}

	private void evalMatrix(Prop p) throws ParseException {
		while (tokens.hasToken()) {
			CompoundProp cp = p.makeBlankCompoundProp();
			addAtomicProp(cp);
			p.addCompoundProp(cp);
			if (tokens.hasToken()) {
				if (tokens.peek().getType().equals(TokenType.TT_PERIOD)) {
					tokens.getNext();
					break;
				}
				if (!tokens.peek().getType().equals(TokenType.TT_PLUS)) {
					throw new ParseException(
							"expected + got " + tokens.peek().getType() + " with lit " + tokens.peek().getLit(), 0);
				}
				// increment forward
				tokens.getNext();
			}
		}
	}

	public Prop evalProp() throws ParseException {
		Prop p = new Prop();
		evalPrefix(p);
		evalMatrix(p);
		return p;
	}

	private void addPrefix(Prop p1, Prop p2, Prop p3) {
		for (Quantifier q : p1.getPrefix()) {
			p3.addQuantifierUnique(q);
		}
		for (Quantifier q : p2.getPrefix()) {
			p3.addQuantifierUnique(q);
		}
	}

	private void addAllCompoundProps(Prop base, Prop from) {
		for (CompoundProp p : from.getMatrix()) {
			CompoundProp np = base.makeBlankCompoundProp();
			for (AtomicProp ap : p.getAtomicProps()) {
				np.addAtomicProp(ap);
			}
			base.addCompoundProp(np);
		}
	}

	public void addMatrix(Prop p1, Prop p2, Prop p3) {
		addAllCompoundProps(p3, p1);
		addAllCompoundProps(p3, p2);
	}

	private void multMatrix(Prop p1, Prop p2, Prop p3) {
		for (CompoundProp m : p1.getMatrix()) {
			for (CompoundProp mc : p2.getMatrix()) {
				CompoundProp np = p3.makeBlankCompoundProp();
				for (AtomicProp ap : m.getAtomicProps()) {
					np.addAtomicProp(ap);
				}
				for (AtomicProp ap : mc.getAtomicProps()) {
					np.addAtomicProp(ap);
				}
				for (Quantifier q : p1.getPrefix()) {
					AtomicProp ap = q.getConstraint();
					if (ap != null) {
						np.addAtomicProp(ap);
					}
				}
				for (Quantifier q : p2.getPrefix()) {
					AtomicProp ap = q.getConstraint();
					if (ap != null) {
						np.addAtomicProp(ap);
					}
				}
				p3.addCompoundProp(np);
			}
		}
	}

	private Prop sumProps(Prop p1, Prop p2) {
		Prop prop3 = new Prop();
		addPrefix(p1, p2, prop3);
		addMatrix(p1, p2, prop3);
		return prop3;

	}

	private Value sum(Value v1, Value v2) {
		if (v1 instanceof Undefined || v2 instanceof Undefined) {
			return Undefined.undefined;
		}
		Prop p1 = (Prop) v1;
		Prop p2 = (Prop) v2;
		return sumProps(p1.copy(), p2.copy());
	}

	private static List<List<Quantifier>> getSegments(List<Quantifier> prefix) {
		List<List<Quantifier>> ret = Lists.newArrayList();
		QuantifierType qt = prefix.get(0).getType();
		List<Quantifier> intermediate = Lists.newArrayList();
		for (Quantifier q : prefix) {
			if (q.getType().equals(qt)) {
				intermediate.add(q);
			} else {
				ret.add(intermediate);
				intermediate = Lists.newArrayList(q);
				qt = q.getType();
			}
		}
		ret.add(intermediate);
		return ret;
	}

	private static <T> T tryToGet(List<T> l, int i) {
		try {
			return l.get(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	private static void multPrefix(Prop p1, Prop p2, Prop p3) {
		List<Quantifier> prefix1 = p1.getPrefix();
		List<Quantifier> prefix2 = p2.getPrefix();
		for (int i = 0, ii = Math.max(prefix1.size(), prefix2.size()); i < ii; i++) {
			Quantifier q1 = tryToGet(prefix1, i);
			Quantifier q2 = tryToGet(prefix2, i);
			if (q1 == null) {
				p3.addQuantifierUnique(q2);
			} else if (q2 == null) {
				p3.addQuantifierUnique(q1);
			} else {
				if (q2.getType().equals(QuantifierType.THEREIS)) {
					p3.addQuantifierUnique(q2);
					p3.addQuantifierUnique(q1);
				} else {
					p3.addQuantifierUnique(q1);
					p3.addQuantifierUnique(q2);
				}

			}
		}
	}

	private Prop prodProps(Prop p1, Prop p2) {
		Prop prop3 = new Prop();
		multPrefix(p1, p2, prop3);
		multMatrix(p1, p2, prop3);
		return prop3;
	}

	private Value product(Value v1, Value v2) {
		if (v1 instanceof Undefined || v2 instanceof Undefined) {
			return Undefined.undefined;
		}
		Prop p1 = (Prop) v1;
		Prop p2 = (Prop) v2;
		return prodProps(p1.copy(), p2.copy());
	}

	private final void addConstructors(Value v) {
		if (!(v instanceof Prop)) {
			return;
		}
		Prop p = (Prop) v;
		List<Quantifier> prefix = p.getPrefix();
		List<Quantifier> foralls = Lists.newArrayList();
		for (Quantifier q : prefix) {
			if (q.getType().equals(QuantifierType.FORALL)) {
				foralls.add(q);
			} else {
				break;
			}
		}
		if (foralls.size() == 0) {
			return;
		}
		constructors.add(p);
	}

	private static <T> List<T> reverse(List<T> in) {
		List<T> ret = Lists.newArrayList();
		for (int i = in.size() - 1, ii = -1; i > ii; i--) {
			ret.add(in.get(i));
		}
		return ret;
	}

	private static Prop removeDefects(Prop p) {
		return removeRedundant(removeContradictions(p));
	}

	private static List<Prop> collectProps(Prop p, List<Hecceity> hecceties) {
		List<Quantifier> pre = reverse(p.getPrefix());
		List<Quantifier> realPre = p.getPrefix();
		List<Prop> ret = Lists.newArrayList();
		for (Quantifier q : pre) {
			if (hecceties.indexOf(q.getHecceity()) >= 0) {
				// continue;
			}
			if (q.getType().equals(QuantifierType.FORALL)) {
				for (int i = 0, ii = realPre.indexOf(q); i < ii; i++) {
					Prop pp = p.copyWithHecceities().replace(q, realPre.get(i));
					pp = removeDefects(pp);
					if (pp.getMatrix().size() > 0) {
						ret.add(pp);
						ret.addAll(collectProps(pp, hecceties));
					}

				}
			}
		}
		return ret;
	}

	private Prop negateConstraints(List<Quantifier> prefix, Prop p1) throws ParseException {
		Prop p = new Prop();
		CompoundProp cp = p.makeBlankCompoundProp();
		for (Quantifier q : prefix) {
			AtomicProp ap = q.getConstraint();
			if (ap != null) {
				QuantifierType qt = q.getType().equals(QuantifierType.FORALL) ? QuantifierType.THEREIS
						: QuantifierType.FORALL;
				Quantifier nq = p.addQuantifier(qt);
				List<Hecceity> arg = Lists.newArrayList(nq.getHecceity());
				AtomicProp atom = new AtomicProp(ap.getName(), arg, false);
				cp.addAtomicProp(atom);
			}
		}
		p.addCompoundProp(cp);
		if (p.getPrefix().size() > 0) {
			return apply(p1, p);
		} else {
			return p1;
		}
	}


	private Prop apply(Prop p1, Prop p2) throws ParseException {
		Prop product = prodProps(p1, p2);
		List<Prop> all = collectProps(product, p1.getHecceties());
		if (all.size() == 0) {
			return negateConstraints(p2.getPrefix(), p1);
		}
		Prop base = all.get(0);
		for (int i = 1, ii = all.size(); i < ii; i++) {
			base = (prodProps(base, all.get(i)));
		}
		Prop check = removeDefects(base);
		if (check.getMatrix().size() == 0) {
			return negateConstraints(p2.getPrefix(), p1);
		}
		return check;
	}

	// we can assume that p has no contradictions
	private static Prop removeRedundant(Prop p) {
		Prop ret = new Prop();
		for (Quantifier q : p.getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		List<CompoundProp> compounds =  Lists.newArrayList();
		for (CompoundProp cp : p.getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			Set<AtomicProp> atoms = Sets.newHashSet();
			for (AtomicProp ap : cp.getAtomicProps()) {
				if (atoms.contains(ap)) {
					continue;
				}
				atoms.add(ap);
				ncp.addAtomicProp(ap);
			}
			if (compounds.contains(ncp)) {
				continue;
			}
			compounds.add(ncp);
			ret.addCompoundProp(ncp);
		}
		return ret;
	}

	private static <T> List<List<T>> getNtuples(List<T> l, int n) {
		List<List<T>> ret = Lists.newArrayList();
		if (n == l.size()) {
			ret.add(l);
			return ret;
		}
		List<T> copy = Lists.newArrayList();
		for (T t : l) {
			copy.add(t);
		}
		int remaining = n - 1;
		while (copy.size() >= n) {
			T head = copy.get(0);
			List<T> tuple = Lists.newArrayList();
			copy.remove(0);
			tuple.add(head);
			if (tuple.size() == n) {
				ret.add(tuple);
				continue;
			}
			for (int i = 1, ii = copy.size(); i <= ii; i++) {
				int endIndex = remaining + i;

				if (endIndex > copy.size()) {
					break;
				}
				for (int z = i; z < endIndex; z++) {
					tuple.add(copy.get(z));
				}
				ret.add(tuple);

			}
		}
		return ret;

	}

	private static <T> List<List<T>> rearrange(List<T> tuple) {
		List<List<T>> ret = Lists.newArrayList();
		if (tuple.size() == 1) {
			ret.add(tuple);
			return ret;
		}
		if (tuple.size() == 2) {
			List<T> a = Lists.newArrayList(tuple.get(0), tuple.get(1));
			List<T> b = Lists.newArrayList(tuple.get(1), tuple.get(0));
			ret.add(a);
			ret.add(b);
			return ret;
		}
		for (int i = 0, ii = tuple.size(); i < ii; i++) {
			T t = tuple.get(i);
			List<T> subTuple = Lists.newArrayList();
			for (int n = 0, nn = tuple.size(); n < nn; n++) {
				if (n == i) {
					continue;
				}
				subTuple.add(tuple.get(n));
			}
			List<List<T>> subPerms = rearrange(subTuple);
			for (List<T> sub : subPerms) {
				List<T> l = new ArrayList<T>();
				l.add(t);
				for (T tt : sub) {
					l.add(tt);
				}
				ret.add(l);
			}
		}
		return ret;
	}

	// we already know that n >= l.length
	private static <T> List<List<T>> getPermutations(List<T> l, int n) {
		List<List<T>> ret = Lists.newArrayList();
		List<List<T>> ntuples = getNtuples(l, n);

		for (List<T> tuple : ntuples) {
			ret.addAll(rearrange(tuple));
		}
		return ret;
	}

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

	private static Prop removeContradictions(Prop p) {
		Prop ret = new Prop();
		for (Quantifier q : p.getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		for (CompoundProp cp : p.getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			if (!contradiction(cp)) {
				ncp.addAllAtomicProp(cp.getAtomicProps());
				
				ret.addCompoundProp(ncp);
			}
		}
		return ret;
	}

	private static boolean primitiveNatOp(String name) {
		return name == "EQ" || name == "SUM" || name == "PRODUCT" || name == "DIFFERENCE";
	}

	private static boolean natLit(String name) {
		return name.matches("^\\d");
	}

	private static BigInteger evalNatLit(String name) {
		String lit = "";
		int i = 0;
		int ii = name.length();
		for (; i < ii; i++) {
			char c = name.charAt(i);
			if (c >= '0' && c <= '9') {
				lit += name.charAt(i);
			} else {
				break;
			}
		}
		return new BigInteger(lit);
	}

	private BigInteger resolveNatPrim(AtomicProp atomicProp) {
		String name = atomicProp.getName();
		List<Hecceity> hecceities = atomicProp.getHecceities();
		if (name == "EQ") {

		}
		return null;
	}

	private BigInteger resolveNat(AtomicProp atomicProp) {
		String name = atomicProp.getName();
		if (natLit(name)) {
			return evalNatLit(name);
		} else if (primitiveNatOp(name)) {
			return resolveNatPrim(atomicProp);
		} else {
			return null;
		}

	}

	private void checkForNativeVals(Prop p) throws ParseException {
		for (CompoundProp cp : p.getMatrix()) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				String predName = ap.getName();
				if (predName.equals("INT")) {
					if (ap.getHecceities().size() > 1) {
						throw new ParseException("NAT is predefined", 0);
					}
					BigInteger b = resolveNat(ap);

				} else if (predName.equals("POLY")) {

				} else if (predName.equals("ARRAY")) {

				} else if (predName.equals("SET")) {

				} else if (predName.equals("DIGRAPH")) {

				} else if (predName.equals("RAT")) {

				} else if (predName.equals("REAL")) {

				}
			}
		}
	}

	private Value doInference(Value v) throws ParseException {
		if (!(v instanceof Prop)) {
			return Undefined.undefined;
		}
		Prop p = (Prop) v;
		List<Prop> facts = Lists.newArrayList();

		for (Prop constructor : constructors) {
			List<Prop> factors = p.getIndividualFacts();
			List<Prop> newFactors = Lists.newArrayList();
			for (Prop f : factors) {
				f = apply(f, constructor.copy());
				newFactors.add(f);
			}
			Prop base = newFactors.get(0);
			for (int i = 1, ii = newFactors.size(); i < ii; i++) {
				base = prodProps(base, newFactors.get(i));
			}
			facts.add(base);

		}
		Prop base = facts.get(0);
		for (int i = 1, ii = facts.size(); i < ii; i++) {
			base = prodProps(base, facts.get(i));
		}
		checkForNativeVals(p);
		return removeDefects(base);

	}

	public Value eval(Environment env) throws ParseException {
		Token t = tokens.peek();
		if (t.getType().equals(TokenType.TT_COLON)) {
			tokens.getNext();
			int i = Integer.parseInt(tokens.getNext().getLit());
			Value v = eval(env);
			Prop p = (Prop) v;
			return p.copySpecificProp(i);

		}
		if (t.getType().equals(TokenType.TT_PERCENT)) {
			tokens.getNext();
			t = tokens.getNext();
			int i = Integer.parseInt(t.getLit());
			Prop p1 = (Prop) eval(env);
			return removeDefects(p1.extractPropFromQuant(i));
		}
		if (t.getType().equals(TokenType.TT_PLUS)) {
			// move forward
			tokens.getNext();
			Value v1 = eval(env);
			Value v2 = eval(env);
			return sum(v1, v2);
		}
		if (t.getType().equals(TokenType.TT_ASTER)) {
			tokens.getNext();
			Value v1 = eval(env);
			Value v2 = eval(env);
			return product(v1, v2);
		}
		if (t.getType().equals(TokenType.TT_DOLLAR)) {
			tokens.getNext();
			Value v = eval(env);
			return doInference(v);
		}
		if (t.getType().equals(TokenType.TT_HASH)) {
			tokens.getNext();
			t = tokens.getNext();
			int i = Integer.parseInt(t.getLit());
			t = tokens.getNext();
			int ii = Integer.parseInt(t.getLit());
			Prop p = (Prop)eval(env);
			p = p.copyWithHecceities();
			List<Quantifier> quants = p.getPrefix();
			return removeDefects(p.replace(quants.get(i), quants.get(ii)));
		}
		//TODO: this should return a value
		if (t.getType().equals(TokenType.TT_AT)) {
			tokens.getNext();
			Value v1 = eval(env);
			Value v2 = eval(env);
			return apply((Prop) v1, (Prop) v2);
		}
		if (t.getType().equals(TokenType.TT_VAR)) {
			tokens.getNext();
			String varName = t.getLit();
			if (tokens.hasToken()) {
				t = tokens.peek();
				if (t.getType().equals(TokenType.TT_EQUALS)) {
					tokens.getNext();
					Value p = eval(env);
					env.put(varName, p);
					return p;
				}
			}
			return env.lookUp(varName);
		} else {
			Prop p = evalProp();
			addConstructors(p);
			addHecs(p);
			return p;
		}

	}

	private static void addHecs(Prop p) {
		for (CompoundProp cp : p.getMatrix()) {
			for (AtomicProp ap : cp.getAtomicProps()) {
				String name = ap.getName();
				List<Hecceity> hecs = ap.getHecceities();

			}
		}

	}
}
