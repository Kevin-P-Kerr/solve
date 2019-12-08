package com.lang.val;

import java.util.List;

import com.google.common.collect.Lists;
import com.lang.ProofResult;
import com.lang.val.prop.Prop;

public class LambdaSet {
	private final List<Lambda> lambdas;

	public LambdaSet(List<Prop> axioms) {
		lambdas = Lists.newArrayList();
		for (Prop p : axioms) {
			lambdas.add(new Lambda(p));
		}
	}

	public ProofResult infer(Prop p) {
		LambdaHeccitySet hecs = p.inferConcreteLambdaHeccities();
		LambdaHeccitySet toBeProven = p.inferAbstractLambdaHeccities();
		List<LambdaHeccity> args = hecs.getHecs();

	}

}
