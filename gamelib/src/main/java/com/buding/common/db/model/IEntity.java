package com.buding.common.db.model;

public abstract interface IEntity<T extends Comparable<T>> {
	public abstract T getIdentity();
}