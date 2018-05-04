package com.lang.val;

public class Undefined extends Value {
	public static Undefined undefined = new Undefined();

	@Override
	public String toString() {
		return "undefined";
	}
}
