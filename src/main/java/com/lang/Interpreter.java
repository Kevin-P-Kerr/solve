package com.lang;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;
import com.lang.val.Prop;
import com.lang.val.Prop.AtomicProp;
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
			p.addQuantifier(qt, t.getLit());
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
		return sumProps(p1, p2);
	}

	private Prop prodProps(Prop p1, Prop p2) {
		Prop prop3 = new Prop();
		addPrefix(p1, p2, prop3);
		multMatrix(p1, p2, prop3);
		System.out.println(prop3.toString());
		return prop3;
	}

	private Value product(Value v1, Value v2) {
		if (v1 instanceof Undefined || v2 instanceof Undefined) {
			return Undefined.undefined;
		}
		Prop p1 = (Prop) v1;
		Prop p2 = (Prop) v2;
		return prodProps(p1, p2);
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
		List<CompoundProp> matrix = p.getMatrix();
		if (foralls.size() == 0) {
			return;
		}
		constructors.add(p);
	}
	private Prop apply(Prop p, Prop constructor) throws ParseException {
		int numOfForall = 0;
		for (Quantifier q: constructor.getPrefix()) {
			if (q.getType().equals(QuantifierType.FORALL)) {
				numOfForall++;
			}
			else {
				break;
			}
		}
		int numThereis = 0;
		List<Quantifier> thereisQuants = Lists.newArrayList();
		for (Quantifier q: p.getPrefix()) {
			if (q.getType().equals(QuantifierType.THEREIS)) {
				numThereis++;
				thereisQuants.add(q);
			}
		}
		if (numThereis < numOfForall) {
			return p;
		}
		List<List<Quantifier>> allQuants = getPermutations(thereisQuants,numOfForall);
		 
		
		
	}
	
	private static <T> List<List<T>> getNtuples(List<T> l, int n) {
		List<List<T>> ret = Lists.newArrayList();
		if (n == l.size()) {
			ret.add(l);
			return ret;
		}
		List<T> copy = Lists.newArrayList();
		for (T t: l) {
			copy.add(t);
		}
		int remaining = n-1;
		while (copy.size() >= n) {
			for (int i = 1,ii=copy.size();i<ii;i++) {
				T head = copy.get(0);
				List<T> tuple = Lists.newArrayList();
				tuple.add(head);
				int endIndex = remaining+i;
				if (endIndex >= copy.size()) {
					break;
				}
				for (int z = i;z<endIndex;z++) {
					tuple.add(copy.get(z));
				}
				ret.add(tuple);
				copy.remove(0);
			}
		}
		return ret;
		
	}
	
	private static <T> List<List<T>> rearrange(List<T> tuple) {
		List<List<T>> ret = Lists.newArrayList();
		if (tuple.size() == 2) {
			List<T> a = Lists.newArrayList(tuple.get(0),tuple.get(1));
			List<T> b = Lists.newArrayList(tuple.get(1),tuple.get(0));
			ret.add(a);
			ret.add(b);
			return ret;
		}
		for(int i=0, ii = tuple.size();i<ii;i++) {
			T t = tuple.get(i);
			List<T> subTuple = Lists.newArrayList();
			for (int n=0,nn=tuple.size();n<nn;n++) {
				if (n == i) {
					continue;
				}
				subTuple.add(tuple.get(n));
			}
			List<List<T>> subPerms = rearrange(subTuple);
			for (List<T> sub: subPerms) {
				List<T> l = Lists.newArrayList(t);
				for (T tt: sub) {
					l.add(tt);
				}
				ret.add(l);
			}
		}
		return ret;
	}
	
	// we already know that n >= l.length
	private static <T> List<List<T>> getPermutations(List<T> l,int n) {
		List<List<T>> ret = Lists.newArrayList();
		List<List<T>> ntuples = getNtuples(l,n);
		
		for (List<T>tuple: ntuples) {
			ret.addAll(rearrange(tuple));
		}
		return ret;
	}
	
	private boolean contradiction(CompoundProp cp) {
		Map<String, Boolean> boolMap = Maps.newHashMap();
		for (AtomicProp ap : cp.getAtomicProps()) {
			String name = ap.getName();
			Boolean b = boolMap.get(name);
			if (b == null) {
				boolMap.put(name, ap.getTruthValue());
				continue;
			}
			if (b != ap.getTruthValue()) {
				return true;
			}
		}
		return false;
	}

	private Prop removeContradictions(Prop p) {
		Prop ret = new Prop();
		for (Quantifier q : p.getPrefix()) {
			ret.addQuantifierUnique(q);
		}
		for (CompoundProp cp : p.getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			if (!contradiction(cp)) {
				for (AtomicProp ap : cp.getAtomicProps()) {
					ncp.addAtomicProp(ap);
				}
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
		Map<Hecceity, String> nativeValMap = Maps.newHashMap();
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
		for (Prop constructor : constructors) {
			p = apply(p, constructor);
		}
		checkForNativeVals(p);
		return p;

	}

	public Value eval(Environment env) throws ParseException {
		Token t = tokens.peek();
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
