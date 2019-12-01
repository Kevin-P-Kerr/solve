package com.lang;

public class ProofResult {
	public static enum PROOF_VALUE {
		PF_PROVED_TRUE, PF_PROVED_FALSE, PF_UNPROVED;
	}

	private PROOF_VALUE pf = PROOF_VALUE.PF_UNPROVED;

	public ProofResult() {

	}

	public void setProofValue(PROOF_VALUE pf) {
		this.pf = pf;
	}

	public PROOF_VALUE getProofValue() {
		return pf;
	}

	public void negateResult() {
		if (pf == PROOF_VALUE.PF_PROVED_FALSE) {
			pf = PROOF_VALUE.PF_PROVED_TRUE;
		} else if (pf == PROOF_VALUE.PF_PROVED_TRUE) {
			pf = PROOF_VALUE.PF_PROVED_FALSE;
		}
	}
}
