package com.buding.battle.logic.module.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.desk.bo.CommonDesk;

@Component
public class GameDebugService  {	
	@Autowired
	BattleSessionManager battleSessionManager;
	
	@Autowired
	GameService gameService;
	
	
	public void printGameData(int playerId) {
		BattleSession session = battleSessionManager.getIoSession(playerId);
		if (session == null) {
			System.out.println("null session");
			return;
		}
		CommonDesk desk = session.getPlayingOrReadyDesk(playerId);
		if (desk == null) {
			System.out.println("null desk");
			return;
		}
//		System.out.println(desk.printGameDetail());
	}
}
