package com.buding.battle.logic.module.desk.bo;

import java.util.List;

import com.buding.api.context.GameContext;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.hall.config.DeskConfig;

import static com.buding.api.game.PokerWanfa.*;

//单机场
public class SinglePlayerDesk extends MJDeskImpl {
	public SinglePlayerDesk(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
		super(listener, room, deskConf, deskId);
		wanfa = ZJH_DI_LONG;
	}

	@Override
	public boolean hasNextGame(GameContext context) {
		return true;
	}

	@Override
	public synchronized int playerSitPre(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		PlayerInfo player = session.player;
		player.score = 0; //积分清零
		return super.playerSitPre(ctx);
	}
	
	@Override
	public void handSettle(GameContext context) {
		//保存战斗数据
		dumpGameData(genVideoId());
		addPokerDDZGameLog(context);
	}

	@Override
	public synchronized void onGameOver() {
		try {
			logger.info("act=onGameOver;deskId={};", getDeskID());

			List<PlayerInfo> players = guard.getPlayerList();
			for (PlayerInfo player : players) {
				BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
				if (session != null) {
					session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.GAME_FINISH);
					session.currentModule = ServiceRepo.matchModule;
				}
				if(!isMultiMatch()) {
					PushService.instance.pushGameStopMsg(player.playerId, id);// 推送游戏结束消息
					ServiceRepo.clusterStubService.removeUserPlaying(session.userId, session.getGame().getId(), ServiceRepo.serverConfig.instanceId);
				}
			}

			this.status = DeskStatus.WATING;

			// ServiceRepo.configManager.getMatchConfig(this.getParent().getParent().getMatchConfig().gameID,
			// player);

			if (!this.isAutoChangeDesk()) {
				guard.ready4NextGame();
				this.reset();
			}

			if (listener != null) {
				listener.onDeskGameFinish(this, game);
			}

		} catch (Exception e) {
			logger.error("act=onGameFinishError;deskId=" + getDeskID(), e);
		}
	}
}
