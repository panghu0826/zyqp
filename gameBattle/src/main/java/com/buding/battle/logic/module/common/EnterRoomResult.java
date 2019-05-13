package com.buding.battle.logic.module.common;

import com.buding.common.result.Result;

public class EnterRoomResult extends Result {
	private static final long serialVersionUID = -8011061375263995942L;
	
	//桌子id，如果房间未满，则分配桌子和座位号
	public String deskId;
	
	public int seatIndex;
	
	public static EnterRoomResult fail(String msg) {
		EnterRoomResult enterRoomResult = new EnterRoomResult();
		enterRoomResult.code = RESULT_FAIL;
		enterRoomResult.msg = msg;
		return enterRoomResult;
	}
	
	public static EnterRoomResult ok(String deskId, int seatIndex) {
		EnterRoomResult ret = new EnterRoomResult();
		ret.deskId = deskId;
		ret.seatIndex = seatIndex;
		return ret;
	}
}
