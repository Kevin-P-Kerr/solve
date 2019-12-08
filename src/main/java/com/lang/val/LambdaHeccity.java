package com.lang.val;

import com.lang.val.prop.Quantifier;
import com.lang.val.prop.Quantifier.QuantifierType;

public class LambdaHeccity {

	private final QuantifierType type;
	private final int id;

	public LambdaHeccity(Quantifier q) {
		this.type = q.getType();
		this.id = q.getIndex();
	}

	public int getId() {
		return id;
	}

}
