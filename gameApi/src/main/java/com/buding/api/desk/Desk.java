package com.buding.api.desk;

import com.buding.api.context.GameContext;
import com.buding.api.player.PlayerInfo;
import packet.game.MsgGame;

import java.util.List;
import java.util.Map;

public interface Desk<MsgType> {
	
   PlayerInfo getDeskPlayer(int nDeskPos);

   PlayerInfo getDeskPlayerById(int playerId);

   List<PlayerInfo> getPlayers();

   List<PlayerInfo> getPlayingPlayers();

   List<PlayerInfo> getAllPlayers();

   int getPlayerCount();
	
  void sendMsg2Player(int position, MsgType content);
  void sendMsg2Player(PlayerInfo player, MsgType content);
   
   void sendMsg2Desk(MsgType content);

   void pushWaitNextMatchStart(MJDesk desk, MsgGame.WaitNextMatchStart.Builder gb);

   void sendMsg2DeskExceptPosition(MsgType content, int excludePosition);

   void sendMsg2DeskExceptPosition(MsgType content, PlayerInfo player);

   int setTimer(long mills);
   
   void killTimer(int timerID);
   
   void setDeskInValid();
   
   String getDeskID();
   
   int getBasePoint();//低分，底注
		
   void onGameOver(); //游戏结束
   
   void finalSettle(GameContext context); //总结算
   
   void handSettle(GameContext context); //一局结算
   
   boolean hasNextGame(GameContext context); //是否还有下一场
   
   void startNextGame(GameContext context); //重置，准备下一场
   
   void ready4NextGame(GameContext context); //开始准备好开始下一场
   
   double getFee();
      
   String getReplyData();

   //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   List<PlayerInfo> loopGetPlayer(int pos, int count, int includePos);

   //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   List<PlayerInfo> loopGetPlayerZJH(int pos, int count, int includePos);

   //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   List<PlayerInfo> loopGetPlayerJACK(int pos, int count, int includePos);

   //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   List<PlayerInfo> loopGetPlayerNN(int pos, int count, int includePos);

   //includePos: 0不包含pos玩家, 1:包含放在末尾 2包含放在开头
   List<PlayerInfo> loopGetPlayerErBa(int pos, int count, int includePos);

   List<Integer> getDebugData(int pos);
   
   void sendErrorMsg(int position, String msg);
   
   void log(LogLevel level, String msg, int position);
   
   int getPlayerActionTimeOut(int act);//获取玩家超时操作时间配置
   
   int getDeskOwner();
   
   void onPlayerHangup(int position);
   
   void onPlayerCancelHangup(int position);

   byte getShangGunBaoCard(byte bao);

   byte getXiaGunBaoCard(byte bao);

   List<Byte> getTongBaoCard(byte bao);

   boolean isMultiMatch();

   void updatePlayerScoreAndRank(Map<Integer,Integer> multiMatchScoreMap);

   boolean isAllBiSaiDeskFinishOneLun();

   void multiMatchStartNotify();

   int getLunNum();

   void sendMultiMatchRank();

   void subServiceFee(PlayerInfo pl);

   void setPauseTime(long l);

   void addUserConsumeDiamondLog(int playerNum);

   public void sendDissmissVoteMsg();

   String getMatchId();

   String getGameId();

   void zanLi(PlayerInfo p);

   void onGameBegin(List<PlayerInfo> mPlayers);

   boolean checkPlayerReady(PlayerInfo p);

   void kickout(int playerId, String msg);

   boolean isClubJiFenDesk();

   List<Integer> getfuLiPlayerList(String gameId);

   void markDeskAsWaitingGame();

 void gameCountIncr();

 void erBaXiaZhuOrConfirmBanker(PlayerInfo pl);

 void clearXiaZhuPlayer();
 int getStarterId();

 List<Integer> getErBaXiaZhuOrBankerPlayerIds();
}
