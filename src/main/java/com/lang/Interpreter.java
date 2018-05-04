package com.lang;

import java.text.ParseException;
import java.util.List;

import com.google.common.collect.Lists;
import com.lang.Prop.Quantifier;
import com.lang.Prop.QuantifierType;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;

public class Interpreter {
	
	private final TokenStream tokens;
	
	public Interpreter (TokenStream s) {
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
			}
			else if (lit.equals("thereis"))  {
				qt = QuantifierType.THEREIS;
			}
			else {
				throw new ParseException(lit, 0);
			}
			t = tokens.getNext();
			p.addQuantifier(qt,t.getLit());
		}
	}
	
	private void evalMatrix(Prop p) throws ParseException {
		while (tokens.hasToken()) {
			Token t = tokens.getNext();
			String name = t.getLit();
			List<String> hecceities = Lists.newArrayList();
			t = tokens.getNext();
			if (!t.getType().equals(TokenType.TT_LPAREN)) {
				throw new ParseException(t.getLit(),0);
			}
			t = tokens.getNext();
			while (!t.getType().equals(TokenType.TT_RPAREN)) {
				hecceities.add(t.getLit());
				t = tokens.getNext();
			}
			p.addAtomicProp(name,hecceities);
		}
	}
	
	public Prop eval () throws ParseException {
		Prop p = new Prop();
		evalPrefix(p);
		evalMatrix(p);
		return p;
	}

}
