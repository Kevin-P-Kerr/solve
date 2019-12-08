package com.lang;

import java.util.List;

import com.google.common.collect.Lists;

public class Permutator {
	public static List<List<Integer>> getAllPermutations(int n) {
		if (n == 0) {
			return Lists.newArrayList();
		}
		List<Integer> seed = Lists.newArrayList();
		for (int i = 0; i < n; i++) {
			seed.add(i);
		}
		return getAllPermutations(seed);
	}

	private static List<List<Integer>> getAllPermutations(List<Integer> seed) {
		List<List<Integer>> ret = Lists.newArrayList();
		if (seed.size() == 1) {
			return ret;
		}
		if (seed.size() == 2) {
			List<Integer> x = Lists.newArrayList();
			x.add(seed.get(1));
			x.add(seed.get(0));
			ret.add(x);
			ret.add(seed);
			return ret;
		}
		for (int i = 0, ii = seed.size(); i < ii; i++) {
			Integer head = seed.get(i);
			List<Integer> nextSeed = Lists.newArrayList();
			for (int n = 0, nn = ii; n < nn; n++) {
				if (n == i) {
					continue;
				}
				nextSeed.add(seed.get(n));
			}
			List<List<Integer>> intermediate = getAllPermutations(nextSeed);
			for (List<Integer> l : intermediate) {
				List<Integer> rx = Lists.newArrayList();
				rx.add(head);
				for (Integer ig : l) {
					rx.add(ig);
				}
				ret.add(rx);
			}
		}
		return ret;
	}
}
