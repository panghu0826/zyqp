package com.buding.battle.logic.module.game.service;

import com.buding.api.game.PokerWanfa;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.*;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.DeskDestoryReason;
import com.buding.battle.logic.module.desk.bo.DeskImpl;
import com.buding.battle.logic.module.desk.bo.VipDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.cache.RedisClient;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.Club;
import com.buding.db.model.ClubUser;
import com.buding.db.model.User;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.DeskFee;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.common.constants.RoomState;
import com.buding.hall.module.game.model.CLubWanfaModel;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.hall.module.ws.HallPortalService;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.MsgGame;
import packet.game.MsgGame.VipRoomListSyn;
import packet.game.MsgGame.VipRoomModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class VipService {
	private Logger log = LogManager.getLogger("DESKLOG");
	@Autowired
	GameService gameService;

	@Autowired
	UserRoomDao userRoomDao;

	@Autowired
	PushService pushService;

	@Autowired
	HallPortalService hallService;

	@Autowired
	ConfigManager configManager;
	
	@Autowired
	BattleSessionManager battleSessionManager;

	@Autowired
    RedisClient redisClient;

	private Gson gson = new Gson();

	public Result enroll(BattleSession session, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if(ur == null) {
			return Result.fail("房间不存在");
		}
//		JSONObject obj = JSONObject.fromObject(ur.getParams());
//		int fee = obj.optInt("fee");
//		int clubRoomType = obj.getInt("clubRoomType");
//		if(clubRoomType != Constants.CLUB_JI_FEN_DESK) {
////			if (session.userId != ur.getOwnerId()) {
//				Result r = ServiceRepo.hallPortalService.hasEnoughCurrency(session.userId, CurrencyType.diamond, fee);
//				if (r.isFail()) {
//					return Result.fail("钻石不足");
//				}
////			}
//		}
		return gameService.enroll(session, BattleContext.create(session).setGameId(ur.getGameId()).setMatchId(ur.getMatchId()).setDeskId(roomCode));
	}
	
	public Result createVipRoom(int playerId, MsgGame.CreateVipRoomRequest request) {
		String matchId = request.getMatchId();
		int quanNum = request.getQuanNum();
		int vipRoomType = request.getVipRoomType();
		int wanfa = request.getWangfa();
		int limitMax = request.getLimitMax();
		int menNum = request.getMenNum();
		int yaZhu = request.getYaZhu();
		long clubId = request.getClubId();
		int clubRoomType = request.getClubRoomType();
		int qiangZhuangNum = request.getQiangZhuangNum();
		String niuFanStr = request.getNiuFanStr();
		int erBaGameType = request.getErBaGameType();
		int enterScore = -1;
        int canFufen = -1;
        int choushuiScore = -1;
        int choushuiNum = -1;
        int zengsongNum = -1;
		if(clubId > 0){
			Club club = ServiceRepo.clubDao.selectClub(clubId);
            CLubWanfaModel model = gson.fromJson(club.getClubWanfa(),CLubWanfaModel.class);
			wanfa = model.getWanfa();
            matchId = model.getMatchID();
            quanNum = model.getJuNum();
            vipRoomType = model.getPlayerNum();
            limitMax = model.getFengDing();
            menNum = model.getBiMen();
            yaZhu = model.getYazhu();
            qiangZhuangNum = model.getQiangZhuangNum();
            niuFanStr = model.getNiuFanStr();
			erBaGameType = model.getErBaGameType();

			if(clubRoomType == Constants.CLUB_JI_FEN_DESK) {
                enterScore = club.getEnterScore();
                canFufen = club.getCanFufen();
                choushuiScore = club.getChoushuiScore();
                choushuiNum = club.getChoushuiNum();
                zengsongNum = club.getZengsongNum();
            }
		}

		User user = hallService.getUser(playerId);
		if (user == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "用户不存在");
			return Result.fail("用户不存在");
		}

		MatchConfig matchConf = configManager.getMatchConfById(matchId);
		RoomConfig roomConf = matchConf.conditionInfo.roomArray[0];
		DeskFee fee = null;
		for (DeskFee df : roomConf.fee) {
			if (df.quanCount == quanNum) {
				fee = df;
				break;
			}
		}
		if (fee == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "quanNum参数不对:" + quanNum);
            return Result.fail("quanNum参数不对");

        }
        if(clubRoomType != Constants.CLUB_JI_FEN_DESK) {
            Result r = hallService.hasEnoughCurrency(playerId, CurrencyType.diamond, fee.diamondCount);
            if (r.isFail()) {
                pushService.pushCreateVipRoomRsp(playerId, false, "钻石不足");
                return Result.fail("钻石不足");

            }
        }

		UserRoom room = new UserRoom();
		room.setMatchId(matchId);
		room.setGameId(matchConf.game.gameId);
		String code = hallService.genRoomUniqCode();
		if (code == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "生成房间编号失败");
			return Result.fail("生成房间编号失败");
		}
		room.setRoomCode(code); // 编号系统生成
		JSONObject json = new JSONObject();
		json.put("quanNum", fee.quanCount);
		json.put("vipRoomType", vipRoomType);
		json.put("fee", fee.diamondCount);
		json.put("menNum", menNum);
		json.put("yaZhu", yaZhu);
		json.put("clubId", clubId);
		json.put("clubRoomType", clubRoomType);
		json.put("enterScore", enterScore);
		json.put("canFufen", canFufen);
		json.put("choushuiScore", choushuiScore);
		json.put("choushuiNum", choushuiNum);
		json.put("zengsongNum", zengsongNum);
		json.put("canZhongTuNotEnter", (wanfa & PokerWanfa.ZJH_ZHONG_TU_JIN_RU) == PokerWanfa.ZJH_ZHONG_TU_JIN_RU ? 1:0);
		json.put("canCuoPai", (wanfa & PokerWanfa.ZJH_CAN_CUO_PAI) == PokerWanfa.ZJH_CAN_CUO_PAI);
		json.put("qiangZhuangNum", qiangZhuangNum);
		json.put("niuFanStr", niuFanStr);
		json.put("erBaGameType", erBaGameType);
		room.setParams(json.toString());
		room.setOwnerId(playerId);
		room.setWanfa(wanfa + "");
		room.setRoomName(code);
		room.setLimitMax(limitMax);

		initRoom(room);
		room.setRoomState(RoomState.ACTIVE);

		Result result = hallService.addRoom(room);
		pushService.pushCreateVipRoomRsp(playerId, result.isOk(), result.msg);
		if(result.isOk()){
		    result.msg = code;
		    return result;
        }else{
		    return result;
        }
	}

    private void initRoom(UserRoom room) {
		MatchConfig conf = configManager.getMatchConfById(room.getMatchId());
		if (conf == null) {
			TResult.fail1("赛场配置不存在");
			return;
		}

		room.setCtime(new Date());
		room.setMtime(new Date());

		RoomConfig rc = conf.conditionInfo.roomArray[0];

		room.setRoomConfId(rc.roomId);
		room.setRoomState(RoomState.ACTIVE);
		TResult.sucess1(room);
	}
	
	public void kick(int playerId, int tokickPlayerId, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if (ur == null) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间不存在");
			return;
		}
		if(ur.getOwnerId() != playerId) {
			pushService.pushDismissVipRoomResponse(playerId, false, "你不是房主");
			return;
		}
		Game game = gameService.getById(ur.getGameId());
		Match m = game.getMatch(ur.getMatchId());
		Room room = m.getRoom(ur.getRoomConfId());
		CommonDesk<?> desk = room.getById(ur.getRoomCode());
		if (desk.getStatus() == DeskStatus.GAMING) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间已开始游戏，无法踢人");
			return;
		}
		desk.onKickoutPacketReceived(playerId, tokickPlayerId);
	}
	
	public void dissmissVipRoom(int playerId, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if (ur == null) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间不存在");
			return;
		}
		Game game = gameService.getById(ur.getGameId());
		Match m = game.getMatch(ur.getMatchId());
		Room room = m.getRoom(ur.getRoomConfId());
		CommonDesk<?> desk = room.getById(ur.getRoomCode());
		List<Integer> canDissmissPlayers = new ArrayList<>();
//		canDissmissPlayers.add(ur.getOwnerId());
		if(desk.getClubId() > 0){
			List<ClubUser> managerlist = ServiceRepo.clubDao.selectClubAllManageUser(desk.getClubId());
			for(ClubUser user : managerlist){
				canDissmissPlayers.add(user.getClubMemberId());
			}
		}
		if(!canDissmissPlayers.contains(playerId)) {
			pushService.pushDismissVipRoomResponse(playerId, false, "没有权限解散");
			return;
		}
		if (desk != null) {
            if(desk instanceof DeskImpl){
				log.info("桌子id--"+desk.getDeskID()+"--"+"玩家解散房间");
				DeskImpl d = (DeskImpl) desk;
				try {
					d.game.gameDismiss();
				}catch (Exception e){
					d.destroy(DeskDestoryReason.FAIL_SAFE_CLOSE);
				}
			}
		}
		ur.setRoomState(RoomState.CLOSE);
		this.userRoomDao.updateUserRoom(ur);
	}
	
	public void pushVipRoomList(int playerId) {
		BattleSession session = battleSessionManager.getIoSession(playerId);
		if(session == null) return;

		VipRoomListSyn.Builder vb = VipRoomListSyn.newBuilder();
		for (Map.Entry<String,CommonDesk> entry : session.getDeskMap().entrySet()) {
			VipRoomModel.Builder model = VipRoomModel.newBuilder();
			CommonDesk desk = entry.getValue();
			if(!(desk instanceof VipDesk)) continue;
			VipDesk vipDesk = (VipDesk)desk;
			if(desk.getStatus() == DeskStatus.DESTROYED) continue;
			model.setDeskId(vipDesk.getDeskID());
			model.setRoomType(vipDesk.getClubId() > 0 ? vipDesk.getClubRoomType(): -1);
			model.setJuNum(vipDesk.getTotalQuan());
			model.setCurJuNum(vipDesk.getGameCount());
			model.setPlayerCount(vipDesk.getPlayerCount());
			model.setWanfa(vipDesk.getWanfa());
			model.setLimitMax(vipDesk.getLimitMax());
			model.setGameId(vipDesk.getGameId());
			model.setClubId(vipDesk.getClubId());
			model.setMatchId(vipDesk.getMatchId());
			vb.addRoomList(model);
		}
		pushService.pushVipRoomListSyn(playerId, vb);
	}
}
