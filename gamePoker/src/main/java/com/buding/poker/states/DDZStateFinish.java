package com.buding.poker.states;

import com.buding.api.context.*;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.ddz.DDZProcessor;
import com.buding.poker.helper.DDZPokerPushHelper;
import com.googlecode.protobuf.format.JsonFormat;
import packet.ddz.DDZ;
import packet.game.MsgGame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description: 结算状态
 */
public class DDZStateFinish extends PokerStateCommon {
    GameContext ctx = null;
    boolean skipHuSettle = false;

    @Override
    public void handlePlayerStatusChange(int position) {

    }

    @Override
    public void handleReconnectFor(PlayerInfo info) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"act=handleReconnectFor;state=finish;position={}", info.position);
        if(info.position >= 0) {
            this.mGameData.mPlayers[info.position] = info;
            if (this.mDesk.hasNextGame(ctx)) pushPlayerHuMsg(this.mGameData, this.mDesk, info.position);
        }
        super.handleReconnectFor(info);
    }
    private void pushPlayerHuMsg(GameData gameData, MJDesk desk,int pos) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----重新推送单局结算-----");
        DDZ.DDZGameOperPlayerHuSyn.Builder ddz = DDZ.DDZGameOperPlayerHuSyn.newBuilder();
        ddz.setFriedKing(gameData.ddzResult.friedKing);
        ddz.setBomb(gameData.ddzResult.bomb);
        ddz.setSpring(gameData.ddzResult.spring);
        ddz.setEndPoints(gameData.ddzResult.endPoints);
        ddz.setInnings(gameData.ddzResult.innings);
        ddz.setPosition(gameData.mGameWin.position);
        ddz.addAllDiPai(DDZProcessor.byte2IntList(gameData.mDeskCard.ddzCards));
        for (PokerDDZResult result : gameData.ddzResult.Result.values()) {
            if (result.pos < 0) return;
            if (result.playerId <= 0) {
                continue;
            }
            DDZ.DDZGameOperPlayerSettle.Builder sy = DDZ.DDZGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.setAllSocre(result.allScore);
            sy.setIsDiZhu(result.isDiZhu);
            sy.setIsDouble(result.isDouble);
            sy.setMultiple(result.multiple);
            sy.setSocre(result.score);
            sy.addAllHandcard(DDZProcessor.byte2IntList(gameData.mPlayerCards[result.pos].cardsInHand));
            ddz.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(ddz.build()));
        DDZPokerPushHelper.pushPlayerHuMsg(desk,pos,ddz, PokerConstants.SEND_TYPE_SINGLE);
    }


    @Override
    public void onEnter() {

        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onEnter is called");
        this.mGameTimer.KillDeskTimer();

        skipHuSettle = false;

        ctx = new GameContext();

        for(PlayerInfo p : this.mDesk.getPlayingPlayers()){
            if(p == null) continue;
            ctx.playingPlayers.put(p.playerId,p);
        }

        ctx.handNum = this.mGameData.handNum;
        ctx.nextHandNum = this.mGameData.handNum + 1;
        ctx.bankerPos = this.mGameData.currentRobIndex;
        ctx.winerPos = this.mGameData.mGameWin.position;
        ctx.ddzResult = this.mGameData.ddzResult;

        //扣除台费 并添加记录
        if(mGameData.handNum == 1){
            mDesk.addUserConsumeDiamondLog(3);
            for(PlayerInfo pl : mGameData.mPlayers){
                if(pl == null) continue;
                pl.gameCount++;
                mDesk.subServiceFee(pl);
            }
        }
        //是否解散,解散时需要计算杠得分
        if (this.mGameData.dismissing) {
            logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----房间解散-----");

            if (this.mDesk.isVipTable()) {
                // 推送总结算画面
                pushFinalSettleMsg(this.mGameData, this.mDesk);
            }
            this.mGameTimer.KillDeskTimer();
            ctx.ddzFinalResult = this.mGameData.ddzFinalResult;
            ctx.ddzFinalResult.endTime = System.currentTimeMillis();
            this.mDesk.finalSettle(ctx);
            this.mDesk.onGameOver();
            return;
        }

        int waitSeconds = 25;
        if (this.mDesk.getPlayers().size() == 3) {
            waitSeconds = 25;
        }
        // 有没有下一局
        if (this.mDesk.hasNextGame(ctx) == false) {//没有下一局
            if (this.mDesk.isVipTable()) {
				// 推送总结算画面
                skipHuSettle = true;
                ctx.ddzFinalResult = this.mGameData.ddzFinalResult;
                this.mDesk.handSettle(ctx);
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                waitSeconds = 3; //3秒后到总结算页面
                if(this.mDesk.isClubJiFenDesk()) {
                    for (PlayerInfo p : ctx.needKickOutPlayers.values()) {
                        this.mDesk.kickout(p.playerId, "您的积分不够了");
                    }
                }
            } else /*if (this.mDesk.isMultiMatch()) {
                //发送胡牌消息,同时扣除台费
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                this.mDesk.handSettle(ctx);
                //所有人都打完每轮的局数,开始下一轮,否则等待下一轮开始
                if (this.mDesk.isAllBiSaiDeskFinishOneLun()) {
                    this.mDesk.multiMatchStartNotify();
                } else {
                    WaitNextMatchStart();
                    this.mDesk.onGameOver();
                }
                return;
            } else*/ {
                //发送胡牌消息,同时扣除台费
                pushPlayerHuMsg(this.mGameData, this.mDesk);
                //同步大厅玩家数据(每局的输赢数据等).最终会与前端同步UserInfoSyn
                this.mDesk.handSettle(ctx);
                ctx.ddzFinalResult = this.mGameData.ddzFinalResult;
                ctx.ddzFinalResult.endTime = System.currentTimeMillis();
                this.mDesk.finalSettle(ctx);
                this.mDesk.onGameOver();
                waitSeconds = 25;
            }
        } else {//有下一局
            ctx.ddzFinalResult = this.mGameData.ddzFinalResult;
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

    private void WaitNextMatchStart() {
        MsgGame.WaitNextMatchStart.Builder pb = MsgGame.WaitNextMatchStart.newBuilder();
        DDZPokerPushHelper.pushWaitNextMatchStart(this.mDesk, pb);
    }

    private void pushFinalSettleMsg(GameData gameData, MJDesk desk) {
        //总结算OK
        DDZ.DDZGameOperFinalSettleSyn.Builder gb = DDZ.DDZGameOperFinalSettleSyn.newBuilder();
//        设置房间号
        gb.setRoomId(Integer.valueOf(desk.getDeskID()));
//        设置游戏圈数
        gb.setInnings(desk.getTotalQuan());
        for (PokerDDZFinalResult r : gameData.ddzFinalResult.finalResults.values()) {
            if (r.playerId <= 0) {
                continue;
            }
            DDZ.DDZPlayerFinalResult.Builder pb = DDZ.DDZPlayerFinalResult.newBuilder();
            pb.setPlayerId(r.playerId);
            pb.setPosition(r.pos);
            pb.setRoomOwner(desk.getDeskOwner() == r.playerId) ;
            pb.setAllScore(r.allScore);
            pb.setHeadImage(r.headImg);
            pb.setWinInnings(r.winInnings);
            pb.setPlayerName(r.playerName);
            gb.addDetail(pb);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--总结算--"+ JsonFormat.printToString(gb.build()));

        DDZPokerPushHelper.pushFinalSettleInfo(desk, 0, gb, PokerConstants.SEND_TYPE_ALL);
    }

    private void pushPlayerHuMsg(GameData gameData, MJDesk<byte[]> desk) {
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"-----单局结算-----");
        DDZ.DDZGameOperPlayerHuSyn.Builder ddz = DDZ.DDZGameOperPlayerHuSyn.newBuilder();
        ddz.setFriedKing(gameData.ddzResult.friedKing);
        ddz.setBomb(gameData.ddzResult.bomb);
        ddz.setSpring(gameData.ddzResult.spring);
        ddz.setEndPoints(gameData.ddzResult.endPoints);
        ddz.setInnings(gameData.ddzResult.innings);
        ddz.setPosition(gameData.mGameWin.position);
        ddz.addAllDiPai(DDZProcessor.byte2IntList(gameData.mDeskCard.ddzCards));

        List<Integer> currentUserIdList = new ArrayList<>();
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p == null) continue;
            currentUserIdList.add(p.playerId);
        }
        for (PokerDDZResult result : gameData.ddzResult.Result.values()) {
            if (result.playerId <= 0 || !currentUserIdList.contains(result.playerId)) {
                continue;
            }
            DDZ.DDZGameOperPlayerSettle.Builder sy = DDZ.DDZGameOperPlayerSettle.newBuilder();
            sy.setPosition(result.pos);
            sy.setPlayerId(result.playerId);
            sy.setPlayerName(result.playerName);
            sy.setAllSocre(result.allScore);
            sy.setIsDiZhu(result.isDiZhu);
            sy.setIsDouble(result.isDouble);
            sy.setMultiple(result.multiple);
            sy.setSocre(result.score);
            sy.addAllHandcard(DDZProcessor.byte2IntList(gameData.mPlayerCards[result.pos].cardsInHand));
            ddz.addDetail(sy);
        }
        logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"--settle--"+ JsonFormat.printToString(ddz.build()));
        DDZPokerPushHelper.pushPlayerHuMsg(desk,-100,ddz, PokerConstants.SEND_TYPE_ALL);
    }

    @Override
    public void onDeskTimer() {
        this.logger.info("桌子id--"+this.mDesk.getDeskID()+"--"+"finish , onDeskTimer is called");
        this.mGameTimer.KillDeskTimer();

        if (skipHuSettle) {
            skipHuSettle = false;
            ctx.ddzFinalResult = this.mGameData.ddzFinalResult;
            ctx.ddzFinalResult.endTime = System.currentTimeMillis();
            pushFinalSettleMsg(this.mGameData, this.mDesk);
            this.mDesk.finalSettle(ctx);
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
