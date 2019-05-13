package com.buding.battle.logic.module.common;

import com.buding.common.result.Result;

public class EnrollResult extends Result {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public String roomId;
	public String matchId;

	public static EnrollResult success(String matchId,String roomId) {
		EnrollResult ret = new EnrollResult();
		ret.code = RESULT_OK;
		ret.matchId = matchId;
		ret.roomId = roomId;
		return ret;
	}
	
	public static EnrollResult fail(String msg) {
		EnrollResult ret = new EnrollResult();
		ret.code = RESULT_FAIL;
		ret.msg = msg;
		return ret;
	}
}
