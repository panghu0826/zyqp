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
import packet.nn.NN;

import java.util.List;

/**
 * @author chen 2017 - 12 - 20
 */
public class NNPokerPushHelper {
    private static Logger logger = LogManager.getLogger("DESKLOG");
    private static boolean debugPacket = true;

    public static void pushActionNofity(GameData gameData, MJDesk<byte[]> desk, int receiver, NN.NNGameOperPlayerActionNotify.Builder db, int sendType) {
        db.setSeq(gameData.genSeq());
        logger.info("act=pokerMsg;deskId={};type=ActionNotify[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForActionNofity(db), sendType);
    }


    public static void pushHandCardSyn(MJDesk<byte[]> desk, int receiver, NN.NNGameOperHandCardSyn.Builder db, int sendType) {
        if(debugPacket) {
//            logger.info("act=pokerMsg;deskId={};receiver={};type=handcardSyn[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForHandcardSyn(db), sendType);
    }

    public static void pushActionSyn(MJDesk<byte[]> desk, int receiver, NN.NNGameOperPlayerActionSyn.Builder db, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};type=actionSyn[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForActionSyn(db), sendType);
    }

    public static void pushActorSyn(MJDesk<byte[]> desk, int receiver, int position, int timeLeft, int sendType) {
        if(debugPacket){
//            logger.info("act=pushActorSyn;deskId={};receiver={};position={};timeLeft={}}", desk.getDeskID(), receiver, position, timeLeft);
        }
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForActorSyn(position, timeLeft), sendType);
    }


    public static void pushPlayerHuMsg(MJDesk<byte[]> desk, int receiver, NN.NNGameOperPlayerHuSyn.Builder bd, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};receiver={};type=pushPlayerHu[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForPlayerWin(bd), sendType);
    }

    public static void pushFinalSettleInfo(MJDesk<byte[]> desk, int receiver, NN.NNGameOperFinalSettleSyn.Builder bd, int sendType) {
        if(debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=finalSettle[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, NNMsgBuilder.getPacketForFinalSettle(bd), sendType);
    }

    /**
     * 同步玩家的手牌 只对自己
     *
     * @param gameData
     * @param desk
     * @param pl
     */
    public static void pushHandCardSyn(GameData gameData, MJDesk<byte[]> desk, PlayerInfo pl) {
        NN.NNGameOperHandCardSyn.Builder handCardBuilder = NN.NNGameOperHandCardSyn.newBuilder();
        // 发给玩家的牌
        List<Byte> cardsInHand = gameData.getCardsInHand(pl.position);
        for (int card : cardsInHand) {
            handCardBuilder.addHandCards(card);
        }

        handCardBuilder.setPosition(pl.position);// 玩家的桌子位置
        pushHandCardSyn(desk, pl.position, handCardBuilder, PokerConstants.SEND_TYPE_SINGLE);
    }

    public static void pushWaitNextMatchStart(MJDesk<byte[]> desk, MsgGame.WaitNextMatchStart.Builder gb) {
        if(debugPacket) {
            logger.info("act=WaitNextMatchStart;deskId={};receiver={};type=WaitNextMatchStart[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        desk.pushWaitNextMatchStart(desk,gb);
    }


    public static void pushMsg(MJDesk<byte[]> desk, int receiver, MessageLite.Builder b, int sendType) {
        if (sendType == PokerConstants.SEND_TYPE_ALL) {
            desk.sendMsg2Desk(b.build().toByteArray());
            return;
        }
        if (sendType == PokerConstants.SEND_TYPE_SINGLE) {
            desk.sendMsg2Player(receiver, b.build().toByteArray());
            return;
        }
        desk.sendMsg2DeskExceptPosition(b.build().toByteArray(), Math.abs(receiver));
    }
}
