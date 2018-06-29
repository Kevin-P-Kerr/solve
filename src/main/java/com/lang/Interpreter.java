package com.lang;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
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

	// TODO: fold these into Prop
	private static Prop removeDefects(Prop p) {
		return p.removeContradictions().removeRedundant();
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
			for (TokenStream ln : lines.subList(0, lines.size() - 1)) {
				try {
					Interpreter interp = new Interpreter(ln.copy());
					v = interp.eval(env);
				} catch (Exception e) {
					throw new LogicException("bad eval");
				}
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
			if (interpreterContext.getCurrentTactic() != null) {
				Tactic ret = interpreterContext.getCurrentTactic();
				interpreterContext.setCurrentTactic(null);
				return ret;
			}
			if (hypothesisContext == null) {
				t = tokens.getNext();
				String name = t.getLit();
				tokens.getClass();
				Prop hypo = (Prop) eval(env);
				hypothesisContext = new HypothesisContext(hypo, name);
				interpreterContext.setHypothesisContext(hypothesisContext);
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
					interpreterContext.setHypothesisContext(null);
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
				Prop p = (Prop) eval(env);
				p = p.copyWithHecceities();
				args.add(p);
			}
			return tactic.eval(args);
		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("tactic")) {
			tokens.getNext();
			List<String> argNames = Lists.newArrayList();
			while (tokens.hasToken()) {
				t = tokens.getNext();
				argNames.add(t.getLit());
			}
			Environment t_env = new Environment(env);
			Tactic tactic = new Tactic(t_env, argNames);
			interpreterContext.setCurrentTactic(tactic);
			return tactic;

		}
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("apply")) {
			tokens.getNext();
			Prop applicator = (Prop) eval(env);
			Prop applicand = (Prop) eval(env);
			List<String> variables = Lists.newArrayList();
			while (tokens.hasToken()) {
				t = tokens.getNext();
				variables.add(t.getLit());
			}
			return applyProp(applicator, applicand, variables);
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
		if (t.getType().equals(TokenType.TT_VAR) && t.getLit().equals("extract")) {
			tokens.getNext();
			t = tokens.getNext();
			String filename = t.getLit();
			String program  = extract(env);
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(filename));
				writer.write(program);
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			return p;
		}

	}
	
	private static String extract(Environment env) {
		
	}

	private static Prop applyProp(Prop applicator, Prop applicand, List<String> variables) throws LogicException {
		Prop applicandCopy = applicand.copy();
		Prop product = prodProps(applicator.copyWithHecceities(), applicandCopy);
		List<String> counterParts = Lists.newArrayList();
		Map<Hecceity, String> h2s = product.getH2S();
		for (Hecceity h : applicandCopy.getHecceties()) {
			String s = h2s.get(h);
			counterParts.add(s);
		}
		if (counterParts.size() != variables.size()) {
			throw new LogicException("arg mismatch");
		}
		for (int i = 0, ii = counterParts.size(); i < ii; i++) {
			product = product.replace(counterParts.get(i), variables.get(i));
		}
		return removeDefects(product);

	}

	public Value enterEval(Environment env) throws ParseException, LogicException {
		if (interpreterContext.getCurrentTactic() != null) {
			interpreterContext.getCurrentTactic().addLine(tokens.copy());
		}
		return eval(env);
	}

	public static class InterpreterContext {
		private HypothesisContext hc = null;
		private Tactic currentTactic = null;

		public void setHypothesisContext(HypothesisContext hc) {
			this.hc = hc;
		}

		public void setCurrentTactic(Tactic t) {
			this.currentTactic = t;
		}

		public HypothesisContext getHypothesisContext() {
			return this.hc;
		}

		public Tactic getCurrentTactic() {
			return this.currentTactic;
		}
	}
}
