package com.buding.hall.module.award.service;

import java.util.*;

import com.buding.db.model.UserMsg;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.msg.helper.MsgPushHelper;
import com.buding.task.helper.TaskPushHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.result.Result;
import com.buding.db.model.Award;
import com.buding.db.model.UserAward;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import packet.game.Msg;
import packet.msgbase.MsgBase;

public class AwardServiceImpl implements AwardService {
	@Autowired
	AwardDao awardDao;
	
	@Autowired
	ItemService itemService;

	@Autowired
	protected MsgPushHelper pushHelper;

	@Autowired
	MsgDao msgDao;

	protected Logger logger = LogManager.getLogger(getClass());


	@Override
	public long addAward2User(int userId, Award award) {
		long id = this.awardDao.insert(award);
		award.setId(id);
		addAward2User(userId, id);
		return id;
	}

	@Override
	public void addAward2User(int userId, long awardId) {
		UserAward ua = new UserAward();
		ua.setAwardId(awardId);
		ua.setReceived(false);
		ua.setUserId(userId);
		ua.setMtime(new Date());
		this.awardDao.insertUserAward(ua);
	}

	@Override
	public long addAward(Award award) {
		return this.awardDao.insert(award);
	}

	@Override
	public Result receiveAward(int userId, long awardId) {
		Award award = this.awardDao.getAward(awardId);
		if(award == null) {
			return Result.fail("礼品不存在");
		}
		if(award.getAwardType() == 1 && award.getReceiverId() != userId) {
			return Result.fail("礼品不属于你");
		}
		
		UserAward ua = this.awardDao.get(awardId, userId);
		if(ua != null && ua.getReceived()) {
			return Result.fail("重复领取");
		}
		
		if(award.getInvalidTime().before(new Date())) {
			return Result.fail("礼品已经过期");
		}
		
		if(ua == null) {
			ua = new UserAward();
			ua.setAwardId(awardId);
			ua.setMtime(new Date());
			ua.setReceived(true);
			ua.setUserId(userId);
			ua.setUserId(userId);
			this.awardDao.insertUserAward(ua);
		} else {
			ua.setReceived(true);
			this.awardDao.updateUserAward(ua);
		}
				
		List<ItemPkg> list = new Gson().fromJson(award.getItems(), new TypeToken<List<ItemPkg>>(){}.getType());
		ItemChangeReason reason = award.getAwardReason() == null ? ItemChangeReason.OTHER : ItemChangeReason.valueOf(award.getAwardReason());
		this.itemService.addItem(userId, reason, award.getSrcSystem() + "_" + award.getAwardNote()+"_"+award.getId(), list);
		
		return Result.success();
	}

	@Override
	public Result quickReceiveAward(int userId, List<UserMsg> userMsgList) {
		Set<Long> msgIdList = new HashSet<>();

		ItemPkg diamondPkg = new ItemPkg();
		diamondPkg.itemId = "A001";
		diamondPkg.count = 0;
		ItemPkg coinPkg = new ItemPkg();
		coinPkg.itemId = "D001";
		coinPkg.count = 0;
		for(UserMsg userMsg:userMsgList){
			Long awardId = userMsg.getAwardId();
			if(awardId==null||awardId<=0) continue;
			com.buding.db.model.Msg msg = this.msgDao.getMsg(userMsg.getMsgId());
			if(msg!=null&&msg.getStatus()==10) continue;

			Award award = this.awardDao.getAward(awardId);
			if(award == null) continue;
			if(award.getAwardType() == 1 && award.getReceiverId() != userId) continue;

			UserAward ua = this.awardDao.get(awardId, userId);
			if(ua != null && ua.getReceived()) continue;
			if(award.getInvalidTime().before(new Date())) continue;

			if(ua == null) {
				ua = new UserAward();
				ua.setAwardId(awardId);
				ua.setMtime(new Date());
				ua.setReceived(true);
				ua.setUserId(userId);
				ua.setUserId(userId);
				this.awardDao.insertUserAward(ua);
			} else {
				ua.setReceived(true);
				this.awardDao.updateUserAward(ua);
			}

			List<ItemPkg> list = new Gson().fromJson(award.getItems(), new TypeToken<List<ItemPkg>>(){}.getType());
			for(ItemPkg pkg:list){
				if(StringUtils.equals(pkg.itemId,"A001")){
					msgIdList.add(userMsg.getMsgId());
					diamondPkg.count += pkg.count;
				}else if(StringUtils.equals(pkg.itemId, "D001")){
					msgIdList.add(userMsg.getMsgId());
					coinPkg.count += pkg.count;
				}
			}
		}

		if(diamondPkg.count==0 && coinPkg.count==0){
			return Result.fail("无附件可领取");
		}

		Msg.quickOperResponse.Builder qb = Msg.quickOperResponse.newBuilder();
		qb.addAllMailId(msgIdList);
		pushHelper.pushPBMsg(userId, MsgBase.PacketType.quickReciveAward,qb.build().toByteString() );

		List<ItemPkg> list = new ArrayList<>();
		if(diamondPkg.count>0) {
			list.add(diamondPkg);
		}
		if(coinPkg.count>0) {
			list.add(coinPkg);
		}

		logger.info("快速领取后领取奖品=========="+list);
		ItemChangeReason reason = ItemChangeReason.QUICK_RECEIVE_AWARD;
		this.itemService.addItem(userId, reason, "快速领取邮件附件奖励", list);
		return Result.success();
	}

	@Override
	public List<Long> quickRemoveAward(int userId, List<UserMsg> userMsgList) {
		List<Long> msgIdList = new ArrayList<>();
		for(UserMsg userMsg:userMsgList){
			Long awardId = userMsg.getAwardId();
			if(awardId == null||awardId==0) continue;

			com.buding.db.model.Msg msg = this.msgDao.getMsg(userMsg.getMsgId());
			if(msg!=null&&msg.getStatus()==10) continue;

			Award award = this.awardDao.getAward(awardId);
			if(award == null) {
				msgIdList.add(userMsg.getMsgId());
			}else if(award.getAwardType() == 1 && award.getReceiverId() != userId) {
				msgIdList.add(userMsg.getMsgId());
			}else{
				UserAward ua = this.awardDao.get(awardId, userId);
				if(ua != null && ua.getReceived()){
					msgIdList.add(userMsg.getMsgId());
				}
			}
		}
		return msgIdList;
	}

}
