package com.buding.card;

import com.buding.api.desk.Desk;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.google.protobuf.GeneratedMessage;


public interface ICardLogic<T extends Desk> {
	
	//组件初始化
	public void init(GameData gameData, T desk);

	//调试接口
	public void handleSetGamingData(GameCardDealer mCardDealer, GameData gameData, T desk, String json);
	
	//主循环
	public void gameTick(GameData data, MJDesk<byte[]> desk);

	//发牌
    public void gameStart(GameData data, MJDesk<byte[]> desk);

	//设置下一个玩家做庄家
	public void selectBanker(GameData data, MJDesk<byte[]> desk);
	
	//提示玩家出牌	
	public void player_chu_notify(GameData gameData, T desk);

    void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p);

    //重新通知玩家操作
	public void re_notify_current_operation_player(GameData gameData, T desk, int position);
	
	//玩家操作(斗地主,扎金花)
	public void playerOperation(GameData gameData, MJDesk<byte[]> gt, GeneratedMessage.Builder msg, PlayerInfo pl);

	//服务器托管自动操作
	public void playerAutoOper(GameData gameData, MJDesk<byte[]> gt, int position);


	//重新推送玩家数据,用于断线重连
	public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position);

	//重新推送玩家数据,用于断线重连
	public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info);

	//-----------------------------牌类------------------------------------

	//抢地主
	public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk);

	//选择加倍
	public void notifyDouble(GameData gameData, T desk);

	void pushDeskInfo(GameData mGameData, MJDesk<byte[]> mDesk, PlayerInfo pl);


	//--------------------------------------------------------------------

}
