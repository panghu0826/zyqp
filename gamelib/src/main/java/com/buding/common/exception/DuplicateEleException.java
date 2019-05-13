package com.buding.common.exception;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class DuplicateEleException extends RuntimeException {	
	static final long serialVersionUID = -7034897190745766949L;
	
	public DuplicateEleException(String msg) {
		super(msg);
	}
}
