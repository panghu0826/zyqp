package com.buding.poker.states;

import com.buding.api.context.GameContext;
import com.buding.api.context.PokerErBaFinalResult;
import com.buding.api.context.PokerErBaResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.ErBaPokerPushHelper;
import com.buding.poker.erba.ErBaProcessor;
import com.googlecode.protobuf.format.JsonFormat;
import packet.erba.ErBa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 结算状态
 */
public class ErBaStateFinish extends PokerStateCommon {
    GameContext ctx = null;
    boolean skipHuSettle = false;

    @Override
    public void handlePlayerStatusChange(int position) {

    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=finish;position={}", info.position);
        if (info.position >= 0) this.mGameData.mPlayers[info.position] = info;
        super.handleReconnectFor(info);
    }

    @Override
    public void onEnter() {

        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onEnter is called");
        this.mGameTimer.KillDeskTimer();

        skipHuSettle = false;

        ctx = new GameContext();

        ctx.handNum = this.mGameData.handNum;
        ctx.nextHandNum = this.mGameData.handNum + 1;
        ctx.bankerPos = this.mGameData.currentRobIndex;
        ctx.winerPos = this.mGameData.mGameWin.position;
        ctx.erBaResult = this.mGameData.erBaResult;

        for(PlayerInfo p : this.mDesk.getPlayingPlayers()){
            if(p == null) continue;
            ctx.playingPlayers.put(p.playerId,p);
        }

        //是否解散,解散时需要计算杠得分
        if (this.mGameData.dismissing) {
            logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----房间解散-----");

            if (this.mDesk.isVipTable()) {
                // 推送总结算画面
                pushFinalSettleMsg(this.mGameData, this.mDesk);
            }
            this.mGameTimer.KillDeskTimer();
            ctx.erBaFinalResult = this.mGameData.erBaFinalResult;
            ctx.erBaFinalResult.endTime = System.currentTimeMillis();
            this.mDesk.finalSettle(ctx);
            this.mDesk.onGameOver();
            return;
        }

        for(PlayerInfo pl : mGameData.erBaCurrentGamingPlayers){
            if(pl == null) continue;
            pl.gameCount++;
        }

        int waitSeconds = 10;
        if (mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) waitSeconds = 13;
        // 有没有下一局
        if (!this.mDesk.hasNextGame(ctx)) {//没有下一局
            if (this.mDesk.isVipTable()) {
				// 推送总结算画面
                skipHuSettle = true;
                ctx.erBaFinalResult = this.mGameData.erBaFinalResult;

                if (mGameData.erBaSettleType) this.mDesk.handSettle(ctx);
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                waitSeconds = 5; //3秒后到总结算页面
                if (mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) waitSeconds = 8;
                if(this.mDesk.isClubJiFenDesk()) {
                    for (PlayerInfo p : ctx.needKickOutPlayers.values()) {
                        this.mDesk.kickout(p.playerId, "您的积分不够了");
                    }
                }
            } else {
                //发送胡牌消息,同时扣除台费
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                //同步大厅玩家数据(每局的输赢数据等).最终会与前端同步UserInfoSyn
                this.mDesk.handSettle(ctx);
                ctx.erBaFinalResult = this.mGameData.erBaFinalResult;
                ctx.erBaFinalResult.endTime = System.currentTimeMillis();
                this.mDesk.finalSettle(ctx);
                this.mDesk.onGameOver();
                waitSeconds = 25;
            }
        } else {//有下一局
            ctx.erBaFinalResult = this.mGameData.erBaFinalResult;
            if (mGameData.erBaSettleType) this.mDesk.handSettle(ctx);
            pushPlayerHuMsg(this.mGameData, this.mDesk);
            if(this.mDesk.isClubJiFenDesk()) {
                for (PlayerInfo p : ctx.needKickOutPlayers.values()) {
                    this.mDesk.kickout(p.playerId, "您的积分不够了");
                }
            }
            this.mDesk.ready4NextGame(ctx);
            this.mDesk.clearXiaZhuPlayer();
            if (mGameData.robIndex > 0) {
                mDesk.erBaXiaZhuOrConfirmBanker(mGameData.mPlayersMap.get(mGameData.robIndex));
            }

        }

        this.mGameData.mActor.gameState = PokerConstants.PokerStateFinish;
        this.mGameTimer.SetDeskTimer(waitSeconds * 1000);

//        if(!mDesk.canForceChuPai() && !mDesk.canTuoGuan()) this.mGameTimer.KillDeskTimer();
    }

    private void pushFinalSettleMsg(GameData gameData, MJDesk desk) {
        int playerNum = 0;
        //添加记录
        for(PlayerInfo pl : mGameData.mPlayers){
            if(pl == null) continue;
            playerNum++;
        }
        desk.addUserConsumeDiamondLog(playerNum);

        //总结算OK
        ErBa.ErBaGameOperFinalSettleSyn.Builder gb = ErBa.ErBaGameOperFinalSettleSyn.newBuilder();
//        设置房间号
        gb.setRoomId(Integer.valueOf(desk.getDeskID()));
//        设置游戏圈数
        gb.setInnings(desk.getTotalQuan());
        for (PokerErBaFinalResult r : gameData.erBaFinalResult.finalResults.values()) {
            if (r.playerId <= 0) continue;

            ErBa.ErBaPlayerFinalResult.Builder pb = ErBa.ErBaPlayerFinalResult.newBuilder();
            pb.setPlayerId(r.playerId);
            pb.setPlayerName(r.playerName);
            pb.setHeadImage(r.headImg);
            pb.setAllScore(r.allScore);
            pb.setMaxScore(r.maxScore);
            pb.setMaxCardType(r.maxCardType);
            pb.setWinNum(r.winNum);
            pb.setLoseNum(r.loseNum);
            pb.setRoomOwner(desk.getDeskOwner() == r.playerId) ;
            if (desk.getErBaGameType() != PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                pb.setDuiHongZhongNum(r.duiHongZhongNum);
                pb.setDuiZiNum(r.duiZiNum);
                pb.setErBaNum(r.erBaNum);
                pb.setSanPai89Num(r.sanPai89Num);
            }
            gb.addDetail(pb);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--总结算--"+ JsonFormat.printToString(gb.build()));

        ErBaPokerPushHelper.pushFinalSettleInfo(desk, null, gb, PokerConstants.SEND_TYPE_ALL);
    }

    private void pushPlayerHuMsg(GameData gameData, MJDesk<byte[]> desk) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----单局结算-----");
        ErBa.ErBaGameOperPlayerHuSyn.Builder erBa = ErBa.ErBaGameOperPlayerHuSyn.newBuilder();
        erBa.setJuNum(gameData.handNum);
        erBa.setPlayerId(gameData.mGameWin.playerId);
        erBa.setErBaGameType(desk.getErBaGameType());
        erBa.setSettleType(gameData.erBaSettleType);

        if (gameData.erBaSettleType) {
            List<Integer> currentUserIdList = new ArrayList<>();
            for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                if (p == null) continue;
                currentUserIdList.add(p.playerId);
            }

            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                for (int i = 0; i < 4; i++) {
                    ErBa.ErBaGameOperPlayerSettle.Builder sy = ErBa.ErBaGameOperPlayerSettle.newBuilder();
                    sy.setSiMenType(i);
                    sy.addAllHandcard(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(i).cardsInHand));
                    erBa.addDetail(sy);
                }
                for (PokerErBaResult result : gameData.erBaResult.Result.values()) {
                    if (result.playerId <= 0 || !currentUserIdList.contains(result.playerId)) continue;
                    ErBa.ErBaGameOperPlayerSettle.Builder sy = ErBa.ErBaGameOperPlayerSettle.newBuilder();
                    sy.setPlayerId(result.playerId);
                    sy.setPlayerName(result.playerName);
                    sy.setLastScore(result.lastScore);
                    sy.setScore(result.score);
                    sy.setAllSocre(result.allScore);
                    sy.setCardNum(result.cardNum);
                    sy.setCardType(result.cardType);
                    sy.setIsBanker(result.playerId == gameData.robIndex ? 1 : 0);
                    sy.setResult(result.result);
                    erBa.addDetail(sy);
                }
            } else {
                for (PokerErBaResult result : gameData.erBaResult.Result.values()) {
                    if (result.playerId <= 0 || !currentUserIdList.contains(result.playerId)) continue;
                    ErBa.ErBaGameOperPlayerSettle.Builder sy = ErBa.ErBaGameOperPlayerSettle.newBuilder();
                    sy.setPlayerId(result.playerId);
                    sy.setPlayerName(result.playerName);
                    sy.addAllHandcard(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(result.playerId) == null ? new ArrayList<>() : gameData.mPlayerCardsMap.get(result.playerId).cardsInHand));
                    sy.setLastScore(result.lastScore);
                    sy.setScore(result.score);
                    sy.setAllSocre(result.allScore);
                    sy.setCardNum(result.cardNum);
                    sy.setCardType(result.cardType);
                    sy.setIsBanker(result.playerId == gameData.robIndex ? 1 : 0);
                    sy.setResult(result.result);
                    erBa.addDetail(sy);
                }
            }
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(erBa.build()));
        ErBaPokerPushHelper.pushPlayerHuMsg(desk,null,erBa, PokerConstants.SEND_TYPE_ALL);
    }

    @Override
    public void onDeskTimer() {
        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onDeskTimer is called");
        this.mGameTimer.KillDeskTimer();

        int num = 0;
        if(mDesk.canForceChuPai()){//30秒不准备暂离
            //判断下这时候坐下的玩家
            for(PlayerInfo p : mGameData.mPlayers){
                if(p == null || p.isZanLi) continue;
                num++;
            }
        }

        if (skipHuSettle || num <= 2) {
            skipHuSettle = false;
            ctx.erBaFinalResult = this.mGameData.erBaFinalResult;
            ctx.erBaFinalResult.endTime = System.currentTimeMillis();
            pushFinalSettleMsg(this.mGameData, this.mDesk);
            this.mDesk.finalSettle(ctx);//TODO
            this.mDesk.onGameOver();
            this.mGameData.mActor.gameState = PokerConstants.PokerStateFinish;
            this.mGameTimer.SetDeskTimer(1 * 1000);//一秒后进入下一局
            return;
        }

        if (this.mDesk.hasNextGame(ctx)) {
            this.mDesk.startNextGame(ctx);
        }
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
}
