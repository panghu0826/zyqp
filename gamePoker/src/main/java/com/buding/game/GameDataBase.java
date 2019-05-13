package com.buding.game;

import com.buding.api.context.*;
import com.buding.api.player.PlayerInfo;
import com.buding.poker.constants.PokerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GameDataBase {

	public GameDataBase() {
        ddzFinalResult.startTime = System.currentTimeMillis();
        zjhFinalResult.startTime = System.currentTimeMillis();
        jackFinalResult.startTime = System.currentTimeMillis();
        erBaFinalResult.startTime = System.currentTimeMillis();
        nnFinalResult.startTime = System.currentTimeMillis();

		for(int index = 0; index < PokerConstants.MYGAME_MAX_PLAYERS_COUNT; index ++){
            if(this.mPlayers[index] != null) {
				if (index < PokerConstants.DDZPlayerCount) {
				    PokerDDZFinalResult ddzResult = new PokerDDZFinalResult();
                    ddzResult.playerId = this.mPlayers[index].playerId;
                    ddzResult.playerName = this.mPlayers[index].name;
                    ddzResult.headImg = this.mPlayers[index].headImg;
                    this.ddzFinalResult.finalResults.put(this.mPlayers[index].playerId,ddzResult);
				}

                PokerZJHFinalResult zjhResult = new PokerZJHFinalResult();
                zjhResult.playerId = this.mPlayers[index].playerId;
                zjhResult.playerName = this.mPlayers[index].name;
                zjhResult.headImg = this.mPlayers[index].headImg;
                this.zjhFinalResult.finalResults.put(this.mPlayers[index].playerId,zjhResult);

                PokerJACKFinalResult jackResult = new PokerJACKFinalResult();
                jackResult.playerId = this.mPlayers[index].playerId;
                jackResult.playerName = this.mPlayers[index].name;
                jackResult.headImg = this.mPlayers[index].headImg;
                this.jackFinalResult.finalResults.put(this.mPlayers[index].playerId,jackResult);
            }
		}
        for (PlayerInfo pl : this.mPlayersMap.values()) {
            PokerErBaFinalResult erBaFinalResult = new PokerErBaFinalResult();
            erBaFinalResult.playerId = pl.playerId;
            erBaFinalResult.playerName = pl.name;
            erBaFinalResult.headImg = pl.headImg;
            this.erBaFinalResult.finalResults.put(pl.playerId,erBaFinalResult);
        }
	}

	/*
	 * 所有数据重置
	 */
	protected void Reset(){
		if(ddzResult == null) {
			this.ddzResult = new DDZResult();
		}
		if(zjhResult == null) {
			this.zjhResult = new ZJHResult();
		}
		if(jackResult == null) {
			this.jackResult = new JACKResult();
		}
		this.ddzResult.startTime = System.currentTimeMillis();
		this.zjhResult.startTime = System.currentTimeMillis();
		this.jackResult.startTime = System.currentTimeMillis();

		this.mDeskCard = new GamePacket.MyGame_DeskCard();
		this.recorder = new GameRecorder();
		this.mActor = new GamePacket.MyGame_Actor();
		this.mPlayerActionMap.clear();
		this.mPlayerCardsMap.clear();

        for (PlayerInfo pl : mPlayersMap.values()) {
            this.mPlayerActionMap.put(pl.playerId, new GamePacket.MyGame_Player_Action());
            this.mPlayerCardsMap.put(pl.playerId, new GamePacket.MyGame_Player_Cards());

            if(this.erBaResult.Result.get(pl.playerId) == null) {
                this.erBaResult.Result.put(pl.playerId,new PokerErBaResult());
            }
            PokerErBaResult erBaResult = this.erBaResult.Result.get(pl.playerId);
            erBaResult.playerId = pl.playerId;
            erBaResult.playerName = pl.name;
            erBaResult.result = PokerDDZResult.GAME_RESULT_EVEN;
            erBaResult.pos = pl.position;

            if(this.erBaFinalResult.finalResults.get(pl.playerId) == null){
                this.erBaFinalResult.finalResults.put(pl.playerId,new PokerErBaFinalResult());
            }
            PokerErBaFinalResult erBafinalResult = this.erBaFinalResult.finalResults.get(pl.playerId);
            erBafinalResult.playerId = pl.playerId;
            erBafinalResult.playerName = pl.name;
            erBafinalResult.headImg = pl.headImg;
        }

		for(int index = 0; index < PokerConstants.MYGAME_MAX_PLAYERS_COUNT; index ++){
			this.mPlayerCards[index] = new GamePacket.MyGame_Player_Cards();

			if(this.mPlayerAction[index] == null) {
				this.mPlayerAction[index] = new GamePacket.MyGame_Player_Action();
			} else {
				this.mPlayerAction[index].reset();
			}

			if(this.mPlayers[index] != null) {
				if (index < PokerConstants.DDZPlayerCount) {
					if(this.ddzResult.Result.get(this.mPlayers[index].playerId) == null) {
						this.ddzResult.Result.put(this.mPlayers[index].playerId,new PokerDDZResult());
					}
					PokerDDZResult ddzResult = this.ddzResult.Result.get(this.mPlayers[index].playerId);
					ddzResult.playerId = this.mPlayers[index].playerId;
					ddzResult.playerName = this.mPlayers[index].name;
					ddzResult.result = PokerDDZResult.GAME_RESULT_EVEN;
					ddzResult.pos = this.mPlayers[index].position;
					this.ddzResult.bomb = 0;
					this.ddzResult.friedKing = 0;
					this.ddzResult.spring = 0;

					if(this.ddzFinalResult.finalResults.get(this.mPlayers[index].playerId) == null){
						this.ddzFinalResult.finalResults.put(this.mPlayers[index].playerId,new PokerDDZFinalResult());
					}
					PokerDDZFinalResult finalResult = this.ddzFinalResult.finalResults.get(this.mPlayers[index].playerId);
					finalResult.playerId = this.mPlayers[index].playerId;
					finalResult.playerName = this.mPlayers[index].name;
					finalResult.headImg = this.mPlayers[index].headImg;
				}

				if(this.zjhResult.Result.get(this.mPlayers[index].playerId) == null) {
					this.zjhResult.Result.put(this.mPlayers[index].playerId,new PokerZJHResult());
				}
				PokerZJHResult zjhResult = this.zjhResult.Result.get(this.mPlayers[index].playerId);
				zjhResult.playerId = this.mPlayers[index].playerId;
				zjhResult.playerName = this.mPlayers[index].name;
				zjhResult.result = PokerDDZResult.GAME_RESULT_EVEN;
				zjhResult.pos = this.mPlayers[index].position;

				if(this.zjhFinalResult.finalResults.get(this.mPlayers[index].playerId) == null){
					this.zjhFinalResult.finalResults.put(this.mPlayers[index].playerId,new PokerZJHFinalResult());
				}
				PokerZJHFinalResult zjhfinalResult = this.zjhFinalResult.finalResults.get(this.mPlayers[index].playerId);
				zjhfinalResult.playerId = this.mPlayers[index].playerId;
				zjhfinalResult.playerName = this.mPlayers[index].name;
				zjhfinalResult.headImg = this.mPlayers[index].headImg;

				if(this.jackResult.Result.get(this.mPlayers[index].playerId) == null) {
					this.jackResult.Result.put(this.mPlayers[index].playerId,new PokerJACKResult());
				}
				PokerJACKResult jackResult = this.jackResult.Result.get(this.mPlayers[index].playerId);
				jackResult.playerId = this.mPlayers[index].playerId;
				jackResult.playerName = this.mPlayers[index].name;
				jackResult.result = PokerDDZResult.GAME_RESULT_EVEN;
				jackResult.pos = this.mPlayers[index].position;

				if(this.jackFinalResult.finalResults.get(this.mPlayers[index].playerId) == null){
					this.jackFinalResult.finalResults.put(this.mPlayers[index].playerId,new PokerJACKFinalResult());
				}
				PokerJACKFinalResult jackfinalResult = this.jackFinalResult.finalResults.get(this.mPlayers[index].playerId);
				jackfinalResult.playerId = this.mPlayers[index].playerId;
				jackfinalResult.playerName = this.mPlayers[index].name;
				jackfinalResult.headImg = this.mPlayers[index].headImg;

				if(this.nnResult.Result.get(this.mPlayers[index].playerId) == null) {
					this.nnResult.Result.put(this.mPlayers[index].playerId,new PokerNNResult());
				}
				PokerNNResult nnResult = this.nnResult.Result.get(this.mPlayers[index].playerId);
				nnResult.playerId = this.mPlayers[index].playerId;
				nnResult.playerName = this.mPlayers[index].name;
				nnResult.result = PokerDDZResult.GAME_RESULT_EVEN;
				nnResult.pos = this.mPlayers[index].position;

				if(this.nnFinalResult.finalResults.get(this.mPlayers[index].playerId) == null){
					this.nnFinalResult.finalResults.put(this.mPlayers[index].playerId,new PokerNNFinalResult());
				}
				PokerNNFinalResult nnfinalResult = this.nnFinalResult.finalResults.get(this.mPlayers[index].playerId);
				nnfinalResult.playerId = this.mPlayers[index].playerId;
				nnfinalResult.playerName = this.mPlayers[index].name;
				nnfinalResult.headImg = this.mPlayers[index].headImg;

				if(this.mPlayers[index].robot == 1) {
					this.mPlayerAction[index].autoOperation = 1;//自动托管
				}
			}
		}

		List<Integer> currentUserIdList = new ArrayList<>();
		for(PlayerInfo p : mPlayers){
		    if(p == null) continue;
            currentUserIdList.add(p.playerId);
        }
        {
            List<PokerDDZResult> needCleanDDZList = new ArrayList<>();
            for (PokerDDZResult r : ddzResult.Result.values()) {
                if (!currentUserIdList.contains(r.playerId)) {
                    PokerDDZResult result = new PokerDDZResult();
                    result.playerName = r.playerName;
                    result.playerId = r.playerId;
                    result.allScore = r.allScore;
                    needCleanDDZList.add(result);
                }
            }
            for (PokerDDZResult r : needCleanDDZList) {
                ddzResult.Result.put(r.playerId, r);
            }
        }
        {
            List<PokerJACKResult> needCleanJACKList = new ArrayList<>();
            for (PokerJACKResult r : jackResult.Result.values()) {
                if (!currentUserIdList.contains(r.playerId)) {
                    PokerJACKResult result = new PokerJACKResult();
                    result.playerName = r.playerName;
                    result.playerId = r.playerId;
                    result.allSocre = r.allSocre;
                    needCleanJACKList.add(result);
                }
            }
            for (PokerJACKResult r : needCleanJACKList) {
                jackResult.Result.put(r.playerId, r);
            }
        }
        {
            List<PokerZJHResult> needCleanZJHList = new ArrayList<>();
            for (PokerZJHResult r : zjhResult.Result.values()) {
                if (!currentUserIdList.contains(r.playerId)) {
                    PokerZJHResult result = new PokerZJHResult();
                    result.playerName = r.playerName;
                    result.playerId = r.playerId;
                    result.allSocre = r.allSocre;
                    needCleanZJHList.add(result);
                }
            }
            for (PokerZJHResult r : needCleanZJHList) {
                zjhResult.Result.put(r.playerId, r);
            }
        }
        {
            List<PokerNNResult> needCleanNNList = new ArrayList<>();
            for (PokerNNResult r : nnResult.Result.values()) {
                if (!currentUserIdList.contains(r.playerId)) {
					PokerNNResult result = new PokerNNResult();
                    result.playerName = r.playerName;
                    result.playerId = r.playerId;
                    result.allScore = r.allScore;
					needCleanNNList.add(result);
                }
            }
            for (PokerNNResult r : needCleanNNList) {
                nnResult.Result.put(r.playerId, r);
            }
        }


        {

            List<Integer> currentUserIdListByMap = new ArrayList<>();
            for(PlayerInfo p : mPlayersMap.values()){
                if(p == null || p.position < 0) continue;
                currentUserIdListByMap.add(p.playerId);
            }
            List<PokerErBaResult> needCleanErBaList = new ArrayList<>();
            for (PokerErBaResult r : erBaResult.Result.values()) {
                if (!currentUserIdListByMap.contains(r.playerId)) {
                    PokerErBaResult result = new PokerErBaResult();
                    result.playerName = r.playerName;
                    result.playerId = r.playerId;
                    result.allScore = r.allScore;
                    needCleanErBaList.add(result);
                }
            }
            for (PokerErBaResult r : needCleanErBaList) {
                erBaResult.Result.put(r.playerId, r);
            }
        }

	}


	////游戏参数,暂时没有用到
	public GameParam mGameParam = new GameParam();

	//负责换宝的玩家, 第一个上听的玩家负责换宝
	public int playerChangeBao = -1;

	//是否解散中状态
	public boolean dismissing = false;

	//是否暂停状态.
	public boolean pause = false;

	///行动者
	public GamePacket.MyGame_Actor mActor = null;

	///玩家相关牌
	public GamePacket.MyGame_Player_Cards mPlayerCards[] = new GamePacket.MyGame_Player_Cards[PokerConstants.MYGAME_MAX_PLAYERS_COUNT];

    //胡牌信息
	public GamePacket.MyGame_Player_Win mGameWin = new GamePacket.MyGame_Player_Win();;

	//公共信息
	public GamePacket.MyGame_PublicInfo mPublic = new GamePacket.MyGame_PublicInfo();

	//桌上的牌
	public GamePacket.MyGame_DeskCard mDeskCard = null;

	//玩家行动
	public GamePacket.MyGame_Player_Action mPlayerAction[] = new GamePacket.MyGame_Player_Action[PokerConstants.MYGAME_MAX_PLAYERS_COUNT];

	//玩家信息
	public PlayerInfo[] mPlayers = new PlayerInfo[PokerConstants.MYGAME_MAX_PLAYERS_COUNT];

	//游戏记录
	public GameRecorder recorder = new GameRecorder();


    //玩家信息
    public Map<Integer, PlayerInfo> mPlayersMap = new ConcurrentHashMap<>();


	//玩家28相关牌,经典28+疯狂28:玩家id - 牌, 传统28: 四门类型 - 牌
	public Map<Integer, GamePacket.MyGame_Player_Cards> mPlayerCardsMap = new ConcurrentHashMap<>();

	//玩家28行动
	public Map<Integer, GamePacket.MyGame_Player_Action> mPlayerActionMap = new ConcurrentHashMap<>();


	//===================================poker===============================================

	//玩家单局结算
	public DDZResult ddzResult = new DDZResult();

	//玩家总结算
	public DDZFinalResult ddzFinalResult = new DDZFinalResult();

	//玩家单局结算
	public ZJHResult zjhResult = new ZJHResult();

	//玩家总结算
	public ZJHFinalResult zjhFinalResult = new ZJHFinalResult();

	//玩家单局结算
	public JACKResult jackResult = new JACKResult();

	//玩家总结算
	public JACKFinalResult jackFinalResult = new JACKFinalResult();

	//玩家单局结算
	public ErBaResult erBaResult = new ErBaResult();

	//玩家总结算
	public ErBaFinalResult erBaFinalResult = new ErBaFinalResult();

	//玩家单局结算
	public NNResult nnResult = new NNResult();

	//玩家总结算
	public NNFinalResult nnFinalResult = new NNFinalResult();

}
