package com.buding.task.network.cmd;

import com.buding.common.cache.RedisClient;
import com.buding.db.model.Award;
import com.buding.db.model.Msg;
import com.buding.db.model.User;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.task.helper.TaskPushHelper;
import com.google.gson.Gson;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketType;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PrayCmd extends TaskBaseCmd {
	protected Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	protected TaskPushHelper pushHelper;

	@Autowired
	ConfigManager configManager;

	@Autowired
	AwardService awardService;

	@Autowired
	UserService userService;

	@Autowired
	MsgDao msgDao;

	@Autowired
	MsgPortalService portal;


	@Autowired
	UserDao userDao;

	@Autowired
	RedisClient redisClient;

	public static ConcurrentHashMap<Integer,Long> opTimeMap = new ConcurrentHashMap<>();
	public static Long sendMarqueeTime = 0l;

	@Override
	public void execute(CmdData data) throws Exception {
//		pushHelper.pushErrorMsg(data.session,PacketType.GlobalMsgSyn,"女神正在沉睡中,预计下个版本苏醒.....");
//		return;
		long now = System.currentTimeMillis();
		int userId= data.session.userId;
		if(!opTimeMap.containsKey(userId)){
			opTimeMap.put(userId,now);
		}else{
			if((now - opTimeMap.get(userId))<2*1000){
//				logger.info("returnreturnreturnreturnreturn");
				return;
			}
		}
		opTimeMap.put(userId,now);
		long min = DateUtils.parseDate(DateFormatUtils.format(new Date(),"yyyyMMdd")+" 20:00:00",new String[]{"yyyyMMdd hh:mm:ss"}).getTime();
		long max = DateUtils.parseDate(DateFormatUtils.format(new Date(),"yyyyMMdd")+" 20:10:00",new String[]{"yyyyMMdd hh:mm:ss"}).getTime();
		if( now<min || now >max){
			pushHelper.pushErrorMsg(data.session,PacketType.GlobalMsgSyn,"祈福时间已经过去");
			return;
		}
		Hall.PrayRequest req = Hall.PrayRequest.parseFrom(data.packet.getData());
		int num = req.getNum()>20?20:req.getNum();

		String key = "PRAY_"+DateFormatUtils.format(new Date(),"yyyyMMdd");
		Double score = redisClient.zscore(key,userId+"");
		if(score==null){
			score = 0.0;
		}
		score += num;
		redisClient.zadd(key,score,userId+"");
		int diamondNum = 0;
		int coinNum = 0;
		for (int i = 0; i < num; i++) {
			int random = (int)(Math.random()*10000);
//			random = 8888;
			if(random==8888){//万分之一获取1000钻石
				diamondNum += 100;
			}else if(random>=0 && random <100){//百分之一获取300金币
				coinNum += 300;
			}else if(random>=100 && random<110){//千分之一获取100钻石
				diamondNum += 2;
			}else if(random == 9999){//万分之一获取10W金币
				coinNum += 100000;
			}else if(random>=1000 && random< 8888){//2分之一获取1-3金币
				coinNum += (int)(Math.random()*10+1);
			}else if(random>8888 && random<= 8898){
				diamondNum += 1;
			}
//			if(StringUtils.isNotBlank(awardName)){
//
//
////				User user = userDao.getUser(data.session.userId);
////				List<ItemPkg> list = new ArrayList<>();
////				ItemPkg pkg = new ItemPkg();
////				pkg.itemId = StringUtils.equals("钻石",awardName)?"A001":"D001";
////				pkg.count = awardNum;
////				list.add(pkg);
//				if(StringUtils.equals("钻石",awardName)){
//					map.put(1,)
//				}else{
//				}
////				sendMail(user,list);
////				if(StringUtils.equals("钻石",awardName)){
////					sendMarquee(user,awardNum+awardName);
////				}
//
//			}
		}
		if(diamondNum>0) {
			Hall.PrayResponse.Builder syn = Hall.PrayResponse.newBuilder();
			syn.setAwardName("钻石");
			syn.setAwardNum(diamondNum + "");
			pushHelper.pushPBMsg(data.session, PacketType.PrayResponse, syn.build().toByteString());
			userService.changeDiamond(userId,diamondNum,false,ItemChangeReason.PRAY);
			if((now-sendMarqueeTime)>10*1000) {
				sendMarquee(userDao.getUser(userId), diamondNum + "钻石");
				sendMarqueeTime = now;
			}
		}
		if(coinNum>0){
			Hall.PrayResponse.Builder syn = Hall.PrayResponse.newBuilder();
			syn.setAwardName("金币");
			syn.setAwardNum(coinNum + "");
			pushHelper.pushPBMsg(data.session, PacketType.PrayResponse, syn.build().toByteString());
			userService.changeCoin(userId ,coinNum,false,ItemChangeReason.PRAY);
			if((now-sendMarqueeTime)>10*1000) {
				sendMarquee(userDao.getUser(userId), coinNum + "金币");
				sendMarqueeTime = now;
			}
		}
	}


	private void sendMarquee(User user, String name) {
		MarqueeMsg msg = new MarqueeMsg();
		msg.loopPushInterval = 1;
		msg.loopPushCount = 1;
		msg.marqueeType = 1;
		msg.msg = "恭喜玩家<color=red>"+user.getNickname()+"</color>在女神祈福活动中轻松拿下"+name+",小伙伴们快祝福他吧";
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

	private void sendMail(User user,List<ItemPkg> itemlist) {

		Award award = new Award();
		award.setItems(new Gson().toJson(itemlist));
		award.setInvalidTime(new Date(getCurrYearLast()));
		award.setSrcSystem("mailSys");
		award.setAwardNote("女神祈福奖励");
		award.setAwardType((1));
		award.setReceiverId(user.getId());
		award.setCtime(new Date());
		award.setAwardReason(ItemChangeReason.PRAY.toString());
		long awardId = awardService.addAward(award);

		Msg a = new Msg();
		a.setMsg("您在女神祈福活动中获得了丰厚奖励,请大侠收下");
		a.setMsgMainType(0);
		a.setPriority(0);
		a.setRewardId(awardId);
		a.setSenderId(-1);
		a.setSenderName("系统");
		a.setStartDateTime(new Date());
		a.setStopDateTime(new Date(getCurrYearLast()));
		a.setTargetType(1);
		a.setTitle("女神祈福奖励");
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
			logger.info("祈福邮件发送失败"+user.getId()+"玩家为==="+user.getNickname()+"====="+user.getId());
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
		return PacketType.PrayRequest;
	}	
}
