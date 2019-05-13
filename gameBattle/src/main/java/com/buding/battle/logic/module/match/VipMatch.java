package com.buding.battle.logic.module.match;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.EnrollResult;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.result.Result;
import com.buding.db.model.UserRoom;

public class VipMatch extends MatchImpl {
	
	public VipMatch(Game parent) {
		super(parent);
	}

	@Override
	public synchronized EnrollResult enroll(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		if(session == null) {
			logger.info("act=vipEnroll;error=sessionMiss;playerId={}", ctx.playerId);
			return EnrollResult.fail("会话超时,请重新登录");			
		}
		
		String deskId = ctx.getDeskId();
		if(deskId == null) {
			return EnrollResult.fail("缺少参数房间编号");
		}
		
		UserRoom r = ServiceRepo.userRoomDao.getByCode(deskId);
		if(r == null) {
			return EnrollResult.fail("vip房间不存在");
		}
		
		ctx.params.put("vipDesk", r);
		
		Room room = this.roomMap.get(r.getRoomConfId());
		if(room == null) {
			return EnrollResult.fail("房间实例不存在");
		}
		
		Result ret = room.playerEnroll(ctx);
		if(ret.isOk()) {
			return EnrollResult.success(ctx.matchId,room.getRoomId());
		}
		return EnrollResult.fail(ret.msg == null? "进入房间失败" : ret.msg);
	}
	
	
}
