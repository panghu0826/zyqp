package com.buding.battle.logic.module.desk.bo;

import com.buding.api.context.*;
import com.buding.api.desk.LogLevel;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.event.*;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.contants.BattleConstants;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.DeskGuard;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.battle.logic.util.IDUtil;
import com.buding.common.monitor.Monitorable;
import com.buding.common.result.Result;
import com.buding.common.schedule.Job;
import com.buding.common.schedule.WorkerPool;
import com.buding.common.util.IOUtil;
import com.buding.db.model.ClubUser;
import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.game.MsgGame;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DeskImpl extends BaseParent<Room> implements Monitorable, CommonDesk<byte[]> {
	public Logger logger = LogManager.getLogger("DESKLOG");
	protected int ownerId = -1;
	protected int starterId = -1;

	protected transient DeskConfig deskConf;
	protected String id;
	public transient Game game;
	protected DeskStatus status = DeskStatus.WATING;

	protected DeskGuard guard;

	protected transient DeskListener listener;
	protected transient Room room;

	protected AtomicInteger timerId = new AtomicInteger();

	public BlockingQueue<DeskEvent> otherEventQueue = new LinkedBlockingQueue<DeskEvent>();
	public BlockingQueue<TimerEvent> timerEventQueue = new LinkedBlockingQueue<TimerEvent>();

	protected DeskTimer timer;

	private long workTime = 0;
	private long sleepTime = 0;
	private long lastSetTimer = -1;
	private int invokeTimerCount;
	private int delayTimerCount;
	private long lastCycleTime;
	protected long errCount = 0; // 发生错误次数，如果大于x, 则解散桌子
	private String replayData;
	private volatile long playerActiveTime;
	private String matchId;
	private String roomId;
	protected int gameCount = 0;
	protected long waitingGameStartTime = System.currentTimeMillis(); // 开始等待游戏时间
	private long waitingGameStopTime = System.currentTimeMillis(); // 开始等待游戏时间
	private boolean adminDesk = false;// 管理桌,调试用
	public long createTime = System.currentTimeMillis();
	protected Map<Integer, Boolean> voteMap = new HashMap<Integer, Boolean>();
	private long lastVoteTime = 0;
	private int applyDissMissPosition = -1;
	private long pauseTime = System.currentTimeMillis();
	private boolean isStart = false;

	class DeskTimer extends Job {

		@Override
		public void run() {
			logger.info("act=startDeskLooper;deskId={}", getDeskID());
			while (true) {
				long startMills = System.currentTimeMillis();

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop1;deskId={};", getDeskID());
					break;
				}

				try {
					checkTimeEvent(); // 检查定时器事件
				} catch (Throwable e) {
					logger.error("checkTimeEventError;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop2;deskId={};", getDeskID());
					break;
				}

				try {
					if (lastSetTimer != -1 && System.currentTimeMillis() - lastSetTimer >= 180000) {// 3分钟
						// logger.error("桌子id--"+this.getDeskID()+"--"+"act=exitDeadThread;deskId=" +
						// DeskImpl.this.getDeskID());
						// DeskImpl.this.destroy();
					}
				} catch (Throwable e) {
					logger.error("TryKillTimeError;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop3;deskId={};", getDeskID());
					break;
				}

				try {
					checkNetEvent();// 检查网络事件
				} catch (Throwable e) {
					logger.error("checkNetEventError;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop4;deskId={};", getDeskID());
					break;
				}

				try {
					checkPlayerEvent(); // 检测玩家状态
				} catch (Throwable e) {
					logger.error("checkPlayerEventError;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop5;deskId={};", getDeskID());
					break;
				}

				try {
					checkDeskStatus(); // 检测桌子状态
				} catch (Throwable e) {
					logger.error("checkDeskStatus;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop51;deskId={};", getDeskID());
					break;
				}

				try {
					tryStartGame(); // 尝试启动游戏
				} catch (Throwable e) {
					logger.error("tryStartGameError;deskId=" + getDeskID(), e);
				}

				if (stop) {
					logger.info("act=skipDeskLooperAsMarkStop6;deskId={};", getDeskID());
					break;
				}

				try {
					checkOtherEvent(); // 检查其它状态
				} catch (Throwable e) {
					logger.error("checkOtherEventError;deskId=" + getDeskID(), e);
				}

				long endMills = System.currentTimeMillis();

				workTime += (endMills - startMills);

				lastCycleTime = startMills;

				try {
					Thread.sleep(50);// 睡眠50毫秒
				} catch (Throwable e) {
					logger.error("", e);
				}
				sleepTime += 50;

				//如果太慢，打警告
//				if(endMills - startMills > 2000) {
//					logger.warn("act=deskLoop;error=slowLoop;deskId={}", id);
//				}
			}
			logger.info("act=endDeskLooper;deskId={};", getDeskID());
		}
	}

	protected void checkOtherEvent() throws Exception {

	}

	private synchronized void checkDeskStatus() {
		if (findVoteOverTimeAndKill()) {
			return;
		}
		if (isEmpty() && findEmptyDeskAndKill()) {
			return;
		}

		if (findDeadGameAndKill()) {
			return;
		}

		if (findFailStartAndKill()) {
			return;
		}

		if (findDeskPauseTimeout()) {
			return;
		}
	}

	private boolean findVoteOverTimeAndKill() {
		if (System.currentTimeMillis() - lastVoteTime >= deskConf.voteTimeLimit * 1000 && lastVoteTime > 0) {
			logger.info(this.id+"---房间解散申请5分钟没操作解散"+"当前时间"+ DateFormatUtils.format(new Date(),"hhMMss")+"计时时间"+lastVoteTime);
			try {
				try {
					this.game.gameDismiss();
				}catch (Exception e){
					this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}catch (Exception e){
				this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
			}
			return true;
		}
		return false;
	}

	private synchronized void checkPlayerEvent() throws Exception {
		List<Integer> set = guard.getplayerIdList();
		for (int playerId : set) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
			if (session == null) {
				//TODO 以后再优化吧,这时候做个处理虚拟个玩家session
				logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerNotInDesk;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
				session = new BattleSession();
				User user = ServiceRepo.hallPortalService.getUser(playerId);
				session.user = user;
				session.player = guard.getPlayerById(playerId);
				session.userId = playerId;
				ServiceRepo.sessionManager.put2AnonymousList(session);
				ServiceRepo.sessionManager.put2OnlineList(playerId,session);
				session.onlineStatus=OnlineStatus.OFFLINE;
				session.enterMatch(this.getParent().getParent());
			}
		}

		for (int playerId : set) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
			if (session==null || session.onlineStatus == OnlineStatus.ONLINE) {
				playerActiveTime = System.currentTimeMillis();
				return;
			}
		}
	}

	private boolean findDeadGameAndKill() {
		if (status == DeskStatus.GAMING && System.currentTimeMillis() - waitingGameStopTime >= deskConf.secondsWaitingGameStop * 1000) {
			logger.info(this.id+"---房间2小时没结束解散"+"当前时间"+ DateFormatUtils.format(new Date(),"hhMMss")+"计时时间"+waitingGameStopTime);
			if(this instanceof VipDesk && gameCount > 0){
				try {
					this.game.gameDismiss();
				}catch (Exception e){
					this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}else{
				this.destroy(DeskDestoryReason.GAME_DEAD);
			}
			return true;
		}
		return false;
	}

	private boolean findDeskPauseTimeout() {
	    if(this.getClubId() > 0 )return false;
		if (System.currentTimeMillis() - pauseTime >= deskConf.gamePauseTimeout * 1000) {
			logger.info(this.id+"---房间暂停15分钟解散,计时时间---"+DateFormatUtils.format(pauseTime,"yyyy-MM-dd HH:mm:ss"));
			if(this instanceof VipDesk && gameCount > 0) {
				try {
					this.game.gameDismiss();
				}catch (Exception e){
					this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}else {
				this.destroy(DeskDestoryReason.CONTINUE_GAME_FAIL);
			}
			return true;
		}
		return false;
	}

	private boolean findFailStartAndKill() {
		if(this.getClubId() <= 0 ) return false;
		if (status == DeskStatus.WATING && System.currentTimeMillis() - waitingGameStartTime >= deskConf.secondsWaitingGameStart * 1000) {
			logger.info(this.id+"---房间10分钟没开始解散"+"当前时间,计时时间"+DateFormatUtils.format(waitingGameStartTime,"yyyy-MM-dd HH:mm:ss"));
			if(this instanceof VipDesk && gameCount > 0) {
				try {
					this.game.gameDismiss();
				}catch (Exception e){
					this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}else{
				this.destroy(DeskDestoryReason.START_GAME_FAIL);
			}
			return true;
		}
		return false;
	}

	private boolean findErrorDeskAndKill() {
		if (errCount > deskConf.errCount4KillDesk) {
			this.destroy(DeskDestoryReason.ERROR);
			return true;
		}
		return false;
	}

	private boolean findEmptyDeskAndKill() {
		if ((System.currentTimeMillis() - playerActiveTime) > deskConf.emptyDeskTTL * 1000) {
			logger.info(this.id+"---房间没人了解散"+"当前时间"+ DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss")+"计时时间"+
                    DateFormatUtils.format(playerActiveTime,"yyyy-MM-dd HH:mm:ss"));
			this.destroy(DeskDestoryReason.EMPTY_DESK);
			return true;
		}
		return false;
	}

	private boolean destroyDeskAsNoActivePlayer() {
		if (System.currentTimeMillis() - playerActiveTime > deskConf.secondsWaitActivePlayer * 1000) {
			this.destroy(DeskDestoryReason.NO_ACTIVIE_PLAYER);
			return true;
		}
		return false;
	}

//	private boolean isTime4Kickout(BattleSession session, long sitdowntime) {
//		return deskConf.secondsBeforKickout > 0 && System.currentTimeMillis() - sitdowntime > deskConf.secondsBeforKickout * 1000;
//	}

	private void checkNetEvent() throws Exception {
		DeskEvent event = otherEventQueue.poll();
		if (event == null) {
			return;
		}

		switch (event.key) {
		case GAME_MSG: {
			GameMsgEvent e = (GameMsgEvent) event;
			gameMsgRecieve(e.playerId, e.content);
		}
			break;
		case PLAYER_READY: {
			PlayerReadyEvent e = (PlayerReadyEvent) event;
			playerReady(e.playerId);
		}
			break;
		case PlaySit: {
			PlaySitEvent e = (PlaySitEvent) event;
			playerSit(e.playerId,e.deskPos);
		}
			break;
		case PlayerExitPosNotExitRoom: {
			PlayerExitPosNotExitRoomEvent e = (PlayerExitPosNotExitRoomEvent) event;
			playerExitPosNotExitRoom(e.playerId,e.deskPos);
		}
			break;
		case CHANGE_DESK: {
			ChangeDeskEvent e = (ChangeDeskEvent) event;
			changeDesk(e);
		}
			break;
		case PLAYER_OFFLINE: {
			PlayerOfflineEvent e = (PlayerOfflineEvent) event;
			playerOffline(e.playerId);
			break;
		}
		case PLAYER_RECONNECT: {
			PlayReconnectEvent e = (PlayReconnectEvent) event;
			playerReconnect(e.playerId);
			break;
		}
		case PLAYER_EXIT: {
			PlayExitEvent e = (PlayExitEvent) event;
			playerTryExit(e.playerId, PlayerExitType.REQUEST_EXIT);
			break;
		}
		case PLAYER_AWAY: {
			PlayAwayEvent e = (PlayAwayEvent) event;
			playerTryAway(e.playerId);
			break;
		}
		case PLAYER_COMBACK: {
			PlayComBackEvent e = (PlayComBackEvent) event;
			playerComeBack(e.playerId);
			break;
		}
		case KICKOUT_PLAYER: {
			KickoutEvent e = (KickoutEvent) event;
			requestKickout(e.playerId, e.targetPlayerId);
			break;
		}
		case PLAYER_HANGUP: {
			PlayerHangupEvent e = (PlayerHangupEvent) event;
			playerHangeup(e.playerId);
			break;
		}
		case PLAYER_CANCELUP: {
			PlayerCancelHangupEvent e = (PlayerCancelHangupEvent) event;
			playerCancelHangup(e.playerId);
			break;
		}
		case DISMISS: {
			dissmiss();
			break;
		}
		case VOTE_DISSMISS: {
			PlayerVoteDissmissEvent e = (PlayerVoteDissmissEvent) event;
			playerVoteDissmiss(e.playerId, e.agree);
			break;
		}
		case START_GAME: {
            StartGameEvent e = (StartGameEvent) event;
			startGame(e.playerId);
			break;
		}
		case ViewGuanZhanReq: {
			ViewGuanZhanEvent e = (ViewGuanZhanEvent) event;
			ViewGuanZhan(e.playerId);
			break;
		}
		default:
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=checkNetEvent;error=keyMismatch;key={};", event.key);
			break;
		}
	}

	private void ViewGuanZhan(int playerId) {
		List<PlayerInfo> list = guard.getNotSitPlayer();
		PushService.instance.pushGuanZhanPlayers(playerId,this.getDeskID(),list);
	}

	@Override
	public void onPlayerSitMsgReceived(int userId, int deskPos) {
		PlaySitEvent event = new PlaySitEvent();
		event.key = DeskEventKey.PlaySit;
		event.playerId = userId;
		event.deskPos = deskPos;
		otherEventQueue.add(event);
	}

	@Override
	public int getTotalQuan() {
		return -1;
	}

	@Override
	public int getWanfa() {
		return -1;
	}

    @Override
    public DeskGuard getGuard() {
        return guard;
    }

    @Override
    public long getClubId() {
        return -1;
    }

    @Override
    public int getClubRoomType() {
        return -1;
    }

    public int getEnterScore(){
        return -1;
    }
    public int getCanFufen(){
        return -1;
    }
    public int getChoushuiScore(){
        return -1;
    }
    public int getChoushuiNum(){
        return -1;
    }
    public int getZengsongNum(){
        return -1;
    }
    public int getQiangZhuangNum(){
        return -1;
    }

    public int getErBaGameType() {
        return -1;
    }

    public Map<Integer, Integer> getNiuFanStr() {
        return new HashMap<>();
    }
    @Override
    public boolean isStart() {
        return isStart;
    }

    public boolean isDDZ() {
        return StringUtils.equals("G_DDZ",this.getParent().getParent().getParent().getId());
    }
    public boolean is28() {
        return StringUtils.equals("G_ErBa",this.getParent().getParent().getParent().getId());
    }
    public boolean isZJH() {
        return StringUtils.equals("G_ZJH",this.getParent().getParent().getParent().getId());
    }
    public boolean isJACK() {
        return StringUtils.equals("G_JACK",this.getParent().getParent().getParent().getId());
    }

    @Override
    public void playerExitPosNotExitRoom(int playerId, int deskPos) {

	    //斗地主游戏中不能离座
		if((isStart || getStatus() == DeskStatus.GAMING || getStatus() == DeskStatus.GAMING_PAUSE )
				&& isDDZ()) return;

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);

		//28中途能退出
		if(session != null && session.player.startGameCount > 0 && !is28()) return;
		if (session != null && is28() && getErBaXiaZhuOrBankerPlayerIds().contains(session.userId)) return;
		if(guard.hasThisPos(deskPos)){
            PlayerInfo player = guard.playerExitPosNotRoom(playerId,"playerExitPosNotExitRoom");
			// 向其它人推送离座人的信息
            for (PlayerInfo p : guard.getPlayerAllList()) {
                PushService.instance.pushPlayerExitPosNotExitRoomMsg(id,deskPos,playerId, p.playerId);
				PushService.instance.pushVipRoomList(p.playerId);
			}
            if(session != null && session.getPlayingOrReadyDesk(playerId) == null) player.position = -1;

			game.playerExitPosNotExitRoom(player);
            if(this.getClubId() > 0) ServiceRepo.taskPortalService.pushClubRoomModelSyn(BattleConstants.SYN_TYPE_MODIFY,this.getClubId(),this.getClubRoomType(),this.getDeskInfo());

		}

    }

	public synchronized void playerSit(int playerId, int deskPos) {

		if (isStart() && isDDZ()) return;
        if(this.getClubId() > 0){
            ClubUser clubUser =  ServiceRepo.clubDao.selectClubUser(this.getClubId(),playerId);
            if(this.getClubRoomType() == Constants.CLUB_JI_FEN_DESK){//积分场
                if(this.getEnterScore() > clubUser.getClubMemberScore()){
                    logger.info("act=playerTrySit;error=ClubScoreNotFuHe;playerId={};deskId={};", playerId, this.getDeskID());
                    PushService.instance.pushErrorMsg(playerId, PacketType.GlobalMsgSyn, "积分不够不能凑热闹");
                    return ;
                }
                if(this.getCanFufen() == Constants.CLUB_CAN_NOT_FU_FEN && clubUser.getClubMemberScore() <= 0){
                    logger.info("act=playerTrySit;error=ClubScoreNotFuHe2;playerId={};deskId={};", playerId, this.getDeskID());
                    PushService.instance.pushErrorMsg(playerId, PacketType.GlobalMsgSyn, "积分不够不能凑热闹啊");
                    return ;
                }
            }
        }

		if(this.isStart() && this.canZhongTuNotEnter()){
			logger.info("act=playerTrySit;error=ZHONGTUJINRU;playerId={};deskId={};", playerId,this.getDeskID());
			PushService.instance.pushErrorMsg(playerId, PacketType.GlobalMsgSyn, "桌子设置了中途禁入只能观战");
			return ;
		}
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);

		BattleContext ctx = BattleContext.create(session).setGameId(this.getParent().getParent().getParent().getId()).setMatchId(this.getParent().getParent().getId()).setRoomId(roomId).setDeskId(id);

		PlayerInfo player = session.player;

		int deskPosNew = guard.mergeSitPos(player,deskPos);

		//已经坐下
		if(deskPosNew  == -2){
			return;
		}
		//没有位置让他做了,直接踢出去,人满开始
		if(deskPosNew  == -1){
			guard.playerForceExit(player);
			PushService.instance.pushKickoutSyn(session, playerId, "人满开始了");
			PushService.instance.pushVipRoomPlayerMsg(this,session);
			return;
		}


		session.player.position = deskPosNew;
		session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.SIT);
		session.awayStatus = AwayStatus.BACK;
		guard.playerSit(player, deskPosNew);

		if (listener != null) {
			listener.onPlayerSit(this, player);
		}
		game.playerSit(player);
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=playerSit;position={};deskId={};roomId={};matchId={};", ctx.playerId, deskPosNew, this.getDeskID(), this.getParent().getRoomId(), this.getParent().getParent().getId());

		// 推送正在游戏的消息
//		pushGamingInfo(ctx);
        onPlayerBeforeSit(player);
		// 推送桌子信息
		PushService.instance.pushDeskInfo(session.userId, this.id, this.getPlayerCount(), null);

		{
			// 向所有人推送入场者的信息
			for (PlayerInfo p : guard.getPlayerAllList()) {
                PushService.instance.pushPlayerSitSyn(player.playerId, p.playerId,id);
				PushService.instance.pushVipRoomList(p.playerId);
			}


//			// 向入场者推送所有人的信息
//			for (PlayerInfo p : guard.getPlayerAllList()) {
//				if (p != player) {
//					PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId);
//				}
//			}

			// 向入场者推送已准备的玩家消息
			for (PlayerInfo p : guard.getPlayerList()) {
				BattleSession s = ServiceRepo.sessionManager.getIoSession(p.playerId);
				if (s.getStatus() == PlayerStatus.READY) {
					PushService.instance.pushReadySyn(ctx.playerId, p.position, p.playerId);
				}
			}
		}

		playerReady(playerId);
        guard.log();
	}

	private void dissmiss() {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=dismiss;deskId={};", this.getDeskID());
		destroy(DeskDestoryReason.REQUEST_DIMISS);
	}

	protected void changeDesk(ChangeDeskEvent e) throws Exception {
		logger.error("桌子id--"+this.getDeskID()+"--"+"act=changeDesk;playerId={};deskId={};", e.playerId, getDeskID());

		BattleSession session = ServiceRepo.sessionManager.getIoSession(e.playerId);
		if (session == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=changeDeskError;error=sessionMiss;playerId={};deskId={};", e.playerId, getDeskID());
			return;
		}

		PlayerInfo player = guard.getPlayerById(e.playerId);
		if (player == null) {// 并发情况下，会出现这种情况
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=changeDeskError;error=playerMiss;playerId={};deskId={};", e.playerId, id);
		}

		long t = guard.getSitdownTime(e.playerId);

		if (System.currentTimeMillis() - t <= 2000) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=changeDeskError;error=frequent;playerId={};deskId={};", e.playerId, id);
			PushService.instance.pushChangeDeskRsp(session.userId, false, "换桌过于频繁");
			return;
		}

		room.changeDesk(e.playerId);
	}

	private void checkTimeEvent() throws Exception {
		TimerEvent waitingEvent = null;

		while (true) {
			TimerEvent tmpEvent = timerEventQueue.peek();

			if (tmpEvent == null) { // 队列为空
				return;
			}

			if (tmpEvent == waitingEvent) {// 队列检查已经循环了一次
				return;
			}

			tmpEvent = timerEventQueue.poll();

			tmpEvent = tryTriggerTimer(tmpEvent);
			if (tmpEvent != null) {
				timerEventQueue.put(tmpEvent);
				if (waitingEvent == null) {
					waitingEvent = tmpEvent;
				}
			}
		}
	}

	private TimerEvent tryTriggerTimer(TimerEvent event) {
		if (event.killed) {
			return null;
		}

		long mills = event.triggerTime;
		long now = System.currentTimeMillis();
		if (mills > now) {
			return event; // 时间未到
		}
		invokeTimerCount++;
		if (now - mills >= deskConf.timerDelayThreadShold) {
			delayTimerCount++;
		}
		game.onTimer(event.timerId);
		return null; // 定时器已经执行
	}

	public DeskImpl(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
		super(room);
		this.id = deskId;
		if (this.id == null) {
			this.id = IDUtil.instance.genIntId("Desk");
		}
		this.listener = listener;
		this.room = room;
		this.playerActiveTime = System.currentTimeMillis();
		this.matchId = room.getParent().getId();
		this.roomId = room.getRoomId();

		logger.info("桌子id--"+this.getDeskID()+"--"+"act=deskInit;deskId={};", id);

		try {
			guard = new DeskGuard(deskConf);
			this.deskConf = deskConf;
			Class<?> cls = getClass().getClassLoader().loadClass(deskConf.gameClassFullName);
			game = (Game) cls.newInstance();
			game.setDesk(this, deskConf.gameParam);
			timer = new DeskTimer();
			WorkerPool.instances.submitJob(timer);

			if (this.listener != null) {
				listener.onDeskCreate(this);
			}

			markDeskAsWaitingGame();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onPlayerHangupPacketReceived(int playerID) {
		PlayerHangupEvent event = new PlayerHangupEvent();
		event.key = DeskEventKey.PLAYER_HANGUP;
		event.playerId = playerID;
		otherEventQueue.add(event);
	}
	@Override
	public void onStartGameMsgReceived(int playerId) {
		StartGameEvent event = new StartGameEvent();
		event.key = DeskEventKey.START_GAME;
		event.playerId = playerId;
		otherEventQueue.add(event);
	}

	private void playerHangeup(int playerID) {
		PlayerInfo p = this.guard.getPlayerById(playerID);
		this.game.hangeUp(p);
	}

	private synchronized void playerVoteDissmiss(int playerId, boolean agree) {
	    List<Integer> canDissmissPlayers = new ArrayList<>();
	    if(this.getClubId() > 0){
	        List<ClubUser> managerlist = ServiceRepo.clubDao.selectClubAllManageUser(this.getClubId());
	        for(ClubUser user : managerlist){
                canDissmissPlayers.add(user.getClubMemberId());
            }
        } else {
			canDissmissPlayers.add(ownerId);
		}
		if(gameCount == 0 && !canDissmissPlayers.contains(playerId)) return;
		if((getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG || gameCount==0) && canDissmissPlayers.contains(playerId) && agree){
			logger.info("桌子id--"+this.getDeskID()+"--"+"桌子id--"+this.getDeskID()+"--"+"玩家"+playerId+"解散房间");
            try {
                try {
                    this.game.gameDismiss();
                }catch (Exception e){
                    this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
                }
            }catch (Exception R){
                this.destroy(DeskDestoryReason.REQUEST_DIMISS);
            }
			return;
		}
        PlayerInfo votePlayer = this.guard.getPlayerById(playerId);
		if(votePlayer == null || votePlayer.position < 0) return;
		if(voteMap.size() == 0){
		    logger.info("桌子id--"+this.getDeskID()+"--"+"---第一个人"+playerId+"申请解散----");
			lastVoteTime = System.currentTimeMillis();
            applyDissMissPosition = votePlayer.position;
		}

        voteMap.put(playerId, agree);
        afterPlayerVote();
//        logger.info("---申请列表voteMap----"+voteMap);

        sendDissmissVoteMsg();

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if (gameCount>0 && isVotingDismissAndPass()) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"玩家解散房间");
			try {
				try {
					this.game.gameDismiss();
				}catch (Exception e){
					this.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}catch (Exception R){
				this.destroy(DeskDestoryReason.REQUEST_DIMISS);
			}
		}

        // 如果全部玩家回复了或者有人拒绝了，重置计时器
        if (!agree ) {
            lastVoteTime = 0;
            voteMap.clear();
            applyDissMissPosition = -1;
        }

	}

	@Override
    public void sendDissmissVoteMsg() {
	    if(voteMap.size() > 0) {
            int timeLeft = (int) ((deskConf.voteTimeLimit * 1000 - (System.currentTimeMillis() - lastVoteTime)));
            List<MsgGame.DissmissStatus> pbList = new ArrayList<>();
            for (Map.Entry<Integer, Boolean> entry : voteMap.entrySet()) {
                MsgGame.DissmissStatus.Builder pb = MsgGame.DissmissStatus.newBuilder();
                PlayerInfo pl = this.guard.getPlayerById(entry.getKey());
                pb.setAgree(entry.getValue());
                pb.setPosition(pl.position);
                pbList.add(pb.build());
            }

            for (PlayerInfo p : guard.getPlayerList()) {
                PushService.instance.pushDismissVote(timeLeft, p.playerId, pbList, applyDissMissPosition);
            }
        }
    }

    protected void afterPlayerVote() {
		
	}

	private boolean isVotingDismissAndPass() {
		int i = 0;
		for (boolean e : voteMap.values()) {
			if (e)
				i++;
		}
		// 多半数同意即解散桌子
//		if (i * 2 > guard.getPlayerCount()) {
//			return true;
//		}
		if (i  >= guard.getPlayerCount()) {
			return true;
		}
		return false;
	}

	@Override
	public void onPlayerHangup(int position) {
		PlayerInfo p = this.guard.getPlayerByPos(position);
		if (p == null) {
			return;
		}
		PushService.instance.pushHangupSyn(p.playerId, p.position, Constants.PLAYER_HANGUP);
	}

	@Override
	public void onPlayerCancelHangupPacketReceived(int playerID) {
		PlayerCancelHangupEvent event = new PlayerCancelHangupEvent();
		event.key = DeskEventKey.PLAYER_CANCELUP;
		event.playerId = playerID;
		otherEventQueue.add(event);
	}

	@Override
	public void onPlayerDissVotePacketReceived(int playerId, boolean agree) {
		PlayerVoteDissmissEvent event = new PlayerVoteDissmissEvent();
		event.playerId = playerId;
		event.agree = agree;
		event.key = DeskEventKey.VOTE_DISSMISS;
		otherEventQueue.add(event);
	}

	private void playerCancelHangup(int playerID) {
		PlayerInfo p = this.guard.getPlayerById(playerID);
		this.game.cancelHangeUp(p);
	}

	@Override
	public void onPlayerCancelHangup(int position) {
		PlayerInfo p = this.guard.getPlayerByPos(position);
		if (p == null) {
			return;
		}
		PushService.instance.pushHangupSyn(p.playerId, p.position, Constants.PLAYER_UNHANGUP);
	}

	@Override
	public byte getShangGunBaoCard(byte baocard) {
		if(baocard==69) return baocard;
		if(baocard==9||baocard==25||baocard==41) return (byte) (baocard-8);
		return (byte) (baocard+1);
	}

	@Override
	public byte getXiaGunBaoCard(byte baocard) {
		if(baocard==69) return baocard;
		if(baocard==1||baocard==17||baocard==33) return (byte) (baocard+8);
		return (byte) (baocard-1);
	}

	@Override
	public List<Byte> getTongBaoCard(byte baocard) {
		List<Byte> baoList = new ArrayList<>();
		if(baocard==69){
			baoList.add(baocard);
		} else if(baocard<10){
			baoList.add((byte)(baocard+16));
			baoList.add((byte)(baocard+32));
		} else if(baocard<26){
			baoList.add((byte)(baocard-16));
			baoList.add((byte)(baocard+16));
		} else if(baocard<42){
			baoList.add((byte)(baocard-16));
			baoList.add((byte)(baocard-32));
		}
		return baoList;
	}

	@Override
	public boolean isMultiMatch() {
		return this.getParent().getParent() instanceof MultiMatchImpl;
	}

	@Override
	public void updatePlayerScoreAndRank(Map<Integer,Integer> multiMatchScoreMap) {
		Match match = this.getParent().getParent();
		MultiMatchImpl multiMatch = (MultiMatchImpl)match;
		multiMatch.updatePlayerScoreAndRank(multiMatchScoreMap);
	}

	@Override
	public void sendMultiMatchRank() {
		if(!isMultiMatch()) return;
		Match match = this.getParent().getParent();
		MultiMatchImpl multiMatch = (MultiMatchImpl)match;
		multiMatch.sendMultiMatchRank();
	}

	@Override
	public void subServiceFee(PlayerInfo pl) {

	}

	@Override
	public void setPauseTime(long l) {
		this.pauseTime = l;
	}

	@Override
	public void addUserConsumeDiamondLog(int playerNum) {

	}

	@Override
	public boolean isAllBiSaiDeskFinishOneLun() {
		Match match = this.getParent().getParent();
		if(!(match instanceof MultiMatchImpl)) return false;
		MultiMatchImpl multiMatch = (MultiMatchImpl)match;
		return multiMatch.isAllBiSaiDeskFinishOneLun();
	}

	@Override
	public void multiMatchStartNotify() {
		Match match = this.getParent().getParent();
		if(!(match instanceof MultiMatchImpl)) return;
		MultiMatchImpl multiMatch = (MultiMatchImpl)match;
		multiMatch.multiMatchStartNotify();
	}

	@Override
	public int getLunNum() {
		if(isMultiMatch()){
			MultiMatchImpl multiMatch = (MultiMatchImpl)this.getParent().getParent();
			return multiMatch.getLunNum();
		}
		return 0;
	}

	@Override
	public PlayerInfo getDeskPlayer(int nDeskPos) {
		return guard.getPlayerByPos(nDeskPos);
	}
	@Override
	public PlayerInfo getDeskPlayerById(int id) {
		return guard.getPlayerById(id);
	}

	@Override
	public void sendMsg2Player(int position, byte[] content) {
		try {
			PlayerInfo player = guard.getPlayerByPos(position);
			if (player.robot == 1) {
				return;
			}
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if(session.awayStatus == AwayStatus.AWAY) {
				return;
			}
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setPacketType(PacketType.GameOperation);
			pb.setData(ByteString.copyFrom(content));
			ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
		} catch (Exception e) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=sendMsg2Player;error=exception;", e);
		}
	}

	@Override
	public void sendMsg2Player(PlayerInfo player, byte[] content) {
		try {
			if (player.robot == 1) {
				return;
			}
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if(session.awayStatus == AwayStatus.AWAY) {
				return;
			}
			PacketBase.Builder pb = PacketBase.newBuilder();
			pb.setPacketType(PacketType.GameOperation);
			pb.setData(ByteString.copyFrom(content));
			ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
		} catch (Exception e) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=sendMsg2Player;error=exception;", e);
		}
	}

	@Override
	public void sendMsg2Desk(byte[] content) {
		for (PlayerInfo player : guard.getPlayerList()) {
			sendMsg2Player(player.position, content);
		}
		for (PlayerInfo player : guard.getNotSitPlayer()) {
			sendMsg2Player(player, content);
		}
	}

	@Override
	public void sendMsg2DeskExceptPosition(byte[] content, int excludePosition) {
		for (PlayerInfo player : guard.getPlayerList()) {
			if (player.position != excludePosition) {
				sendMsg2Player(player.position, content);
			}
		}

		for (PlayerInfo player : guard.getNotSitPlayer()) {
			sendMsg2Player(player, content);
		}
	}

    @Override
    public void sendMsg2DeskExceptPosition(byte[] content, PlayerInfo pl) {
        for (PlayerInfo player : guard.getPlayerList()) {
            if (player.playerId != pl.playerId) {
                sendMsg2Player(player.position, content);
            }
        }

        for (PlayerInfo player : guard.getNotSitPlayer()) {
            sendMsg2Player(player, content);
        }
    }

    @Override
	public void pushWaitNextMatchStart(MJDesk desk, MsgGame.WaitNextMatchStart.Builder gb) {
		try {
			for (PlayerInfo player : guard.getPlayerList()) {
				if (player.robot == 1) {
					return;
				}
				BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
				if(session.awayStatus == AwayStatus.AWAY) {
					return;
				}
				PacketBase.Builder pb = PacketBase.newBuilder();
				pb.setPacketType(PacketType.WaitNextMatchStart);
				pb.setData(gb.build().toByteString());
				ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
			}
		} catch (Exception e) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=pushWaitNextMatchStart;error=exception;", e);
		}
	}

	@Override
	public int setTimer(long mills) {
		TimerEvent tv = new TimerEvent();
		tv.setTime = System.currentTimeMillis();
		tv.triggerTime = tv.setTime + mills;
		tv.timerId = timerId.addAndGet(1);
		timerEventQueue.add(tv);
		lastSetTimer = tv.setTime;

		return tv.timerId;
	}

	@Override
	public void killTimer(int timerID) {
		for (TimerEvent tv : timerEventQueue) {
			if (tv.timerId == timerID) {
				tv.killed = true;
			}
		}
	}

	@Override
	public String getDeskID() {
		return id;
	}

    @Override
    public String getMatchId() {
        return this.getParent().getParent().getId();
    }

    @Override
    public String getGameId() {
        return this.getParent().getParent().getParent().getId();
    }

    @Override
    public void onGameBegin(List<PlayerInfo> mPlayers) {
	    this.gameCount++;
		this.waitingGameStopTime = System.currentTimeMillis();
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=onGameBegin;deskId={};", getDeskID());

		for (PlayerInfo player : mPlayers) {
		    if(player == null || player.isWait || player.isZanLi) continue;
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if (session != null) {
				session.setStatus(PlayerStatus.GAMING, StatusChangeReason.GAME_BEGIN);
				session.currentModule = ServiceRepo.gameModule;
				try {
					ServiceRepo.clusterStubService.notifyUserPlaying(session.userId, session.getGame().getId(), ServiceRepo.serverConfig.instanceId);
				}catch (Exception e){
					logger.info("id--------"+id);
					e.printStackTrace();
				}
			}
		}

//		this.status = DeskStatus.GAMING;

		if (listener != null) {
			listener.onDeskGameStart(this, game);
		}
	}
    @Override
    public boolean checkPlayerReady(PlayerInfo p) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
        return session.getStatus() == PlayerStatus.READY;
    }

	public void addPokerDDZGameLog(GameContext ctx) {
		GameLog log = new GameLog();
		log.setDeskId(getDeskID());
		log.setRoomId(getParent().getRoomId());
		log.setMatchId(getParent().getParent().getId());
		log.setGameId(getParent().getParent().getParent().getId());
		log.setGameStartTime(new Date(ctx.ddzResult.startTime));
		log.setGameEndTime(new Date());
		if(!ctx.ddzResult.Result.isEmpty()){
			List<PokerDDZResult> list = new ArrayList<>(ctx.ddzResult.Result.values());
			for (int i = 0; i < list.size(); i++) {
				PokerDDZResult result = list.get(i);
				if (i == 0) {
					log.setUser1Id(result.playerId);
					log.setUser1Score(result.score);
					log.setUser1FanNum(result.multiple);
					log.setUser1FanDesc("斗地主");
				}
				if (i == 1) {
					log.setUser2Id(result.playerId);
					log.setUser2Score(result.score);
					log.setUser2FanNum(result.multiple);
					log.setUser2FanDesc("斗地主");
				}
				if (i == 2) {
					log.setUser3Id(result.playerId);
					log.setUser3Score(result.score);
					log.setUser3FanNum(result.multiple);
					log.setUser3FanDesc("斗地主");
				}
				if (i == 3) {
					log.setUser4Id(result.playerId);
					log.setUser4Score(result.score);
					log.setUser4FanNum(result.multiple);
					log.setUser4FanDesc("斗地主");
				}
			}
		}
		ServiceRepo.hallPortalService.addGameLog(log);
	}

	public void addPokerZJHGameLog(GameContext ctx) {
		GameLog log = new GameLog();
		log.setDeskId(getDeskID());
		log.setRoomId(getParent().getRoomId());
		log.setMatchId(getParent().getParent().getId());
		log.setGameId(getParent().getParent().getParent().getId());
		log.setGameStartTime(new Date(ctx.zjhResult.startTime));
		log.setGameEndTime(new Date());

		if(!ctx.zjhResult.Result.isEmpty()) {
			List<PokerZJHResult> list = new ArrayList<>(ctx.zjhResult.Result.values());
			for (int i = 0; i < list.size(); i++) {
				PokerZJHResult result = list.get(i);
				if (i == 0) {
					log.setUser1Id(result.playerId);
					log.setUser1Score(result.score);
					log.setUser1FanDesc("扎金花");
				}
				if (i == 1) {
					log.setUser2Id(result.playerId);
					log.setUser2Score(result.score);
					log.setUser2FanDesc("扎金花");
				}
				if (i == 2) {
					log.setUser3Id(result.playerId);
					log.setUser3Score(result.score);
					log.setUser3FanDesc("扎金花");
				}
				if (i == 3) {
					log.setUser4Id(result.playerId);
					log.setUser4Score(result.score);
					log.setUser4FanDesc("扎金花");
				}
			}
		}

		ServiceRepo.hallPortalService.addGameLog(log);
	}
	public void addPokerJACKGameLog(GameContext ctx) {
		GameLog log = new GameLog();
		log.setDeskId(getDeskID());
		log.setRoomId(getParent().getRoomId());
		log.setMatchId(getParent().getParent().getId());
		log.setGameId(getParent().getParent().getParent().getId());
		log.setGameStartTime(new Date(ctx.jackResult.startTime));
		log.setGameEndTime(new Date());

		if(!ctx.jackResult.Result.isEmpty()) {
			List<PokerJACKResult> list = new ArrayList<>(ctx.jackResult.Result.values());
			for (int i = 0; i < list.size(); i++) {
				PokerJACKResult result = list.get(i);
				if (i == 0) {
					log.setUser1Id(result.playerId);
					log.setUser1Score(result.score);
					log.setUser1FanDesc("杰克");
				}
				if (i == 1) {
					log.setUser2Id(result.playerId);
					log.setUser2Score(result.score);
					log.setUser2FanDesc("杰克");
				}
				if (i == 2) {
					log.setUser3Id(result.playerId);
					log.setUser3Score(result.score);
					log.setUser3FanDesc("杰克");
				}
				if (i == 3) {
					log.setUser4Id(result.playerId);
					log.setUser4Score(result.score);
					log.setUser4FanDesc("杰克");
				}
			}
		}

		ServiceRepo.hallPortalService.addGameLog(log);
	}

	public void addPokerNNGameLog(GameContext ctx) {
		GameLog log = new GameLog();
		log.setDeskId(getDeskID());
		log.setRoomId(getParent().getRoomId());
		log.setMatchId(getParent().getParent().getId());
		log.setGameId(getParent().getParent().getParent().getId());
		log.setGameStartTime(new Date(ctx.nnResult.startTime));
		log.setGameEndTime(new Date());

		if(!ctx.nnResult.Result.isEmpty()) {
			List<PokerNNResult> list = new ArrayList<>(ctx.nnResult.Result.values());
			for (int i = 0; i < list.size(); i++) {
				PokerNNResult result = list.get(i);
				if (i == 0) {
					log.setUser1Id(result.playerId);
					log.setUser1Score(result.score);
					log.setUser1FanDesc("牛牛");
				}
				if (i == 1) {
					log.setUser2Id(result.playerId);
					log.setUser2Score(result.score);
					log.setUser2FanDesc("牛牛");
				}
				if (i == 2) {
					log.setUser3Id(result.playerId);
					log.setUser3Score(result.score);
					log.setUser3FanDesc("牛牛");
				}
				if (i == 3) {
					log.setUser4Id(result.playerId);
					log.setUser4Score(result.score);
					log.setUser4FanDesc("牛牛");
				}
			}
		}

		ServiceRepo.hallPortalService.addGameLog(log);
	}

	protected void subUerviceFee(PlayerInfo player) {
		if (getFee() > 0) {
			// 扣除台费
			Result r = ServiceRepo.hallPortalService.changeCoin(player.playerId, -1 * (int) getFee(), false, ItemChangeReason.ENROLL);
			if(r.isOk()) {
				User user = ServiceRepo.hallPortalService.getUser(player.playerId);
				player.coin = user.getCoin();
			}
		}
	}

	@Override
	public synchronized void onGameOver() {
		try {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=onGameOver;deskId={};", getDeskID());

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
			this.waitingGameStartTime = System.currentTimeMillis();
			// ServiceRepo.configManager.getMatchConfig(this.getParent().getParent().getMatchConfig().gameID,
			// player);

			if (!this.isAutoChangeDesk()) {
				guard.ready4NextGame();
				this.reset();
			}

			if (listener != null) {
				listener.onDeskGameFinish(this, game);
			}
			if(!isMultiMatch()){
				logger.info("桌子id--"+this.getDeskID()+"--"+"11111111111111111111111111111111111");
				 // 金币场检查是否符合入场条件
				 for (PlayerInfo player : players) {
					 BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
					 if (session != null) {
						 ServiceRepo.matchService.checkCoinInMath(session, session.getMatch());
					 }
				 }
			}
		} catch (Exception e) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=onGameFinishError;deskId=" + getDeskID(), e);
		}
	}

	protected synchronized void playerReady(int playerId) {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=playerReady;playerId={};deskId={};", playerId, getDeskID());
		PlayerInfo player = guard.getPlayerById(playerId);
		if (player == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerReady;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}
		BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
		if (session == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerReady;error=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}
		if ((session.getStatus() == PlayerStatus.READY || session.getStatus() == PlayerStatus.GAMING) && !player.isZanLi) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerReady;error=alreadySo;playerId={};deskId={};", playerId, getDeskID());
			return;
		}

		game.playerAgree(player);

		session.setStatus(PlayerStatus.READY, StatusChangeReason.READY);
		session.currentModule = ServiceRepo.gameModule;
        player.isZanLi = false;
        player.isWait = true;
		if (listener != null) {
			listener.onPlayerReady(this, player);
		}

		for (int deskPlayer : guard.getplayerIdList()) {
			PushService.instance.pushReadySyn(deskPlayer, player.position, player.playerId);
		}

		tryStartGame();
	}

	@Override
	public void tryStartGame() {
		if(!isDDZ() && gameCount <1) return;

//		logger.info("status:"+status);
		// 桌子不是组队状态,返回
		if (status != DeskStatus.WATING) {
			return;
		}

		// 玩家人数未达到开赛要求,返回
        int playerCount = guard.getPlayerCount();
        if (getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            playerCount = guard.getPlayerAllList().size();
        }
        if ( playerCount < deskConf.seatSizeLower) {
            return;
        }

		// 不是全部玩家处于就绪状态,返回
        List<PlayerInfo> playerList = guard.getPlayerList();
        if (getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG && gameCount >= 1) return;
        for (PlayerInfo p : playerList) {
            if(p.isZanLi) continue;
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            if (session != null) {
                if (session.getStatus() != PlayerStatus.READY) {
                    return;
                }
            }
        }

        // 推送游戏开始消息给玩家
        for (PlayerInfo p : playerList) {
            PushService.instance.pushGameStartMsg(p.playerId, id);
        }


		this.isStart = true;
		// 告知游戏模块开始事件
		game.gameBegin();

        status = DeskStatus.GAMING;
	}

	public void startGame(int playerId) {
		if(isStart) return;

		// 桌子不是组队状态,返回
		if (status != DeskStatus.WATING) {
			return;
		}

		// 玩家人数未达到开赛要求,返回
        int playerCount = guard.getPlayerCount();
		if (getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
		    playerCount = guard.getPlayerAllList().size();
        }
		if ( playerCount < deskConf.seatSizeLower) {
			return;
		}

		// 不是全部玩家处于就绪状态,返回
		List<PlayerInfo> playerList = guard.getPlayerList();
		for (PlayerInfo p : playerList) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			if (session != null) {
				if (session.getStatus() != PlayerStatus.READY) {
					return;
				}
			}
		}

		// 推送游戏开始消息给玩家
		for (PlayerInfo p : playerList) {
			PushService.instance.pushGameStartMsg(p.playerId, id);
		}

		this.isStart = true;
		this.starterId = playerId;
		// 告知游戏模块开始事件
		game.gameBegin();

	}

	@Override
	public boolean isEmpty() {
		return guard.isEmpty();
	}

	@Override
	public boolean isFull() {
		return guard.isFull();
	}
	
	@Override
	public synchronized int playerSitPre(BattleContext ctx) {
		// 推送正在游戏的消息
		pushGamingInfo(ctx.session.userId);

        //同步玩家俱乐部积分
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);

		// 向入场者推送已坐下的玩家消息
		for (PlayerInfo p : guard.getPlayerList()) {
			BattleSession s = ServiceRepo.sessionManager.getIoSession(p.playerId);
			if (s.getStatus() == PlayerStatus.READY || this.isStart) {
				PushService.instance.pushPlayerSitSyn(p.playerId,ctx.playerId,id);
			}
		}
        guard.playerEroll(ServiceRepo.sessionManager.getIoSession(ctx.playerId).player);
		game.playerEnter(session.player);
		if(isStart) game.pushDeskInfo(session.player);
        // 向其它人推送入场者的信息
        for (PlayerInfo p : guard.getPlayerAllList()) {
            PushService.instance.pushPlayerEnterSyn(session.player.playerId, p.playerId, id);
            if (getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) PushService.instance.pushVipRoomList(p.playerId);
        }

        // 向入场者推送所有人的信息
        for (PlayerInfo p : guard.getPlayerAllList()) {
            if (p != session.player) {
                PushService.instance.pushPlayerEnterSyn(p.playerId, session.player.playerId, id);
            }
        }
		return playerSitAfter(ctx);
	}

	@Override
	public boolean canZhongTuNotEnter() {
		return true;
	}

	public boolean canCuoPai() {
		return true;
	}

	@Override
	public int playerSitAfter(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);

		PlayerInfo player = session.player;
		int ind = guard.getEmptySeat(this);

		if (ind == -1) {
			logger.error("桌子id--" + this.getDeskID() + "--" + "act=playerSit;error=invalidPos;playerId={};deskId={};", ctx.playerId, getDeskID());
			return ind;
		}

		if (ind != -1) {
			session.player.position = ind;
			session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.SIT);
			session.awayStatus = AwayStatus.BACK;
			guard.playerSit(player, ind);

			if (listener != null) {
				listener.onPlayerSit(this, player);
			}
			game.playerSit(player);
			logger.info("桌子id--" + this.getDeskID() + "--" + "act=playerSit;position={};deskId={};roomId={};matchId={};", ctx.playerId, ind, this.getDeskID(), this.getParent().getRoomId(), this.getParent().getParent().getId());
		}

        onPlayerBeforeSit(player);
		// 推送正在游戏的消息
		pushGamingInfo(ctx.session.userId);

		// 推送桌子信息
		PushService.instance.pushDeskInfo(session.userId, this.id, this.getPlayerCount(), null);

		{
			// 向其它人推送入场者的信息
			for (PlayerInfo p : guard.getPlayerList()) {
				PushService.instance.pushPlayerSitSyn(player.playerId, p.playerId, id);
			}

			// 向入场者推送所有人的信息
			for (PlayerInfo p : guard.getPlayerList()) {
				if (p != player) {
					PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId, id);
				}
			}

			// 向入场者推送已准备的玩家消息
			for (PlayerInfo p : guard.getPlayerList()) {
				BattleSession s = ServiceRepo.sessionManager.getIoSession(p.playerId);
				if (s.getStatus() == PlayerStatus.READY) {
					PushService.instance.pushReadySyn(ctx.playerId, p.position, p.playerId);
				}
			}
		}

		return ind;
	}

	protected void onPlayerBeforeSit(PlayerInfo player) {
        if(isClubJiFenDesk()) {
            ClubUser clubUser = ServiceRepo.clubDao.selectClubUser(this.getClubId(), player.playerId);
            player.score = clubUser.getClubMemberScore();
        }
	}

	@Override
	public void kickout(int playerId, String msg) {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=kickout;reason={};playerId={};deskId={};", PlayerExitType.UNREADY_KICK, playerId, getDeskID());

		playerExit(playerId, PlayerExitType.UNREADY_KICK);

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=kickout;reason=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}
		session.leaveDesk(this);
		session.player.Reset();
		// PushService.instance.pushDeskInfo(playerId, null, -1, msg);
		PushService.instance.pushKickoutSyn(playerId, msg);
		PushService.instance.pushErrorMsg(session, PacketType.GlobalMsgSyn, msg);
	}

	protected void requestKickout(int requester, int targetPlayerId) {
		logger.error("桌子id--"+this.getDeskID()+"--"+"act=requestKickout;error=illegalreq;player={};targetPlayer={};", requester, targetPlayerId);
		PushService.instance.pushDeskPlayerKickoutRsp(requester, false, "普通场不支持踢人");
	}

	protected synchronized void playerTryAway(int playerId) {
		if(this.deskConf.awayIsExit) { //尝试退出
			this.playerTryExit(playerId, PlayerExitType.REQUEST_EXIT);
			return;
		}

        if (getErBaGameType() > 0) {
            if (!guard.getErBaXiaZhuOrBankerPlayerIds().contains(playerId)) {
                playerExit(playerId, PlayerExitType.REQUEST_EXIT);
            } else {
                playerAway(playerId);
            }
            return;
        }

		PlayerInfo p = guard.getErollNotSitPlayerById(playerId);
		if(p != null){
			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
//			if(playerId != ownerId) {
//				session.leaveDesk(this);
				guard.removeErollNotSitPlayerById(playerId);
				p.position = -1;
//			}
			PushService.instance.pushPlayerExitSyn(-1, playerId, playerId);
			return;
		}

		playerAway(playerId);
	}

	private void playerAway(int playerId) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerAway;error=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}
		PlayerInfo player = guard.getPlayerById(playerId);
		if(player == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerAway;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}

		//广播离开状态
		game.playerAway(player);

		session.awayStatus = AwayStatus.AWAY;
		for(PlayerInfo p : guard.getPlayerAllList()) {
			PushService.instance.pushPlayerAwaySyn(player.position, playerId, p.playerId);
		}
		//离开桌子时回调
		onPlayerAfterAway(player);

		tryPauseGame();
	}

	protected synchronized void playerTryExit(int playerId, PlayerExitType reason) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerTryExit;error=sessionMiss;reason={};playerId={};deskId={};", reason, playerId, getDeskID());
			return;
		}

		//普通场如果游戏中，退出，则托管游戏
		if (session.getStatus() == PlayerStatus.GAMING && !deskConf.allowExitWhenGaming) {
			playerAway(playerId); //如果是游戏中，当作是playerLeave处理
			return;
		}

		if (getErBaGameType() > 0) {
            if (!guard.getErBaXiaZhuOrBankerPlayerIds().contains(playerId)) {
                playerExit(playerId, reason);
            } else {
                playerAway(playerId);
            }
            return;
        }

		PlayerInfo p = guard.getErollNotSitPlayerById(playerId);
		if(p != null){
			if(playerId != ownerId) {
//				session.leaveDesk(this);
				guard.removeErollNotSitPlayerById(playerId);
				p.position = -1;
			}
			PushService.instance.pushPlayerExitSyn(-1, playerId, playerId);
			return;
		}

		playerExit(playerId, reason);
	}

	protected synchronized void tryPauseGame() {
//		if (this.status == DeskStatus.GAMING_PAUSE) {
//			return;
//		}
//		this.status = DeskStatus.GAMING_PAUSE;
//		this.pauseTime = System.currentTimeMillis();
		if(isDDZ()) this.game.gamePause();

//		for (int playerId : guard.getplayerIdList()) {
//			PushService.instance.pushGamePauseSyn(this.id, playerId);
//		}
	}

	@Override
	public synchronized void playerExit(int playerId, PlayerExitType reason) {
		logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerExit;reason={};playerId={};deskId={};", reason, playerId, getDeskID());
		List<Integer> list = guard.getplayerIdList();
		// 告知管理人,申请离开, 腾出座位
		PlayerInfo player = guard.playerExit(playerId, reason.toString());

		if (player == null) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=playerExit;error=playerMiss;reason={};playerId={};deskId={};", reason, playerId, getDeskID());
			return;
		}

		int pos = player.position;
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);

        if((list.contains(playerId) && pos >= 0) || getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            game.playerExit(player);
            if (session != null) {
//                session.leaveDesk(this);
                session.setStatus(PlayerStatus.ENTER_ROOM, StatusChangeReason.LEAVE);// 在房间，不在桌子上
                session.currentModule = ServiceRepo.matchModule;
            }

            if (listener != null) {
                listener.onPlayerLeave(this, player);
                if(this.getClubId() > 0) ServiceRepo.taskPortalService.pushClubRoomModelSyn(BattleConstants.SYN_TYPE_MODIFY,this.getClubId(),this.getClubRoomType(),this.getDeskInfo());
			}
        }

        logger.info("guard.getPlayerAllList()---"+guard.getPlayerAllList());
		// 告知同桌我已离开
		for (PlayerInfo p : guard.getPlayerAllList()) {
        	if((pos >= 0 || getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG))
			PushService.instance.pushPlayerExitPosNotExitRoomMsg(id, pos,playerId, p.playerId);
		}
//        if(pos >= 0){
//            PushService.instance.pushPlayerExitPosNotExitRoomMsg(id,pos,playerId, playerId);
//        }
		PushService.instance.pushPlayerExitSyn(pos, playerId, playerId);
        if(session != null && session.getPlayingOrReadyDesk(playerId) == null )player.position = -1;
		onPlayerAfterExit(player);
	}
	@Override
	public void destroy(DeskDestoryReason reason) {
		logger.error("桌子id--"+this.getDeskID()+"--"+"act=destroy;deskId={};reason={};", getDeskID(), reason);

		List<PlayerInfo> playerList = guard.getPlayerList();

		for (PlayerInfo player : playerList) {
			for (PlayerInfo other : playerList) {
				if (player != other) {
					PushService.instance.pushPlayerExitSyn(player.position, player.playerId, other.playerId);
				}
			}
			for (PlayerInfo other : playerList) {
				if (player == other) {
					PushService.instance.pushPlayerExitSyn(player.position, player.playerId, other.playerId);
				}
			}
		}

//		BattleSession ownerSession = ServiceRepo.sessionManager.getIoSession(ownerId);
//		if(ownerSession != null && ownerSession.channel.isOpen()) {
//            PushService.instance.pushPlayerExitSyn(ownerSession.player.position, ownerSession.userId, ownerSession.userId);
//        }

        Set<PlayerInfo> playerAllList = guard.getPlayerAllList();
		logger.info("桌子id--"+id+"解散,playerAllList--"+playerAllList);
        for (PlayerInfo player : playerAllList) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
			if (session != null) {
				session.leaveDesk(this);
				if(playerList.contains(player)) {
                    session.currentModule = ServiceRepo.matchModule;
                }
				PushService.instance.pushDeskDestory(player.playerId, this.getDeskID());
			}
			guard.playerExit(player.playerId, "DeskDestroy");
			player.Reset();
			onPlayerAfterExit(player);
			this.listener.onPlayerLeave(this, player);
			PushService.instance.pushVipRoomList(player.playerId);
            if(session.getPlayingOrReadyDesk(player.playerId) == null) player.position = -1;

        }

		if (timer != null) {
			timer.setStop(true);
		}

		if (listener != null) {
			listener.onDeskDestroy(this);
		}
		this.status = DeskStatus.DESTROYED;
		dumpGameData(genVideoId());
	}

	@Override
	public String dumpGameData(long videoId) {
		try {
			String data = this.game.dumpGameData();
			String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			String dateFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String dateFileName = videoId + "_" + this.id + "_" + this.gameCount;
			IOUtil.writeFileContent("/home/work/log/record" + date + "/" + dateFileName + ".json", data);
			return data;
		} catch (Exception e) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=dumpGameDataError;deskId=" + getDeskID(), e);
		}
		return null;
	}

	protected void onPlayerAfterExit(PlayerInfo player) {

	}

	protected void onPlayerAfterAway(PlayerInfo player) {
		//默认玩家离开时托管游戏
		this.playerHangeup(player.playerId);
	}

	protected void onPlayerAfterOffline(PlayerInfo player) {
		//比赛场默认玩家离线时托管游戏
		if(isMultiMatch()) {
			this.playerHangeup(player.playerId);
		}
	}

	private void gameMsgRecieve(int playerID, byte[] content) {
		PlayerInfo player = guard.getPlayerById(playerID);
		game.handleGameMsg(player.playerId, player.position, content);
	}

	@Override
	public void onPlayerReadyPacketReceived(int playerId) {
		PlayerReadyEvent event = new PlayerReadyEvent();
		event.key = DeskEventKey.PLAYER_READY;
		event.playerId = playerId;
		otherEventQueue.add(event);
	}

	@Override
	public void onPlayerExitPosNotExitRoomRequstReceived(int playerId, int deskPos) {
		PlayerExitPosNotExitRoomEvent event = new PlayerExitPosNotExitRoomEvent();
		event.key = DeskEventKey.PlayerExitPosNotExitRoom;
		event.playerId = playerId;
		event.deskPos = deskPos;
		otherEventQueue.add(event);
	}

	@Override
	public void onGameMsgPacketReceived(int playerID, byte[] content) {
		GameMsgEvent event = new GameMsgEvent();
		event.key = DeskEventKey.GAME_MSG;
		event.playerId = playerID;
		event.content = content;
		otherEventQueue.add(event);
	}

	@Override
	public void onChatMsgPacketReceived(int playerID, int contentType, byte[] content) {
		try {
			PlayerInfo p = guard.getPlayerById(playerID);
			if (p == null) {
				return;
			}
			PushService.instance.pushChatMsg(p, getDeskID(), guard.getplayerIdList(), contentType, content);
		} catch (Exception e) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"", e);
		}
	}

	@Override
	public void onKickoutPacketReceived(int playerID, int targetPlayerId) {
		KickoutEvent event = new KickoutEvent();
		event.key = DeskEventKey.KICKOUT_PLAYER;
		event.playerId = playerID;
		event.targetPlayerId = targetPlayerId;
		otherEventQueue.add(event);
	}

	@Override
	public void onPlayerChangeDeskPacketReceived(int playerId) {
		ChangeDeskEvent event = new ChangeDeskEvent();
		event.key = DeskEventKey.CHANGE_DESK;
		event.playerId = playerId;
		otherEventQueue.add(event);
	}

    @Override
    public void multiMatchResetAndStart(List<PlayerInfo> playerInfoList) {
        // 重新new
        try {
            Class<?> cls = getClass().getClassLoader().loadClass(deskConf.gameClassFullName);
            game = (Game) cls.newInstance();
            game.setDesk(this, deskConf.gameParam);
        } catch (Exception e) {
            logger.error("act=reset;error=exception;deskId=" + getDeskID(), e);
        }
        this.deskConf = getParent().getParent().getMatchConfig().conditionInfo.deskConf;
        this.status = DeskStatus.WATING;
        this.waitingGameStartTime = System.currentTimeMillis();
        for (PlayerInfo p : this.guard.getPlayerList()) {
            game.playerAgree(p);
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            session.setStatus(PlayerStatus.READY, StatusChangeReason.READY);
        }
    }

    @Override
	public int getBasePoint() {
		return this.getParent().getRoomConfig().basePoint;
	}

	@Override
	public void finalSettle(GameContext context) {
		// nothing 每局分开结算，总结算可以不用
	}

//	private void settleInternal(PlayHandResult res) {
//		PlayerInfo p = guard.getPlayerByPos(res.pos);
//
//		if (p == null) {
//			return;
//		}
//
//		// 机器人结算
//		if (p.robot == 1) {
//			ServiceRepo.robotManager.robotSettle(p, this.getParent().getParent().getMatchConfig().matchID, res.score);
//		}
//
//		int playerid = p.playerId;
//
//		User user = ServiceRepo.hallPortalService.getUser(p.playerId);
//		if (user == null) {
//			logger.error("桌子id--"+this.getDeskID()+"--"+"act=settleInternal;error=userMiss;playerId={};deskId={};", p.playerId, getDeskID());
//			return;
//		}
//
//		if (res.score < 0) {
//			logger.error("桌子id--"+this.getDeskID()+"--"+"ErrorRankPoint,userId:{},point:{};", playerid, res.score);
//		}
//
//		GamePlayingVo ret = new GamePlayingVo();
//		ret.coin = res.score;
//		ret.gameId = this.getParent().getParent().getParent().getId();
//		ret.matchId = this.getParent().getParent().getId();
//		ret.enemyBankrupt = false; // TODO
//		ret.bankrupt = false; // TODO
//		ret.rankPoint = res.score;
//		ret.tax = res.tax;
//		ret.userId = res.playerId;
//		ret.winCount = res.result == PlayHandResult.GAME_RESULT_WIN ? 1 : 0;
//		ret.loseCount = res.result == PlayHandResult.GAME_RESULT_LOSE ? 1 : 0;
//		ret.evenCount = res.result == PlayHandResult.GAME_RESULT_EVEN ? 1 : 0;
//		ret.continueWin = ret.winCount;
//		ret.gameTime = new Date();
//		ret.maxFanDesc = res.fanDesc;
//		ret.maxFanType = res.fanType;
//		ret.maxFanNum = res.fanNum;
//		ret.maxDownCards = res.downcards;
//		ret.maxFanHandCards = res.handcards;
//		if(!isMultiMatch() && p.robot != 1) {
//			ServiceRepo.hallPortalService.addGameResult(ret);
//		}
//		//TODO 扣除台费,赢的人扣除台费,目前写死 以后可配
//		if(res.result==PlayHandResult.GAME_RESULT_WIN && !isMultiMatch()){
//			logger.info("桌子id--"+this.getDeskID()+"--"+"扣台费啦==================+玩家为====="+playerid+"扣除"+(int)(-0.03 * ret.coin));
//			ServiceRepo.hallPortalService.changeCoin(playerid, (int)(-0.03 * ret.coin), false, ItemChangeReason.ENROLL);
//		}
//
//		// 更新用户属性
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
//		if (user != null && session != null) {
//			UserHelper.copyUser2Player(user, session.player);
//		}
//
//		MatchConfig conf = this.getParent().getParent().getMatchConfig();
//		if (conf.game.isRank || conf.isRank) { // 是排位赛
//			// ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId,
//			// res.playerName, conf.gameID, res.score, new Date());
//		}
//	}

	@Override
	public int getPlayerCount() {
		return guard.getPlayerCount();
	}

	@Override
	public DeskStatus getStatus() {
		return this.status;
	}

	@Override
	public boolean isAutoChangeDesk() {
		return deskConf.autoChangeDesk;
	}

	@Override
	public void reset() {
		markDeskAsWaitingGame();
		// TODO 重新new还是直接调用原来的reset?
		try {
			Class<?> cls = getClass().getClassLoader().loadClass(deskConf.gameClassFullName);
			game = (Game) cls.newInstance();
			game.setDesk(this, deskConf.gameParam);
		} catch (Exception e) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=reset;error=exception;deskId=" + getDeskID(), e);
		}
		this.deskConf = getParent().getParent().getMatchConfig().conditionInfo.deskConf;
	}

	public void markDeskAsWaitingGame() {
		this.status = DeskStatus.WATING;
		this.waitingGameStartTime = System.currentTimeMillis();
		for (PlayerInfo p : this.guard.getPlayerList()) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.NEXT_GAME_UNREADY);
		}
	}

	// class GameMsgJob extends DelayJob {
	// public GameMsgEvent event;
	//
	// public GameMsgJob(GameMsgEvent event) {
	// super(1, 0, TimeUnit.MILLISECONDS);
	// this.event = event;
	// }
	// @Override
	// public void run() {
	// GameMsgEvent e = (GameMsgEvent)event;
	// gameMsgRecieve(e.playerId, e.content);
	// }
	// }

	@Override
	public void check() {
		JSONObject json = new JSONObject();
		json.put("deskId", id);
		json.put("roomId", room.getRoomId());
		json.put("deskStatus", status);
		json.put("delay", delayTimerCount + "/" + invokeTimerCount);

		JSONArray players = new JSONArray();
		json.put("players", players);
		for (PlayerInfo p : guard.getPlayerList()) {
			JSONObject player = new JSONObject();
			player.put("playerId", p.playerId);
			player.put("name", player.names());
			player.put("position", p.position);
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			if (session != null) {
				player.put("status", session.getStatus());
			}
			players.add(player);
		}

		logger.info("桌子id--"+this.getDeskID()+"--"+"\r\n" + new Gson().toJson(json));
	}

	@Override
	public void onPlayerReconnectPacketReceived(int playerId) {
		PlayReconnectEvent e = new PlayReconnectEvent();
		e.playerId = playerId;
		e.key = DeskEventKey.PLAYER_RECONNECT;
		this.otherEventQueue.add(e);
	}

	@Override
	public void onPlayerComeBackPacketReceived(int playerId) {
		PlayComBackEvent e = new PlayComBackEvent();
		e.playerId = playerId;
		e.key = DeskEventKey.PLAYER_COMBACK;
		this.otherEventQueue.add(e);
	}

	@Override
	public void onPlayerExitPacketReceived(int playerId) {
		PlayExitEvent e = new PlayExitEvent();
		e.playerId = playerId;
		e.key = DeskEventKey.PLAYER_EXIT;
		this.otherEventQueue.add(e);
	}

	@Override
	public void onPlayerAwayPacketReceived(int playerId) {
		PlayAwayEvent e = new PlayAwayEvent();
		e.playerId = playerId;
		e.key = DeskEventKey.PLAYER_AWAY;
		this.otherEventQueue.add(e);
	}

	@Override
	public void onPlayerOfflinePacketReceived(int playerId) {
		PlayerOfflineEvent e = new PlayerOfflineEvent();
		e.playerId = playerId;
		e.key = DeskEventKey.PLAYER_OFFLINE;
		this.otherEventQueue.add(e);
	}

	private void playerOffline(int playerId) {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=playerOffline;playerId={};deskId={};", playerId, getDeskID());

		// 玩家A掉线
		PlayerInfo player = this.guard.getPlayerById(playerId);
		if (player == null) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=playerOffline;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
			return;
		}

		// 是否需要告知其他人玩家A已掉线
		if (isNeedPushPlayerOfflineMsg()) {
			for (PlayerInfo p : this.guard.getPlayerAllList()) {
				if (p != player) {
					PushService.instance.pushPlayerOfflineSyn(player.position, player.playerId, p.playerId);
				}
			}
		}

		// 告知游戏模块玩家已掉线
		this.game.playerOffline(player);

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if(session!=null) {
			session.onlineStatus = OnlineStatus.OFFLINE;
		}

		onPlayerAfterOffline(player);

		tryPauseGame();
	}

	/**
	 * 是否需要告知其他人玩家A已掉线
	 *
	 * @return
	 */
	private boolean isNeedPushPlayerOfflineMsg() {
		// 如果是游戏中
		// 游戏没有开始直接当离开房间处理
		if (this.getStatus() == DeskStatus.GAMING && deskConf.synPlayerOfflineAndReconnect) {
			return true;
		}
		return true;
	}

	private boolean isNeedPushPlayerReconnectMsg() {
		// 如果是游戏中
		// 游戏没有开始直接当离开房间处理
		if (this.getStatus() == DeskStatus.GAMING && deskConf.synPlayerOfflineAndReconnect) {
			return true;
		}
		return true;
	}

	private void playerReconnect(int playerId) {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=playerReconnect;playerId={};deskId={};", playerId, getDeskID());
		PlayerInfo player = this.guard.getPlayerById(playerId);
		boolean enRollNotSit = false;
		if (player == null) {
			player = guard.getErollNotSitPlayerById(playerId);
			if(player == null) {
				logger.error("桌子id--" + this.getDeskID() + "--" + "act=playeyComeBack;error=playerMissById;playerId={};deskId={};", playerId, getDeskID());
				return;
			}
			enRollNotSit = true;
		}

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if(session != null) {
			session.onlineStatus = OnlineStatus.ONLINE;
			session.awayStatus = AwayStatus.BACK;
			session.currentModule = ServiceRepo.gameModule;
		}

		// 向重连玩家A推送赛场、房间、桌子消息(已废弃，都是空方法,没有推消息)
		PushService.instance.pushMatchInfo(playerId, this.getParent().getParent().getId(), "");
		PushService.instance.pushRoomInfo(playerId, this.getParent().getRoomId(), "");
		PushService.instance.pushDeskInfo(playerId, this.getDeskID(), this.getPlayerCount(), "");

		// 向重连玩家A推送正在游戏的消息(推送赛场、房间、桌子消息)
        pushGamingInfo(playerId);

		// 向其它人推送入场者的信息
		for (PlayerInfo p : guard.getPlayerAllList()) {
			PushService.instance.pushPlayerEnterSyn(player.playerId, p.playerId, id);
		}

		// 向入场者推送所有人的信息
		for (PlayerInfo p : guard.getPlayerAllList()) {
			if (p != player) {
				PushService.instance.pushPlayerEnterSyn(p.playerId, player.playerId, id);
			}
		}

		// 向重连玩家A推送A入场坐下的信息
		if(!enRollNotSit) PushService.instance.pushPlayerSitSyn(playerId, playerId,this.id);

		// 向重连玩家A推送所有人(除自己,即B、C、D)的入场坐下信息
		for (PlayerInfo p : guard.getPlayerList()) {
			if (p != player) {
				PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId,this.id);
			}
		}
		// 向重连玩家A推送游戏开始消息
		PushService.instance.pushGameStartMsg(playerId, this.getDeskID());

		// 向其他人推送A已重连的消息
		if (isNeedPushPlayerReconnectMsg()) {
			for (PlayerInfo p : this.guard.getPlayerAllList()) {
				if (p != player) {
					PushService.instance.pushPlayerReconnectSyn(player.position, player.playerId, p.playerId);
				}
			}
		}

		// 通知游戏模块玩家重连(重发牌局数据)
		if(!enRollNotSit) {
		    this.game.playerReconnect(player);
        }else{
		    if(isStart){
                game.pushDeskInfo(session.player);
            }
        }

		// 如果游戏暂停，尝试恢复游戏
		this.tryResumeGame();
	}

	// 恢复已暂停的游戏
	protected void tryResumeGame() {
//		if (this.getStatus() == DeskStatus.GAMING) {
//			return;
//		}

//		for (int playerId : guard.getplayerIdList()) {
//			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
//			if (session.getStatus() != PlayerStatus.GAMING) {
//				return;
//			}
//		}
//		this.status = DeskStatus.GAMING;
//
//		for (int playerId : guard.getplayerIdList()) {
//			PushService.instance.pushGameResumeSyn(this.id, playerId);
//		}
//		if (this.getStatus() == DeskStatus.GAMING) {
//			return;
//		}

		for(PlayerInfo p : guard.getPlayerList()){
			if(!p.isOnline) return;
		}

//		this.status = DeskStatus.GAMING;

		this.game.gameResume();
	}

	private void playerComeBack(int playerId) {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=playeyComeBack;playerId={};deskId={};", playerId, getDeskID());
		PlayerInfo player = this.guard.getPlayerById(playerId);
		boolean enRollNotSit = false;
		if (player == null) {
			player = guard.getErollNotSitPlayerById(playerId);
			if(player == null) {
				logger.error("桌子id--" + this.getDeskID() + "--" + "act=playeyComeBack;error=playerMissById;playerId={};deskId={};", playerId, getDeskID());
				return;
			}
			enRollNotSit = true;
		}

		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if(session != null) {
			session.awayStatus = AwayStatus.BACK;
			session.onlineStatus = OnlineStatus.ONLINE;
			session.currentModule = ServiceRepo.gameModule;
		}

		// 向重连玩家A推送赛场、房间、桌子消息(已废弃，都是空方法,没有推消息)
		PushService.instance.pushMatchInfo(playerId, this.getParent().getParent().getId(), "");
		PushService.instance.pushRoomInfo(playerId, this.getParent().getRoomId(), "");
		PushService.instance.pushDeskInfo(playerId, this.getDeskID(), this.getPlayerCount(), "");

		// 向重连玩家A推送正在游戏的消息(推送赛场、房间、桌子消息)
        pushGamingInfo(playerId);

		// 向其它人推送入场者的信息
		for (PlayerInfo p : guard.getPlayerAllList()) {
			PushService.instance.pushPlayerEnterSyn(player.playerId, p.playerId, id);
		}

		// 向入场者推送所有人的信息
		for (PlayerInfo p : guard.getPlayerAllList()) {
			if (p != player) {
				PushService.instance.pushPlayerEnterSyn(p.playerId, player.playerId, id);
			}
		}

		// 向重连玩家A推送A入场坐下的信息
		if(!enRollNotSit) PushService.instance.pushPlayerSitSyn(playerId, playerId,this.id);


		// 向重连玩家A推送所有人(除自己,即B、C、D)的入场坐下信息
		for (PlayerInfo p : guard.getPlayerList()) {
			if (p != player) {
				PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId,this.id);
			}
		}
		// 向重连玩家A推送游戏开始消息
		PushService.instance.pushGameStartMsg(playerId, this.getDeskID());

		// 向其他人推送A已重连的消息
		if (isNeedPushPlayerReconnectMsg()) {
			for (PlayerInfo p : this.guard.getPlayerAllList()) {
				if (p != player) {
					PushService.instance.pushPlayerComebackSyn(player.position, player.playerId, p.playerId);
				}
			}
		}

		// 通知游戏模块玩家重连(重发牌局数据)
		if(!enRollNotSit) this.game.playerComeBack(player);

		// 如果游戏暂停，尝试恢复游戏
//		if(StringUtils.equals(this.getParent().getParent().getId(),"G_DDZ_MATCH_3VIP")){
			this.tryResumeGame();
//		}
	}

    private void pushGamingInfo(int playerId) {
        int wanfa = this instanceof MJDesk ? ((MJDesk) this).getWanfa() : 0;
        int roomType = this instanceof MJDesk ? ((MJDesk) this).getRoomType() : 0;
        int totalQuan = this instanceof MJDesk ? ((MJDesk) this).getTotalQuan() : 0;
        int limitMax = 	this instanceof MJDesk ? ((MJDesk) this).getLimitMax() : 0;
        int menNum = this instanceof MJDesk ? ((MJDesk) this).getMenNum() : 0;
        int yaZhu = this instanceof MJDesk ? ((MJDesk) this).getYaZhu() : 0;
        long clubId = this instanceof MJDesk ? ((MJDesk) this).getClubId() : 0;
        int clubRoomType = this instanceof MJDesk ? ((MJDesk) this).getClubRoomType() : -1;
        int enterScore = this instanceof MJDesk ? ((MJDesk) this).getEnterScore(): -1;
        int canFufen = this instanceof MJDesk ? ((MJDesk) this).getCanFufen(): -1;
        int choushuiScore = this instanceof MJDesk ? ((MJDesk) this).getChoushuiScore(): -1;
        int choushuiNum = this instanceof MJDesk ? ((MJDesk) this).getChoushuiNum(): -1;
        int zengsongNum = this instanceof MJDesk ? ((MJDesk) this).getZengsongNum(): -1;
		int qiangZhuangNum = this instanceof MJDesk ? ((MJDesk) this).getQiangZhuangNum(): -1;
		int erBaGameType = this instanceof MJDesk ? ((MJDesk) this).getErBaGameType(): -1;
		Map<Integer,Integer> niuFan = this instanceof MJDesk ? this.getNiuFanStr(): new HashMap<>();
		String niuFanStr = new Gson().toJson(niuFan);
        int ownerId = this.getDeskOwner();

        PushService.instance.pushPlayerGamingInfo(playerId, this.getParent().getParent().getParent().getId(),
                this.getParent().getParent().getId(), this.getParent().getRoomId(),
                this.getDeskID() + "", wanfa, roomType, totalQuan,limitMax,yaZhu,menNum,ownerId
        ,clubId,clubRoomType,enterScore,choushuiScore,canFufen,choushuiNum,zengsongNum,qiangZhuangNum,niuFanStr,erBaGameType);
    }

    @Override
	public List<PlayerInfo> getPlayers() {
		return guard.getPlayerList();
	}

	@Override
	public List<PlayerInfo> getPlayingPlayers() {
		List<PlayerInfo> result = new ArrayList<>();
		for(PlayerInfo p: guard.getPlayerList()){
			if(p.isWait || p.isZanLi) continue;
			result.add(p);
		}
		return result;
	}

	@Override
	public List<PlayerInfo> getAllPlayers() {
		return new ArrayList<>(guard.getPlayerAllList());
	}

	@Override
	public DeskConfig getDeskConfig() {
		return this.deskConf;
	}

	@Override
	public void setDeskConfig(DeskConfig conf) {
		this.deskConf = conf;
		if (this.game != null) {
			this.game.setGameParam(this.deskConf.gameParam);
		}
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	@Override
	public double getFee() {
		return this.getParent().getRoomConfig().fee.get(0).currenceCount;
	}

	@Override
	public int getDeskOwner() {
		return this.ownerId;
	}

	@Override
	public void setDeskOwner(int ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public double getDeskDelayStatus() {
		if (invokeTimerCount == 0) {
			return 0;
		}
		double f = delayTimerCount * 100 / invokeTimerCount;
		BigDecimal bd = new BigDecimal(f);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	@Override
	public void onDismissPacketRequest() {
		DismissEvent event = new DismissEvent();
		event.key = DeskEventKey.DISMISS;
		otherEventQueue.add(event);
	}

	@Override
	public void setDeskInValid() {
		errCount++;
	}

	@Override
	public boolean isAutoReady() {
		return this.deskConf.autoReady;
	}

	@Override
	public String getReplyData() {
		return replayData;
	}

	public void setReplayData(String historyData) {
		this.replayData = historyData;
	}

	@Override
	public void markAsAdminUse() {
		this.adminDesk = true;

		loadReplayData();
	}

	private void loadReplayData() {
		logger.info("桌子id--"+this.getDeskID()+"--"+"act=loadReplayData;deskId={};", getDeskID());
		try {
			byte data[] = IOUtil.tryGetFileData("/home/game/data/replay.json");
			if (data != null) {
				String json = new String(data, "UTF-8");
				this.setReplayData(json);
			}
		} catch (Exception e) {
			logger.error("桌子id--"+this.getDeskID()+"--"+"act=loadReplayData;error=exception;", e);
		}
	}

	@Override
	public boolean isAdminUse() {
		return adminDesk;
	}

	@Override
	public List<PlayerInfo> loopGetPlayer(int pos, int count, int includePos) {
		List<PlayerInfo> list = new LinkedList<PlayerInfo>();
		int maxInd = getDeskConfig().seatSizeUpper;
		int fromInd = (pos + 1) % maxInd;
		while (fromInd != pos) {
			PlayerInfo p = guard.getPlayerByPos(fromInd);
			if (p != null) {
				list.add(p);
				if (count > 0 && list.size() >= count) {
					break;
				}
			}
			fromInd++;
			fromInd = (fromInd) % maxInd;
		}
		if (includePos == 1) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null) {
				list.add(p);
			}
		}
		if (includePos == 2) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null) {
				list.add(0, p);
			}
		}
		return list;
	}

	@Override
	public List<PlayerInfo> loopGetPlayerZJH(int pos, int count, int includePos) {
		List<PlayerInfo> list = new LinkedList<PlayerInfo>();
		int maxInd = getDeskConfig().seatSizeUpper;
		int fromInd = (pos + 1) % maxInd;
		while (fromInd != pos) {
			PlayerInfo p = guard.getPlayerByPos(fromInd);
			if (p != null && !p.isQiPai && !p.isLose && !p.isWait) {
				list.add(p);
				if (count > 0 && list.size() >= count) {
					break;
				}
			}
			fromInd++;
			fromInd = (fromInd) % maxInd;
		}
		if (includePos == 1) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isQiPai && !p.isLose && !p.isWait) {
				list.add(p);
			}
		}
		if (includePos == 2) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isQiPai && !p.isLose && !p.isWait) {
				list.add(0, p);
			}
		}
		return list;
	}

	@Override
	public List<PlayerInfo> loopGetPlayerJACK(int pos, int count, int includePos) {
		List<PlayerInfo> list = new LinkedList<>();
		int maxInd = getDeskConfig().seatSizeUpper;
		int fromInd = (pos + 1) % maxInd;
		while (fromInd != pos) {
			PlayerInfo p = guard.getPlayerByPos(fromInd);
			if (p != null && !p.isWait && !p.isZanLi) {
				list.add(p);
				if (count > 0 && list.size() >= count) {
					break;
				}
			}
			fromInd++;
			fromInd = (fromInd) % maxInd;
		}
		if (includePos == 1) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait && !p.isZanLi) {
				list.add(p);
			}
		}
		if (includePos == 2) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait && !p.isZanLi) {
				list.add(0, p);
			}
		}
		return list;
	}

	@Override
	public List<PlayerInfo> loopGetPlayerNN(int pos, int count, int includePos) {
		List<PlayerInfo> list = new LinkedList<>();
		int maxInd = getDeskConfig().seatSizeUpper;
		int fromInd = (pos + 1) % maxInd;
		while (fromInd != pos) {
			PlayerInfo p = guard.getPlayerByPos(fromInd);
			if (p != null && !p.isWait) {
				list.add(p);
				if (count > 0 && list.size() >= count) {
					break;
				}
			}
			fromInd++;
			fromInd = (fromInd) % maxInd;
		}
		if (includePos == 1) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait) {
				list.add(p);
			}
		}
		if (includePos == 2) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait) {
				list.add(0, p);
			}
		}
		return list;
	}

	@Override
	public List<PlayerInfo> loopGetPlayerErBa(int pos, int count, int includePos) {
		List<PlayerInfo> list = new LinkedList<>();
		int maxInd = getDeskConfig().seatSizeUpper;
		int fromInd = (pos + 1) % maxInd;
		while (fromInd != pos) {
			PlayerInfo p = guard.getPlayerByPos(fromInd);
			if (p != null && !p.isWait) {
				list.add(p);
				if (count > 0 && list.size() >= count) {
					break;
				}
			}
			fromInd++;
			fromInd = (fromInd) % maxInd;
		}
		if (includePos == 1) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait) {
				list.add(p);
			}
		}
		if (includePos == 2) {
			PlayerInfo p = guard.getPlayerByPos(pos);
			if (p != null && !p.isWait) {
				list.add(0, p);
			}
		}
		return list;
	}

	@Override
	public void ready4NextGame(GameContext context) {
		for(PlayerInfo p : this.guard.getPlayerList()) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.NEXT_GAME_UNREADY);
		}
		guard.ready4NextGame();
		if(isMultiMatch()) {
			for (PlayerInfo p : this.guard.getPlayerList()) {
				BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
				session.setStatus(PlayerStatus.READY, StatusChangeReason.NEXT_GAME_READY);
			}
		}
	}

	@Override
	public void startNextGame(GameContext context) {
		this.game.setDesk(this, deskConf.gameParam);
		this.status = DeskStatus.WATING;
		this.waitingGameStartTime = System.currentTimeMillis();
	}

	@Override
	public void zanLi(PlayerInfo p) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
		if(session.getStatus() == PlayerStatus.UNREADY) {
			game.playerZanLi(p);
		}
	}

	@Override
	public List<Integer> getDebugData(int pos) {
		PlayerInfo p = getDeskPlayer(pos);
		BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
		return session.debugData == null ? new ArrayList<Integer>() : session.debugData;
	}

	@Override
	public void sendErrorMsg(int position, String msg) {
		try {
			PlayerInfo player = guard.getPlayerByPos(position);
			PushService.instance.pushGlobalErrorSyn(player.playerId, msg);
		} catch (Exception e) {
			logger.info("桌子id--"+this.getDeskID()+"--"+"act=sendErrorMsg;error=exception;", e);
		}
	}

	@Override
	public void log(LogLevel level, String msg, int position) {
		msg += ";position=" + position + ";deskId=" + getDeskID() + ";matchId=" + matchId + ";roomId=" + roomId;
		switch (level) {
		case DEBUG:
//			logger.debug(msg);
			break;
		case INFO:
			logger.info("桌子id--"+this.getDeskID()+"--"+msg);
			break;
		case ERROR:
			logger.error("桌子id--"+this.getDeskID()+"--"+msg);
			break;
		default:
			break;
		}

	}

	@Override
	public void setDeskId(String id) {
		this.id = id;
	}

	@Override
	public void handSettle(GameContext context) {
		dumpGameData(genVideoId());
		addPokerDDZGameLog(context);
	}

	@Override
	public boolean hasNextGame(GameContext context) {
		if(this.getParent().getParent() instanceof MultiMatchImpl){
			return context.nextHandNum <= ((MultiMatchImpl)this.getParent().getParent()).getMatchConf().juNum;
		}
		return false;
	}

	@Override
	public int getGameCount() {
		return gameCount;
	}

	@Override
	public int getPlayerActionTimeOut(int act) {
		return this.deskConf.gameOperTimeOut;
	}

	@Override
	public DeskModel getDeskInfo() {
		Room room = this.getParent();
		Match match = room.getParent();
		com.buding.battle.logic.module.game.Game game = match.getParent();
		DeskModel model = new DeskModel();
		model.deskId = getDeskID();
		model.gameId = game.getId();
		model.gameName = game.getName();
		model.matchId = match.getId();
		model.matchName = match.getName();
		model.roomId = room.getRoomId();
		model.roomName = room.getRoomConfig().roomName;
		model.deskStatus = this.status.toString();
		model.gameCount = this.gameCount;
		model.wanfa = this.getWanfa();
		model.totalJuNum = this.getTotalQuan();
		model.limitMax = this.getLimitMax();
		model.clubId = this.getClubId();
		model.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(createTime));
		model.players.addAll(guard.getPlayerList());		
		return model;
	}

	@Override
	public void onSetGamingDataReq(String json) {
		this.game.setGamingDate(json);
	}

	@Override
	public String printGameDetail() {
		return null;
	}

	@Override
	public boolean isHasPlayer(int playerId) {
		for(int pid : guard.getplayerIdList()) {
			if(pid == playerId) {
				return true;
			}
		}
		return false;
	}

	//生成videoId
	public long genVideoId() {
		long id = 0;
		try{
			id = ServiceRepo.hallPortalService.genVideoId();
		}catch (Exception e){
			String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			id = Long.valueOf(date);
		}finally {
			return id;
		}
	}

    @Override
    public List<Integer> getplayerIdList() {
        return guard.getplayerIdList();
    }

    @Override
    public List<Integer> getNotSitPlayerIdList() {
        return guard.getNotSitPlayerIdList();
    }

    public int getLimitMax(){
    return -1;
	}

	@Override
    public boolean isClubJiFenDesk() {
        return this.getClubRoomType() == Constants.CLUB_JI_FEN_DESK;
    }

    @Override
    public List<Integer> getfuLiPlayerList(String gameId){
	    return ServiceRepo.userRoomDao.getfuLiPlayerList(gameId);
    }

    public Map<Integer, Boolean> getVoteMap() {
        return voteMap;
    }

	@Override
	public void onViewGuanZhanReqPacketReceived(int playerId) {
		ViewGuanZhanEvent event = new ViewGuanZhanEvent();
		event.key = DeskEventKey.ViewGuanZhanReq;
		event.playerId = playerId;
		otherEventQueue.add(event);
	}

	@Override
	public void gameCountIncr() {
	    gameCount++;
        for (PlayerInfo p : guard.getPlayerAllList()) {
            PushService.instance.pushVipRoomList(p.playerId);
        }
    }

    @Override
    public void erBaXiaZhuOrConfirmBanker(PlayerInfo pl) {
        guard.erBaXiaZhuOrConfirmBanker(pl);
    }

    @Override
    public List<Integer> getErBaXiaZhuOrBankerPlayerIds() {
       return guard.getErBaXiaZhuOrBankerPlayerIds();
    }

    @Override
    public void clearXiaZhuPlayer() {
        guard.clearXiaZhuPlayer();
    }

    @Override
    public int getStarterId() {
        return  starterId;
    }
}
