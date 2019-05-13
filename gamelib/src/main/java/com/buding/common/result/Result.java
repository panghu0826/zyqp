package com.buding.common.result;

import java.io.Serializable;


public class Result implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public static int RESULT_OK = 0;
	public static int RESULT_FAIL = 1;
	public static int RESULT_BACK = 2;

	public int code;
	public String msg;
	
	public Result(int code) {
		this.code = code;
	}
	
	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public Result() {
	
	}
	
	public boolean isFail() {
		return code == RESULT_FAIL;
	}
	
	public boolean isOk() {
		return code == RESULT_OK;
	}
	
	public static Result success() {
		return new Result(RESULT_OK);
	}
	
	public static Result fail() {
		return new Result(RESULT_FAIL);
	}
	
	public static Result fail(String msg) {
		return new Result(RESULT_FAIL, msg);
	}
}
