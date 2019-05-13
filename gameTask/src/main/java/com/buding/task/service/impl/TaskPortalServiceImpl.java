package com.buding.task.service.impl;

import com.buding.api.player.PlayerInfo;
import com.buding.db.model.Club;
import com.buding.db.model.ClubApply;
import com.buding.db.model.ClubUser;
import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.ws.TaskPortalService;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSession;
import com.buding.task.network.TaskSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import packet.club.CLUB;
import packet.game.Hall;
import packet.msgbase.MsgBase;

import java.util.Date;
import java.util.List;

public class TaskPortalServiceImpl implements TaskPortalService {
	@Autowired
	private TaskSessionManager taskSessionManager;

	@Autowired
	private ConfigManager configManager;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
	UserDao userDao;

	@Override
	public void stopService(String instanceId) {

	}

	@Override
	public void startService(String instanceId) {

	}

	@Override
	public void closeSocket(int playerId) {
		TaskSession session = taskSessionManager.getIoSession(playerId);
		if(session != null && session.channel.isOpen()){
			session.channel.close();
		}
	}

	@Override
	public void startPray() {
		for(Integer playeId: taskSessionManager.getOnlinePlayerIdList()){
			TaskSession session = taskSessionManager.getIoSession(playeId);
			Hall.ActivityStartNotify.Builder syn = Hall.ActivityStartNotify.newBuilder();
			syn.setActivityType(0);
			pushHelper.pushPBMsg(session, MsgBase.PacketType.ActivityStartNotify,syn.build().toByteString());
		}
	}

	@Override
	public void closePray() {
		for (Integer playeId : taskSessionManager.getOnlinePlayerIdList()) {
			TaskSession session = taskSessionManager.getIoSession(playeId);
			Hall.ActivityFinishNotify.Builder syn = Hall.ActivityFinishNotify.newBuilder();
			syn.setActivityType(0);
			pushHelper.pushPBMsg(session, MsgBase.PacketType.ActivityFinishNotify, syn.build().toByteString());
		}
	}

	@Override
	public void pushClubRoomModelSyn(int synType, long clubId, int roomType, DeskModel model) {
		List<ClubUser> list = clubDao.selectClubAllUser(clubId);
		if(list == null || list.isEmpty()) return;
		CLUB.ClubRoomModelSyn.Builder bu = CLUB.ClubRoomModelSyn.newBuilder();
		bu.setClubId(clubId);
		bu.setSynType(synType);
		bu.setRoomType(roomType);
		if(model != null) {
            bu.setDeskId(model.deskId);
            bu.setCurJuNum(model.gameCount);
            bu.setJuNum(model.totalJuNum);
            bu.setWanfa(model.wanfa);
            bu.setGameId(model.gameId);
            bu.setMatchId(model.matchId);
            bu.setLimitMax(model.limitMax);
            if(model.players != null && !model.players.isEmpty()) {
                for (PlayerInfo pl : model.players) {
                    CLUB.DeskPalyer.Builder pb = CLUB.DeskPalyer.newBuilder();
                    pb.setPlayerId(pl.playerId);
                    pb.setNickName(pl.name);
                    pb.setPlayerScore(pl.score);
                    pb.setImgUrl(pl.headImg);
                    bu.addPlayerList(pb.build());
                }
            }
        }
		for(ClubUser clubUser : list){
			pushHelper.pushClubRoomModelSyn(clubUser.getClubMemberId(),bu.build());
		}
	}

	@Override
	public void pushMemberInfoSyn(ClubUser clubUser) {
        List<ClubUser> allList = clubDao.selectClubAllUser(clubUser.getClubId());
        if(allList == null || allList.isEmpty()) return;

		CLUB.ClubMemberSyn.Builder syn = CLUB.ClubMemberSyn.newBuilder();
		syn.setSynType(TaskConstants.SYN_TYPE_MODIFY);
		syn.setClubId(clubUser.getClubId());
		syn.setMemberId(clubUser.getClubMemberId());
		syn.setMemberImg(clubUser.getClubMemberImg());
		syn.setMemberName(clubUser.getClubMemberName());
		syn.setMemberScore(clubUser.getClubMemberScore());
		syn.setMemberType(clubUser.getClubMemberType());
		syn.setOnline(taskSessionManager.isOnline(clubUser.getClubMemberId()) ? 1 : 0);

//        System.out.println("成员消息--"+syn.build());
        //发送消息
        for(ClubUser clubUser1 : allList){
            pushHelper.pushClubMemberSyn(clubUser1.getClubMemberId(), syn.build());
        }
	}

	@Override
	public void pushMemberInfoSyn(int userId) {
		List<ClubUser> clubs = clubDao.selectUserClub(userId);
		if(clubs == null || clubs.isEmpty()) return;
		for(ClubUser clubUser : clubs){
			pushMemberInfoSyn(clubUser);
		}
	}

	@Override
	public String applyClub(int userId,long clubId) {
		Club club = clubDao.selectClub(clubId);
		if(club == null){
			return "大爷您要找的俱乐部地球上找不到";
		}

		ClubUser clubUser = clubDao.selectClubUser(clubId,userId);
		if(clubUser != null) {
			return "大爷您已经加入该俱乐部";
		}

		ClubApply apply = clubDao.selectClubApply(clubId,userId);
		if(apply != null){
			return "大爷您已经申请了请等待结果";
		}
		apply = new ClubApply();
		apply.setClubId(clubId);
		apply.setApplyUserId(userId);
		apply.setCtime(new Date());
		clubDao.insertClubApply(apply);

		User user = userDao.getUser(userId);
		CLUB.ApplyInfo.Builder bu = CLUB.ApplyInfo.newBuilder();
		bu.setSynType(TaskConstants.SYN_TYPE_ADD);
		bu.setApplyType(TaskConstants.APPLY_TYPE_CLUB);
		bu.setClubId(clubId);
		bu.setPlayerId(user.getId());
		bu.setPlayerName(user.getNickname());
		bu.setPlayerImg(user.getHeadImg());
		List<ClubUser> manageList = clubDao.selectClubAllManageUser(clubId);
		for(ClubUser manager : manageList){
			pushHelper.pushApplyInfo(manager.getClubMemberId(), bu.build());
		}
		return "申请成功";
	}

}
