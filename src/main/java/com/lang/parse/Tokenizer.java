package com.lang.parse;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer.Token.TokenType;

public class Tokenizer {
	public static class Token {
		public enum TokenType {
			TT_PLUS, TT_LPAREN, TT_RPAREN, TT_COLON, TT_ASTER, TT_VAR, TT_PERIOD, TT_EQUALS;
		}

		private final TokenType type;
		private final String literal;

		public Token(TokenType type, String lit) {
			this.type = type;
			this.literal = lit;
		}

		public TokenType getType() {
			return type;
		}

		public String getLit() {
			return literal;
		}

		@Override
		public String toString() {
			return type + " : " + literal;
		}
	}

	private static boolean isWhite(char c) {
		return c == ' ';
	}

	public static TokenStream tokenize(String ln) {

		List<Token> tokens = Lists.newArrayList();
		for (int i = 0, ii = ln.length(); i < ii; i++) {
			char c = ln.charAt(i);
			if (isWhite(c)) {
				continue;
			}
			String lit = Character.toString(c);
			TokenType type;
			if (c == '+') {
				type = TokenType.TT_PLUS;
			} else if (c == '(') {
				type = TokenType.TT_LPAREN;
			} else if (c == ')') {
				type = TokenType.TT_RPAREN;
			} else if (c == '*') {
				type = TokenType.TT_ASTER;
			} else if (c == ':') {
				type = TokenType.TT_COLON;
			} else if (c == '.') {
				type = TokenType.TT_PERIOD;
			} else if (c == '=') {
				type = TokenType.TT_EQUALS;
			} else {
				type = TokenType.TT_VAR;
				i++;
				while (i < ii && (c = ln.charAt(i)) != ' ') {
					if (isReserved(c)) {
						i--;
						break;
					}
					lit += Character.toString(c);
					i++;
				}
			}
			Token t = new Token(type, lit);
			tokens.add(t);

		}
		return new TokenStream(tokens);
	}

	private static boolean isReserved(char c) {
		return c == ':' || c == '+' || c == '*' || c == '(' || c == ')';
	}

	public static class TokenStream {
		private final List<Token> tokens;
		private int ptr = 0;

		public TokenStream(List<Token> tokens) {
			this.tokens = tokens;
		}

		public boolean hasToken() {
			return tokens.size() > ptr;
		}

		public Token getNext() {
			Token t = tokens.get(ptr);
			ptr++;
			return t;
		}

		/**
		 * get the next token without incrementing the token counter
		 */
		public Token peek() {
			return tokens.get(ptr);
		}

		@Override
		public String toString() {
			String ret = "";
			for (Token t : tokens) {
				ret += t.toString();
				ret += ", ";
			}
			return ret;
		}
	}

}
