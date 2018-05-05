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
		Token t = tokens.getNext();
		String name = t.getLit();
		List<String> hecceities = Lists.newArrayList();
		t = tokens.getNext();
		if (!t.getType().equals(TokenType.TT_LPAREN)) {
			throw new ParseException(t.getLit(), 0);
		}
		t = tokens.getNext();
		while (!t.getType().equals(TokenType.TT_RPAREN)) {
			hecceities.add(t.getLit());
			t = tokens.getNext();
		}
		cp.addAtomicProp(name, hecceities);
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
					getConstructors(ip);
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
		Map<String, List<Hecceity>> argMap = constructor.getPredicates2Hecceity();
		Map<Hecceity,List<String>>  prefixInfo = invertPredMap(argMap);
		Map<String, List<Hecceity>> prefixMap = p.getPredicates2Hecceity();
		Prop ret = new Prop();
		boolean forallFlag = true;
		for (Quantifier q: constructor.getPrefix()) {
			QuantifierType type;
			if (forallFlag) {
				if (q.getType().equals(QuantifierType.THEREIS)) {
					forallFlag = false;
				}
				type = QuantifierType.THEREIS;
			}
			else {
				type = q.getType();
			}
			Hecceity h = q.getHecceity();
			Hecceity hecceity = null;
			List<String> preds = prefixInfo.get(h);
			for (String predName : preds) {
				List<Hecceity> predInfo = prefixMap.get(predName);
				if (predInfo == null) {
					continue;
				}
				List<Hecceity> consInfo = argMap.get(predName);
				int i = consInfo.indexOf(h);
				hecceity = predInfo.get(i);
				break;
			}
			
			Quantifier nq = new Quantifier(type, hecceity);
			ret.addQuantifierUnique(nq);
		}
		for (CompoundProp cp: constructor.getMatrix()) {
			CompoundProp ncp = ret.makeBlankCompoundProp();
			for (AtomicProp ap: cp.getAtomicProps()) {
				String name = ap.getName();
				List<Hecceity> hecs = ap.getHecceities();
				List<Hecceity> ents = Lists.newArrayList();
				List<he>
				AtomicProp nap = new AtomicProp(name, ents);
			}
		}
		
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
