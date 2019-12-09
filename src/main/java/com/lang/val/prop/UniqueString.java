package com.lang.val.prop;

class UniqueString {
	private int c = 0;
	private static String[] alpha = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
			"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

	public String getString() {
		int mod = alpha.length;
		int i = c;
		c++;
		int digits = 1;
		int m = mod;
		while (m <= i) {
			m = m * mod;
			if (m > i) {
				m = m / mod;
				digits++;
				break;
			}
			digits++;
		}
		String ret = "";

		while (digits > 0) {
			if (i > m) {
				int index = i / m;
				i = i % m;
				ret += alpha[index - 1];
			} else {
				int index = i % m;
				ret += alpha[index];
			}
			m = m / mod;
			digits--;
		}
		return ret;
	}
}