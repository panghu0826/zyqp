package com.buding.poker.states;

import com.buding.api.player.PlayerInfo;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 发牌状态
 */
public class PokerStateDeal extends PokerStateCommon {

    @Override
    public void handlePlayerStatusChange(int position) {

    }

    @Override
    protected void handlePlayerExit(PlayerInfo info) {
        if (mDesk.getErBaGameType() > 0 && !mGameData.chouMaMap.containsKey(info.playerId) && !mGameData.trandition28UserChouMaMap.containsKey(info.playerId)) {

            if (mGameData.erBaCurrentGamingPlayers.isEmpty()) {
                List<PlayerInfo> playerInfos = mDesk.getPlayers();
                if(mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) playerInfos = mDesk.getAllPlayers();
                if (playerInfos.size() <= 1) {
                    this.mGameTimer.KillDeskTimer();
                    mGameData.erBaSettleType = false;
                    mGameData.handNum++;
                    mDesk.gameCountIncr();
                    if (mDesk.canJingDianQiangZhuang()) mGameData.robIndex = 0;
                    game_over();
                }
            } else {
                for (PlayerInfo p : mGameData.erBaCurrentGamingPlayers) {
                    if (p.playerId == info.playerId) {
                        mGameData.erBaCurrentGamingPlayers.remove(p);
                        break;
                    }
                }

                List<PlayerInfo> playerInfos = mDesk.getPlayers();
                if(mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) playerInfos = mDesk.getAllPlayers();
                if (playerInfos.size() <= 1) {
                    this.mGameTimer.KillDeskTimer();
                    mGameData.erBaSettleType = false;
                    mGameData.handNum++;
                    mDesk.gameCountIncr();
                    if (mDesk.canJingDianQiangZhuang()) mGameData.robIndex = 0;

                    game_over();
                }
            }

        }
    }

    private void game_over() {
        DispatchEvent event = new DispatchEvent();
        event.eventID = PokerConstants.PokerStateFinish;
        this.mDispatcher.StateDispatch(event);
    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=deal;position={}", info.position);
        if (info.position >= 0) this.mGameData.mPlayers[info.position] = info;
        this.mGameData.mPlayersMap.put(info.playerId, info);
        super.handleReconnectFor(info);
    }

    @Override
    public void onEnter() {
        this.mGameData.mActor.gameState = PokerConstants.PokerStateDeal;

        //重置桌子参数
        this.mGameData.Reset();

        //清楚玩家当前的某些状态(上一局的倍数,分数)
        cleanPlayerInfo();
        this.mCardDealer.dealCard();
        this.mCardLogic.selectBanker(this.mGameData, this.mDesk);// 设置从谁开始喊地主
        this.mGameData.handStartTime = System.currentTimeMillis();
        this.mGameData.handEndTime = 0;
        this.mGameData.handNum++;// 局数加一

        this.mGameData.recorder.deskId = mDesk.getDeskID();
        this.mGameData.recorder.matchId = this.mDesk.getMatchId();
        this.mGameData.recorder.gameId = this.mDesk.getGameId();
        this.mGameData.recorder.wanfa = this.mDesk.getWanfa();
        this.mGameData.recorder.limitMax = this.mDesk.getLimitMax();
        this.mGameData.recorder.yaZhu = this.mDesk.getYaZhu();
        this.mGameData.recorder.menNum = this.mDesk.getMenNum();
        this.mGameData.recorder.erBaGameType = this.mDesk.getErBaGameType();
        this.mGameData.recorder.juNum = this.mGameData.handNum;

        this.mDesk.onGameBegin(StringUtils.equals(this.mDesk.getGameId(), "G_ErBa") ?
                new ArrayList<>(this.mGameData.mPlayersMap.values()) : new ArrayList<>(Arrays.asList(this.mGameData.mPlayers)));
        // 发牌
        this.mCardLogic.gameStart(this.mGameData, this.mDesk);

        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=gameDeal;isDealerAgain={};bankerPos={};bankerPlayerId={};handStartTime={};handNum={};seq={}",
                this.mGameData.mPublic.isContinueBanker, this.mGameData.mPublic.mbankerPos, this.mGameData.mPublic.mBankerUserId,
                this.mGameData.handStartTime, this.mGameData.handNum, this.mGameData.gameSeq);


        //2秒后开始游戏
        this.mGameTimer.KillDeskTimer();
        this.mGameTimer.SetDeskTimer(10);
    }

    /**
     * poker 单局结束清楚玩家当前的某些字段
     *
     */
    public void cleanPlayerInfo() {
//        logger.error("----------------------------------------------");
//        for(PokerJACKFinalResult f : mGameData.jackFinalResult.finalResults.values()){
//            logger.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
//        }
//        for(PokerJACKResult f : mGameData.jackResult.Result.values()){
//            logger.error("玩家Result--"+f.playerName+"--allsocre:"+f.allSocre);
//        }
//        for(PlayerInfo pls : mDesk.getPlayingPlayers()){
//            logger.error("玩家--"+pls.name+"--socre:"+pls.score);
//        }
        List<PlayerInfo> playerInfos = mDesk.getPlayers();
        if (mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            playerInfos = mDesk.getAllPlayers();
        }

        for (PlayerInfo playerInfo : playerInfos) {
            logger.error("桌子ID--"+mDesk.getDeskID()+"玩家--"+playerInfo.name+"--gamecount:"+playerInfo.gameCount);
            logger.error("桌子ID--"+mDesk.getDeskID()+"玩家--"+playerInfo.name+"--startGameCount:"+playerInfo.startGameCount);
            if(!playerInfo.isZanLi) playerInfo.startGameCount++;
            playerInfo.isQiPai = false;
            playerInfo.isKanPai = false;
            playerInfo.xiQian = 0;
            playerInfo.realXiQian = 0;
            playerInfo.isLose = false;
            playerInfo.chouMa = 0;
            playerInfo.isWait = false;
            playerInfo.danZhu = 0;
            playerInfo.isKaiPai = false;
            playerInfo.isXiaZhu = false;
            playerInfo.yanPaiResult = -1;
            playerInfo.multiple = 1;
            playerInfo.yaWuXiaoLong = -1;
            playerInfo.isTingPai = false;
            playerInfo.isBeiYanPai = false;
            playerInfo.curJuScore = 0;
            playerInfo.nnRobotNum = PokerConstants.NN_MEI_QIANG_ZHUANG;
            if(playerInfo.gameCount == 0 && this.mDesk.isClubJiFenDesk()){
                if(mGameData.zjhResult.Result.get(playerInfo.playerId) != null)
                    mGameData.zjhResult.Result.get(playerInfo.playerId).allSocre = playerInfo.score;
                if(mGameData.zjhFinalResult.finalResults.get(playerInfo.playerId) != null)
                    mGameData.zjhFinalResult.finalResults.get(playerInfo.playerId).allScore = playerInfo.score;
                if(mGameData.jackResult.Result.get(playerInfo.playerId) != null)
                    mGameData.jackResult.Result.get(playerInfo.playerId).allSocre = playerInfo.score;
                if(mGameData.jackFinalResult.finalResults.get(playerInfo.playerId) != null)
                    mGameData.jackFinalResult.finalResults.get(playerInfo.playerId).allScore = playerInfo.score;
                if(mGameData.ddzResult.Result.get(playerInfo.playerId) != null)
                    mGameData.ddzResult.Result.get(playerInfo.playerId).allScore = playerInfo.score;
                if(mGameData.ddzFinalResult.finalResults.get(playerInfo.playerId) != null)
                    mGameData.ddzFinalResult.finalResults.get(playerInfo.playerId).allScore = playerInfo.score;
                if(mGameData.nnResult.Result.get(playerInfo.playerId) != null)
                    mGameData.nnResult.Result.get(playerInfo.playerId).allScore = playerInfo.score;
                if(mGameData.nnFinalResult.finalResults.get(playerInfo.playerId) != null)
                    mGameData.nnFinalResult.finalResults.get(playerInfo.playerId).allScore = playerInfo.score;
            }
        }

//        logger.error("----------------------------------------------");
//        for(PokerJACKFinalResult f : mGameData.jackFinalResult.finalResults.values()){
//            logger.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
//        }
//        for(PokerJACKResult f : mGameData.jackResult.Result.values()){
//            logger.error("玩家Result--"+f.playerName+"--allsocre:"+f.allSocre);
//        }
//        for(PlayerInfo pls : mDesk.getPlayingPlayers()){
//            logger.error("玩家--"+pls.name+"--socre:"+pls.score);
//        }
    }

    @Override
    public void onPlatform(PlatformEvent event) {
        switch (event.eventID) {

            case GameLogicEvent.Game_Dismiss: {

                this.mGameData.dismissing = true;
                // //1秒后状态跳转
                this.mGameTimer.KillDeskTimer();

                DispatchEvent e = new DispatchEvent();
                e.eventID = PokerConstants.PokerStateFinish;
                this.mDispatcher.StateDispatch(e);
            }
            break;

            default: {
                super.onPlatform(event);
                break;
            }
        }
    }

    @Override
    public void onDeskTimer() {
        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"deal , onDeskTimer is called; deskId={}", mDesk.getDeskID());
        this.mGameTimer.KillDeskTimer();

        DispatchEvent event = new DispatchEvent();
        event.eventID = PokerConstants.PokerStateRun;
        this.mDispatcher.StateDispatch(event);
    }

    @Override
    public void onPlayerTimerEvent(int position) {

    }

    @Override
    public void onExit() {

    }

    @Override
    public void handlePlayerHangup(int position) {

    }

}
