package com.lang;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.val.Prop;

public class ProofTrace {
	public static interface Instruction {
		public void doInstruction(Prop p);
	}

	public static class RemoveQuantifierInstruction implements Instruction {

		private final int index;

		public RemoveQuantifierInstruction(int i) {
			this.index = i;
		}

		@Override
		public void doInstruction(Prop p) {
			p.removeQuantifier(index);
		}
	}

	public static class ReplaceHeccityInstruction implements Instruction {
		private final int from;
		private final int to;
		private final String name;

		public ReplaceHeccityInstruction(int from, int to, String name) {
			this.to = to;
			this.from = from;
			this.name = name;
		}

		@Override
		public void doInstruction(Prop p) {
			p.replaceHeccity(to, from, name);
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
	}

	private final List<Instruction> instructions = Lists.newArrayList();
	private Prop base;

	public ProofTrace(Prop p) {
		this.base = p;
	}

	public void removeQuantifier(int f) {
		instructions.add(new RemoveQuantifierInstruction(f));

	}

	public void replaceHeccity(int t, int f, String name) {
		instructions.add(new ReplaceHeccityInstruction(f, t, name));
	}

	public void multiply(Prop copy) {
		instructions.add(new MultiplyInstruction(copy, this));
	}

	public void doTrace() {
		System.out.println("starting with: " + base.toString());
		int i = 1;
		for (Instruction instruction : instructions) {
			instruction.doInstruction(base);
			System.out.println("(" + i + ")\t" + base.toString());
		}
		System.out.println("fin");
	}

}
