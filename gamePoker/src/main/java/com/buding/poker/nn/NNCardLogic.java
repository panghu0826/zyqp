package com.buding.poker.nn;

import com.buding.api.context.PokerNNFinalResult;
import com.buding.api.context.PokerNNResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.NNRecordData;
import com.buding.api.player.PlayerInfo;
import com.buding.api.player.RecordData;
import com.buding.card.ICardLogic;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.poker.common.CardWasher;
import com.buding.poker.common.NNBiPaiResult;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.NNHelper;
import com.buding.poker.helper.NNPokerPushHelper;
import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.mj.MJBase;
import packet.nn.NN;
import packet.nn.NN.NNGameOperPlayerActionSyn;

import java.util.*;

public class NNCardLogic implements ICardLogic<MJDesk<byte[]>> {

    private Logger log = LogManager.getLogger("DESKLOG");
    private NNProcessor processor = new NNProcessor();
    private GameData gameData;
    private MJDesk<byte[]> desk;

    @Override
    public void init(GameData gameData, MJDesk<byte[]> desk) {
        this.gameData = gameData;
        this.desk = desk;
    }

    @Override
    public void gameStart(GameData data, MJDesk<byte[]> desk) {
        //=================================福利玩家,作弊系统==================================
        List<Integer> fuLiPlayerList = desk.getfuLiPlayerList(desk.getGameId());
        for (PlayerInfo p : data.mPlayers) {
            if (p == null) continue;
            if (fuLiPlayerList.contains(p.playerId) && data.fuliPlayerMap.get(p.playerId) == null) {
                if (desk.getTotalQuan() - data.handNum > 2) {
                    //随机两把
                    List<Integer> juNumList = new ArrayList<>();
                    for (int i = data.handNum; i < desk.getTotalQuan() + 1; i++) {
                        juNumList.add(i);
                    }
                    Collections.shuffle(juNumList);
                    List<Integer> fuliJuNumList = new ArrayList<>();
                    fuliJuNumList.add(juNumList.get(0));
                    fuliJuNumList.add(juNumList.get(1));
                    data.fuliPlayerMap.put(p.playerId, fuliJuNumList);
                }
            }
        }
        List<PlayerInfo> playerList = desk.getPlayingPlayers();
        int fuliPlayerPos = processor.isFuliPlayerAndFaPai(desk,data);
        if(fuliPlayerPos >= 0) {
            playerList = desk.loopGetPlayerNN(fuliPlayerPos,desk.getPlayingPlayers().size(),2);
            List<Byte> deskCardsNew = new ArrayList<>(data.mDeskCard.cards);
            data.mDeskCard.cards = processor.fuli(desk.getPlayingPlayers().size(),deskCardsNew,5,desk);
        }
        log.info("福利局数据--"+gameData.fuliPlayerMap+"发牌--"+data.mDeskCard.cards);
        log.info("桌子id--"+desk.getDeskID()+"--"+"playerList-----"+playerList);
        //=================================福利玩家,作弊系统==================================

        sendCards2Players(desk, playerList);

        gameData.gameSeq = (int) (System.nanoTime() % 10000);

        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            gameData.recorder.recordNNPlayerCard(cl , pl.position);
        }

        List<RecordData> recordData = new ArrayList<>();
        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            NNRecordData nnRecordData = new NNRecordData();
            nnRecordData.position = pl.position;
            nnRecordData.cardType = (gameData.mPlayerCards[pl.position].nnBiPaiResult.cardTypeStr);
            nnRecordData.score = gameData.nnFinalResult.finalResults.get(pl.playerId).allScore;
            recordData.add(nnRecordData);
        }

        gameData.recorder.recordNNGameStart(gameData.mPlayers ,recordData, gameData.mDeskCard.cards);
        gameData.recorder.recordBasicInfo(gameData);
        // 把牌下发给客户端
        NN.NNGameOperStartSyn.Builder msg = NN.NNGameOperStartSyn.newBuilder();
        msg.setJuNum(gameData.handNum);// 当前局数
        msg.setSeq(gameData.gameSeq);
        msg.setBankerPos(!processor.canQiangZhuang(desk) ? gameData.currentRobIndex : -1);
        gameData.recorder.seq = msg.getSeq(); // 记录序列号
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null || pl.isZanLi || pl.isWait) continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            log.info("桌子id--"+desk.getDeskID()+"--"+"act=initcards;position={};cards={};", pl.position, new Gson().toJson(cl));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--座位号--"+pl.position+"--id--"+pl.playerId);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--累计分数/金币数--"+gameData.nnFinalResult.finalResults.get(pl.playerId).allScore);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--手牌--"+ NNHelper.getSingleCardListName(gameData.getCardsInHand(pl.position)));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--排序分叉手牌--"+ NNHelper.getSingleCardListName(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSortAndFenCha));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--排序手牌--"+ NNHelper.getSingleCardListName(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSort));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--牌型--"+ gameData.mPlayerCards[pl.position].nnBiPaiResult.cardType);
        }

        for (PlayerInfo pl : desk.getAllPlayers()) {
            msg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getPlayingPlayers()) {
                NN.NNGameOperHandCardSyn.Builder handCardBuilder = NN.NNGameOperHandCardSyn.newBuilder();
                // 发给玩家的牌
                if(gameData.fuliPlayerMap.keySet().contains(pl.playerId)) {
                    //福利玩家可以看
                    for (int card : gameData.mPlayerCards[p.position].nnBiPaiResult.cardsSortAndFenCha) {
                        handCardBuilder.addHandCards(card);
                    }
                }else{
                    //非福利玩家
                    for (int i = 0; i < gameData.getCardsInHand(p.position).size(); i++) {
                        int card = gameData.getCardsInHand(p.position).get(i);
                        if(desk.canMingPaiQiangZhuang() && i <= 3 && p.position == pl.position){
                            handCardBuilder.addHandCards(card);
                        }else{
                            handCardBuilder.addHandCards(-1);
                        }
                    }
                }

                handCardBuilder.setPosition(p.position);// 玩家的桌子位置
                handCardBuilder.setIsWait(p.isWait);
                msg.addPlayerHandCards(handCardBuilder);
            }

            MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
            gb.setOperType(MJBase.GameOperType.NNGameOperStartSyn);
            gb.setContent(msg.build().toByteString());
            gb.setType(0);

            desk.sendMsg2Player(pl, gb.build().toByteArray());
            log.info("桌子id--"+desk.getDeskID()+"--给玩家--"+ pl.name+"发牌消息"+JsonFormat.printToString(msg.build()));
        }
        gameData.showInitCardTime = System.currentTimeMillis();
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS);
        gameData.setWaitingStartTime(System.currentTimeMillis());
    }

    private void sendCards2Players(MJDesk<byte[]> desk, List<PlayerInfo> playerList) {
        try {
            for (int i = 0; i < playerList.size(); i++) {
                PlayerInfo pl = playerList.get(i);
                pl.danZhu = gameData.danZhu;
                List<Byte> cl = gameData.getCardsInHand(pl.position);
                cl.clear();
                List<Byte> src = new ArrayList<>();

                boolean faPai = false; //false 随机发牌  true 自定义发牌
                if (faPai) {
                    StringBuffer bu = new StringBuffer();
                    if (i == 0) {
                        bu.append("小王 ");
                        bu.append("梅8 ");
                        bu.append("方8 ");
                        bu.append("黑7 ");
                        bu.append("红A");
                        src.addAll(processor.chineseName2CardList(bu.toString()));

                    } else {
                        if (i == 1) {
                            bu.append("方Q ");
                            bu.append("红7 ");
                            bu.append("方3 ");
                            bu.append("梅10 ");
                            bu.append("红9");
                            src.addAll(processor.chineseName2CardList(bu.toString()));

                        }
//
//                    if(i == 2) {
//                        bu.append("梅7 ");
//                        bu.append("红6 ");
//                        bu.append("黑3 ");
//                        bu.append("梅2 ");
//                        bu.append("方6");
//                        src.addAll(processor.chineseName2CardList(bu.toString()));
//
//                    }
//
                    }
                } else {
                    for (int j = src.size(); j < 5; j++) {
                        Byte b = gameData.popCard();
                        src.add(b);
                    }
                }
                // 排个序
//            cl.addAll(NNProcessor.sortHandCards(src));
                cl.addAll(src);
                gameData.mPlayerCards[pl.position].nnBiPaiResult = processor.getCardsResult(cl, desk);

            }
        } catch (Exception e) {
            log.error("桌子id--"+desk.getDeskID()+"出问题了"+e.getMessage());
            gameData.mDeskCard.cards.clear();
            gameData.mDeskCard.cards.addAll(washCards());
            sendCards2Players(desk, playerList);
        }
    }

    public static List<Byte> washCards() {
        List<Byte> cards = new ArrayList<Byte>();
        for (int j = 0; j < 4; j++) {
            for (int i = 1; i <= 13; i++) {
                int ib = (j << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
                byte b = (byte) (ib & 0xff);
                cards.add(b);
            }
        }

//        if(mDesk.canWangLaiZi()) {
        cards.add(PokerConstants.POKER_CODE_XIAO_WANG);
        cards.add(PokerConstants.POKER_CODE_DA_WANG);
//        }

        cards = new CardWasher().wash(cards);
        return cards;
    }


    @Override
    public void gameTick(GameData data, MJDesk<byte[]> desk) {
        long ctt = System.currentTimeMillis();

//        PlayerInfo currentPl = desk.getDeskPlayer(data.getPokerOpPlayerIndex());

        //获取玩家的子原因状态
        int substate = gameData.getPlaySubstate();

        switch (substate) {
            case PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS: {
                if (ctt - gameData.getWaitingStartTime() > gameData.mGameParam.sendCardPlayMills) {
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
                }
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER: {//抢庄
                //
                if(!processor.canQiangZhuang(desk)){
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU);
                }else {
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                    //提醒玩家操作
                    player_robot_notify(data, desk);
                    gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
                }
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //提醒玩家操作
                player_xiazhu_notify(data, desk);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU;
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //提醒玩家操作
                player_chu_notify(data, desk);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD;
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_NONE: {
                //超时处理,斗地主只有在玩家只有过选项时,自动处理过
                if(desk.canForceChuPai()) {
                    for(PlayerInfo p : desk.getPlayingPlayers()) {
                        long time = gameData.mPlayerAction[p.position].opStartTime;
                        if (desk.canForceChuPai() && time != 0) {
                            NNGameOperPlayerActionSyn.Builder msg = NNGameOperPlayerActionSyn.newBuilder();
                            msg.setPosition(p.position);
                            if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER
                                    && (ctt - time) > (10 * 1000)) {
                                msg.setAction(PokerConstants.NN_OPERTAION_QIANG_ZHUANG);
                                playerAutoOper(data, desk, msg, p.position);
                            } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU
                                    && (ctt - time) > (10 * 1000)) {
                                msg.setAction(PokerConstants.NN_OPERTAION_XIA_ZHU);
                                if(desk.canXiaManZhu()){
                                    msg.setXiaZhuNum(desk.getYaZhu() == PokerConstants.NN_ER_WU_ER_SHI_CAHNG ? 20:desk.getYaZhu());
                                }else{
                                    msg.setXiaZhuNum(1);
                                }
                                playerAutoOper(data, desk, msg, p.position);
                            } else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD
                                    && (ctt - time) > (5 * 1000)) {
                                msg.setAction(PokerConstants.NN_OPERTAION_SEE_CARD);
                                playerAutoOper(data, desk, msg, p.position);
                            }

                        }
                    }
                }
            }
            break;
        }
    }



    private void player_robot_notify(GameData data, MJDesk<byte[]> desk) {
        for(PlayerInfo plx : desk.getPlayingPlayers()) {
            single_player_robot_notify(desk, plx);
        }
    }

    private void single_player_robot_notify(MJDesk<byte[]> desk, PlayerInfo plx) {
        if(plx==null || plx.nnRobotNum > PokerConstants.NN_MEI_QIANG_ZHUANG) return;
        log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + plx.name + "抢地主");
        NN.NNGameOperPlayerActionNotify.Builder msg = NN.NNGameOperPlayerActionNotify.newBuilder();
        msg.setActions(PokerConstants.NN_OPERTAION_QIANG_ZHUANG);
        msg.setPosition(plx.position);
        msg.addQiangZhuangNum(PokerConstants.NN_BU_QIANG_ZHUANG);
        if(desk.canMingPaiQiangZhuang()){
            for (int i = 1; i <= desk.getQiangZhuangNum(); i++) {
                msg.addQiangZhuangNum(i);
            }
        }else if(desk.canJingDianQiangZhuang()){
            msg.addQiangZhuangNum(PokerConstants.NN_QIANG_ZHUANG);
        }
        //推送消息
        NNPokerPushHelper.pushActionNofity(gameData, desk, plx.position, msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        NNPokerPushHelper.pushActorSyn(desk, plx.position, plx.position, 10, PokerConstants.SEND_TYPE_ALL);
        gameData.mPlayerAction[plx.position].opStartTime = System.currentTimeMillis();
    }

    private void single_player_xiazhu_notify(GameData data, MJDesk<byte[]> desk, PlayerInfo plx) {
        if(plx==null || plx.isXiaZhu || plx.position == gameData.currentRobIndex) return;
        if(desk.canXiaManZhu()) {
            gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU;
            log.info("桌子id--" + desk.getDeskID() + "--" + "帮玩家--" + plx.name + "下满注");
            NNGameOperPlayerActionSyn.Builder msg = NNGameOperPlayerActionSyn.newBuilder();
            msg.setPosition(plx.position);
            msg.setAction(PokerConstants.NN_OPERTAION_XIA_ZHU);
            msg.setXiaZhuNum(desk.getYaZhu() == PokerConstants.NN_ER_WU_ER_SHI_CAHNG ? 20 : desk.getYaZhu());
            playerAutoOper(data, desk,msg, plx.position);
        }else {
            log.info("桌子id--" + desk.getDeskID() + "--" + "提示玩家--" + plx.name + "下注");
            NN.NNGameOperPlayerActionNotify.Builder msg = NN.NNGameOperPlayerActionNotify.newBuilder();
            msg.setActions(PokerConstants.NN_OPERTAION_XIA_ZHU);
            msg.setPosition(plx.position);
            if(desk.getYaZhu() == PokerConstants.NN_ER_WU_ER_SHI_CAHNG){
                msg.addXiaZhuNum(5);
                msg.addXiaZhuNum(10);
                msg.addXiaZhuNum(15);
                msg.addXiaZhuNum(20);
            }else {
                for (int i = 1; i <= desk.getYaZhu(); i++) {
                    msg.addXiaZhuNum(i);
                }
            }
            //推送消息
            NNPokerPushHelper.pushActionNofity(gameData, desk, plx.position, msg, PokerConstants.SEND_TYPE_SINGLE);
            //广播当前正在操作的玩家
            NNPokerPushHelper.pushActorSyn(desk, plx.position, plx.position, 10, PokerConstants.SEND_TYPE_ALL);
            gameData.mPlayerAction[plx.position].opStartTime = System.currentTimeMillis();
        }
    }

    private void player_xiazhu_notify(GameData data, MJDesk<byte[]> desk) {
        for(PlayerInfo plx : desk.getPlayingPlayers()) {
            single_player_xiazhu_notify(data,desk, plx);
        }
    }

    @Override
    public void player_chu_notify(GameData gameData, MJDesk<byte[]> desk) {
        for(PlayerInfo plx : desk.getPlayingPlayers()) {
            single_player_chu_notify(gameData,desk, plx);
        }
    }

    private void single_player_chu_notify(GameData gameData, MJDesk<byte[]> desk, PlayerInfo plx) {
        if (plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为" + plx.position);
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"提示玩家--" + plx.name + "出牌");
        NN.NNGameOperPlayerActionNotify.Builder msg = NN.NNGameOperPlayerActionNotify.newBuilder();
        msg.setPosition(plx.position);
        //可以搓牌/看牌
        msg.setActions(PokerConstants.NN_OPERTAION_CUO_PAI | PokerConstants.NN_OPERTAION_SEE_CARD);

        gameData.mPlayerAction[plx.position].opStartTime = System.currentTimeMillis();
        //推送消息
        NNPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        NNPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,5, PokerConstants.SEND_TYPE_ALL);
    }

    public void playerAutoOper(GameData gameData, MJDesk<byte[]> gt,GeneratedMessage.Builder msg, int position) {
        this.playerOperation(gameData,gt,msg,gameData.mPlayers[position]);
    }

    @Override
    public void playerOperation(GameData gameData, MJDesk<byte[]> desk, GeneratedMessage.Builder m, PlayerInfo pl) {
        NNGameOperPlayerActionSyn.Builder msg = (NNGameOperPlayerActionSyn.Builder) m;

        if (msg == null || pl == null || msg.getAction() == 0) return;

        desk.setPauseTime(System.currentTimeMillis());
        //抢庄
        if ((msg.getAction() & PokerConstants.NN_OPERTAION_QIANG_ZHUANG) == PokerConstants.NN_OPERTAION_QIANG_ZHUANG) {
            player_op_qiangZhuang(gameData, desk, msg, pl);
        }
        //下注
        else if ((msg.getAction() & PokerConstants.NN_OPERTAION_XIA_ZHU) == PokerConstants.NN_OPERTAION_XIA_ZHU) {
            player_op_xiaZhu(gameData, desk, msg, pl);
        }
        //搓牌
        else if ((msg.getAction() & PokerConstants.NN_OPERTAION_CUO_PAI) == PokerConstants.NN_OPERTAION_CUO_PAI) {
            player_op_cuoPai(gameData, desk, msg, pl);
        }
        //看牌
        else if ((msg.getAction() & PokerConstants.NN_OPERTAION_SEE_CARD) == PokerConstants.NN_OPERTAION_SEE_CARD) {
            player_op_seeCard(gameData, desk, msg, pl);
        }
        else
            throw new RuntimeException("UnKnowOperation;");
    }

    private void player_op_qiangZhuang(GameData gameData, MJDesk<byte[]> desk, NNGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是抢庄状态========");
            return;
        }
        if(pl.nnRobotNum != PokerConstants.NN_MEI_QIANG_ZHUANG) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========已经抢庄了========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========抢庄========");

        //游戏数据
        pl.nnRobotNum = msg.getQiangZhuangNum();
        //取消超时
        gameData.mPlayerAction[pl.position].opStartTime = 0L;
        //发送消息
        NNPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_ALL);
        //判断是否结束抢庄状态
        checkQiangZhuangOver(gameData,desk);
        //回放
        gameData.recorder.recordNNPlayerAction(gameData.genSeq(),pl.position,msg.getAction(),pl.nnRobotNum,-1);
        
    }

    private void checkQiangZhuangOver(GameData gameData, MJDesk<byte[]> desk) {
        boolean isOver = true;
        Map<Integer,List<PlayerInfo>> map = new HashMap<>();
        int max = -2;
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p.nnRobotNum == PokerConstants.NN_MEI_QIANG_ZHUANG) {
                isOver = false;
                break;
            }else{
                if(max < p.nnRobotNum) max = p.nnRobotNum;
                if(!map.containsKey(p.nnRobotNum)){
                    map.put(p.nnRobotNum,new ArrayList<>());
                }
                map.get(p.nnRobotNum).add(p);
            }
        }
        if(isOver){
            //随即产生庄家
            List<PlayerInfo> playerInfoList = map.get(max);
            Collections.shuffle(playerInfoList);
            PlayerInfo banker = playerInfoList.get(0);
            
            //游戏数据
            gameData.currentRobIndex = banker.position;
            gameData.mPublic.mbankerPos = banker.position;
            gameData.mPublic.mBankerUserId = banker.playerId;
            gameData.qiangZhuangNum = banker.nnRobotNum;
            gameData.setPokerOpPlayerIndex(banker.position);

            log.info("桌子id--"+desk.getDeskID()+"--"+"确定庄家为--"+banker.name+"--座位号-"+banker.position+"--id--"+banker.playerId);

            //发送消息
            NN.NNGameOperPlayerActionSyn.Builder msg = NN.NNGameOperPlayerActionSyn.newBuilder();
            msg.setAction(PokerConstants.NN_OPERTAION_CONFIRM_BANKER);
            msg.setPosition(banker.position);
            msg.setQiangZhuangNum(banker.nnRobotNum);
            //推送消息
            NNPokerPushHelper.pushActionSyn(desk, -1, msg, PokerConstants.SEND_TYPE_ALL);
            // 回放
            gameData.recorder.recordNNPlayerAction(gameData.genSeq(),banker.position,msg.getAction(),gameData.qiangZhuangNum,-1);
            //切换游戏状态
            resetNextPlayerOperation(gameData,desk,PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU);
        }
    }

    private void player_op_xiaZhu(GameData gameData, MJDesk<byte[]> desk, NNGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是下注状态========");
            return;
        }
        
        if(pl.chouMa != 0) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========已经下注了========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========下注========");

        //游戏数据
        pl.chouMa = msg.getXiaZhuNum();
        //取消超时
        gameData.mPlayerAction[pl.position].opStartTime = 0L;
        //发送消息
        NNPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_ALL);
        //判断是否结束抢庄状态
        checkXiaZhuOver(gameData,desk);
        // 回放
        gameData.recorder.recordNNPlayerAction(gameData.genSeq(),pl.position,msg.getAction(),-1,pl.chouMa);
    }

    private void checkXiaZhuOver(GameData gameData, MJDesk<byte[]> desk) {
        boolean isOver = true;
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(p.position == gameData.currentRobIndex) continue;
            if(p.chouMa == 0) {
                isOver = false;
                break;
            }
        }
        if(isOver){
            //切换游戏状态
            resetNextPlayerOperation(gameData,desk,PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
        }
    }

    private void player_op_cuoPai(GameData gameData, MJDesk<byte[]> desk, NNGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是出牌状态不能搓牌========");
            return;
        }
        if(pl.isKanPai) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========已经看牌了不能搓牌========");
            return;
        }

        //发送消息
        msg.addAllHandcard(NNProcessor.byte2IntList(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSortAndFenCha));
        msg.setCardType(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardTypeStr);
        NNPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_SINGLE);
        //回放
        gameData.recorder.recordNNPlayerAction(gameData.genSeq(),pl.position,msg.getAction(),-1,-1);
    }

    private void player_op_seeCard(GameData gameData, MJDesk<byte[]> desk, NNGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是出牌状态不能看牌========");
            return;
        }

        if(pl.isKanPai) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========已经看牌了不能看牌========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家"+pl.name+"========看牌========");

        //游戏数据
        pl.isKanPai = true;
        //取消超时
        gameData.mPlayerAction[pl.position].opStartTime = 0L;
        //发送消息
        msg.addAllHandcard(NNProcessor.byte2IntList(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSortAndFenCha));
        msg.setCardType(gameData.mPlayerCards[pl.position].nnBiPaiResult.cardTypeStr);
        NNPokerPushHelper.pushActionSyn(desk,pl.position,msg,PokerConstants.SEND_TYPE_ALL);
        //判断是否结束抢庄状态
        checkGameOver(gameData,desk);
        //回放
        gameData.recorder.recordNNPlayerAction(gameData.genSeq(),pl.position,msg.getAction(),-1,-1);
    }

    private void resetNextPlayerOperation(GameData gameData, MJDesk<byte[]> desk ,int Substate) {
        // 等待客户端播动画
        gameData.setWaitingStartTime(System.currentTimeMillis());
        gameData.setPlaySubstate(Substate);
    }

    private void checkGameOver(GameData gameData,MJDesk<byte[]> desk) {
        boolean isOver = true;
        for(PlayerInfo p : desk.getPlayingPlayers()){
            if(!p.isKanPai) {
                isOver = false;
                break;
            }
        }
        if(!isOver) return;

        PlayerInfo winPlayer = biPai(gameData,desk);

        log.info("桌子id--"+desk.getDeskID()+"--"+"--打牌结束,赢牌人--" + winPlayer.name);

        NN.NNGameOperPlayerActionNotify.Builder notify = NN.NNGameOperPlayerActionNotify.newBuilder();
        notify.setPosition(winPlayer.position);
        notify.setActions(PokerConstants.POKER_OPERTAION_GAME_OVER);
        //消息推送
        NNPokerPushHelper.pushActionNofity(gameData,desk,0,notify, PokerConstants.SEND_TYPE_ALL);

        gameData.recorder.recordNNPlayerAction(gameData.genSeq(),-1,notify.getActions(),-1,-1);

        //设置胡牌人信息
        gameData.mGameWin.position = winPlayer.position;
        gameData.handEndTime = System.currentTimeMillis();

        // 结算番型和金币
        settlement(gameData, desk, winPlayer);

        //设置下一个庄家的下标
        gameData.robIndex = gameData.currentRobIndex;
        if(desk.canNiuNiuShangZhuang()){
            List<PlayerInfo> niuniuPlayers = new ArrayList<>();
            for(PlayerInfo p : desk.getPlayingPlayers()){
                if(gameData.mPlayerCards[p.position].nnBiPaiResult.cardType == 10) niuniuPlayers.add(p);
            }
            if(!niuniuPlayers.isEmpty()) {
                Collections.shuffle(niuniuPlayers);
                gameData.robIndex = niuniuPlayers.get(0).position;
            }
        }else if(desk.canLunLiuZhuang()){
            List<PlayerInfo> nextPlayer = desk.loopGetPlayerNN(gameData.currentRobIndex, 1, 0);
            gameData.robIndex = nextPlayer.get(0).position;
        }
        gameData.currentRobIndex = -1;

        //设置一局结束的状态,循环获取状态后结束这局游戏
        gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);
    }

    //当前无庄家(通比牛牛)时返回一个赢家,其余时候传null
    private PlayerInfo biPai(GameData gameData, MJDesk<byte[]> desk) {
        List<PlayerInfo> list = desk.getPlayingPlayers();
        if(desk.canTongBiNiuNiu()){
            PlayerInfo winner = list.get(0);
            for (int i = 0; i < list.size()-1; i++) {
                for (int j = i; j < list.size() - i -1; j++) {
                    NNBiPaiResult biPai1 = gameData.mPlayerCards[list.get(j).position].nnBiPaiResult;
                    NNBiPaiResult biPai2 = gameData.mPlayerCards[list.get(j+1).position].nnBiPaiResult;
                    int result = processor.biPai(biPai1,biPai2);
                    if(result > 0) {
                        winner = list.get(j+1);
                    }
                }
            }
            for(PlayerInfo p : list){
                if(p.position != winner.position){
                    p.yanPaiResult = PokerConstants.GAME_RESULT_LOSE;
                }else{
                    p.yanPaiResult = PokerConstants.GAME_RESULT_WIN;
                }
            }
            return winner;
        }else{
            for(PlayerInfo p : list){
                if(p.position != gameData.currentRobIndex){
                    int result = processor.biPai(gameData.mPlayerCards[p.position].nnBiPaiResult,gameData.mPlayerCards[gameData.currentRobIndex].nnBiPaiResult);
                    p.yanPaiResult = result > 0 ? PokerConstants.GAME_RESULT_LOSE: PokerConstants.GAME_RESULT_WIN;
                    log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+p.name+"--和庄家比牌--"+(p.yanPaiResult == PokerConstants.GAME_RESULT_LOSE ? "输":"赢"));
                }
            }
            return new PlayerInfo();
        }
    }

    /**
     *  计算喜钱
     *  赢的人拿总注
     */
    private void settlement(GameData gameData, MJDesk<byte[]> desk, PlayerInfo winPlayer) {
        gameData.nnResult.endTime = System.currentTimeMillis();
        gameData.nnResult.juNum = gameData.handNum;
        gameData.nnResult.winnerIndex = winPlayer.position;

        List<PlayerInfo> playerList = desk.getPlayingPlayers();
        if(desk.canTongBiNiuNiu()){
            int allScore = 0;
            for (PlayerInfo p : playerList) {
                PokerNNResult nnResult = gameData.nnResult.Result.get(p.playerId);
                PokerNNFinalResult finalResult = gameData.nnFinalResult.finalResults.get(p.playerId);
                
                nnResult.pos = p.position;
                nnResult.playerId = p.playerId;
                nnResult.playerName = p.name;
                nnResult.lastScore = nnResult.allScore;
                nnResult.isBanker = false;
                nnResult.cardType = gameData.mPlayerCards[p.position].nnBiPaiResult.cardTypeStr;
                
                finalResult.pos = p.position;
                finalResult.playerId = p.playerId;
                finalResult.playerName = p.name;
                finalResult.headImg = p.headImg;
                if(p.position != winPlayer.position) {
                    nnResult.score = -gameData.mPlayerCards[winPlayer.position].nnBiPaiResult.cardMultiple * p.chouMa;
                    if((nnResult.score + nnResult.allScore < 0) && desk.isClubJiFenDesk() && desk.getCanFufen() == 1){
                        nnResult.score = -nnResult.allScore;
                    }
                    nnResult.allScore += nnResult.score;
                    nnResult.result = PokerConstants.GAME_RESULT_LOSE;
                    
                    allScore -= nnResult.score;
                    
                    finalResult.allScore = nnResult.allScore;
                    finalResult.loseNum++;
                    finalResult.score += nnResult.score;
                    
                    p.curJuScore = nnResult.score;
                }
            }
            PokerNNResult nnResult = gameData.nnResult.Result.get(winPlayer.playerId);
            PokerNNFinalResult finalResult = gameData.nnFinalResult.finalResults.get(winPlayer.playerId);
            nnResult.score = allScore;
            nnResult.allScore += nnResult.score;
            nnResult.result = PokerConstants.GAME_RESULT_WIN;
            
            finalResult.allScore = nnResult.allScore;
            finalResult.winNum++;
            finalResult.score += nnResult.score;
            
            winPlayer.curJuScore = nnResult.score;
        }else {
            int total = 0;
            for (PlayerInfo p : playerList) {
                PokerNNResult nnResult = gameData.nnResult.Result.get(p.playerId);
                PokerNNFinalResult finalResult = gameData.nnFinalResult.finalResults.get(p.playerId);

                nnResult.pos = p.position;
                nnResult.playerId = p.playerId;
                nnResult.playerName = p.name;
                nnResult.lastScore = nnResult.allScore;
                nnResult.isBanker = p.position == gameData.currentRobIndex;
                nnResult.cardType = gameData.mPlayerCards[p.position].nnBiPaiResult.cardTypeStr;
                nnResult.result = p.yanPaiResult;
                
                finalResult.pos = p.position;
                finalResult.playerId = p.playerId;
                finalResult.playerName = p.name;
                finalResult.headImg = p.headImg;
                
                if(p.position == gameData.currentRobIndex) continue;
                
                int niuFan = p.yanPaiResult ==PokerConstants.GAME_RESULT_WIN ? 
                        gameData.mPlayerCards[p.position].nnBiPaiResult.cardMultiple :
                        -gameData.mPlayerCards[gameData.currentRobIndex].nnBiPaiResult.cardMultiple;
                nnResult.score = niuFan * p.chouMa * (desk.canMingPaiQiangZhuang() ? Math.abs(gameData.qiangZhuangNum) : 1);
                if((nnResult.score + nnResult.allScore < 0) && desk.isClubJiFenDesk() && desk.getCanFufen() == 1){
                    nnResult.score = -nnResult.allScore;
                }
                nnResult.allScore += nnResult.score;
                
                finalResult.allScore = nnResult.allScore;
                finalResult.score += nnResult.score;
                finalResult.winNum += p.yanPaiResult ==PokerConstants.GAME_RESULT_WIN ? 1:0;
                finalResult.loseNum += p.yanPaiResult ==PokerConstants.GAME_RESULT_LOSE ? 1:0;
                
                p.curJuScore = nnResult.score ;

                total -= nnResult.score;
            }

            PokerNNResult nnResult = gameData.nnResult.Result.get(gameData.mPublic.mBankerUserId);
            PokerNNFinalResult finalResult = gameData.nnFinalResult.finalResults.get(gameData.mPublic.mBankerUserId);
            nnResult.score = total;
            
            if(desk.isClubJiFenDesk() && nnResult.score+nnResult.allScore < 0 && desk.getCanFufen()==1){
                nnResult.score = -nnResult.allScore;
                List<PlayerInfo> list = desk.loopGetPlayerNN(gameData.currentRobIndex, desk.getPlayingPlayers().size() - 1, 0);
                
                //庄家桌面的钱加上 桌子上输家的输的前作为分配的钱(分配给赢家)
                int allScore = nnResult.allScore;
                for (PlayerInfo pl : list) {
                    if (pl == null) continue;
                    if (pl.yanPaiResult == PokerConstants.GAME_RESULT_LOSE) {
                        allScore -= pl.curJuScore;
                    }
                }

                //赢家的钱重新分配
                for (PlayerInfo pl : list) {
                    if (pl == null || pl.yanPaiResult == PokerConstants.GAME_RESULT_LOSE) continue;
                    PokerNNResult res = gameData.nnResult.Result.get(pl.playerId);
                    PokerNNFinalResult finalRes = gameData.nnFinalResult.finalResults.get(pl.playerId);

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
            nnResult.allScore += nnResult.score;
            
            finalResult.score += nnResult.score;
            finalResult.allScore = nnResult.allScore;
            finalResult.loseNum += (total < 0 ? 1: 0);
            finalResult.winNum += (total > 0 ? 1: 0);
        }
        log.error("----------------------------------------------");
        for(PokerNNFinalResult f : gameData.nnFinalResult.finalResults.values()){
            log.error("玩家FinalResult--"+f.playerName+"--allsocre:"+f.allScore);
        }
        for(PokerNNResult f : gameData.nnResult.Result.values()){
            log.error("玩家Result--"+f.playerName+"--allsocre:"+f.allScore);
            log.error("玩家Result--"+f.playerName+"--socre:"+f.score);
        }
        for(PlayerInfo pls : desk.getPlayingPlayers()){
            log.error("玩家--"+pls.name+"--socre:"+pls.score);
            log.error("玩家--"+pls.name+"--cursocre:"+pls.curJuScore);
        }
    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info) {

    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"act=repushGameData; position={};deskId={};", position, desk.getDeskID());
        //把当前桌子的状况发给重连玩家
        NN.NNGameOperStartSyn.Builder nn = NN.NNGameOperStartSyn.newBuilder();
        nn.setJuNum(gameData.handNum);//桌子当前圈数
        nn.setBankerPos(gameData.currentRobIndex);//当前地主的下标
        nn.setReconnect(true);//表示断线重连
        nn.setSeq(gameData.gameSeq);
        if(desk.canMingPaiQiangZhuang()) nn.setQiangZhuangNum(gameData.qiangZhuangNum);
        gameData.recorder.seq = nn.getSeq(); // 记录序列号

        PlayerInfo p = desk.getDeskPlayer(position);
        for (PlayerInfo pl : desk.getAllPlayers()) {
            NN.NNGameOperHandCardSyn.Builder dz = NN.NNGameOperHandCardSyn.newBuilder();
            // 发给玩家的牌
            if(gameData.fuliPlayerMap.keySet().contains(p.playerId)) {
                //福利玩家可以看
                if(pl.position >= 0) {
                    for (int card : gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSortAndFenCha) {
                        dz.addHandCards(card);
                    }
                }
            }else{
                //非福利玩家
                if(!p.isWait) {
                    if(pl.position >= 0) {
                        for (int i = 0; i < gameData.getCardsInHand(pl.position).size(); i++) {
                            int card = gameData.getCardsInHand(pl.position).get(i);
                            if (desk.canMingPaiQiangZhuang() && i <= 3 && p.position == pl.position) {
                                dz.addHandCards(card);
                            } else {
                                dz.addHandCards(-1);
                            }
                        }
                    }
                }
            }
            if(gameData.nnFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.nnFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            dz.setPosition(pl.position);
            dz.setIsWait(pl.isWait);
            if(desk.canMingPaiQiangZhuang() && gameData.currentRobIndex != -1){
                dz.setXiazhuNum(p.chouMa);
            }
            nn.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.NNGameOperStartSyn);
        gb.setContent(nn.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(nn.build()));

        desk.sendMsg2Player(p.position, gb.build().toByteArray());

        // 发送当前操作人
        NNPokerPushHelper.pushActorSyn(desk, position, position, gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD? 5:10, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, position);
    }


    @Override
    public void pushDeskInfo(GameData mGameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子信息给新来玩家");

        //把当前桌子的状况发给重连玩家
        NN.NNGameOperStartSyn.Builder nn = NN.NNGameOperStartSyn.newBuilder();
        nn.setJuNum(gameData.handNum);//桌子当前圈数
        nn.setBankerPos(gameData.currentRobIndex);//当前地主的下标
        if(desk.canMingPaiQiangZhuang()) nn.setQiangZhuangNum(gameData.qiangZhuangNum);
        nn.setReconnect(true);//表示断线重连
        nn.setSeq(gameData.gameSeq);
        gameData.recorder.seq = nn.getSeq(); // 记录序列号

        for (PlayerInfo pl : desk.getPlayers()) {
            NN.NNGameOperHandCardSyn.Builder dz = NN.NNGameOperHandCardSyn.newBuilder();
            if(gameData.nnFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.nnFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            if(gameData.fuliPlayerMap.keySet().contains(p.playerId)) {
                //福利玩家可以看
                if(pl.position >= 0) {
                    for (int card : gameData.mPlayerCards[pl.position].nnBiPaiResult.cardsSortAndFenCha) {
                        dz.addHandCards(card);
                    }
                }
            }else{
                //非福利玩家
//                if(!p.isWait) {
                    if(pl.position >= 0) {
                        for (int i = 0; i < gameData.getCardsInHand(pl.position).size(); i++) {
                            int card = gameData.getCardsInHand(pl.position).get(i);
                            if (desk.canMingPaiQiangZhuang() && i <= 3 && p.position == pl.position) {
                                dz.addHandCards(card);
                            } else {
                                dz.addHandCards(-1);
                            }
                        }
                    }
//                }
            }
            dz.setPosition(pl.position);
            dz.setIsWait(pl.isWait);
            if(desk.canMingPaiQiangZhuang() && gameData.currentRobIndex != -1){
                dz.setXiazhuNum(p.chouMa);
            }
            nn.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.NNGameOperStartSyn);
        gb.setContent(nn.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(nn.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());
    }

    /**
     * 重新通知玩家操作
     *
     */
    @Override
    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, int position) {
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER) {
            PlayerInfo p = desk.getDeskPlayer(position);
            if(p.nnRobotNum == PokerConstants.NN_MEI_QIANG_ZHUANG && !p.isZanLi && !p.isWait) {
                log.info("桌子id--" + desk.getDeskID() + "--" + "提醒玩家重新抢地主");
                single_player_robot_notify(desk, p);
            }
        }else if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_XIA_ZHU){
            PlayerInfo p = desk.getDeskPlayer(position);
            if(!p.isXiaZhu && !p.isZanLi && !p.isWait){
                log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新下注");
                single_player_xiazhu_notify(gameData,desk,p);
            }
        }else if (gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD) {
            //提醒玩家重新出
            PlayerInfo p = desk.getDeskPlayer(position);
            if(!p.isKanPai && !p.isZanLi && !p.isWait) {
                log.info("桌子id--" + desk.getDeskID() + "--" + "提醒玩家重新出");
                single_player_chu_notify(gameData, desk, p);
            }
        }
    }

    /**
     * 设置下一把庄家
     * 默认是开房间的人,开房间的人的座位号为0
     */
    @Override
    public void selectBanker(GameData data, MJDesk<byte[]> desk) {
        if(!processor.canQiangZhuang(desk)) {
            if(desk.canTongBiNiuNiu()){
                gameData.currentRobIndex = -1;
            }else {
                if (data.handNum == 0) {
                    List<PlayerInfo> playerList = desk.getPlayingPlayers();
                    Collections.shuffle(playerList);
                    gameData.currentRobIndex =playerList.get(0).position;
                } else {
                    gameData.currentRobIndex = gameData.robIndex;
                }
                PlayerInfo playerInfo = desk.getDeskPlayer(gameData.currentRobIndex);
                data.mPublic.mbankerPos = gameData.currentRobIndex;
                data.mPublic.mBankerUserId = playerInfo.playerId;
            }
        }
    }

    @Override
    public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk) {

    }

    @Override
    public void notifyDouble(GameData gameData, MJDesk<byte[]> desk) {

    }

    @Override
    public void playerAutoOper(GameData gameData, MJDesk<byte[]> gt, int position) {

    }

    @Override
    public void handleSetGamingData(GameCardDealer mCardDealer, GameData gameData, MJDesk<byte[]> desk, String json) {

    }

    @Override
    public void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {

    }
}
