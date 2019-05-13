package com.buding.test;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface Player {	
	public boolean isInit();
	public boolean isMsgInit();
	public boolean isGameInit();
	public boolean isLogin();
	public boolean isAuth();
	public boolean isGameAuth();
	public boolean isMsgAuth();
	public boolean isEnroll();
	public boolean isReady();
	public boolean hasAccount();
	public boolean isInitInteractCmd();
			
	public boolean init(String serverIp, int serverPort);
	public boolean initMsg();
	public boolean initGame();
	public void login();
	public void setAccount(String username, String passwd);
	public void auth();
	public void authMsg();
	public void authGame();
	public void gameHeatbeat();
	public void hallHeatbeat();
	public void msgHeatbeat();
	public void enroll(String match, String roomCode);
	public void ready();
	public void loadAccount();
	public void initInteractCmd();
	
	public GameServerProxy getGameServerProxy();

	public HallServerProxy getHallServerProxy() ;

	public MsgServerProxy getMsgServerProxy();
	
	
}
