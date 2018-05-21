
package com.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;
import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.val.Prop.LogicException;
import com.lang.val.Value;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		Environment env = new Environment();
		List<String> lines = Lists.newArrayList();
		/*
		 * // lines.add("forall a thereis b :~Man(a) + Man(b)*Mother(b a)"); //
		 * lines.add("forall a forall b forall c : ~Mother(b a) + ~Mother(c a)"); //
		 * lines.add("forall a forall b : ~Man(a) + ~Mother(b a) + Man(b)"); lines.add("forall a: ~Man(a) + Mortal(a)");
		 * lines.add("forall a forall b thereis c: ~Man(a) + ~Man(b) + ~Friends(a b) + Likes(a c)*Likes(b c)");
		 *
		 * lines.add(" $ thereis a: Man(a)");
		 * lines.add("$ thereis a thereis b: Man(a)*Man(b)*Friends(a b)*Friends(b a)");
		 */
		// lines.add("forall a in Man: Mortal(a)");
		// lines.add("$ thereis a : ~Mortal(a)");
		// for (String s : lines) {
		while (scanner.hasNext()) {
			try {
				String s = scanner.nextLine();
				if (s.indexOf("exit") == 0) {
					break;
				}
				if (s.indexOf("write") == 0) {
					String[] l = s.split(" ");
					BufferedWriter writer = new BufferedWriter(new FileWriter(l[1]));
					StringBuilder program = new StringBuilder();
					for (String str : lines) {
						program.append(str).append("\n");
					}
					writer.write(program.toString());
					writer.close();
					System.out.print(" > ");
					continue;
				}
				if (s.indexOf("read") == 0) {
					String[] l = s.split(" ");
					BufferedReader br = new BufferedReader(new FileReader(l[1]));
					String st;
					while ((st = br.readLine()) != null) {
						System.out.println(st);
						lines.add(st);
						TokenStream tokens = Tokenizer.tokenize(st);
						Interpreter i = new Interpreter(tokens);
						Value p = i.eval(env);
						System.out.println(p.toString());
					}
					br.close();
					System.out.print(" > ");
					continue;
				}
				lines.add(s);
				TokenStream tokens = Tokenizer.tokenize(s);
				Interpreter i = new Interpreter(tokens);
				Value p = i.eval(env);
				System.out.println(p.toString());
				System.out.print(" > ");
			} catch (ParseException e) {
				System.out.println(e);
				System.out.println("continue...");
				System.out.print(" > ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				System.out.println(e);
			} catch (LogicException e) {
				System.out.println(e);
			}
		}
		scanner.close();
	}

}
