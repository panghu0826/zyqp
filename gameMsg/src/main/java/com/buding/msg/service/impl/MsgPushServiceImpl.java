package com.buding.msg.service.impl;

import com.googlecode.protobuf.format.JsonFormat;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Msg.ActAndNoticeMsgSyn;
import packet.game.Msg.ActMsgModel;
import packet.game.Msg.MailMsgModel;
import packet.game.Msg.MarqueeMsgSyn;
import packet.game.Msg.NewMailMsgSyn;
import packet.game.MsgGame.GameChatMsgSyn;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.server.BaseServerComponent;
import com.buding.common.thread.NamedThreadFactory;
import com.buding.common.util.StrUtil;
import com.buding.db.model.ActNotice;
import com.buding.hall.module.msg.vo.ActNoticeMsg;
import com.buding.hall.module.msg.vo.BaseMsg;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgList;
import com.buding.hall.module.msg.vo.GameChatMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.msg.vo.TextMsg;
import com.buding.msg.cluster.MsgClusterServer;
import com.buding.msg.helper.MsgPushHelper;
import com.buding.msg.network.MsgSession;
import com.buding.msg.network.MsgSessionManager;
import com.buding.msg.service.MsgPushService;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

@Component
public class MsgPushServiceImpl extends BaseServerComponent implements InitializingBean, MsgPushService {
	private Logger logger = LogManager.getLogger(getClass());

	public BlockingQueue<BaseMsg> msgQueue = new LinkedBlockingQueue<BaseMsg>();
	private boolean run = true;
	
	@Autowired
	public MsgSessionManager sessionManager;
	
	@Autowired
	MsgPushHelper pushHelper;

	ExecutorService pool;
	
	ConcurrentMap<Integer, ConcurrentSet<String>> sendedMap = new ConcurrentHashMap<Integer, ConcurrentSet<String>>();
	
	@Autowired
	MsgClusterServer server;

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		
		server.getComponentList().add(this);
	}

	@Override
	public void onLoginListener(Integer userId) {
		sendedMap.remove(userId);
	}

	public void onLogoutUpdateListener(Integer userId, String remoteIp) {
		sendedMap.remove(userId);
	}
	
	public void init() {
		pool = Executors.newCachedThreadPool(new NamedThreadFactory(new ThreadGroup("消息线程组"), "消息发送线程"));
		addWorker();
	}

	private void addWorker() {
		pool.submit(new Worker());
	}

	public void push2SendQueue(BaseMsg msg) throws Exception {
		msgQueue.put(msg);
	}

	public void trySendMsg() throws Exception {
		while(true) {
			BaseMsg msg = msgQueue.take();
			if(msg == null) {
				break;
			} else {
				logger.info("take a msg");
			}
			if (msg instanceof MarqueeMsg) {
				MarqueeMsgSyn.Builder msb = MarqueeMsgSyn.newBuilder();
				msb.setContent(msg.msg);
				msb.setPlayerSetting(((MarqueeMsg) msg).playSetting+"x"+((MarqueeMsg) msg).level);
				pushHelper.pushPBMsg(msg.receiver, PacketType.MarqueeMsgSyn, msb.build().toByteString());				
			}  else if (msg instanceof BoxMsgList) {
				BoxMsgList msgList = (BoxMsgList)msg;
				NewMailMsgSyn.Builder mb = NewMailMsgSyn.newBuilder();
				for(BoxMsg item : msgList.msgList) {
					MailMsgModel.Builder mail = boxMsg2PBModel(item);
					mb.addMails(mail);
				}
				pushHelper.pushPBMsg(msgList.receiver, PacketType.NewMailMsgSyn, mb.build().toByteString());
			} else if (msg instanceof BoxMsg) {
				NewMailMsgSyn.Builder mb = NewMailMsgSyn.newBuilder();
				MailMsgModel.Builder mail = boxMsg2PBModel((BoxMsg)msg);
				logger.info("mail=========="+JsonFormat.printToString(mail.build()));
				logger.info("mailmsg=========="+msg);
				mb.addMails(mail);
				pushHelper.pushPBMsg(msg.receiver, PacketType.NewMailMsgSyn, mb.build().toByteString());
			} else if (msg instanceof TextMsg) {
				JSONObject json = new JSONObject();
				json.put(((TextMsg) msg).key, msg.msg);
				String txt = new Gson().toJson(json);
				//TODO
//				sessionManager.writeTextWebSocketFrame(userId, txt);
			} else if (msg instanceof GameChatMsg) {
				GameChatMsg gameMsg = (GameChatMsg)msg;
				GameChatMsgSyn.Builder gb = GameChatMsgSyn.newBuilder();
				gb.setData(ByteString.copyFrom(gameMsg.data));
				gb.setDeskId(gameMsg.deskId);
				gb.setPosition(gameMsg.senderPosition);
				for(int receiver : gameMsg.receiverIds) {
					MsgSession session = sessionManager.getIoSession(receiver);
					if(session != null) {
						pushHelper.pushPBMsg(session, PacketType.GameChatMsgSyn, gb.build().toByteString());
					}
				}
			} else if (msg instanceof ActNoticeMsg) {
				ActNoticeMsg actMsg = (ActNoticeMsg)msg;
				ActAndNoticeMsgSyn.Builder ab = ActAndNoticeMsgSyn.newBuilder();
				for(ActNotice model : actMsg.actList) {
					if(model.getType() == 0) { //公告
						ab.setNotice(StrUtil.null2string(model.getContent(), ""));
					} else {
						ActMsgModel.Builder amsg = ActMsgModel.newBuilder();
						amsg.setContent(StrUtil.null2string(model.getContent(), ""));
						amsg.setTitle(StrUtil.null2string(model.getTitle(), ""));
						ab.addActs(amsg);
					}
				}
				pushHelper.pushPBMsg(actMsg.receiver, PacketType.ActAndNoticeMsgSyn, ab.build().toByteString());
			}
		}
	}

	private MailMsgModel.Builder boxMsg2PBModel(BoxMsg item) {
		MailMsgModel.Builder mail = MailMsgModel.newBuilder();
		mail.setMailId(item.msgId);
		mail.setAttachNum(0);
		if(item.awardId > 0 && item.rawMsg.getItemId() != null) {
			if(item.rawMsg.getItemId().startsWith("A")) {
				mail.setAttachType(2); //0 无附件 1 金币 2钻石
			} else {
				mail.setAttachType(1); //0 无附件 1 金币 2钻石
			}
			mail.setAttachNum(item.rawMsg.getItemCount());
		} else {
			mail.setAttachType(0); //0 无附件 1 金币 2房卡
		}
		
		mail.setContent(item.msg);
		if (item.received) {
			mail.setState(2); //已经领取
		} else if(item.read) {
			mail.setState(1); //已读
		} else {
			mail.setState(0); //未读
		}
		mail.setTitle(item.title);
		mail.setSendTime(item.startTime/1000);
		return mail;
	}
	
	@Override
	public boolean isMsgSended2User(int userId, String msgType, long msgId) {
		ConcurrentSet<String> set = this.sendedMap.get(userId);
		if(set == null) {
			set = new ConcurrentSet<String>();
			this.sendedMap.putIfAbsent(userId, set);
			set = this.sendedMap.get(userId);
		}
		return set.contains(msgType+"_" + msgId);
	}

	class Worker implements Runnable {
		public void run() {
			while (run) {
				try {
					trySendMsg();
					Thread.sleep(50);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "MsgPusherService";
	}

	@Override
	public String getStatusDesc() {
		return "WatingPushMsgCount:" + msgQueue.size();
	}
	
	
}
