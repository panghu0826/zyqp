package com.buding.rank.service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.ParseException;
import java.util.*;

import com.buding.db.model.User;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.user.dao.UserDao;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.rank.Rank.RankItem;
import packet.rank.Rank.RankSyn;

import com.buding.common.loop.Looper;
import com.buding.common.loop.ServerLoop;
import com.buding.common.network.session.SessionManager;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.common.constants.RankPointType;
import com.buding.rank.model.RankModel;
import com.buding.rank.processor.RankProcessor;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class UserRankManager implements InitializingBean, Looper {
	private Logger logger = LogManager.getLogger(getClass());
	
	public Map<Integer, RankProcessor> rankProcessorMap = new HashMap<Integer, RankProcessor>();

	@Autowired
	ConfigManager configManager;
	
	@Autowired
	@Qualifier("ServerBgTaskLoop")
	ServerLoop serverLoop;
	
	@Autowired
	SessionManager hallSessionManager;
	
	public long refreshTime = 0;

    @Autowired
    UserDao userDao;
		
	public void registerProcessor(int pointType, RankProcessor prossor) {
		rankProcessorMap.put(pointType, prossor);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		serverLoop.register(this);
	}
	
	@Override
	public void loop() throws Exception {
		checkRefresh();
	}

	private void checkRefresh() {
		if(System.currentTimeMillis() - refreshTime >= 10*1000) {
			doRefresh();
		}
	}

	private void doRefresh() {
		refreshTime = System.currentTimeMillis();
		boolean needPush = false;
		for(RankProcessor prossor : rankProcessorMap.values()) {
			try {
				boolean ret = prossor.refresh();
				if(ret) {
					needPush = true;
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		
		if(needPush) {
//			pushRank();
		}
	}
	public RankSyn buldRankSyn(int userId, String gameId, String rankType) {
		RankSyn.Builder rank = RankSyn.newBuilder();
		rank.setGameid(gameId);
		rank.setRankType(rankType);
		Integer type = Integer.valueOf(rankType);
		{
			if(type == RankPointType.coin) {
				RankProcessor p = rankProcessorMap.get(RankPointType.coin);
				int i = 1;
				for (RankModel tmp : getRank(userId, p.getRankType(), "ALL")) {
					RankItem.Builder item = RankItem.newBuilder();
					item.setRank(i++);
					item.setPlayerId(tmp.playerId);
					item.setPlayerName(tmp.name);
					item.setPlayerHeadImg(tmp.img);
					item.setPoint(tmp.rankPoint);
					UserGameOutline outline = userDao.getUserGameOutline(tmp.playerId);
					item.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
					item.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
					if (hallSessionManager.getIoSession(tmp.playerId) == null) {
						item.setIp("未知");
					} else {
						SocketAddress remoteAddress = hallSessionManager.getIoSession(tmp.playerId).getChannel().remoteAddress();
						if (remoteAddress instanceof InetSocketAddress) {
							item.setIp(((InetSocketAddress) remoteAddress).getAddress().getHostAddress());
						} else {
							item.setIp("未知");
						}
					}
					double winRate = 0;
					if (outline != null && outline.getTotalCount() > 0) {
						winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
					}
					item.setWinRate(winRate);
					User user = userDao.getUser(tmp.playerId);
					item.setFanka(user.getFanka());
					item.setCoin(user.getCoin());
					item.setDiamond(user.getDiamond());
					rank.addRankList(item);
				}
			}
		}
		{
			if(type == RankPointType.diamond) {
				RankProcessor p = rankProcessorMap.get(RankPointType.diamond);
				int i = 1;
				for (RankModel tmp : getRank(userId, p.getRankType(), "ALL")) {
					RankItem.Builder item = RankItem.newBuilder();
					item.setRank(i++);
					item.setPlayerId(tmp.playerId);
					item.setPlayerName(tmp.name);
					item.setPlayerHeadImg(tmp.img);
					item.setPoint(tmp.rankPoint);
					UserGameOutline outline = userDao.getUserGameOutline(tmp.playerId);
					item.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
					item.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
					if (hallSessionManager.getIoSession(tmp.playerId) == null) {
						item.setIp("未知");
					} else {
						SocketAddress remoteAddress = hallSessionManager.getIoSession(tmp.playerId).getChannel().remoteAddress();
						if (remoteAddress instanceof InetSocketAddress) {
							item.setIp(((InetSocketAddress) remoteAddress).getAddress().getHostAddress());
						} else {
							item.setIp("未知");
						}
					}
					double winRate = 0;
					if (outline != null && outline.getTotalCount() > 0) {
						winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
					}
					item.setWinRate(winRate);
					User user = userDao.getUser(tmp.playerId);
					item.setFanka(user.getFanka());
					item.setCoin(user.getCoin());
					item.setDiamond(user.getDiamond());
					rank.addRankList(item);
				}
			}
		}
		{
			if(type == RankPointType.gameCountInWeek) {
				RankProcessor p = rankProcessorMap.get(RankPointType.gameCountInWeek);
				int i = 1;
				for (RankModel tmp : getRank(userId, p.getRankType(), gameId)) {
					RankItem.Builder item = RankItem.newBuilder();
					item.setRank(i++);
					item.setPlayerId(tmp.playerId);
					item.setPlayerName(tmp.name);
					item.setPlayerHeadImg(tmp.img);
					item.setPoint(tmp.rankPoint);
					UserGameOutline outline = userDao.getUserGameOutline(tmp.playerId);
					item.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
					item.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
					if (hallSessionManager.getIoSession(tmp.playerId) == null) {
						item.setIp("未知");
					} else {
						SocketAddress remoteAddress = hallSessionManager.getIoSession(tmp.playerId).getChannel().remoteAddress();
						if (remoteAddress instanceof InetSocketAddress) {
							item.setIp(((InetSocketAddress) remoteAddress).getAddress().getHostAddress());
						} else {
							item.setIp("未知");
						}
					}
					double winRate = 0;
					if (outline != null && outline.getTotalCount() > 0) {
						winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
					}
					item.setWinRate(winRate);
					User user = userDao.getUser(tmp.playerId);
					item.setFanka(user.getFanka());
					item.setCoin(user.getCoin());
					item.setDiamond(user.getDiamond());
					rank.addRankList(item);
				}
			}
		}
		{
			if(type == RankPointType.gameCountInMonth) {

				RankProcessor p = rankProcessorMap.get(RankPointType.gameCountInMonth);
				int i = 1;
				for (RankModel tmp : getRank(userId, p.getRankType(), gameId)) {
					RankItem.Builder item = RankItem.newBuilder();
					item.setRank(i++);
					item.setPlayerId(tmp.playerId);
					item.setPlayerName(tmp.name);
					item.setPlayerHeadImg(tmp.img);
					item.setPoint(tmp.rankPoint);
					UserGameOutline outline = userDao.getUserGameOutline(tmp.playerId);
					item.setContinueWinCount(outline == null ? 0 : outline.getContinueWinCount());
					item.setTotalGameCount(outline == null ? 0 : outline.getTotalCount());
					if (hallSessionManager.getIoSession(tmp.playerId) == null) {
						item.setIp("未知");
					} else {
						SocketAddress remoteAddress = hallSessionManager.getIoSession(tmp.playerId).getChannel().remoteAddress();
						if (remoteAddress instanceof InetSocketAddress) {
							item.setIp(((InetSocketAddress) remoteAddress).getAddress().getHostAddress());
						} else {
							item.setIp("未知");
						}
					}
					double winRate = 0;
					if (outline != null && outline.getTotalCount() > 0) {
						winRate = (outline.getWinCount() * 1.0) / outline.getTotalCount();
					}
					item.setWinRate(winRate);
					User user = userDao.getUser(tmp.playerId);
					item.setFanka(user.getFanka());
					item.setCoin(user.getCoin());
					item.setDiamond(user.getDiamond());
					rank.addRankList(item);
				}
			}
		}
//		logger.info("rank===================="+JsonFormat.printToString(rank.build()));
		return rank.build();
	}
	
	public List<RankModel> getRank(int userId, int pointType,String gameId) {
		checkRefresh();
		
		RankProcessor processor = rankProcessorMap.get(pointType);
		if(processor != null) {
			return processor.getRank(userId,gameId);
		}
		return new ArrayList<RankModel>();
	}
}
