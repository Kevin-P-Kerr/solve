package com.lang;

import java.util.concurrent.Callable;

import com.lang.val.AxiomSet;
import com.lang.val.Prop;

public class ProofTask implements Callable<ProofResult> {

	private final AxiomSet axioms;
	private final Prop toBeProven;

	public ProofTask(AxiomSet as, Prop p) {
		this.axioms = as;
		this.toBeProven = p;
	}

	@Override
	public ProofResult call() throws Exception {
		ProofResult pf = new ProofResult();
		try {
			pf = axioms.proveByContradiction(toBeProven);
		} catch (InterruptedException e) {

		} finally {
			return pf;
		}
	}

}
