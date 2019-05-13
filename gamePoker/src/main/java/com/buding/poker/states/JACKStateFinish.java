package com.buding.poker.states;

import com.buding.api.context.GameContext;
import com.buding.api.context.PokerJACKFinalResult;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 结算状态
 */
public class JACKStateFinish extends PokerStateCommon {
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

        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onEnter is called");
        this.mGameTimer.KillDeskTimer();

        skipHuSettle = false;

        ctx = new GameContext();

        ctx.handNum = this.mGameData.handNum;
        ctx.nextHandNum = this.mGameData.handNum + 1;
        ctx.bankerPos = this.mGameData.currentRobIndex;
        ctx.winerPos = this.mGameData.mGameWin.position;
        ctx.jackResult = this.mGameData.jackResult;

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
            ctx.jackFinalResult = this.mGameData.jackFinalResult;
            ctx.jackFinalResult.endTime = System.currentTimeMillis();
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
        if(mDesk.canForceChuPai()) waitSeconds = 30;
        // 有没有下一局
        if (!this.mDesk.hasNextGame(ctx)) {//没有下一局
            if (this.mDesk.isVipTable()) {
				// 推送总结算画面
                skipHuSettle = true;
                ctx.jackFinalResult = this.mGameData.jackFinalResult;

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
                ctx.jackFinalResult = this.mGameData.jackFinalResult;
                ctx.jackFinalResult.endTime = System.currentTimeMillis();
                this.mDesk.finalSettle(ctx);
                this.mDesk.onGameOver();
                waitSeconds = 25;
            }
        } else {//有下一局
            ctx.jackFinalResult = this.mGameData.jackFinalResult;
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
        JACK.JACKGameOperFinalSettleSyn.Builder gb = JACK.JACKGameOperFinalSettleSyn.newBuilder();
//        设置房间号
        gb.setRoomId(Integer.valueOf(desk.getDeskID()));
//        设置游戏圈数
        gb.setInnings(desk.getTotalQuan());
        for (PokerJACKFinalResult r : gameData.jackFinalResult.finalResults.values()) {
            if (r.playerId <= 0) continue;

            JACK.JACKPlayerFinalResult.Builder pb = JACK.JACKPlayerFinalResult.newBuilder();
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

        JACKPokerPushHelper.pushFinalSettleInfo(desk, 0, gb, PokerConstants.SEND_TYPE_ALL);
    }

    private void pushPlayerHuMsg(GameData gameData, MJDesk<byte[]> desk) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----单局结算-----");
        JACK.JACKGameOperPlayerHuSyn.Builder jack = JACK.JACKGameOperPlayerHuSyn.newBuilder();
        jack.setJuNum(gameData.jackResult.juNum);
        jack.setPosition(gameData.mGameWin.position);

        List<Integer> currentUserIdList = new ArrayList<>();
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p == null) continue;
            currentUserIdList.add(p.playerId);
        }

        for (PokerJACKResult result : gameData.jackResult.Result.values()) {
            if (result.playerId <= 0 || !currentUserIdList.contains(result.playerId)) continue;
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
        JACKPokerPushHelper.pushPlayerHuMsg(desk,-100,jack, PokerConstants.SEND_TYPE_ALL);
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
            ctx.jackFinalResult = this.mGameData.jackFinalResult;
            ctx.jackFinalResult.endTime = System.currentTimeMillis();
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
