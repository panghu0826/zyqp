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
import packet.jack.JACK;

import java.util.List;

/**
 * @author chen 2017 - 12 - 20
 */
public class JACKPokerPushHelper {
    private static Logger logger = LogManager.getLogger("DESKLOG");
    private static boolean debugPacket = true;

    public static void pushActionNofity(GameData gameData, MJDesk desk, int receiver, JACK.JACKGameOperPlayerActionNotify.Builder db, int sendType) {
        db.setSeq(gameData.genSeq());
        logger.info("act=pokerMsg;deskId={};type=ActionNotify[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForActionNofity(db), sendType);
    }


    public static void pushHandCardSyn(MJDesk desk, int receiver, JACK.JACKGameOperHandCardSyn.Builder db, int sendType) {
        if(debugPacket) {
//            logger.info("act=pokerMsg;deskId={};receiver={};type=handcardSyn[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForHandcardSyn(db), sendType);
    }

    public static void pushActionSyn(MJDesk desk, int receiver, JACK.JACKGameOperPlayerActionSyn.Builder db, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};type=actionSyn[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForActionSyn(db), sendType);
    }

    public static void pushActorSyn(MJDesk desk, int receiver, int position, int timeLeft, int sendType) {
        if(debugPacket){
//            logger.info("act=pushActorSyn;deskId={};receiver={};position={};timeLeft={}}", desk.getDeskID(), receiver, position, timeLeft);
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForActorSyn(position, timeLeft), sendType);
    }


    public static void pushPlayerHuMsg(MJDesk desk, int receiver, JACK.JACKGameOperPlayerHuSyn.Builder bd, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};receiver={};type=pushPlayerHu[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForPlayerWin(bd), sendType);
    }

    public static void pushFinalSettleInfo(MJDesk desk, int receiver, JACK.JACKGameOperFinalSettleSyn.Builder bd, int sendType) {
        if(debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=finalSettle[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForFinalSettle(bd), sendType);
    }


    /**
     * 发给一个人呢
     *
     */
    public static void pushPublicInfoMsg2Single(MJDesk desk, int position, GameData data,JACK.JACKGameOperPublicInfoSyn.Builder JACK) {
        PlayerInfo p = data.mPlayers[position];
        int receiver = p.position;
        if(debugPacket) {
            logger.info("act=pushPublicInfo;deskId={};receiver={};PublicInfo={};bankerPos={}", desk.getDeskID(), receiver, JsonFormat.printToString(JACK.build()), data.mPublic.mbankerPos);
        }
        pushMsg(desk, receiver, JACKMsgBuilder.getPacketForPublicInfo(JACK), PokerConstants.SEND_TYPE_SINGLE);
    }
    
    /**
     * 同步玩家的手牌 只对自己
     */
    public static void pushHandCardSyn(GameData gameData, MJDesk desk, PlayerInfo pl) {
        JACK.JACKGameOperHandCardSyn.Builder handCardBuilder = JACK.JACKGameOperHandCardSyn.newBuilder();
        // 发给玩家的牌
        List<Byte> cardsInHand = gameData.getCardsInHand(pl.position);
        for (int card : cardsInHand) {
            handCardBuilder.addHandCards(card);
        }

        handCardBuilder.setPosition(pl.position);// 玩家的桌子位置
        pushHandCardSyn(desk, pl.position, handCardBuilder, PokerConstants.SEND_TYPE_SINGLE);
    }

    public static void pushWaitNextMatchStart(MJDesk desk, MsgGame.WaitNextMatchStart.Builder gb) {
        if(debugPacket) {
            logger.info("act=WaitNextMatchStart;deskId={};receiver={};type=WaitNextMatchStart[{}]", desk.getDeskID(), JsonFormat.printToString(gb.build()));
        }
        desk.pushWaitNextMatchStart(desk,gb);
    }


    public static void pushMsg(MJDesk desk, int receiver, MessageLite.Builder b, int sendType) {
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
