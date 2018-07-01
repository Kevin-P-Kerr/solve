package com.lang;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lang.val.Prop;

public class Extractor {
	/*
	 * we consider relations to be data types
	 * and propositions to be constructors
	 */
	public static enum RESERVED_RELATIONS {
		BOOL,T,F,STR, INT, ADD, SUB, MULT, DIV, LAMBDA, APPLY
	}
	private static class GraphNode {
		private final String name;
		private final boolean isRoot;
		private final List<GraphNode> children = Lists.newArrayList();
		private final List<GraphNode> parents = Lists.newArrayList();
		
		public GraphNode(String n) {
			if (RESERVED_RELATIONS.valueOf(n) != null) {
				isRoot = true;
			}
			else {
				isRoot = false;
			}
			this.name = n;
		}
		
	}
	public static String extractProgram(Map<String,Prop> namedProps) {
		return null;
	}
}
