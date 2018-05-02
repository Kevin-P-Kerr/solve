package com.lang;

import java.util.Scanner;

import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String s = scanner.nextLine();
			TokenStream tokens = Tokenizer.tokenize(s);
			System.out.println(tokens.toString());
			System.out.print(" > ");
		}
		scanner.close();
	}

}
