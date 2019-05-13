package com.buding.battle.logic.module.user.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.battle.logic.module.match.MultiMatchImpl;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.util.Util;
import com.buding.common.randomName.RandomNameService;
import com.buding.common.token.TokenClient;
import com.buding.db.model.User;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class LoginService {
	Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	BattleSessionManager sessionManager;

	@Autowired
	PushService pushService;

	@Autowired
	HallPortalService hallService;

	@Autowired
	GameService gameService;

	@Autowired
	UserDao userDao;

	@Autowired
	RandomNameService randomNameService;

	@Autowired
	TokenClient tokenClient;

	AtomicInteger idGen = new AtomicInteger(10001);

	boolean testMode = true;
	
	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	public void auth(BattleSession session, int userId, String token) {
		try {
//			token = userSecurityHelper.decrypt(token);
			
			// 会话验证
			if (!tokenClient.verifyToken(userId, token)) {
				logger.error("会话无效");
//				pushService.pushLoginRsp(session, false, null, "会话无效");
				session.channel.close();
				return;
			}
			
			User user = hallService.getUser(userId);
			init(session, user);
		} catch (Exception e) {
			logger.error("无法登入比赛服务器", e);
			session.channel.close();
//			pushService.pushLoginRsp(session, false, null, "无法登入比赛服务器");
		}
	}

	private void init(BattleSession session, User user) {
		PlayerInfo p = new PlayerInfo();
		// 检查是否需要踢出同帐号的用户
		BattleSession oldSession = sessionManager.getIoSession(user.getId());
		
		//该代码待验证
		if(oldSession == session && oldSession.player != null) {
			pushService.pushLoginRsp(session, true, session.player, null);
			return;
		}
		

		UserHelper.copyUser2Player(user, p);
		Util.initSession(session, p, user);
		session.currentModule = ServiceRepo.matchModule;// 进入大厅
		pushService.pushLoginRsp(session, true, session.player, null);

		pushService.pushKickoutSyn(session,user.getId(),"请返回大厅等待");
		// 检查是否需要直接进入游戏
		if (oldSession != null) {
			session.setDeskMap(oldSession.getDeskMap());
			CommonDesk desk = oldSession.getPlayingOrReadyDesk(session.userId);
			if(desk != null) {
				PlayerStatus status = PlayerStatus.UNREADY;
				if (oldSession.getStatus() == PlayerStatus.GAMING) {
					status = PlayerStatus.GAMING;
				}
				if (oldSession.getStatus() == PlayerStatus.READY) {
					status = PlayerStatus.READY;
				}
				session.setStatus(status, StatusChangeReason.PLAYER_RECONNECT_GAMING);
				session.enterMatch(oldSession.getMatch());
				session.enterRoom(oldSession.getRoom());
				session.enterDesk(desk);
//				session.recentDeskId = oldSession.recentDeskId;
				session.currentModule = ServiceRepo.gameModule;
				session.player = oldSession.player;
				session.user = oldSession.user;
				session.player.position = desk.getGuard().getPosById(user.getId());
				if (oldSession.awayStatus == AwayStatus.AWAY) {
					logger.info("session status:{}", session.onlineStatus);
					Util.userHasLogin(session);
					desk.onPlayerComeBackPacketReceived(session.userId);
					if (oldSession.channel != null && oldSession.channel.isOpen()) {
						// 通知旧连接下线
						logger.info("user {} be kickout as other deviced login ", user.getId());
						oldSession.channel.close();
					}
					return;
				} else {
					logger.info("session status:{}", session.onlineStatus);
					Util.userHasLogin(session);
					desk.onPlayerReconnectPacketReceived(session.userId);
					if (oldSession.channel != null && oldSession.channel.isOpen()) {
						// 通知旧连接下线
						logger.info("user {} be kickout as other deviced login ", user.getId());
						oldSession.channel.close();
					}
					return;
				}
			}else{
				//重登录顶掉号如果有报名比赛发送同步消息
				if (oldSession.getStatus() == PlayerStatus.LUNKONG || (oldSession.getMatch()!=null&&oldSession.getMatch() instanceof MultiMatchImpl)) {
					logger.info("session status:{}", session.onlineStatus);
					Util.userHasLogin(session);
					PlayerStatus status = oldSession.getStatus();
					session.setStatus(status, StatusChangeReason.PLAYER_RECONNECT_GAMING);
					logger.info("oldSession.getMatchId========"+oldSession.getMatchId());
					session.enterMatch(oldSession.getMatch());
					session.currentModule = ServiceRepo.matchModule;
					pushService.multiMatchEnrollSynWithLogin(session,session.getGameId(),session.getMatchId(),status==PlayerStatus.LUNKONG?1:0);
					if (oldSession.channel != null && oldSession.channel.isOpen()) {
						// 通知旧连接下线
						logger.info("user {} be kickout as other deviced login ", user.getId());
						oldSession.channel.close();
					}
					return;
				}
			}
		}else{

			//大退重新登陆如果有报名比赛发送同步消息
			List<MultiMatchImpl> multiMatchList = gameService.getMultiMatchPlayer(session.userId);
			if(multiMatchList!=null && multiMatchList.size()>0){
				for(MultiMatchImpl match:multiMatchList){
					logger.info("session status:{}", session.onlineStatus);
					Util.userHasLogin(session);
					session.enterMatch(match);
					session.currentModule = ServiceRepo.matchModule;
					pushService.multiMatchEnrollSynWithLogin(session,match.getParent().getId(),match.getId(),0);
					if (oldSession.channel != null && oldSession.channel.isOpen()) {
						// 通知旧连接下线
						logger.info("user {} be kickout as other deviced login ", user.getId());
						oldSession.channel.close();
					}
					return;
				}
			}
		}
		logger.info("session status:{}", session.onlineStatus);
		Util.userHasLogin(session);
	}

	private User registerUser() {
		int playerId = idGen.getAndIncrement();
		User user = hallService.initUser();
		user.setId(playerId);
		user.setCtime(new Date());
		user.setUserType(0);
		user.setGender((int) (System.currentTimeMillis() % 2));
		user.setNickname(randomNameService.randomName(user.getGender()));
		return user;
	}
}
