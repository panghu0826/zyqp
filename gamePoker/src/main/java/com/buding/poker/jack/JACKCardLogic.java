package com.buding.poker.jack;

import com.buding.api.context.PokerJACKFinalResult;
import com.buding.api.context.PokerJACKResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.JACKPLayerHandRecordData;
import com.buding.api.player.JACKRecordData;
import com.buding.api.player.PlayerInfo;
import com.buding.api.player.RecordData;
import com.buding.card.ICardLogic;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.game.GamePacket;
import com.buding.poker.common.JACKRule;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.JACKHelper;
import com.buding.poker.helper.JACKMsgBuilder;
import com.buding.poker.helper.JACKPokerPushHelper;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.jack.JACK;
import packet.jack.JACK.JACKGameOperPlayerActionSyn;
import packet.mj.MJBase;

import java.util.*;

public class JACKCardLogic implements ICardLogic<MJDesk<byte[]>> {

    private Logger log = LogManager.getLogger("DESKLOG");
    private GameData gameData;
    private MJDesk<byte[]> desk;

    @Override
    public void init(GameData gameData, MJDesk<byte[]> desk) {
        this.gameData = gameData;
        this.desk = desk;
    }

    @Override
    public void handleSetGamingData(GameCardDealer mCardDealer, GameData gameData, MJDesk<byte[]> desk, String json) {

    }

    @Override
    public void gameTick(GameData data, MJDesk<byte[]> desk) {
        long ctt = System.currentTimeMillis();

        PlayerInfo currentPl = desk.getDeskPlayer(data.getPokerOpPlayerIndex());

        //获取玩家的子原因状态
        int substate = gameData.getPlaySubstate();

        switch (substate) {
            case PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS: {
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS;
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_SEND_CARD: {
                //记录超时
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //重连配置
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
                //发牌
                sendCards(data,desk);
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD;
                //提醒玩家操作
                player_chu_notify(data, desk);
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_NONE: {
                //下注阶段判断所有闲家
                if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS){
                    for(PlayerInfo p : (List<PlayerInfo>)desk.getPlayingPlayers()){
                        if(p == null || p.position == gameData.robIndex) continue;
                        long time = gameData.mPlayerAction[p.position].opStartTime;
                        boolean isTimeout = (ctt - time) > (gameData.mGameParam.operTimeOutSeconds * 1000);
                        if ((isTimeout && time != 0) && (desk.canTuoGuan() || desk.canForceChuPai())) playerAutoXiaZhu(data, desk, p.position);
                    }
                }else{
                    //超时处理,斗地主只有在玩家只有过选项时,自动处理过
                    long time = gameData.mPlayerAction[currentPl.position].opStartTime;
                    boolean isTimeout = (ctt - time) > (gameData.mGameParam.operTimeOutSeconds * 1000);
                    if(gameData.mPlayerCards[currentPl.position].cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI){
                        isTimeout = (ctt - time) > (5 * 1000);
                        if (isTimeout && time != 0 && currentPl.position != gameData.robIndex)
                            playerAutoOper(data, desk, data.getPokerOpPlayerIndex());
                    }else {
                        if (isTimeout && time != 0
                                && (desk.canTuoGuan() || desk.canForceChuPai())
                                && currentPl.position != gameData.robIndex)
                            playerAutoOper(data, desk, data.getPokerOpPlayerIndex());
                    }
                }
            }
            break;
        }
    }

    private void playerAutoXiaZhu(GameData gameData, MJDesk<byte[]> desk, int position) {
        //能自动弃牌
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家自动下注---");
        JACKGameOperPlayerActionSyn.Builder msg = JACKGameOperPlayerActionSyn.newBuilder();
        msg.setPosition(position);
        msg.setAction(PokerConstants.JACK_OPERTAION_XIA_ZHU);
        int xiaZhu = 0;
        if(desk.getYaZhu() == PokerConstants.JACK_WU_FEN_CAHNG){
            if(desk.canXiaManZhu()){
                xiaZhu = desk.getYaZhu();
            }else{
                xiaZhu = 1;
            }
        }else if (desk.getYaZhu() == PokerConstants.JACK_SHI_FEN_CAHNG){
            if(desk.canXiaManZhu()){
                xiaZhu = desk.getYaZhu();
            }else{
                xiaZhu = 2;
            }
        }else if(desk.getYaZhu() == PokerConstants.JACK_ER_SHI_FEN_CAHNG){
            if(desk.canXiaManZhu()){
                xiaZhu = desk.getYaZhu();
            }else{
                xiaZhu = 3;
            }
        }
        msg.setChouMa(xiaZhu);
        this.playerOperation(gameData,desk,msg,gameData.mPlayers[position]);
    }

    @Override
    public void playerAutoOper(GameData gameData, MJDesk<byte[]> gt, int position) {
        if(gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_JACK){
            //自动不压五小龙
            log.info("桌子id--" + desk.getDeskID() + "--" + "--玩家自动不压五小龙---");
            JACKGameOperPlayerActionSyn.Builder msg = JACKGameOperPlayerActionSyn.newBuilder();
            msg.setPosition(position);
            msg.setAction(PokerConstants.JACK_OPERTAION_BU_YA_WU_XIAO_LONG);
            this.playerOperation(gameData, gt, msg, gameData.mPlayers[position]);
        }else {
            //自动停牌
            log.info("桌子id--" + desk.getDeskID() + "--" + "--玩家自动停牌---");
            JACKGameOperPlayerActionSyn.Builder msg = JACKGameOperPlayerActionSyn.newBuilder();
            msg.setPosition(position);
            msg.setAction(PokerConstants.JACK_OPERTAION_TING_PAI);
            this.playerOperation(gameData, gt, msg, gameData.mPlayers[position]);
        }
    }

    /**
     * 玩家操作后不切换角色,在提示的时候根据牌型决定切不切换
     */
    @Override
    public void player_chu_notify(GameData gameData, MJDesk<byte[]> desk) {
        PlayerInfo plx = desk.getDeskPlayer(gameData.getPokerOpPlayerIndex());
        if (plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为" + gameData.getPokerOpPlayerIndex());
            return;
        }
        JACK.JACKGameOperPlayerActionNotify.Builder msg = JACK.JACKGameOperPlayerActionNotify.newBuilder();

        if(gameData.mPlayerCards[plx.position].cardType != PokerConstants.JACK_CARDTYPE_COMMON){
            if(plx.position == gameData.robIndex){
                log.info("桌子id--"+desk.getDeskID()+"--"+"---当前庄家--" + plx.name+"--牌型不是普通牌型只能验牌--");
                msg.setActions(PokerConstants.JACK_OPERTAION_YAN_PAI);
                for(PlayerInfo p : desk.getPlayingPlayers()){
                    if(p == null || p.yanPaiResult >= 0 || p.position == gameData.robIndex) continue;
                    msg.addYanPaiPos(p.position);
                }
            }else{
                if(gameData.mPlayerCards[plx.position].cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI){
                    log.info("桌子id--" + desk.getDeskID() + "--" + "---当前玩家--" + plx.name + "--爆牌给5秒停牌--");
                    msg.setActions(PokerConstants.JACK_OPERTAION_TING_PAI);
                }else {
                    log.info("桌子id--" + desk.getDeskID() + "--" + "---当前玩家--" + plx.name + "--牌型不是普通牌也不是爆牌跳过--");
                    List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(plx.position, 1, 0);
                    gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
                    return;
                }
            }
        }else{
            if(plx.position == gameData.robIndex){
                msg.setActions(PokerConstants.JACK_OPERTAION_YAN_PAI);
                if(!plx.isTingPai) msg.setActions(msg.getActions() | PokerConstants.JACK_OPERTAION_TING_PAI | PokerConstants.JACK_OPERTAION_YAO_PAI );
                for(PlayerInfo p : desk.getPlayingPlayers()){
                    if(p == null || p.yanPaiResult >= 0 || p.position == gameData.robIndex) continue;
                    msg.addYanPaiPos(p.position);
                }
            }else{
                if(gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_JACK) {//庄家是杰克,闲家普通牌型时只能选择赌小龙
                    msg.setActions(PokerConstants.JACK_OPERTAION_YA_WU_XIAO_LONG | PokerConstants.JACK_OPERTAION_BU_YA_WU_XIAO_LONG);
                }else{
                    if(plx.isTingPai) return;
                    msg.setActions(PokerConstants.JACK_OPERTAION_TING_PAI |PokerConstants.JACK_OPERTAION_YAO_PAI);
                }
            }
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"提示玩家--" + plx.name + "操作");

        msg.setPosition(plx.position);

        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = System.currentTimeMillis();
        //推送消息
        JACKPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        int timeOut = 15;
        if(gameData.mPlayerCards[plx.position].cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI) {
            JACKPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,timeOut, PokerConstants.SEND_TYPE_EXCEPT_ONE);
            timeOut = 5;
            JACKPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,timeOut, PokerConstants.SEND_TYPE_SINGLE);
        }else{
            JACKPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,timeOut, PokerConstants.SEND_TYPE_ALL);
        }
        //同步手牌
        JACKPokerPushHelper.pushHandCardSyn(gameData,desk,plx);

        //推送桌子消息
        for(PlayerInfo p : desk.getAllPlayers()) {
            desk.sendMsg2Player(p, JACKMsgBuilder.getPacketForPublicInfo(getDeskPublicInfoMsg(desk)).build().toByteArray());
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子消息"+JsonFormat.printToString(getDeskPublicInfoMsg(desk).build()));
    }

    @Override
    public void playerOperation(GameData gameData, MJDesk<byte[]> desk, GeneratedMessage.Builder m, PlayerInfo pl) {
        JACKGameOperPlayerActionSyn.Builder msg = (JACKGameOperPlayerActionSyn.Builder) m;

        if (msg == null || pl == null || msg.getAction() == 0) return;

        desk.setPauseTime(System.currentTimeMillis());
        //要牌
        if ((msg.getAction() & PokerConstants.JACK_OPERTAION_YAO_PAI) == PokerConstants.JACK_OPERTAION_YAO_PAI) {
            player_op_yaoPai(gameData, desk, msg, pl);
        }
        //停牌
        else if ((msg.getAction() & PokerConstants.JACK_OPERTAION_TING_PAI) == PokerConstants.JACK_OPERTAION_TING_PAI) {
            player_op_tingPai(gameData, desk, msg, pl);
        }
        //验牌
        else if ((msg.getAction() & PokerConstants.JACK_OPERTAION_YAN_PAI) == PokerConstants.JACK_OPERTAION_YAN_PAI) {
            player_op_yanPai(gameData, desk, msg, pl);
        }
        //下注
        else if ((msg.getAction() & PokerConstants.JACK_OPERTAION_XIA_ZHU) == PokerConstants.JACK_OPERTAION_XIA_ZHU) {
            player_op_xiaZhu(gameData, desk, msg, pl);
        }
        //压五小龙
        else if ((msg.getAction() & PokerConstants.JACK_OPERTAION_YA_WU_XIAO_LONG) == PokerConstants.JACK_OPERTAION_YA_WU_XIAO_LONG) {
            player_op_yaWuXiaoLong(gameData, desk, msg, pl);
        }
        //不压五小龙
        else if ((msg.getAction() & PokerConstants.JACK_OPERTAION_BU_YA_WU_XIAO_LONG) == PokerConstants.JACK_OPERTAION_BU_YA_WU_XIAO_LONG) {
            player_op_buYaWuXiaoLong(gameData, desk, msg, pl);
        }
        else {
            throw new RuntimeException("UnKnowOperation;");
        }

//        //推送桌子消息
//        for(PlayerInfo p : desk.getAllPlayers()) {
//            desk.sendMsg2Player(p, JACKMsgBuilder.getPacketForPublicInfo(getDeskPublicInfoMsg(desk)).build().toByteArray());
//        }
//
//        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子消息"+JsonFormat.printToString(getDeskPublicInfoMsg(desk).build()));
    }

    private void player_op_yaWuXiaoLong(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (pl.position != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        //游戏数据
        pl.multiple = 3;
        pl.yaWuXiaoLong = 0;

        //先发一张看看能不能是豹子或者豹七
        byte card1 = gameData.popCard();
        gameData.mPlayerCards[pl.position].cardsInHand.add(card1);
        if(!isBaoZi(gameData.mPlayerCards[pl.position].cardsInHand)
                && !isBaoQi(gameData.mPlayerCards[pl.position].cardsInHand)){
            byte card2 = gameData.popCard();
            byte card3 = gameData.popCard();
            gameData.mPlayerCards[pl.position].cardsInHand.add(card2);
            gameData.mPlayerCards[pl.position].cardsInHand.add(card3);
            gameData.mPlayerCards[pl.position].cardNum = getCardNum(gameData.mPlayerCards[pl.position].cardsInHand);
            gameData.mPlayerCards[pl.position].cardType = getCardType(gameData.mPlayerCards[pl.position].cardsInHand);
            pl.yanPaiResult = gameData.mPlayerCards[pl.position].cardType == PokerConstants.JACK_CARDTYPE_WU_XIAO_LONG ?
                    PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
        }else{
            gameData.mPlayerCards[pl.position].cardNum = getCardNum(gameData.mPlayerCards[pl.position].cardsInHand);
            gameData.mPlayerCards[pl.position].cardType = getCardType(gameData.mPlayerCards[pl.position].cardsInHand);
            pl.yanPaiResult = PokerConstants.GAME_RESULT_WIN;
        }
        
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(pl.position, 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);

        //取消超时
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        //发送消息
        msg.addAllCardsInHand(JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand));
        mergePlayerData(desk,msg,gameData);
        if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON){
            msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
            msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        }
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
        msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
        msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);

        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                -1,-1,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));

        //继续通知下家操作
        if(!checkGameIsOver(gameData,desk)) player_chu_notify(gameData,desk);
    }

    private void player_op_buYaWuXiaoLong(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (pl.position != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        //游戏数据
        pl.multiple = 2;
        pl.yaWuXiaoLong = 1;
        pl.yanPaiResult = PokerConstants.GAME_RESULT_LOSE;
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(pl.position, 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);

        //取消超时
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        //发送消息
        msg.addAllCardsInHand(JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand));
        mergePlayerData(desk,msg,gameData);
        if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON){
            msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
            msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        }
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
        msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
        msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);

        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                -1,-1,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));

        //继续通知下家操作
        if(!checkGameIsOver(gameData,desk)) player_chu_notify(gameData,desk);
    }

    private void player_op_yaoPai(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (pl.position != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        if (gameData.mPlayerCards[pl.position].cardsInHand.size() >= 5) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========玩家手牌已经达到5个========");
            return;
        }

        //游戏数据
        byte popCard = gameData.popCard();
//        popCard = 2;
        gameData.mPlayerCards[pl.position].cardsInHand.add(popCard);
        gameData.mPlayerCards[pl.position].cardNum = getCardNum(gameData.mPlayerCards[pl.position].cardsInHand);
        gameData.mPlayerCards[pl.position].cardType = getCardType(gameData.mPlayerCards[pl.position].cardsInHand);
        pl.multiple = getMulti(gameData.mPlayerCards[pl.position]);

        //取消超时
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        msg.addAllCardsInHand(JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand));
        //继续通知下家操作
        if(gameData.mPlayerCards[pl.position].cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI){//爆牌
            if(pl.position == gameData.robIndex){//庄家爆牌
                for(PlayerInfo p : desk.getPlayingPlayers()){
                    if(p == null || p.position == gameData.robIndex || p.yanPaiResult >=0) continue;
                    p.yanPaiResult = PokerConstants.GAME_RESULT_WIN;
                    p.multiple = pl.multiple > p.multiple ? pl.multiple : p.multiple;
                }
                //发送消息 
                mergePlayerData(desk,msg,gameData);
                if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON){
                    msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                    msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
                }
                JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
                msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
                JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);
                //回放
                gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                        -1,-1,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                        JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));


                gameOver(gameData, desk);
                return;
            }else{//闲家爆牌

//                List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(pl.position, 1, 0);
//                gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
                //发送消息

                mergePlayerData(desk,msg,gameData);
                if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON){
                    msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                    msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
                }
                JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
                msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
                JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);
                player_chu_notify(gameData, desk);
            }
        }else {
            //发送消息
            mergePlayerData(desk,msg,gameData);
            if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON
                    || pl.position == gameData.robIndex){
                msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
            }
            JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
            msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
            msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
            JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);
            player_chu_notify(gameData, desk);
        }

        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                -1,-1,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));

    }

    private void player_op_tingPai(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (pl.position != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        //游戏数据
        if(pl.position != gameData.robIndex) {
            List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(pl.position, 1, 0);
            gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
        }
        pl.isTingPai = true;

        //取消超时
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        //发送消息
        msg.addAllCardsInHand(JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand));
        mergePlayerData(desk,msg,gameData);
        if(gameData.mPlayerCards[pl.position].cardType> PokerConstants.JACK_CARDTYPE_COMMON
                || pl.position == gameData.robIndex){
            msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
            msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        }
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);
        msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
        msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);

        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                -1,-1,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));

        if (pl.position == gameData.robIndex) {
            gameData.onlyZhuangYanPai = true;
            List<PlayerInfo> list = desk.loopGetPlayerJACK(gameData.robIndex, desk.getPlayingPlayers().size()-1, 0);
            for (PlayerInfo p : list) {
                if (gameData.mPlayers[p.position].isBeiYanPai) continue;
                JACKGameOperPlayerActionSyn.Builder builder = JACKGameOperPlayerActionSyn.newBuilder();
                builder.setYanPaiPos(p.position);
                builder.setAction(PokerConstants.JACK_OPERTAION_YAN_PAI);
                player_op_yanPai(gameData, desk, builder, pl);
            }
            gameOver(gameData,desk);
            return;
        }

        //继续通知下家操作
        player_chu_notify(gameData,desk);

    }

    private void player_op_yanPai(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (pl.position != gameData.robIndex){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========该玩家不是庄家========");
            return;
        }
        if (pl.position != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        if(msg.getYanPaiPos() >=0 && gameData.mPlayers[msg.getYanPaiPos()].isBeiYanPai){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========该玩家已经被验牌========");
            return;
        }
        int winPos;
        List<Byte> cards = gameData.mPlayerCards[pl.position].cardsInHand;
        List<Byte> biPaiCards = gameData.mPlayerCards[msg.getYanPaiPos()].cardsInHand;
        int cardNum = gameData.mPlayerCards[pl.position].cardNum;
        int yanPaiCardNum = gameData.mPlayerCards[msg.getYanPaiPos()].cardNum;
        int cardType = gameData.mPlayerCards[pl.position].cardType;
        int yanPaiCardType = gameData.mPlayerCards[msg.getYanPaiPos()].cardType;
        
        if(cardType == yanPaiCardType){
            if(cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI){
                winPos = msg.getYanPaiPos();
            }else if (cardType != PokerConstants.JACK_CARDTYPE_COMMON && cardType != PokerConstants.JACK_CARDTYPE_BAO_ZI){
                winPos = -1;
            }else{
                if(cardNum == yanPaiCardNum){
                    winPos = cards.size() == biPaiCards.size() ? -1 : (cards.size() > biPaiCards.size() ? msg.getYanPaiPos(): pl.position);
                }else {
                    winPos = cardNum > yanPaiCardNum ? pl.position:msg.getYanPaiPos();
                }
            }
        } else {
            winPos = cardType>yanPaiCardType ? pl.position : msg.getYanPaiPos();
        }

        //记录日志
        log.info("桌子id--" + desk.getDeskID() + "--" + "庄家--"+gameData.mPlayers[pl.position].name+"--与闲家--"+gameData.mPlayers[msg.getYanPaiPos()].name+"比牌");
        if(winPos != -1) {
            PlayerInfo winPlayer = desk.getDeskPlayer(winPos);
            log.info("桌子id--" + desk.getDeskID() + "--" + "比牌结果--" + winPlayer.name + "--赢--座位号--" + winPlayer.position);
        }else{
            log.info("桌子id--" + desk.getDeskID() + "--" + "比牌结果--"+"平局");
        }
        //游戏数据
        gameData.mPlayers[msg.getYanPaiPos()].yanPaiResult = winPos == -1 ? PokerConstants.GAME_RESULT_EVEN :
                (msg.getYanPaiPos() == winPos ? PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE);
        gameData.mPlayers[msg.getYanPaiPos()].isBeiYanPai = true;
        if(gameData.mPlayers[msg.getYanPaiPos()].multiple < gameData.mPlayers[pl.position].multiple)
        gameData.mPlayers[msg.getYanPaiPos()].multiple = gameData.mPlayers[pl.position].multiple;
        //取消超时
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        //发送消息
        if (!gameData.onlyZhuangYanPai) {
            msg.addAllCardsInHand(JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand));
            mergePlayerData(desk, msg, gameData);
            msg.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
            msg.setCardType(gameData.mPlayerCards[pl.position].cardType);
            msg.setWinnerPos(winPos);
            JACKPokerPushHelper.pushActionSyn(desk, pl.position, msg, PokerConstants.SEND_TYPE_ALL);
        }
        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                msg.getYanPaiPos(),winPos,pl.chouMa, gameData.mPlayerCards[pl.position].cardNum,gameData.mPlayerCards[pl.position].cardType,
                JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand),mergeRecordPlayerHandCards(gameData,desk));

        //判断游戏是否结束,如果庄家停牌了,只能验牌(系统帮弄)
        if(!gameData.onlyZhuangYanPai && !checkGameIsOver(gameData,desk)) player_chu_notify(gameData,desk);
    }

    private void  mergePlayerData(MJDesk<byte[]> desk,JACKGameOperPlayerActionSyn.Builder msg,GameData gameData) {
        for(PlayerInfo pl  : desk.getPlayingPlayers()){
            if(pl == null) continue;
            JACK.JACKGameOperHandCardSyn.Builder builder = JACK.JACKGameOperHandCardSyn.newBuilder();
            builder.setPosition(pl.position);
            List<Integer> otherPlayerCards = new ArrayList<>();
            for(byte b : gameData.mPlayerCards[pl.position].cardsInHand){
                if(gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_JACK){//庄家是杰克,闲家压五小龙
                    if(pl.position != gameData.robIndex ){//闲家
                        if(gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON){//闲家不是普通牌型全部显示
                            otherPlayerCards.add((int)b);
                            builder.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                            builder.setCardType(gameData.mPlayerCards[pl.position].cardType);
                        }else {//闲家爆牌或者普通牌型,发牌的底牌不显示
                            if (otherPlayerCards.size() < 2) {
                                otherPlayerCards.add(-1);
                            } else {
                                otherPlayerCards.add((int)b);
                            }
                        }
                    }else{//庄家显示全部
                        otherPlayerCards.add((int)b);
                        builder.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                        builder.setCardType(gameData.mPlayerCards[pl.position].cardType);
                    }
                }else{//庄家不是杰克
                    if(gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON
                            ||gameData.mPlayers[pl.position].isBeiYanPai){//不是普通牌型或者被验牌了都显示
                        otherPlayerCards.add((int)b);
                        builder.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                        builder.setCardType(gameData.mPlayerCards[pl.position].cardType);
                    }else{
                        if(pl.position != gameData.robIndex ) {//闲家是普通牌型或者爆牌,发牌的底牌不显示
                            if (otherPlayerCards.size() < 2) {
                                otherPlayerCards.add(-1);
                            } else {
                                otherPlayerCards.add((int)b);
                            }
                        }else{//庄家是普通牌型或者爆牌,轮到他操作时显示所有牌否则不显示
                            if(gameData.getPokerOpPlayerIndex() == gameData.robIndex){
                                otherPlayerCards.add((int)b);
                                builder.setCardNum(gameData.mPlayerCards[pl.position].cardNum);
                                builder.setCardType(gameData.mPlayerCards[pl.position].cardType);
                            }else{
                                otherPlayerCards.add(-1);
                            }
                        }
                    }
                }
            }
            builder.addAllHandCards(otherPlayerCards);
            msg.addPlayerHandCards(builder.build());
        }
    }

    private void player_op_xiaZhu(GameData gameData, MJDesk<byte[]> desk, JACKGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是下注阶段========");
            return;
        }
        if(pl.isXiaZhu){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========已经下注========");
            return;
        }

        //日志
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--下注--");

        //游戏数据
        pl.chouMa = msg.getChouMa();
        pl.isXiaZhu = true;

        //取消超时判断
        gameData.mPlayerAction[pl.position].opStartTime = 0L;

        //发送消息
        JACKPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_ALL);

        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),pl.position, msg.getAction(),
                -1,-1,pl.chouMa, -1,-1,
                new ArrayList<>(),new ArrayList<>());
        
        //判断下注阶段是否结束
        if(checkXiaZhuOver(gameData,desk)) resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_SEND_CARD);

        //推送桌子消息
        for(PlayerInfo p : desk.getAllPlayers()) {
            desk.sendMsg2Player(p, JACKMsgBuilder.getPacketForPublicInfo(getDeskPublicInfoMsg(desk)).build().toByteArray());
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子消息"+JsonFormat.printToString(getDeskPublicInfoMsg(desk).build()));
    }

    private boolean checkXiaZhuOver(GameData gameData, MJDesk<byte[]> desk) {
        for(Object p : desk.getPlayingPlayers()){
            if(p == null) continue;
            PlayerInfo o = (PlayerInfo) p;
            if(o.position == gameData.robIndex) continue;
            if(!o.isXiaZhu) return false;
        }
        return true;
    }

    private void resetNextPlayerOperation(GameData gameData, MJDesk<byte[]> desk ,int Substate) {
        // 等待客户端播动画
        gameData.setWaitingStartTime(System.currentTimeMillis());
        gameData.setPlaySubstate(Substate);
        // 顺序，轮到下一个玩家行动
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(gameData.getPokerOpPlayerIndex(), 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
    }

    /**
     * 提醒下注
     */
    @Override
    public void gameStart(GameData data, MJDesk<byte[]> desk) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"desk.getPlayingPlayers()-----"+desk.getPlayingPlayers());
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null || pl.isZanLi || pl.isWait) continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--座位号--"+pl.position+"--id--"+pl.playerId);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--累计分数/金币数--"+gameData.jackResult.Result.get(pl.playerId).allSocre);
        }

        //推送桌子开始消息
        JACK.JACKGameOperStartSyn.Builder startMsg = JACK.JACKGameOperStartSyn.newBuilder();
        startMsg.setJuNum(gameData.handNum);// 当前局数
        startMsg.setSeq(gameData.gameSeq);
        startMsg.setBankerPos(gameData.robIndex);
        for (PlayerInfo pl : desk.getAllPlayers()) {
            startMsg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getAllPlayers()) {
                JACK.JACKGameOperHandCardSyn.Builder handCardBuilder = JACK.JACKGameOperHandCardSyn.newBuilder();
                handCardBuilder.setPosition(p.position);// 玩家的桌子位置
                handCardBuilder.setIsWait(p.isWait);
                handCardBuilder.setIsZanLi(p.isZanLi);
                startMsg.addPlayerHandCards(handCardBuilder);
            }

            MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
            gb.setOperType(MJBase.GameOperType.JACKGameOperStartSyn);
            gb.setContent(startMsg.build().toByteString());
            gb.setType(0);

            desk.sendMsg2Player(pl, gb.build().toByteArray());
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"游戏开始发送消息--" + JsonFormat.printToString(startMsg.build()));

        //回放
      gameData.recorder.recordBasicInfo(gameData);

        gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS;
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);

        for(PlayerInfo p : desk.getAllPlayers()){
            if(p == null) continue;

            if(p.position != gameData.robIndex && !p.isWait && !p.isZanLi && p.position >= 0) {
                log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + p.name + "下注");
                //设置超时
                gameData.mPlayerAction[p.position].opStartTime = System.currentTimeMillis();
                if (desk.canXiaManZhu()) {
                    playerAutoXiaZhu(data, desk, p.position);
                } else {
                    JACK.JACKGameOperPlayerActionNotify.Builder msg = JACK.JACKGameOperPlayerActionNotify.newBuilder();
                    msg.setPosition(p.position);
                    msg.setActions(PokerConstants.JACK_OPERTAION_XIA_ZHU);
                    if (desk.getYaZhu() == PokerConstants.JACK_WU_FEN_CAHNG) {
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
                    } else if (desk.getYaZhu() == PokerConstants.JACK_SHI_FEN_CAHNG) {
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(2, 3, 4, 5, 10)));
                    } else if (desk.getYaZhu() == PokerConstants.JACK_ER_SHI_FEN_CAHNG) {
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(3, 4, 5, 10, 20)));
                    }
                    //推送消息
                    JACKPokerPushHelper.pushActionNofity(gameData, desk, p.position, msg, PokerConstants.SEND_TYPE_SINGLE);
                    //广播当前正在操作的玩家
                    JACKPokerPushHelper.pushActorSyn(desk, p.position, p.position, 15, PokerConstants.SEND_TYPE_ALL);
                }
            }

            //推送桌子消息
            desk.sendMsg2Player(p, JACKMsgBuilder.getPacketForPublicInfo(getDeskPublicInfoMsg(desk)).build().toByteArray());

        }

    }

    /**
     * 发牌
     */
    public void sendCards(GameData gameData, MJDesk<byte[]> desk) {
        gameData.danZhu = desk.getYaZhu() < 0 ? 1:desk.getYaZhu();
        for (Object temp : desk.getPlayingPlayers()) {
            PlayerInfo pl = (PlayerInfo) temp;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            cl.clear();
            List<Byte> src = new ArrayList<Byte>();
            boolean isBanker = pl.playerId == gameData.mPublic.mBankerUserId;
            boolean faPai = false; //false 随机发牌  true 自定义发牌
            if(faPai){
                StringBuffer bu = new StringBuffer();
                if(isBanker){
                    bu.append("黑3 ");
                    bu.append("黑10");
                    src.addAll(chineseName2CardList(bu.toString()));

                }else{
                    if(pl.position == (gameData.robIndex+1)%2) {
                        bu.append("黑J ");
                        bu.append("小王");
                        src.addAll(chineseName2CardList(bu.toString()));

                    }
//                    else if(pl.position == (gameData.robIndex+2)%3){
//                        bu.append("方6 ");
//                        bu.append("黑6");
//                        src.addAll(chineseName2CardList(bu.toString()));
//                    }
                }
            }else{
                for (int j = src.size(); j < 2; j++) {
                    Byte b = gameData.popCard();
                    src.add(b);
                }
            }
            // 排个序
            cl.addAll(JACKProcessor.sortHandCards(src));
            gameData.mPlayerCards[pl.position].cardType = getCardType(cl);
            gameData.mPlayerCards[pl.position].cardNum = getCardNum(cl);
            pl.multiple = getMulti(gameData.mPlayerCards[pl.position]);
        }

        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null || pl.isZanLi || pl.isWait) continue;
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--手牌--"+ JACKHelper.getSingleCardListName(gameData.getCardsInHand(pl.position)));
        }

        // 把牌下发给客户端
        boolean allXianJiaCardTypeNotCommon = true;
        for (PlayerInfo p : desk.getAllPlayers()) {
            if(p.position >= 0) {
                int cardType = gameData.mPlayerCards[p.position].cardType;
                if (p.position != gameData.robIndex && cardType == PokerConstants.JACK_CARDTYPE_COMMON) {
                    allXianJiaCardTypeNotCommon = false;
                }
            }
        }

        //推送发牌消息
        for (PlayerInfo pl : desk.getAllPlayers()) {
            JACKGameOperPlayerActionSyn.Builder msg = JACKGameOperPlayerActionSyn.newBuilder();
            msg.setAction(PokerConstants.JACK_OPERTAION_SEND_CARD);
            msg.setPosition(pl.position);
            msg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getAllPlayers()) {
                boolean showHandCardVal = (p.position == pl.position && p.position >= 0) && !p.isWait && !p.isZanLi;
                JACK.JACKGameOperHandCardSyn.Builder handCardBuilder = JACK.JACKGameOperHandCardSyn.newBuilder();

                if(p.position >= 0) {
                    int cardType = gameData.mPlayerCards[p.position].cardType;
                    int cardNum = gameData.mPlayerCards[p.position].cardNum;

                    if(showHandCardVal || cardType > PokerConstants.JACK_CARDTYPE_COMMON) {
                        handCardBuilder.setCardNum(cardNum);
                        handCardBuilder.setCardType(cardType);
                    }
                    for (int card : gameData.getCardsInHand(p.position)) {
                        handCardBuilder.addHandCards(showHandCardVal
                                || cardType > PokerConstants.JACK_CARDTYPE_COMMON? card : -1);
                    }
                }
                if(allXianJiaCardTypeNotCommon && p.position == gameData.robIndex){
                    handCardBuilder.setCardType(gameData.mPlayerCards[p.position].cardType);
                    handCardBuilder.setCardNum(gameData.mPlayerCards[p.position].cardNum);
                    handCardBuilder.clearHandCards();
                    for (int card : gameData.getCardsInHand(p.position)) {
                        handCardBuilder.addHandCards(card);
                    }
                }
                handCardBuilder.setPosition(p.position);// 玩家的桌子位置
                handCardBuilder.setXiaZhu(p.chouMa);
                msg.addPlayerHandCards(handCardBuilder);
            }
            log.info("--发牌消息--"+pl.name+"--"+JsonFormat.printToString(msg.build()));
            MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
            tt.setContent(msg.build().toByteString());
            tt.setOperType( MJBase.GameOperType.JACKGameOperPlayerActionSyn);
            desk.sendMsg2Player(pl, tt.build().toByteArray());
        }

        //回放
        gameData.gameSeq = (int) (System.nanoTime() % 10000);
        for (Object temp : desk.getPlayingPlayers()) {
            PlayerInfo pl = (PlayerInfo) temp;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            gameData.recorder.recordJACKPlayerCard(cl , pl.position);
        }

        List<RecordData> recordData = new ArrayList<>();
        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            JACKRecordData jackRecordData = new JACKRecordData();
            jackRecordData.position = pl.position;
            jackRecordData.score = gameData.jackFinalResult.finalResults.get(pl.playerId).allScore;
            recordData.add(jackRecordData);
        }
        gameData.recorder.recordJACKGameStart(gameData.mPlayers ,recordData, gameData.mDeskCard.cards);
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),-1, PokerConstants.JACK_OPERTAION_SEND_CARD,
                -1,-1,0, -1,-1,
                new ArrayList<>(),mergeRecordPlayerHandCards(gameData,desk));
        
        //游戏数据
        gameData.showInitCardTime = System.currentTimeMillis();
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
        gameData.setWaitingStartTime(System.currentTimeMillis());
    }

    private List<JACKPLayerHandRecordData> mergeRecordPlayerHandCards(GameData gameData, MJDesk<byte[]> desk) {
        List<JACKPLayerHandRecordData> playerHandCards = new ArrayList<>();
        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            JACKPLayerHandRecordData playerCardData = new JACKPLayerHandRecordData();
            playerCardData.position = pl.position;
            playerCardData.cardsInHand = JACKProcessor.byte2IntList(gameData.mPlayerCards[pl.position].cardsInHand);
            playerCardData.xiaZhu = pl.chouMa;
            playerCardData.cardNum = gameData.mPlayerCards[pl.position].cardNum;
            playerCardData.cardType = gameData.mPlayerCards[pl.position].cardType;
            playerHandCards.add(playerCardData);
        }
        return playerHandCards;
    }

    private int getMulti(GamePacket.MyGame_Player_Cards cards) {
        int cardType = cards.cardType;
        int multi = 1;
        if(cardType == PokerConstants.JACK_CARDTYPE_JACK){
            multi = 2;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_WU_XIAO_LONG){
            multi = 3;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_BAO_ZI){
            multi = 5;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_SHUANG_LONG){
            multi = 5;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_SHUANG_WANG){
            multi = 8;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_BAO_QI){
            multi = 10;
        }else if(cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI){
            if(cards.cardsInHand.size() >= 5) multi = 3;
        }
        return multi;
    }

    private boolean isShuangWang(List<Byte> cards) {
        return cards != null && !cards.isEmpty() && cards.size() == 2
                && cards.get(0) == (cards.get(1) + 1) && cards.get(0) == PokerConstants.POKER_CODE_DA_WANG;
    }

    private boolean isWuXiaoLong(List<Byte> cards) {
        if(cards != null && !cards.isEmpty() && cards.size() == 5){
            List<Byte> temp = JACKRule.modular(cards);
            Set<Integer> result = new HashSet<>();
            List<List<Integer>> zuHe = new ArrayList<>();
            for(byte b :temp){
                zuHe.add(convert(b));
            }
            for(int a1 : zuHe.get(0)){
                for(int a2 : zuHe.get(1)){
                    for(int a3 : zuHe.get(2)){
                        for(int a4 : zuHe.get(3)){
                            for(int a5 : zuHe.get(4)){
                                result.add(a1+a2+a3+a4+a5);
                            }
                        }
                    }
                }
            }
            List<Integer> list = new ArrayList<>(result);
            Collections.sort(list);
            return list.get(0) <= 21;
        }
        return false;
    }

    private boolean isBaoQi(List<Byte> cards) {
        return cards != null && !cards.isEmpty() && cards.size() == 3
                && ((cards.get(0) & 0x0f) == 7)
                && ((cards.get(1) & 0x0f) == 7)
                && ((cards.get(2) & 0x0f) == 7);
    }

    private boolean isBaoZi(List<Byte> cards) {
        if(cards == null) return false;
        if(cards.contains(PokerConstants.POKER_CODE_DA_WANG )|| cards.contains(PokerConstants.POKER_CODE_XIAO_WANG)) return false;
        return !cards.isEmpty() && cards.size() == 3
                && (cards.get(0) & 0x0f) == ((cards.get(1) & 0x0f))
                && ((cards.get(1) & 0x0f) == ((cards.get(2) & 0x0f))
                && ((cards.get(1) & 0x0f) < 7));
    }

    private List<Integer> convert(int b) {
        List<Integer> list = new ArrayList<>();
        if(b < 11){
            list.add(b);
        }else if(b < 14){
            list.add(10);
        }else{
            list.add(1);
            list.add(11);
        }
        return list;
    }

    private boolean isShuangLong(List<Byte> cards) {
        return cards != null && !cards.isEmpty() && cards.size() == 2
                && (cards.get(0) & 0x0f)== (cards.get(1) & 0x0f) && (cards.get(0) & 0x0f) == 14;
    }

    private boolean isJack(List<Byte> cards) {
        if(cards != null && !cards.isEmpty() && cards.size() == 2){
            byte a1 = cards.get(0);
            byte a2 = cards.get(1);
            if(a1 == PokerConstants.POKER_CODE_DA_WANG || a1 == PokerConstants.POKER_CODE_XIAO_WANG || (a1 & 0x0f) == 14){
                return (a2 & 0x0f) >= 10;
            }
        }
        return false;
    }

    private boolean checkGameIsOver(GameData gameData, MJDesk<byte[]> desk) {
       
        int num = 0;
        for(PlayerInfo p : (List<PlayerInfo>)desk.getPlayingPlayers()){
            if(p == null || p.position == gameData.robIndex) continue;
            if(p.yanPaiResult> 0) num++;
        }
        if(gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_BAO_PAI
                || desk.getPlayingPlayers().size() == (num +1)){
            gameOver(gameData,desk);
            return true;
        }
        return false;
    }

    private void gameOver(GameData gameData, MJDesk<byte[]> desk) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"--打牌结束,庄家倍数"+gameData.mPlayers[gameData.robIndex].multiple);
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p == null || p.position == gameData.robIndex) continue;
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+p.name+"--"+(p.yanPaiResult ==1 ? "赢" : (p.yanPaiResult == 2 ? "输":"平局")));
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+p.name+"--倍数--"+p.multiple);
        }

        gameData.jackResult.endTime = System.currentTimeMillis();
        gameData.jackResult.juNum = gameData.handNum;

        log.error("----------------------------------------------");
        for(PokerJACKFinalResult f : gameData.jackFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerJACKResult f : gameData.jackResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allSocre);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : desk.getPlayingPlayers()){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        int score = 0;
        for(PlayerInfo p : desk.getPlayingPlayers()) {
            if (p == null || p.position == gameData.robIndex) continue;
            PokerJACKResult result = gameData.jackResult.Result.get(p.playerId);
            result.pos = p.position;
            result.playerId = p.playerId;
            result.playerName = p.name;
            result.lastScore = result.score;
            result.score = p.yanPaiResult == PokerConstants.GAME_RESULT_LOSE ?-p.multiple * p.chouMa :
                    (p.yanPaiResult == PokerConstants.GAME_RESULT_EVEN ?0 : p.multiple*p.chouMa);
            if((result.score + result.allSocre < 0) && desk.isClubJiFenDesk() && desk.getCanFufen() == 1){
                result.score = -result.allSocre;
            }
            result.allSocre += result.score;
            result.cardType = gameData.mPlayerCards[p.position].cardType;
            result.cardNum = gameData.mPlayerCards[p.position].cardNum;
            result.isBanker = false;
            result.result = p.yanPaiResult;
            result.maxCardType = result.maxCardType > result.cardType ? result.maxCardType : result.cardType;
            result.maxScore = result.maxScore > result.score ? result.maxScore : result.score;
            
            PokerJACKFinalResult finalResult = gameData.jackFinalResult.finalResults.get(p.playerId);
            finalResult.allScore = result.allSocre;
            finalResult.loseNum += (p.yanPaiResult == PokerConstants.GAME_RESULT_LOSE ? 1: 0);
            finalResult.winNum += (p.yanPaiResult == PokerConstants.GAME_RESULT_WIN ? 1: 0);
            finalResult.maxCardType = result.maxCardType;
            finalResult.maxScore = result.maxScore;
            finalResult.score += result.score;

            score -= result.score;
            p.curJuScore = result.score;

        }

        log.error("----------------------------------------------");
        for(PokerJACKFinalResult f : gameData.jackFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerJACKResult f : gameData.jackResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allSocre);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : desk.getPlayingPlayers()){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        PlayerInfo p = gameData.mPlayers[gameData.robIndex];
        PokerJACKResult result = gameData.jackResult.Result.get(p.playerId);
        PokerJACKFinalResult finalResult = gameData.jackFinalResult.finalResults.get(p.playerId);
        result.pos = gameData.robIndex;
        result.playerId = p.playerId;
        result.playerName = p.name;
        result.lastScore = result.score;
        result.score = score;
        p.curJuScore = result.score;
        if(desk.isClubJiFenDesk()) {
            if (result.score + result.allSocre < 0 && desk.getCanFufen() == 1) {
                //庄家本局最多输桌面的钱
                result.score = -result.allSocre;
                List<PlayerInfo> list = desk.loopGetPlayerJACK(gameData.robIndex, desk.getPlayingPlayers().size() - 1, 0);
                //庄家桌面的钱加上 桌子上输家的输的前作为分配的钱(分配给赢家)
                int allScore = result.allSocre;
                for (PlayerInfo pl : list) {
                    if (pl == null) continue;
                    if (pl.yanPaiResult == PokerConstants.GAME_RESULT_LOSE) {
                        allScore -= pl.curJuScore;
                    }
                }

                //赢家的钱重新分配
                for (PlayerInfo pl : list) {
                    if (pl == null || pl.yanPaiResult == PokerConstants.GAME_RESULT_LOSE) continue;
                    PokerJACKResult res = gameData.jackResult.Result.get(pl.playerId);
                    PokerJACKFinalResult finalRes = gameData.jackFinalResult.finalResults.get(pl.playerId);

                    //先还原成上一局的总分数
                    res.allSocre -= res.score;
                    finalRes.score -= res.score;
                    //按照规则分配
                    if (allScore < res.score) {
                        res.score = allScore < 0 ? 0 : allScore;
                    }
                    //分配完之后重新计算总分数
                    res.allSocre += res.score;
                    finalRes.allScore = res.allSocre;
                    finalRes.score += res.score;
                    allScore -= res.score;
                }
            }
        }

        result.allSocre += result.score;
        result.cardType = gameData.mPlayerCards[p.position].cardType;
        result.cardNum = gameData.mPlayerCards[p.position].cardNum;
        result.isBanker = true;
        result.result = p.yanPaiResult;
        result.maxCardType = result.maxCardType > result.cardType ? result.maxCardType : result.cardType;
        result.maxScore = result.maxScore > result.score ? result.maxScore : result.score;

        finalResult.allScore = result.allSocre;
        finalResult.loseNum += (score < 0 ? 1: 0);
        finalResult.winNum += (score > 0 ? 1: 0);
        finalResult.maxCardType = result.maxCardType;
        finalResult.maxScore = result.maxScore;
        finalResult.score += result.score;

        log.error("----------------------------------------------");
        for(PokerJACKFinalResult f : gameData.jackFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerJACKResult f : gameData.jackResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allSocre);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : desk.getPlayingPlayers()){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        JACK.JACKGameOperPlayerActionNotify.Builder notify = JACK.JACKGameOperPlayerActionNotify.newBuilder();
        notify.setPosition(-1);
        notify.setActions(PokerConstants.POKER_OPERTAION_GAME_OVER);
        //消息推送
        JACKPokerPushHelper.pushActionNofity(gameData,desk,0,notify, PokerConstants.SEND_TYPE_ALL);
        //回放
        gameData.recorder.recordJACKPlayerAction(gameData.genSeq(),-1,PokerConstants.POKER_OPERTAION_GAME_OVER,
                -1,-1,-1, -1,-1,
                new ArrayList<>(),mergeRecordPlayerHandCards(gameData,desk));


        //设置下一个庄家的下标
        if(desk.canLunLiuZhuang()){
            List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(gameData.robIndex, 1, 0);
            gameData.robIndex = nextPlayer.get(0).position;
        }
        
        //设置一局结束的状态,循环获取状态后结束这局游戏
        gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);
    }

    private int getCardType(List<Byte> cardsInHand) {
        if(isBaoQi(cardsInHand)) return PokerConstants.JACK_CARDTYPE_BAO_QI;
        if(isShuangWang(cardsInHand)) return PokerConstants.JACK_CARDTYPE_SHUANG_WANG;
        if(isShuangLong(cardsInHand)) return PokerConstants.JACK_CARDTYPE_SHUANG_LONG;
        if(isBaoZi(cardsInHand)) return PokerConstants.JACK_CARDTYPE_BAO_ZI;
        if(isWuXiaoLong(cardsInHand)) return PokerConstants.JACK_CARDTYPE_WU_XIAO_LONG;
        if(isJack(cardsInHand)) return PokerConstants.JACK_CARDTYPE_JACK;
        int num = getCardNum(cardsInHand);
        if(num > 0) return PokerConstants.JACK_CARDTYPE_COMMON;
        if(num < 0) return PokerConstants.JACK_CARDTYPE_BAO_PAI;
        return 0;
    }

    /**
     * -1:爆牌
     * @param cards
     * @return
     */
    private int getCardNum(List<Byte> cards) {
        if(cards != null && !cards.isEmpty() && cards.size() <= 5){
            List<Byte> temp = JACKRule.modular(cards);
            byte[] combo = new byte[5];
            Set<Integer> result = new HashSet<>();
            List<List<Integer>> zuHe = new ArrayList<>();
            for (int i = 0; i < combo.length; i++) {
                if(i < cards.size()) {
                    combo[i] = temp.get(i);
                }
                zuHe.add(convert(combo[i]));
            }
            for(int a1 : zuHe.get(0)){
                for(int a2 : zuHe.get(1)){
                    for(int a3 : zuHe.get(2)){
                        for(int a4 : zuHe.get(3)){
                            for(int a5 : zuHe.get(4)){
                                result.add(a1+a2+a3+a4+a5);
                            }
                        }
                    }
                }
            }
            List<Integer> list = new ArrayList<>(result);
            Collections.sort(list);
            List<Integer> list1 = new ArrayList<>();
            for(int i : list){
                if(i <= 21) list1.add(i);
            }
            if(list1.isEmpty()) return -1;
            Collections.sort(list1);
            return list1.get(list1.size()-1);
        }
        return 0;
    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info) {

    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"act=repushGameData; position={};deskId={};", position, desk.getDeskID());

        //把当前桌子的状况发给重连玩家
        JACK.JACKGameOperStartSyn.Builder jack = JACK.JACKGameOperStartSyn.newBuilder();
        jack.setJuNum(gameData.handNum);//桌子当前圈数
        jack.setBankerPos(gameData.robIndex);//当前地主的下标
        jack.setReconnect(true);//表示断线重连
        jack.setSeq(gameData.gameSeq);
        gameData.recorder.seq = jack.getSeq(); // 记录序列号

        PlayerInfo p = desk.getDeskPlayer(position);
        boolean allXianJiaCardTypeNotCommon = true;
        for (PlayerInfo pl : desk.getPlayers()) {
            if(!pl.isWait && !pl.isZanLi && pl.position != gameData.robIndex){//看下非庄家的人是不是都不是普通牌,如果是显示庄家牌
                if(gameData.mPlayerCards[pl.position].cardType == PokerConstants.JACK_CARDTYPE_COMMON){
                    allXianJiaCardTypeNotCommon = false;
                }
            }
        }

        for (PlayerInfo pl : desk.getPlayers()) {
            JACK.JACKGameOperHandCardSyn.Builder builder = JACK.JACKGameOperHandCardSyn.newBuilder();
            if(!pl.isWait && !pl.isZanLi) {
                int cardType = gameData.mPlayerCards[pl.position].cardType;
                int cardNum = gameData.mPlayerCards[pl.position].cardNum;
                if(p.position == pl.position){//自己肯定能看到自己的全部手牌,点数和类型
                    for (Byte card : gameData.mPlayerCards[pl.position].cardsInHand) {
                        builder.addHandCards(card);
                        builder.setCardNum(cardNum);
                        builder.setCardType(cardType);
                    }
                }else {//自己看别人的牌,点数和类型的情况
                    for (Byte b : gameData.mPlayerCards[pl.position].cardsInHand) {
                        if (gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_JACK) {//庄家是杰克,闲家压五小龙
                            if (pl.position != gameData.robIndex) {//闲家
                                if (gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON) {//闲家不是普通牌型全部显示
                                    builder.addHandCards(b);
                                    builder.setCardNum(cardNum);
                                    builder.setCardType(cardType);
                                } else {//闲家爆牌或者普通牌型,发牌的底牌不显示
                                    if (builder.getHandCardsCount() < 2) {
                                        builder.addHandCards(-1);
                                    } else {
                                        builder.addHandCards(b);
                                    }
                                }
                            } else {//庄家显示全部
                                builder.addHandCards(b);
                                builder.setCardNum(cardNum);
                                builder.setCardType(cardType);
                            }
                        } else {//庄家不是杰克
                            if (gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON
                                   ||gameData.mPlayers[pl.position].isBeiYanPai) {//不是普通牌型或者被验牌了都显示
                                builder.addHandCards(b);
                                builder.setCardNum(cardNum);
                                builder.setCardType(cardType);
                            } else {
                                if (pl.position != gameData.robIndex) {//闲家是普通牌型或者爆牌,发牌的底牌不显示
                                    if (builder.getHandCardsCount() < 2) {
                                        builder.addHandCards(-1);
                                    } else {
                                        builder.addHandCards(b);
                                    }
                                } else {//庄家是普通牌型或者爆牌,轮到他操作时显示所有牌否则不显示
                                    if (gameData.getPokerOpPlayerIndex() == gameData.robIndex) {
                                        builder.addHandCards(b);
                                        builder.setCardNum(cardNum);
                                        builder.setCardType(cardType);
                                    } else {
                                        builder.addHandCards(-1);
                                    }
                                }
                            }
                        }
                    }
                }

                if(allXianJiaCardTypeNotCommon && pl.position == gameData.robIndex){
                    builder.clearHandCards();
                    for (Byte b : gameData.mPlayerCards[pl.position].cardsInHand) {
                        builder.addHandCards(b);
                    }
                    builder.setCardNum(cardNum);
                    builder.setCardType(cardType);
                }
            }
            if(gameData.jackFinalResult.finalResults.get(pl.playerId) != null) {
                builder.setSocre(gameData.jackFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            builder.setPosition(pl.position);
            builder.setXiaZhu(pl.chouMa);
            builder.setIsWait(pl.isWait);
            builder.setIsZanLi(pl.isZanLi);
            jack.addPlayerHandCards(builder);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.JACKGameOperStartSyn);
        gb.setContent(jack.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(jack.build()));

        desk.sendMsg2Player(p.position, gb.build().toByteArray());

        // 发送桌子公告信息
        JACKPokerPushHelper.pushPublicInfoMsg2Single(desk, position, gameData,getDeskPublicInfoMsg(desk));

        // 发送当前操作人
        JACKPokerPushHelper.pushActorSyn(desk, position, gameData.getPokerOpPlayerIndex(), 9, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, position);
    }

    private JACK.JACKGameOperPublicInfoSyn.Builder getDeskPublicInfoMsg(MJDesk<byte[]> desk) {
        JACK.JACKGameOperPublicInfoSyn.Builder JACK = packet.jack.JACK.JACKGameOperPublicInfoSyn.newBuilder();
        JACK.setDeskState(gameData.currentDeskState);
        for(PlayerInfo pl : desk.getPlayingPlayers()){
            if(pl.position >= 0 && pl.position != gameData.robIndex){
                packet.jack.JACK.JACKGameDeskData.Builder builder = packet.jack.JACK.JACKGameDeskData.newBuilder();
                builder.setIsXiaZhu(pl.isXiaZhu);
                builder.setPosition(pl.position);
                JACK.addData(builder.build());
            }
        }
        return JACK;
    }


    @Override
    public void pushDeskInfo(GameData mGameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子信息给新来玩家");

        //把当前桌子的状况发给重连玩家
        JACK.JACKGameOperStartSyn.Builder jack = JACK.JACKGameOperStartSyn.newBuilder();
        jack.setJuNum(gameData.handNum);//桌子当前圈数
        jack.setBankerPos(gameData.robIndex);//当前地主的下标
        jack.setReconnect(true);//表示断线重连
        jack.setSeq(gameData.gameSeq);
        gameData.recorder.seq = jack.getSeq(); // 记录序列号

        boolean allXianJiaCardTypeNotCommon = true;
        for (PlayerInfo pl : desk.getPlayers()) {
            if(!pl.isWait && !pl.isZanLi && pl.position != gameData.robIndex){//看下非庄家的人是不是都不是普通牌,如果是显示庄家牌
                if(gameData.mPlayerCards[pl.position].cardType == PokerConstants.JACK_CARDTYPE_COMMON){
                    allXianJiaCardTypeNotCommon = false;
                }
            }
        }

        for (PlayerInfo pl : desk.getPlayers()) {
            JACK.JACKGameOperHandCardSyn.Builder builder = JACK.JACKGameOperHandCardSyn.newBuilder();
            builder.setSocre(gameData.jackFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            builder.setPosition(pl.position);
            builder.setXiaZhu(pl.chouMa);
            builder.setIsWait(pl.isWait);
            builder.setIsZanLi(pl.isZanLi);

            if(!pl.isWait && !pl.isZanLi) {
                int cardType = gameData.mPlayerCards[pl.position].cardType;
                int cardNum = gameData.mPlayerCards[pl.position].cardNum;
                if(p.position == pl.position){//自己肯定能看到自己的全部手牌,点数和类型
                    for (Byte card : gameData.mPlayerCards[pl.position].cardsInHand) {
                        builder.addHandCards(card);
                        builder.setCardNum(cardNum);
                        builder.setCardType(cardType);
                    }
                }else {//自己看别人的牌,点数和类型的情况
                    for (Byte b : gameData.mPlayerCards[pl.position].cardsInHand) {
                        if (gameData.mPlayerCards[gameData.robIndex].cardType == PokerConstants.JACK_CARDTYPE_JACK) {//庄家是杰克,闲家压五小龙
                            if (pl.position != gameData.robIndex) {//闲家
                                if (gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON) {//闲家不是普通牌型全部显示
                                    builder.addHandCards(b);
                                    builder.setCardNum(cardNum);
                                    builder.setCardType(cardType);
                                } else {//闲家爆牌或者普通牌型,发牌的底牌不显示
                                    if (builder.getHandCardsCount() < 2) {
                                        builder.addHandCards(-1);
                                    } else {
                                        builder.addHandCards(b);
                                    }
                                }
                            } else {//庄家显示全部
                                builder.addHandCards(b);
                                builder.setCardNum(cardNum);
                                builder.setCardType(cardType);
                            }
                        } else {//庄家不是杰克
                            if (gameData.mPlayerCards[pl.position].cardType > PokerConstants.JACK_CARDTYPE_COMMON
                                    ||gameData.mPlayers[pl.position].isBeiYanPai) {//不是普通牌型或者被验牌了都显示
                                builder.addHandCards(b);
                                builder.setCardNum(cardNum);
                                builder.setCardType(cardType);
                            } else {
                                if (pl.position != gameData.robIndex) {//闲家是普通牌型或者爆牌,发牌的底牌不显示
                                    if (builder.getHandCardsCount() < 2) {
                                        builder.addHandCards(-1);
                                    } else {
                                        builder.addHandCards(b);
                                    }
                                } else {//庄家是普通牌型或者爆牌,轮到他操作时显示所有牌否则不显示
                                    if (gameData.getPokerOpPlayerIndex() == gameData.robIndex) {
                                        builder.addHandCards(b);
                                        builder.setCardNum(cardNum);
                                        builder.setCardType(cardType);
                                    } else {
                                        builder.addHandCards(-1);
                                    }
                                }
                            }
                        }
                    }
                }

                if(allXianJiaCardTypeNotCommon && pl.position == gameData.robIndex){
                    builder.clearHandCards();
                    for (Byte b : gameData.mPlayerCards[pl.position].cardsInHand) {
                        builder.addHandCards(b);
                    }
                    builder.setCardNum(cardNum);
                    builder.setCardType(cardType);
                }
            }
            jack.addPlayerHandCards(builder);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.JACKGameOperStartSyn);
        gb.setContent(jack.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(jack.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());

        // 发送桌子公告信息
        JACK.JACKGameOperPublicInfoSyn.Builder JACK = getDeskPublicInfoMsg(desk);
        desk.sendMsg2Player(p, JACKMsgBuilder.getPacketForPublicInfo(JACK).build().toByteArray());
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送桌子消息为---"+ JsonFormat.printToString(JACK.build()));
    }

    /**
     * 重新通知玩家操作
     *
     */
    @Override
    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, int position) {
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS){
            PlayerInfo p = desk.getDeskPlayer(position);
            if(!p.isXiaZhu && !p.isWait && !p.isZanLi && p.position != gameData.robIndex) {
                log.info("桌子id--"+desk.getDeskID()+"--"+"重新提示玩家--" + p.name + "下注");
                JACK.JACKGameOperPlayerActionNotify.Builder msg = JACK.JACKGameOperPlayerActionNotify.newBuilder();
                msg.setPosition(p.position);
                msg.setActions(PokerConstants.JACK_OPERTAION_XIA_ZHU);
                if(desk.getYaZhu() == PokerConstants.JACK_WU_FEN_CAHNG){
                    if(desk.canXiaManZhu()){
                        msg.addXiaZhu(desk.getYaZhu());
                    }else{
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(1,2,3,4,5)));
                    }
                }else if (desk.getYaZhu() == PokerConstants.JACK_SHI_FEN_CAHNG){
                    if(desk.canXiaManZhu()){
                        msg.addXiaZhu(desk.getYaZhu());
                    }else {
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(2, 3, 4, 5, 10)));
                    }
                }else if(desk.getYaZhu() == PokerConstants.JACK_ER_SHI_FEN_CAHNG){
                    if(desk.canXiaManZhu()){
                        msg.addXiaZhu(desk.getYaZhu());
                    }else {
                        msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(3, 4, 5, 10, 20)));
                    }
                }
                //设置超时
                gameData.mPlayerAction[p.position].opStartTime = System.currentTimeMillis();
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS;
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //推送消息
                JACKPokerPushHelper.pushActionNofity(gameData,desk,p.position,msg, PokerConstants.SEND_TYPE_SINGLE);
                //广播当前正在操作的玩家
                JACKPokerPushHelper.pushActorSyn(desk,p.position,p.position,15, PokerConstants.SEND_TYPE_ALL);
                return;
            }
        }

        if (gameData.getPokerOpPlayerIndex() == position) {
            //获取玩家的子原因状态
            int substate = gameData.currentDeskState;
            switch (substate) {
                case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD:
                    //提醒玩家重新出
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新操作");
                    player_chu_notify(gameData, desk);
                    break;
                case PokerConstants.POKER_TABLE_SUB_STATE_SEND_CARD:
                    //提醒玩家重新出
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新发牌");
                    sendCards(gameData, desk);
                    break;
            }
        }
    }

    /**
     * 设置下一把庄家
     * 默认是开房间的人,开房间的人的座位号为0
     */
    @Override
    public void selectBanker(GameData data, MJDesk<byte[]> desk) {
        if(data.handNum == 0) gameData.robIndex = desk.getPlayers().get(0).position;
        PlayerInfo playerInfo = desk.getDeskPlayer(gameData.robIndex);
        data.mPublic.mbankerPos = gameData.robIndex;
        data.mPublic.mBankerUserId = playerInfo.playerId;
//        List<PlayerInfo> nextPlayer = desk.loopGetPlayerJACK(gameData.robIndex, 1, 0);
        gameData.setPokerOpPlayerIndex(gameData.robIndex);
    }

    private List<Byte> chineseName2CardList(String name){
        String[] cardListChinese = name.split(" ");
        List<Byte> cardList = new ArrayList<>();
        for(String card : cardListChinese){
            cardList.add(JACKHelper.singleCardMapChinese.get(card));
        }
        return cardList;
    }

    @Override
    public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk) {

    }

    @Override
    public void notifyDouble(GameData gameData, MJDesk<byte[]> desk) {

    }

    @Override
    public void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {

    }
}
