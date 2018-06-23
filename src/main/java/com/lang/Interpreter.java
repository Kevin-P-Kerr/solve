package com.lang;

import java.beans.PropertyDescriptor;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;
import com.lang.val.Prop;
import com.lang.val.Prop.AtomicProp;
import com.lang.val.Prop.CompoundProp;
import com.lang.val.Prop.Hecceity;
import com.lang.val.Prop.LogicException;
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

	private static void multMatrix(Prop p1, Prop p2, Prop p3) {
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

	private static int countThereis(List<Quantifier> quants) {
		int ret = 0;
		for (Quantifier q : quants) {
			if (q.getType().equals(QuantifierType.THEREIS)) {
				ret++;
			}
		}
		return ret;
	}

	private static void multPrefix(Prop p1, Prop p2, Prop p3) {
		// TODO: revisit this
		/*
		 * List<Quantifier> prefix1 = p1.getPrefix(); List<Quantifier> prefix2 = p2.getPrefix(); List<Quantifier>
		 * greater; List<Quantifier> lesser; List<Quantifier> order1 = Lists.newArrayList(); List<Quantifier> order2 =
		 * Lists.newArrayList(); int num1 = countThereis(prefix1); int num2 = countThereis(prefix2); if (num1 > num2) {
		 * greater = prefix1; lesser = prefix2; } else { greater = prefix2; lesser = prefix1; }
		 * p3.addAllQuants(greater); p3.addAllQuants(lesser); p3.addQuantifierConstraint(order1);
		 * p3.addQuantifierConstraint(order2);
		 */
		p3.addAllQuants(p1.getPrefix());
		p3.addAllQuants(p2.getPrefix());
	}

	private static Prop prodProps(Prop p1, Prop p2) {
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
		boolean copyHecceities = !p1.sharesHecceities(p2);
		if (copyHecceities) {
			return prodProps(p1.copyWithHecceities(), p2.copyWithHecceities());
		}
		return prodProps(p1.copyWithHecceities(), p2.copy());
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

	// TODO: fold these into Prop
	private static Prop removeDefects(Prop p) {
		return p.removeContradictions().removeRedundant();
	}

	private static List<Prop> collectProps(Prop p, List<Hecceity> hecceties) throws LogicException {
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

	private Prop negateConstraints(List<Quantifier> prefix, Prop p1) throws ParseException, LogicException {
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

	private Prop apply(Prop p1, Prop p2) throws ParseException, LogicException {
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

	private Value doInference(Value v) throws ParseException, LogicException {
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

	private HypothesisContext hypothesisContext = null;
	private InterpreterContext interpreterContext = new InterpreterContext();

	public InterpreterContext getInterpreterContext() {
		return interpreterContext;
	}

	public void setInterpreterContext(InterpreterContext context) {
		this.interpreterContext = context;
		this.hypothesisContext = interpreterContext.getHypothesisContext();
	}

	private static class Tactic extends Value {
		private final List<TokenStream> lines = Lists.newArrayList();
		private final List<String> envNames;
		private final Environment env;

		public Tactic(Environment env, List<String> names) {
			this.env = env;
			this.envNames = names;
		}

		public Value eval(List<Value> args) throws LogicException, ParseException {
			Value v = Undefined.undefined;
			if (args.size() != envNames.size()) {
				throw new LogicException("args and passed vals must agree");
			}
			for (int i = 0, ii = args.size(); i < ii; i++) {
				env.put(envNames.get(i), args.get(i));
			}
			for (TokenStream ln : lines) {
				Interpreter interp = new Interpreter(ln);
				v = interp.eval(env);
				
			}
			return env.lookUp(envNames.get(0));
		}

		public void addLine(TokenStream tokens) {
			lines.add(tokens);
		}
	}

	private static class HypothesisContext {
		private final Prop hypothesis;
		private final String name;
		private Prop currentHypothesis;
		private boolean proven = false;
		private List<Quantifier> coveredQuants = Lists.newArrayList();
		private Prop entity;
		private int entityCount = 0;
		private int entityIndex = 0;
		private boolean isInduction = false;
		private boolean baseCaseProven = false;

		public HypothesisContext(Prop hy, String name) {
			this.name = name;
			this.hypothesis = hy;
		}

		public boolean isProven() {
			return proven;
		}

		public Prop getHypothesis() {
			return hypothesis;
		}

		public boolean compare(Prop p) {
			if (p == currentHypothesis) {
				return false; // no cheating!
			}
			if (currentHypothesis.evaluate(p)) {
				if (entity != null) {
					System.out.println("subcase proven");
					if (isInduction && !baseCaseProven) {
						baseCaseProven = true;
					}
					entityCount++;
					if (entityCount < entity.getMatrix().size()) {
						return true;
					} else {
						entity = null;
						entityCount = 0;
					}
				}
				if (currentHypothesis.getPrefix().size() == hypothesis.getPrefix().size()) {
					proven = true;
				}
				return true;
			}
			return false;
		}

		public Prop getNextHypothesis() {
			int currentIndex = coveredQuants.size() == 0 ? 0
					: hypothesis.getPrefix().indexOf(coveredQuants.get(coveredQuants.size() - 1));
			currentIndex++;
			if (coveredQuants.size() < hypothesis.getPrefix().size()) {
				coveredQuants.add(hypothesis.getPrefix().get(currentIndex));
			}
			Prop p = hypothesis.getSubset(coveredQuants);
			for (Quantifier q : coveredQuants) {
				if (!p.usesQuantifier(q)) {
					p = getNextHypothesis();
					break;
				}
			}
			List<Quantifier> newPrefix = Lists.newArrayList();
			for (Quantifier q : p.getPrefix()) {
				newPrefix.add(new Quantifier(QuantifierType.THEREIS, q.getHecceity()));
			}
			p.getPrefix().clear();
			p.getPrefix().addAll(newPrefix);
			currentHypothesis = p;
			return p;
		}

		public Prop getNextEntity() {
			if (hasCase()) {
				try {
					return getCase(entityIndex);
				} catch (LogicException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			int currentIndex = coveredQuants.size() == 0 ? 0
					: hypothesis.getPrefix().indexOf(coveredQuants.get(coveredQuants.size() - 1));
			boolean hasThereis = false;
			for (int ii = hypothesis.getPrefix().size(); currentIndex < ii; currentIndex++) {
				Quantifier q = hypothesis.getPrefix().get(currentIndex);
				if (q.getType().equals(QuantifierType.THEREIS)) {
					hasThereis = true;
					break;
				} else {
					coveredQuants.add(q);
				}
			}
			Prop p;
			if (hasThereis) {
				p = hypothesis.getSubset(coveredQuants).negateMatrix();
			} else {
				Prop coveredCompounds = hypothesis.getSubset(coveredQuants);
				coveredCompounds.getMatrix().remove(coveredCompounds.getMatrix().size() - 1);
				p = coveredCompounds.negateMatrix();
			}
			List<Quantifier> newPrefix = Lists.newArrayList();
			for (Quantifier q : p.getPrefix()) {
				newPrefix.add(new Quantifier(QuantifierType.THEREIS, q.getHecceity()));
			}
			p.getPrefix().clear();
			p.getPrefix().addAll(newPrefix);
			return removeDefects(p);
		}

		public String getName() {
			return name;
		}

		public boolean hasCase() {
			return entity != null;
		}

		public void setCase(Prop p) {
			entity = p;
		}

		public Prop getCase(int index) throws LogicException {
			entityIndex = (1 + index) % entity.getMatrix().size();
			Prop ret = entity.getCase(index);
			if (isInduction && baseCaseProven) {
				return removeDefects(prodProps(ret.copyWithHecceities(), currentHypothesis.copyWithHecceities()));
			}
			return ret;
		}

		@Deprecated
		public void startInduction() {
			 isInduction = true;
			 baseCaseProven = false;
		}	
	}

	public Value eval(Environment env) throws ParseException, LogicException {
		if (!tokens.hasToken()) {
			return Undefined.undefined;
		}
		Token t = tokens.peek();
		if (t.getType().equals(TokenType.TT_EXCLAIM)) {
			tokens.getNext();
			Prop p = (Prop) eval(env);
			return removeDefects(p.negateMatrix());
		}
		if (t.getType().equals(TokenType.TT_RBRAK)) {
			tokens.getNext();
			t = tokens.getNext();
			String from = t.getLit();
			t = tokens.getNext();
			String to = t.getLit();
			Prop p = (Prop) eval(env);
			p = p.copyWithHecceities();
			return p.swapQuantifiers(from, to);
		}
		if (t.getType().equals(TokenType.TT_LBRAK)) {
			tokens.getNext();
			t = tokens.getNext();
			String from = t.getLit();
			Prop p = (Prop) eval(env);
			p = p.copyWithHecceities();
			return p.invertQuantifier(from);
		}
		if (t.getType().equals(TokenType.TT_COLON)) {
			tokens.getNext();
			if (hypothesisContext == null) {
				t = tokens.getNext();
				String name = t.getLit();
				tokens.getClass();
				Prop hypo = (Prop) eval(env);
				hypothesisContext = new HypothesisContext(hypo, name);
				Prop ret = hypothesisContext.getNextEntity();
				env.put("given", ret);
				return ret;
			}
			if (!tokens.hasToken()) {
				Prop prop = hypothesisContext.getNextHypothesis();
				env.put("intermediate", prop);
				return prop;
			}
			Prop p = (Prop) eval(env);
			if (hypothesisContext.compare(p)) {
				if (hypothesisContext.isProven()) {
					System.out.println(hypothesisContext.getName() + " is proven");
					env.put(hypothesisContext.getName(), hypothesisContext.getHypothesis());
					Prop ret = hypothesisContext.getHypothesis();
					String name = hypothesisContext.getName();
					env.put("intermediate", Undefined.undefined);
					hypothesisContext = null;
					return ret;
				}
				Prop ret = hypothesisContext.getNextEntity();
				if (hypothesisContext.hasCase()) {
					env.put("subcase", ret);
				} else {
					env.put("given", ret);
				}
				return ret;
			} else {
				System.out.println("proof not accepted");
				return Undefined.undefined;
			}

		}
		if (t.getType().equals(TokenType.TT_PERCENT)) {
			tokens.getNext();
			t = tokens.getNext();
			int i = Integer.parseInt(t.getLit());
			Prop p1 = (Prop) eval(env);
			return removeDefects(p1.getIndividualFacts().get(i));
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
			t = tokens.peek();

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
			String from = t.getLit();
			t = tokens.getNext();
			String to = t.getLit();
			Prop p = (Prop) eval(env);
			p = p.copyWithHecceities();
			return removeDefects(p.replace(from, to));
		}
		if (t.getType().equals(TokenType.TT_SLASH)) {
			tokens.getNext();
			t = tokens.getNext();
			int i = Integer.parseInt(t.getLit());
			Prop p = (Prop) eval(env);
			p = p.copyWithHecceities();
			List<Prop> factors = p.factor();
			return factors.get(i);
		}
		// TODO: this should return a value
		if (t.getType().equals(TokenType.TT_AT)) {
			tokens.getNext();
			Tactic tactic = (Tactic) eval(env);
			List<Value> args = Lists.newArrayList();
			while (tokens.hasToken()) {
				args.add(eval(env));
			}
			return tactic.eval(args);
		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("tactic")) {
			tokens.getNext();
			t = tokens.getNext();

			String tacticName = t.getLit();
			t = tokens.getNext();
			List<String> argNames = Lists.newArrayList();
			while (!t.getType().equals(TokenType.TT_COLON)) {
				argNames.add(t.getLit());
				t = tokens.getNext();
			}
			Environment t_env = new Environment(env);
			Tactic tactic = new Tactic(t_env, argNames);
			interpreterContext.setCurrentTactic(tactic);
			Prop ret = hypothesisContext.getNextEntity();
			env.put("given", ret);
			return ret;

		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("case")) {
			tokens.getNext();
			t = tokens.getNext();
			if (!hypothesisContext.hasCase()) {
				hypothesisContext.setCase((Prop) env.lookUp("given"));
			}
			int i = Integer.parseInt(t.getLit());
			Prop p = hypothesisContext.getCase(i);
			env.put("subcase", p);
			return p;
		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("merge")) {
			tokens.getNext();
			Prop p1 = (Prop) eval(env);
			Prop p2 = (Prop) eval(env);
			return prodProps(p1.copyWithHecceities(), p2.copyWithHecceities());
		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("induct")) {
			tokens.getNext();
			t = tokens.getNext();
			if (!hypothesisContext.hasCase()) {
				hypothesisContext.setCase((Prop) env.lookUp("given"));
				hypothesisContext.startInduction();
			}
			int i = Integer.parseInt(t.getLit());
			Prop p = hypothesisContext.getCase(i);
			env.put("subcase", p);
			return p;
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

	public Value enterEval(Environment env) throws ParseException, LogicException {
		if (interpreterContext.getCurrentTactic() != null) {
			interpreterContext.getCurrentTactic().addLine(tokens);
		}
		return eval(env);
	}
	
	public static class InterpreterContext {
		private HypothesisContext hc = null;
		private Tactic currentTactic = null;
		
		public void setHypothesisContext (HypothesisContext hc) {
			this.hc = hc;
		}
		
		public void setCurrentTactic(Tactic t) {
			this.currentTactic = t;
		}
		
		public HypothesisContext getHypothesisContext () {
			return this.getHypothesisContext();
		}
		
		public Tactic getCurrentTactic () {
			return this.currentTactic;
		}
	}
}
