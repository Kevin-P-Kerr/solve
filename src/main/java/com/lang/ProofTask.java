package com.lang;

import java.util.concurrent.Callable;

import com.lang.val.AxiomSet;
import com.lang.val.Prop;

public class ProofTask implements Callable<ProofResult> {

	private final AxiomSet axioms;
	private final Prop toBeProven;
	private final boolean isNegated;

	public ProofTask(AxiomSet as, Prop p) {
		this(as, p, false);
	}

	public ProofTask(AxiomSet as, Prop p, boolean b) {
		this.axioms = as;
		this.toBeProven = p;
		this.isNegated = b;
	}

	@Override
	public ProofResult call() throws Exception {
		ProofResult pf = new ProofResult();

		pf = axioms.proveByContradiction(toBeProven);
		if (isNegated) {
			pf.negateResult();
		}

		return pf;

	}

}
