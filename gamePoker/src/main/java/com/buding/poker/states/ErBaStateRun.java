package com.buding.poker.states;

import com.buding.api.player.PlayerInfo;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.NetEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.google.protobuf.InvalidProtocolBufferException;
import packet.erba.ErBa;
import packet.mj.MJBase.GameOperation;

import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 游戏状态
 */
public class ErBaStateRun extends PokerStateCommon {
    private boolean mTimerForStateChange = false;

    @Override
    public void handlePlayerStatusChange(int position) {

    }


    @Override
    public void playerSit(PlayerInfo info) {
        super.playerSit(info);
        mCardLogic.erBa_notify_new_player(mGameData, mDesk, info);
    }

    @Override
    protected void handlePlayerExit(PlayerInfo info) {
        if (mDesk.getErBaGameType() > 0 && !mGameData.chouMaMap.containsKey(info.playerId) && !mGameData.trandition28UserChouMaMap.containsKey(info.playerId)) {
            mGameData.qiangZhuangMap.remove(info.playerId);

            if (mGameData.mPlayerActionMap.get(info.playerId) != null) mGameData.mPlayerActionMap.get(info.playerId).opStartTime = 0L;

            for (PlayerInfo p : mGameData.erBaCurrentGamingPlayers) {
                if (p.playerId == info.playerId) {
                    mGameData.erBaCurrentGamingPlayers.remove(p);
                    break;
                }
            }

            List<PlayerInfo> playerInfos = mGameData.erBaCurrentGamingPlayers;
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

    @Override
    public void handleOffline(PlayerInfo info) {
        mGameData.mPlayersMap.get(info.playerId).isOnline = false;
        this.mGameData.canAutoOper = false;
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"玩家:"+ info.name + "离线");
        super.handleOffline(info);
        if (mGameData.mPlayerActionMap.get(info.playerId) != null) mGameData.mPlayerActionMap.get(info.playerId).opStartTime = 0L;

        if (mDesk.getErBaGameType() > 0 && !mGameData.chouMaMap.containsKey(info.playerId) && !mGameData.trandition28UserChouMaMap.containsKey(info.playerId) && info.playerId != mGameData.robIndex) {
            mGameData.qiangZhuangMap.remove(info.playerId);

            for (PlayerInfo p : mGameData.erBaCurrentGamingPlayers) {
                if (p.playerId == info.playerId) {
                    mGameData.erBaCurrentGamingPlayers.remove(p);
                    break;
                }
            }
        }

        List<PlayerInfo> playerInfos = mGameData.erBaCurrentGamingPlayers;
        if (playerInfos.size() <= 1) {
            this.mGameTimer.KillDeskTimer();
            mGameData.erBaSettleType = false;
            mGameData.handNum++;
            mDesk.gameCountIncr();
            if (mDesk.canJingDianQiangZhuang()) mGameData.robIndex = 0;
            game_over();
        }
    }

    @Override
    public void handleExitPos(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"玩家:"+ info.name + "离座");
        if (mGameData.mPlayerActionMap.get(info.playerId) != null) mGameData.mPlayerActionMap.get(info.playerId).opStartTime = 0L;

        if (mDesk.getErBaGameType() > 0 && !mGameData.chouMaMap.containsKey(info.playerId) && !mGameData.trandition28UserChouMaMap.containsKey(info.playerId) && info.playerId != mGameData.robIndex) {
            mGameData.qiangZhuangMap.remove(info.playerId);

            for (PlayerInfo p : mGameData.erBaCurrentGamingPlayers) {
                if (p.playerId == info.playerId) {
                    mGameData.erBaCurrentGamingPlayers.remove(p);
                    break;
                }
            }
        }

        List<PlayerInfo> playerInfos = mGameData.erBaCurrentGamingPlayers;
        if (playerInfos.size() <= 1) {
            this.mGameTimer.KillDeskTimer();
            mGameData.erBaSettleType = false;
            mGameData.handNum++;
            mDesk.gameCountIncr();
            if (mDesk.canJingDianQiangZhuang()) mGameData.robIndex = 0;
            game_over();
        }
    }




    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=run;position={};seq={};", info.position, mGameData.recorder.seq);
        if (info.position >= 0) this.mGameData.mPlayers[info.position] = info;

        this.mCardLogic.repushGameData(this.mGameData, this.mDesk, info);

        if ((info.position >= 0) && this.mGameData.mPlayerAction[info.position].autoOperation == 1) {
            this.mDesk.onPlayerHangup(info.position);
        }
        if (info.position >= 0) this.mGameData.mPlayers[info.position].isOnline = true;
        super.handleReconnectFor(info);
    }

    @Override
    public void onNet(NetEvent event) {
        try {
            byte[] data = (byte[]) event.msg;
            GameOperation p = GameOperation.parseFrom(data);
            if(this.mGameData.pause) return;
            this.mGameData.canAutoOper = true;
            switch (p.getOperType()) {
                case ErBaGameOperPlayerActionSyn: {
                    ErBa.ErBaGameOperPlayerActionSyn.Builder gb = ErBa.ErBaGameOperPlayerActionSyn.newBuilder();
                    gb.mergeFrom(p.getContent());
                    gb.setPlayerId(event.playerId);
                    mCardLogic.playerOperation(mGameData, mDesk, gb, mDesk.getDeskPlayerById(gb.getPlayerId()));
                }
                break;
                case GameOperReNofity: {// ???????? 母鸡?
                    this.mCardLogic.re_notify_current_operation_player(this.mGameData, this.mDesk, event.playerId);
                }
                break;
                default: {
                    throw new RuntimeException("不支持的类型:" + p.getOperType());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("", e);
        }
    }

    @Override
    public void onEnter() {
        // 发送公共信息
//        ErBaPokerPushHelper.pushPublicInfoMsg2All(this.mDesk, this.mGameData);

        // 桌子进入开打状态
        this.mGameData.mActor.gameState = PokerConstants.PokerStateRun;
        this.mGameData.setState(PokerConstants.GAME_TABLE_STATE_PLAYING);

        this.checkFinish();
    }

    private void checkFinish() {
        if (this.mGameData.mActor.gameState == PokerConstants.PokerStateFinish) {
            this.mTimerForStateChange = true;
            this.mGameTimer.KillDeskTimer();
            this.mGameTimer.SetDeskTimer(50); // 立刻跳到游戏结束状态
            return;
        }
        long sleep = 100;
        if (mGameData.sleepTo > System.currentTimeMillis()) {
            sleep = mGameData.sleepTo - System.currentTimeMillis();
        }
        this.mTimerForStateChange = false;
        this.mGameTimer.KillDeskTimer();
        this.mGameTimer.SetDeskTimer((int) sleep); // 立刻跳到游戏结束状态
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
            }
            break;
        }
    }

    private void game_over() {
        DispatchEvent event = new DispatchEvent();
        event.eventID = PokerConstants.PokerStateFinish;
        this.mDispatcher.StateDispatch(event);
    }

    @Override
    public void onDeskTimer() {
        // this.logger.info("deal , onDeskTimer is called");
        this.mGameTimer.KillDeskTimer();
        if (mGameData.pause) {//暂停中
            this.checkFinish();
            return;
        }

        if (!this.mTimerForStateChange) {
            int state = this.mGameData.getState();// 当前桌子的状态
            if (state == PokerConstants.GAME_TABLE_STATE_PLAYING) {// 玩家玩牌中,状态为4
                this.mCardLogic.gameTick(this.mGameData, this.mDesk);
            } else if (state == PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN) {
                game_over();
                return;
            }
            checkFinish();
            return;
        }

        DispatchEvent event = new DispatchEvent();
        event.eventID = PokerConstants.PokerStateFinish;
        this.mDispatcher.StateDispatch(event);
    }

    @Override
    public void onPlayerTimerEvent(int position) {
        logger.info("================进入玩家定时事件============");
    }

    @Override
    public void onExit() {

    }

    @Override
    public void handlePlayerHangup(int position) {
//		this.mCardLogic.playerAutoOper(this.mGameData, this.mDesk, position);
    }
}
