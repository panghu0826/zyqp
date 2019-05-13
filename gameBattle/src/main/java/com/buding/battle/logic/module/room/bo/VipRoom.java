package com.buding.battle.logic.module.room.bo;

import com.buding.api.desk.MJDesk;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.Constants;
import com.buding.battle.logic.module.common.EnterRoomResult;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.BattleConstants;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.DeskImpl;
import com.buding.battle.logic.module.desk.bo.VipDesk;
import com.buding.battle.logic.module.match.Match;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.module.game.model.DeskModel;
import net.sf.json.JSONObject;

public class VipRoom extends RoomImpl {
	public VipRoom(Match parent) {
		super(parent);
	}

	@Override
	public Result playerEnroll(BattleContext ctx) {
		UserRoom desk = (UserRoom)ctx.params.get("vipDesk");
		if(desk == null) {
			return Result.fail("vip房间不存在");
		}
		
		return Result.success();
	}

	@Override
	public synchronized TResult<CommonDesk> applyDesk(BattleContext ctx) throws Exception {
		String deskId = ctx.getDeskId();
		CommonDesk deskIns = this.guard.getDeskById(ctx.deskId);
		
		if(ctx.params.get("vipDesk") == null) {
			logger.error("act=applyDesk;error=noVipDesk;deskId={};roomId={};", ctx.deskId, getRoomId());
			return null;
		}
		
		if(deskIns == null) {
			UserRoom ur = ServiceRepo.userRoomDao.getByCode(deskId);
			if(ur == null) {
				logger.info("act=playerTrySit;error=notFindRoomInMYSQL;playerId={}", ctx.playerId);
				return TResult.fail1("未找到该房间");
			}
			int clubId = JSONObject.fromObject(ur.getParams()).getInt("clubId");
			//校验玩家是否满足俱乐部信息
			if(clubId > 0){
				ClubUser clubUser =  ServiceRepo.clubDao.selectClubUser(clubId,ctx.playerId);
				Club club = ServiceRepo.clubDao.selectClub(clubId);
				if(clubUser == null){
					logger.info("act=playerTrySit;error=notInClub;playerId={};", ctx.playerId);
					return TResult.fail1("大爷您不是该俱乐部的成员");
				}
				if(club.getCreateRoomMode() == Constants.CLUB_CREATE_CREATE_ROOM_MODE_MANAGE
						&& clubUser.getClubMemberType() != Constants.CLUB_MEMEBER_TYPE_OWNER
						&& clubUser.getClubMemberType() != Constants.CLUB_MEMEBER_TYPE_MANAGER ){
					logger.info("act=playerTrySit;error=noPermissionCreateRoom;playerId={};", ctx.playerId);
					return TResult.fail1("大爷您无权开房");
				}
			}
			deskIns = genDesk(deskId,ctx.getWanfa());
			deskIns = guard.tryAddDesk(deskIns);
			afterDeskCreate(deskIns);
			return TResult.sucess2(deskIns,true);
		}

		return TResult.sucess1(deskIns);
	}

	@Override
	public CommonDesk genDesk(String deskId,int wanfa) {
		DeskConfig deskConf = this.getMatchConf().conditionInfo.deskConf;
		VipDesk desk = new VipDesk(this, this, deskConf, deskId, wanfa);
		UserRoom ur = ServiceRepo.userRoomDao.getByCode(deskId);
		if(ur == null) {
			return null;
		}
		
		desk.setDeskOwner(ur.getOwnerId());
		desk.setUserRoom(ur);
		return desk;
	}

	private void afterDeskCreate(CommonDesk desk) {
		if(desk.getClubId() > 0 )
			ServiceRepo.taskPortalService.pushClubRoomModelSyn(BattleConstants.SYN_TYPE_ADD,desk.getClubId(),desk.getClubRoomType(),desk.getDeskInfo());
	}

}
