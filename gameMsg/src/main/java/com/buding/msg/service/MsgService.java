package com.buding.msg.service;


import com.buding.common.result.Result;
import com.buding.hall.module.msg.vo.BaseMsg;
import com.buding.hall.module.msg.vo.GameChatMsg;

public interface MsgService extends MsgContainerFacade {
	//移除跑马灯消息
	public void removeMarquee(long id) throws Exception;
	//发送跑马灯消息
	public void sendMarquee(long id, boolean check) throws Exception;
	//发送邮件
	public void sendMail(long id) throws Exception;
	//删除邮件
	public void removeMail(long id) throws Exception;
	//推送公告与活动
	public Result repushActNotice() throws Exception;
	//保存并且发送消息
	public void saveAndSend(BaseMsg msg) throws Exception;
	//发送聊天消息
	public void sendChatMsg(GameChatMsg msg) throws Exception;
	//标记消息为已读
	public void markMsgRead(int userId, long msgId) throws Exception;

	void closeSocket(int playerId);

	void removeAllMarquee();
}
