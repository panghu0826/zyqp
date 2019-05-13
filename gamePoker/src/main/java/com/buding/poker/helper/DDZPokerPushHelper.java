package com.buding.poker.helper;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.poker.constants.PokerConstants;
import com.google.protobuf.MessageLite;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.ddz.DDZ;
import packet.game.MsgGame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chen 2017 - 12 - 20
 */
public class DDZPokerPushHelper {
    private static Logger logger = LogManager.getLogger("DESKLOG");
    private static boolean debugPacket = true;

    public static void pushActionNofity(GameData gameData, MJDesk desk, int receiver, DDZ.DDZGameOperPlayerActionNotify.Builder db, int sendType) {
        db.setSeq(gameData.genSeq());

        //获得可以出的牌 名子
        String name = "";
        List<DDZ.DDZGameOperPrompt> list = db.getPromptCardsList();
        for (DDZ.DDZGameOperPrompt p : list) {
            List<Integer> l = p.getCardsList();
            List<Byte> ls = new ArrayList<>();
            for (int i : l) {
                ls.add((byte)i);
            }
            String n = DDZHelper.getSingleCardListName(ls);
            name = name + "  " + n;
        }

        String desc = "服务器推送:玩家手牌(" + DDZHelper.getSingleCardListName(gameData.mPlayerCards[receiver].cardsInHand) + "),玩家操作:(" + db.getActions() + "),玩家可出的牌:(" + name + ")";
        logger.info("act=pokerMsg;deskId={};type=ActionNotify[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));

        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForActionNofity(db), sendType);
    }


    public static void pushHandCardSyn(MJDesk desk, int receiver, DDZ.DDZGameOperHandCardSyn.Builder db, int sendType) {
        if(debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=handcardSyn[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForHandcardSyn(db), sendType);
    }

    public static void pushActionSyn(MJDesk desk, int receiver, DDZ.DDZGameOperPlayerActionSyn.Builder db, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};type=actionSyn[{}]", desk.getDeskID(), JsonFormat.printToString(db.build()));
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForActionSyn(db), sendType);
    }

    public static void pushActorSyn(MJDesk desk, int receiver, int position, int timeLeft, int sendType) {
        if(debugPacket){
            logger.info("act=pushActorSyn;deskId={};receiver={};position={};timeLeft={}}", desk.getDeskID(), receiver, position, timeLeft);
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForActorSyn(position, timeLeft), sendType);
    }


    public static void pushPlayerHuMsg(MJDesk desk, int receiver, DDZ.DDZGameOperPlayerHuSyn.Builder bd, int sendType) {
        if(debugPacket){
            logger.info("act=pokerMsg;deskId={};receiver={};type=pushPlayerHu[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForPlayerWin(bd), sendType);
    }

    public static void pushFinalSettleInfo(MJDesk desk, int receiver, DDZ.DDZGameOperFinalSettleSyn.Builder bd, int sendType) {
        if(debugPacket) {
            logger.info("act=pokerMsg;deskId={};receiver={};type=finalSettle[{}]", desk.getDeskID(), receiver, JsonFormat.printToString(bd.build()));
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForFinalSettle(bd), sendType);
    }

    /**
     * 发给所有人
     *
     * @param desk
     * @param data
     */
    public static void pushPublicInfoMsg2All(MJDesk desk, GameData data) {
        for(PlayerInfo p : data.mPlayers) {
            if(p == null) {
                continue;
            }
            int receiver = p.position;
            if(debugPacket) {
                logger.info("act=pushPublicInfo;deskId={};receiver={};cardLeft={};bankerPos={}", desk.getDeskID(), receiver, data.getCardLeftNum(), data.mPublic.mbankerPos);
            }
            pushMsg(desk, receiver, DDZMsgBuilder.getPacketForPublicInfo(data.mDeskCard.cards), PokerConstants.SEND_TYPE_ALL);
        }
    }

    /**
     * 发给一个人呢
     *
     * @param desk
     * @param position
     * @param data
     */
    public static void pushPublicInfoMsg2Single(MJDesk desk, int position, GameData data) {
        PlayerInfo p = data.mPlayers[position];
        int receiver = p.position;
        if(debugPacket) {
            logger.info("act=pushPublicInfo;deskId={};receiver={};cardLeft={};bankerPos={}", desk.getDeskID(), receiver, data.getCardLeftNum(), data.mPublic.mbankerPos);
        }
        pushMsg(desk, receiver, DDZMsgBuilder.getPacketForPublicInfo(data.mDeskCard.cards), PokerConstants.SEND_TYPE_SINGLE);
    }

    /**
     * 同步玩家的手牌 只对自己
     *
     * @param gameData
     * @param desk
     * @param pl
     */
    public static void pushHandCardSyn(GameData gameData, MJDesk desk, PlayerInfo pl) {
        DDZ.DDZGameOperHandCardSyn.Builder handCardBuilder = DDZ.DDZGameOperHandCardSyn.newBuilder();
        // 发给玩家的牌
        List<Byte> cardsInHand = gameData.getCardsInHand(pl.position);
        List<Byte> cardsBefore = gameData.getCardsBefore(pl.position);

        for (int card : cardsInHand) {
            handCardBuilder.addHandCards(card);
        }

        handCardBuilder.setPosition(pl.position);// 玩家的桌子位置
        for (byte b : cardsBefore) {
            handCardBuilder.addCardsBefore(b);
        }
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
