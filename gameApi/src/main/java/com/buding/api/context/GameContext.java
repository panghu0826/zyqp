package com.buding.api.context;


import com.buding.api.player.PlayerInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GameContext {
	public long gameStartTime;
	public long handNum; //已进行游戏局数
	public long nextHandNum; //已进行游戏局数
	public long quanNum; //已进行圈数
	public long nextQuanNum; //已进行圈数
	
	public int bankerPos;
	public int winerPos;
	public Map<Integer,PlayerInfo> playingPlayers = new HashMap<>();
	public Map<Integer,PlayerInfo> needKickOutPlayers = new HashMap<>();

	//==============================poker=====================================
	public DDZResult ddzResult = null;
	public DDZFinalResult ddzFinalResult = null;

	public ZJHResult zjhResult = null;
	public ZJHFinalResult zjhFinalResult = null;

	public JACKResult jackResult = null;
	public JACKFinalResult jackFinalResult = null;

	public NNResult nnResult = null;
	public NNFinalResult nnFinalResult = null;

	public ErBaResult erBaResult = null;
	public ErBaFinalResult erBaFinalResult = null;

}
