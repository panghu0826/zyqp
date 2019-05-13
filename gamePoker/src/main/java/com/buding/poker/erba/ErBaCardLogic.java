package com.buding.poker.erba;

import com.buding.api.context.PokerDDZResult;
import com.buding.api.context.PokerErBaFinalResult;
import com.buding.api.context.PokerErBaResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.*;
import com.buding.card.ICardLogic;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.game.GamePacket;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.ErBaHelper;
import com.buding.poker.helper.ErBaPokerPushHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.erba.ErBa;
import packet.erba.ErBa.ErBaGameOperPlayerActionSyn;
import packet.mj.MJBase;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ErBaCardLogic implements ICardLogic<MJDesk<byte[]>> {

    private Logger log = LogManager.getLogger("DESKLOG");
    private GameData gameData;
    private MJDesk<byte[]> desk;
    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<Integer,Integer>>(){}.getType();

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
        //获取玩家的子原因状态
        int substate = gameData.getPlaySubstate();

        checkQiangZhuangOver(null, false);

        checkXiaZhuOver();
        switch (substate) {
            case PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS;
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER: {
                //记录超时
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
                notifyRobotBanker(gameData, desk);
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU: {
                notifyXiaZhu();
                //记录超时
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU;
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_SEND_CARD: {
                //记录超时
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //重连配置
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD;
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
                if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
                    if (desk.getErBaGameType() != PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                        for(PlayerInfo p : gameData.erBaCurrentGamingPlayers){
                            long time = gameData.mPlayerActionMap.get(p.playerId).opStartTime;
                            boolean isTimeout = (ctt - time) > (10 * 1000);
                            if (isTimeout && time != 0) playerAutoNotRobot(data, desk, p);
                        }
                    }
                }else if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU){
                    // 自动下注0
                    for(PlayerInfo p : gameData.erBaCurrentGamingPlayers){
                        if (p.playerId == gameData.robIndex) continue;
                        long time = gameData.mPlayerActionMap.get(p.playerId).opStartTime;
                        boolean isTimeout = (ctt - time) > (10 * 1000);
                        if (isTimeout && time != 0) playerAutoXiaZhu(data, desk, p);
                    }
                } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD) {
                    // 自动开牌
                    if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                        PlayerInfo p = gameData.mPlayersMap.get(gameData.robIndex);
                        long time = gameData.mPlayerActionMap.get(p.playerId).opStartTime;
                        boolean isTimeout = (ctt - time) > (10 * 1000);
                        if (isTimeout && time != 0) playerAutoKaiPai(data, desk, p);
                    } else {
                        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
//                            if (p.playerId == gameData.robIndex) continue;
                            long time = gameData.mPlayerActionMap.get(p.playerId).opStartTime;
                            boolean isTimeout = (ctt - time) > (10 * 1000);
                            if (isTimeout && time != 0) playerAutoKaiPai(data, desk, p);
                        }
                    }
                }
            }
            break;
        }
    }

    private void playerAutoKaiPai(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家自动开牌---");
        ErBaGameOperPlayerActionSyn.Builder msg = ErBaGameOperPlayerActionSyn.newBuilder();
        msg.setPlayerId(p.playerId);
        msg.setAction(PokerConstants.ErBa_OPERTAION_KAI_CARD);
        this.playerOperation(gameData,desk,msg,p);
    }

    private void playerAutoXiaZhu(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家自动下注---");
        ErBaGameOperPlayerActionSyn.Builder msg = ErBaGameOperPlayerActionSyn.newBuilder();
        msg.setPlayerId(p.playerId);
        msg.setAction(PokerConstants.ErBa_OPERTAION_XIA_ZHU);
        msg.setChouMa(0);
        ErBa.ErBaGameOperHandCardSyn.Builder cardSyn = ErBa.ErBaGameOperHandCardSyn.newBuilder();
        cardSyn.setPlayerId(p.playerId);
        for (int i = 0; i < 4; i++) {
            ErBa.TraditionalErBaUserYaZhu.Builder xiaZhu = ErBa.TraditionalErBaUserYaZhu.newBuilder();
            xiaZhu.setPlayerId(p.playerId);
            xiaZhu.setSiMenType(i);
            xiaZhu.setChouMa(0);
            cardSyn.addTraditionXiaZhu(xiaZhu);
        }
        msg.addPlayerHandCards(cardSyn);
        this.playerOperation(gameData,desk,msg,p);
        p.isXiaZhu = true;
        gameData.mPlayerActionMap.get(p.playerId).opStartTime = 0L;
        //判断下注阶段是否结束
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) checkXiaZhuOver();
    }

    private void playerAutoNotRobot(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家自动不抢庄---");
        ErBaGameOperPlayerActionSyn.Builder msg = ErBaGameOperPlayerActionSyn.newBuilder();
        msg.setPlayerId(p.playerId);
        msg.setAction(PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG);
        this.playerOperation(gameData,desk,msg,p);
    }

    @Override
    public void playerAutoOper(GameData gameData, MJDesk<byte[]> gt, int position) {

    }

    @Override
    public void player_chu_notify(GameData gameData, MJDesk<byte[]> desk) {
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            // 传统28提示庄家操作
            PlayerInfo plx = gameData.mPlayersMap.get(gameData.robIndex);
            log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + plx.name + "操作");

            // 设置操作时间
            gameData.mPlayerActionMap.get(plx.playerId).opStartTime = System.currentTimeMillis();

            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(plx.playerId);
            msg.setActions(PokerConstants.ErBa_OPERTAION_KAI_CARD | PokerConstants.ErBa_OPERTAION_SEE_CARD);

            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, plx, msg, PokerConstants.SEND_TYPE_SINGLE);

            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, plx, plx.playerId, 10, PokerConstants.SEND_TYPE_ALL);
        } else {
            // 经典和疯狂28提示所有人操作
            for (PlayerInfo plx : gameData.erBaCurrentGamingPlayers) {
                log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + plx.name + "操作");

                // 设置操作时间
                gameData.mPlayerActionMap.get(plx.playerId).opStartTime = System.currentTimeMillis();

                ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
                msg.setPlayerId(plx.playerId);
                msg.setActions(PokerConstants.ErBa_OPERTAION_KAI_CARD | PokerConstants.ErBa_OPERTAION_SEE_CARD);

                //推送消息
                ErBaPokerPushHelper.pushActionNofity(gameData, desk, plx, msg, PokerConstants.SEND_TYPE_SINGLE);

                //广播当前正在操作的玩家
                ErBaPokerPushHelper.pushActorSyn(desk, plx, plx.playerId, 10, PokerConstants.SEND_TYPE_ALL);
            }
        }

        //推送桌子消息
        for (PlayerInfo p : desk.getAllPlayers()) {
            ErBa.ErBaGameOperPublicInfoSyn.Builder erba = ErBa.ErBaGameOperPublicInfoSyn.newBuilder();
            erba.setDeskState(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
            ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData, erba);
        }
    }

    @Override
    public void playerOperation(GameData gameData, MJDesk<byte[]> desk, GeneratedMessage.Builder m, PlayerInfo pl) {
        ErBaGameOperPlayerActionSyn.Builder msg = (ErBaGameOperPlayerActionSyn.Builder) m;

        if (msg == null || pl == null || msg.getAction() == 0) return;

        desk.setPauseTime(System.currentTimeMillis());
        //抢庄
        if ((msg.getAction() & PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG) == PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG) {
            player_op_qiangzhuang(gameData, desk, msg, pl);
        }
        //不抢庄
        else if ((msg.getAction() & PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG) == PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG) {
            player_op_bu_qiangzhuang(gameData, desk, msg, pl);
        }
        //下注
        else if ((msg.getAction() & PokerConstants.ErBa_OPERTAION_XIA_ZHU) == PokerConstants.ErBa_OPERTAION_XIA_ZHU) {
            player_op_xiaZhu(gameData, desk, msg, pl);
        }
        //看牌
        else if ((msg.getAction() & PokerConstants.ErBa_OPERTAION_SEE_CARD) == PokerConstants.ErBa_OPERTAION_SEE_CARD) {
            player_op_see(gameData, desk, msg, pl);
        }
        //开牌
        else if ((msg.getAction() & PokerConstants.ErBa_OPERTAION_KAI_CARD) == PokerConstants.ErBa_OPERTAION_KAI_CARD) {
            player_op_kaiPai(gameData, desk, msg, pl);
        }
        else {
            throw new RuntimeException("UnKnowOperation;");
        }

    }

    private void player_op_kaiPai(GameData gameData, MJDesk<byte[]> desk, ErBaGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"开牌,但是不能开牌,因为不是出牌状态");
            return;
        }
        if (pl.isKaiPai) {
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"开牌,但是不能开牌,因为已经开牌");
            return;
        }

        //游戏数据
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            if (pl.playerId != gameData.robIndex) {
                log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"开牌,但是不能开牌,因为不是庄家");
                return;
            }

            //日志
            log.info("桌子id--"+desk.getDeskID()+"--"+"庄家--"+pl.name+"--开牌--");

            pl.isKaiPai = true;

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 2; j++) {
                    gameData.erBaLiangPaiMap.get(i).put(j, true);
                }
            }

            for (int i = 0; i < 4; i++) {
                ErBa.TraditionalErBaSiMenInfo.Builder siMenInfo = ErBa.TraditionalErBaSiMenInfo.newBuilder();
                siMenInfo.setSiMenType(i);
                siMenInfo.setChouMa(gameData.siMenChouMaMap.getOrDefault(i, 0));
                siMenInfo.addAllCardsInHand(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(i).cardsInHand));
                msg.addSiMenInfo(siMenInfo);
            }

            //取消超时判断
            gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;

            //发送消息
            ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

            //判断下注阶段是否结束
            gameOver(gameData, desk);

        } else {
            //日志
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--开牌--");

            pl.isKaiPai = true;

            for (int j = 0; j < 2; j++) {
                gameData.erBaLiangPaiMap.get(pl.playerId).put(j, true);
            }

            msg.addAllCardsInHand(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand));
        }

        //取消超时判断
        gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;

        //发送消息
        ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

        //回放
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),pl.playerId, msg.getAction(), -1,new ArrayList<>(), new ArrayList<>(),-1, -1);

        //判断开牌阶段是否结束
        if (allPlayerKaiPai()) gameOver(gameData,desk);

    }

    private boolean allPlayerKaiPai() {
        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            if (!p.isKaiPai) return false;
        }
        return true;
    }

    private void player_op_see(GameData gameData, MJDesk<byte[]> desk, ErBaGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"看牌,但是不能看牌,因为不是出牌状态");
            return;
        }

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            if (pl.playerId != gameData.robIndex) {
                log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"看牌,但是不能看牌,因为不是庄家");
                return;
            }

            //日志
            log.info("桌子id--"+desk.getDeskID()+"--"+"庄家--"+pl.name+"--看:"+ErBaHelper.siMenMap.get(msg.getSiMen())+":的第"+(msg.getCardValuePos()+1)+"张牌--");

            if (gameData.erBaLiangPaiMap.get(msg.getSiMen()).get(msg.getCardValuePos())) {
                log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"看牌,但是不能看牌,因为该门该牌位置已经亮牌");
                return;
            }

            gameData.erBaLiangPaiMap.get(msg.getSiMen()).put(msg.getCardValuePos(), true);

            // 看哪门牌 所有人就都能看到 发送全体消息
            msg.setCardValue(gameData.mPlayerCardsMap.get(msg.getSiMen()).cardsInHand.get(msg.getCardValuePos()));
            msg.addAllCardsInHand(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(msg.getSiMen()).cardsInHand));
            ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

            // 校验是否所有人都看了,都看了就结束
            boolean allLiangPai = true;
            a:for (Map<Integer, Boolean> map : gameData.erBaLiangPaiMap.values()) {
                for (Boolean b : map.values()) {
                    if (!b) {
                        allLiangPai = false;
                        break a;
                    }
                }
            }
            if (allLiangPai) {
                // 设置已经开牌,防止再次点击开牌
                pl.isKaiPai = true;

                //取消超时判断
                gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;

                gameOver(gameData, desk);
            }

        } else {
            //日志
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--看第"+(msg.getCardValuePos()+1)+"张牌--");
            if (gameData.erBaLiangPaiMap.get(pl.playerId).get(msg.getCardValuePos())) {
                log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"看牌,但是不能看牌,因为该牌位置已经亮牌");
                return;
            }

            gameData.erBaLiangPaiMap.get(pl.playerId).put(msg.getCardValuePos(), true);

            // 看哪门牌 仅仅自己能看到 发送单体消息
            msg.setCardValue(gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand.get(msg.getCardValuePos()));
            boolean allPaiLiangPai = true;
            for (boolean b : gameData.erBaLiangPaiMap.get(pl.playerId).values()) {
                if (!b) {
                    allPaiLiangPai = false;
                    break;
                }
            }
            if (allPaiLiangPai) msg.addAllCardsInHand(ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand));
            ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_SINGLE);
        }

        //回放
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),pl.playerId, msg.getAction(), -1,new ArrayList<>(),new ArrayList<>(),msg.getSiMen(), msg.getCardValuePos());

    }

    private void player_op_xiaZhu(GameData gameData, MJDesk<byte[]> desk, ErBaGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU){
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"下注,但是不能下注,因为不是下注状态");
            return;
        }

        List<PlayerSiMenChouMa> playerSiMenChouMas = new ArrayList<>();
        List<SiMenChouMa> siMenChouMas = new ArrayList<>();

        //游戏数据
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            if (pl.isXiaZhu) {
                log.info("桌子id--"+desk.getDeskID()+"--"+"========已经下注========");
                return;
            }

            //日志
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--下注--" +  msg.getPlayerHandCardsList().get(0).getTraditionXiaZhuList());
            Map<Integer,Integer> xiaZhuMap = gameData.trandition28UserChouMaMap.get(pl.playerId);
            if (xiaZhuMap == null) {
                gameData.trandition28UserChouMaMap.put(pl.playerId, new HashMap<>());
                xiaZhuMap = gameData.trandition28UserChouMaMap.get(pl.playerId);
            }
            int maxYaZhu = desk.getYaZhu() == PokerConstants.ERBA_ZI_YOU_CAHNG ? 100 : desk.getYaZhu();
            for (ErBa.TraditionalErBaUserYaZhu xiaZhu : msg.getPlayerHandCardsList().get(0).getTraditionXiaZhuList()) {
                int playerChouMa = xiaZhu.getChouMa();
                playerChouMa += gameData.trandition28UserChouMaMap.get(pl.playerId).getOrDefault(xiaZhu.getSiMenType(),0);
                if (playerChouMa > maxYaZhu) {
                    log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"下"+ErBaHelper.siMenMap.get(xiaZhu.getSiMenType())+"注,超限");
                    return;
                }
            }
            for (ErBa.TraditionalErBaUserYaZhu xiaZhu : msg.getPlayerHandCardsList().get(0).getTraditionXiaZhuList()) {
                int playerChouMa = xiaZhu.getChouMa();
                playerChouMa += gameData.trandition28UserChouMaMap.get(pl.playerId).getOrDefault(xiaZhu.getSiMenType(),0);
                xiaZhuMap.put(xiaZhu.getSiMenType(), playerChouMa);

                PlayerSiMenChouMa playerSiMenChouMa = new PlayerSiMenChouMa();
                playerSiMenChouMa.playerId = pl.playerId;
                playerSiMenChouMa.xiaZhuChouMa = xiaZhu.getChouMa();
                playerSiMenChouMa.totalChouMa = playerChouMa;
                playerSiMenChouMa.siMenType = xiaZhu.getSiMenType();
                playerSiMenChouMas.add(playerSiMenChouMa);

                int chouMa = gameData.siMenChouMaMap.getOrDefault(xiaZhu.getSiMenType(), 0);
                chouMa += xiaZhu.getChouMa();
                gameData.siMenChouMaMap.put(xiaZhu.getSiMenType(), chouMa);

                SiMenChouMa siMenChouMa = new SiMenChouMa();
                siMenChouMa.siMenType = xiaZhu.getSiMenType();
                siMenChouMa.totalChouMa = chouMa;
                siMenChouMas.add(siMenChouMa);
            }

            for (int i = 0; i < 4; i++) {
                ErBa.TraditionalErBaSiMenInfo.Builder siMeninfo = ErBa.TraditionalErBaSiMenInfo.newBuilder();
                siMeninfo.setSiMenType(i);
                siMeninfo.setChouMa(gameData.siMenChouMaMap.getOrDefault(i, 0));
                msg.addSiMenInfo(siMeninfo);
            }
            msg.clearPlayerHandCards();
            ErBa.ErBaGameOperHandCardSyn.Builder handcardsyn = ErBa.ErBaGameOperHandCardSyn.newBuilder();
            handcardsyn.setPlayerId(pl.playerId);
            for (int i = 0; i < 4; i++) {
                ErBa.TraditionalErBaUserYaZhu.Builder traditionXiaZhu = ErBa.TraditionalErBaUserYaZhu.newBuilder();
                traditionXiaZhu.setPlayerId(pl.playerId);
                traditionXiaZhu.setSiMenType(i);
                traditionXiaZhu.setChouMa(gameData.trandition28UserChouMaMap.get(pl.playerId).getOrDefault(i,0 ));
                handcardsyn.addTraditionXiaZhu(traditionXiaZhu);
            }
            msg.addPlayerHandCards(handcardsyn);

        } else {
            if (gameData.chouMaMap.containsKey(pl.playerId)) {
                log.info("桌子id--"+desk.getDeskID()+"--"+"========已经下注========");
                return;
            }

            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--下注--" +  msg.getChouMa());

            gameData.chouMaMap.put(pl.playerId, msg.getChouMa());

            //取消超时判断
            gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;
        }

        if (!gameData.erBaDiamondMap.getOrDefault(pl.playerId, false)) {
            desk.subServiceFee(pl);
            gameData.erBaDiamondMap.put(pl.playerId, true);
        }

        desk.erBaXiaZhuOrConfirmBanker(pl);

        //发送消息
        ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

        //回放
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),pl.playerId, msg.getAction(), msg.getChouMa(), playerSiMenChouMas,siMenChouMas,-1, -1);

        //判断下注阶段是否结束
        checkXiaZhuOver();
    }

    private boolean checkXiaZhuOver() {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU){
            return false;
        }
        boolean xiaZhuOver = false;
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            int num = 0;
            for (PlayerInfo pl : gameData.erBaCurrentGamingPlayers) {
                if (pl.playerId != gameData.robIndex && pl.isXiaZhu) num++;
            }
            if (num + 1 >= gameData.erBaCurrentGamingPlayers.size()) {
                xiaZhuOver = true;
            }
        } else {
            if (gameData.chouMaMap.size() + 1 >= gameData.erBaCurrentGamingPlayers.size()) {
                xiaZhuOver = true;
            }
        }
        if (xiaZhuOver) {
            gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_SEND_CARD);
        }
        return xiaZhuOver;
    }


    private synchronized void player_op_qiangzhuang(GameData gameData, MJDesk<byte[]> desk, ErBaGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER) {
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"抢庄,但是不能抢,因为不是抢庄状态");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"抢庄");

        if (gameData.robIndex > 0) return;


        //游戏数据
        gameData.qiangZhuangMap.put(pl.playerId, PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG);

        //取消超时
        gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;

        //回放
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),pl.playerId, msg.getAction(),-1,new ArrayList<>(),new ArrayList<>(),-1, -1);

        //发送消息
        ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

        // 校验抢庄结束
        checkQiangZhuangOver(pl, true);

    }

    private void player_op_bu_qiangzhuang(GameData gameData, MJDesk<byte[]> desk, ErBaGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER) {
            log.error("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"不抢庄,但是不能抢,因为不是抢庄状态");
            return;
        }
        if (gameData.robIndex > 0) return;
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+ pl.name +"不抢庄");

        //游戏数据
        gameData.qiangZhuangMap.put(pl.playerId, PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG);

        //取消超时
        gameData.mPlayerActionMap.get(pl.playerId).opStartTime = 0L;

        //回放
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),pl.playerId, msg.getAction(),-1,new ArrayList<>(),new ArrayList<>(),-1, -1);

        ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

        // 校验抢庄结束
        checkQiangZhuangOver(pl, false);

    }

    private synchronized void checkQiangZhuangOver(PlayerInfo pl, boolean qiangZhuang) {
        if (gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER) {
            return;
        }
        if (qiangZhuang) {
            gameData.robIndex = pl.playerId;
            gameData.mPublic.mBankerUserId = gameData.robIndex;

            gameData.recorder.bankerUserId = gameData.robIndex;

            desk.erBaXiaZhuOrConfirmBanker(pl);

            ErBaGameOperPlayerActionSyn.Builder msg = ErBaGameOperPlayerActionSyn.newBuilder();
            msg.setAction(PokerConstants.ErBa_OPERTAION_CONFIRM_BANKER);
            msg.setPlayerId(gameData.robIndex);
            //发送消息
            ErBaPokerPushHelper.pushActionSyn(desk,pl,msg,PokerConstants.SEND_TYPE_ALL);

            gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU);
        } else {
            // 经典28 和 疯狂28 操作人数+1 >= 当前游戏人数.结束
            if (gameData.qiangZhuangMap.size() >= gameData.erBaCurrentGamingPlayers.size()) {
                List<Integer> agreeList = new ArrayList<>();
                for (Map.Entry<Integer, Integer> op : gameData.qiangZhuangMap.entrySet()) {
                    if (op.getValue() == PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG) agreeList.add(op.getKey());
                }
                if (agreeList.isEmpty()) {
                    gameData.erBaSettleType = false;
                    gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);
                }
            }
        }

    }

    /**
     * 提醒下注
     */
    @Override
    public void gameStart(GameData data, MJDesk<byte[]> desk) {
        // 记录本局参与游戏玩家
        gameData.erBaCurrentGamingPlayers.clear();
        List<PlayerInfo> playerInfos = desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG ? desk.getAllPlayers() : desk.getPlayingPlayers();
        gameData.erBaCurrentGamingPlayers.addAll(playerInfos);

        // 记录参与过的玩家
        for (PlayerInfo playerInfo : playerInfos) {
            if (!gameData.erBaAllGamingPlayers.contains(playerInfo.playerId)) gameData.erBaAllGamingPlayers.add(playerInfo.playerId);
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"desk.getPlayingPlayers()-----"+gameData.erBaCurrentGamingPlayers);
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.erBaCurrentGamingPlayers) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--座位号--"+pl.position+"--id--"+pl.playerId);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--累计分数/金币数--"+gameData.erBaResult.Result.get(pl.playerId).allScore);
        }

        //推送桌子开始消息
        ErBa.ErBaGameOperStartSyn.Builder startMsg = ErBa.ErBaGameOperStartSyn.newBuilder();
        startMsg.setJuNum(gameData.handNum);// 当前局数
        startMsg.setSeq(gameData.gameSeq);
        startMsg.setBankerId(gameData.mPublic.mBankerUserId);
        for (PlayerInfo pl : desk.getAllPlayers()) {
            startMsg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getAllPlayers()) {
                ErBa.ErBaGameOperHandCardSyn.Builder handCardBuilder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                handCardBuilder.setPlayerId(p.playerId);// 玩家的桌子位置
//                handCardBuilder.setIsWait(p.isWait);
//                handCardBuilder.setIsZanLi(p.isZanLi);
                startMsg.addPlayerHandCards(handCardBuilder);
            }

            MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
            gb.setOperType(MJBase.GameOperType.ErBaGameOperStartSyn);
            gb.setContent(startMsg.build().toByteString());
            gb.setType(0);

            desk.sendMsg2Player(pl, gb.build().toByteArray());
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"游戏开始发送消息--" + JsonFormat.printToString(startMsg.build()));

        //回放
        gameData.recorder.recordBasicInfo(gameData);

        gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS;
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS);

    }

    @Override
    public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk) {
        if (gameData.robIndex > 0) {
            gameData.recorder.bankerUserId = gameData.robIndex;
            gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU);
            return;
        }
        long time = System.currentTimeMillis();
        for(PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + p.name + "抢庄");
            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(p.playerId);

            if (desk.getErBaGameType() ==PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG);

            } else{
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG | PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG);
            }
            //设置超时
            gameData.mPlayerActionMap.get(p.playerId).opStartTime = time;
            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);

        }

        for (PlayerInfo p : desk.getAllPlayers()) {
            //推送桌子消息
            ErBa.ErBaGameOperPublicInfoSyn.Builder erba = ErBa.ErBaGameOperPublicInfoSyn.newBuilder();
            erba.setDeskState(PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
            ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData, erba);
        }

    }

    private void notifyXiaZhu() {
        for(PlayerInfo p : gameData.erBaCurrentGamingPlayers){
            if(p == null) continue;

            if(p.playerId != gameData.robIndex) {
                log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + p.name + "下注");
                //设置超时
                gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();
                {
                    ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
                    msg.setPlayerId(p.playerId);
                    msg.setActions(PokerConstants.ErBa_OPERTAION_XIA_ZHU);
                    if (desk.getYaZhu() == PokerConstants.ErBa_WU_FEN_CAHNG) {
                        if (desk.canXiaManZhu())
                            msg.addXiaZhu(5);
                        else
                            msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
                    } else if (desk.getYaZhu() == PokerConstants.ErBa_YI_BAI_LIU_SHI_FEN_CAHNG) {
                        if (desk.canXiaManZhu())
                            msg.addXiaZhu(160);
                         else
                            msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(20, 40, 80, 160)));
                    } else if (desk.getYaZhu() == PokerConstants.ErBa_ER_SHI_FEN_CAHNG) {
                        if (desk.canXiaManZhu())
                            msg.addXiaZhu(20);
                         else
                            msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(5, 10, 15, 20)));
                    } else if (desk.getYaZhu() == PokerConstants.ERBA_ZI_YOU_CAHNG) {
                        if (desk.canXiaManZhu())
                            msg.addXiaZhu(100);
                        else
                            msg.addXiaZhu(-1);
                    }
                    //推送消息
                    ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
                    //广播当前正在操作的玩家
                    ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);
                }
            }
        }

        for (PlayerInfo p : desk.getAllPlayers()) {
            //推送桌子消息
            ErBa.ErBaGameOperPublicInfoSyn.Builder erba = ErBa.ErBaGameOperPublicInfoSyn.newBuilder();
            erba.setDeskState(PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU);
            ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData, erba);
        }
    }

    /**
     * 发牌
     */
    public void sendCards(GameData gameData, MJDesk<byte[]> desk) {

        boolean faPai = false; //false 随机发牌  true 自定义发牌

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            gameData.mPlayerCardsMap.clear();
            for (int i = 0; i < 4; i++) {
                gameData.mPlayerCardsMap.put(i, new GamePacket.MyGame_Player_Cards());
                List<Byte> src = new ArrayList<>();
                if (faPai) {
                    if (i == 0) {
                        src.add((byte)0x29);
                        src.add((byte)0x28);
                    } else  if (i == 1) {
                        src.add((byte)0x29);
                        src.add((byte)0x28);
                    } else  if (i == 2) {
                        src.add((byte)0x29);
                        src.add((byte)0x28);
                    } else  if (i == 3) {
                        src.add((byte)0x29);
                        src.add((byte)0x28);
                    }
                } else {
                    for (int j = 0; j < 2; j++) {
                        Byte b = gameData.popCard();
                        src.add(b);
                    }
                }

                gameData.mPlayerCardsMap.get(i).cardsInHand.addAll(src);
                gameData.mPlayerCardsMap.get(i).cardType = getCardType(src);
                gameData.mPlayerCardsMap.get(i).cardNum = getCardNum(src);

                log.info("桌子id--" + desk.getDeskID() + "--" + ErBaHelper.siMenMap.get(i) + "--手牌--" + ErBaHelper.getSingleCardListName(src));
            }
        } else {
            for (PlayerInfo pl : gameData.erBaCurrentGamingPlayers) {
                List<Byte> cl = gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand;
                cl.clear();
                List<Byte> src = new ArrayList<Byte>();
                boolean isBanker = pl.playerId == gameData.mPublic.mBankerUserId;
                if(faPai){
                    if(isBanker){
                       src.add((byte)0x22);
                       src.add((byte)0x28);
                    }else{
                        src.add((byte)0x22);
                        src.add((byte)0x41);
                    }
                }else{
                    for (int j = src.size(); j < 2; j++) {
                        Byte b = gameData.popCard();
                        src.add(b);
                    }
                }
                // 排个序
                cl.addAll(src);
                gameData.mPlayerCardsMap.get(pl.playerId).cardType = getCardType(src);
                gameData.mPlayerCardsMap.get(pl.playerId).cardNum = getCardNum(src);
                pl.multiple = getMulti(gameData.mPlayerCardsMap.get(pl.playerId));

                log.info("桌子id--" + desk.getDeskID() + "--" + "--玩家--" + pl.name + "--手牌--" + ErBaHelper.getSingleCardListName(cl));
            }
        }

        // 初始化亮牌情况
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_FENG_KUANG) {
            // 疯狂28: 全不亮, 按玩家位置初始亮牌
            for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                Map<Integer,Boolean> m = new HashMap<>();
                m.put(0, false);
                m.put(1, false);
                gameData.erBaLiangPaiMap.put(p.playerId, m);
            }
        } else if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            // 传统28: 亮第一个, 按四门位置初始亮牌
            for (int i = 0; i < 4; i++) {
                Map<Integer,Boolean> m = new ConcurrentHashMap<>();
                m.put(0, true);
                m.put(1, false);
                gameData.erBaLiangPaiMap.put(i, m);
            }
        } else {
            // 经典29: 亮第一个, 按玩家位置初始亮牌
            for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                Map<Integer,Boolean> m = new ConcurrentHashMap<>();
                m.put(0, true);
                m.put(1, false);
                gameData.erBaLiangPaiMap.put(p.playerId, m);
            }
        }
        //推送发牌消息
        for (PlayerInfo pl : desk.getAllPlayers()) {
            ErBaGameOperPlayerActionSyn.Builder msg = ErBaGameOperPlayerActionSyn.newBuilder();
            msg.setAction(PokerConstants.ErBa_OPERTAION_SEND_CARD);
            msg.setPlayerId(pl.playerId);
            msg.clearPlayerHandCards();
            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                for (int i = 0; i < 4; i++) {
                    ErBa.TraditionalErBaSiMenInfo.Builder siMenInfo = ErBa.TraditionalErBaSiMenInfo.newBuilder();
                    siMenInfo.setSiMenType(i);
                    siMenInfo.setChouMa(gameData.siMenChouMaMap.getOrDefault(i, 0));
                    for (int j = 0; j < 2; j++) {
                        siMenInfo.addCardsInHand(gameData.erBaLiangPaiMap.get(i).get(j) ? gameData.mPlayerCardsMap.get(i).cardsInHand.get(j) : -1);
                    }
                    msg.addSiMenInfo(siMenInfo);
                }
            } else {
                for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                    ErBa.ErBaGameOperHandCardSyn.Builder handCardBuilder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                    for (int i = 0; i < 2; i++) {
                        handCardBuilder.addHandCards(gameData.erBaLiangPaiMap.get(p.playerId).get(i) ? gameData.mPlayerCardsMap.get(p.playerId).cardsInHand.get(i) : -1);
                    }
                    handCardBuilder.setPlayerId(p.playerId);// 玩家的桌子位置
                    msg.addPlayerHandCards(handCardBuilder);
                }
            }
            log.info("--发牌消息--"+pl.name+"--"+JsonFormat.printToString(msg.build()));
            MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
            tt.setContent(msg.build().toByteString());
            tt.setOperType( MJBase.GameOperType.ErBaGameOperPlayerActionSyn);
            desk.sendMsg2Player(pl, tt.build().toByteArray());
        }

        //回放
        gameData.gameSeq = (int) (System.nanoTime() % 10000);

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            for (int i = 0; i < 4; i++) {
                List<Byte> cl = gameData.mPlayerCardsMap.get(i).cardsInHand;
                gameData.recorder.recordErBaPlayerCard(cl, i);
            }
        } else {
            for (PlayerInfo pl : gameData.erBaCurrentGamingPlayers) {
                List<Byte> cl = gameData.mPlayerCardsMap.get(pl.playerId).cardsInHand;
                gameData.recorder.recordErBaPlayerCard(cl, pl.playerId);
            }
        }

        List<RecordData> recordData = new ArrayList<>();
        for (PlayerInfo pl : gameData.erBaCurrentGamingPlayers) {
            ErBaRecordData erBaRecordData = new ErBaRecordData();
            erBaRecordData.playerId = pl.playerId;
            erBaRecordData.score = gameData.erBaFinalResult.finalResults.get(pl.playerId).allScore;
            recordData.add(erBaRecordData);
        }
        gameData.recorder.recordErBaGameStart(gameData.erBaCurrentGamingPlayers.toArray(new PlayerInfo[0]) ,recordData, gameData.mDeskCard.cards);
        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),-1, PokerConstants.ErBa_OPERTAION_SEND_CARD,-1,new ArrayList<>(),new ArrayList<>(),-1, -1);

        //游戏数据
        gameData.showInitCardTime = System.currentTimeMillis();
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
        gameData.setWaitingStartTime(System.currentTimeMillis());
    }

    //对红中5倍，对子4倍，28杠3倍，8.9点2倍(同点数区分大小，必10不区分)
    private int getMulti(GamePacket.MyGame_Player_Cards cards) {
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_FENG_KUANG) {
            if (cards.cardNum == 40) {
                return 5;
            } else if (cards.cardNum > 20){
                return 4;
            } else if (cards.cardNum == 20) {
                return 3;
            } else if (cards.cardNum >= 16) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    private void gameOver(GameData gameData, MJDesk<byte[]> desk) {
        PlayerInfo banker = gameData.mPlayersMap.get(gameData.robIndex);

        if (!gameData.erBaDiamondMap.getOrDefault(gameData.robIndex, false)) {
            desk.subServiceFee(banker);
            gameData.erBaDiamondMap.put(banker.playerId, true);
        }

        // 比牌结果 玩家id(四门类型) - 输赢
        Map<Integer,Integer> biPaiResult = biPai();
        // 玩家id - 分数
        Map<Integer,Integer> biPaiUserScoreMap  = getBiPaiUserScoreMap(biPaiResult);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--打牌结束庄家:"+gameData.robIndex);
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            for (int i = 1; i < 4; i++) {
                log.info("桌子id--" + desk.getDeskID() + "--" + ErBaHelper.siMenMap.get(i) + "--" + (biPaiResult.get(i) == 1 ? "赢" : "输"));
            }
        } else {
            for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                if (p == null || p.playerId == gameData.robIndex) continue;
                log.info("桌子id--" + desk.getDeskID() + "--" + "玩家--" + p.name + "--" + (biPaiResult.get(p.playerId) == 1 ? "赢" : "输"));
            }
        }
        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            log.info("桌子id--" + desk.getDeskID() + "--" + "玩家--" + p.name + "--分数:" + biPaiUserScoreMap.get(p.playerId));
        }
        gameData.erBaResult.endTime = System.currentTimeMillis();
        gameData.erBaResult.juNum = gameData.handNum;

        log.error("----------------------------------------------");
        for(PokerErBaFinalResult f : gameData.erBaFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerErBaResult f : gameData.erBaResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allScore);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : gameData.erBaCurrentGamingPlayers){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        int score = 0;

        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            if (p == null || p.playerId == gameData.robIndex) continue;
            PokerErBaResult result = gameData.erBaResult.Result.get(p.playerId);
            result.pos = p.position;
            result.playerId = p.playerId;
            result.playerName = p.name;
            result.lastScore = result.score;
            result.score = biPaiUserScoreMap.get(p.playerId);
            if ((result.score + result.allScore < 0) && desk.isClubJiFenDesk() && desk.getCanFufen() == 1) {
                result.score = -result.allScore;
            }
            result.allScore += result.score;
//            result.cardType = gameData.mPlayerCardsMap.get(p.playerId).cardType;
//            result.cardNum = gameData.mPlayerCardsMap.get(p.playerId).cardNum;
            result.isBanker = false;
            result.result = biPaiUserScoreMap.get(p.playerId) < 0 ? PokerConstants.GAME_RESULT_LOSE : PokerConstants.GAME_RESULT_WIN;
            result.maxCardType = result.maxCardType > result.cardType ? result.maxCardType : result.cardType;
            result.maxScore = result.maxScore > result.score ? result.maxScore : result.score;

            PokerErBaFinalResult finalResult = gameData.erBaFinalResult.finalResults.get(p.playerId);
            finalResult.allScore = result.allScore;
            finalResult.loseNum += (biPaiUserScoreMap.get(p.playerId) < 0 ? 1 : 0);
            finalResult.winNum += (biPaiUserScoreMap.get(p.playerId) > 0 ? 1 : 0);
            finalResult.maxCardType = result.maxCardType;
            finalResult.maxScore = result.maxScore;
            finalResult.score += result.score;

            score -= result.score;
            p.curJuScore = result.score;
        }


        log.error("----------------------------------------------");
        for(PokerErBaFinalResult f : gameData.erBaFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerErBaResult f : gameData.erBaResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allScore);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : gameData.erBaCurrentGamingPlayers){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        PlayerInfo p = gameData.mPlayersMap.get(gameData.robIndex);
        PokerErBaResult result = gameData.erBaResult.Result.get(p.playerId);
        PokerErBaFinalResult finalResult = gameData.erBaFinalResult.finalResults.get(p.playerId);
        result.pos = gameData.robIndex;
        result.playerId = p.playerId;
        result.playerName = p.name;
        result.lastScore = result.score;
        result.score = score;
        p.curJuScore = result.score;
        if(desk.isClubJiFenDesk()) {
            if (result.score + result.allScore < 0 && desk.getCanFufen() == 1) {
                //庄家本局最多输桌面的钱
                result.score = -result.allScore;
                List<PlayerInfo> list = desk.loopGetPlayerErBa(gameData.robIndex, desk.getPlayingPlayers().size() - 1, 0);
                if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                    list = gameData.erBaCurrentGamingPlayers;
                }
                //庄家桌面的钱加上 桌子上输家的输的前作为分配的钱(分配给赢家)
                int allScore = result.allScore;
                for (PlayerInfo pl : list) {
                    if (pl == null || p.playerId == gameData.robIndex) continue;
                    if (biPaiUserScoreMap.get(p.playerId) < 0) {
                        allScore -= pl.curJuScore;
                    }
                }

                //赢家的钱重新分配
                for (PlayerInfo pl : list) {
                    if (pl == null || biPaiUserScoreMap.get(p.playerId) < 0 || p.playerId == gameData.robIndex) continue;
                    PokerErBaResult res = gameData.erBaResult.Result.get(pl.playerId);
                    PokerErBaFinalResult finalRes = gameData.erBaFinalResult.finalResults.get(pl.playerId);

                    //先还原成上一局的总分数
                    res.allScore -= res.score;
                    finalRes.score -= res.score;
                    //按照规则分配
                    if (allScore < res.score) {
                        res.score = allScore < 0 ? 0 : allScore;
                    }
                    //分配完之后重新计算总分数
                    res.allScore += res.score;
                    finalRes.allScore = res.allScore;
                    finalRes.score += res.score;
                    allScore -= res.score;
                }
            }
        }

        result.allScore += result.score;
//        result.cardType = gameData.mPlayerCardsMap.get(p.playerId).cardType;
//        result.cardNum = gameData.mPlayerCardsMap.get(p.playerId).cardNum;
        result.isBanker = true;
        result.result = score > 0 ?PokerConstants.GAME_RESULT_WIN : (score == 0 ? PokerConstants.GAME_RESULT_EVEN : PokerConstants.GAME_RESULT_LOSE);
        result.maxCardType = result.maxCardType > result.cardType ? result.maxCardType : result.cardType;
        result.maxScore = result.maxScore > result.score ? result.maxScore : result.score;

        finalResult.allScore = result.allScore;
        finalResult.loseNum += (score < 0 ? 1: 0);
        finalResult.winNum += (score > 0 ? 1: 0);
        finalResult.maxCardType = result.maxCardType;
        finalResult.maxScore = result.maxScore;
        finalResult.score += result.score;

        log.error("----------------------------------------------");
        for(PokerErBaFinalResult f : gameData.erBaFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerErBaResult f : gameData.erBaResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allScore);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : gameData.erBaCurrentGamingPlayers){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }

        ErBa.ErBaGameOperPlayerActionNotify.Builder notify = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
        notify.setPlayerId(-1);
        notify.setActions(PokerConstants.POKER_OPERTAION_GAME_OVER);

        //消息推送
        ErBaPokerPushHelper.pushActionNofity(gameData,desk,null,notify, PokerConstants.SEND_TYPE_ALL);

        gameData.recorder.recordErBaPlayerAction(gameData.genSeq(),-1, PokerConstants.POKER_OPERTAION_GAME_OVER,-1,new ArrayList<>(),new ArrayList<>(),-1, -1);


        //设置下一个庄家的下标
        if(desk.canLunLiuZhuang()){
            List<PlayerInfo> nextPlayer = desk.loopGetPlayerErBa(banker.position, 1, 0);
            gameData.robIndex = nextPlayer.get(0).playerId;
            gameData.mPublic.mBankerUserId = gameData.robIndex;
        } else if (desk.canJingDianQiangZhuang()) {
            // 初始化
            gameData.robIndex = -1;
            gameData.mPublic.mBankerUserId = 0;
        } else {
            // 房主庄,不变
        }

        if (desk.getErBaGameType() != PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG){
            for (PlayerInfo player : gameData.erBaCurrentGamingPlayers) {
                PokerErBaFinalResult f = gameData.erBaFinalResult.finalResults.get(player.playerId);
                int cardNum = gameData.mPlayerCardsMap.get(player.playerId).cardNum;
                if (f != null) {
                    if (cardNum == 40) {
                        f.duiHongZhongNum++;
                    } else if (cardNum > 20) {
                        f.duiZiNum++;
                    } else if (cardNum == 20) {
                        f.erBaNum++;
                    } else if (cardNum >= 16) {
                        f.sanPai89Num++;
                    }
                }
            }
        }

        gameData.trandition28UserChouMaMap.clear();
        gameData.chouMaMap.clear();
        //设置一局结束的状态,循环获取状态后结束这局游戏
        gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);
    }

    private Map<Integer, Integer> getBiPaiUserScoreMap(Map<Integer, Integer> biPaiResult) {
        Map<Integer, Integer> result = new HashMap<>();
        int allScore = 0;
        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            if (p.playerId == gameData.robIndex) continue;
            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                int userScore = 0;
                for (int i = 1; i < 4; i++) {
                    int chouMa = 0;
//                    try {
                        chouMa = gameData.trandition28UserChouMaMap.get(p.playerId).get(i);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        log.info(gameData.trandition28UserChouMaMap);
//                        log.info(p.playerId);
//                    }
                    userScore += (biPaiResult.get(i) == PokerConstants.GAME_RESULT_WIN ? chouMa : -chouMa);
                }
                result.put(p.playerId, userScore);
                allScore += userScore;
            } else if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_JING_DIAN) {
                // 经典28. 没有倍数
                int chouMa = gameData.chouMaMap.get(p.playerId);
                int userScore = biPaiResult.get(p.playerId) == PokerConstants.GAME_RESULT_WIN ? chouMa : -chouMa;
                result.put(p.playerId, userScore);
                allScore += userScore;
            } else {
                // 疯狂28 ,有倍数
                PlayerInfo banker = gameData.mPlayersMap.get(gameData.robIndex);
                int chouMa = gameData.chouMaMap.get(p.playerId);
                int userScore = biPaiResult.get(p.playerId) == PokerConstants.GAME_RESULT_WIN ? p.multiple * chouMa : -banker.multiple * chouMa;
                result.put(p.playerId, userScore);
                allScore += userScore;
            }
        }
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            result.put(gameData.robIndex, allScore);

        } else {
            result.put(gameData.robIndex, allScore);
        }
        return result;
    }

    private Map<Integer, Integer> biPai() {
        Map<Integer, Integer> result = new HashMap<>();
        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            int cardNumBanker = gameData.mPlayerCardsMap.get(PokerConstants.ErBa_SIMEN_ZHUANG_JIA).cardNum;
            List<Byte> cardsInHandBanker = new ArrayList<>(gameData.mPlayerCardsMap.get(PokerConstants.ErBa_SIMEN_ZHUANG_JIA).cardsInHand);
            for (int i = 1; i < 4; i++) {
                int cardNum = gameData.mPlayerCardsMap.get(i).cardNum;
                List<Byte> cardsInHand = new ArrayList<>(gameData.mPlayerCardsMap.get(i).cardsInHand);
                int r;
                if (cardNum == cardNumBanker) {
                    if (cardNum == 0) {
                        r = PokerConstants.GAME_RESULT_LOSE;
                    } else {
                        Collections.sort(cardsInHandBanker);
                        Collections.sort(cardsInHand);
                        r = cardsInHand.get(1) > cardsInHandBanker.get(1) ? PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
                    }
                } else {
                    r = cardNum > cardNumBanker ? PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
                }
                result.put(i, r);
            }
        } else {
            int cardNumBanker = gameData.mPlayerCardsMap.get(gameData.robIndex).cardNum;
            List<Byte> cardsInHandBanker = new ArrayList<>(gameData.mPlayerCardsMap.get(gameData.robIndex).cardsInHand);
            for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
                if (p.playerId == gameData.robIndex) continue;
                int cardNum = gameData.mPlayerCardsMap.get(p.playerId).cardNum;
                List<Byte> cardsInHand = new ArrayList<>(gameData.mPlayerCardsMap.get(p.playerId).cardsInHand);
                int r;
                if (cardNum == cardNumBanker) {
                    if (cardNum == 0) {
                        r = PokerConstants.GAME_RESULT_LOSE;
                    } else {
                        Collections.sort(cardsInHandBanker);
                        Collections.sort(cardsInHand);
                        r = cardsInHand.get(1) > cardsInHandBanker.get(1) ? PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
                    }
                } else {
                    r = cardNum > cardNumBanker ? PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
                }
                result.put(p.playerId, r);
            }
         }
         return result;
    }

    private int getCardType(List<Byte> cardsInHand) {
        if (cardsInHand == null || cardsInHand.isEmpty() || cardsInHand.size() != 2) {
            log.error("出大问题了--"+ cardsInHand);
            return 0;
        }

        if (isDuiZi(cardsInHand)) {
            return PokerConstants.ERBA_CARDTYPE_DUI_ZI;
        }

        if (is28(cardsInHand)) {
            return PokerConstants.ERBA_CARDTYPE_ER_BA;
        }

        return PokerConstants.ERBA_CARDTYPE_SAN_PAI;
    }

    private boolean is28(List<Byte> cardsInHand) {
        int a = cardsInHand.get(0);
        int b = cardsInHand.get(1);
        return (a == 0x28 && b == 0x22) || (a == 0x22 && b == 0x28);
    }

    private boolean isDuiZi(List<Byte> cardsInHand) {
        return cardsInHand.get(0).equals(cardsInHand.get(1));
    }

    private int getCardNum(List<Byte> cardsInHand) {
        if (cardsInHand == null || cardsInHand.isEmpty() || cardsInHand.size() != 2) return 0;
        int type = getCardType(cardsInHand);
        if (type == PokerConstants.ERBA_CARDTYPE_DUI_ZI) {
            if (cardsInHand.get(0) == PokerConstants.MJ_CODE_HONG_ZHONG) {
                return 20 * 2;
            } else {
                int value = cardsInHand.get(0) & 0x0f;
                return (value + 10) * 2;
            }
        } else if (type == PokerConstants.ERBA_CARDTYPE_ER_BA) {
            return 10 * 2;
        } else {
            Collections.sort(cardsInHand);
            int a = cardsInHand.get(0);
            int b = cardsInHand.get(1);
            int value1 = cardsInHand.get(0) & 0x0f;
            int value2 = cardsInHand.get(1) & 0x0f;
            if (b == PokerConstants.MJ_CODE_HONG_ZHONG) {
                return value1 * 2 + 1;
            } else {
                return ((value1 + value2) % 10) * 2;
            }
        }
    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position) {
        
    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--" + desk.getDeskID() + "--" + "act=repushGameData; name={};deskId={};", p.name, desk.getDeskID());

        //把当前桌子的状况发给重连玩家
        ErBa.ErBaGameOperStartSyn.Builder erBa = ErBa.ErBaGameOperStartSyn.newBuilder();
        erBa.setJuNum(gameData.handNum);//桌子当前圈数
        erBa.setBankerId(gameData.mPublic.mBankerUserId);//当前地主的下标
        erBa.setReconnect(true);//表示断线重连
        erBa.setSeq(gameData.gameSeq);
        gameData.recorder.seq = erBa.getSeq(); // 记录序列号

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            // 传统28
            for (Integer playerId : gameData.erBaAllGamingPlayers) {
                ErBa.ErBaGameOperHandCardSyn.Builder builder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                builder.setPlayerId(playerId);

                // 该玩家是本局游戏玩家
                if (checkPlayerInCurrentGame(playerId)) {
                    // 如果已经下注,设置下注信息
                    if (gameData.trandition28UserChouMaMap.containsKey(playerId)) {
                        for (Map.Entry<Integer,Integer> entry : gameData.trandition28UserChouMaMap.get(playerId).entrySet()) {
                            ErBa.TraditionalErBaUserYaZhu.Builder userXiaZhu = ErBa.TraditionalErBaUserYaZhu.newBuilder();
                            userXiaZhu.setPlayerId(playerId);
                            userXiaZhu.setSiMenType(entry.getKey());
                            userXiaZhu.setChouMa(entry.getValue());
                            builder.addTraditionXiaZhu(userXiaZhu);
                        }
                    }
                }

                // 设置玩家的分数
                if (gameData.erBaFinalResult.finalResults.get(playerId) != null)
                    builder.setSocre(gameData.erBaFinalResult.finalResults.get(playerId).allScore);

                erBa.addPlayerHandCards(builder);
            }
            for (int i = 0; i < 4; i++) {
                ErBa.TraditionalErBaSiMenInfo.Builder siMenInfo = ErBa.TraditionalErBaSiMenInfo.newBuilder();
                siMenInfo.setSiMenType(i);
                siMenInfo.setChouMa(gameData.siMenChouMaMap.getOrDefault(i, 0));
                if (gameData.mPlayerCardsMap.containsKey(i)) {
                    List<Byte> cardsInHand = gameData.mPlayerCardsMap.get(i).cardsInHand;
                    if (!cardsInHand.isEmpty()) {
                        for (int j = 0; j < 2; j++) {
                            siMenInfo.addCardsInHand(gameData.erBaLiangPaiMap.get(i).get(j) ? cardsInHand.get(j) : -1);
                        }
                    }
                }
                erBa.addSiMenInfo(siMenInfo);
            }
        } else {
            for (Integer playerId : gameData.erBaAllGamingPlayers) {
                // 经典28+疯狂28
                ErBa.ErBaGameOperHandCardSyn.Builder builder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                builder.setPlayerId(playerId);

                // 该玩家是本局游戏玩家
                if (checkPlayerInCurrentGame(playerId)) {
                    //手牌
                    if (gameData.erBaLiangPaiMap.get(playerId) != null) {
                        for (int i = 0; i < 2; i++) {
                            builder.addHandCards(gameData.erBaLiangPaiMap.get(playerId).get(i) && !gameData.mPlayerCardsMap.get(playerId).cardsInHand.isEmpty()? gameData.mPlayerCardsMap.get(playerId).cardsInHand.get(i) : -1);
                        }
                    }

                    builder.setXiaZhu(gameData.mPlayersMap.get(playerId).chouMa);
                }

                // 设置玩家的分数
                if (gameData.erBaFinalResult.finalResults.get(playerId) != null)
                    builder.setSocre(gameData.erBaFinalResult.finalResults.get(playerId).allScore);

                erBa.addPlayerHandCards(builder);
            }
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.ErBaGameOperStartSyn);
        gb.setContent(erBa.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(erBa.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());

        // 发送桌子公告信息
//        ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData,getDeskPublicInfoMsg(desk));

        //推送桌子消息
        ErBa.ErBaGameOperPublicInfoSyn.Builder erba = ErBa.ErBaGameOperPublicInfoSyn.newBuilder();
        erba.setDeskState(gameData.currentDeskState);
        ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData, erba);

        // 发送当前操作人
//        ErBaPokerPushHelper.pushActorSyn(desk, p, gameData.getPokerOpPlayerId(), 9, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, p);
    }

    private boolean checkPlayerInCurrentGame(int playerId) {
        for (PlayerInfo p : gameData.erBaCurrentGamingPlayers) {
            if (p.playerId == playerId) return true;
        }
        return false;
    }


    @Override
    public void pushDeskInfo(GameData mGameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子信息给新来玩家");

        //把当前桌子的状况发给重连玩家
        ErBa.ErBaGameOperStartSyn.Builder erBa = ErBa.ErBaGameOperStartSyn.newBuilder();
        erBa.setJuNum(gameData.handNum);//桌子当前圈数
        erBa.setBankerId(gameData.mPublic.mBankerUserId);//当前地主的下标
        erBa.setReconnect(true);//表示断线重连
        erBa.setSeq(gameData.gameSeq);
        gameData.recorder.seq = erBa.getSeq(); // 记录序列号

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            // 传统28
            for (Integer playerId : gameData.erBaAllGamingPlayers) {
                ErBa.ErBaGameOperHandCardSyn.Builder builder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                builder.setPlayerId(playerId);

                // 该玩家是本局游戏玩家
                if (checkPlayerInCurrentGame(playerId)) {
                    // 如果已经下注,设置下注信息
                    if (gameData.trandition28UserChouMaMap.containsKey(playerId)) {
                        for (Map.Entry<Integer,Integer> entry : gameData.trandition28UserChouMaMap.get(playerId).entrySet()) {
                            ErBa.TraditionalErBaUserYaZhu.Builder userXiaZhu = ErBa.TraditionalErBaUserYaZhu.newBuilder();
                            userXiaZhu.setPlayerId(playerId);
                            userXiaZhu.setSiMenType(entry.getKey());
                            userXiaZhu.setChouMa(entry.getValue());
                            builder.addTraditionXiaZhu(userXiaZhu);
                        }
                    }
                }

                // 设置玩家的分数
                if (gameData.erBaFinalResult.finalResults.get(playerId) != null)
                    builder.setSocre(gameData.erBaFinalResult.finalResults.get(playerId).allScore);

                erBa.addPlayerHandCards(builder);
            }
            for (int i = 0; i < 4; i++) {
                ErBa.TraditionalErBaSiMenInfo.Builder siMenInfo = ErBa.TraditionalErBaSiMenInfo.newBuilder();
                siMenInfo.setSiMenType(i);
                siMenInfo.setChouMa(gameData.siMenChouMaMap.getOrDefault(i, 0));
                siMenInfo.addAllCardsInHand(gameData.mPlayerCardsMap.containsKey(i) ? ErBaProcessor.byte2IntList(gameData.mPlayerCardsMap.get(i).cardsInHand): new ArrayList<>());
                erBa.addSiMenInfo(siMenInfo);
            }
        } else {
            for (Integer playerId : gameData.erBaAllGamingPlayers) {
                // 经典28+疯狂28
                ErBa.ErBaGameOperHandCardSyn.Builder builder = ErBa.ErBaGameOperHandCardSyn.newBuilder();
                builder.setPlayerId(playerId);

                // 该玩家是本局游戏玩家
                if (checkPlayerInCurrentGame(playerId)) {
                    //手牌
                    if (gameData.erBaLiangPaiMap.get(playerId) != null) {
                        for (int i = 0; i < 2; i++) {
                            builder.addHandCards(gameData.erBaLiangPaiMap.get(playerId).get(i) ? gameData.mPlayerCardsMap.get(playerId).cardsInHand.get(i) : -1);
                        }
                    }

                    builder.setXiaZhu(gameData.mPlayersMap.get(playerId).chouMa);
                }

                // 设置玩家的分数
                if (gameData.erBaFinalResult.finalResults.get(playerId) != null)
                    builder.setSocre(gameData.erBaFinalResult.finalResults.get(playerId).allScore);

                erBa.addPlayerHandCards(builder);
            }
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.ErBaGameOperStartSyn);
        gb.setContent(erBa.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(erBa.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());

        //推送桌子消息
        ErBa.ErBaGameOperPublicInfoSyn.Builder erba = ErBa.ErBaGameOperPublicInfoSyn.newBuilder();
        erba.setDeskState(gameData.currentDeskState);
        ErBaPokerPushHelper.pushPublicInfoMsg2Single(desk, p, gameData, erba);

        if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) this.erBa_notify_new_player(gameData, desk, p);

    }

    @Override
    public void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD) return;

        if (gameData.erBaFinalResult.finalResults.get(p.playerId) == null) {
            PokerErBaFinalResult erBaFinalResult = new PokerErBaFinalResult();
            erBaFinalResult.playerId = p.playerId;
            erBaFinalResult.playerName = p.name;
            erBaFinalResult.headImg = p.headImg;
            gameData.erBaFinalResult.finalResults.put(p.playerId,erBaFinalResult);
        }

        if(gameData.erBaResult.Result.get(p.playerId) == null) {
            gameData.erBaResult.Result.put(p.playerId,new PokerErBaResult());
        }
        PokerErBaResult erBaResult = gameData.erBaResult.Result.get(p.playerId);
        erBaResult.playerId = p.playerId;
        erBaResult.playerName = p.name;
        erBaResult.result = PokerDDZResult.GAME_RESULT_EVEN;
        erBaResult.pos = p.position;

        gameData.mPlayersMap.put(p.playerId, p);
        if (!checkPlayerInCurrentGame(p.playerId)) {
            gameData.erBaCurrentGamingPlayers.add(p);
        }
        if (gameData.mPlayerActionMap.get(p.playerId) == null) gameData.mPlayerActionMap.put(p.playerId, new GamePacket.MyGame_Player_Action());
        if (gameData.mPlayerCardsMap.get(p.playerId) == null) gameData.mPlayerCardsMap.put(p.playerId, new GamePacket.MyGame_Player_Cards());
        if (!gameData.erBaAllGamingPlayers.contains(p.playerId)) gameData.erBaAllGamingPlayers.add(p.playerId);
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            if (gameData.robIndex > 0) return;
            if (gameData.qiangZhuangMap.containsKey(p.playerId)) return;
            log.info("桌子id--" + desk.getDeskID() + "--" + "重新提示玩家--" + p.name + "抢庄");
            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(p.playerId);
            if (desk.getErBaGameType() != PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG | PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG);
            } else {
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG );
            }
            //设置超时
            gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();
            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);
        } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU) {
            if(p.playerId == gameData.robIndex ) return;
            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                if (gameData.trandition28UserChouMaMap.containsKey(p.playerId)) return;
            } else {
                if (gameData.chouMaMap.containsKey(p.playerId)) return;
            }
            log.info("桌子id--" + desk.getDeskID() + "--" + "重新提示玩家--" + p.name + "下注");
            //设置超时
            gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();

            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(p.playerId);
            msg.setActions(PokerConstants.ErBa_OPERTAION_XIA_ZHU);
            if (desk.getYaZhu() == PokerConstants.ErBa_WU_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(5);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
            } else if (desk.getYaZhu() == PokerConstants.ErBa_YI_BAI_LIU_SHI_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(160);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(20, 40, 80, 160)));
            } else if (desk.getYaZhu() == PokerConstants.ErBa_ER_SHI_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(20);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(5, 10, 15, 20)));
            } else if (desk.getYaZhu() == PokerConstants.ERBA_ZI_YOU_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(100);
                else
                    msg.addXiaZhu(-1);
            }
            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);
        }
    }

    /**
     * 重新通知玩家操作
     *
     */
    @Override
    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, int playerId) {

    }

    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {
        if (!checkPlayerInCurrentGame(p.playerId)) return;
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            if (gameData.robIndex > 0) return;
            if (gameData.qiangZhuangMap.containsKey(p.playerId)) return;
            log.info("桌子id--" + desk.getDeskID() + "--" + "重新提示玩家--" + p.name + "抢庄");
            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(p.playerId);
            if (desk.getErBaGameType() != PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG | PokerConstants.ErBa_OPERTAION_BU_QIANG_ZHUANG);
            } else {
                msg.setActions(PokerConstants.ErBa_OPERTAION_QIANG_ZHUANG );
            }
            //设置超时
            if (gameData.mPlayerActionMap.get(p.playerId) == null) gameData.mPlayerActionMap.put(p.playerId, new GamePacket.MyGame_Player_Action());
            if (gameData.mPlayerCardsMap.get(p.playerId) == null) gameData.mPlayerCardsMap.put(p.playerId, new GamePacket.MyGame_Player_Cards());
            gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();
            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);
        } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU) {
            if(p.playerId == gameData.robIndex ) return;
            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                if (gameData.trandition28UserChouMaMap.containsKey(p.playerId)) return;
            } else {
                if (gameData.chouMaMap.containsKey(p.playerId)) return;
            }
            log.info("桌子id--" + desk.getDeskID() + "--" + "重新提示玩家--" + p.name + "下注");
            if (gameData.mPlayerActionMap.get(p.playerId) == null) gameData.mPlayerActionMap.put(p.playerId, new GamePacket.MyGame_Player_Action());
            if (gameData.mPlayerCardsMap.get(p.playerId) == null) gameData.mPlayerCardsMap.put(p.playerId, new GamePacket.MyGame_Player_Cards());
            //设置超时
            gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();

            ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
            msg.setPlayerId(p.playerId);
            msg.setActions(PokerConstants.ErBa_OPERTAION_XIA_ZHU);
            if (desk.getYaZhu() == PokerConstants.ErBa_WU_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(5);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
            } else if (desk.getYaZhu() == PokerConstants.ErBa_YI_BAI_LIU_SHI_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(160);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(20, 40, 80, 160)));
            } else if (desk.getYaZhu() == PokerConstants.ErBa_ER_SHI_FEN_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(20);
                else
                    msg.addAllXiaZhu(new ArrayList<>(Arrays.asList(5, 10, 15, 20)));
            } else if (desk.getYaZhu() == PokerConstants.ERBA_ZI_YOU_CAHNG) {
                if (desk.canXiaManZhu())
                    msg.addXiaZhu(100);
                else
                    msg.addXiaZhu(-1);
            }
            //推送消息
            ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 15, PokerConstants.SEND_TYPE_ALL);
        } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD) {
            if (desk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
                if (p.playerId != gameData.robIndex || p.isKaiPai) return;
                log.info("桌子id--" + desk.getDeskID() + "--" + "提示庄家--" + p.name + "操作");

                // 设置操作时间
                if (gameData.mPlayerActionMap.get(p.playerId) == null) gameData.mPlayerActionMap.put(p.playerId, new GamePacket.MyGame_Player_Action());
                if (gameData.mPlayerCardsMap.get(p.playerId) == null) gameData.mPlayerCardsMap.put(p.playerId, new GamePacket.MyGame_Player_Cards());
                gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();

                ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
                msg.setPlayerId(p.playerId);
                msg.setActions(PokerConstants.ErBa_OPERTAION_KAI_CARD | PokerConstants.ErBa_OPERTAION_SEE_CARD);

                //推送消息
                ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);

                //广播当前正在操作的玩家
                ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 10, PokerConstants.SEND_TYPE_ALL);
            } else {
                if (p.isKaiPai) return;
                boolean allPaiLiangPai = true;

                for (Boolean b : gameData.erBaLiangPaiMap.get(p.playerId).values()) {
                    if (!b) {
                        allPaiLiangPai = false;
                        break;
                    }
                }
                log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + p.name + "操作");

                // 设置操作时间
                if (gameData.mPlayerActionMap.get(p.playerId) == null) gameData.mPlayerActionMap.put(p.playerId, new GamePacket.MyGame_Player_Action());
                if (gameData.mPlayerCardsMap.get(p.playerId) == null) gameData.mPlayerCardsMap.put(p.playerId, new GamePacket.MyGame_Player_Cards());
                gameData.mPlayerActionMap.get(p.playerId).opStartTime = System.currentTimeMillis();

                ErBa.ErBaGameOperPlayerActionNotify.Builder msg = ErBa.ErBaGameOperPlayerActionNotify.newBuilder();
                msg.setPlayerId(p.playerId);
                int actions = PokerConstants.ErBa_OPERTAION_KAI_CARD;
                if (!allPaiLiangPai) actions |= PokerConstants.ErBa_OPERTAION_SEE_CARD;
                msg.setActions(actions);

                //推送消息
                ErBaPokerPushHelper.pushActionNofity(gameData, desk, p, msg, PokerConstants.SEND_TYPE_SINGLE);

                //广播当前正在操作的玩家
                ErBaPokerPushHelper.pushActorSyn(desk, p, p.playerId, 10, PokerConstants.SEND_TYPE_ALL);
            }
        }
    }

    /**
     * 设置下一把庄家
     * 默认是开房间的人,开房间的人的座位号为0
     */
    @Override
    public void selectBanker(GameData data, MJDesk<byte[]> desk) {
        if(data.handNum == 0 && !desk.canJingDianQiangZhuang()) gameData.robIndex = desk.getStarterId();

        if (desk.canLunLiuZhuang() && gameData.robIndex <= 0)  gameData.robIndex = desk.getPlayers().get(0).playerId;
        if (gameData.robIndex > 0) {
            data.mPublic.mBankerUserId = gameData.robIndex;
            desk.erBaXiaZhuOrConfirmBanker(gameData.mPlayersMap.get(gameData.robIndex));
        }
    }

    @Override
    public void notifyDouble(GameData gameData, MJDesk<byte[]> desk) {

    }
}
