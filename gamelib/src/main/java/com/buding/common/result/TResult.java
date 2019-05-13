package com.buding.common.result;

public class TResult<T> extends Result {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public T t;
	public boolean isCreateRoom = false;
	
//	public TResult<T> t(T model) {
//		this.t = model;
//		return this;
//	}
	
//	public TResult<T> failWithMsg(String msg) {
//		this.code = RESULT_FAIL;
//		this.msg = msg;
//		return this;
//	}
//	
	public static <T> TResult<T> sucess1(T t) {
		TResult<T> ret = new TResult<T>();
		ret.t = t;
		return ret;
	}
	public static <T> TResult<T> sucess2(T t,boolean isCreateRoom) {
		TResult<T> ret = new TResult<T>();
		ret.t = t;
		ret.isCreateRoom = isCreateRoom;
		return ret;
	}
	
	public static <T> TResult<T> fail1(String msg) {
		TResult<T> ret = new TResult<T>();
		ret.code = -1;
		ret.msg = msg;
		return ret;
	}
}
