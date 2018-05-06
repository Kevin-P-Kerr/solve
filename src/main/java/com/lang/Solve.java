package com.lang;

import java.text.ParseException;
import java.util.Scanner;

import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.val.Value;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		Environment env = new Environment();
		while (scanner.hasNext()) {
			try {
				String s = scanner.nextLine();
				TokenStream tokens = Tokenizer.tokenize(s);
				Interpreter i = new Interpreter(tokens);
				Value p = i.eval(env);
				System.out.println(p.toString());
				System.out.print(" > ");
			} catch (ParseException e) {
				System.out.println(e);
				System.out.println("continue...");
				System.out.print(" > ");
			}
		}
		scanner.close();
	}

}
