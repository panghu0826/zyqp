package com.buding.battle.logic.module.common;

public interface ParentAware<T> {
	
	public T getParent();

	public void setParent(T parent);
}
