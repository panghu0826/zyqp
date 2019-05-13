package com.buding.battle.logic.util;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.db.model.User;

public class Util {
	
	public static void userHasLogin(BattleSession session) {
//		int sessionId = session.getSessionId();
		ServiceRepo.sessionManager.put2AnonymousList(session);
		ServiceRepo.sessionManager.put2OnlineList(session.userId, session);
	}
	
	public static void initSession(BattleSession session, PlayerInfo player, User user) {
		session.setPlayerId(user.getId());
		
//		session.currentModule = hallModule;//进入大厅
		
		session.player = player;
		session.user = user;
	}
}
