package com.lang;

import java.util.Map;

import com.google.common.collect.Maps;
import com.lang.val.Undefined;
import com.lang.val.Value;

public class Environment {

	private Map<String, Value> env = Maps.newHashMap();

	private final Environment parent;

	public Environment(Environment parent) {
		this.parent = parent;
	}

	public Environment() {
		this.parent = null;
	}

	public void put(String name, Value v) {
		env.put(name, v);
	}

	public Value lookUp(String name) {
		Value v = env.get(name);
		if (v == null) {
			if (parent == null) {
				return Undefined.undefined;
			}
			return parent.lookUp(name);
		}
		return v;
	}
}
