package com.buding.poker.states;

import com.buding.api.context.PokerJACKResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.JACKPokerPushHelper;
import com.buding.poker.jack.JACKProcessor;
import com.googlecode.protobuf.format.JsonFormat;
import packet.jack.JACK;

/**
 * @author jaime qq_1094086610
 * @Description: 准备状态
 */
public class JACKStateReady extends PokerStateCommon {

    @Override
    public void handlePlayerStatusChange(int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=ready;position={}", info.position);
        this.mGameData.mPlayers[info.position] = info;
        if(this.mGameData.handNum > 0) pushPlayerHuMsg(this.mGameData, this.mDesk ,info.position);
        super.handleReconnectFor(info);
    }

    private void pushPlayerHuMsg(GameData gameData, MJDesk desk,int pos) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----重新推送单局结算-----");
        JACK.JACKGameOperPlayerHuSyn.Builder jack = JACK.JACKGameOperPlayerHuSyn.newBuilder();
        jack.setJuNum(gameData.jackResult.juNum);
        jack.setPosition(gameData.mGameWin.position);
        for (PokerJACKResult result : gameData.jackResult.Result.values()) {
            if (result.pos < 0) return;
            if (result.playerId <= 0) continue;
            JACK.JACKGameOperPlayerSettle.Builder sy = JACK.JACKGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.addAllHandcard(JACKProcessor.byte2IntList(gameData.mPlayerCards[result.pos].cardsInHand));
            sy.setLastScore(result.lastScore);
            sy.setScore(result.score);
            sy.setAllSocre(result.allSocre);
            sy.setIsZanLi(result.isZanLi);
            sy.setCardNum(result.cardNum);
            sy.setCardType(result.cardType);
            sy.setIsBanker(result.pos == gameData.robIndex ? 1 : 0);
            sy.setResult(result.result);
            jack.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(jack.build()));
        JACKPokerPushHelper.pushPlayerHuMsg(desk,pos,jack, PokerConstants.SEND_TYPE_SINGLE);
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onPlatform(PlatformEvent event) {
        switch (event.eventID) {

            case GameLogicEvent.Game_Begin: {
                this.mGameData.Reset();

                this.mGameData.mActor.gameState = PokerConstants.PokerStateReady;

                if (this.mGameData.mPublic.mBankerUserId <= 0) {
                    PlayerInfo bankPlayer = this.mDesk.getPlayers().get(0); //庄家
                    this.mGameData.mPublic.mbankerPos = bankPlayer.position;
                    this.mGameData.mPublic.mBankerUserId = bankPlayer.playerId;
                }

                // //1秒后状态跳转
                this.mGameTimer.KillDeskTimer();
                this.mGameTimer.SetDeskTimer(500);

            }
            break;

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
            }
            break;
        }
    }

    @Override
    public void onDeskTimer() {
        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"ready , onDeskTimer is called; deskId={}", mDesk.getDeskID());

        this.mGameTimer.KillDeskTimer();

        int num = 0;
        if(mDesk.canForceChuPai()){//30秒不准备暂离
            //判断下这时候坐下的玩家
            for(PlayerInfo p : mGameData.mPlayers){
                if(p == null || p.isZanLi || p.position == mGameData.robIndex) continue;
                if(p.isWait || mDesk.checkPlayerReady(p))num++;
            }
        }
        if(mDesk.canForceChuPai()) {
            if (num == 0 && mGameData.handNum > 0) {
                this.mGameData.dismissing = true;
                DispatchEvent e = new DispatchEvent();
                e.eventID = PokerConstants.PokerStateFinish;
                this.mDispatcher.StateDispatch(e);
                return;
            } else {
                for (PlayerInfo p : mGameData.mPlayers) {
                    if (p == null || p.isZanLi || p.position == mGameData.robIndex || p.isWait) continue;
                    if (!mDesk.checkPlayerReady(p)) mDesk.zanLi(p);
                }
            }
        }
        ////跳转到发牌
        DispatchEvent event = new DispatchEvent();
        event.eventID = PokerConstants.PokerStateDeal;
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
