package com.buding.hall.network;

import com.buding.common.network.session.BaseSession;
import com.buding.common.network.session.SessionStatus;

public class HallSession extends BaseSession {

	@Override
	public boolean isCanRemove() {
		//已经计划移除&&等待时间已到
		return sessionStatus == SessionStatus.INVALID && System.currentTimeMillis() - planRemoveTime >= 5*1000;
	}
	
}
