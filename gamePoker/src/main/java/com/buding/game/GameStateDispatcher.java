package com.buding.game;

import com.buding.poker.constants.PokerConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.api.desk.Desk;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.NetEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.game.events.PlayerEvent;
import com.buding.game.events.TimerEvent;
import com.google.gson.Gson;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class GameStateDispatcher<T extends Desk> {
	protected Logger logger = LogManager.getLogger(getClass());

	protected GameTimerMgr mTimerMgr = new GameTimerMgr();

	protected GameData mGameData = new GameData();

	public abstract void StateDispatch(DispatchEvent event);

	public abstract void SetDesk(T desk);

	public void setGameParam(String param) {
		if(StringUtils.isBlank(param)) {
			return;
		}
		this.mGameData.mGameParam = new Gson().fromJson(param, GameParam.class);
		logger.info("GameParam:"+param);
	}

	/*
	 * 
	 */
	public void HandlePlatformEvent(PlatformEvent event) {

		if (null == this.m_currentState) {
			this.logger.error("HandlePlatformEvent currentstate is null");
			return;
		}

		this.m_currentState.onPlatform(event);

	}

	public void HandlePlayerEvent(PlayerEvent event) {
		if (null == this.m_currentState) {
			this.logger.error("HandlePlayerEvent currentstate is null");
			return;
		}

		this.m_currentState.onPlayer(event);

	}

	public void HandleNetEvent(NetEvent event) {
		if (null == this.m_currentState) {
			this.logger.error("HandleNetEvent currentstate is null");
			return;
		}

		this.m_currentState.onNet(event);
	}
	
	public void setGamingDate(String gameData) {
		if (null == this.m_currentState) {
			this.logger.error("HandleNetEvent currentstate is null");
			return;
		}
		this.m_currentState.setGamingDate(gameData);
	}

	public void HandleTimerEvent(TimerEvent event) {

//		this.logger.info("HandleTimerEvent/ timerid is " + event.timerID+"deskTimeID is "+this.mTimerMgr.GetDeskTimerID()+"playerTimer is"+this.mTimerMgr.getmPlayerTimersMap());

		if (event.timerID == this.mTimerMgr.GetDeskTimerID()) {
			this.m_currentState.onDeskTimer();
		} else {

			for (int position = 0; position < PokerConstants.MYGAME_MAX_PLAYERS_COUNT; position++) {

				if (event.timerID == this.mTimerMgr.getPlayerTimerID(position)) {
					this.m_currentState.onPlayerTimerEvent(position);
					break;
				}
			}

			this.logger.error("HandleTimerEvent can not find one way out");
		}
	}

	// //////状态转换函数，子类不能覆盖，只能调用
	protected void goTo(GameState pNextState) {

		if (null == pNextState) {
			this.logger.error("pNextState should not be null");
			return;
		}

		// ///这里在转换状态之前需要kill定时器啊
		this.mTimerMgr.KillDeskTimer();

		// /上一个状态的exit
		if (null != this.m_currentState) {
			this.m_currentState.onExit();
		}

		this.m_currentState = pNextState;

		// //下一个状态
		this.m_currentState.onEnter();
	}

	private GameState m_currentState = null;
}
