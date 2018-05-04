package com.lang;

import java.text.ParseException;
import java.util.Scanner;

import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		
		while (scanner.hasNext()) {
			try {
				String s = scanner.nextLine();
				TokenStream tokens = Tokenizer.tokenize(s);
				Interpreter i = new Interpreter(tokens);
				Prop p = i.eval();
				System.out.println(p.toString());
				System.out.print(" > ");
			}catch (ParseException | NullPointerException e) {
				System.out.println(e);
			}
		}
		scanner.close();
	}

}