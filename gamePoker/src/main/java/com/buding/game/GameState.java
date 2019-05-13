package com.buding.game;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.api.desk.Desk;
import com.buding.card.ICardLogic;
import com.buding.game.events.NetEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.game.events.PlayerEvent;
import com.buding.game.events.TimerEvent;


public abstract class GameState<T extends Desk> {
	protected Logger logger = LogManager.getLogger("DESKLOG");
	protected T mDesk = null;
	protected GameTimerMgr mGameTimer = null;
	protected GameStateDispatcher mDispatcher = null;
	protected GameData  mGameData = null;
	protected ICardLogic mCardLogic = null;
	protected GameCardDealer mCardDealer = null;
		
	public void Init(T desk, GameTimerMgr mgr , GameStateDispatcher dispatcher, GameData  data, ICardLogic logic,GameCardDealer dealer){
		this.mDesk = desk;
		this.mGameTimer = mgr;
		this.mDispatcher = dispatcher;
		this.mGameData = data;
		this.mCardLogic = logic;
		this.mCardDealer = dealer;
	}
		
	public abstract void onEnter();////在onEnter里面禁止进行状态跳转，且需要启动定时器驱动

	public abstract void onPlayer(PlayerEvent event);
	
	public abstract void onPlatform(PlatformEvent event);
	
	public abstract void onNet(NetEvent event);
	
	public abstract void onDeskTimer();
	
	public abstract void onPlayerTimerEvent(int position);
	
	public abstract void onExit();
	
	public abstract void setGamingDate(String gameData);
}
