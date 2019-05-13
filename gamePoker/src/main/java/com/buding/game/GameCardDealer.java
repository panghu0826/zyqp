package com.buding.game;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.api.desk.Desk;
import com.buding.card.ICardLogic;

public abstract class GameCardDealer<T extends Desk> {

	protected Logger logger = LogManager.getLogger(getClass());
	protected GameData  mGameData = null;
	protected T mDesk = null;
	protected ICardLogic mCardLogic = null;
	public void Init(GameData  data, T desk, ICardLogic logic){
		this.mGameData = data;
		this.mDesk = desk;
		this.mCardLogic = logic;
	}
	
	//洗牌
	public abstract void dealCard();
	
	//处理宝牌
	public abstract void dealPublicCard();
}
