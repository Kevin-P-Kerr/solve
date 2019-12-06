package com.lang.val.prop;

import java.util.List;

import com.google.common.collect.Lists;

public class AtomicProp {
	boolean negate;
	private final String name;
	private final List<Heccity> heccesities;

	public AtomicProp(boolean n, String name, List<Heccity> h) {
		this.negate = n;
		this.name = name;
		this.heccesities = h;
	}

	List<Integer> fromIndices = Lists.newArrayList();
	List<Integer> toIndices = Lists.newArrayList();

	public boolean couldContradict(AtomicProp aap, QuantifierPart quantifierPart) {
		if (!(name.equals(aap.name) && negate != aap.negate)) {
			return false;
		}
		List<Integer> toList = Lists.newArrayList();
		List<Integer> fromList = Lists.newArrayList();
		for (int i = 0, ii = heccesities.size(); i < ii; i++) {
			int from;
			int to;
			Heccity a = heccesities.get(i);
			Heccity b = aap.heccesities.get(i);
			if (a.equals(b)) {
				continue;
			}
			int indexA = a.index;
			int indexB = b.index;
			Quantifier qA = quantifierPart.getQuantifier(indexA);
			Quantifier qB = quantifierPart.getQuantifier(indexB);
			if (qA.canTransmitInto(qB)) {
				from = qA.index;
				to = qB.index;
			} else if (qB.canTransmitInto(qA)) {
				from = qB.index;
				to = qA.index;
			} else {
				return false;
			}
			if (fromList.contains(to)) {
				return false;
			}
			if (fromList.contains(from)) {
				continue;
			}
			toList.add(to);
			fromList.add(from);
		}
		fromIndices = fromList;
		toIndices = toList;
		return true;
	}

	public void transmitHecName(int index, String s) {
		for (Heccity h : heccesities) {
			if (h.index == index) {
				h.name = s;
			}
		}
	}

	public void setUpIndex(String n, int i) {
		for (Heccity h : heccesities) {
			if (h.name.equals(n)) {
				h.index = i;
			}
		}
	}

	public boolean contradicts(AtomicProp ap) {
		AtomicProp c = new AtomicProp(!ap.negate, ap.name, ap.heccesities);
		return equals(c);

	}

	public void replaceHecceities(int from, int to, String name) {
		for (Heccity h : heccesities) {
			if (h.index == from) {
				h.index = to;
				h.name = name;
			}
		}
	}

	@Override
	public boolean equals(Object anO) {
		if (anO == this) {
			return true;
		}
		if (!(anO instanceof AtomicProp)) {
			return false;
		}
		AtomicProp ap = (AtomicProp) anO;
		if (ap.negate != negate) {
			return false;
		}
		if (!ap.name.equals(name)) {
			return false;
		}
		if (ap.heccesities.size() != heccesities.size()) {
			return false;
		}
		for (int i = 0, ii = heccesities.size(); i < ii; i++) {
			Heccity h = heccesities.get(i);
			Heccity hh = ap.heccesities.get(i);
			if (!(h.equals(hh))) {
				return false;
			}
		}
		return true;
	}

	public AtomicProp copy() {
		List<Heccity> hh = Lists.newArrayList();
		for (Heccity h : heccesities) {
			hh.add(h.copy());
		}
		return new AtomicProp(negate, name, hh);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (negate) {
			sb.append("~");
		}
		sb.append(name);
		sb.append("(");
		for (int i = 0, ii = heccesities.size(); i < ii; i++) {
			sb.append(heccesities.get(i).name);
			if (ii - i > 1) {
				sb.append(" ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
}