package com.lang.parse;

public class Tokenizer {
	public static class Token {
		public enum TokenType {
			TT_PLUS, TT_LPAREN, TT_RPAREN, TT_COLON, TT_ASTER
		}

		private final TokenType type;
		private final String literal;

		public Token(TokenType type, String lit) {
			this.type = type;
			this.literal = lit;
		}
	}

	private final String str;

	public Tokenizer(String str) {
		this.str = str;
	}

}
