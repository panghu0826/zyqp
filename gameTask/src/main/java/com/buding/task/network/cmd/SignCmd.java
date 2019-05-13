package com.buding.task.network.cmd;

import com.buding.db.model.Award;
import com.buding.db.model.Msg;
import com.buding.db.model.User;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.config.SignConfig;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.task.helper.TaskPushHelper;
import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketType;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class SignCmd extends TaskBaseCmd {
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
		int sign = user.getSignNums();
		int signDay = user.getSignDay();
		if(user.getCanSign()<=sign){
			pushHelper.pushErrorMsg(data.session,PacketType.GlobalMsgSyn,"超过可签到次数");
			return;
		}
		user.setSignNums(sign+1);

		Date lastSign = user.getLastSign();
		if(lastSign==null){
			signDay = 1;
		}else if(differentDays(lastSign,new Date())>0){
			signDay++;
		}

		user.setLastSign(new Date());
		user.setSignDay(signDay);
		userDao.updateUser(user);

		String id = String.valueOf(signDay+50000);
		SignConfig config = configManager.signConfigMap.get(id);
		sendMarquee(user,config);
		sendMail(user,config);

		Hall.SignResponse.Builder syn = Hall.SignResponse.newBuilder();
		syn.setSignDays(user.getSignDay());
		syn.setSignNum(user.getCanSign()-user.getSignNums());
		pushHelper.pushPBMsg(data.session,PacketType.SignResponse,syn.build().toByteString());
	}

	/**
	 * date2比date1多的天数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int differentDays(Date date1,Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int day1= cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if(year1 != year2) { //不同年
			int timeDistance = 0 ;
			for(int i = year1 ; i < year2 ; i ++) {
				if(i%4==0 && i%100!=0 || i%400==0) {//闰年
					timeDistance += 366;
				}
				else {
					timeDistance += 365;
				}
			}
			return timeDistance + (day2-day1) ;
		} else {
			return day2-day1;
		}
	}


	private void sendMarquee(User user,SignConfig config) {
		logger.info("user"+user+"config"+config);
		MarqueeMsg msg = new MarqueeMsg();
		msg.loopPushInterval = 1;
		msg.loopPushCount = 1;
		msg.marqueeType = 1;
		msg.msg = "恭喜玩家<color=red>"+user.getNickname()+"</color>在签到活动中轻松拿下"+config.name+",小伙伴们快祝福他吧";
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

	private void sendMail(User user,SignConfig config) {

		Award award = new Award();
		List<ItemPkg> itemlist = config.items;
		award.setItems(new Gson().toJson(itemlist));
		award.setInvalidTime(new Date(getCurrYearLast()));
		award.setSrcSystem("mailSys");
		award.setAwardNote("签到奖励");
		award.setAwardType((1));
		award.setReceiverId(user.getId());
		award.setCtime(new Date());
		award.setAwardReason(ItemChangeReason.SIGN.toString());
		long awardId = awardService.addAward(award);

		Msg a = new Msg();
		a.setMsg("您在签到活动中获得了丰厚奖励,请大侠收下");
		a.setMsgMainType(0);
		a.setPriority(0);
		a.setRewardId(awardId);
		a.setSenderId(-1);
		a.setSenderName("系统");
		a.setStartDateTime(new Date());
		a.setStopDateTime(new Date(getCurrYearLast()));
		a.setTargetType(1);
		a.setTitle("签到奖励");
		a.setAttachNum(1);
		a.setTargetId(user.getId());
		a.setStatus(1);
		a.setItemCount(itemlist.get(0).count);
		a.setItemId(itemlist.get(0).itemId);
		a.setId(msgDao.insertMsg(a));

		UserMsg userMsg = new UserMsg();
		userMsg.setAwardId(awardId);
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
			logger.info("签到邮件发送失败"+user.getId()+"玩家为==="+user.getNickname()+"====="+user.getId());
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
		return PacketType.SignRequest;
	}	
}
