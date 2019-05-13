package com.buding.hall.module.event;

import com.buding.hall.module.task.event.*;
import com.buding.hall.module.task.vo.*;
import com.buding.hall.module.user.vo.PlayerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.util.IOUtil;
import com.buding.hall.config.ConfigManager;
import com.google.gson.Gson;

import javax.swing.event.ChangeEvent;

@Component
public class EventService {
	@Autowired
	@Qualifier("hallEventBus")
	EventBus eventBus;
	
	@Autowired
	ConfigManager configManager;
	
//	@Autowired
//	TaskService TaskServiceImpl;
		
	public void postShareEvent(int userId, int rating) {
		ShareEvent event = new ShareEvent(userId);
		eventBus.post(event);
	}
		
	public void postBindMobileEvent(int userId) {
		BindMobileEvent event = new BindMobileEvent(userId);
		eventBus.post(event);
	}
	
	public void postCoinChangeEvent(int userId, int coinChange,String gameId) {
		PlayerCoinVo vo = new PlayerCoinVo(userId, coinChange,gameId);
		CoinChangeEvent event = new CoinChangeEvent(vo);
		eventBus.post(event);
	}
	
	public void postGamePlayedInWeekEvent(GamePlayingVo ret) {
		GamePlayedInWeekEvent event = new GamePlayedInWeekEvent(ret);
		eventBus.post(event);
	}

	public void postGamePlayedInMonthEvent(GamePlayingVo ret) {
		GamePlayedInMonthEvent event = new GamePlayedInMonthEvent(ret);
		eventBus.post(event);
	}

//	public void postConsumeFangkaGamePlayedEvent(ConsumeGamePlayingVo ret) {
//		ConsumeFangkaGamePlayedEvent event = new ConsumeFangkaGamePlayedEvent(ret);
//		eventBus.post(event);
//	}
	
	public void postLoginEvent(int userId) {
		LoginEvent event = new LoginEvent(userId);
		eventBus.post(event);
	}
	
	public void postRatingEvent(RatingVo ret) {
		RatingEvent event = new RatingEvent(ret);
		eventBus.post(event);
	}
	
	public void triggerGameResultEvent() throws Exception {
		String path = configManager.gmPath;
		String json = IOUtil.getFileResourceAsString(path+"/GameResult.json", "utf-8");
		GamePlayingVo copy = new Gson().fromJson(json, GamePlayingVo.class);
		postGamePlayedInWeekEvent(copy);
	}

	public void postDiamondChangeEvent(int userId, int change, String all) {
		PlayerDiamondVo vo = new PlayerDiamondVo(userId, change,all);
		DiamondChangeEvent event = new DiamondChangeEvent(vo);
		eventBus.post(event);
	}
}
