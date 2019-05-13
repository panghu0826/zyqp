package com.buding.msg.service;

import com.buding.hall.module.msg.vo.BaseMsg;

public interface MsgPushService extends MsgContainerFacade {
	public void push2SendQueue(BaseMsg msg) throws Exception;
	public boolean isMsgSended2User(int userId, String msgType, long msgId);
}
