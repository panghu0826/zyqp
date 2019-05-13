package com.buding.poker.states;

import com.buding.api.context.GameContext;
import com.buding.api.context.PokerZJHFinalResult;
import com.buding.api.context.PokerZJHResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.zjh.ZJHProcessor;
import com.buding.poker.helper.ZJHPokerPushHelper;
import com.googlecode.protobuf.format.JsonFormat;
import packet.zjh.ZJH;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 结算状态
 */
public class ZJHStateFinish extends PokerStateCommon {
    GameContext ctx = null;
    boolean skipHuSettle = false;

    @Override
    public void handlePlayerStatusChange(int position) {

    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=finish;position={}", info.position);
        this.mGameData.mPlayers[info.position] = info;
        if (this.mDesk.hasNextGame(ctx) && info.position >= 0) pushPlayerHuMsg(this.mGameData, this.mDesk ,info.position);
        super.handleReconnectFor(info);
    }
    private void pushPlayerHuMsg(GameData gameData, MJDesk<byte[]> desk,int pos) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----重新推送单局结算-----");
        ZJH.ZJHGameOperPlayerHuSyn.Builder zjh = ZJH.ZJHGameOperPlayerHuSyn.newBuilder();
        zjh.setJuNum(gameData.zjhResult.juNum);
        zjh.setPosition(gameData.mGameWin.position);
        for (PokerZJHResult result : gameData.zjhResult.Result.values()) {
            if (result.pos < 0) return;
            if (result.playerId <= 0) continue;
            ZJH.ZJHGameOperPlayerSettle.Builder sy = ZJH.ZJHGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.addAllHandcard(ZJHProcessor.byte2IntList(gameData.mPlayerCards[result.pos].cardsInHand));
            sy.setLastScore(result.lastScore);
            sy.setScore(result.score);
            sy.setAllSocre(result.allSocre);
            sy.setXiQian(result.xiQian);
            sy.setIsQiPai(result.isQiPai);
            sy.setIsKanPai(result.isKanPai);
            sy.setCardType(result.cardType);
            sy.setIsBanker(result.pos == gameData.robIndex ? 1 : 0);
            sy.setResult(result.result);
            zjh.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(zjh.build()));
        ZJHPokerPushHelper.pushPlayerHuMsg(desk,pos,zjh, PokerConstants.SEND_TYPE_SINGLE);
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
        ctx.zjhResult = this.mGameData.zjhResult;
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
            ctx.zjhFinalResult = this.mGameData.zjhFinalResult;
            ctx.zjhFinalResult.endTime = System.currentTimeMillis();
            this.mDesk.finalSettle(ctx);
            this.mDesk.onGameOver();
            return;
        }

        for(PlayerInfo pl : mGameData.mPlayers){
            if(pl == null || pl.isWait || pl.isZanLi) continue;
            pl.gameCount++;
            if(pl.gameCount == 1) mDesk.subServiceFee(pl);
        }

        int waitSeconds = 15;
        // 有没有下一局
        if (!this.mDesk.hasNextGame(ctx)) {//没有下一局
            if (this.mDesk.isVipTable()) {
				// 推送总结算画面
                skipHuSettle = true;
                ctx.zjhFinalResult = this.mGameData.zjhFinalResult;
                this.mDesk.handSettle(ctx);
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                waitSeconds = 5; //3秒后到总结算页面
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
                ctx.zjhFinalResult = this.mGameData.zjhFinalResult;
                ctx.zjhFinalResult.endTime = System.currentTimeMillis();
                this.mDesk.finalSettle(ctx);
                this.mDesk.onGameOver();
                waitSeconds = 25;
            }
        } else {//有下一局
            ctx.zjhFinalResult = this.mGameData.zjhFinalResult;
            this.mDesk.handSettle(ctx);
            pushPlayerHuMsg(this.mGameData, this.mDesk);
            if(this.mDesk.isClubJiFenDesk()) {
                for (PlayerInfo p : ctx.needKickOutPlayers.values()) {
                    this.mDesk.kickout(p.playerId, "您的积分不够了");
                }
            }
            this.mDesk.ready4NextGame(ctx);
        }

        this.mGameData.mActor.gameState = PokerConstants.PokerStateFinish;
        this.mGameTimer.SetDeskTimer(waitSeconds * 1000);
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
        ZJH.ZJHGameOperFinalSettleSyn.Builder gb = ZJH.ZJHGameOperFinalSettleSyn.newBuilder();
//        设置房间号
        gb.setRoomId(Integer.valueOf(desk.getDeskID()));
//        设置游戏圈数
        gb.setInnings(desk.getTotalQuan());
        for (PokerZJHFinalResult r : gameData.zjhFinalResult.finalResults.values()) {
            if (r.playerId <= 0) continue;

            ZJH.ZJHPlayerFinalResult.Builder pb = ZJH.ZJHPlayerFinalResult.newBuilder();
            pb.setPlayerId(r.playerId);
            pb.setPlayerName(r.playerName);
            pb.setPosition(r.pos);
            pb.setHeadImage(r.headImg);
            pb.setAllScore(r.allScore);
            pb.setMaxScore(r.maxScore);
            pb.setMaxCardType(r.maxCardType);
            pb.setWinNum(r.winNum);
            pb.setLoseNum(r.loseNum);
            pb.setRoomOwner(desk.getDeskOwner() == r.playerId) ;
            gb.addDetail(pb);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--总结算--"+ JsonFormat.printToString(gb.build()));

        ZJHPokerPushHelper.pushFinalSettleInfo(desk, 0, gb, PokerConstants.SEND_TYPE_ALL);
    }

    private void pushPlayerHuMsg(GameData gameData, MJDesk<byte[]> desk) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----单局结算-----");
        ZJH.ZJHGameOperPlayerHuSyn.Builder zjh = ZJH.ZJHGameOperPlayerHuSyn.newBuilder();
        zjh.setJuNum(gameData.zjhResult.juNum);
        zjh.setPosition(gameData.mGameWin.position);

        List<Integer> currentUserIdList = new ArrayList<>();
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p == null) continue;
            currentUserIdList.add(p.playerId);
        }

        for (PokerZJHResult result : gameData.zjhResult.Result.values()) {
            if (result.playerId <= 0 || !currentUserIdList.contains(result.playerId)) continue;
            ZJH.ZJHGameOperPlayerSettle.Builder sy = ZJH.ZJHGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.addAllHandcard(ZJHProcessor.byte2IntList(gameData.mPlayerCards[result.pos].cardsInHand));
            sy.setLastScore(result.lastScore);
            sy.setScore(result.score);
            sy.setAllSocre(result.allSocre);
            sy.setXiQian(result.xiQian);
            sy.setIsQiPai(result.isQiPai);
            sy.setIsKanPai(result.isKanPai);
            sy.setCardType(result.cardType);
            sy.setIsBanker(result.pos == gameData.robIndex ? 1 : 0);
            sy.setResult(result.result);
            zjh.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(zjh.build()));
        ZJHPokerPushHelper.pushPlayerHuMsg(desk,-100,zjh, PokerConstants.SEND_TYPE_ALL);
    }

    @Override
    public void onDeskTimer() {
        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onDeskTimer is called");
        this.mGameTimer.KillDeskTimer();

        if (skipHuSettle) {
            skipHuSettle = false;
            ctx.zjhFinalResult = this.mGameData.zjhFinalResult;
            ctx.zjhFinalResult.endTime = System.currentTimeMillis();
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
