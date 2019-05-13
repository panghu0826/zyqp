package com.buding.common.network.model;


public class BaseRsp {
	public int result;
	public String msg;

	public static int RESULT_OK = 0;
	public static int RESULT_FAIL = 1;

	
	public BaseRsp(int code) {
		this.result = code;
	}
	
	public BaseRsp(int code, String msg) {
		this.result = code;
		this.msg = msg;
	}
	
	public BaseRsp() {
	
	}
	
	public boolean isFail() {
		return result == RESULT_FAIL;
	}
	
	public boolean isOk() {
		return result == RESULT_OK;
	}
	
	public static BaseRsp success() {
		return new BaseRsp(RESULT_OK);
	}
	
	public static BaseRsp fail() {
		return new BaseRsp(RESULT_FAIL);
	}
	
	public static BaseRsp fail(String msg) {
		return new BaseRsp(RESULT_FAIL, msg);
	}
	
	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
