package com.buding.msg.service.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.buding.db.model.*;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.msg.network.MsgSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.result.Result;
import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.msg.vo.ActNoticeMsg;
import com.buding.hall.module.msg.vo.BaseMsg;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgList;
import com.buding.hall.module.msg.vo.GameChatMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.msg.network.MsgSessionManager;
import com.buding.msg.service.MsgPushService;
import com.buding.msg.service.MsgService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class MsgServiceImpl implements MsgService, InitializingBean,Looper {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	MsgDao msgDao;
	
	@Autowired
	MsgPushService msgPushService;
	
	ConcurrentMap<Long, MarqueeMsg> marqueeMsgMap = new ConcurrentHashMap<Long, MarqueeMsg>();
	
	public List<ActNotice> actNoticeList = new ArrayList<ActNotice>();
		
	@Autowired
	MsgSessionManager sessionManager;
	
	@Autowired
	AwardDao awardDao;
	
	@Autowired
	PortalTest portalTest;	

	@Autowired
	@Qualifier("sendMsgServerLoop")
	ServerLoop serverLoop;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		actNoticeList = msgDao.getActAndNoticeList();
		
		reloadMarqueeMsg();
		
		serverLoop.register(this);
	}

	private void reloadMarqueeMsg() {
		List<Marquee> marqueeList = msgDao.getMarqueeList();
		for(Marquee model : marqueeList) {
			MarqueeMsg msg = model2MarqueeMsg(model);
			marqueeMsgMap.put(msg.msgId, msg);
		}
	}

	private MarqueeMsg model2MarqueeMsg(Marquee model) {
		MarqueeMsg msg = new MarqueeMsg();
		msg.loopPushInterval = model.getLoopPushInterval();
		msg.loopPushCount = model.getLoopPushCount();
		msg.marqueeType = model.getMarqueeType();
		msg.msg = model.getMsgContent();
		msg.msgId = model.getId();
		msg.playSetting = model.getLoopPlayCount() + "x10";
		msg.receiver = -1;
		msg.senderId = -1;
		msg.senderName = "系统管理员";
		msg.startTime = model.getStartTime().getTime();
		msg.stopTime = model.getEndTime().getTime();
		msg.userGroup = model.getUserGroup();
		msg.pushOnLogin = model.getPushOnLogin();
		return msg;
	}

	@Override
	public void loop() throws Exception {
//		logger.info("Loop check scheudle Send Msg");
		if(marqueeMsgMap.size()>40) {
			marqueeMsgMap.clear();
		}

		for (MarqueeMsg msg : marqueeMsgMap.values()) {
			pushMarquee(msg, true);
			if (msg.stopTime < System.currentTimeMillis()) {
				marqueeMsgMap.remove(msg.msgId);
			}
		}
	}

	@Override
	public void onLogoutUpdateListener(Integer playerId, String remoteIp) {
		
	}

	public void onLoginListener(Integer userId){
		try {
			//重新加载发送
			reloadBoxMsg4UserLogin(userId);
			
			//重新加载活动与公告
			reloadActAndNotice4UserLogin(userId);
			
			reloadMarqueeMsg4UserLogin(userId);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void reloadMarqueeMsg4UserLogin(Integer userId) throws Exception {
		for(MarqueeMsg msg : marqueeMsgMap.values()) {
			if(msg.pushOnLogin == false) {
				continue;
			}
			if(System.currentTimeMillis() > msg.stopTime) { //已结束
				continue;
			}
			if(System.currentTimeMillis() < msg.startTime) { //未开始
				continue;
			}
			if(msg.receiver != -1 && msg.receiver != userId) {//不是我的
				continue;
			}
			
			if(msg.receiver == userId) {//个人消息
				sendInternal(msg);
			}
			
			if(msg.receiver == -1) { //全局消息
				MarqueeMsg copy = msg.copy();
				copy.receiver = userId;
				sendInternal(copy);
			}
		}
	}

	//推送活动与公告
	public void reloadActAndNotice4UserLogin(Integer userId) throws Exception {
		if(actNoticeList.isEmpty() == false) {
			ActNoticeMsg msg = new ActNoticeMsg();
			msg.msgId = System.currentTimeMillis();
			msg.actList = actNoticeList;
			msg.receiver = userId;
			sendInternal(msg);
		}
	}
	
	private void reloadBoxMsg4UserLogin(Integer userId) throws Exception {
		List<UserMsg> list = this.msgDao.getUserMsg(userId);
		
		Map<Long, UserAward> awardMap = awardDao.getUserAwardMap(userId);
		
		Set<Long> msgIdSet = new HashSet<Long>();
		boolean needUpdate = false;
		List<BoxMsg> toSendList = new ArrayList<BoxMsg>();		
		for(UserMsg item : list) {
			msgIdSet.add(item.getMsgId());
			
			if(item.getDeled()) {
				continue;
			}
			Date date = new Date();
			long msgId = item.getMsgId();
			Msg msg = msgDao.getMsg(msgId);
			if(msg == null) {
				item.setDeled(true);
				needUpdate = true;
				continue;
			}
			if(msg.getStatus() == 10) { //已经删除
				item.setDeled(true);
				needUpdate = true;
				continue;
			}
			if(date.after(msg.getStopDateTime())) {
				item.setDeled(true);
				needUpdate = true;
				continue;
			}
			if(item.getAwardId()!=null&&item.getAwardId() > 0 && item.getReceived() == false) {
				UserAward ua = awardMap.get(item.getAwardId());
				if(ua != null) {
					item.setReceived(ua.getReceived());
					needUpdate = true;
				}
			}
			if(needUpdate) {
				msgDao.update(item);
			}
			
			if(msg.getMsgMainType() == 0) { //box msg
				if(msgPushService.isMsgSended2User(userId, msg.getMsgType(), msg.getId())) {
					continue;
				}
				BoxMsg bmsg = msg2BoxMsg(userId, item, msg);
//				sendInternal(bmsg);
				toSendList.add(bmsg);
			} else {
				//走马灯消息不走这里
			}
		}
		
		//查找所有未加入玩家信箱的邮件
		List<Msg> msgList = this.msgDao.listShareMsg(new Date());
		for(Msg msg : msgList) {
			if(msgIdSet.contains(msg.getId())) { //已经在发送列表里面
				continue;
			}			
			UserMsg userMsg = insertUserMsg(msg.getRewardId(), msg, userId);
			msgIdSet.add(msg.getId());
			
			BoxMsg bmsg = msg2BoxMsg(userId, userMsg, msg);
//			sendInternal(bmsg);
			toSendList.add(bmsg);
		}
		if(!toSendList.isEmpty()) {
			BoxMsgList msg = new BoxMsgList();
			msg.msgList = toSendList;
			msg.receiver = userId;
			msgPushService.push2SendQueue(msg);
		}
	}

	private BoxMsg msg2BoxMsg(Integer userId, UserMsg userMsg, Msg msg) {
		BoxMsg bmsg = new BoxMsg();
		bmsg.awardId = msg.getRewardId() == null ? 0 : msg.getRewardId();
		bmsg.clientType = new Gson().fromJson(msg.getStrClientType(), new TypeToken<List<Integer>>(){}.getType());
		bmsg.img = msg.getImg();
		bmsg.msg = msg.getMsg();
		bmsg.msgId = msg.getId();
		bmsg.msgType = msg.getMsgType();
//		bmsg.params = new Gson().fromJson(msg.getStrParams(), new TypeToken<Map<String, String>>(){}.getType());
//		bmsg.popup = msg.getPopup();
//		bmsg.pos = new Gson().fromJson(msg.getStrPos(), new TypeToken<List<Integer>>(){}.getType());
//		bmsg.priority = msg.getPriority();
		bmsg.read = userMsg.getReaded();
		bmsg.startTime = msg.getStartDateTime().getTime();
		bmsg.stopTime = msg.getStopDateTime().getTime();
		bmsg.title = msg.getTitle();
		bmsg.senderId = msg.getSenderId();
		bmsg.senderName = msg.getSenderName();
		bmsg.senderId = msg.getSenderId();
		bmsg.senderName = msg.getSenderName();
		bmsg.receiver = userId;
		bmsg.attachNum = msg.getAttachNum() == null ? 0 : msg.getAttachNum();
		bmsg.received = userMsg.getReceived();
		bmsg.rawMsg = msg;
		return bmsg;
	}
	
	@Override
	public void sendChatMsg(GameChatMsg msg) throws Exception {
		msgPushService.push2SendQueue(msg);
	}

	public void sendInternal(BaseMsg msg) throws Exception {
		if(msgPushService.isMsgSended2User(msg.receiver, msg.msgType, msg.msgId)) {
			return;
		}
		msgPushService.push2SendQueue(msg);
	}
	
	public long saveMarquee(MarqueeMsg msg) {
		Marquee marquee = new Marquee();
		if(msg.stopTime > 0) {
			marquee.setEndTime(new Date(msg.stopTime));
		} else {
			marquee.setEndTime(new Date());
		}
		if(msg.startTime > 0) {
			marquee.setStartTime(new Date(msg.startTime));
		} else {
			marquee.setStartTime(new Date());
		}
		marquee.setLoopPlayCount(msg.loopPlayCount);
		marquee.setLoopPushCount(msg.loopPushCount);
		marquee.setLoopPushInterval(msg.loopPushInterval);
		marquee.setMarqueeType(msg.marqueeType);
		marquee.setMsgContent(msg.msg);
		marquee.setUserGroup(msg.userGroup);
		
		return msgDao.insertMarquee(marquee);
	}
	
	@Override
	public void sendMarquee(long msgId, boolean check) throws Exception {
		if(marqueeMsgMap.containsKey(msgId) == false) {
			Marquee marquee = this.msgDao.getMarquee(msgId);
			if(marquee == null) {
				return;
			}
			this.marqueeMsgMap.put(marquee.getId(), model2MarqueeMsg(marquee));
		}
		MarqueeMsg marquee = this.marqueeMsgMap.get(msgId);		
		pushMarquee(marquee, check);
	}

	@Override
	public Result repushActNotice() throws Exception {
		actNoticeList = msgDao.getActAndNoticeList();
		for(int userId : sessionManager.getOnlinePlayerIdList()) {
			ActNoticeMsg msg = new ActNoticeMsg();
			msg.msgId = System.currentTimeMillis();
			msg.actList = actNoticeList;
			msg.receiver = userId;
			sendInternal(msg);
		}
		return Result.success();
	}

	@Override
	public void sendMail(long id) throws Exception {
		Msg msg = this.msgDao.getMsg(id);
		int targetType = msg.getTargetType();
		if(targetType == 1) { //个人
			sendMsg(msg, msg.getTargetId());
			return;
		}
		
		if(targetType == 2) { //全服
			for(int userId : sessionManager.getOnlinePlayerIdList()) {
				sendMsg(msg, userId);
			}
		}
	}

	private void sendMsg(Msg msg, int userId) throws Exception {
		UserMsg userMsg = this.msgDao.getUserMsg(userId, msg.getId());
		if(userMsg == null) {
			logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			userMsg = insertUserMsg(msg.getRewardId(), msg, userId);
		}
		BoxMsg bmsg = msg2BoxMsg(userId, userMsg, msg);
		sendInternal(bmsg);
	}

	@Override
	public void saveAndSend(BaseMsg msg) throws Exception {
		if(msg instanceof MarqueeMsg) {
			msg.msgId = this.saveMarquee((MarqueeMsg)msg);
			this.marqueeMsgMap.put(msg.msgId, (MarqueeMsg)msg);
			
			pushMarquee((MarqueeMsg)msg, false);
		} else if(msg instanceof BoxMsg) {
			Award award = new Award();
			Map items = new HashMap();
			items.put("itemId","A001");
			items.put("count",((BoxMsg) msg).itemCount);
			award.setItems(new Gson().toJson(items));
			award.setInvalidTime(new Date(msg.stopTime));
			award.setSrcSystem("mailSys");
			award.setAwardNote("比赛场排行奖励");
			award.setAwardType(((BoxMsg) msg).awardType);
			award.setReceiverId(msg.receiver);
			award.setCtime(new Date());
			award.setAwardReason(ItemChangeReason.RankAward.toString());
			long awardId = this.awardDao.insert(award);
			award.setId(awardId);
			Msg a = new Msg();
			a.setImg(((BoxMsg) msg).img);
			a.setMsg(msg.msg);
			a.setMsgMainType(0);
			a.setMsgType(msg.msgType);
			a.setPopup(((BoxMsg) msg).popup);
			a.setPriority(msg.priority);			
			a.setRewardId(awardId);
			a.setSenderId(msg.senderId);
			a.setSenderName(msg.senderName);
			a.setStartDateTime(new Date(msg.startTime));
			a.setStopDateTime(new Date(msg.stopTime));
			a.setStrClientType(new Gson().toJson(msg.clientType));
			a.setStrParams(new Gson().toJson(((BoxMsg) msg).params));
			a.setStrPos(new Gson().toJson(msg.pos));
			a.setTargetType(msg.receiver > 0 ? 1 : 2);
			a.setTitle(((BoxMsg) msg).title);
			a.setAttachNum(((BoxMsg) msg).attachNum);
			a.setTargetId(msg.receiver);
			a.setStatus(1);
			a.setItemCount(((BoxMsg) msg).itemCount);
			a.setItemId("A001");
			a.setId(this.msgDao.insertMsg(a));
			msg.msgId = a.getId();
			((BoxMsg) msg).rawMsg = a;
			((BoxMsg) msg).awardId = awardId;
			//特定玩家的邮件才存入用户邮箱中
			if(msg.receiver > 0) {
				insertUserMsg(awardId, a, msg.receiver);
				this.sendInternal(msg);
			} else {
				for(int userId : this.sessionManager.getOnlinePlayerIdList()) {
					BoxMsg copy = ((BoxMsg) msg).copy();					
					copy.receiver = userId;
					insertUserMsg(awardId, a, userId);
					this.sendInternal(copy);
				}
			}
		} else {
			throw new RuntimeException("未知类型");
		}
	}

	private void pushMarquee(MarqueeMsg msg, boolean check) throws Exception {
		if(check) {
			if(msg.startTime > System.currentTimeMillis()) {
				return;
			}
			if(msg.stopTime < System.currentTimeMillis() && msg.pushedCount > 0) { //至少推送一次
				return;
			}
			if(System.currentTimeMillis() - msg.lastPushTime < msg.loopPushInterval*60*1000) {
				return;
			}
			if(msg.pushedCount >= msg.loopPushCount) {
				return;
			}
		}
		msg.pushedCount++;
		msg.lastPushTime = System.currentTimeMillis();
		if(msg.receiver > 0) { //特定玩家
			this.sendInternal(msg);
		} else {
			for(int userId : this.sessionManager.getOnlinePlayerIdList()) {//全局玩家
				MarqueeMsg copy = ((MarqueeMsg) msg).copy();
				copy.receiver = userId;
				this.sendInternal(copy);
			}
		}
	}

	private UserMsg insertUserMsg(long awardId, Msg a, int userid) {
		UserMsg userMsg = new UserMsg();
		userMsg.setAwardId(awardId);
		userMsg.setDeled(false);
		userMsg.setReaded(false);
		userMsg.setMsgId(a.getId());
		userMsg.setReceived(false);
		userMsg.setUserId(userid);
		userMsg.setMtime(new Date());
		userMsg.setCtime(new Date());
		msgDao.insert(userMsg);
		return userMsg;
	}

	@Override
	public void markMsgRead(int userId, long msgId) {
		UserMsg userMsg = this.msgDao.getUserMsg(userId, msgId);
		if(userMsg == null) {
			return;
		}
		userMsg.setReaded(true);
		this.msgDao.update(userMsg);
	}

	@Override
	public void closeSocket(int playerId) {
		MsgSession session = sessionManager.getIoSession(playerId);
		if(session != null) {
			session.channel.close();
		}
	}

	@Override
	public void removeAllMarquee() {
		this.marqueeMsgMap.clear();
	}

	@Override
	public void removeMarquee(long id) throws Exception {
		this.marqueeMsgMap.remove(id);
	}

	@Override
	public void removeMail(long id) throws Exception {
		Msg msg = this.msgDao.getMsg(id);
		msg.setStatus(10);
		this.msgDao.update(msg);
	}
	
//	public synchronized CUserMsg getUserMsg(int userId) {
//		CUserMsg userMsg = this.msgDao.getUserMsg(userId);
//		if(userMsg == null) {
//			userMsg = new CUserMsg();
//			userMsg.setId(userId);
//			userMsg.setMsgIds("");
//			this.msgDao.insert(userMsg);
//		}
//		return userMsg;
//	}
	
}
