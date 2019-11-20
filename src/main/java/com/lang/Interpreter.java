package com.lang;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;
import com.lang.val.Prop;

public class Interpreter {

	private final TokenStream tokens;

	public Interpreter(TokenStream s) {
		this.tokens = s;
	}

	public void eval() throws Exception {
		List<Prop> axioms = Lists.newArrayList();
		while (tokens.hasToken()) {
			Token t = tokens.peek();
			switch (t.getType()) {
			case TT_FORALL:
			case TT_THEREIS:
				Prop ax = ParseProp(tokens);
				axioms.add(ax);
				break;
			case TT_VAR:
				if (t.getLit() == "get") {
					break;
				}
			default:
				System.err.println("parse error");
				System.exit(1);
			}
		}
	}

	private Prop ParseProp(TokenStream tokens) throws Exception {
		Prop.Quantifier q = parseQuantifier(tokens);
		Prop.BooleanPart b = parseBoolean(tokens);
	}

	private Prop.BooleanPart parseBoolean(TokenStream tokens) throws Exception {
		Token t = tokens.getNext();
		if (t.getType() != TokenType.TT_COLON) {
			throw new Exception();
		}
		t = tokens.getNext();
		while (t.getType() != TokenType.TT_PERIOD) {
			List<Token> ts = Lists.newArrayList();
			while (t.getType() != TokenType.TT_PLUS) {

			}
		}
	}

	private Prop.Quantifier parseQuantifier(TokenStream tokens) throws Exception {
		Token t = tokens.getNext();
		switch (t.getType()) {
		case TT_FORALL:
			return Prop.Quantifier.newUniversal(t.getLit());
		case TT_THEREIS:
			return Prop.Quantifier.newExistential(t.getLit());
		}
		throw new Exception("bad quantifier");
	}

}
