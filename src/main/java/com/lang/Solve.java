package com.lang;

import java.text.ParseException;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.val.Value;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		Environment env = new Environment();
		List<String> lines = Lists.newArrayList();
		lines.add("a = forall a : ~Man(a) + Mortal(a)");
		lines.add("b = forall a thereis b forall c: ~Man(a) + Man(b)*Mother(b a)*~Mother(c a)*~Mother(a a)");
		// lines.add("forall a: ~Man(a) + ~Mother(a a)");
		// lines.add("x = thereis a thereis b forall c :Man(a)*Mortal(a)*Man(b)*Mother(b a)*~Mother(c a)");
		lines.add("$ thereis a : Man(a)");
		// lines.add("* forall a thereis b: A(a) + ~B(a b) . thereis c: E(c)");
		for (String s : lines) {
			// while (scanner.hasNext()) {
			try {
				// String s = scanner.nextLine();
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
