package com.buding.msg.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.result.Result;
import com.buding.common.util.VelocityUtil;
import com.buding.hall.config.BoxMsgConfig;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.MarqueeMsgConfig;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgReq;
import com.buding.hall.module.msg.vo.GameChatMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.msg.vo.MarqueeMsgReq;
import com.buding.hall.module.msg.vo.TextMsg;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.msg.service.MsgService;

public class MsgPortalServiceImpl implements MsgPortalService {
	@Autowired
	private MsgService msgService;

	@Autowired
	private ConfigManager configManager;

	@Override
	public Result sendGameChatMsg(GameChatMsg req) throws Exception  {
		msgService.sendChatMsg(req);
		return Result.success();
	}

	public Result sendTplMarqueeMsg(MarqueeMsgReq req) throws Exception {
		MarqueeMsgConfig conf = configManager.marqueeMsgConfMap.get(req.msgType);
		MarqueeMsg msg = new MarqueeMsg();
		msg.clientType = conf.clientType;
		msg.msg = VelocityUtil.merge(conf.contentTpl, req.context);
		msg.msgType = req.msgType;
		msg.playSetting = conf.playSetting;
		msg.pos = conf.pos;
		msg.priority = conf.priority;
		msg.startTime = System.currentTimeMillis();
		msg.stopTime = msg.startTime + conf.lifeTime*1000;
		msg.senderId = req.senderId;
		msg.senderName = req.senderName;
		msg.receiver = req.receiver;
		sendMarqueeMsg(msg);
		return Result.success();
	}

	public Result sendTplBoxMsg(BoxMsgReq req) throws Exception {
		BoxMsgConfig conf = configManager.boxMsgConfMap.get(req.msgType);
		BoxMsg msg = new BoxMsg();
		msg.clientType = conf.clientType;
		msg.img = VelocityUtil.merge(conf.img, req.context);
		msg.msg = VelocityUtil.merge(conf.contentTpl, req.context);
		msg.msgType = req.msgType;
		msg.params = req.params;
		msg.popup = conf.popup;
		msg.priority = conf.priority;
		msg.read = false;
		msg.awardId = req.awardId;
		msg.startTime = System.currentTimeMillis();
		msg.stopTime = msg.startTime + conf.lifeTime*1000;
		msg.title = VelocityUtil.merge(conf.title, req.context);
		msg.senderId = req.senderId;
		msg.senderName = req.senderName;
		msg.receiver = req.received;
		sendBoxMsg(msg);
		return Result.success();
	}

	public Result sendMarqueeMsg(MarqueeMsg req) throws Exception {
		msgService.saveAndSend(req);
		return Result.success();
	}

	public Result sendBoxMsg(BoxMsg req) throws Exception {
		msgService.saveAndSend(req);
		return Result.success();
	}

	@Override
	public Result sendTextMsg(TextMsg req) throws Exception {
		msgService.saveAndSend(req);
		return Result.success();
	}

	@Override
	public Result sendMarquee(long msgId, boolean check) throws Exception {
		msgService.sendMarquee(msgId, check);
		return Result.success();
	}

	@Override
	public Result sendMail(long msgId) throws Exception {
		msgService.sendMail(msgId);
		return Result.success();
	}

	@Override
	public Result repushActNotice() throws Exception {
		msgService.repushActNotice();
		return Result.success();
	}

	@Override
	public void closeSocket(int playerId) {
		msgService.closeSocket(playerId);
	}

	@Override
	public void removeAllMarquee() {
		msgService.removeAllMarquee();
	}

	@Override
	public Result removeMarquee(long msgId) throws Exception {
		msgService.removeMarquee(msgId);
		return Result.success();
	}

	@Override
	public Result removeMail(long mailId) throws Exception {
		msgService.removeMail(mailId);
		return Result.success();
	}	
	
}
