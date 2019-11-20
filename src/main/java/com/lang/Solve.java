
package com.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;

public class Solve {

	public static void main(String args[]) {
		String fn = args[0];
		try {
			File file = new File(fn);
			FileReader r = new FileReader(file);
			BufferedReader bf = new BufferedReader(r);
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = bf.readLine()) != null) {
				sb.append(s);
			}
			String input = sb.toString();
			Tokenizer t = new Tokenizer();
			TokenStream tokens = t.tokenize(input);
			Interpreter i = new Interpreter(tokens);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
