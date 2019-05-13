package com.buding.battle.logic.module.common;


public class BaseParent<T> {
	private transient T parent;
	public BaseParent(T t) {
		this.parent = t;
	}

	public T getParent() {
		return parent;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}
}

