package com.buding.poker.states;

import com.buding.api.context.PokerNNResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.NNPokerPushHelper;
import com.buding.poker.nn.NNProcessor;
import com.googlecode.protobuf.format.JsonFormat;
import packet.nn.NN;

/**
 * @author jaime qq_1094086610
 * @Description: 准备状态
 */
public class NNStateReady extends PokerStateCommon {

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
        NN.NNGameOperPlayerHuSyn.Builder nn = NN.NNGameOperPlayerHuSyn.newBuilder();
        nn.setJuNum(gameData.nnResult.juNum);
        nn.setPosition(gameData.mGameWin.position);
        for (PokerNNResult result : gameData.nnResult.Result.values()) {
            if (result.pos < 0) return;
            if (result.playerId <= 0) continue;
            NN.NNGameOperPlayerSettle.Builder sy = NN.NNGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.addAllHandcard(NNProcessor.byte2IntList(gameData.mPlayerCards[result.pos].nnBiPaiResult.cardsSortAndFenCha));
            sy.setLastScore(result.lastScore);
            sy.setScore(result.score);
            sy.setAllSocre(result.allScore);
            sy.setCardType(result.cardType);
            sy.setIsBanker(result.isBanker ? 1 : 0);
            sy.setResult(result.result);
            nn.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(nn.build()));
        NNPokerPushHelper.pushPlayerHuMsg(desk,pos,nn, PokerConstants.SEND_TYPE_SINGLE);
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
