package com.lang;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.prop.Prop;

public class ProofTrace {
	public static interface Instruction {
		public void doInstruction(Prop p);

		public String getNote();
	}

	public static class ReplaceHeccityInstruction implements Instruction {
		private final int from;
		private final int to;
		private final String name;
		private final String oldName;

		public ReplaceHeccityInstruction(int from, int to, String name, String oldName) {
			this.to = to;
			this.from = from;
			this.name = name;
			this.oldName = oldName;
		}

		@Override
		public void doInstruction(Prop p) {
			p.removeQuantifier(from);
			p.replaceHeccity(to, from, name);
		}

		@Override
		public String getNote() {
			return "rh + : " + oldName + " " + name;
		}
	}

	public static class MultiplyInstruction implements Instruction {
		private final Prop m;

		private final ProofTrace pt;

		public MultiplyInstruction(Prop m, ProofTrace pt) {
			this.m = m;
			this.pt = pt;
		}

		@Override
		public void doInstruction(Prop p) {
			pt.base = p.multiply(m);
		}

		@Override
		public String getNote() {
			return "m : " + m.toString();
		}
	}

	public static class RemoveContradictionsInstruction implements Instruction {

		@Override
		public void doInstruction(Prop p) {
			p.simplify();
			p.removeContradictions();
		}

		@Override
		public String getNote() {
			return "rc";
		}
	}

	private final List<Instruction> instructions = Lists.newArrayList();
	private Prop base;

	public ProofTrace(Prop p) {
		this.base = p;
	}

	public void replaceHeccity(int t, int f, String name, String oldName) {
		instructions.add(new ReplaceHeccityInstruction(f, t, name, oldName));
	}

	public void multiply(Prop copy) {
		instructions.add(new MultiplyInstruction(copy, this));
	}

	public void doTrace() {
		System.out.println("starting with: " + base.toString());
		int i = 1;
		for (Instruction instruction : instructions) {
			instruction.doInstruction(base);
			System.out.println("(" + i++ + ")\t" + base.toString() + "," + instruction.getNote());
		}
		System.out.println("fin");
	}

	public void removeContradictions() {
		instructions.add(new RemoveContradictionsInstruction());
	}

}
