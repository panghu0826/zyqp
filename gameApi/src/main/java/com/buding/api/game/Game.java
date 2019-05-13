package com.buding.api.game;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.buding.api.desk.Desk;
import com.buding.api.player.PlayerInfo;

public abstract class Game {
	
	protected Logger logger = LogManager.getLogger(getClass());

	public abstract void setDesk(Desk desk, String params);
	
	public abstract void setGameParam(String params);

    public abstract void playerSit(PlayerInfo player);

    public abstract void playerEnter(PlayerInfo player);

    public abstract void playerExitPosNotExitRoom(PlayerInfo player);

    public abstract void playerAgree(PlayerInfo player);
    
    public abstract void playerExit(PlayerInfo player);

    public abstract void playerZanLi(PlayerInfo player);

    public abstract void playerAway(PlayerInfo player);
    
    public abstract void playerComeBack(PlayerInfo player);

    public abstract void playerShuffle(PlayerInfo player);

    public abstract void hangeUp(PlayerInfo player);
    
    public abstract void cancelHangeUp(PlayerInfo player);
        
    public abstract void handleGameMsg(int playerId, int position, Object content);
    
    public abstract void gameBegin();
    
    public abstract void onTimer(int timerID);
    
    public abstract void playerOffline(PlayerInfo player);
    
    public abstract void playerReconnect(PlayerInfo player);
    
//    public abstract void dismiss();
    
    public abstract String dumpGameData();

	public abstract void setGamingDate(String json);
	
	public abstract void gameDismiss();
	
	public abstract void gamePause();
	
	public abstract void gameResume();

    public abstract void pushDeskInfo(PlayerInfo pl);
}

