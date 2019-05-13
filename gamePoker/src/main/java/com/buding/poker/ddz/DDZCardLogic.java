package com.buding.poker.ddz;

import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.PlayerInfo;
import com.buding.card.ICardLogic;
import com.buding.game.CardChangeReason;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.poker.common.DDZRule;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.DDZHelper;
import com.buding.poker.helper.DDZPokerPushHelper;
import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.ddz.DDZ;
import packet.ddz.DDZ.*;
import packet.mj.MJBase;
import packet.mj.MJBase.GameOperType;
import packet.mj.MJBase.GameOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDZCardLogic implements ICardLogic<MJDesk<byte[]>> {
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
//            case PokerConstants.POKER_TABLE_SUB_STATE_PAUSE: {
//                return;
//            }
            case PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS: {
                if (ctt - gameData.getWaitingStartTime() > gameData.mGameParam.sendCardPlayMills) {
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
                }
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER: {
                //通知玩家抢地主
                notifyRobotBanker(data, desk);
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE: {
                //通知玩家是否加倍
                if (ctt - gameData.getWaitingStartTime() > gameData.mGameParam.changeBaoMills){
                    notifyDouble(data, desk);
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                    gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE;
                }
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD: {
//                if(currentPl.position != gameData.currentRobIndex){
//                    this.playerAutoOper(gameData,desk,currentPl.position);
//                }else {
                    //提醒玩家出牌
                    player_chu_notify(data, desk);
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
//                }
                //设置当前桌子操作(用于断线回来做判断)
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD;
            }
            break;

            case PokerConstants.POKER_TABLE_SUB_STATE_NONE: {
                //TODO 记得解开
                //超时处理,斗地主只有在玩家只有过选项时,自动处理过
                if(gameData.mPlayerAction[currentPl.position] == null){
                    log.error("桌子ID---"+this.desk.getDeskID()+"--为null"+gameData.mPlayerAction[currentPl.position]);
                    return;
                }
                long time = gameData.mPlayerAction[currentPl.position].opStartTime;
                boolean isTimeout = (ctt - time) > (gameData.mGameParam.operTimeOutSeconds * 1000);
                if (gameData.canAutoOper && isTimeout && time != 0) playerAutoOper(data, desk, data.getPokerOpPlayerIndex());
            }
            break;
        }
    }

    public List<Byte> chineseName2CardList(String name){
        String[] cardListChinese = name.split(" ");
        List<Byte> cardList = new ArrayList<>();
        for(String card : cardListChinese){
            cardList.add(DDZHelper.singleCardMapChinese.get(card));
        }
        return cardList;
    }

    /**
     *  发 牌
     *
     * @param gameData
     * @param desk
     */
    @Override
    public void gameStart(GameData gameData, MJDesk<byte[]> desk) {
        //清楚玩家当前的某些状态(上一局的倍数,分数)
        cleanPlayerInfo(gameData,desk);

        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null)
                continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            cl.clear();
            List<Byte> src = new ArrayList<Byte>();

            boolean isBanker = pl.playerId == gameData.mPublic.mBankerUserId;

            List<Integer> initCards = desk.getDebugData(pl.position);
            for (int card : initCards) {
                Byte c = (byte) (card & 0xff);
                boolean ok = gameData.mDeskCard.ddzCards.remove(c);
                if (ok) {
                    src.add(c);
                }
                if (src.size() >= 17) {
                    break;
                }
            }
            boolean faPai = false; //false 随机发牌  true 自定义发牌
            if(faPai){
                StringBuffer bu = new StringBuffer();

                gameData.mDeskCard.ddzCards = chineseName2CardList("小王 黑Q 黑6");

                if(isBanker){
                    bu.append("梅3 ");

                    bu.append("黑2 ");
                    bu.append("梅2 ");
                    bu.append("梅K ");
                    bu.append("红K ");

                    bu.append("方A ");
                    bu.append("方K ");
                    bu.append("梅J ");
                    bu.append("方9 ");

                    bu.append("梅7 ");
                    bu.append("红7 ");
                    bu.append("方7 ");
                    bu.append("红6 ");

                    bu.append("梅6 ");
                    bu.append("方6 ");
                    bu.append("方4 ");
                    bu.append("梅4");
                    src.addAll(chineseName2CardList(bu.toString()));

                }else{
                    if(pl.position == (gameData.mPublic.mbankerPos+1)%2) {
                        bu.append("红2 ");

                        bu.append("方2 ");
                        bu.append("黑A ");
                        bu.append("红A ");
                        bu.append("黑J ");

                        bu.append("方J ");
                        bu.append("红J ");
                        bu.append("黑10 ");
                        bu.append("梅10 ");

                        bu.append("红10 ");
                        bu.append("红8 ");
                        bu.append("方8 ");
                        bu.append("黑6 ");

                        bu.append("方5 ");
                        bu.append("黑5 ");
                        bu.append("方3 ");
                        bu.append("黑3");
                        src.addAll(chineseName2CardList(bu.toString()));

                    }
                    else if(pl.position == (gameData.mPublic.mbankerPos+2)%3){

                        bu.append("黑K ");

                        bu.append("大王 ");
                        bu.append("梅A ");
                        bu.append("方Q ");
                        bu.append("梅Q ");

                        bu.append("梅5 ");
                        bu.append("红Q ");
                        bu.append("黑9 ");
                        bu.append("红9 ");

                        bu.append("梅9 ");
                        bu.append("黑8 ");
                        bu.append("梅8 ");
                        bu.append("黑7 ");

                        bu.append("红5 ");
                        bu.append("黑4 ");
                        bu.append("红4 ");
                        bu.append("红3");
                        src.addAll(chineseName2CardList(bu.toString()));
                    }
                }
            }else{
                for (int j = src.size(); j < 17; j++) {
                    Byte b = gameData.popCard();
                    src.add(b);
                }
            }
            // 排个序
            cl.addAll(DDZProcessor.sortHandCards(src));
        }
        for (int i = 0; i < 3; i++) {
            Byte b = gameData.popCard();
            gameData.mDeskCard.ddzCards.add(b);
        }

        gameData.gameSeq = (int) (System.nanoTime() % 10000);

        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null)
                continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            gameData.recorder.recordDDZPlayerCard(cl , pl.position);
        }
        gameData.recorder.recordDDZGameStart(gameData.mPlayers , gameData.mDeskCard.ddzCards);
        gameData.recorder.recordBasicInfo(gameData);
        // 把牌下发给客户端
        DDZGameOperStartSyn.Builder msg = DDZGameOperStartSyn.newBuilder();

        int handNum = gameData.handNum;
        msg.setQuanNum(handNum);// 当前局数
        msg.setServiceGold((int) desk.getFee());// 本局服务费
        msg.setSeq(gameData.gameSeq);
        msg.setMultiple(1);

        gameData.recorder.seq = msg.getSeq(); // 记录序列号
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null) continue;

            List<Byte> cl = gameData.getCardsInHand(pl.position);
            log.info("act=initcards;position={};cards={};", pl.position, new Gson().toJson(cl));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--座位号--"+pl.position+"--id--"+pl.playerId);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--累计分数/金币数--"+gameData.ddzResult.Result.get(pl.playerId).score+"--上局倍数--"+gameData.ddzResult.Result.get(pl.playerId).multiple);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--手牌--"+ DDZHelper.getSingleCardListName(gameData.getCardsInHand(pl.position)));
        }

        for (PlayerInfo pl : desk.getAllPlayers()) {
            if (pl == null) continue;
            msg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getPlayers()) {
                boolean showHandCardVal = p.position == pl.position;
                DDZGameOperHandCardSyn.Builder handCardBuilder = DDZGameOperHandCardSyn.newBuilder();
                // 发给玩家的牌
                for (int card : gameData.getCardsInHand(p.position)) {
                    handCardBuilder.addHandCards(showHandCardVal ? card : -1);
                }
                handCardBuilder.setPosition(p.position);// 玩家的桌子位置
                msg.addPlayerHandCards(handCardBuilder);
            }

            GameOperation.Builder gb = GameOperation.newBuilder();
            gb.setOperType(GameOperType.DDZGameOperStartSyn);
            gb.setContent(msg.build().toByteString());
            gb.setType(0);

            desk.sendMsg2Player(pl, gb.build().toByteArray());
        }

        gameData.showInitCardTime = System.currentTimeMillis();
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS);
        gameData.setWaitingStartTime(System.currentTimeMillis());

        log.info("act=onSendCard;seq={};players={}", msg.getSeq(), new Gson().toJson(gameData.mPlayers));

    }


    /**
     * 提示出牌
     *
     * @param gameData
     * @param desk
     */
    @Override
    public void player_chu_notify(GameData gameData, MJDesk<byte[]> desk) {
        
        //获得当前操作的玩家
        PlayerInfo plx = desk.getDeskPlayer(gameData.getPokerOpPlayerIndex());

        if (plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为" + gameData.getPokerOpPlayerIndex());
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"提示出牌 :　" + plx.name);
        if(gameData.cardInDeskMap.get(plx.position) != null) gameData.cardInDeskMap.get(plx.position).clear();
        DDZGameOperPlayerActionNotify.Builder msg = DDZGameOperPlayerActionNotify.newBuilder();
        msg.setPosition(gameData.getPokerOpPlayerIndex());
        msg.setActions(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
        if (gameData.prevCardType == -1 && gameData.prevCards == null || gameData.countNum == 2){
            //第一次出牌 或 出的牌两个人不要了 可以随便出
            if (gameData.unnatural ==1) {
                msg.setCanOut(4);//设置为4说明出的牌不符合类型 重新出牌
                gameData.unnatural = 0;
            } else {
                //判断下能否一次性出完,直接帮他出了
                if(gameData.mPlayerCards[plx.position].cardsInHand.size() == 1){
                    DDZGameOperPlayerActionSyn.Builder autoMsg = DDZGameOperPlayerActionSyn.newBuilder();
                    autoMsg.setCanOut(1);
                    autoMsg.setPosition(plx.position);
                    autoMsg.setAction(PokerConstants.POKER_OPERTAION_CHU);
                    autoMsg.addCardValue(gameData.mPlayerCards[plx.position].cardsInHand.get(0));

                    this.playerOperation(gameData, desk, autoMsg, plx);
                    return;
                }else {
                    msg.setCanOut(1);
                }
            }
            gameData.cardInDeskMap = gameData.initCardInDeskMap();

            DDZ.DDZGameOperPlayerActionSyn.Builder bu = DDZ.DDZGameOperPlayerActionSyn.newBuilder();
            bu.setPosition(gameData.getPokerOpPlayerIndex());
            bu.setAction(PokerConstants.POKER_OPERTAION_CLEAR_CARDS_IN_DESK);
            DDZPokerPushHelper.pushActionSyn(desk,-1,bu, PokerConstants.SEND_TYPE_ALL);

        } else {
            //手牌是否能打过上一家 给予提示
            List<List<Byte>> listCards = DDZProcessor.isOverBigReturnList(gameData.mPlayerCards[gameData.getPokerOpPlayerIndex()].cardsInHand,gameData.prevCards,gameData.prevCardType);
            if (listCards != null && listCards.size() != 0) {
                if (gameData.unnatural ==1) {
                    msg.setCanOut(5);//设置为5说明出的牌不符合类型 重新出牌
                    gameData.unnatural = 0;
                } else {
                    msg.setCanOut(2);
                }
                //可以出牌
                for (List<Byte> cards : listCards) {
                    DDZGameOperPrompt.Builder prompt = DDZGameOperPrompt.newBuilder();
                    for (byte card : cards) {
                        prompt.addCards(card);
                    }
                    msg.addPromptCards(prompt);
                }
            } else {
                //没有牌大过上家不能出牌
                msg.setCanOut(3);
                gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = System.currentTimeMillis();
            }

        }

//=====================================消息推送=====================================
        DDZPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        DDZPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,9, PokerConstants.SEND_TYPE_ALL);
        //同步手牌
        DDZPokerPushHelper.pushHandCardSyn(gameData,desk,plx);
        System.out.println(msg.build().toString());

    }

    /**
     * 自动出牌
     *
     * @param gameData
     * @param gt
     * @param position
     */
    @Override
    public void playerAutoOper(GameData gameData, MJDesk gt, int position) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"-------------玩家自动出牌------------------");
       DDZGameOperPlayerActionSyn.Builder msg = DDZGameOperPlayerActionSyn.newBuilder();

        msg.setPosition(position);

        msg.setAction(PokerConstants.POKER_OPERTAION_CANCEL);
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE){
            msg.setAction(PokerConstants.POKER_OPERTAION_NOT_DOUBLE);
        }
        this.playerOperation(gameData, gt, msg, gameData.mPlayers[position]);
    }


    /**
     * 玩家的操作
     */
    @Override
    public void playerOperation(GameData gameData, MJDesk gt, GeneratedMessage.Builder m, PlayerInfo pl) {
        DDZGameOperPlayerActionSyn.Builder msg = (DDZGameOperPlayerActionSyn.Builder) m;

        if (msg == null || pl == null) return;
        log.info("action:"+msg.getAction());
        gt.setPauseTime(System.currentTimeMillis());

        // 玩家出牌
        if ((msg.getAction() & PokerConstants.POKER_OPERTAION_CHU) == PokerConstants.POKER_OPERTAION_CHU) {
            player_op_chu(gameData, gt, msg, pl);
        }

        // 玩家不出和要不起
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_CANCEL) == PokerConstants.POKER_OPERTAION_CANCEL) {
            player_op_cancel(gameData, gt, msg, pl);
        }

        // 玩家提示(暂时没写)
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_PROMPT) == PokerConstants.POKER_OPERTAION_PROMPT) {
//            player_op_peng(gameData, gt, msg, pl);
        }

        // 玩家抢地主
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_ROBOT_BANKER) == PokerConstants.POKER_OPERTAION_ROBOT_BANKER) {
            player_op_robot(gameData, gt, msg, pl);
        }

        // 玩家不抢
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER) == PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER) {
            player_op_not_robot(gameData, gt, msg, pl);
        }

        // 玩家加倍
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_DOUBLE) == PokerConstants.POKER_OPERTAION_DOUBLE) {
            player_op_double(gameData, gt, msg, pl);
        }

        // 玩家不加倍
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_NOT_DOUBLE) == PokerConstants.POKER_OPERTAION_NOT_DOUBLE) {
            player_op_not_double(gameData, gt, msg, pl);
        }
        // 玩家不叫
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_BU_JIAO_DIZHU) == PokerConstants.POKER_OPERTAION_BU_JIAO_DIZHU) {
            player_op_not_robot(gameData, gt, msg, pl);
        }

        else
            throw new RuntimeException("UnKnowOperation;");
    }


    /**
     * 不抢地主
     *
     * @param gameData
     * @param gt
     * @param msg
     * @param pl
     */
    private void player_op_not_robot(GameData gameData, MJDesk gt, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
//            logger.error("act=player_op_not_robot;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, gt.getDeskID());
            return;
        }

        if (msg.getRobNum() != 0) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========当前玩家不抢地主错误========");
//            logger.error("act=player_op_not_robot;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, gt.getDeskID());
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"== 不抢地主");
        gameData.countNum += 1;//抢地主次数加一
        gameData.mPlayers[index].robNum = 0;

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);


//     =================poker 不抢地主的消息推送================================
        gameData.lastActionPlayerPos = pl.position;
        DDZPokerPushHelper.pushActionSyn(gt,0,msg,PokerConstants.SEND_TYPE_ALL);


        //判断是否可以可以结束抢地主(置换为下一个状态值)
        checkRobIsOver(gameData,gt,msg,pl);

    }
    /**
     * 抢地主
     *
     * @param gameData
     * @param gt
     * @param msg
     * @param pl
     */
    private void player_op_robot(GameData gameData, MJDesk gt, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
//            logger.error("act=player_op_robot;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, gt.getDeskID());
            return;
        }

        if (msg.getRobNum() == 0) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========当前玩家抢地主错误========");
//            logger.error("act=player_op_robot;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, gt.getDeskID());
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"== 抢地主");
        //抢地主参数
        int  num = msg.getRobNum();

        if (gameData.currentRobIndex == -1) {
            gameData.currentRobIndex = pl.position;
            gameData.bottomFraction = num;
        } else if(num > gameData.mPlayers[gameData.currentRobIndex].robNum) {
            gameData.currentRobIndex = pl.position;
            gameData.bottomFraction = num;
        } else {
            log.error("act=player_op_robot;error=notcurrentoperation;expect={};actual={};position={};deskId={};desc=抢地主参数错误;", index, pl.position, pl.position, desk.getDeskID());
            return;
        }

        pl.robNum = num ;//设置抢地主的分数
        gameData.countNum++;//抢地主次数加一

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);


        //=================poker 抢地主的消息推送================================
        gameData.lastActionPlayerPos = pl.position;
        DDZPokerPushHelper.pushActionSyn(gt,0,msg,PokerConstants.SEND_TYPE_ALL);


        //判断是否可以可以结束抢地主(置换为下一个状态值)
        checkRobIsOver(gameData,gt,msg,pl);
    }

    /**
     *
     * 判断是否可以结束抢地主
     *
     * @param gameData
     * @param desk
     * @param msg
     * @param pl
     */
    private  void checkRobIsOver (GameData gameData, MJDesk<byte[]> desk ,DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        
        DDZGameOperPlayerActionSyn.Builder operMsg = DDZGameOperPlayerActionSyn.newBuilder();
        if (gameData.countNum == 3 && gameData.currentRobIndex == -1){//三个人多不抢
            log.info("桌子id--"+desk.getDeskID()+"--"+"没有人抢地主 ___  重新发牌");
            //设置黄桩参数 如果=1 下次发牌就不用重新设置谁先抢地主
            gameData.yellowPile = 1;
            //先调结算
            settlement(gameData,desk,pl);
            //在设置状态值
            gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);
            return;
        }
        if (gameData.currentRobIndex < 0) {
            resetNextPlayerOperation(gameData , desk , PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
            return;
        }
        if (gameData.mPlayers[gameData.currentRobIndex].robNum == 3 || gameData.countNum == 3) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"有人喊了三分 或者已经喊了三次 (把底牌三张添加到地主手里去)状态跳转进入加倍阶段");
            //有人喊了三分 或者已经喊了三次 (把底牌三张添加到地主手里去)状态跳转进入加倍阶段
            operMsg.setPosition(gameData.currentRobIndex);
            operMsg.setAction(PokerConstants.POKER_OPERTAION_PLAY_ROBOT);//确认地主是谁..
            operMsg.setRobNum(gameData.bottomFraction);//设置底分
            operMsg.setCardNum(gameData.mPlayerCards[gameData.currentRobIndex].cardsInHand.size());
            List<Integer> list = new ArrayList<>();
            if(!desk.canDouble() || gameData.countNum == 3) {
                gameData.addCardsInHand(gameData.mDeskCard.ddzCards,gameData.currentRobIndex);

                for (Byte card : gameData.mDeskCard.ddzCards) {
                    operMsg.addCardValue(card);//三张底牌
                    list.add((int) card);
                }
            }
            //推送消息给其他玩家
            DDZPokerPushHelper.pushActionSyn(desk,0,operMsg, PokerConstants.SEND_TYPE_ALL);

            //推送合并手牌手牌
            DDZPokerPushHelper.pushHandCardSyn(gameData,desk,pl);

            //回放
            gameData.recorder.bankerPos = gameData.currentRobIndex;
            gameData.recorder.bankerUserId = gameData.mPlayers[gameData.currentRobIndex].playerId;
            gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), gameData.currentRobIndex, PokerConstants.POKER_OPERTAION_PLAY_ROBOT,list,gameData.bottomFraction,-1);


            //===================================确认地主的消息推送==================================

            if (!desk.canDouble() || gameData.countNum == 3) {
                //设置出牌人的下标
                gameData.setPokerOpPlayerIndex(gameData.currentRobIndex);
                gameData.setWaitingStartTime(System.currentTimeMillis());
                gameData.countNum = 2;
                //不加倍(状态跳转出牌)
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
            } else {
                //判断是否可以加倍
                // 顺序，轮到下一个玩家行动(地主的下一个玩家)
                List<PlayerInfo> nextPlayer = desk.loopGetPlayer(gameData.currentRobIndex, 1, 0);
                gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
                gameData.setWaitingStartTime(System.currentTimeMillis());
                //加倍 (状态跳转到加倍) 设置下一个加倍人的下标
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE);
            }
        }else {
            //如果都不是的话找到下一个玩家index 和设置桌子的子状态 (继续抢地主)
            resetNextPlayerOperation(gameData , desk , PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
        }

    }

    /**
     * 不加倍
     *
     * @param gameData
     * @param gt
     * @param msg
     * @param pl
     */
    private void player_op_not_double(GameData gameData, MJDesk gt, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }

        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
//            logger.error("act=player_op_not_double;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, gt.getDeskID());
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name+ "== 不加倍");
        gameData.mPlayers[index].multiple = 1;
        gameData.countNum += 1;


//=====================================消息推送======================================

        gameData.mPlayerAction[pl.position].opStartTime = 0L;

        //判断下一个人是否可以加倍
        if (index == gameData.currentRobIndex || gameData.countNum == 3) {
            //如果当前玩家坐标等于地主玩家坐标说明地主选择完加倍 可以开始跳转出牌状态(设置出牌人是index)
            gameData.setPokerOpPlayerIndex(gameData.currentRobIndex);
            gameData.setWaitingStartTime(System.currentTimeMillis());
            gameData.countNum = 2;
            gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);

            gameData.addCardsInHand(gameData.mDeskCard.ddzCards,gameData.currentRobIndex);
            //告诉前端三张底牌
            for (Byte card : gameData.mDeskCard.ddzCards) {
                msg.addCardValue(card);//三张底牌
            }
        } else {
            //找到下一个继续提示是否加倍
            resetNextPlayerOperation( gameData,  gt , PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE);
        }
        msg.clearRobNum();

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),msg.getCardValueList(),gameData.bottomFraction,-1);
        gameData.lastActionPlayerPos = pl.position;

        DDZPokerPushHelper.pushActionSyn(gt,0,msg, PokerConstants.SEND_TYPE_ALL);

    }

    /**
     * 加倍
     *
     * @param gameData
     * @param gt
     * @param msg
     * @param pl
     */
    private void player_op_double(GameData gameData, MJDesk gt, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }

        int index = msg.getPosition();
        PlayerInfo plx = gameData.mPlayers[index];
        if (index != gameData.getPokerOpPlayerIndex() || plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name + "== 加倍");
        //设置玩家倍数
        plx.multiple = 2;
        gameData.countNum = 0;
        gameData.countNum += 1;

        //=================================消息推送============================================
        gameData.mPlayerAction[pl.position].opStartTime = 0L;

        //判断是否结束加倍环节,跳转出牌阶段
        if (index == gameData.currentRobIndex) {
            //如果当前玩家坐标等于地主玩家坐标说明地主选择完加倍 可以开始跳转出牌状态(设置出牌人是index)
            gameData.setPokerOpPlayerIndex(index);
            gameData.setWaitingStartTime(System.currentTimeMillis());
            gameData.countNum = 2;
            gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);

            gameData.addCardsInHand(gameData.mDeskCard.ddzCards,gameData.currentRobIndex);

            //告诉前端三张底牌
            for (Byte card : gameData.mDeskCard.ddzCards) {
                msg.addCardValue(card);//三张底牌
            }
        }else {
            //找到下一个继续提示是否加倍
            resetNextPlayerOperation( gameData,  gt , PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE);
            //加倍阶段,如果下一个人是地主了,看下上一个农民是不是选择了不加倍

            int lastPos = (index+2)%3;

            if( gameData.getPokerOpPlayerIndex() == gameData.currentRobIndex
                    && lastPos != gameData.currentRobIndex
                    && (lastPos + 1) == index){
                PlayerInfo p = desk.getDeskPlayer(lastPos);
                if(p.multiple == 1){
                    gameData.setPokerOpPlayerIndex(lastPos);
                    gameData.netxDoublePlayerIsDiZhu = true;
                }
            }
        }

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),msg.getCardValueList(),gameData.bottomFraction,-1);

        msg.clearRobNum();
        gameData.lastActionPlayerPos = pl.position;

        DDZPokerPushHelper.pushActionSyn(gt,0,msg,PokerConstants.SEND_TYPE_ALL);
    }


    /**
     * 玩家不出牌
     */
    private void player_op_cancel(GameData gameData, MJDesk gt, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {

        int index = msg.getPosition();
        PlayerInfo plx = gameData.mPlayers[index];
        if (index != gameData.getPokerOpPlayerIndex() || plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        if (msg.getCardValueCount() != 0) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不出牌Error========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name + "== 过");
        gameData.countNum += 1;
        if (gameData.countNum == 2) {
            gameData.prevCardType = 0;
            gameData.prevCards = new ArrayList<>();
        }
        //重置桌子子状态
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0;

        if(gameData.cardInDeskMap.get(pl.position) != null) gameData.cardInDeskMap.get(pl.position).clear();

        for(Map.Entry<Integer,List<Integer>> entry : gameData.cardInDeskMap.entrySet()){
            DDZGameCardInDesk.Builder cardInDesk = DDZGameCardInDesk.newBuilder();
            cardInDesk.setPosition(entry.getKey());
            cardInDesk.addAllCardValue(entry.getValue());
            msg.addCardInDesk(cardInDesk.build());
        }

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);

        //====================================消息推送===========================================
        DDZPokerPushHelper.pushActionSyn(gt,0,msg, PokerConstants.SEND_TYPE_ALL);

        resetNextPlayerOperation(gameData,gt, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    /**
     *
     * 玩家出牌
     *
     * @param gameData
     * @param desk
     * @param msg
     * @param pl
     */
    private void player_op_chu(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        

        int index = msg.getPosition();
        PlayerInfo plx = gameData.mPlayers[index];
        if (index != gameData.getPokerOpPlayerIndex() || plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
//            logger.error("act=player_op_chu;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, desk.getDeskID());
            return;
        }
        if (msg.getCardValueCount() == 0 || msg.getCardValueList().contains(0)) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========出牌Error========"+msg.getCardValueList());
//            logger.error("act=player_op_chu;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, desk.getDeskID());
            return;
        }
        if (!gameData.mPlayerCards[pl.position].cardsInHand.containsAll(DDZProcessor.int2ByteList(msg.getCardValueList()))) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有这张牌不能出========"+"出的牌"+msg.getCardValueList());
//            logger.error("act=player_op_chu;stage=gaming;error=CardNotInHand;position={};deskId={};", pl.position, desk.getDeskID());
            return;
        }

        //获得玩家出的牌
        List<Integer> outCards = msg.getCardValueList();
        List<Byte> cards = new ArrayList<>();
        for (int card : outCards) {
            cards.add((byte)card);
            System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&&&&&  " + card);
        }
        //检测牌型SignAndLunPanSch
        int pokerType = DDZProcessor.getCardType(cards);
//        pokerType = 0x100;
        if (msg.getCanOut() == 1 || msg.getCanOut() == 4) {//非压牌时看牌型,牌型不对不能出
            if (pokerType == 0) {
                //重新推送出牌消息
                gameData.unnatural = 1;
                player_chu_notify(gameData,desk);
                return;
            }
        } else if (msg.getCanOut() == 2 || msg.getCanOut() == 5) {//压牌时看下能不能压
            boolean falg = DDZProcessor.isOvercomePrev(cards,pokerType,gameData.prevCards,gameData.prevCardType);
            if (!falg) {
                //重新推送出牌消息
                gameData.unnatural = 1;
                player_chu_notify(gameData,desk);
                return;
            }
        } else {
            log.info("桌子id--"+desk.getDeskID()+"--"+"======== msg.getCanOut() Error ========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name + "== 出牌" + DDZHelper.getSingleCardListName(cards));
        //往桌子设置属性参数
        //统计地主出牌次数 为反春天做判断
        if (index == gameData.currentRobIndex) {
            gameData.robOutCard += 1;
        }
        gameData.prevCards.clear();
        gameData.prevCards.addAll(cards);
        gameData.prevCardType = pokerType;
        gameData.prevIndex = plx.position;
        gameData.countNum = 0;
        gameData.unnatural = 0;
        //炸弹和王炸底分翻倍
        if (pokerType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            gameData.bottomFraction = gameData.bottomFraction * 2 ;
            gameData.ddzResult.bomb = gameData.ddzResult.bomb + 1;
        }
        if (pokerType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            gameData.bottomFraction = gameData.bottomFraction * 2 ;
            gameData.ddzResult.friedKing = gameData.ddzResult.friedKing + 1;
        }
        gameData.removeCardInHand(cards,index, CardChangeReason.CHU);//移除玩家打出的牌
        //记录打出去的牌
        for (Byte card : cards) {
            gameData.addCardBefore(card,index);
        }
//        gameData.mPlayerCards[index].cardsInHand = DDZProcessor.sortHandCards(gameData.mPlayerCards[index].cardsInHand);

        for (PlayerInfo p : gameData.mPlayers) {
            if(p == null) continue;
//            Collections.sortInShunXu(gameData.mPlayerCards[p.position].cardsInHand);
            log.info("桌子id--"+desk.getDeskID()+"--"+"========玩家===="+p.name+"==牌的状况为:");
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家剩余手牌张数 : " + gameData.getCardNumInHand(p.position));
            log.info("桌子id--"+desk.getDeskID()+"--"+"-----手牌 : " + DDZHelper.getSingleCardListName(gameData.getCardsInHand(p.position)));
            log.info("桌子id--"+desk.getDeskID()+"--"+"---打出去的牌 : " + DDZHelper.getSingleCardListName(gameData.mPlayerCards[p.position].cardsBefore));
        }

        List<Integer> outCardSort = DDZProcessor.sortOutCards(outCards,pokerType);
        if(gameData.cardInDeskMap.get(pl.position) != null) {
            gameData.cardInDeskMap.get(pl.position).clear();
            gameData.cardInDeskMap.get(pl.position).addAll(outCardSort);
        }

        //设置玩家出的牌型
        msg.clearCardValue();
        for(Integer i : outCardSort){
            msg.addCardValue(i);
        }
        msg.setCardType(pokerType);
        msg.setRobNum(gameData.bottomFraction);//设置底分
        msg.setCardNum(gameData.getCardNumInHand(index));//位置玩家剩余几张牌
        for(Map.Entry<Integer,List<Integer>> entry : gameData.cardInDeskMap.entrySet()){
            DDZGameCardInDesk.Builder cardInDesk = DDZGameCardInDesk.newBuilder();
            cardInDesk.setPosition(entry.getKey());
            cardInDesk.addAllCardValue(entry.getValue());
            msg.addCardInDesk(cardInDesk.build());
        }
        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),outCards,gameData.bottomFraction,pokerType);


//====================================消息推送===================================
        DDZPokerPushHelper.pushActionSyn(desk,0,msg, PokerConstants.SEND_TYPE_ALL);

        //同步玩家手牌
        DDZPokerPushHelper.pushHandCardSyn(gameData,desk,plx);

        //当前玩家的手牌为0的话 说明这把游戏已经结束了
        if (gameData.getCardNumInHand(index) == 0) {
            gameData.mPlayerCards[index].cardsInHand = DDZProcessor.int2ByteList(msg.getCardValueList());
            //总结算页面 统计当前这人赢了几句
            gameData.ddzFinalResult.finalResults.get(plx.playerId).winInnings += 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("桌子id--"+desk.getDeskID()+"--"+"--打牌结束,赢牌人--" + plx.name);


            DDZGameOperPlayerActionNotify.Builder notify = DDZGameOperPlayerActionNotify.newBuilder();
            notify.setPosition(plx.position);
            notify.setActions(PokerConstants.POKER_OPERTAION_GAME_OVER);

            //判断是否为春天后者反春
            if (isSpring(gameData,desk,plx)) {
                gameData.spring = gameData.spring * 2;

                gameData.ddzResult.spring = 1;

                if (gameData.currentRobIndex == plx.position) {//春天
                    notify.setSpring(1);
                } else {//反春
                    notify.setSpring(2);
                }
            }

            //把其他两家的牌亮给所有人看
            for (PlayerInfo p :(List<PlayerInfo>)desk.getPlayers()) {
                DDZGameOperOver.Builder over = DDZGameOperOver.newBuilder();
                if (p.playerId != plx.playerId) {
                    over.setPosition(p.position);
                    for (Byte card : gameData.mPlayerCards[p.position].cardsInHand) {
                        over.addCards(card);
                    }
                    notify.addOtherCard(over);
                }
            }

            //回放
            gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, PokerConstants.POKER_OPERTAION_GAME_OVER,new ArrayList<Integer>(),gameData.bottomFraction,-1);

            //消息推送
            DDZPokerPushHelper.pushActionNofity(gameData,desk,0,notify, PokerConstants.SEND_TYPE_ALL);

            //设置胡牌人信息
            gameData.mGameWin.position = index;
            gameData.handEndTime = System.currentTimeMillis();

            // 结算番型和金币
            settlement(gameData, desk, plx);


            //加入到结算方法中(设置下一个抢地主人的下标)
            if (desk.canRoundPile()) {
                List<PlayerInfo> nextPlay = this.desk.loopGetPlayer(gameData.robIndex,1,0);
                gameData.robIndex = nextPlay.get(0).position;
            } else {
                gameData.robIndex = gameData.mGameWin.position;
            }

            //设置一局结束的状态,循环获取状态后结束这局游戏
            gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);

            //清楚玩家当前的某些状态
//            cleanPlayerInfo(gameData,desk);

            return;
        }

        resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }


    /**
     * 判断是否为春天
     *
     * @param gameData
     * @param desk
     * @param pl
     * @return  默认返回是春天
     */
    public boolean isSpring (GameData gameData,MJDesk<byte[]> desk ,PlayerInfo pl) {

        if (pl.position == gameData.currentRobIndex) {
            for (PlayerInfo playerInfo : (List<PlayerInfo>) desk.getPlayers()) {
                if (playerInfo.playerId != pl.playerId) {
                    if (gameData.getCardNumInHand(playerInfo.position) != 17) {
                        return false;
                    }
                }
            }
        } else {
            if (gameData.robOutCard > 1) {
                return false;
            }
        }

        return true;
    }
    /**
     * 斗地主的结算
     *
     * @param gameData
     * @param desk
     * @param pl
     */
    private void settlement (GameData gameData, MJDesk<byte[]> desk, PlayerInfo pl) {
        

        gameData.ddzResult.endTime = System.currentTimeMillis();
        //判断是否为黄桩 (如果黄桩再看是否有比优)


        if (gameData.yellowPile == 1) {
            if (!desk.canYellowPile()){//直接发牌
                for (PlayerInfo px : (List<PlayerInfo>) desk.getPlayers() ) {

                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore;
                }
            } else {//扣除分数最大的玩家
                PlayerInfo playerInfo = thanCard(gameData,desk);//返回手牌最大的那个玩家
                if (playerInfo == null) {
                    log.info("桌子id--"+desk.getDeskID()+"--"+"=================比优返回玩家信息为null====================");
//                    logger.error("act=settlement;stage=gaming;error=invalidCardCount;position={};deskId={};", pl.position, desk.getDeskID());
                    return;
                }
                for (PlayerInfo px : (List<PlayerInfo>) desk.getPlayers() ) {

                    if (px.playerId == playerInfo.playerId) {
                        gameData.ddzResult.Result.get(px.playerId).score = -6;
                        gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore - 6;
                        gameData.ddzFinalResult.finalResults.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore - 6;
                        gameData.ddzFinalResult.finalResults.get(px.playerId).score = gameData.ddzFinalResult.finalResults.get(px.playerId).score - 6;
                    } else {
                        gameData.ddzFinalResult.finalResults.get(px.playerId).winInnings += 1;
                        gameData.ddzResult.Result.get(px.playerId).score = 3;
                        gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + 3;
                        gameData.ddzFinalResult.finalResults.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + 3;
                        gameData.ddzFinalResult.finalResults.get(px.playerId).score = gameData.ddzFinalResult.finalResults.get(px.playerId).score + 3;
                    }

                }
            }
            return;
        }

        //普通结算
        for (PlayerInfo px : desk.getPlayers()) {
           if (px.position != gameData.currentRobIndex) {
                //两农民应该输赢得分
                if (px.multiple == 1) {
                    px.curJuScore = gameData.bottomFraction * px.multiple  * gameData.spring;
                } else {
                    px.curJuScore = gameData.bottomFraction * px.multiple * gameData.mPlayers[gameData.currentRobIndex].multiple * gameData.spring;
                }
                if(px.curJuScore > desk.getLimitMax()){
                    px.curJuScore = desk.getLimitMax();
                }
                //地主的分数应该是两个玩家的分数之和
                gameData.mPlayers[gameData.currentRobIndex].curJuScore = gameData.mPlayers[gameData.currentRobIndex].curJuScore + px.curJuScore;

               System.out.println(px.name + " : " + px.curJuScore + " ---- " + gameData.mPlayers[gameData.currentRobIndex].name + " : " + gameData.mPlayers[gameData.currentRobIndex].curJuScore);
           }
        }

        gameData.ddzResult.innings = gameData.handNum;
        gameData.ddzResult.endPoints = gameData.bottomFraction * gameData.spring;

        if (pl.playerId == gameData.mPlayers[gameData.currentRobIndex].playerId) {
            int diZhuScore = 0;
            //地主赢
            for (PlayerInfo px : desk.getPlayers()) {
                if (px.playerId == pl.playerId) {//是地主


                } else {//农民
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
//                    setSettelAttribute(px);
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.multiple;
                    px.curJuScore = -px.curJuScore;
                    gameData.ddzResult.Result.get(px.playerId).score = px.curJuScore;
                    if(desk.isClubJiFenDesk() && desk.getCanFufen() == 1 &&
                            (gameData.ddzResult.Result.get(px.playerId).score + gameData.ddzFinalResult.finalResults.get(px.playerId).allScore) < 0){
                        gameData.ddzResult.Result.get(px.playerId).score = -gameData.ddzFinalResult.finalResults.get(px.playerId).allScore;
                    }
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + gameData.ddzResult.Result.get(px.playerId).score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore +=  gameData.ddzResult.Result.get(px.playerId).score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score += gameData.ddzResult.Result.get(px.playerId).score;
                    diZhuScore -= gameData.ddzResult.Result.get(px.playerId).score;
                }
            }

            gameData.mPlayers[gameData.currentRobIndex].curJuScore = diZhuScore;
            if (pl.multiple != 1) {
                //是否加倍
                gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).isDouble = 1;
            }
//                    setSettelAttribute(px);
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).isDiZhu = 1;
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).multiple = pl.multiple;
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score = pl.curJuScore;
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore =
                    gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore + pl.curJuScore;
            gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore +=  pl.curJuScore;
            gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score +=  pl.curJuScore;
        } else {//农民赢
            int diZhuScore = 0;
            for (PlayerInfo px : (List<PlayerInfo>) desk.getPlayers()) {
                if (px.playerId == gameData.mPlayers[gameData.currentRobIndex].playerId) {//地主

                } else {
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
//                    setSettelAttribute(px);
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.multiple;
                    gameData.ddzResult.Result.get(px.playerId).score =  px.curJuScore;
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + px.curJuScore;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore +=  px.curJuScore;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score +=  px.curJuScore;
                    diZhuScore -= gameData.ddzResult.Result.get(px.playerId).score;
                }
            }

            gameData.mPlayers[gameData.currentRobIndex].curJuScore = diZhuScore;
            if (gameData.mPlayers[gameData.currentRobIndex].multiple != 1) {
                //是否加倍
                gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).isDouble = 1;
            }
//                    setSettelAttribute(px);
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).isDiZhu = 1;
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).multiple = gameData.mPlayers[gameData.currentRobIndex].multiple;
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score = gameData.mPlayers[gameData.currentRobIndex].curJuScore ;
            if(desk.isClubJiFenDesk() && desk.getCanFufen() == 1 &&
                    (gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score + gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore) < 0){
                gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score = -gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore;
                //分配规则,按顺序分配,没钱了就没钱了
                int allScore = -gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score;
                List<PlayerInfo> list = this.desk.loopGetPlayer(gameData.robIndex,2,0);
                for(PlayerInfo p : list){
                    gameData.ddzFinalResult.finalResults.get(p.playerId).allScore -= gameData.ddzResult.Result.get(p.playerId).score;
                    if(allScore < gameData.ddzResult.Result.get(p.playerId).score){
                        gameData.ddzResult.Result.get(p.playerId).score = allScore <=0 ? 0 : allScore;
                    }
                    gameData.ddzResult.Result.get(p.playerId).allScore =
                            gameData.ddzFinalResult.finalResults.get(p.playerId).allScore + gameData.ddzResult.Result.get(p.playerId).score;
                    gameData.ddzFinalResult.finalResults.get(p.playerId).allScore = gameData.ddzResult.Result.get(p.playerId).allScore;
                    gameData.ddzFinalResult.finalResults.get(p.playerId).score = gameData.ddzResult.Result.get(p.playerId).allScore;

                    allScore -= gameData.ddzResult.Result.get(p.playerId).score;
                }
            }
            gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore =
                    gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore +
                    gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score;
            gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).allScore +=  gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score;
            gameData.ddzFinalResult.finalResults.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score +=  gameData.ddzResult.Result.get(gameData.mPlayers[gameData.currentRobIndex].playerId).score;
        }


        for (PlayerInfo px : (List<PlayerInfo>) desk.getPlayers()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"======" + px.name + " : " + gameData.ddzResult.Result.get(px.playerId).score + " 总分 : " + gameData.ddzFinalResult.finalResults.get(px.playerId).allScore);
        }

    }


    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info) {

    }

    /**
     * 重新推送玩家数据,用于断线重连
     *
     * @param gameData
     * @param desk
     * @param position
     */
    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position) {
        log.info("act=repushGameData; position={};deskId={};", position, desk.getDeskID());

        //把当前桌子的状况发给重连玩家
        DDZGameOperStartSyn.Builder ddz = DDZGameOperStartSyn.newBuilder();

        ddz.setRobNum(gameData.bottomFraction);
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            for(Byte card : gameData.mDeskCard.ddzCards){
                ddz.addDiPai(card);
            }
        }

        for(Map.Entry<Integer,List<Integer>> entry : gameData.cardInDeskMap.entrySet()){
            DDZGameCardInDesk.Builder cardInDesk = DDZGameCardInDesk.newBuilder();
            cardInDesk.setPosition(entry.getKey());
            cardInDesk.addAllCardValue(entry.getValue());
            ddz.addCardInDesk(cardInDesk.build());
        }
        ddz.setMultiple(gameData.bottomFraction);//桌子当前底分
        ddz.setQuanNum(gameData.handNum);//桌子当前圈数
        for (int card : gameData.mDeskCard.ddzCards){
            ddz.addCardLeft(card);//剩下的三张地主牌
        }
        ddz.setServiceGold((int) desk.getFee());// 本局服务费
        ddz.setBankerPos(gameData.currentRobIndex);//当前地主的下标
        ddz.setLastActionPosition(gameData.prevIndex);//出牌玩家的下标
        ddz.setReconnect(true);//表示断线重连
        ddz.setSeq(gameData.gameSeq);
        for (int card : gameData.prevCards) {
            ddz.addLastActionCard(card);//添加玩家打出去的牌
        }

        gameData.recorder.seq = ddz.getSeq(); // 记录序列号

        PlayerInfo p = desk.getDeskPlayer(position);
        for (PlayerInfo pl : (List<PlayerInfo>) desk.getPlayers()) {
            DDZPlayerCard.Builder b = DDZPlayerCard.newBuilder();
            b.setCardNum(gameData.mPlayerCards[pl.position].cardsInHand.size());
            b.setJiaBei(gameData.mPlayers[pl.position].multiple);
            b.setPosition(pl.position);
            ddz.addPlayerCardLeft(b.build());
            boolean showHandCardVal = p.position == pl.position;
            DDZGameOperHandCardSyn.Builder dz = DDZGameOperHandCardSyn.newBuilder();
                for (int card : gameData.getCardsInHand(p.position)) {
                    dz.addHandCards(showHandCardVal ? card : -1);//如果是掉线玩家就给牌 否则是-1
                }

            if(gameData.ddzFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.ddzFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            dz.setCardNum(gameData.getCardNumInHand(pl.position));//设置玩家的手牌
            dz.setPosition(pl.position);
            if (gameData.getPokerOpPlayerIndex() == pl.position){
                dz.setNeedFenCha(1);
            }
            ddz.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.DDZGameOperStartSyn);
        gb.setContent(ddz.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(ddz.build()));

        desk.sendMsg2Player(p.position, gb.build().toByteArray());
        desk.sendMultiMatchRank();
        // 发送公告信息
        DDZPokerPushHelper.pushPublicInfoMsg2Single(desk, position, gameData);

        // 发送当前操作人
        DDZPokerPushHelper.pushActorSyn(desk, position, gameData.getPokerOpPlayerIndex(), 9, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, position);
    }


    @Override
    public void pushDeskInfo(GameData mGameData, MJDesk<byte[]> mDesk, PlayerInfo p) {

        //把当前桌子的状况发给重连玩家
        DDZGameOperStartSyn.Builder ddz = DDZGameOperStartSyn.newBuilder();

        ddz.setRobNum(gameData.bottomFraction);
        if(gameData.currentDeskState == PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            for(Byte card : gameData.mDeskCard.ddzCards){
                ddz.addDiPai(card);
            }
        }

        for(Map.Entry<Integer,List<Integer>> entry : gameData.cardInDeskMap.entrySet()){
            DDZGameCardInDesk.Builder cardInDesk = DDZGameCardInDesk.newBuilder();
            cardInDesk.setPosition(entry.getKey());
            cardInDesk.addAllCardValue(entry.getValue());
            ddz.addCardInDesk(cardInDesk.build());
        }
        ddz.setMultiple(gameData.bottomFraction);//桌子当前底分
        ddz.setQuanNum(gameData.handNum);//桌子当前圈数
        for (int card : gameData.mDeskCard.ddzCards){
            ddz.addCardLeft(card);//剩下的三张地主牌
        }
        ddz.setServiceGold((int) desk.getFee());// 本局服务费
        ddz.setBankerPos(gameData.currentRobIndex);//当前地主的下标
        ddz.setLastActionPosition(gameData.prevIndex);//出牌玩家的下标
        ddz.setReconnect(true);//表示断线重连
        ddz.setSeq(gameData.gameSeq);
        for (int card : gameData.prevCards) {
            ddz.addLastActionCard(card);//添加玩家打出去的牌
        }

        gameData.recorder.seq = ddz.getSeq(); // 记录序列号

        for (PlayerInfo pl : desk.getPlayers()) {
            DDZPlayerCard.Builder b = DDZPlayerCard.newBuilder();
            b.setCardNum(gameData.mPlayerCards[pl.position].cardsInHand.size());
            b.setJiaBei(gameData.mPlayers[pl.position].multiple);
            b.setPosition(pl.position);
            ddz.addPlayerCardLeft(b.build());
            boolean showHandCardVal = p.position == pl.position;
            DDZGameOperHandCardSyn.Builder dz = DDZGameOperHandCardSyn.newBuilder();
            for (int card : gameData.getCardsInHand(pl.position)) {
                dz.addHandCards(showHandCardVal ? card : -1);//如果是掉线玩家就给牌 否则是-1
            }

            if(gameData.ddzFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.ddzFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            dz.setCardNum(gameData.getCardNumInHand(pl.position));//设置玩家的手牌
            dz.setPosition(pl.position);
            if (gameData.getPokerOpPlayerIndex() == pl.position){
                dz.setNeedFenCha(1);
            }
            ddz.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.DDZGameOperStartSyn);
        gb.setContent(ddz.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+ desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(ddz.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());

    }


    /**
     * 重新通知玩家操作
     *
     * @param gameData
     * @param desk
     * @param position
     */
    @Override
    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, int position) {

        if (gameData.getPokerOpPlayerIndex() == position) {
            //获取玩家的子原因状态
            int substate = gameData.currentDeskState;

            switch (substate) {
                case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER:
                    //提醒玩家重新抢地主
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新抢地主");
                    notifyRobotBanker(gameData,desk);
                    break;
                case PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE:
                    //提醒玩家重新加倍
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新加倍");
                    notifyDouble(gameData,desk);
                    break;
                case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD:
                    //提醒玩家重新出
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新出");
                    player_chu_notify(gameData, desk);
                    break;
            }

        }

    }

    /**
     * 设置下一把谁先喊地主
     *
     * @param data
     * @param desk
     */
    @Override
    public void selectBanker(GameData data, MJDesk<byte[]> desk) {
//------------------设置谁先喊地主--------------------------------------------
        if (gameData.yellowPile != 0) {
            //黄庄 不需要重新设置
            return;
        } else {
            gameData.setPokerOpPlayerIndex(gameData.robIndex);
        }

    }

    /**
     * 提示是否可以抢地主
     *
     * @param data
     * @param desk
     */
    @Override
    public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk) {
        
        //获得当前操作的玩家
        PlayerInfo plx = desk.getDeskPlayer(data.getPokerOpPlayerIndex());
        if (plx == null) {
//            logger.error("act=notifyRobotBanker;stage=gaming;error=noSuchPlayer;position={};deskId={};", data.getPokerOpPlayerIndex(), desk.getDeskID());
            log.info("桌子id--"+desk.getDeskID
                    ()+"--"+"未找到该玩家,出牌玩家座位号为"+data.getPokerOpPlayerIndex());
            return;
        }
        DDZGameOperPlayerActionNotify.Builder msg = DDZGameOperPlayerActionNotify.newBuilder();
        msg.setActions(PokerConstants.POKER_OPERTAION_ROBOT_BANKER);//设置action为抢地主\
        msg.setPosition(plx.position);

//        ------------------------判断当前玩家含地主能喊几分,然后给前端推送消息(推送消息暂时没写)----------------------------
        if (DDZRule.isBoomOrFourTwo(data.mPlayerCards[plx.position].cardsInHand)) {
            //如果手里有王炸和四个二必须喊3分     前两家没人要的话第三家只能喊3分
            msg.addAllRobNum(DDZHelper.switchRobotNum(3));
        } else if (data.countNum == 2 && data.currentRobIndex == -1) {
            msg.addAllRobNum(DDZHelper.switchRobotNum(2));
        } else if (data.currentRobIndex == -1){
            //点一次开始喊的地主
            msg.addAllRobNum(DDZHelper.switchRobotNum(0));
        } else {
            //上一家已经喊过地主了
//            if(!desk.canYellowPile()) {
                msg.addAllRobNum(DDZHelper.switchRobotNum(data.mPlayers[data.currentRobIndex].robNum));
//            }else{
//                msg.addAllRobNum(DDZHelper.switchRobotNumWithBiYou(data.countNum));
//            }
        }


//----------------------------------- 消息推送 ----------------------------------------------------
        DDZPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        DDZPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,9, PokerConstants.SEND_TYPE_ALL);
        System.out.println(msg.build().toString());
        System.out.println(msg.getRobNumList());

    }

    /**
     * 提示加不加呗
     *
     * @param gameData
     * @param desk
     */
    @Override
    public void notifyDouble(GameData gameData, MJDesk<byte[]> desk) {
        //获得当前操作的玩家
        PlayerInfo plx = desk.getDeskPlayer(gameData.getPokerOpPlayerIndex());

        if (plx == null) {
//            logger.error("act=notifyDouble;stage=gaming;error=noSuchPlayer;position={};deskId={};", gameData.getPokerOpPlayerIndex(), desk.getDeskID());
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为"+gameData.getPokerOpPlayerIndex());
            return;
        }

        DDZGameOperPlayerActionNotify.Builder msg = DDZGameOperPlayerActionNotify.newBuilder();
        msg.setPosition(plx.position);
        msg.setActions(PokerConstants.POKER_OPERTAION_DOUBLE);
//        msg.setDouble(1);
//==========================消息推送=================================
        gameData.mPlayerAction[plx.position].opStartTime = System.currentTimeMillis();
        DDZPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        DDZPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,9, PokerConstants.SEND_TYPE_ALL);

    }
    /**
     * 用于黄庄比较大小 确定谁得牌是最大的
     *
     * @param gameData
     * @param desk
     */
    private PlayerInfo thanCard (GameData gameData ,MJDesk<byte[]> desk) {

        Map<Integer,Map<Integer,Integer>> map = new HashMap<>();
        Map<Integer,Integer> num = new HashMap<>();
        List<PlayerInfo> player = desk.getPlayers();
        for (PlayerInfo playerInfo :player ) {
            Map<Integer,Integer> cardNum = returnNum(gameData.mPlayerCards[playerInfo.position].cardsInHand);
            gameData.cardMap.put(playerInfo.position,cardNum);

            int count = 0;
            count = cardNum.get(0) + cardNum.get(1) + cardNum.get(2);
            gameData.cardNum.put(playerInfo.position,count);
        }

//        logger.info("gameData.cardMap---"+gameData.cardMap);
//        logger.info("gameData.cardNum---"+gameData.cardNum);

        PlayerInfo p1 = player.get(0);
        PlayerInfo p2 = player.get(1);
        PlayerInfo p3 = player.get(2);

        PlayerInfo p4 = comparePlayer(gameData,desk,p1,p2);

        if (p4 != null) {
            //这个人是手牌最大的那个玩家
            PlayerInfo p = comparePlayer(gameData,desk,p4,p3);
            return p;
        } else {
            return p4;
        }
    }

    /**
     *
     * 两两比较 比出手牌最大的那个玩家
     *
     * @param gameData
     * @param desk
     * @param p1
     * @param p2
     * @return  返回手牌最大的那个玩家
     */
    public PlayerInfo comparePlayer (GameData gameData , MJDesk<byte[]> desk ,PlayerInfo p1 ,PlayerInfo p2) {

        if (gameData.cardMap ==null || gameData.cardNum ==null) {
            return null;
        }

        if (gameData.cardNum.get(p1.position) > gameData.cardNum.get(p2.position)) {
            //返回p1
            return p1;
        } else if (gameData.cardNum.get(p1.position) == gameData.cardNum.get(p2.position)) {
            if (gameData.cardMap.get(p1.position).get(2) != 0) {
                //返回p1
                return p1;
            }
            if (gameData.cardMap.get(p2.position).get(2) != 0) {
                //返回p1
                return p2;
            }
            if (gameData.cardMap.get(p1.position).get(1) != 0){
                //返回p1
                return p1;
            }
            if (gameData.cardMap.get(p2.position).get(1) != 0){
                //返回p1
                return p2;
            }
            if (gameData.robIndex == p1.position) {
                //返回p1
                return p1;
            }
            if ((gameData.robIndex + 1) % 3 == p1.position) {
                //返回p1
                return p1;
            }
            return p2;
        } else {
            //返回p2
            return p2;
        }

    }

    /**
     * 检测手牌中有几个2 和 大小王  用于黄庄比较大小(扣分)
     *
     * @param handCards
     * @return
     */
    private Map<Integer,Integer> returnNum (List<Byte> handCards) {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(0,0);//存放二
        map.put(1,0);//存放小王
        map.put(2,0);//存放大王
        if (handCards == null) return map;
        handCards = DDZRule.modular(handCards);
        for (byte card : handCards) {
            if (card == 15) {
                map.put(0,map.get(0) + 1);
            }
            if (card == 17) {
                map.put(1,1);
            }
            if (card == 18) {
               map.put(2,1);
            }
        }
        return map;
    }


    /**
     * poker 单局结束清楚玩家当前的某些字段
     *
     * @param gameData
     * @param desk
     */
    public void cleanPlayerInfo (GameData gameData ,MJDesk<byte[]> desk) {
        for (PlayerInfo playerInfo : desk.getPlayers()) {
            playerInfo.robNum = 0;
            playerInfo.multiple = 1;
//            playerInfo.score = 0;
        }
    }

    private void resetNextPlayerOperation(GameData gameData, MJDesk<byte[]> desk ,int Substate) {
        // 等待客户端播动画
        gameData.setWaitingStartTime(System.currentTimeMillis());
        gameData.setPlaySubstate(Substate);
//        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;
        // 顺序，轮到下一个玩家行动
        List<PlayerInfo> nextPlayer = desk.loopGetPlayer(gameData.getPokerOpPlayerIndex(), 1, 0);

        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
        if(Substate == PokerConstants.POKER_TABLE_SUB_STATE_DOUBLE){
            if(gameData.netxDoublePlayerIsDiZhu) gameData.setPokerOpPlayerIndex(gameData.currentRobIndex);
        }
    }

    @Override
    public void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {

    }
}
