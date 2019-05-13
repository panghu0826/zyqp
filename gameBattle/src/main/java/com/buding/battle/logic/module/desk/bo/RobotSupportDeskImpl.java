package com.buding.battle.logic.module.desk.bo;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.robot.Robot;
import com.buding.battle.logic.module.robot.RobotContext;
import com.buding.battle.logic.module.robot.RobotManager;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.hall.config.DeskConfig;

import java.util.Collection;

public class RobotSupportDeskImpl extends DeskImpl {
	long nextAddRobotTime = 0;
	int currentAddRobotInterval = -1;

	public RobotSupportDeskImpl(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
		super(listener, room, deskConf, deskId);
	}

	@Override
	protected void checkOtherEvent() throws Exception {
		if (this.getStatus() != DeskStatus.WATING) {
			return;
		}

		// 有真实玩家，继续加机器人,没有真实玩家则回收机器人
		if (getDeskConfig().supportRobot) {
			processRobot();
		}
	}

	private void processRobot() throws Exception {
		if (isHasReallyPlayer()) {
			addRobot();
			robotReady();
		} else {
			tryRecycleResource();
		}
	}

	public boolean isHasReallyPlayer() {
		for (PlayerInfo player : getPlayers()) {
			if (player.robot != 1) {
				return true;
			}
		}
		return false;
	}

	public void tryRecycleResource() {
		for (PlayerInfo player : getPlayers()) {
			if (player.robot == 1) {
				playerExit(player.playerId, PlayerExitType.ROBOT_RECYCLE);
			}
		}
	}

	@Override
	public synchronized void playerTryExit(int playerId, PlayerExitType reason) {
		// 如果其它的都是机器人，则终止游戏
		boolean allRobot = true;
		for (PlayerInfo p : guard.getPlayerList()) {
			if (p.playerId != playerId && p.robot == 0) {
				allRobot = false;
			}
		}
		if (allRobot && deskConf.allowExitWhenGaming && !isMultiMatch()) {
//			logger.info("全部为机器人===allRobot========销毁桌子==destroy desk==========");
//			this.destroy(DeskDestoryReason.NO_ACTIVIE_PLAYER);
//			return;
		}
		
		super.playerTryExit(playerId, reason);
	}

	@Override
	protected void afterPlayerVote() {
		for (PlayerInfo p : guard.getPlayerList()) {
			if(p.robot == 1) {
				voteMap.put(p.playerId, true);
			}
		}
		super.afterPlayerVote();
	}

	private void robotReady() {
		Collection<PlayerInfo> players = getPlayers();
		for (PlayerInfo player : players) {
			if (player.robot != 1) {
				continue;
			}
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if (session.getStatus() == PlayerStatus.UNREADY) {
				this.onPlayerReadyPacketReceived(player.playerId);
			}
		}
	}

	private void addRobot() throws Exception {
		if (nextAddRobotTime == 0) {
			return;
		}

		if (this.isFull()) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now < nextAddRobotTime) {
			return;
		}

		if (!this.getDeskConfig().supportRobot) {
			return;
		}

		RobotManager robotManager = ServiceRepo.robotManager;
		RobotContext ctx = new RobotContext();
		ctx.matchConfig = this.getParent().getParent().getMatchConfig();
		ctx.roomConfig = this.getParent().getRoomConfig();

		Robot robot = robotManager.borrowRobot(ctx);
		if (robot == null) {
			errCount++;
			logger.error("act=addRobot;error=borrownull;deskId={};", getDeskID());
			return;
		}

		BattleSession session = ServiceRepo.sessionManager.getIoSession(robot.playerId);
		BattleContext t = BattleContext.create(session).setGameId(ctx.matchConfig.gameID).setMatchId(ctx.matchConfig.matchID).setRoomId(ctx.roomConfig.roomId).setDeskId(this.id);
		int ind = this.playerSitPre(t);
		if (ind > -1) {
			session.enterRoom(this.getParent());
			BattleContext gameCtx = BattleContext.create(session).setMatchId(ctx.matchConfig.matchID).setRoomId(this.getParent().getRoomId()).setDeskId(this.getDeskID());
			session.getRoom().playerTryEnter(gameCtx);
		}
		setNextAddRobotTime(true);
	}

	public synchronized void playerReady(int playerId) {
		super.playerReady(playerId);

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			return;
		}

		PlayerInfo player = session.player;
		if (player == null || player.robot == 1) {
			return;
		}

		setNextAddRobotTime(false);
	}

	private void setNextAddRobotTime(boolean force) {
		if (nextAddRobotTime != 0 && !force) {
			return;
		}

		if (nextAddRobotTime == 0) {
			currentAddRobotInterval = this.getDeskConfig().secondsAddFirstRobot * 1000;
			nextAddRobotTime = System.currentTimeMillis() + currentAddRobotInterval;
		} else {
			currentAddRobotInterval = (int) (currentAddRobotInterval * this.getDeskConfig().addRobotRate);
			nextAddRobotTime = System.currentTimeMillis() + currentAddRobotInterval;
		}
	}

	@Override
	public void onPlayerAfterExit(PlayerInfo player) {
		if (player.robot == 1) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if (session != null) {
				session.leaveMatch();
				session.setStatus(PlayerStatus.IN_HALL, StatusChangeReason.LEAVE);
			}

			RobotManager robotManager = ServiceRepo.robotManager;
			Robot robot = (Robot) player;
			robotManager.returnRobot(robot);
		}
	}

	@Override
	public void markDeskAsWaitingGame() {
		super.markDeskAsWaitingGame();
		nextAddRobotTime = 0;
	}

	
	
}
