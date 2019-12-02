package com.lang;

import java.util.concurrent.Callable;

import com.lang.val.AxiomSet;
import com.lang.val.Prop;

public class ProofTask implements Callable<ProofResult> {

	private final AxiomSet axioms;
	private final Prop toBeProven;
	private final boolean isNegated;
	private final int order;

	public ProofTask(AxiomSet as, Prop p, int order) {
		this(as, p, false, order);
	}

	public ProofTask(AxiomSet as, Prop p, boolean b, int order) {
		this.axioms = as;
		this.toBeProven = p;
		this.isNegated = b;
		this.order = order;
	}

	@Override
	public ProofResult call() throws Exception {
		ProofResult pf = new ProofResult();

		pf = axioms.contradicts(toBeProven, order);
		if (isNegated) {
			pf.negateResult();
		}

		return pf;

	}

}
