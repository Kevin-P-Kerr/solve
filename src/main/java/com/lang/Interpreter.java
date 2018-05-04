package com.lang;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lang.Prop.CompoundProp;
import com.lang.Prop.QuantifierType;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;

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
			QuantifierType qt;
			String lit = t.getLit();
			if (lit.equals("forall")) {
				qt = QuantifierType.FORALL;
			} else if (lit.equals("thereis")) {
				qt = QuantifierType.THEREIS;
			} else {
				throw new ParseException(lit, 0);
			}
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

	public Prop eval(Map<String, Prop> env) throws ParseException {
		Token t = tokens.peek();
		if (t.getType().equals(TokenType.TT_VAR)) {
			tokens.getNext();
			String varName = t.getLit();
			t = tokens.peek();
			if (t.getType().equals(TokenType.TT_EQUALS)) {
				tokens.getNext();
				Prop p = eval(env);
				env.put(varName, p);
				return p;
			} else {
				return env.get(varName);
			}
		} else {
			return evalProp();
		}

	}

}
