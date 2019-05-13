package com.buding.task.network.cmd;

import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.ws.BattlePortalBroadcastService;
import com.buding.task.helper.TaskPushHelper;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ClubConfigSynCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	ClubDao clubDao;

	@Autowired
    BattlePortalBroadcastService battleService;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubConfigSyn ur = CLUB.ClubConfigSyn.parseFrom(data.packet.getData());
		//逻辑校验
        logger.info("玩法--"+JsonFormat.printToString(ur.toBuilder().build()));
        //业务逻辑
		Club club = clubDao.selectClub(ur.getClubId());
		if(club == null){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "没找到该俱乐部");
            return;
        }
		if(!StringUtils.equals(ur.getWanfa(),club.getClubWanfa())){
		    //修改玩法判断下当前有无在玩桌子,有不能解散
            List<DeskModel> desks = battleService.getClubCommonDesk(ur.getClubId());
            desks.addAll(battleService.getClubJiFenDesk(ur.getClubId()));
            if(desks.isEmpty()){
                club.setClubWanfa(ur.getWanfa());
            } else {
                for(DeskModel model : desks){
                   if(model == null) continue;
                   if(model.gameCount > 0) {
                       pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "当前有在玩桌子无法修改玩法");
                       return;
                   }
                }
                for(DeskModel model : desks){
                    if(model == null) continue;
                    battleService.destroyDesk("all",model.gameId,model.matchId,model.deskId);
                }
                club.setClubWanfa(ur.getWanfa());
            }

		}
		if(ur.getClubRoomEnterScore() != club.getEnterScore() ||
            ur.getClubRoomChouShuiScore() != club.getChoushuiScore() ||
            ur.getClubRoomChouShuiNum() != club.getChoushuiNum() ||
            ur.getClubRoomCanFuFen() != club.getCanFufen() ||
            ur.getClubRoomZengSongNum() != club.getZengsongNum()){
            List<DeskModel> desks = battleService.getClubJiFenDesk(ur.getClubId());
            if(desks != null && !desks.isEmpty()){
                for(DeskModel model : desks){
                    if(model == null) continue;
                    if(model.gameCount > 0) {
                        pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "当前有在玩桌子无法修改玩法");
                        return;
                    }
                }
                for(DeskModel model : desks){
                    if(model == null) continue;
                    battleService.destroyDesk("all",model.gameId,model.matchId,model.deskId);
                }
            }
        }

		if(ur.getClubRoomEnterScore() >= 0){
            club.setEnterScore(ur.getClubRoomEnterScore());
        }
        if(ur.getClubRoomChouShuiScore() >= 0){
		    club.setChoushuiScore(ur.getClubRoomChouShuiScore());
        }
        if(ur.getClubRoomChouShuiNum() > 0){
            club.setChoushuiNum(ur.getClubRoomChouShuiNum());
        }
        if(ur.getClubRoomCanFuFen() > 0){
            club.setCanFufen(ur.getClubRoomCanFuFen());
        }
        if(ur.getClubRoomZengSongNum() >= 0){
            club.setZengsongNum(ur.getClubRoomZengSongNum());
        }

        if(StringUtils.isNotBlank(ur.getClubName())){
            club.setClubName(ur.getClubName());
        }
        if(ur.getCreateRoomMode() != 0){
            club.setCreateRoomMode(ur.getCreateRoomMode());
        }
        if(StringUtils.isNotBlank(ur.getNotice())){
            club.setClubNotice(ur.getNotice());
        }

		clubDao.updateClub(club);

		CLUB.ClubConfigSyn.Builder bu = CLUB.ClubConfigSyn.newBuilder();
		bu.setClubId(club.getId());
		bu.setWanfa(club.getClubWanfa());
		bu.setCreateRoomMode(club.getCreateRoomMode());
		bu.setNotice(club.getClubNotice());
		bu.setClubName(club.getClubName());
		bu.setClubRoomEnterScore(club.getEnterScore());
		bu.setClubRoomChouShuiScore(club.getChoushuiScore());
		bu.setClubRoomChouShuiNum(club.getChoushuiNum());
		bu.setClubRoomCanFuFen(club.getCanFufen());
		bu.setClubRoomZengSongNum(club.getZengsongNum());
//		logger.info("配置"+JsonFormat.printToString(bu.build()));
		//发送消息
        List<ClubUser> allList = clubDao.selectClubAllUser(ur.getClubId());
        for(ClubUser clubUser1 : allList) {
            pushHelper.pushClubConfigSyn(clubUser1.getClubMemberId(), bu.build());
        }
	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubConfigSyn;
	}

}
