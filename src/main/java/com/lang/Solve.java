
package com.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lang.parse.Tokenizer;
import com.lang.parse.Tokenizer.TokenStream;

public class Solve {

	private static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService exec = Executors.newFixedThreadPool(NUM_CPUS - 2);

	public static void main(String args[]) {
		// String fn = args[0];

		String fn = null;
		if (fn == null) {
			fn = "gm.lg";
		}

		try {
			File file = new File(fn);
			FileReader r = new FileReader(file);
			BufferedReader bf = new BufferedReader(r);
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = bf.readLine()) != null) {
				sb.append(s + " \n");
			}
			String input = sb.toString();
			TokenStream tokens = Tokenizer.tokenize(input);
			Interpreter i = new Interpreter(tokens, exec);
			i.eval();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			exec.shutdownNow();
			System.exit(0);
		}
	}

}
