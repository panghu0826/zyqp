package com.buding.hall.module.task.vo;

import java.io.Serializable;
import java.util.Date;



public class GamePlayingVo implements Serializable {
	private static final long serialVersionUID = -8011061374263995942L;
	
	public String gameId;
	public String matchId;
//	public GameResult result;
	public boolean enemyBankrupt;//对手是否破产
	public boolean bankrupt; //自己是否破产
	
	public int userId;
	public int coin;
	public int rankPoint;
	public int tax;
	public int winCount;
	public int loseCount;
	public int evenCount;
	public int continueWin;
	public Date gameTime;
	public String maxFanDesc;
	public int maxFanType;
	public int maxFanNum;
	public String maxFanHandCards;
	public String maxDownCards;
}
