package com.buding.poker.helper;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.poker.constants.PokerConstants;
import com.google.protobuf.MessageLite;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.game.MsgGame;
import packet.erba.ErBa;

import java.util.List;

/**
 * @author chen 2017 - 12 - 20
 */
public class ErBaPokerPushHelper {
    private static Logger logger = LogManager.getLogger("DESKLOG");
    private static boolean debugPacket = true;

    public static void pushActionNofity(GameData gameData, MJDesk desk, PlayerInfo receiver, ErBa.ErBaGameOperPlayerActionNotify.Builder db, int sendType) {
        db.setSeq(gameData.genSeq());
        logger.info("act=pokerMsg;deskId={};type=ActionNotify[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForActionNofity(db), sendType);
    }


    public static void pushHandCardSyn(MJDesk desk, PlayerInfo receiver, ErBa.ErBaGameOperHandCardSyn.Builder db, int sendType) {
        if(debugPacket) {
//            logger.info("act=pokerMsg;deskId={};receiver={};type=handcardSyn[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForHandcardSyn(db), sendType);
    }

    public static void pushActionSyn(MJDesk desk, PlayerInfo receiver, ErBa.ErBaGameOperPlayerActionSyn.Builder db, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};type=actionSyn[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForActionSyn(db), sendType);
    }

    public static void pushActorSyn(MJDesk desk, PlayerInfo receiver, int playerId, int timeLeft, int sendType) {
        if(debugPacket){
//            logger.info("act=pushActorSyn;deskId={};receiver={};position={};timeLeft={}}", desk.getDeskID(), receiver, position, timeLeft);
        }
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForActorSyn(playerId, timeLeft), sendType);
    }


    public static void pushPlayerHuMsg(MJDesk desk, PlayerInfo receiver, ErBa.ErBaGameOperPlayerHuSyn.Builder bd, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};receiver={};type=pushPlayerHu[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForPlayerWin(bd), sendType);
    }

    public static void pushFinalSettleInfo(MJDesk desk, PlayerInfo receiver, ErBa.ErBaGameOperFinalSettleSyn.Builder bd, int sendType) {
        if(debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=finalSettle[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, ErBaMsgBuilder.getPacketForFinalSettle(bd), sendType);
    }


    /**
     * 发给一个人呢
     *
     */
    public static void pushPublicInfoMsg2Single(MJDesk desk, PlayerInfo p, GameData data,ErBa.ErBaGameOperPublicInfoSyn.Builder ErBa) {
        if(debugPacket) {
            logger.info("act=pushPublicInfo;deskId={};receiver={};PublicInfo={};bankerPos={}", desk.getDeskID(), p.name, JsonFormat.printToString(ErBa.build()), data.mPublic.mbankerPos);
        }
        pushMsg(desk, p, ErBaMsgBuilder.getPacketForPublicInfo(ErBa), PokerConstants.SEND_TYPE_SINGLE);
    }
    
    /**
     * 同步玩家的手牌 只对自己
     */
    public static void pushHandCardSyn(GameData gameData, MJDesk desk, PlayerInfo pl) {
        ErBa.ErBaGameOperHandCardSyn.Builder handCardBuilder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
        // 发给玩家的牌
        List<Byte> cardsInHand = gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand;
        for (int card : cardsInHand) {
            handCardBuilder.addHandCards(card);
        }

        handCardBuilder.setPlayerId(pl.playerId);// 玩家的桌子位置
        pushHandCardSyn(desk, pl, handCardBuilder, PokerConstants.SEND_TYPE_SINGLE);
    }

    public static void pushWaitNextMatchStart(MJDesk desk, MsgGame.WaitNextMatchStart.Builder gb) {
        if(debugPacket) {
            logger.info("act=WaitNextMatchStart;deskId={};receiver={};type=WaitNextMatchStart[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        desk.pushWaitNextMatchStart(desk,gb);
    }


    public static void pushMsg(MJDesk desk, PlayerInfo receiver, MessageLite.Builder b, int sendType) {
        if (sendType == PokerConstants.SEND_TYPE_ALL) {
            desk.sendMsg2Desk(b.build().toByteArray());
            return;
        }
        if (sendType == PokerConstants.SEND_TYPE_SINGLE) {
            desk.sendMsg2Player(receiver, b.build().toByteArray());
            return;
        }
        desk.sendMsg2DeskExceptPosition(b.build().toByteArray(), receiver);
    }
}
