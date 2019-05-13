package com.buding.battle.logic.module.match;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.DeskDestoryReason;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.room.bo.MultiRoomImpl;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.cluster.model.RoomOnlineModel;
import com.buding.common.result.Result;
import com.buding.common.util.VelocityUtil;
import com.buding.db.model.Award;
import com.buding.db.model.Msg;
import com.buding.db.model.User;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.EnterCondition;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.user.helper.UserHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.game.MsgGame.*;
import packet.msgbase.MsgBase;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jaime
 * @qq 1094086610
 */
public class MultiMatchImpl extends BaseParent<Game> implements Match {
	protected Logger logger = LogManager.getLogger(getClass());
	ConcurrentMap<String, Room> roomMap = new ConcurrentHashMap<String, Room>();
	public int playerCount = 0;
	transient MatchConfig matchConf;
	//赛场初始建立时间
	public long initTime;
	//赛场开始游戏时间
	public long startTime;
	//赛场id
	public String id;
	//比赛第几轮
	public int lunNum = 0;
	//上一轮人数
	public int lastLunPlayerNum = 0;
	//本轮人数
	public int currentLunPlayerNum = 0;
	//轮空人数
	public int lunKongNum = 0;
	//淘汰人数
	public int taoTaiNum = 0;
	//参加比赛人数
	public int biSaiNum = 0;
	//每轮桌子完成局数的数量
	public int biSaiFinishNum = 0;
	//总奖池
	public int totalPrizePool = 0;
	//赛场玩家
	public List<Integer> playerList = new ArrayList<>();
	//赛场玩家积分,key:玩家id,value:比赛积分,生命周期与playerList一样
	public HashMap<Integer,Integer> playerScoreMap = new HashMap<>();
	//赛场玩家排名,key:玩家id,value:玩家名次,生命周期与playerList一样
	public HashMap<Integer,Integer> playerRankMap = new HashMap<>();
	//赛场被淘汰人的排名
	public HashMap<Integer,Integer> playerTaoTaiRankMap = new HashMap<>();
	//赛场分配的桌子
	public Set<CommonDesk> deskList = new HashSet<>();
	private final Timer timer = new Timer();
	public MultiMatchImpl(Game parent) {
		super(parent);
	}

	@Override
	public void triggerByTime() {
		Date time = new Date(startTime);
//		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				logger.info("-------开始执行任务--------");
				multiMatchStartNotify();
				timer.cancel();
			}
		}, time);
	}

	class ValueComparator implements Comparator<Integer> {
		Map<Integer, Integer> base;
		//这里需要将要比较的map集合传进来
		public ValueComparator(Map<Integer, Integer> base) {
			this.base = base;
		}
		//比较的时候，传入的两个参数应该是map的两个key，根据上面传入的要比较的集合base，可以获取到key对应的value，然后按照value进行比较
		@Override
		public int compare(Integer a, Integer b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	public HashMap<Integer, Integer> sortMapByValue(Map<Integer, Integer> map) {
		TreeMap<Integer, Integer> treeMap = new TreeMap<>(new ValueComparator(map));
		treeMap.putAll(map);
//		System.out.println(treeMap);
		return new HashMap<>(treeMap);
	}
	/**
	 * 赛场开始分配玩家,每轮开始的方法
	 * 1.积分计算:
	 * 		初始积分:1000,按每一把结果分数加减
	 * 		下一轮比赛积分=上一轮比赛积分*20%+500
	 * 2.晋级规则:
	 * 		1,比赛开始时,未满4人直接解散本次赛事
	 * 		2,整体规则为,取半晋级,轮空直接晋级
	 *
	 * 			晋级人数          轮空   取排名多少晋级       总人数
	 * 	第一轮	n/2-(n/2)%4	     n%4   n/2-(n/2)%4-n%4    n
	 * 	第二轮	以后的n= n/2-(n/2)%4
	 * 	.
	 * 	.
	 * 	.
	 */
	public void multiMatchStartNotify() {
		int playerNum = playerList.size();
		if(playerNum<4){
			//赛场报名人数小于4人,直接解散比赛,给玩家相应提示
			for(Integer playerId:playerList){
				BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
				if(session==null) continue;
				//清除session
				session.leaveMatch();
				//标记回到大厅
				session.setStatus(PlayerStatus.IN_HALL, StatusChangeReason.UNENROLL);
				DissmissMultiMatch.Builder pb = DissmissMultiMatch.newBuilder();
				PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.DissmissMultiMatch,pb.build().toByteString());
				//返还钻石
                ServiceRepo.hallPortalService.changeDiamond(playerId, this.matchConf.conditionInfo.enterCondition.minCoinLimit, false, ItemChangeReason.EXIT_MULTI_MATCH);
            }
			destroy();
			return;
		}
		lunNum++;
		//第n轮当前赛场玩家数
		everyLunStartDataCal();
		logger.info(lunKongNum+"========");
		logger.info(biSaiNum+"========");
		logger.info(taoTaiNum+"========");
		//比赛结束
		if(lastLunPlayerNum == 4){
			HashMap<Integer,Integer> playerScoreMapTemp = sortMapByValue(playerScoreMap);
			playerRankMap = initRankByScore(playerScoreMapTemp);
			for(Integer playerId:playerList){
				FinishMultiMatch.Builder pb = FinishMultiMatch.newBuilder();
				int rank = playerRankMap.get(playerId);
				pb.setRankNum(rank);
				pb.setRankNumAward((int)(totalPrizePool*(rank==1?0.5:(rank==2?0.3:0.2))));
				pb.setAwardType(0);
				logger.info("比赛结束当前玩家==="+playerId+"排名为"+ JsonFormat.printToString(pb.build()));
				PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.FinishMultiMatch,pb.build().toByteString());
			}
			for(Integer playerId:playerList){
				BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
				if(session==null) continue;
				//清除session
				session.leaveMatch();
				//标记回到大厅
				session.setStatus(PlayerStatus.IN_HALL, StatusChangeReason.UNENROLL);
			}
			//发送全局跑马等通知
			MarqueeMsg msg = new MarqueeMsg();
			msg.loopPushInterval = 1;
			msg.loopPushCount = 1;
			msg.marqueeType = 1;
			msg.msg = getMsgContent();
			msg.playSetting = "1x1";
			msg.receiver = -1;
			msg.senderId = -1;
			msg.senderName = "系统管理员";
			msg.startTime = System.currentTimeMillis();
			msg.stopTime = System.currentTimeMillis();
			msg.pushOnLogin = false;
			try {
				ServiceRepo.msgServicePortal.sendMarqueeMsg(msg);
			}catch (Exception e){
				logger.info("error======跑马灯发送失败"+this.id);
			}
			//邮件发放奖励
            sendAwardMail2Player();
			destroyRoom();
			destroy();
			for(CommonDesk desk : deskList){
				desk.destroy(DeskDestoryReason.GAME_OVER);
			}
			return;
		}
		biSaiFinishNum=0;
		//轮空玩家
		List<Integer> lunKongList = new ArrayList<>();
		//进行游戏玩家
		List<Integer> biSaiList = new ArrayList<>();
		//淘汰的玩家
		List<Integer> taoTaiList = new ArrayList<>();
		mergeMultiMatch(lunKongList,biSaiList,taoTaiList);
		logger.info("lunKongList"+lunKongList);
		logger.info("biSaiList"+biSaiList);
		logger.info("taoTaiList"+taoTaiList);
		//第一轮初始积分1000,以后:上一轮的积分*20%+500
		if(lunNum==1) {
		    //初始化奖池
		    initPrizePool();
			HashMap<Integer, Integer> playerScoreMapTemp = new HashMap<>();
			for (Integer playerId : playerList) {
				playerScoreMapTemp.put(playerId, 1000);
			}
			playerScoreMap = sortMapByValue(playerScoreMapTemp);
		}else{
			Map<Integer, Integer> playerScoreMapTemp = new HashMap<>();
			for(Map.Entry<Integer,Integer> entry:playerScoreMap.entrySet()){
				Integer score = entry.getValue();
				Integer playerId = entry.getKey();
				playerScoreMapTemp.put(playerId,(int)(score*0.2+500));
			}
			playerScoreMap = sortMapByValue(playerScoreMapTemp);
		}
		playerRankMap = initRankByScore(playerScoreMap);

		//给相应的玩家发送相应消息
		for(Integer playerId:lunKongList){
			MultiMatchStartNotify.Builder pb = MultiMatchStartNotify.newBuilder();
			pb.setLunNum(lunNum);
			pb.setType(PlayerMultiMatchStatus.LUN_KONG);
			pb.setRankNum(playerRankMap.get(playerId));
			PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.MultiMatchStartNotify,pb.build().toByteString());
			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
			//玩家掉线的情况下,获取不到session,创建一个假session,定义为离线,玩家上来能直接进入
			if(session==null){
				session = new BattleSession();
				User user = ServiceRepo.hallPortalService.getUser(playerId);
				session.user = user;
				PlayerInfo player = new PlayerInfo();
				UserHelper.copyUser2Player(user, player);
				session.player = player;
				session.userId = playerId;
				ServiceRepo.sessionManager.put2AnonymousList(session);
				ServiceRepo.sessionManager.put2OnlineList(playerId,session);
//				session.setStatus(PlayerStatus.GAMING,StatusChangeReason.PLAYER_OFFLINE_CREATE_SESSION);
				session.onlineStatus=OnlineStatus.OFFLINE;
				session.enterMatch(this);
			}
			session.setStatus(PlayerStatus.LUNKONG, StatusChangeReason.MULTIMATCH_LUNKONG);
		}
		for(Integer playerId:taoTaiList){
			MultiMatchStartNotify.Builder pb = MultiMatchStartNotify.newBuilder();
			pb.setLunNum(lunNum);
			pb.setType(PlayerMultiMatchStatus.TAO_TAI);
			pb.setRankNum(playerTaoTaiRankMap.get(playerId));
			PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.MultiMatchStartNotify,pb.build().toByteString());
			BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
			if(session==null) continue;
			//清除session
			session.leaveMatch();
			//标记回到大厅
			session.setStatus(PlayerStatus.IN_HALL, StatusChangeReason.UNENROLL);
		}
		//进行游戏的玩家分配桌子
		List<Integer> biSaiListTemp = new ArrayList();
		biSaiListTemp.addAll(biSaiList);
		List<List<Integer>> deskBiSaiList = new ArrayList<>();
		int deskNum = biSaiList.size()/4;
		for (int i = 0; i < deskNum; i++) {
			List<Integer> deskPlayerList = new ArrayList<>();
			deskPlayerList.add(biSaiListTemp.remove(0));
			deskPlayerList.add(biSaiListTemp.remove(0));
			deskPlayerList.add(biSaiListTemp.remove(0));
			deskPlayerList.add(biSaiListTemp.remove(0));
			deskBiSaiList.add(deskPlayerList);
		}
        for(Integer playerId:biSaiList){
            MultiMatchStartNotify.Builder pb = MultiMatchStartNotify.newBuilder();
            pb.setLunNum(lunNum);
            pb.setType(PlayerMultiMatchStatus.PLAY_MATCH);
            pb.setRankNum(playerRankMap.get(playerId));
            List<Integer> deskPlayerList = getDeskPlayerList(playerId,deskBiSaiList);
            for(Integer deskPlayerId:deskPlayerList){
                User user = ServiceRepo.hallPortalService.getUser(deskPlayerId);
                DeskPalyer.Builder deskPb = DeskPalyer.newBuilder();
                deskPb.setPlayerId(deskPlayerId);
                deskPb.setNickName(user.getNickname());
                deskPb.setPlayerScore(playerScoreMap.get(deskPlayerId));
                deskPb.setImgUrl(user.getHeadImg());
                pb.addDeskPlayer(deskPb);
            }
            PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.MultiMatchStartNotify,pb.build().toByteString());
        }

		//玩家积分信息,同步排行榜
		sendMultiMatchRank();
        //组织进行游戏的玩家加入桌子
		for (List<Integer> deskPlayerList:deskBiSaiList){
			CommonDesk d = null;
			List<PlayerInfo> playerInfoList = new ArrayList<>();
            for (Integer playerId:deskPlayerList){
                BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
                //玩家掉线的情况下,获取不到session,创建一个假session,定义为离线,玩家上来能直接进入
                if(session==null){
                	session = new BattleSession();
                	User user = ServiceRepo.hallPortalService.getUser(playerId);
                	session.user = user;
					PlayerInfo player = new PlayerInfo();
					UserHelper.copyUser2Player(user, player);
					session.player = player;
					session.userId = playerId;
					ServiceRepo.sessionManager.put2AnonymousList(session);
					ServiceRepo.sessionManager.put2OnlineList(playerId,session);
					session.setStatus(PlayerStatus.GAMING,StatusChangeReason.PLAYER_OFFLINE_CREATE_SESSION);
					session.onlineStatus=OnlineStatus.OFFLINE;
					session.enterMatch(this);
				}
                logger.info(session.onlineStatus+"==============session.onlineStatus");
                BattleContext ctx = BattleContext.create(session).setGameId(this.getParent().getId()).setMatchId(this.id).setRoomId("31011");
                this.enterRoom(ctx);
                logger.info("ctx.getDeskId()-----------"+ctx.getDeskId());
                logger.info("session------------"+session);
                //该场次桌子都是自动准备的
				d = session.getDeskMap().get(ctx.getDeskId());
                d.onPlayerReadyPacketReceived(session.userId);
                deskList.add(d);
                if(session.onlineStatus==OnlineStatus.OFFLINE){
					d.onPlayerHangupPacketReceived(playerId);
				}
				playerInfoList.add(session.player);
            }
            if(d != null && lunNum > 1) {
				d.multiMatchResetAndStart(playerInfoList);
			}
		}
	}

    private void initPrizePool() {
        totalPrizePool = (int)((playerList.size()*this.matchConf.conditionInfo.enterCondition.minCoinLimit)*0.9);
	}

    private void sendAwardMail2Player() {
        Integer first = 0;
        Integer second = 0;
        Integer third = 0;
        for(Integer playerId:playerRankMap.keySet()){
            if(playerRankMap.get(playerId)==1){
                first = playerId;
            }else if(playerRankMap.get(playerId)==2){
                second = playerId;
            }else if(playerRankMap.get(playerId)==3){
                third = playerId;
            }
        }

        List<Integer> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);
        for(Integer playerId:list){
            User user = ServiceRepo.hallPortalService.getUser(playerId);
            Integer rank = playerRankMap.get(playerId);
            int awardNum = (int)(totalPrizePool*(rank==1?0.5:(rank==2?0.3:0.2)));
			Award award = new Award();
			List<ItemPkg> itemlist = new ArrayList<>();
			ItemPkg pkg = new ItemPkg();
			pkg.itemId = "A001";
			pkg.count = awardNum;
			itemlist.add(pkg);
			award.setItems(new Gson().toJson(itemlist));
			award.setInvalidTime(new Date(getCurrYearLast()));
			award.setSrcSystem("mailSys");
			award.setAwardNote("比赛场排行奖励");
			award.setAwardType((1));
			award.setReceiverId(playerId);
			award.setCtime(new Date());
			award.setAwardReason(ItemChangeReason.RankAward.toString());
			long awardId = ServiceRepo.hallPortalService.addAward(award);

			Msg a = new Msg();
			a.setMsg("过五关斩六将,您在1分钟钻石赛中获得了第"+rank+"名,特赠予您"+awardNum+"钻石,请大侠务必收下");
			a.setMsgMainType(0);
			a.setPriority(0);
			a.setRewardId(awardId);
			a.setSenderId(-1);
			a.setSenderName("系统");
			a.setStartDateTime(new Date());
			a.setStopDateTime(new Date(getCurrYearLast()));
			a.setTargetType(1);
			a.setTitle("1分钟钻石赛排名奖励");
			a.setAttachNum(1);
			a.setTargetId(playerId);
			a.setStatus(1);
			a.setItemCount(awardNum);
			a.setItemId("A001");
			a.setId(ServiceRepo.hallPortalService.insertMsg(a));

			UserMsg userMsg = new UserMsg();
			userMsg.setAwardId(awardId);
			userMsg.setDeled(false);
			userMsg.setReaded(false);
			userMsg.setMsgId(a.getId());
			userMsg.setReceived(false);
			userMsg.setUserId(playerId);
			userMsg.setMtime(new Date());
			userMsg.setCtime(new Date());
			long id = ServiceRepo.hallPortalService.insertUserMsg(userMsg);

            try {
                ServiceRepo.msgServicePortal.sendMail(a.getId());
            }catch (Exception e){
                logger.info("奖励邮件发送失败"+this.id+"玩家为==="+user.getNickname()+"====="+user.getId());
            }
        }
    }

	private long getCurrYearLast() {
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, currentYear+3);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime().getTime();
	}

	private String getMsgContent() {
		Integer first = 0;
		Integer second = 0;
		Integer third = 0;
		for(Integer playerId:playerRankMap.keySet()){
			if(playerRankMap.get(playerId)==1){
				first = playerId;
			}else if(playerRankMap.get(playerId)==2){
				second = playerId;
			}else if(playerRankMap.get(playerId)==3){
				third = playerId;
			}
		}
        User user1 = ServiceRepo.hallPortalService.getUser(first);
        User user2 = ServiceRepo.hallPortalService.getUser(second);
        User user3 = ServiceRepo.hallPortalService.getUser(third);

        StringBuilder content = new StringBuilder();
		content.append("华山之巅,群雄纷争,玩家");
		content.append(user1.getNickname()+","+user2.getNickname()+","+user3.getNickname());
		content.append("智勇双全,所向披靡,分别获得了第一名,第二名,第三名,比赛大奖将会通过邮件发送,请注意查收");
		return content.toString();
	}

	private void destroyRoom() {
		MultiRoomImpl multiRoom = (MultiRoomImpl)this.roomMap.get("31011");
		multiRoom.destroyAllDesk(DeskDestoryReason.MULTIMATCH_OVER);
	}

	public boolean isAllBiSaiDeskFinishOneLun(){
		logger.info(biSaiFinishNum+"=============biSaiFinishNum");
		logger.info(biSaiNum+"=============biSaiNum");
		logger.info(this.matchConf.juNum+"=============this.matchConf.juNum");
		return biSaiFinishNum >= biSaiNum/4*this.matchConf.juNum;
	}

	public void updatePlayerScoreAndRank(Map<Integer,Integer> multiMatchScoreMap) {
		Map<Integer,Integer> playerScoreMapTemp = new HashMap();
		Map<Integer,Integer> playerScoreMapTemp2 = new HashMap();
		playerScoreMapTemp.putAll(playerScoreMap);
		playerScoreMapTemp2.putAll(playerScoreMap);
		for(Map.Entry<Integer,Integer> entry:playerScoreMapTemp.entrySet()){
			int playerId = entry.getKey();
			int score = entry.getValue()+(multiMatchScoreMap.get(playerId)==null?0:multiMatchScoreMap.get(playerId));
			playerScoreMapTemp2.put(playerId,score);
		}
		playerScoreMap = sortMapByValue(playerScoreMapTemp2);
		logger.info("playerScoreMap======"+playerScoreMap);
		playerRankMap = initRankByScore(playerScoreMap);
		logger.info("playerRankMap======"+playerRankMap);
		biSaiFinishNum++;
		sendMultiMatchRank();
	}

	public void sendMultiMatchRank() {
		List<PlayerScoreInMatch> scoreList = getPlayerScoreInMatches();
		logger.info("scoreList============"+scoreList.toString());
		for(Integer playerId:playerList){
			MultiMatchRankSyn.Builder pb= MultiMatchRankSyn.newBuilder();
			pb.addAllScores(scoreList);
			PushService.instance.pushPBMsg(playerId, MsgBase.PacketType.MultiMatchRankSyn,pb.build().toByteString());
		}
	}

	public List<PlayerScoreInMatch> getPlayerScoreInMatches() {
		List<PlayerScoreInMatch> scorePbList = new ArrayList<>();
		for(Integer playerId:playerRankMap.keySet()){
			PlayerScoreInMatch.Builder scorePb = PlayerScoreInMatch.newBuilder();
			User user = ServiceRepo.hallPortalService.getUser(playerId);
			scorePb.setPlayerId(playerId);
			scorePb.setPlayerName(user.getNickname());
			scorePb.setPlayerImgUrl(user.getHeadImg());
			scorePb.setPlayerScore(playerScoreMap.get(playerId));
			scorePb.setRankNum(playerRankMap.get(playerId));
			scorePbList.add(scorePb.build());
		}
		return scorePbList;
	}


	private List<Integer> getDeskPlayerList(Integer playerId,List<List<Integer>> deskBiSaiList) {
	    for (List<Integer> l:deskBiSaiList){
	        if(l.contains(playerId)) return l;
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, currentYear);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date currYearLast = calendar.getTime();
        System.out.println(DateFormatUtils.format(currYearLast,"yyyy-MM-dd HH:mm:ss"));
    }
	/**
	 * 每轮完成后筛选被淘汰的玩家并移除
	 * 先获取上一轮的玩家数,n
	 * 晋级规则:
	 * 		1,比赛开始时,未满4人直接解散本次赛事
	 * 		2,整体规则为,取半晋级,轮空直接晋级
	 *
	 * 			晋级人数(currentLunPlayerNum)   轮空(lunKongNum)    淘汰(taoTaiNum)           取排名多少晋级       总人数(lastLunPlayerNum)
	 * 	第一轮	n/2-(n/2)%4	    			   n%4               n-(currentLunPlayerNum)   n/2-(n/2)%4-n%4    n
	 * 	第二轮	以后的n= n/2-(n/2)%4
	 */
	private void everyLunStartDataCal(){
		if(lunNum==1){
			lastLunPlayerNum = 0;
			currentLunPlayerNum = playerList.size();
			taoTaiNum = 0;
		}else{
			lastLunPlayerNum = currentLunPlayerNum;
			currentLunPlayerNum = lastLunPlayerNum/2<4?4:(lastLunPlayerNum/2 - (lastLunPlayerNum/2)%4);
			taoTaiNum = lastLunPlayerNum - currentLunPlayerNum;
		}
		lunKongNum = currentLunPlayerNum%4;
		biSaiNum = currentLunPlayerNum - lunKongNum;
	}

	/**
	 * 计算赛场第N轮的数据
	 * 最后一轮特殊计算
	 * 根据积分先筛除被淘汰的玩家,得知晋级玩家
	 * 然后在晋级的玩家中筛除轮空的人,得知比赛玩家
	 * @param lunKongList 轮空玩家
	 * @param biSaiList  参加比赛玩家
	 * @param taoTaiList 淘汰的玩家
	 */
	private void mergeMultiMatch(List<Integer> lunKongList, List<Integer> biSaiList, List<Integer> taoTaiList) {
//		if(currentLunPlayerNum > 4){
			int rankNum = playerRankMap.size();
			for (int i = 0; i < taoTaiNum; i++) {
				for (Map.Entry<Integer,Integer> rank:playerRankMap.entrySet()) {
					if(rank.getValue()==rankNum-i){
						taoTaiList.add(rank.getKey());
					}
				}
			}
			playerList.removeAll(taoTaiList);
			for(Integer playerId:taoTaiList){
				playerScoreMap.remove(playerId);
				playerTaoTaiRankMap.put(playerId,playerRankMap.get(playerId));
				playerRankMap.remove(playerId);
			}

			List<Integer> playerListTemp = new ArrayList<>();
			playerListTemp.addAll(playerList);
			for (int i = 0; i < lunKongNum; i++) {
				lunKongList.add(playerListTemp.remove((int)(Math.random()*currentLunPlayerNum)));
			}
			biSaiList.addAll(playerListTemp);
//		}else{
//			biSaiList.addAll(playerList);
//		}
	}

//	private void removePlayerById(Integer playerId) {
//		Iterator<Map.Entry<Integer, Integer>> it = playerScoreMap.entrySet().iterator();
//		while(it.hasNext()){
//			Map.Entry<Integer, Integer> entry=it.next();
//			int key=entry.getKey();
//			if(key==playerId){
//				System.out.println("delete this: "+key+" = "+key);
//				it.remove();
//				return;
//			}
//		}
//	}


	@Override
	public synchronized EnrollResult enroll(BattleContext ctx) {
		if(playerList.contains(ctx.playerId)){
			return EnrollResult.success(this.id,ctx.roomId);
		}
		BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);
		if (session == null) {
			logger.error("act=enroll;error=sessionMiss;playerId={};matchId={};", ctx.playerId, getId());
			return EnrollResult.fail("会话超时,请重新登录");
		}
		PlayerInfo player = session.player;
		EnterCondition condition = matchConf.conditionInfo.enterCondition;

		if (condition.minCoinLimit > 0 && player.diamond < condition.minCoinLimit) {
			logger.info("act=enroll;error=diamondLess;expect={};actual={};playerId={};matchId={};", condition.minCoinLimit, player.diamond, ctx.playerId, matchConf.matchID);
			return EnrollResult.fail("你的钻石数不足,还差" + (condition.minCoinLimit - player.diamond) + "钻石");
		}

		if (condition.maxCoinLimit > 0 && player.diamond > condition.maxCoinLimit) {
			logger.info("act=enroll;error=diamondFlowout;expect={};actual={};playerId={};matchId={};", condition.maxCoinLimit, player.diamond, player.playerId, matchConf.matchID);
			return EnrollResult.fail("你的钻石数已经超过本场最大限制,推荐前往其它场");
		}

		logger.info("ctx.roomid========================="+ctx.roomId);
		Room room = roomMap.get(ctx.roomId);
		Result ret = room.playerEnroll(ctx);

		//判断玩家报名条件是否满足
		if (ret.isOk()) {
			if(condition.minCoinLimit>0){
                ServiceRepo.hallPortalService.changeDiamond(ctx.playerId, -1*condition.minCoinLimit, false, ItemChangeReason.ENROLL_MULTI_MATCH);
            }
			//将玩家准备信息存储在Map中
			playerList.add(ctx.playerId);
			return EnrollResult.success(this.id,ctx.roomId);
		}

		return EnrollResult.fail("进入失败,没有满足报名条件的房间");
	}

	private HashMap<Integer,Integer> initRankByScore(Map<Integer, Integer> playerScoreMap) {
		HashMap<Integer,Integer> map = new HashMap<>();
		int i = 0;
		TreeMap<Integer,Integer> treeMap = new TreeMap<>(new ValueComparator(playerScoreMap));
		treeMap.putAll(playerScoreMap);
		for(Map.Entry<Integer,Integer> entry:treeMap.entrySet()){
			map.put(entry.getKey(),++i);
		}
		return map;
	}

	@Override
	public synchronized EnterRoomResult enterRoom(BattleContext ctx) {
		if (isFull()) {
			logger.error("act=enterRoom;error=matchfull;playerId={};roomId={};matchId={};", ctx.playerId, ctx.roomId, ctx.matchId);
			return EnterRoomResult.fail("赛场人数已满");
		}
		Room room = roomMap.get(ctx.getRoomId());
		if (room == null) {
			logger.error("act=enterRoom;error=roomMiss;playerId={};roomId={};matchId={};", ctx.playerId, ctx.roomId, ctx.matchId);
			return EnterRoomResult.fail("房间不存在");
		}
		EnterRoomResult res = room.playerTryEnter(ctx);
		if (res.isOk()) {
			res = room.playerTrySit(ctx);
		}

		return res;
	}

	@Override
	public void onPlayerCountDecr(int playerId) {
		playerCount--;
	}


	@Override
	public Result unEnroll(int playerId) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("act=unEnroll;error=sessionMiss;playerId={};matchId={};", playerId, getId());
			return Result.fail("用户会话失效");
		}
		if(biSaiNum>0){
			return Result.fail("比赛已开始,不能退出");
		}
		//清除session
		session.leaveMatch();
		//标记回到大厅
		session.setStatus(PlayerStatus.IN_HALL, StatusChangeReason.UNENROLL);
		//离开赛场
		playerList.remove(Integer.valueOf(playerId));
		return Result.success();
	}

	@Override
	public Result playerExit(int playerId, PlayerExitType reason) {
		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
		if (session == null) {
			logger.error("act=playerExit;error=sessionMiss;playerId={};matchId={};", playerId, getId());
			return Result.fail("用户会话失效");
		}
		Room room = session.getRoom();
		if (room == null) {
			// playerCount --;
			session.leaveMatch();
			return Result.success();
		}

		room.playerExit(playerId, reason);

		return Result.success();
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return playerCount == 0;
	}

	@Override
	public void init(MatchConfig conf) {
		this.matchConf = conf;
		this.id = matchConf.matchID;
		
		logger.info("act=matchInit;matchId={};", matchConf.matchID);

		for (RoomConfig roomConf : conf.conditionInfo.roomArray) {
			try {
				Room room = roomMap.get(roomConf.roomId);
				if(room == null) {
					if (StringUtils.isNotBlank(roomConf.roomClassFullName)) {
						Class<?> cls = getClass().getClassLoader().loadClass(roomConf.roomClassFullName);
						Constructor<?> c = cls.getConstructor(Match.class);
						room = (Room) c.newInstance(this);
					} else {
						room = new MultiRoomImpl(this);
					}
//					Monitor.add2Monitor(room);
				}

				room.init(roomConf);
				roomMap.put(room.getRoomId(), room);				
			} catch (Exception e) {
				logger.error("act=matchInit;error=exception;", e);
			}
		}
	}

	@Override
	public void destroy() {
		Game game = this.getParent();
		logger.info("销毁前========"+game.getMatchMap().keySet().toString());
		game.getMatchMap().remove(this.id);
		logger.info("销毁后========"+game.getMatchMap().keySet().toString());
	}

	@Override
	public Room getRoom(String roomId) {
		return roomMap.get(roomId);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return matchConf.matchName;
	}

	@Override
	public List<DeskModel> getDeskList() {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getDeskList());
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubCommonDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getClubCommonDesk(clubId));
		}
		return list;
	}

	@Override
	public List<DeskModel> getClubJiFenDesk(long clubId) {
		List<DeskModel> list = new ArrayList<DeskModel>();
		for(Room room : this.roomMap.values()) {
			list.addAll(room.getClubJiFenDesk(clubId));
		}
		return list;
	}


	@Override
	public MatchConfig getMatchConfig() {
		return this.matchConf;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	@Override
	public int getPlayerCount() {
		return playerCount;
	}

	@Override
	public void onPlayerCountIncr(int playerId) {
		playerCount++;
	}

	@Override
	public Map<String, Integer> getPlayerMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String matchId : roomMap.keySet()) {
			map.put(matchId, roomMap.get(matchId).getPlayerCount());
		}
		return map;
	}

	@Override
	public Map<String, Map<String, Double>> getDeskDelayStatus() {
		Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();
		for (String deskId : roomMap.keySet()) {
			map.put(deskId + "", roomMap.get(deskId).getDeskDelayStatsu());
		}
		return map;
	}

	@Override
	public CommonDesk findDesk(String deskId) {
		for(Room r : this.roomMap.values()) {
			CommonDesk desk = r.getById(deskId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}
	
	@Override
	public DeskModel findDesk(int playerId) {
		for(Room r : this.roomMap.values()) {
			DeskModel desk = r.findDesk(playerId);
			if(desk != null) {
				return desk;
			}
		}
		return null;
	}

	@Override
	public List<RoomOnlineModel> getRoomOnlineList() {
		List<RoomOnlineModel> list = new ArrayList<RoomOnlineModel>();
		for(Room r : this.roomMap.values()) {
			 RoomOnlineModel model = new RoomOnlineModel();
			 model.serverInstanceId = ServiceRepo.serverConfig.instanceId;
			 model.gameId = this.getParent().getId();
			 model.matchId = this.getId();
			 model.roomId = r.getRoomId();
			 model.lastUpdate = System.currentTimeMillis();
			 model.onlineNum = r.getPlayerCount();
			 list.add(model);
		}
		return list;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getInitTime() {
		return initTime;
	}
	@Override
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public ConcurrentMap<String, Room> getRoomMap() {
		return roomMap;
	}

	public void setRoomMap(ConcurrentMap<String, Room> roomMap) {
		this.roomMap = roomMap;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public MatchConfig getMatchConf() {
		return matchConf;
	}

	public void setMatchConf(MatchConfig matchConf) {
		this.matchConf = matchConf;
	}

	public int getLunNum() {
		return lunNum;
	}

	public void setLunNum(int lunNum) {
		this.lunNum = lunNum;
	}

	public int getLastLunPlayerNum() {
		return lastLunPlayerNum;
	}

	public void setLastLunPlayerNum(int lastLunPlayerNum) {
		this.lastLunPlayerNum = lastLunPlayerNum;
	}

	public int getCurrentLunPlayerNum() {
		return currentLunPlayerNum;
	}

	public void setCurrentLunPlayerNum(int currentLunPlayerNum) {
		this.currentLunPlayerNum = currentLunPlayerNum;
	}

	public int getLunKongNum() {
		return lunKongNum;
	}

	public void setLunKongNum(int lunKongNum) {
		this.lunKongNum = lunKongNum;
	}

	public int getTaoTaiNum() {
		return taoTaiNum;
	}

	public void setTaoTaiNum(int taoTaiNum) {
		this.taoTaiNum = taoTaiNum;
	}

	public int getBiSaiNum() {
		return biSaiNum;
	}

	public void setBiSaiNum(int biSaiNum) {
		this.biSaiNum = biSaiNum;
	}

	public int getBiSaiFinishNum() {
		return biSaiFinishNum;
	}

	public void setBiSaiFinishNum(int biSaiFinishNum) {
		this.biSaiFinishNum = biSaiFinishNum;
	}

	public List<Integer> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<Integer> playerList) {
		this.playerList = playerList;
	}

	public HashMap<Integer, Integer> getPlayerScoreMap() {
		return playerScoreMap;
	}

	public void setPlayerScoreMap(HashMap<Integer, Integer> playerScoreMap) {
		this.playerScoreMap = playerScoreMap;
	}

	public HashMap<Integer, Integer> getPlayerRankMap() {
		return playerRankMap;
	}

	public void setPlayerRankMap(HashMap<Integer, Integer> playerRankMap) {
		this.playerRankMap = playerRankMap;
	}

	public HashMap<Integer, Integer> getPlayerTaoTaiRankMap() {
		return playerTaoTaiRankMap;
	}

	public void setPlayerTaoTaiRankMap(HashMap<Integer, Integer> playerTaoTaiRankMap) {
		this.playerTaoTaiRankMap = playerTaoTaiRankMap;
	}

	public Timer getTimer() {
		return timer;
	}
}
