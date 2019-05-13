package com.buding.common.db.model;

import java.io.Serializable;

public abstract class BaseModel<PK extends Comparable<PK>> implements IEntity<PK>, Serializable {
	private static final long serialVersionUID = -8011061374263995942L;

	public abstract PK getId();

	public abstract void setId(PK paramPK);

	public String toString() {
		return getClass().getName() + "[" + getId() + "]";
	}
	
	public PK getIdentity() {
		return getId();
	}
}