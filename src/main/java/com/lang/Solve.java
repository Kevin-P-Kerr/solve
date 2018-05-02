package com.lang;

import java.util.Scanner;

public class Solve {

	public static void main(String args[]) {
		System.out.print(" > ");
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String s = scanner.nextLine();
			System.out.println(s);
			System.out.print(" > ");
		}
		scanner.close();
	}

}
