package com.lang;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.lang.val.Undefined;
import com.lang.val.Value;
import com.lang.val.prop.Prop;

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

	public Map<String, Value> getAllValues() {
		Map<String, Value> ret = Maps.newHashMap();
		for (Entry<String, Value> entry : env.entrySet()) {
			ret.put(entry.getKey(), entry.getValue());
		}
		if (parent != null) {
			ret.putAll(parent.getAllValues());
		}
		return ret;
	}

	public Map<String, Prop> getAllProps() {
		Map<String, Value> values = getAllValues();
		Map<String, Prop> ret = Maps.newHashMap();
		for (Entry<String, Value> entry : values.entrySet()) {
			Value v = entry.getValue();
			if (v instanceof Prop) {
				ret.put(entry.getKey(), (Prop) entry.getValue());
			}
		}
		return ret;
	}
}
