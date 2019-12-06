package com.lang.val.prop;

import java.util.List;

import com.google.common.collect.Lists;

public class QuantifierPart {
	List<Quantifier> quantifiers;

	public QuantifierPart(List<Quantifier> q) {
		this.quantifiers = q;

	}

	@Override
	public boolean equals(Object a) {
		if (super.equals(a)) {
			return true;
		}
		if (!(a instanceof QuantifierPart)) {
			return false;
		}
		QuantifierPart qp = (QuantifierPart) a;
		if (qp.quantifiers.size() != quantifiers.size()) {
			return false;
		}
		for (int i = 0, ii = quantifiers.size(); i < ii; i++) {
			if (!(qp.quantifiers.get(i).equals(quantifiers.get(i)))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Quantifier q : quantifiers) {
			b.append(q.toString());
			b.append(" ");
		}
		return b.toString();
	}

	public QuantifierPart copy() {
		List<Quantifier> qs = Lists.newArrayList();
		for (Quantifier q : quantifiers) {
			qs.add(q.copy());
		}
		return new QuantifierPart(qs);
	}

	// mutates the object
	// thereis a forall b forall c thereis d :Man(a)*~Mother(b a)*~Man(c) + Man(a)*~Mother(b a)*Man(d)*Mother(d c).
	public void add(QuantifierPart quantifierPart) {
		for (Quantifier q : quantifierPart.quantifiers) {
			boolean isForall = q.type == QuantifierType.FORALL;
			for (Quantifier qq : quantifiers) {
				boolean localForAll = qq.type == QuantifierType.FORALL;
				if (isForall) {
					q.indicesForTransmission.add(qq.index);
				}
				if (localForAll) {
					qq.indicesForTransmission.add(q.index);
				}
			}
		}
		quantifiers.addAll(quantifierPart.quantifiers);
	}

	public void removeQuantifier(Quantifier r) {

		quantifiers.remove(r);

	}

	public void negate() {
		// forall a thereis b
		// ~ exists a forall b
		for (Quantifier q : quantifiers) {
			if (q.type == QuantifierType.FORALL) {
				q.type = QuantifierType.THEREIS;
			} else {
				q.type = QuantifierType.FORALL;
			}
		}
	}

	public int getLargestOffset() {
		return quantifiers.get(quantifiers.size() - 1).index;
	}

	public Quantifier getQuantifier(int t) {
		for (Quantifier q : quantifiers) {
			if (q.index == t) {
				return q;
			}
		}
		return null;
	}

	public void removeQuantifier(int f) {
		Quantifier toRemove = null;
		for (Quantifier q : quantifiers) {
			if (q.index == f) {
				toRemove = q;
				break;
			}
		}
		if (toRemove != null) {
			quantifiers.remove(toRemove);
		}
	}
}