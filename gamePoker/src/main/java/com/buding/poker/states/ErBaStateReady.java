package com.buding.poker.states;

import com.buding.api.player.PlayerInfo;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;

import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 准备状态
 */
public class ErBaStateReady extends PokerStateCommon {

    @Override
    public void handlePlayerStatusChange(int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=ready;position={}", info.position);
        if (info.position >= 0) mGameData.mPlayers[info.position] = info;
        super.handleReconnectFor(info);
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

//                if (this.mGameData.mPublic.mBankerUserId <= 0) {
//                    PlayerInfo bankPlayer = this.mDesk.getPlayers().get(0); //庄家
//                    this.mGameData.mPublic.mbankerPos = bankPlayer.position;
//                    this.mGameData.mPublic.mBankerUserId = bankPlayer.playerId;
//                }

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
        List<PlayerInfo> playerInfos = mDesk.getPlayers();
        if(mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) playerInfos = mDesk.getAllPlayers();
        if (playerInfos.size() <= 1) {
            this.logger.info("桌子id--" + this.mDesk.getDeskID() + "--" + "人数不够");
            this.mGameTimer.KillDeskTimer();
            mGameData.handNum++;
            mGameData.erBaSettleType = false;
            mDesk.gameCountIncr();
            if (mDesk.canJingDianQiangZhuang()) mGameData.robIndex = 0;
            DispatchEvent event = new DispatchEvent();
            event.eventID = PokerConstants.PokerStateFinish;
            this.mDispatcher.StateDispatch(event);
            return;
        }
        //跳转到发牌
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
