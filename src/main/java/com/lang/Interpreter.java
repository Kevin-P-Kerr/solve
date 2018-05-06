package com.lang;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
	private final static Map<String,List<Prop>> constructors = Maps.newConcurrentMap();

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
			throw new ParseException("expecting a LPAREN, got "+t.getLit(), 0);
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
					System.out.println(p.toString());
					System.out.println(tokens.toString());
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
	
	private void addAllCompoundProps (Prop base, Prop from) {
		for (CompoundProp p: from.getMatrix()) {
			CompoundProp np = base.makeBlankCompoundProp();
			for (AtomicProp ap:p.getAtomicProps()) {
				np.addAtomicProp(ap);
			}
			base.addCompoundProp(np);
		}
	}

	public void addMatrix(Prop p1, Prop p2, Prop p3) {
		addAllCompoundProps(p3,p1);
		addAllCompoundProps(p3,p2);
	}
	
	private void multMatrix(Prop p1, Prop p2, Prop p3) {
		for (CompoundProp m: p1.getMatrix()) {
			for (CompoundProp mc: p2.getMatrix()) {
				CompoundProp np = p3.makeBlankCompoundProp();
				for (AtomicProp ap: m.getAtomicProps()) {
					np.addAtomicProp(ap);
				}
				for (AtomicProp ap: mc.getAtomicProps()) {
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
		addPrefix(p1,p2,prop3);
		multMatrix(p1,p2,prop3);
		return prop3;
	}
	
	private Value product(Value v1, Value v2) {
		if (v1 instanceof Undefined || v2 instanceof Undefined) {
			return Undefined.undefined;
		}
		Prop p1 = (Prop) v1;
		Prop p2 = (Prop) v2;
		return prodProps(p1,p2);
	}
	
	private final void addConstructors (Value v) {
		if (!(v instanceof Prop)) {
			return;
		}
		Prop p = (Prop) v;
		List<Quantifier> prefix = p.getPrefix();
		List<Quantifier> foralls = Lists.newArrayList();
		for (Quantifier q: prefix) {
			if (q.getType().equals(QuantifierType.FORALL)) {
				foralls.add(q);
			}
			else {
				break;
			}
		}
		List<CompoundProp> matrix = p.getMatrix();
		if (foralls.size() == 0) {
			return;
		}
		for (CompoundProp cp: matrix) {
			for (AtomicProp ap:cp.getAtomicProps()) {
				String name = ap.getName();
				List<Prop> props = constructors.get(name);
				if (props == null) {
					props = Lists.newArrayList();
					constructors.put(name, props);
				}
				if (!props.contains(p)) {
					props.add(p);
				}
				
			}
		}
		
	}
	
	private void getConstructorsRecurs(Prop p, List<Prop> constructors) {
		if (constructors.contains(p)) {
			return;
		}
		for (CompoundProp cp: p.getMatrix()) {
			for (AtomicProp ap: cp.getAtomicProps()) {
				List<Prop> intermediateConstructors = Interpreter.constructors.get(ap.getName());
				if (intermediateConstructors == null) {
					continue;
				}
				constructors.addAll(intermediateConstructors);
				for (Prop ip: intermediateConstructors) {
					getConstructorsRecurs(ip, constructors);
				}
			}
		}
		return;
	}
	
	private List<Prop> getConstructors (Prop p) {
		List<Prop> ret = Lists.newArrayList();
		 getConstructorsRecurs(p,ret);
		 return ret;
	}
	
	private Map<Hecceity,List<String>> invertPredMap (Map<String,List<Hecceity>> argMap) {
		Map<Hecceity,List<String>> ret = Maps.newHashMap();
		for (Entry<String, List<Hecceity>> entry: argMap.entrySet()) {
			for (Hecceity h : entry.getValue()) {
				List<String> v = ret.get(h);
				if (v == null) {
					v = Lists.newArrayList();
					ret.put(h, v);
				}
				v.add(entry.getKey());
			}
		}
		return ret;
	}
	
	private Prop apply(Prop p, Prop constructor) {
		/*
		 * to apply a constructor to a prop, 
		 * first create a new prop which we will return
		 * then populate the prefix of the prop by the following method
		 *   for each hecceity in the constructor
		 *      if that hecceity populates a boolean predicate found in the prop, replace that hecceity with the one in the prod
		 */
		 Map<Hecceity, List<String>> preds2hecceity = invertPredMap(constructor.getPredicates2Hecceity());
		 //Map<Hecceity, List<String>> propPreds2h = invertPredMap(p.getPredicates2Hecceity());
		 Prop ret = new Prop();
		 boolean forallFlag = true;
		 for (Quantifier q:constructor.getPrefix()) {
			 QuantifierType qt;
			 if (forallFlag) {
				 if (q.getType().equals(QuantifierType.THEREIS)) {
					 forallFlag = false;
				 }
				 qt = QuantifierType.THEREIS;
			 }
			 else {
				 qt = q.getType();
			 }
			 List<String> preds = preds2hecceity.get(q.getHecceity());
			 boolean foundPred = false;
			 for (String pred:preds) {
				 List<Hecceity> ph = p.getPredicates2Hecceity().get(pred);
				 if (ph == null) {
					 continue;
				 }
				 List<Hecceity> ch = constructor.getPredicates2Hecceity().get(pred);
				 int i = ch.indexOf(q.getHecceity());
				 Hecceity h = ph.get(i);
				 Quantifier quantifier = new Quantifier(qt, h);
				 ret.addQuantifierUnique(quantifier);
				 foundPred = true;
				 break;
			 }
			 if (!foundPred) {
				 ret.addQuantifier(qt);
			 }
		 }
		 List<Hecceity> constructorHecs = constructor.getHecceties();
		 List<Hecceity> corresponding = ret.getHecceties();
		 // matrix
		 for (CompoundProp cp:constructor.getMatrix()) {
			 CompoundProp ncp = ret.makeBlankCompoundProp();
			 for (AtomicProp ap:cp.getAtomicProps()) {
				 String name = ap.getName();
				 List<Hecceity> hecs = ap.getHecceities();
				 List<Hecceity> corh = Lists.newArrayList();
				 for (Hecceity h:hecs) {
					 int i = constructorHecs.indexOf(h);
					 corh.add(corresponding.get(i));
				 }
				 AtomicProp nap = new AtomicProp(name,corh,ap.getTruthValue());
				 ncp.addAtomicProp(nap);
				 
			 }
			 ret.addCompoundProp(ncp);
		 }
		 return prodProps(p, ret);
	}
	
	
	private Value doInference (Value v) {
		if (!(v instanceof Prop)) {
			return Undefined.undefined;
		}
		Prop p = (Prop) v;
		List<Prop> consts = getConstructors(p);
		for (Prop constructor: consts) {
			p =apply(p, constructor);
		}
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
			return product(v1,v2);
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
					addConstructors(p);
					env.put(varName, p);
					return p;
				}
			}
			return env.lookUp(varName);
		} else {
			return evalProp();
		}

	}

}
