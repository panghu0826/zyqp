package com.buding.poker.dualddz;

import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.PlayerInfo;
import com.buding.card.ICardLogic;
import com.buding.game.CardChangeReason;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.DDZHelper;
import com.buding.poker.helper.DDZPokerPushHelper;
import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.ddz.DDZ.*;
import packet.mj.MJBase.GameOperType;
import packet.mj.MJBase.GameOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DualDDZCardLogic implements ICardLogic<MJDesk<byte[]>> {
    private Logger log = LogManager.getLogger("DESKLOG");
    private GameData gameData;
    private MJDesk<byte[]> desk;

    @Override
    public void init(GameData gameData, MJDesk<byte[]> desk) {
        this.gameData = gameData;
        this.desk = desk;
    }

    @Override
    public void gameTick(GameData data, MJDesk<byte[]> desk) {
        long ctt = System.currentTimeMillis();
        PlayerInfo currentPl = desk.getDeskPlayer(data.getPokerOpPlayerIndex());
        //获取玩家的子原因状态
        int substate = gameData.getPlaySubstate();

        switch (substate) {
            case PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS: {
                if (ctt - gameData.getWaitingStartTime() > gameData.mGameParam.sendCardPlayMills) {
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
                }
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
                //通知玩家抢地主
                notifyRobotBanker(data, desk);
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD: {
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_NONE);
                //设置当前桌子操作(用于断线回来做判断)
                gameData.currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD;
                //提醒玩家出牌
                player_chu_notify(data, desk);
            }
            break;
            case PokerConstants.POKER_TABLE_SUB_STATE_NONE: {
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

    @Override
    public void gameStart(GameData gameData, MJDesk<byte[]> desk) {
        //清楚玩家当前的某些状态(上一局的倍数,分数)
        cleanPlayerInfo(gameData,desk);

        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null) continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            cl.clear();
            List<Byte> src = new ArrayList<>();

            boolean faPai = false; //false 随机发牌  true 自定义发牌
            if(faPai){
                StringBuffer bu = new StringBuffer();

                gameData.mDeskCard.ddzCards = chineseName2CardList("小王 黑Q 方3");

                if(pl.position == 0){
                    bu.append("小王 黑2 红2 方2 方A 红Q 梅Q 黑J 梅J 红10 梅9 方9 黑8 红8 黑7 梅7 黑6");
                    src.addAll(chineseName2CardList(bu.toString()));

                }else{
                    if(pl.position == 1) {
                        bu.append("大王 黑A 红A 黑K 方K 黑Q 方10 梅8 方8 红7 方7 红6 梅6 黑5 红5 梅5 方5");
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
            cl.addAll(DualDDZProcessor.sortHandCards(src));
        }

        for (int i = 0; i < 3; i++) {
            Byte b = gameData.popCard();
            gameData.mDeskCard.ddzCards.add(b);
        }

//        gameData.mDeskCard.ddzCards = new ArrayList<>(Arrays.asList((byte)58,(byte)29,(byte)12));

        gameData.gameSeq = (int) (System.nanoTime() % 10000);

        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null) continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            gameData.recorder.recordDDZPlayerCard(cl , pl.position);
        }
        gameData.recorder.recordDDZGameStart(gameData.mPlayers , gameData.mDeskCard.ddzCards);
        gameData.recorder.recordBasicInfo(gameData);
        // 把牌下发给客户端
        DDZGameOperStartSyn.Builder msg = DDZGameOperStartSyn.newBuilder();
        gameData.bottomFraction = 1;
        msg.setRobNum(gameData.bottomFraction);
        int handNum = gameData.handNum;
        msg.setQuanNum(handNum);// 当前局数
        msg.setServiceGold((int) desk.getFee());// 本局服务费
        msg.setSeq(gameData.gameSeq);
        msg.setMultiple(1);

        gameData.recorder.seq = msg.getSeq(); // 记录序列号
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null)
                continue;

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
     * poker 单局结束清楚玩家当前的某些字段
     */
    private void cleanPlayerInfo(GameData gameData, MJDesk<byte[]> desk) {
        for (PlayerInfo playerInfo : desk.getPlayers()) {
            playerInfo.multiple = 1;
            playerInfo.score = 0;
        }
    }

    private List<Byte> chineseName2CardList(String name){
        String[] cardListChinese = name.split(" ");
        List<Byte> cardList = new ArrayList<>();
        for(String card : cardListChinese){
            cardList.add(DDZHelper.singleCardMapChinese.get(card));
        }
        return cardList;
    }

    /**
     * 提示出牌
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

        if (gameData.prevCardType == -1 && gameData.prevCards == null || gameData.countNum == 1){
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
            DDZGameOperPlayerActionSyn.Builder bu = DDZGameOperPlayerActionSyn.newBuilder();
            bu.setPosition(gameData.getPokerOpPlayerIndex());
            bu.setAction(PokerConstants.POKER_OPERTAION_CLEAR_CARDS_IN_DESK);
            DDZPokerPushHelper.pushActionSyn(desk,-1,bu,PokerConstants.SEND_TYPE_ALL);

        } else {
            //手牌是否能打过上一家 给予提示
            List<List<Byte>> listCards = DualDDZProcessor.isOverBigReturnList(gameData.mPlayerCards[gameData.getPokerOpPlayerIndex()].cardsInHand,gameData.prevCards,gameData.prevCardType);
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

        //消息推送
        DDZPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        DDZPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,9,PokerConstants.SEND_TYPE_ALL);
        //同步手牌
        DDZPokerPushHelper.pushHandCardSyn(gameData,desk,plx);
    }

    /**
     * 自动出牌
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

    @Override
    public void playerOperation(GameData gameData, MJDesk<byte[]> desk, GeneratedMessage.Builder m, PlayerInfo pl) {
        DDZGameOperPlayerActionSyn.Builder msg = (DDZGameOperPlayerActionSyn.Builder) m;
        if (msg == null || pl == null) return;

        desk.setPauseTime(System.currentTimeMillis());

        // 玩家出牌
        if ((msg.getAction() & PokerConstants.POKER_OPERTAION_CHU) == PokerConstants.POKER_OPERTAION_CHU) {
            player_op_chu(gameData, desk, msg, pl);
        }

        // 玩家不出和要不起
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_CANCEL) == PokerConstants.POKER_OPERTAION_CANCEL) {
            player_op_cancel(gameData, desk, msg, pl);
        }

        // 玩家叫地主
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_JIAO_DIZHU) == PokerConstants.POKER_OPERTAION_JIAO_DIZHU) {
            player_op_jiaoDiZhu(gameData, desk, msg, pl);
        }

        // 玩家不叫地主
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_BU_JIAO_DIZHU) == PokerConstants.POKER_OPERTAION_BU_JIAO_DIZHU) {
            player_op_buJiaoDiZhu(gameData, desk, msg, pl);
        }

        // 玩家抢地主
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_ROBOT_BANKER) == PokerConstants.POKER_OPERTAION_ROBOT_BANKER) {
            player_op_robot(gameData, desk, msg, pl);
        }

        // 玩家不抢
        else if ((msg.getAction() & PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER) == PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER) {
            player_op_not_robot(gameData, desk, msg, pl);
        }

        else
            throw new RuntimeException("UnKnowOperation;");
    }

    private void player_op_jiaoDiZhu(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是抢地主,不可以叫地主========");
            return;
        }
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--叫地主");
        //设置桌子和玩家参数
        gameData.currentJiaoOrRobIndex = pl.position;
        gameData.bottomFraction++;
        gameData.jiaoOrQiangDiZhuNum++;//抢地主次数加一
        gameData.lastActionPlayerPos = pl.position;
        gameData.isJiaoDiZhu = true;

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);

        //消息推送
        msg.setRobNum(gameData.bottomFraction);
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);

        //判断是否结束抢地主
        checkRobIsOver(gameData,desk,msg,pl);
    }

    private void player_op_robot(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是抢地主,不可以抢地主========");
            return;
        }
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--抢地主");

        //设置桌子和玩家参数
        gameData.currentJiaoOrRobIndex = pl.position;
        gameData.bottomFraction++;
        gameData.jiaoOrQiangDiZhuNum++;//抢地主次数加一
        gameData.lastActionPlayerPos = pl.position;

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);

        //消息推送
        msg.setRobNum(gameData.bottomFraction);
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);

        //判断是否可以可以结束抢地主(置换为下一个状态值)
        checkRobIsOver(gameData,desk,msg,pl);
    }

    private void player_op_not_robot(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是抢地主,不可以不抢地主========");
            return;
        }
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--不抢地主");

        //桌子数据
        gameData.notJiaoDiZhuNum++;
        gameData.lastActionPlayerPos = pl.position;

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);

        //消息推送
        msg.setRobNum(gameData.bottomFraction);
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);

        //判断是否可以可以结束抢地主(置换为下一个状态值)
        checkRobIsOver(gameData,desk,msg,pl);
    }

    private void player_op_buJiaoDiZhu(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是抢地主,不可以不叫地主========");
            return;
        }
        if(gameData.lastActionPlayerPos == pl.position) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========请等待上一次操作执行========");
            return;
        }
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--不叫地主");

        //桌子数据
        gameData.notJiaoDiZhuNum++;
        gameData.lastActionPlayerPos = pl.position;

        //回放
        gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),new ArrayList<Integer>(),gameData.bottomFraction,-1);

        //消息推送
        msg.setRobNum(gameData.bottomFraction);
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);

        //判断是否可以可以结束抢地主(置换为下一个状态值)
        checkRobIsOver(gameData,desk,msg,pl);
    }

    private void checkRobIsOver (GameData gameData, MJDesk<byte[]> desk ,DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if (!gameData.isJiaoDiZhu){//尚未叫地主
            if(gameData.notJiaoDiZhuNum == 2) {
                //两个人都没叫地主,重新发牌,注意此时不计算一局,不走结算
                log.info("桌子id--" + desk.getDeskID() + "--" + "没有人抢地主重新发牌");
                gameData.setPokerOpPlayerIndex(-1);
                if (desk.canRoundPile()) {
                    List<PlayerInfo> nextPlay = this.desk.loopGetPlayer(gameData.robIndex,1,0);
                    gameData.robIndex = nextPlay.get(0).position;
                }
                //在设置状态值
                gameData.setState(PokerConstants.GAME_TABLE_STATE_RE_SEND_CARD);
                return;
            }
            resetNextPlayerOperation(gameData , desk , PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
        }else{//已经叫了地主
            if((msg.getAction() & PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER) == PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER
                    || ((msg.getAction() & PokerConstants.POKER_OPERTAION_JIAO_DIZHU) == PokerConstants.POKER_OPERTAION_JIAO_DIZHU
                            && gameData.notJiaoDiZhuNum == 1
                        )
                    || gameData.bottomFraction >= 6){
                //如果当前玩家不抢地主则确认地主,出牌
                log.info("桌子id--"+desk.getDeskID()+"--"+"确认了地主,开始打牌");
                gameData.currentRobIndex = gameData.currentJiaoOrRobIndex;

                DDZGameOperPlayerActionSyn.Builder operMsg = DDZGameOperPlayerActionSyn.newBuilder();
                operMsg.setPosition(gameData.currentRobIndex);
                operMsg.setAction(PokerConstants.POKER_OPERTAION_PLAY_ROBOT);//确认地主是谁..
                operMsg.setRobNum(gameData.bottomFraction);//设置底分
                gameData.addCardsInHand(gameData.mDeskCard.ddzCards,gameData.currentRobIndex);
                operMsg.setCardNum(gameData.mPlayerCards[gameData.currentRobIndex].cardsInHand.size());
                operMsg.setRangPaiNum(gameData.jiaoOrQiangDiZhuNum);
                List<Integer> list = new ArrayList<>();
                for (Byte card : gameData.mDeskCard.ddzCards) {
                    operMsg.addCardValue(card);//三张底牌
                    list.add((int) card);
                }

                //推送消息给其他玩家
                DDZPokerPushHelper.pushActionSyn(desk,0,operMsg,PokerConstants.SEND_TYPE_ALL);
                DDZPokerPushHelper.pushHandCardSyn(gameData,desk,pl);

                //回放
                gameData.recorder.recordDDZPlayerAction(gameData.genSeq(), gameData.currentRobIndex, PokerConstants.POKER_OPERTAION_PLAY_ROBOT,list,gameData.bottomFraction,-1);

                gameData.setPokerOpPlayerIndex(gameData.currentRobIndex);
                gameData.setWaitingStartTime(System.currentTimeMillis());

                //状态跳转出牌
                gameData.countNum = 1;
                gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
            }else{
                //否则下个玩家继续抢
                resetNextPlayerOperation(gameData , desk , PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER);
            }
        }
    }

    private void resetNextPlayerOperation(GameData gameData, MJDesk<byte[]> desk ,int Substate) {
        //等待客户端播动画
        gameData.setWaitingStartTime(System.currentTimeMillis());
        gameData.setPlaySubstate(Substate);
        //顺序,轮到下一个玩家行动
        List<PlayerInfo> nextPlayer = desk.loopGetPlayer(gameData.getPokerOpPlayerIndex(), 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
    }

    /**
     * 玩家不出
     */
    private void player_op_cancel(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是操作状态,不可以cancel========");
            return;
        }
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
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name + "--过");
        gameData.countNum++;
        if (gameData.countNum == 1) {
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

        //消息
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);
        resetNextPlayerOperation(gameData,desk,PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    /**
     *
     * 玩家出牌
     */
    private void player_op_chu(GameData gameData, MJDesk<byte[]> desk, DDZGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        if(gameData.currentDeskState != PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========不是操作状态,不可以操作========");
            return;
        }
        int index = msg.getPosition();
        PlayerInfo plx = gameData.mPlayers[index];
        if (index != gameData.getPokerOpPlayerIndex() || plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        if (msg.getCardValueCount() == 0 || msg.getCardValueList().contains(0)) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========出牌Error========"+msg.getCardValueList());
            return;
        }
        if (!gameData.mPlayerCards[pl.position].cardsInHand.containsAll(DualDDZProcessor.int2ByteList(msg.getCardValueList()))) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有这张牌不能出========"+"出的牌"+msg.getCardValueList());
            return;
        }

        //获得玩家出的牌
        List<Integer> outCards = msg.getCardValueList();
        List<Byte> cards = new ArrayList<>();
        for (int card : outCards) {
            cards.add((byte)card);
        }
        //检测牌型
        int pokerType = DualDDZProcessor.getCardType(cards);
        if (msg.getCanOut() == 1 || msg.getCanOut() == 4) {//非压牌时看牌型,牌型不对不能出
            if (pokerType == 0) {
                //重新推送出牌消息
                gameData.unnatural = 1;
                player_chu_notify(gameData,desk);
                return;
            }
        } else if (msg.getCanOut() == 2 || msg.getCanOut() == 5) {//压牌时看下能不能压
            boolean falg = DualDDZProcessor.isOvercomePrev(cards,pokerType,gameData.prevCards,gameData.prevCardType);
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

        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--" + pl.name + "--出牌" + DDZHelper.getSingleCardListName(cards));
        //往桌子设置属性参数

        //统计地主出牌次数 为反春天做判断
        if (index == gameData.currentRobIndex) {
            gameData.robOutCard++;
        }
        gameData.prevCards.clear();
        gameData.prevCards.addAll(cards);
        gameData.prevCardType = pokerType;
        gameData.prevIndex = plx.position;
        gameData.countNum = 0;
        gameData.unnatural = 0;
        //炸弹和王炸底分翻倍
        if (pokerType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            gameData.bottomFraction *= 2 ;
            gameData.ddzResult.bomb++;
        }
        if (pokerType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            gameData.bottomFraction *= 2 ;
            gameData.ddzResult.friedKing++;
        }
        gameData.removeCardInHand(cards,index, CardChangeReason.CHU);//移除玩家打出的牌
        //记录打出去的牌
        for (Byte card : cards) {
            gameData.addCardBefore(card,index);
        }

        for (PlayerInfo p : gameData.mPlayers) {
            if(p == null) continue;
            log.info("桌子id--"+desk.getDeskID()+"--"+"========玩家===="+p.name+"==牌的状况为:");
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家剩余手牌张数 : " + gameData.getCardNumInHand(p.position));
            log.info("桌子id--"+desk.getDeskID()+"--"+"-----手牌 : " + DDZHelper.getSingleCardListName(gameData.getCardsInHand(p.position)));
            log.info("桌子id--"+desk.getDeskID()+"--"+"---打出去的牌 : " + DDZHelper.getSingleCardListName(gameData.mPlayerCards[p.position].cardsBefore));
        }

        List<Integer> outCardSort = DualDDZProcessor.sortOutCards(outCards,pokerType);
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

        //消息推送
        DDZPokerPushHelper.pushActionSyn(desk,0,msg,PokerConstants.SEND_TYPE_ALL);

        //同步玩家手牌
        DDZPokerPushHelper.pushHandCardSyn(gameData,desk,plx);

        //当前玩家的手牌为0的话 说明这把游戏已经结束了
        if ((index == gameData.currentRobIndex && gameData.getCardNumInHand(index) == 0)
                || (index != gameData.currentRobIndex && gameData.getCardNumInHand(index) <= gameData.jiaoOrQiangDiZhuNum)) {
            if(index == gameData.currentRobIndex || gameData.mPlayerCards[index].cardsInHand.isEmpty()) {
                gameData.mPlayerCards[index].cardsInHand = DualDDZProcessor.int2ByteList(msg.getCardValueList());
            }
            //总结算页面 统计当前这人赢了几句
            gameData.ddzFinalResult.finalResults.get(plx.playerId).winInnings++;
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
                gameData.spring *= 2;
                gameData.ddzResult.spring = 1;
                if (gameData.currentRobIndex == plx.position) {//春天
                    notify.setSpring(1);
                } else {//反春
                    notify.setSpring(2);
                }
            }

            //把其他两家的牌亮给所有人看
            for (PlayerInfo p :desk.getPlayers()) {
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
            DDZPokerPushHelper.pushActionNofity(gameData,desk,0,notify,PokerConstants.SEND_TYPE_ALL);

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
            return;
        }

        resetNextPlayerOperation(gameData,desk,PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }


    /**
     * 判断是否为春天
     * 默认返回是春天
     */
    private boolean isSpring(GameData gameData, MJDesk<byte[]> desk, PlayerInfo pl) {
        if (pl.position == gameData.currentRobIndex) {
            for (PlayerInfo playerInfo : desk.getPlayers()) {
                if (playerInfo.playerId != pl.playerId) {
                    if (gameData.getCardNumInHand(playerInfo.position) != 17) {
                        return false;
                    }
                }
            }
        } else {
            return gameData.robOutCard <= 1;
        }

        return true;
    }
    /**
     * 斗地主的结算
     */
    private void settlement (GameData gameData, MJDesk<byte[]> desk, PlayerInfo pl) {
        gameData.ddzResult.endTime = System.currentTimeMillis();
        for (PlayerInfo px : desk.getPlayers()) {
           if (px.position != gameData.currentRobIndex) {
                //两农民应该输赢得分
                if (px.multiple == 1) {
                    px.score = gameData.bottomFraction * px.multiple  * gameData.spring;
                } else {
                    px.score = gameData.bottomFraction * px.multiple * gameData.mPlayers[gameData.currentRobIndex].multiple * gameData.spring;
                }
                if(px.score > desk.getLimitMax()){
                    px.score = desk.getLimitMax();
                }
                //地主的分数应该是两个玩家的分数之和
                gameData.mPlayers[gameData.currentRobIndex].score = gameData.mPlayers[gameData.currentRobIndex].score + px.score;
           }
        }

        gameData.ddzResult.innings = gameData.handNum;
        gameData.ddzResult.endPoints = gameData.bottomFraction * gameData.spring;

        if (pl.playerId == gameData.mPlayers[gameData.currentRobIndex].playerId) {
            //地主赢
            for (PlayerInfo px : desk.getPlayers()) {
                if (px.playerId == pl.playerId) {//是地主
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
                    gameData.ddzResult.Result.get(px.playerId).isDiZhu = 1;
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.score;
                    gameData.ddzResult.Result.get(px.playerId).score = px.score;
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore +=  px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score +=  px.score;

                } else {//农民
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.score;
                    gameData.ddzResult.Result.get(px.playerId).score = - px.score;
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore - px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore -=  px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score -=  px.score;
                }
            }
        } else {//农民赢
            for (PlayerInfo px : desk.getPlayers()) {
                if (px.playerId == gameData.mPlayers[gameData.currentRobIndex].playerId) {//地主
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
                    gameData.ddzResult.Result.get(px.playerId).isDiZhu = 1;
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.score;
                    gameData.ddzResult.Result.get(px.playerId).score = - px.score;
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore - px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore -=  px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score -=  px.score;
                } else {
                    if (px.multiple != 1) {
                        //是否加倍
                        gameData.ddzResult.Result.get(px.playerId).isDouble = 1;
                    }
                    gameData.ddzResult.Result.get(px.playerId).multiple = px.score;
                    gameData.ddzResult.Result.get(px.playerId).score =  px.score;
                    gameData.ddzResult.Result.get(px.playerId).allScore = gameData.ddzFinalResult.finalResults.get(px.playerId).allScore + px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).allScore +=  px.score;
                    gameData.ddzFinalResult.finalResults.get(px.playerId).score +=  px.score;
                }
            }
        }

        for (PlayerInfo px : desk.getPlayers()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"======" + px.name + " : " + gameData.ddzResult.Result.get(px.playerId).score + " 总分 : " + gameData.ddzFinalResult.finalResults.get(px.playerId).allScore);
        }

    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info) {

    }

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
        ddz.setRangPaiNum(gameData.jiaoOrQiangDiZhuNum);
        ddz.setSeq(gameData.gameSeq);
        for (int card : gameData.prevCards) {
            ddz.addLastActionCard(card);//添加玩家打出去的牌
        }

        gameData.recorder.seq = ddz.getSeq(); // 记录序列号

        PlayerInfo p = desk.getDeskPlayer(position);
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
            dz.setSocre(gameData.ddzFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            dz.setCardNum(gameData.getCardNumInHand(pl.position));//设置玩家的手牌
            dz.setPosition(pl.position);
            if (gameData.getPokerOpPlayerIndex() == pl.position){
                dz.setNeedFenCha(1);
            }
            ddz.addPlayerHandCards(dz);
        }

        GameOperation.Builder gb = GameOperation.newBuilder();
        gb.setOperType(GameOperType.DDZGameOperStartSyn);
        gb.setContent(ddz.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(ddz.build()));

        desk.sendMsg2Player(p.position, gb.build().toByteArray());
        // 发送公告信息
        DDZPokerPushHelper.pushPublicInfoMsg2Single(desk, position, gameData);

        // 发送当前操作人
        DDZPokerPushHelper.pushActorSyn(desk, position, gameData.getPokerOpPlayerIndex(), 9, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, position);
    }

    /**
     * 重新通知玩家操作
     *
     */
    @Override
    public void re_notify_current_operation_player(GameData gameData, MJDesk<byte[]> desk, int position) {

        if (gameData.getPokerOpPlayerIndex() == position) {
            //获取玩家的子原因状态
            int substate = gameData.currentDeskState;

            switch (substate) {
                case PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER :
                    //提醒玩家重新抢地主
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新抢地主");
                    notifyRobotBanker(gameData,desk);
                    break;
                case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD:
                    //提醒玩家重新出
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新出");
                    player_chu_notify(gameData, desk);
                    break;
            }

        }

    }

    @Override
    public void selectBanker(GameData data, MJDesk<byte[]> desk) {
        gameData.setPokerOpPlayerIndex(gameData.robIndex);
//        gameData.setPokerOpPlayerIndex(1);
    }

    /**
     * 提示是否可以抢地主
     */
    @Override
    public void notifyRobotBanker(GameData data, MJDesk<byte[]> desk) {
        //获得当前操作的玩家
        PlayerInfo plx = desk.getDeskPlayer(data.getPokerOpPlayerIndex());
        if (plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为"+data.getPokerOpPlayerIndex());
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"----提示玩家--"+plx.name+(data.isJiaoDiZhu ? "--抢" : "--叫")+"地主---");

        DDZGameOperPlayerActionNotify.Builder msg = DDZGameOperPlayerActionNotify.newBuilder();
        msg.setActions(!data.isJiaoDiZhu ? PokerConstants.POKER_OPERTAION_JIAO_DIZHU:PokerConstants.POKER_OPERTAION_ROBOT_BANKER);
        msg.setPosition(plx.position);

        //给操作者推送提示消息
        DDZPokerPushHelper.pushActionNofity(data,desk,plx.position,msg,PokerConstants.SEND_TYPE_SINGLE);
        //给桌子所有玩家推送操作者操作信息
        DDZPokerPushHelper.pushActorSyn(desk,-1,plx.position,9,PokerConstants.SEND_TYPE_ALL);
    }

    @Override
    public void notifyDouble(GameData gameData, MJDesk<byte[]> desk) {

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
        ddz.setRangPaiNum(gameData.jiaoOrQiangDiZhuNum);
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
            dz.setSocre(gameData.ddzFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            dz.setCardNum(gameData.getCardNumInHand(pl.position));//设置玩家的手牌
            dz.setPosition(pl.position);
            if (gameData.getPokerOpPlayerIndex() == pl.position){
                dz.setNeedFenCha(1);
            }
            ddz.addPlayerHandCards(dz);
        }

        GameOperation.Builder gb = GameOperation.newBuilder();
        gb.setOperType(GameOperType.DDZGameOperStartSyn);
        gb.setContent(ddz.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(ddz.build()));
        desk.sendMsg2Player(p, gb.build().toByteArray());
    }

    @Override
    public void handleSetGamingData(GameCardDealer mCardDealer, GameData gameData, MJDesk<byte[]> desk, String json) {

    }

    @Override
    public void erBa_notify_new_player(GameData gameData, MJDesk<byte[]> desk, PlayerInfo p) {

    }
}
