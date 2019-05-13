//package com.buding.rank.processor;
//
//import com.buding.common.event.EventBus;
//import com.buding.common.event.Receiver;
//import com.buding.hall.config.RankConfig;
//import com.buding.hall.module.common.constants.RankPointType;
//import com.buding.hall.module.task.event.ConsumeFangkaGamePlayedEvent;
//import com.buding.hall.module.task.type.EventType;
//import com.buding.hall.module.task.vo.ConsumeGamePlayingVo;
//import com.buding.hall.module.user.dao.UserDao;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//
//import java.util.Calendar;
//
//
///**
// * @author jaime qq_1094086610
// * @Description:
// *
// */
//@Component
//public class FangKaRankProcessor extends BaseRankProcessor implements Receiver<ConsumeFangkaGamePlayedEvent> {
//	@Autowired
//	@Qualifier("hallEventBus")
//	EventBus eventBus;
//
//	@Autowired
//	UserDao userDao;
//
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		super.afterPropertiesSet();
//		eventBus.register(EventType.FANGKA_GAME, this);
//	}
//	@Override
//	public int getRankType() {
//		return RankPointType.consumeFangKaCount;
//	}
//
//	@Override
//	protected long getRankGroupTime() {
//		Calendar c = Calendar.getInstance();
//		int year = c.get(Calendar.YEAR);
//		int week = c.get(Calendar.WEEK_OF_YEAR);
//		return year * 1000 + week;
//	}
//
//	@Override
//	protected RankConfig getRankConfig() {
//		return configManager.rankConfMap.get("GAME_FANGKA_RANK");
//	}
//
//	@Override
//	public void onEvent(ConsumeFangkaGamePlayedEvent event) throws Exception {
//		ConsumeGamePlayingVo model = event.getBody();
//		addRankPoint(model.userId, 1, getRankGroupTime());
//	}
//
//	@Override
//	public String getEventName() {
//		return EventType.FANGKA_GAME;
//	}
//
//}
