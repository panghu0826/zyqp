package com.buding.task.network.cmd;

import com.buding.api.player.PlayerInfo;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.hall.module.ws.BattlePortalBroadcastService;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.game.MsgGame;
import packet.msgbase.MsgBase.PacketType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 *
 */
@Component
public class ClubRoomListReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserService userService;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	BattlePortalBroadcastService battleService;


	@Override
	public void execute(CmdData data) throws Exception {
		CLUB.ClubRoomListReq ur = CLUB.ClubRoomListReq.parseFrom(data.packet.getData());
		if(ur.getClubId() ==0) return;
	    //业务逻辑
        CLUB.ClubRoomListRsp.Builder bu = CLUB.ClubRoomListRsp.newBuilder();
        bu.setClubId(ur.getClubId());
		List<DeskModel> list = ur.getRoomType() == 0 ?
				battleService.getClubCommonDesk(ur.getClubId()) : battleService.getClubJiFenDesk(ur.getClubId());
		logger.info("deskList---"+new Gson().toJson(list));
		for(DeskModel model : list){
			CLUB.ClubRoomModelSyn.Builder m = CLUB.ClubRoomModelSyn.newBuilder();
			m.setRoomType(ur.getRoomType());
			m.setCurJuNum(model.gameCount);
			m.setJuNum(model.totalJuNum);
			m.setWanfa(model.wanfa);
			m.setGameId(model.gameId);
			m.setClubId(ur.getClubId());
			m.setSynType(TaskConstants.SYN_TYPE_ALL);
			m.setDeskId(model.deskId);
            m.setMatchId(model.matchId);
            m.setLimitMax(model.limitMax);
            for(PlayerInfo pl : model.players){
				CLUB.DeskPalyer.Builder pb = CLUB.DeskPalyer.newBuilder();
				pb.setPlayerId(pl.playerId);
				pb.setNickName(pl.name);
				pb.setPlayerScore(pl.score);
				pb.setImgUrl(pl.headImg);
				m.addPlayerList(pb.build());
			}
			bu.addRoomList(m.build());
		}

        //发送消息
		pushHelper.pushClubRoomListRsp(data.session, bu.build());
	}

	@Override
	public PacketType getKey() {
		return PacketType.ClubRoomListReq;
	}

}
