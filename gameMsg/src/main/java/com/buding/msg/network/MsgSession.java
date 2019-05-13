package com.buding.msg.network;

import com.buding.common.network.session.BaseSession;
import com.buding.common.network.session.SessionStatus;

public class MsgSession extends BaseSession {

	@Override
	public boolean isCanRemove() {
		//已经计划移除&&等待时间已到
		return sessionStatus == SessionStatus.INVALID && System.currentTimeMillis() - planRemoveTime >= 3*60*1000;
	}
	
}
