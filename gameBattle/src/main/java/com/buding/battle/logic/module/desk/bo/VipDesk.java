package com.buding.battle.logic.module.desk.bo;

import com.buding.api.context.*;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.event.ChangeDeskEvent;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.db.model.*;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.common.constants.RoomState;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sf.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

public class VipDesk extends MJDeskImpl {
	UserRoom userRoom = null;
	private int vipRoomType;
	private int quanNum;
	private int fee;
	private int limitMax;
	private int menNum ;
	private int yaZhu ;
	private long clubId ;
	private int clubRoomType = -1;
	private int danZhuLimix ;
	private int canZhongTuNotEnter ;
	private boolean canCuoPai ;
	private int enterScore = -1;
	private int canFufen = -1;
	private int choushuiScore = -1;
	private int choushuiNum = -1;
	private int zengsongNum = -1;
	private int qiangZhuangNum = 0;	//牛牛抢庄倍数,经典抢庄传0
	private Map<Integer,Integer> niuFanStr = new HashMap<>();   //牛番
	private int erBaGameType = 0;   //28游戏类型
	private Gson gson = new Gson();
	private Type type = new TypeToken<Map<Integer,Integer>>(){}.getType();


    public VipDesk(DeskListener listener, Room room, DeskConfig deskConf, String deskId,int wanfa) {
		super(listener, room, deskConf, deskId);
		this.wanfa = wanfa;
	}

	@Override
	public void changeDesk(ChangeDeskEvent e) throws Exception {
		// 不允许换桌
		logger.error("桌子ID"+this.getDeskID()+"--"+"act=changeDesk;error=vipMatchNotAllowChangeDesk;userId={};deskId={};roomId={};", e.playerId, this.id, this.getParent().getRoomId());
	}

	@Override
	public void destroy(DeskDestoryReason reason) {
//		if (gameCount == 0) {
//			UserRoom room = ServiceRepo.userRoomDao.getByCode(this.id);
//			if(room != null) {
//				JSONObject obj = JSONObject.fromObject(room.getParams());
//				int fee = obj.optInt("fee");
//				//退钻石
//				List<PlayerInfo> playerList = guard.getPlayerList();
//				for (PlayerInfo p : playerList) {
//					logger.info("桌子ID"+this.getDeskID()+"--"+"==================" + p.getPlayerID());
//					ServiceRepo.hallPortalService.changeDiamond(p.getPlayerID(), fee, false, ItemChangeReason.DESTORY_RET);
//				}
//			}
//		}

		super.destroy(reason);

		// 记录数据到数据库
		try {
			logger.info("桌子ID"+this.getDeskID()+"--"+"act=destory;deskId={};diamondCount={};id={}", this.getDeskID(), gameCount,this.id);
			UserRoom room = ServiceRepo.userRoomDao.getByCode(this.id);
			room.setLastActiveTime(new Date());
			room.setRoomState(RoomState.CLOSE);
			ServiceRepo.userRoomDao.updateUserRoom(room);
//			if(StringUtils.isNotBlank(ServiceRepo.hallPortalService.getQunZhuRoom(this.id))) {
//				ServiceRepo.vipService.pushVipRoomList(ownerId);
//				ServiceRepo.hallPortalService.delQunZhuRoom(this.id);
//			}
		} catch (Exception e) {
			logger.error("桌子ID"+this.getDeskID()+"--"+"act=destroy;error=exception", e);
		}
	}
		
	public double getFee() {
		return 0; //不扣除服务费
	}

    private void finalClubScore(Map<Integer,Integer> gameScoreMap,Map<Integer,Integer> allScoreMap) {
        if(isClubJiFenDesk()){
            List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(gameScoreMap.entrySet());
            list.sort((o1, o2) -> {
                //升序
//                    return o1.getValue()-o2.getValue();
                //降序
                return o2.getValue() - o1.getValue();
            });
            for (int i = 0; i < list.size(); i++) {
                Map.Entry<Integer, Integer> entry = list.get(i);
                ClubUser clubUser = ServiceRepo.clubDao.selectClubUser(this.getClubId(), entry.getKey());
                if(clubUser != null) {
                    int score = entry.getValue();
                    if(score >= this.getChoushuiScore() && i < this.getChoushuiNum()){
                        score *= (1- (double)this.getZengsongNum()/100);
                    }

                    int scoreModify = score - entry.getValue();
                    if(scoreModify != 0) {
//                        int allScore = scoreModify + allScoreMap.get(entry.getKey());
                        clubUser.setClubId(clubUser.getClubId());
                        clubUser.setClubMemberId(clubUser.getClubMemberId());
                        clubUser.setClubMemberType(clubUser.getClubMemberType());
                        clubUser.setClubMemberName(clubUser.getClubMemberName());
                        clubUser.setClubMemberImg(clubUser.getClubMemberImg());
                        clubUser.setCtime(new Date());
                        clubUser.setClubMemberScore(score);
                        ServiceRepo.clubDao.updateClubUser(clubUser);
                    }

					//记录日志
					if(scoreModify != 0) {
//                        int allScore = scoreModify + allScoreMap.get(entry.getKey());
						ClubScoreLog log = new ClubScoreLog();
						log.setClubId(this.getClubId());
						log.setPlayerId(clubUser.getClubMemberId());
						log.setScoreModify(scoreModify);
						log.setScoreLeft(score);
						log.setMtime(new Date());
						log.setType(Constants.SCORE_LOG_BIAO_QING);
						String info = "表情赠送";
						log.setInfo(info);
						ServiceRepo.clubDao.insertClubScoreLog(log);
					}

                    ServiceRepo.taskPortalService.pushMemberInfoSyn(clubUser);
                }
            }
        }
    }

	@Override
	public void finalSettle(GameContext context) {
		//房间总战绩
		UserRoomResult ur = new UserRoomResult();
		//斗地主
		if (context.ddzFinalResult != null) {
			System.out.println("斗地主 房间总战绩 : " + userRoom.getId());
			System.out.println("context.ddzFinalResult.endTime" + context.ddzFinalResult.endTime);
			List<PokerDDZFinalResult> list2 = new ArrayList<PokerDDZFinalResult>();
            Map<Integer,Integer> gameScoreMap = new TreeMap<>();
            Map<Integer,Integer> allScoreMap = new HashMap<>();
			for(PokerDDZFinalResult res : context.ddzFinalResult.finalResults.values()) {
				if(res.playerId <= 0) {
					continue;
				}
				list2.add(res);
				gameScoreMap.put(res.playerId,res.score);
				allScoreMap.put(res.playerId,res.allScore);
			}
			ur.setRoomId(userRoom.getId());
			ur.setRoomName(userRoom.getRoomName());
			ur.setStartTime(new Date(context.ddzFinalResult.startTime));
			ur.setEndTime(new Date(context.ddzFinalResult.endTime));
			ur.setDetail(new Gson().toJson(list2));
			ur.setGameId(this.getParent().getParent().getId());
			ur.setClubId(this.getClubId());
			ur.setClubRoomType(this.getClubRoomType());
			finalClubScore(gameScoreMap,allScoreMap);
		}
		//扎金花
		if (context.zjhFinalResult != null) {
			List<Integer> fuliUsers = getfuLiPlayerList("G_ZJH");
			Map<Integer, Integer> fuliMap = new HashMap<>();
			System.out.println("扎金花 房间总战绩 : " + userRoom.getId());
//			System.out.println("context.ddzFinalResult.endTime" + context.ddzFinalResult.endTime);
			List<PokerZJHFinalResult> list2 = new ArrayList<PokerZJHFinalResult>();
            Map<Integer,Integer> gameScoreMap = new TreeMap<>();
			Map<Integer,Integer> allScoreMap = new HashMap<>();
			for(PokerZJHFinalResult res : context.zjhFinalResult.finalResults.values()) {
				if(res.playerId <= 0) continue;
				if (fuliUsers.contains(res.playerId)) {
					fuliMap.put(res.playerId, res.allScore);
				}
				list2.add(res);
                gameScoreMap.put(res.playerId,res.score);
                allScoreMap.put(res.playerId,res.allScore);
			}
			ur.setRoomId(userRoom.getId());
			ur.setRoomName(userRoom.getRoomName());
			ur.setStartTime(new Date(context.zjhFinalResult.startTime));
			ur.setEndTime(new Date(context.zjhFinalResult.endTime));
			ur.setDetail(new Gson().toJson(list2));
			ur.setGameId(this.getParent().getParent().getId());
            ur.setClubId(this.getClubId());
            ur.setClubRoomType(this.getClubRoomType());
            finalClubScore(gameScoreMap,allScoreMap);
            for (Map.Entry<Integer,Integer> entry : fuliMap.entrySet()) {
				FuLiCount fuLiCount = new FuLiCount();
				fuLiCount.setPlayerId(entry.getKey());
				fuLiCount.setNum(entry.getValue());
				ServiceRepo.userRoomDao.insertFuliCount(fuLiCount);
			}

		}
		//杰克
		if (context.jackFinalResult != null) {
			System.out.println("杰克 房间总战绩 : " + userRoom.getId());
//			System.out.println("context.ddzFinalResult.endTime" + context.ddzFinalResult.endTime);
			List<PokerJACKFinalResult> list2 = new ArrayList<>();
            Map<Integer,Integer> gameScoreMap = new TreeMap<>();
			Map<Integer,Integer> allScoreMap = new HashMap<>();
			for(PokerJACKFinalResult res : context.jackFinalResult.finalResults.values()) {
				if(res.playerId <= 0) continue;
				list2.add(res);
                gameScoreMap.put(res.playerId,res.allScore);
				allScoreMap.put(res.playerId,res.score);
			}
			ur.setRoomId(userRoom.getId());
			ur.setRoomName(userRoom.getRoomName());
			ur.setStartTime(new Date(context.jackFinalResult.startTime));
			ur.setEndTime(new Date(context.jackFinalResult.endTime));
			ur.setDetail(new Gson().toJson(list2));
			ur.setGameId(this.getParent().getParent().getId());
            ur.setClubId(this.getClubId());
            ur.setClubRoomType(this.getClubRoomType());
            finalClubScore(gameScoreMap,allScoreMap);
		}
		//杰克
		if (context.nnFinalResult != null) {
			System.out.println("杰克 房间总战绩 : " + userRoom.getId());
//			System.out.println("context.ddzFinalResult.endTime" + context.ddzFinalResult.endTime);
			List<PokerNNFinalResult> list2 = new ArrayList<>();
            Map<Integer,Integer> gameScoreMap = new TreeMap<>();
			Map<Integer,Integer> allScoreMap = new HashMap<>();
			for(PokerNNFinalResult res : context.nnFinalResult.finalResults.values()) {
				if(res.playerId <= 0) continue;
				list2.add(res);
                gameScoreMap.put(res.playerId,res.allScore);
				allScoreMap.put(res.playerId,res.score);
			}
			ur.setRoomId(userRoom.getId());
			ur.setRoomName(userRoom.getRoomName());
			ur.setStartTime(new Date(context.nnFinalResult.startTime));
			ur.setEndTime(new Date(context.nnFinalResult.endTime));
			ur.setDetail(new Gson().toJson(list2));
			ur.setGameId(this.getParent().getParent().getId());
            ur.setClubId(this.getClubId());
            ur.setClubRoomType(this.getClubRoomType());
            finalClubScore(gameScoreMap,allScoreMap);
		}
		//28
		if (context.erBaFinalResult != null) {
			System.out.println("28 房间总战绩 : " + userRoom.getId());
//			System.out.println("context.ddzFinalResult.endTime" + context.ddzFinalResult.endTime);
			List<PokerErBaFinalResult> list2 = new ArrayList<>();
            Map<Integer,Integer> gameScoreMap = new TreeMap<>();
			Map<Integer,Integer> allScoreMap = new HashMap<>();
			for(PokerErBaFinalResult res : context.erBaFinalResult.finalResults.values()) {
				if(res.playerId <= 0) continue;
				list2.add(res);
                gameScoreMap.put(res.playerId,res.allScore);
				allScoreMap.put(res.playerId,res.score);
			}
			ur.setRoomId(userRoom.getId());
			ur.setRoomName(userRoom.getRoomName());
			ur.setStartTime(new Date(context.erBaFinalResult.startTime));
			ur.setEndTime(new Date(context.erBaFinalResult.endTime));
			ur.setDetail(new Gson().toJson(list2));
			ur.setGameId(this.getParent().getParent().getId());
            ur.setClubId(this.getClubId());
            ur.setClubRoomType(this.getClubRoomType());
            finalClubScore(gameScoreMap,allScoreMap);
		}
		ServiceRepo.userRoomDao.insertUserRoomResult(ur);
	}

	@Override
	public void requestKickout(int playerId, int targetPlayerId) {
		if (playerId != ownerId) {
			logger.error("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;error=nopriviledge;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "你不是房主,没有权限踢走其他玩家");
			return;
		}

		PlayerInfo p = this.guard.getPlayerById(targetPlayerId);
		if (p == null) {
			logger.error("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;result=userleaved;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, true, null);
			return;
		}

		BattleSession session = ServiceRepo.sessionManager.getIoSession(targetPlayerId);
		if (session != null) {
			if (session.getStatus() == PlayerStatus.GAMING) {
				logger.error("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;error=usergaming;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
				PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "玩家正在游戏中,无法踢出");
				return;
			}
			// if (session.getStatus() == PlayerStatus.READY) {
			// logger.error("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;error=userready;playerId={};targetPlayerId={};deskId={}",
			// playerId, targetPlayerId, this.id);
			// PushService.instance.pushDeskPlayerKickoutRsp(playerId, false,
			// "玩家已准备,无法踢出");
			// return;
			// }
		}

		if (this.status == DeskStatus.GAMING) {
			logger.error("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;error=deskgaming;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
			PushService.instance.pushDeskPlayerKickoutRsp(playerId, false, "桌子已开赛,无法踢人");
			return;
		}

		kickout(targetPlayerId, "房主已将你踢出桌子");
		PushService.instance.pushDeskPlayerKickoutRsp(playerId, true, null);
		// PushService.instance.pushJumpBack2HallSyn(targetPlayerId,
		// "桌主已将你踢出桌子");
		logger.info("桌子ID"+this.getDeskID()+"--"+"act=requestKickout;result=ok;playerId={};targetPlayerId={};deskId={};", playerId, targetPlayerId, this.id);
	}

	public void setUserRoom(UserRoom userRoom) {
		this.userRoom = userRoom;
		JSONObject obj = JSONObject.fromObject(userRoom.getParams());
		vipRoomType = obj.getInt("vipRoomType");
		quanNum = obj.getInt("quanNum");
		fee = obj.optInt("fee");
		limitMax = userRoom.getLimitMax();
		menNum = obj.optInt("menNum");
		yaZhu = obj.optInt("yaZhu");
		clubId = obj.optInt("clubId");
		clubRoomType = obj.optInt("clubRoomType");
        enterScore = obj.optInt("enterScore");
        canFufen = obj.optInt("canFufen");
        choushuiScore = obj.optInt("choushuiScore");
        choushuiNum = obj.optInt("choushuiNum");
        zengsongNum = obj.optInt("zengsongNum");
        zengsongNum = obj.optInt("zengsongNum");
		qiangZhuangNum = obj.optInt("qiangZhuangNum");

        niuFanStr = gson.fromJson(obj.optString("niuFanStr"),type);

        erBaGameType = obj.getInt("erBaGameType");
        canCuoPai = obj.optBoolean("canCuoPai");
		canZhongTuNotEnter = obj.optInt("canZhongTuNotEnter");
		danZhuLimix = yaZhu == PokerConstants.ZJH_ZI_YOU_CAHNG ? 15 : yaZhu * 2;
		wanfa = Integer.valueOf(userRoom.getWanfa());
	}
	
	@Override
	public void onPlayerAfterExit(PlayerInfo player) {
		super.onPlayerAfterExit(player);
//		ServiceRepo.vipService.pushVipRoomList(ownerId);
	}

	@Override
	protected void onPlayerAfterAway(PlayerInfo player) {
		//vip 离开时不能托管游戏
	}

	public synchronized void playerTryExit(int playerId, PlayerExitType reason) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		PlayerInfo player = session.player;
		if(player != null && player.startGameCount > 0){
            playerTryAway(playerId);
            return;
        }
		if(player != null && !player.isWait && player.position >= 0){
			playerTryAway(playerId);
			return;
		}

		if(isStart() && !this.deskConf.allowExitWhenGaming) { //游戏已开始，不允许退出
			playerTryAway(playerId);
			return;
		}
//		if(isStart() && this.getPlayerCount() == 2 && this.getplayerIdList().contains(playerId)){
//			playerTryAway(playerId);
//			return;
//		}
//		if(ownerId == playerId) {
//			//只离开，占住坑
//			playerTryAway(playerId);
//			return;
//		}
		super.playerTryExit(playerId, reason);
	}
	
	@Override
	public synchronized int playerSitPre(BattleContext ctx) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		PlayerInfo player = session.player;
		player.score =  0; //积分清零
		return super.playerSitPre(ctx);
	}

	@Override
	public int playerSitAfter(BattleContext ctx) {
//		if(ctx.playerId == ownerId){
//			BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
//			PlayerInfo player = session.player;
//			int ind;
//			if(player.position < 0){
//				ind = guard.getEmptySeat(this);
//			}else{
//				ind = player.position;
//			}
//			if(ind >= 0){
//				playerSit(ownerId,ind);
//				return ind;
//			}else{
//				return 100;
//			}
//
//		}else{
			return 100;
//		}
	}

	@Override
	public void handSettle(GameContext context) {

		//保存战斗数据
		long videoId = genVideoId();
		dumpGameData(videoId);

		//更新房间战绩
		//斗地主
		if (context.ddzResult != null) {
			System.out.println("斗地主 单局战绩 : " + userRoom.getId());
			List<PokerDDZResult> list2 = new ArrayList<PokerDDZResult>();
			for (PokerDDZResult item : context.ddzResult.Result.values()) {
				if (item.playerId > 0) {
					list2.add(item);
				}
			}
			UserRoomResultDetail detail = new UserRoomResultDetail();
			detail.setStartTime(new Date(context.ddzResult.startTime));
			detail.setEndTime(new Date(context.ddzResult.endTime));
			detail.setDetail(new Gson().toJson(list2));
			detail.setRoomId(userRoom.getId());
			detail.setRoomName(userRoom.getRoomName());
			detail.setBankerPos(context.bankerPos);
			detail.setWinerPos(context.winerPos);
			detail.setVideoId(videoId);
			detail.setVideoDetail(this.game.dumpGameData());
			detail.setGameCount(this.getGameCount());
			detail.setGameId(this.getParent().getParent().getId());
			detail.setClubId(this.getClubId());
			detail.setClubRoomType(this.getClubRoomType());
			ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);

			//更新胜败记录和排行榜信息
			for(PokerDDZResult res : list2) {
				GamePlayingVo ret = new GamePlayingVo();
				ret.coin = 0; //vip场不扣除金币
				ret.gameId = this.getParent().getParent().getParent().getId();
				ret.matchId =this.getParent().getParent().getId();
				ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
				ret.bankrupt = false; //不扣金币，不存在破产的可能性
				ret.rankPoint = res.score;
				ret.tax = 0; //不扣服务费，房主用房卡一次性支付
				ret.userId = res.playerId;
				ret.winCount = res.result == PokerConstants.GAME_RESULT_WIN ? 1 : 0;
				ret.loseCount = res.result == PokerConstants.GAME_RESULT_LOSE ? 1 : 0;
				ret.evenCount = res.result == PokerConstants.GAME_RESULT_EVEN ? 1 : 0;
				ret.continueWin = ret.winCount;
				ret.gameTime = new Date();
				ret.maxFanType = res.isDouble;
				ret.maxFanNum = res.isDiZhu;

				User user = ServiceRepo.hallPortalService.addGameResult(ret);

				//更新用户属性
				BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
				session.player.score += res.score; //改变累计积分
                handleClubScore(ret, session);
                if(user != null) {
					UserHelper.copyUser2Player(user, session.player);
				}

				MatchConfig conf = this.getParent().getParent().getMatchConfig();
				if (conf.game.isRank || conf.isRank) { // 是排位赛
					//ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId, res.playerName, conf.gameID, res.score, new Date());
				}
			}
			// 2018 / 1 / 12 修改
			if(context.handNum==1){
				List<PokerDDZFinalResult> list3 = new ArrayList<>();
				for(PokerDDZFinalResult res : context.ddzFinalResult.finalResults.values()) {
					if(res.playerId <= 0) {
						continue;
					}
					list3.add(res);
				}

				//每个人在哪个房间游戏过
				for(PokerDDZFinalResult res : list3) {
					UserRoomGameTrack track = new UserRoomGameTrack();
					track.setGameTime(new Date());
					track.setRoomId(userRoom.getId());
					track.setUserId(Long.valueOf(res.playerId));
					ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
				}
			}

			addPokerDDZGameLog(context);
		}
		//扎金花
		if (context.zjhResult != null) {
			System.out.println("扎金花 单局战绩 : " + userRoom.getId());
			List<PokerZJHResult> list2 = new ArrayList<PokerZJHResult>();
			for (PokerZJHResult item : context.zjhResult.Result.values()) {
				if (item.playerId > 0) {
					list2.add(item);
				}
			}
			UserRoomResultDetail detail = new UserRoomResultDetail();
			detail.setStartTime(new Date(context.zjhResult.startTime));
			detail.setEndTime(new Date(context.zjhResult.endTime));
			detail.setDetail(new Gson().toJson(list2));
			detail.setRoomId(userRoom.getId());
			detail.setRoomName(userRoom.getRoomName());
			detail.setBankerPos(context.bankerPos);
			detail.setWinerPos(context.winerPos);
			detail.setVideoId(videoId);
			detail.setVideoDetail(this.game.dumpGameData());
			detail.setGameCount(this.getGameCount());
			detail.setGameId(this.getParent().getParent().getId());
            detail.setClubId(this.getClubId());
            detail.setClubRoomType(this.getClubRoomType());
			ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);

			//更新胜败记录和排行榜信息
			for(PokerZJHResult res : list2) {
				GamePlayingVo ret = new GamePlayingVo();
				ret.coin = 0; //vip场不扣除金币
				ret.gameId = this.getParent().getParent().getParent().getId();
				ret.matchId =this.getParent().getParent().getId();
				ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
				ret.bankrupt = false; //不扣金币，不存在破产的可能性
				ret.rankPoint = res.score;
				ret.tax = 0; //不扣服务费，房主用房卡一次性支付
				ret.userId = res.playerId;
				ret.winCount = res.result == PokerConstants.GAME_RESULT_WIN ? 1 : 0;
				ret.loseCount = res.result == PokerConstants.GAME_RESULT_LOSE ? 1 : 0;
				ret.evenCount = res.result == PokerConstants.GAME_RESULT_EVEN ? 1 : 0;
				ret.continueWin = ret.winCount;
				ret.gameTime = new Date();

				User user = ServiceRepo.hallPortalService.addGameResult(ret);

				//更新用户属性
				BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
                session.player.score += res.score; //改变累计积分
                handleClubScore(ret, session);
                if(user != null) {
					UserHelper.copyUser2Player(user, session.player);
				}

				MatchConfig conf = this.getParent().getParent().getMatchConfig();
				if (conf.game.isRank || conf.isRank) { // 是排位赛
					//ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId, res.playerName, conf.gameID, res.score, new Date());
				}
			}
			// 2018 / 1 / 12 修改
//			if(context.handNum==1){
				if(context.zjhFinalResult != null) {
					List<PokerZJHFinalResult> list3 = new ArrayList<>();
					for (PokerZJHFinalResult res : context.zjhFinalResult.finalResults.values()) {
						if (res.playerId <= 0) continue;
						PlayerInfo p = guard.getPlayerById(res.playerId);
						if(p != null && p.gameCount == 1) list3.add(res);
					}

					//每个人在哪个房间游戏过
					for (PokerZJHFinalResult res : list3) {
						UserRoomGameTrack track = new UserRoomGameTrack();
						track.setGameTime(new Date());
						track.setRoomId(userRoom.getId());
						track.setUserId(Long.valueOf(res.playerId));
						ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
					}
				}
//			}

			addPokerZJHGameLog(context);
		}
		//杰克
		if (context.jackResult != null) {
			System.out.println("杰克 单局战绩 : " + userRoom.getId());
			List<PokerJACKResult> list2 = new ArrayList<PokerJACKResult>();
			for (PokerJACKResult item : context.jackResult.Result.values()) {
				if (item.playerId > 0) {
					list2.add(item);
				}
			}
			UserRoomResultDetail detail = new UserRoomResultDetail();
			detail.setStartTime(new Date(context.jackResult.startTime));
			detail.setEndTime(new Date(context.jackResult.endTime));
			detail.setDetail(new Gson().toJson(list2));
			detail.setRoomId(userRoom.getId());
			detail.setRoomName(userRoom.getRoomName());
			detail.setBankerPos(context.bankerPos);
			detail.setWinerPos(context.winerPos);
			detail.setVideoId(videoId);
			detail.setVideoDetail(this.game.dumpGameData());
			detail.setGameCount(this.getGameCount());
			detail.setGameId(this.getParent().getParent().getId());
            detail.setClubId(this.getClubId());
            detail.setClubRoomType(this.getClubRoomType());
			ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);

			//更新胜败记录和排行榜信息
			for(PokerJACKResult res : list2) {
				GamePlayingVo ret = new GamePlayingVo();
				ret.coin = 0; //vip场不扣除金币
				ret.gameId = this.getParent().getParent().getParent().getId();
				ret.matchId =this.getParent().getParent().getId();
				ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
				ret.bankrupt = false; //不扣金币，不存在破产的可能性
				ret.rankPoint = res.score;
				ret.tax = 0; //不扣服务费，房主用房卡一次性支付
				ret.userId = res.playerId;
				ret.winCount = res.result == PokerConstants.GAME_RESULT_WIN ? 1 : 0;
				ret.loseCount = res.result == PokerConstants.GAME_RESULT_LOSE ? 1 : 0;
				ret.evenCount = res.result == PokerConstants.GAME_RESULT_EVEN ? 1 : 0;
				ret.continueWin = ret.winCount;
				ret.gameTime = new Date();

				User user = ServiceRepo.hallPortalService.addGameResult(ret);

				//更新用户属性
				BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
                session.player.score += res.score; //改变累计积分
                handleClubScore(ret, session);
                if(user != null) {
					UserHelper.copyUser2Player(user, session.player);
				}

				MatchConfig conf = this.getParent().getParent().getMatchConfig();
				if (conf.game.isRank || conf.isRank) { // 是排位赛
					//ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId, res.playerName, conf.gameID, res.score, new Date());
				}
			}
			// 2018 / 1 / 12 修改
//			if(context.handNum==1){
				if(context.jackFinalResult != null) {
					List<PokerJACKFinalResult> list3 = new ArrayList<>();
					for (PokerJACKFinalResult res : context.jackFinalResult.finalResults.values()) {
						if (res.playerId <= 0) continue;
						PlayerInfo p = guard.getPlayerById(res.playerId);
						if(p != null && p.gameCount == 1) list3.add(res);
					}

					//每个人在哪个房间游戏过
					for (PokerJACKFinalResult res : list3) {
						UserRoomGameTrack track = new UserRoomGameTrack();
						track.setGameTime(new Date());
						track.setRoomId(userRoom.getId());
						track.setUserId(Long.valueOf(res.playerId));
						ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
					}
				}
//			}

			addPokerJACKGameLog(context);
		}
		//28
		if (context.erBaResult != null) {
			System.out.println("28 单局战绩 : " + userRoom.getId());
			List<PokerErBaResult> list2 = new ArrayList<>();
			for (PokerErBaResult item : context.erBaResult.Result.values()) {
				if (item.playerId > 0) {
					list2.add(item);
				}
			}
			UserRoomResultDetail detail = new UserRoomResultDetail();
			detail.setStartTime(new Date(context.erBaResult.startTime));
			detail.setEndTime(new Date(context.erBaResult.endTime));
			detail.setDetail(new Gson().toJson(list2));
			detail.setRoomId(userRoom.getId());
			detail.setRoomName(userRoom.getRoomName());
			detail.setBankerPos(context.bankerPos);
			detail.setWinerPos(context.winerPos);
			detail.setVideoId(videoId);
			detail.setVideoDetail(this.game.dumpGameData());
			detail.setGameCount(this.getGameCount());
			detail.setGameId(this.getParent().getParent().getId());
            detail.setClubId(this.getClubId());
            detail.setClubRoomType(this.getClubRoomType());
			ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);

			//更新胜败记录和排行榜信息
			for(PokerErBaResult res : list2) {
				GamePlayingVo ret = new GamePlayingVo();
				ret.coin = 0; //vip场不扣除金币
				ret.gameId = this.getParent().getParent().getParent().getId();
				ret.matchId =this.getParent().getParent().getId();
				ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
				ret.bankrupt = false; //不扣金币，不存在破产的可能性
				ret.rankPoint = res.score;
				ret.tax = 0; //不扣服务费，房主用房卡一次性支付
				ret.userId = res.playerId;
				ret.winCount = res.result == PokerConstants.GAME_RESULT_WIN ? 1 : 0;
				ret.loseCount = res.result == PokerConstants.GAME_RESULT_LOSE ? 1 : 0;
				ret.evenCount = res.result == PokerConstants.GAME_RESULT_EVEN ? 1 : 0;
				ret.continueWin = ret.winCount;
				ret.gameTime = new Date();

				User user = ServiceRepo.hallPortalService.addGameResult(ret);

				//更新用户属性
				BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
                session.player.score += res.score; //改变累计积分
                handleClubScore(ret, session);
                if(user != null) {
                    UserHelper.copyUser2Player(user, session.player);
                }
			}

			if(context.erBaFinalResult != null) {
				List<PokerErBaFinalResult> list3 = new ArrayList<>();
				for (PokerErBaFinalResult res : context.erBaFinalResult.finalResults.values()) {
					if (res.playerId <= 0) continue;
					PlayerInfo p = guard.getPlayerById(res.playerId);
					if(p != null && p.gameCount == 1) list3.add(res);
				}

				//每个人在哪个房间游戏过
				for (PokerErBaFinalResult res : list3) {
					UserRoomGameTrack track = new UserRoomGameTrack();
					track.setGameTime(new Date());
					track.setRoomId(userRoom.getId());
					track.setUserId(Long.valueOf(res.playerId));
					ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
				}
			}
		}
		//牛牛
		if (context.nnResult != null) {
			System.out.println("牛牛 单局战绩 : " + userRoom.getId());
			List<PokerNNResult> list2 = new ArrayList<>();
			for (PokerNNResult item : context.nnResult.Result.values()) {
				if (item.playerId > 0) {
					list2.add(item);
				}
			}
			UserRoomResultDetail detail = new UserRoomResultDetail();
			detail.setStartTime(new Date(context.nnResult.startTime));
			detail.setEndTime(new Date(context.nnResult.endTime));
			detail.setDetail(new Gson().toJson(list2));
			detail.setRoomId(userRoom.getId());
			detail.setRoomName(userRoom.getRoomName());
			detail.setBankerPos(context.bankerPos);
			detail.setWinerPos(context.winerPos);
			detail.setVideoId(videoId);
			detail.setVideoDetail(this.game.dumpGameData());
			detail.setGameCount(this.getGameCount());
			detail.setGameId(this.getParent().getParent().getId());
            detail.setClubId(this.getClubId());
            detail.setClubRoomType(this.getClubRoomType());
			ServiceRepo.userRoomDao.insertUserRoomResultDetail(detail);

			//更新胜败记录和排行榜信息
			for(PokerNNResult res : list2) {
				GamePlayingVo ret = new GamePlayingVo();
				ret.coin = 0; //vip场不扣除金币
				ret.gameId = this.getParent().getParent().getParent().getId();
				ret.matchId =this.getParent().getParent().getId();
				ret.enemyBankrupt = false; //不扣金币，不存在破产的可能性
				ret.bankrupt = false; //不扣金币，不存在破产的可能性
				ret.rankPoint = res.score;
				ret.tax = 0; //不扣服务费，房主用房卡一次性支付
				ret.userId = res.playerId;
				ret.winCount = res.result == PokerConstants.GAME_RESULT_WIN ? 1 : 0;
				ret.loseCount = res.result == PokerConstants.GAME_RESULT_LOSE ? 1 : 0;
				ret.evenCount = res.result == PokerConstants.GAME_RESULT_EVEN ? 1 : 0;
				ret.continueWin = ret.winCount;
				ret.gameTime = new Date();

				User user = ServiceRepo.hallPortalService.addGameResult(ret);

				//更新用户属性
				BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
                session.player.score += res.score; //改变累计积分
                handleClubScore(ret, session);
                if(user != null) {
					UserHelper.copyUser2Player(user, session.player);
				}

				MatchConfig conf = this.getParent().getParent().getMatchConfig();
				if (conf.game.isRank || conf.isRank) { // 是排位赛
					//ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId, res.playerName, conf.gameID, res.score, new Date());
				}
			}
			// 2018 / 1 / 12 修改
//			if(context.handNum==1){
				if(context.nnFinalResult != null) {
					List<PokerNNFinalResult> list3 = new ArrayList<>();
					for (PokerNNFinalResult res : context.nnFinalResult.finalResults.values()) {
						if (res.playerId <= 0) continue;
						PlayerInfo p = guard.getPlayerById(res.playerId);
						if(p != null && p.gameCount == 1) list3.add(res);
					}

					//每个人在哪个房间游戏过
					for (PokerNNFinalResult res : list3) {
						UserRoomGameTrack track = new UserRoomGameTrack();
						track.setGameTime(new Date());
						track.setRoomId(userRoom.getId());
						track.setUserId(Long.valueOf(res.playerId));
						ServiceRepo.userRoomDao.insertUserRoomGameTrack(track);
					}
				}
//			}

			addPokerNNGameLog(context);
		}
	}

    private void handleClubScore(GamePlayingVo ret, BattleSession session) {
        if(isClubJiFenDesk()){
            ClubUser clubUser = ServiceRepo.clubDao.selectClubUser(this.getClubId(), ret.userId);
            if(clubUser != null) {
                clubUser.setClubId(clubUser.getClubId());
                clubUser.setClubMemberId(clubUser.getClubMemberId());
                clubUser.setClubMemberType(clubUser.getClubMemberType());
                clubUser.setClubMemberScore(session.player.score);
                clubUser.setClubMemberName(clubUser.getClubMemberName());
                clubUser.setClubMemberImg(clubUser.getClubMemberImg());
                clubUser.setCtime(new Date());
                ServiceRepo.clubDao.updateClubUser(clubUser);

				//记录日志
				int scoreModify = ret.rankPoint;
				if(scoreModify != 0) {
					ClubScoreLog log = new ClubScoreLog();
					log.setClubId(this.getClubId());
					log.setPlayerId(ret.userId);
					log.setScoreModify(scoreModify);
					log.setScoreLeft(session.player.score);
					log.setMtime(new Date());
					log.setType(Constants.SCORE_LOG_GAME);
					String info = "";
					if (scoreModify > 0) {
						info += "游戏增加";
					} else {
						info += "游戏减少";
					}
					log.setInfo(info);
					ServiceRepo.clubDao.insertClubScoreLog(log);
				}
                ServiceRepo.taskPortalService.pushMemberInfoSyn(clubUser);
            }
        }
    }

    @Override
	public void ready4NextGame(GameContext context) {
		//自动准备
		for (PlayerInfo p : this.guard.getPlayerList()) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.NEXT_GAME_READY);
		}
		this.game.setDesk(this, deskConf.gameParam);
		markDeskAsWaitingGame();
	}

    @Override
    public void subServiceFee(PlayerInfo pl) {
		ServiceRepo.hallPortalService.changeDiamond(pl.playerId,-1 * fee,false,ItemChangeReason.ENROLL);
    }

	@Override
	public void startNextGame(GameContext context) {
		for (PlayerInfo p : this.guard.getPlayerList()) {
			BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
			if(session.getStatus() != PlayerStatus.GAMING) session.setStatus(PlayerStatus.READY, StatusChangeReason.NEXT_GAME_READY);
		}
		this.game.setDesk(this, deskConf.gameParam);
		markDeskAsWaitingGame();
	}

	public void markDeskAsWaitingGame() {
		this.status = DeskStatus.WATING;
		this.waitingGameStartTime = System.currentTimeMillis();
	}
	
	@Override
	public boolean hasNextGame(GameContext context) {
//		return false;
		if(isClubJiFenDesk()){
            if(context.nextHandNum > quanNum) return false;
	        int needKickOutCount = 0;
	        boolean canContinue = true;
	        if(this.getCanFufen() == Constants.CLUB_CAN_NOT_FU_FEN){
	            for(PlayerInfo p : context.playingPlayers.values()){
	                if(p == null) continue;
	                if((p.score + p.curJuScore) <= 0) {
                        needKickOutCount++;
	                    context.needKickOutPlayers.put(p.playerId,p);
                    }
                }
            }
            int playerCount = this.guard.getPlayerCount();
            if((playerCount - needKickOutCount) < this.deskConf.seatSizeLower){
	            canContinue = false;
            }
            return canContinue;
        }else {
            return context.nextHandNum <= quanNum;
        }
	}

	@Override
	public void addUserConsumeDiamondLog(int playerNum) {
		UserConsumeDiamond userConsumeDiamond = new UserConsumeDiamond();
		userConsumeDiamond.setCtime(new Date());
		userConsumeDiamond.setDiamondNum(playerNum * fee);
		userConsumeDiamond.setMatchId(this.getParent().getParent().getId());
		ServiceRepo.hallPortalService.addUserConsumeDiamondLog(userConsumeDiamond);
	}

	@Override
	public synchronized void onGameOver() {
		try {
			logger.info("桌子ID"+this.getDeskID()+"--"+"act=onGameOver;deskId={};", getDeskID());

			List<PlayerInfo> players = guard.getPlayerList();
			for (PlayerInfo player : players) {
				BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
				if (session != null) {
					session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.GAME_FINISH);
					session.currentModule = ServiceRepo.matchModule;
				}
			}

			this.status = DeskStatus.WATING;

			if (!this.isAutoChangeDesk()) {
				guard.ready4NextGame();
				this.reset();
			}

			if (listener != null) {
				listener.onDeskGameFinish(this, game);
			}
		} catch (Exception e) {
			logger.error("桌子ID"+this.getDeskID()+"--"+"act=onGameFinishError;deskId=" + getDeskID(), e);
		}
	}

	@Override
	public boolean isVipTable() {
		return true;
	}

	@Override
	public int getTotalQuan() {
		return quanNum;
	}

	@Override
	public int getLimitMax() {
		return limitMax;
	}

	@Override
	public int getMenNum() {
		return menNum;
	}

	@Override
	public int getYaZhu() {
		return yaZhu;
	}

	@Override
    public int getDanZhuLimix() {
        return danZhuLimix;
    }

    @Override
    public boolean canZhongTuNotEnter() {
        return canZhongTuNotEnter == 1;
    }

    @Override
    public boolean canCuoPai() {
        return canCuoPai;
    }

	@Override
	public long getClubId() {
		return clubId;
	}

	@Override
	public int getClubRoomType() {
		return clubRoomType;
	}


	@Override
	public int getEnterScore() {
		return enterScore;
	}

	@Override
	public int getCanFufen() {
		return canFufen;
	}

	@Override
	public int getChoushuiScore() {
		return choushuiScore;
	}

	@Override
	public int getChoushuiNum() {
		return choushuiNum;
	}

	@Override
	public int getZengsongNum() {
		return zengsongNum;
	}

    @Override
    public int getQiangZhuangNum() {
        return qiangZhuangNum;
    }

    @Override
    public int getErBaGameType() {
        return erBaGameType;
    }

    @Override
    public Map<Integer, Integer> getNiuFanStr() {
        return niuFanStr;
    }
}
