package com.buding.task.network.cmd;

import com.buding.db.model.Award;
import com.buding.db.model.Msg;
import com.buding.db.model.User;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.config.LunPanConfig;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.task.helper.TaskPushHelper;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class SymplecticRunnerCmd extends TaskBaseCmd {
	protected Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	protected TaskPushHelper pushHelper;
	
	@Autowired
	ConfigManager configManager;

	@Autowired
	UserDao userDao;

	@Autowired
	AwardService awardService;

	@Autowired
	MsgDao msgDao;

	@Autowired
	MsgPortalService portal;
	
	@Override
	public void execute(CmdData data) throws Exception {
		int userId = data.session.userId;
		User user = userDao.getUser(userId);
		int lunpanNums = user.getLunpanNums();
		if(user.getCanLunpan()<=lunpanNums){
			pushHelper.pushErrorMsg(data.session,PacketType.GlobalMsgSyn,"超过可转动次数");
		}
		user.setLunpanNums(lunpanNums+1);
		userDao.updateUser(user);

		//几率
		int random = (int) (Math.random()*10000);
		LunPanConfig result = new LunPanConfig();
		for(LunPanConfig config:configManager.lunPanConfigMap.values()){
			if(random>=config.min && random<config.max){
				result = config;
				break;
			}
		}

		sendMail(user,result);
		sendMarquee(user,result);

		Hall.SymplecticRunnerResponse.Builder syn = Hall.SymplecticRunnerResponse.newBuilder();
		syn.setId(Integer.valueOf(result.id));
		syn.setLunpanNum(user.getCanLunpan()-user.getLunpanNums());
		pushHelper.pushPBMsg(data.session,PacketType.SymplecticRunnerResponse,syn.build().toByteString());
	}

	public static void main(String[] args) {
		int random = (int) (Math.random()*10000);
		System.out.println(random);
	}
	private void sendMarquee(User user,LunPanConfig config) {
		MarqueeMsg msg = new MarqueeMsg();
		msg.loopPushInterval = 1;
		msg.loopPushCount = 1;
		msg.marqueeType = 1;
		msg.msg = "天赐洪福,<color=red>"+user.getNickname()+"</color>运气爆表,在幸运轮盘活动中轻松拿下"+config.name+",小伙伴们快祝福他吧";
		msg.playSetting = "1x1";
		msg.receiver = -1;
		msg.senderId = -1;
		msg.senderName = "系统管理员";
		msg.startTime = System.currentTimeMillis();
		msg.stopTime = System.currentTimeMillis();
		msg.pushOnLogin = false;
		try {
			portal.sendMarqueeMsg(msg);
		}catch (Exception e){
			logger.info("error======跑马灯发送失败"+user.getNickname());
		}
	}

	private void sendMail(User user,LunPanConfig config) {
		List<ItemPkg> itemlist = new ArrayList<>();
		long awardId = 0;
		if(!StringUtils.equals(config.id,"40011")) {
			Award award = new Award();
			itemlist = config.items;
			award.setItems(new Gson().toJson(itemlist));
			award.setInvalidTime(new Date(getCurrYearLast()));
			award.setSrcSystem("mailSys");
			award.setAwardNote("轮盘奖励");
			award.setAwardType((1));
			award.setReceiverId(user.getId());
			award.setCtime(new Date());
			award.setAwardReason(ItemChangeReason.LUNPAN_AWARD.toString());
			awardId = awardService.addAward(award);
		}

		Msg a = new Msg();
		a.setMsg("您在轮盘活动中获得了丰厚奖励,请大侠收下(若为实物奖励,请联系客服获取)");
		a.setMsgMainType(0);
		a.setPriority(0);
		a.setRewardId(StringUtils.equals(config.id,"40011")?0:awardId);
		a.setSenderId(-1);
		a.setSenderName("系统");
		a.setStartDateTime(new Date());
		a.setStopDateTime(new Date(getCurrYearLast()));
		a.setTargetType(1);
		a.setTitle("轮盘奖励");
		a.setTargetId(user.getId());
		a.setStatus(1);
		a.setAttachNum(1);
		a.setItemCount(itemlist!=null && itemlist.size()>0?itemlist.get(0).count:null);
		a.setItemId(itemlist!=null && itemlist.size()>0?itemlist.get(0).itemId:null);
		a.setId(msgDao.insertMsg(a));

		UserMsg userMsg = new UserMsg();
		userMsg.setAwardId(awardId == 0 ?null:awardId);
		userMsg.setDeled(false);
		userMsg.setReaded(false);
		userMsg.setMsgId(a.getId());
		userMsg.setReceived(false);
		userMsg.setUserId(user.getId());
		userMsg.setMtime(new Date());
		userMsg.setCtime(new Date());
		long id = msgDao.insert(userMsg);

		try {
			portal.sendMail(a.getId());
		}catch (Exception e){
			logger.info("奖励邮件发送失败"+user.getId()+"玩家为==="+user.getNickname()+"====="+user.getId());
		}
	}

	private long getCurrYearLast() {
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, currentYear+3);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime().getTime();
	}
	@Override
	public PacketType getKey() {
		return PacketType.SymplecticRunnerRequest;
	}	
}
