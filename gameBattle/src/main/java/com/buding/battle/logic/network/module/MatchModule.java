package com.buding.battle.logic.network.module;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;

import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.battle.logic.module.game.service.VipService;
import com.buding.battle.logic.module.user.service.LoginService;
import com.buding.battle.logic.network.BaseModule;

/**
 * 游戏开始前(未进入准备状态)的网络包在此处理
 * 
 * @author Administrator
 * 
 */
@Component
public class MatchModule extends BaseModule {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	GameService gameService;

	@Autowired
	PushService pushService;

	@Autowired
	LoginService loginService;
	
	@Autowired
	VipService vipService;

	@Override
	public void onChangeDeskMsgReceived(BattleSession session, PacketBase packet) {
		gameService.changeDesk(session);
	}
	
	@Override
	public void onQuickStartMsgReceived(BattleSession session, PacketBase packet) {
		gameService.quickStart(session);
	}
}
