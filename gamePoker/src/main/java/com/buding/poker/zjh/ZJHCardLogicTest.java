package com.buding.poker.zjh;

import com.buding.api.context.PokerZJHFinalResult;
import com.buding.api.context.PokerZJHResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.api.player.PlayerInfo;
import com.buding.api.player.RecordData;
import com.buding.api.player.ZJHRecordData;
import com.buding.card.ICardLogic;
import com.buding.game.GameCardDealer;
import com.buding.game.GameData;
import com.buding.poker.common.ZJHBiPaiResult;
import com.buding.poker.common.ZJHRule;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.ZJHHelper;
import com.buding.poker.helper.ZJHPokerPushHelper;
import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packet.mj.MJBase;
import packet.zjh.ZJH;
import packet.zjh.ZJH.ZJHGameOperPlayerActionSyn;

import java.util.*;

public class ZJHCardLogicTest implements ICardLogic<MJDesk<byte[]>> {

    private static Logger log = LogManager.getLogger("DESKLOG");
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
                if (ctt - gameData.getWaitingStartTime() > gameData.mGameParam.sendCardPlayMills) {
                    gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
                }
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
                long time = gameData.mPlayerAction[currentPl.position].opStartTime;
                boolean isTimeout = (ctt - time) > (gameData.mGameParam.operTimeOutSeconds * 1000);
                if (isTimeout && time != 0) playerAutoOper(data, desk, data.getPokerOpPlayerIndex());
            }
            break;
        }
    }

    /**
     * 发牌
     *
     */
    @Override
    public void gameStart(GameData data, MJDesk<byte[]> desk) {
        //福利玩家,作弊系统
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
                    // 40% - 2把 40% - 1把, 20% - 1把
                    int fuliNum = 0;
                    int random = (int)(Math.random() *10) + 1;
                    if (random >= 1 && random <= 4) {
                        fuliNum = 2;
                    } else if (random >= 5 && random <= 8){
                        fuliNum = 1;
                    }
                    if (fuliNum > 0) {
                        List<Integer> fuliJuNumList = new ArrayList<>();
                        for (int i = 0; i < fuliNum; i++) {
                            fuliJuNumList.add(juNumList.get(i));
                        }
                        data.fuliPlayerMap.put(p.playerId, fuliJuNumList);
                    }
                }
            }
        }

        List<PlayerInfo> playerList = desk.getPlayingPlayers();
        int fuliPlayerPos = isFuliPlayerAndFaPai(desk,data);
        if(fuliPlayerPos >= 0 && !desk.canWangLaiZi()) {
            playerList = desk.loopGetPlayerZJH(fuliPlayerPos,desk.getPlayingPlayers().size(),2);
            List<Byte> deskCardsNew = new ArrayList<>(data.mDeskCard.cards);
            data.mDeskCard.cards = ZJHUtil.fuli(desk.getPlayingPlayers().size(),deskCardsNew);
        }
        gameData.danZhu = desk.getYaZhu() < 0 ? 1:desk.getYaZhu();
        log.info("福利局数据--"+gameData.fuliPlayerMap+"发牌--"+data.mDeskCard.cards);
        log.info("桌子id--"+desk.getDeskID()+"--"+"playerList-----"+playerList);
        for (PlayerInfo pl : playerList) {
            pl.danZhu = gameData.danZhu;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            cl.clear();
            List<Byte> src = new ArrayList<Byte>();

            boolean isBanker = pl.playerId == gameData.mPublic.mBankerUserId;

            boolean faPai = false; //false 随机发牌  true 自定义发牌
            if(faPai){
                StringBuffer bu = new StringBuffer();
                if(isBanker){
                    bu.append("梅A ");
                    bu.append("黑3 ");
                    bu.append("红2");
                    src.addAll(chineseName2CardList(bu.toString()));

                }else{
                    if(pl.position == (gameData.robIndex+1)%2) {
                        bu.append("梅9 ");
                        bu.append("方9 ");
                        bu.append("黑9");
                        src.addAll(chineseName2CardList(bu.toString()));

                    }
//                    else if(pl.position == (gameData.robIndex+2)%3){
//                        bu.append("方6 ");
//                        bu.append("红6 ");
//                        bu.append("黑6");
//                        src.addAll(chineseName2CardList(bu.toString()));
//                    }
                }
            }else{
                for (int j = src.size(); j < 3; j++) {
                    Byte b = gameData.popCard();
                    src.add(b);
                }
            }
            // 排个序
            cl.addAll(ZJHProcessor.sortHandCards(src));
        }

        gameData.gameSeq = (int) (System.nanoTime() % 10000);

        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            gameData.recorder.recordZJHPlayerCard(cl , pl.position);
        }

        List<RecordData> recordData = new ArrayList<>();
        for (PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            ZJHRecordData zjhRecordData = new ZJHRecordData();
            zjhRecordData.position = pl.position;
            zjhRecordData.cardType = getCardType(cl,desk);
            zjhRecordData.score = gameData.zjhFinalResult.finalResults.get(pl.playerId).allScore;
            recordData.add(zjhRecordData);
        }

        gameData.recorder.recordZJHGameStart(gameData.mPlayers ,recordData, gameData.mDeskCard.cards);
        gameData.recorder.recordBasicInfo(gameData);
        // 把牌下发给客户端
        ZJH.ZJHGameOperStartSyn.Builder msg = ZJH.ZJHGameOperStartSyn.newBuilder();

        gameData.zongZhu = desk.getPlayingPlayers().size() * (desk.getYaZhu() < 0 ? 1:desk.getYaZhu());
        int handNum = gameData.handNum;
        msg.setJuNum(handNum);// 当前局数
        msg.setSeq(gameData.gameSeq);
        msg.setLunNum(gameData.lunNum);
        msg.setZongZhu(gameData.zongZhu);
        msg.setBankerPos(gameData.robIndex);
        gameData.recorder.seq = msg.getSeq(); // 记录序列号
        log.info("桌子id--"+desk.getDeskID()+"--"+"-----第"+gameData.handNum+"把开始----玩法为"+ PokerWanfa.getWanFaString(desk.getWanfa()));
        for (PlayerInfo pl : gameData.mPlayers) {
            if (pl == null || pl.isZanLi || pl.isWait) continue;
            List<Byte> cl = gameData.getCardsInHand(pl.position);
            log.info("桌子id--"+desk.getDeskID()+"--"+"act=initcards;position={};cards={};", pl.position, new Gson().toJson(cl));
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--座位号--"+pl.position+"--id--"+pl.playerId);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--累计分数/金币数--"+gameData.zjhResult.Result.get(pl.playerId).allSocre);
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+pl.name+"--手牌--"+ ZJHHelper.getSingleCardListName(gameData.getCardsInHand(pl.position)));
        }

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        for (PlayerInfo pl : desk.getAllPlayers()) {
            msg.clearPlayerHandCards();
            for (PlayerInfo p : desk.getAllPlayers()) {
                boolean showHandCardVal = (p.position == pl.position && p.isKanPai && !p.isWait && p.position >= 0)
                        || (gameData.fuliPlayerMap.keySet().contains(pl.playerId));
                ZJH.ZJHGameOperHandCardSyn.Builder handCardBuilder = ZJH.ZJHGameOperHandCardSyn.newBuilder();
                // 发给玩家的牌
                if(p.position >= 0) {
                    for (int card : gameData.getCardsInHand(p.position)) {
                        handCardBuilder.addHandCards(showHandCardVal ? card : -1);
                    }
                }
                p.chouMa = desk.getYaZhu() < 0 ? 1:desk.getYaZhu();

                handCardBuilder.setPosition(p.position);// 玩家的桌子位置
                handCardBuilder.setCanKanPai(gameData.lunNum > desk.getMenNum() && !p.isKanPai);
                handCardBuilder.setChouMa(p.chouMa);
                handCardBuilder.setPlayerDanZhu(p.danZhu);
                handCardBuilder.setIsWait(p.isWait);
                msg.addPlayerHandCards(handCardBuilder);
            }

            MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
            gb.setOperType(MJBase.GameOperType.ZJHGameOperStartSyn);
            gb.setContent(msg.build().toByteString());
            gb.setType(0);

            desk.sendMsg2Player(pl, gb.build().toByteArray());
            log.info("桌子id--"+desk.getDeskID()+"--给玩家--"+ pl.name+"发牌消息"+JsonFormat.printToString(msg.build()));
        }
        gameData.showInitCardTime = System.currentTimeMillis();
        gameData.setPlaySubstate(PokerConstants.POKER_TABLE_SUB_STATE_INIT_CARDS);
        gameData.setWaitingStartTime(System.currentTimeMillis());

//        log.info("桌子id--"+desk.getDeskID()+"--"+"act=onSendCard;seq={};players={}", msg.getSeq(), new Gson().toJson(gameData.mPlayers));

    }

    /**
     * 获取福利玩家,如果当局有多个,返回其中一个福利玩家位置
     * @param desk
     * @param gameData
     * @return
     */
    private int isFuliPlayerAndFaPai(MJDesk<byte[]> desk, GameData gameData) {
        List<Integer> positionList = new ArrayList<>();
        for(PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Integer> juNumList = gameData.fuliPlayerMap.get(pl.playerId);
            if (juNumList != null && juNumList.contains(gameData.handNum))
                positionList.add(pl.position);
        }
        if(positionList.isEmpty()) return -1;
        Collections.shuffle(positionList);
        return positionList.get(0);
    }

    /**
     * 提示玩家操作
     * 1.操作有 弃牌,跟住,加注,比牌,看牌
     * 2.逆时针玩牌
     * 3.庄家下家开始提示,庄家操作完算一轮
     * 4.
     *      弃牌:肯定有,无需传值
     *      跟注:最后一轮 或者积分场不可负分情况下玩家筹码不够了 或者一闷到底并且还没加注的情况下 没有跟注,传跟注的筹码
     *      加注:当前筹码 >= 单注封顶 或者最后一轮 或者积分场不可负分情况下玩家筹码不够了 则不能加注,传加注集合
     *      比牌:当前轮数 > 桌子的必闷选项 或者积分场不可负分情况下玩家筹码不够了 则可以比牌,传可被比牌人座位集合
     *      看牌:当前轮数 > 桌子的必闷选项 并且 没有看过牌 则可以看牌,无需传值
     */
    @Override
    public void player_chu_notify(GameData gameData, MJDesk<byte[]> desk) {
        PlayerInfo plx = desk.getDeskPlayer(gameData.getPokerOpPlayerIndex());
        if (plx == null) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"未找到该玩家,出牌玩家座位号为" + gameData.getPokerOpPlayerIndex());
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"提示玩家--" + plx.name + "出牌");
        ZJH.ZJHGameOperPlayerActionNotify.Builder msg = ZJH.ZJHGameOperPlayerActionNotify.newBuilder();
        msg.setPosition(plx.position);
        //可以弃牌
        msg.setActions(msg.getActions() | PokerConstants.ZJH_OPERTAION_QI_PAI);
        checkKanPai(gameData,desk,plx,msg);
        checkBiPai(gameData,desk,plx,msg);
        checkGenZhu(gameData,desk,plx,msg);
        checkJiaZhu(gameData,desk,plx,msg);

        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = System.currentTimeMillis();
        //推送消息
        ZJHPokerPushHelper.pushActionNofity(gameData,desk,plx.position,msg, PokerConstants.SEND_TYPE_SINGLE);
        //广播当前正在操作的玩家
        ZJHPokerPushHelper.pushActorSyn(desk,plx.position,plx.position,15, PokerConstants.SEND_TYPE_ALL);
        //同步手牌
        ZJHPokerPushHelper.pushHandCardSyn(gameData,desk,plx);
    }

    private void checkJiaZhu(GameData gameData, MJDesk<byte[]> desk,PlayerInfo plx, ZJH.ZJHGameOperPlayerActionNotify.Builder msg) {
        if(desk.isClubJiFenDesk() && desk.getCanFufen() == 1 && plx.chouMa >= plx.score) return;
        if(gameData.lunNum <= PokerConstants.ZJH_MAX_LUN && desk.getDanZhuLimix() > gameData.danZhu){
            msg.setActions(msg.getActions() | PokerConstants.ZJH_OPERTAION_JIA_ZHU);
            if(desk.getYaZhu() != PokerConstants.ZJH_ZI_YOU_CAHNG){
                int jiaZhu = desk.getDanZhuLimix() * (plx.isKanPai ? 2 : 1);
                if(desk.getYaZhu() == PokerConstants.ZJH_YI_FEN_CAHNG || desk.getYaZhu() == PokerConstants.ZJH_ER_FEN_CAHNG){
                    int a = jiaZhu % 4;
                    jiaZhu = (a == 0 && plx.isKanPai) ? (jiaZhu/4) * 5 : jiaZhu;
                }
                msg.addJiaZhuNum(jiaZhu);
            } else {
                if(desk.canXiaManZhu()){
                    msg.addJiaZhuNum(desk.getDanZhuLimix() * (plx.isKanPai ? 2 : 1));
                }else {
                    for (int i = (gameData.danZhu + 1); i <= desk.getDanZhuLimix(); i++) {
                        msg.addJiaZhuNum(i * (plx.isKanPai ? 2 : 1));
                    }
                }
            }
        }
    }

    private void  checkGenZhu(GameData gameData, MJDesk<byte[]> desk,PlayerInfo plx, ZJH.ZJHGameOperPlayerActionNotify.Builder msg) {
        if(desk.isClubJiFenDesk() && desk.getCanFufen() == 1 && plx.chouMa >= plx.score) return;
        if(desk.getMenNum() == PokerConstants.ZJH_MAX_LUN && !gameData.isJiaZhu) return;
        if(gameData.lunNum <= PokerConstants.ZJH_MAX_LUN) {
            msg.setActions(msg.getActions() | PokerConstants.ZJH_OPERTAION_GEN_ZHU);
            int genZhu = gameData.danZhu * (plx.isKanPai ? 2 : 1);
            if(desk.getYaZhu() == PokerConstants.ZJH_YI_FEN_CAHNG || desk.getYaZhu() == PokerConstants.ZJH_ER_FEN_CAHNG){
                int a = genZhu % 4;
                genZhu = (a == 0 && plx.isKanPai)? (genZhu/4) * 5 : genZhu;
            }
            msg.setGenZhu(genZhu);
        }
    }

    private void checkBiPai(GameData gameData, MJDesk<byte[]> desk,PlayerInfo plx, ZJH.ZJHGameOperPlayerActionNotify.Builder msg) {
        if(gameData.lunNum > desk.getMenNum() || (desk.isClubJiFenDesk() && desk.getCanFufen() == 1 && plx.chouMa >= plx.score)){
            msg.setActions(msg.getActions() | PokerConstants.ZJH_OPERTAION_BI_PAI);
           for(PlayerInfo p : gameData.mPlayers){
               if(p == null || p.isQiPai || p.isWait || p.isLose || p.position == plx.position) continue;
               msg.addBiPaiPos(p.position);
           }
        }
    }

    private void checkKanPai(GameData gameData, MJDesk<byte[]> desk,PlayerInfo plx, ZJH.ZJHGameOperPlayerActionNotify.Builder msg) {
        //可以看牌
        if(gameData.lunNum > desk.getMenNum()){
            if(!plx.isKanPai) {
                msg.setActions(msg.getActions() | PokerConstants.ZJH_OPERTAION_SEE);
            }

            //这时候所有人都可以看牌
            for(Object o : desk.getPlayingPlayers()){
                PlayerInfo p = (PlayerInfo)o;
                if(p.isQiPai || p.isLose || p.isKanPai || p.position == plx.position) continue;
                ZJH.ZJHGameOperPlayerActionNotify.Builder msg2 = ZJH.ZJHGameOperPlayerActionNotify.newBuilder();
                msg2.setPosition(p.position);
                msg2.setActions(PokerConstants.ZJH_OPERTAION_SEE);
                //推送消息
                ZJHPokerPushHelper.pushActionNofity(gameData,desk,p.position,msg2, PokerConstants.SEND_TYPE_SINGLE);
            }
        }
    }

    @Override
    public void playerAutoOper(GameData gameData, MJDesk gt, int position) {
        //能自动弃牌
        if (gt.canAutoQiPai()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家自动弃牌---");
            ZJHGameOperPlayerActionSyn.Builder msg = ZJHGameOperPlayerActionSyn.newBuilder();
            msg.setPosition(position);
            msg.setAction(PokerConstants.ZJH_OPERTAION_QI_PAI);
            this.playerOperation(gameData,gt,msg,gameData.mPlayers[position]);
        }

    }

    @Override
    public void playerOperation(GameData gameData, MJDesk gt, GeneratedMessage.Builder m, PlayerInfo pl) {
        ZJHGameOperPlayerActionSyn.Builder msg = (ZJHGameOperPlayerActionSyn.Builder) m;

        if (msg == null || pl == null || msg.getAction() == 0) return;

        gt.setPauseTime(System.currentTimeMillis());
        //比牌
        if ((msg.getAction() & PokerConstants.ZJH_OPERTAION_BI_PAI) == PokerConstants.ZJH_OPERTAION_BI_PAI) {
            player_op_biPai(gameData, gt, msg, pl);
        }
        //看牌
        else if ((msg.getAction() & PokerConstants.ZJH_OPERTAION_SEE) == PokerConstants.ZJH_OPERTAION_SEE) {
            player_op_seeCard(gameData, gt, msg, pl);
        }
        //跟注
        else if ((msg.getAction() & PokerConstants.ZJH_OPERTAION_GEN_ZHU) == PokerConstants.ZJH_OPERTAION_GEN_ZHU) {
            player_op_genZhu(gameData, gt, msg, pl);
        }
        //弃牌
        else if ((msg.getAction() & PokerConstants.ZJH_OPERTAION_QI_PAI) == PokerConstants.ZJH_OPERTAION_QI_PAI) {
            player_op_qiPai(gameData, gt, msg, pl);
        }
        //加注
        else if ((msg.getAction() & PokerConstants.ZJH_OPERTAION_JIA_ZHU) == PokerConstants.ZJH_OPERTAION_JIA_ZHU) {
            player_op_jiaZhu(gameData, gt, msg, pl);
        }
        else
            throw new RuntimeException("UnKnowOperation;");
    }

    private void resetNextPlayerOperation(GameData gameData, MJDesk<byte[]> desk ,int Substate) {
        // 等待客户端播动画
        gameData.setWaitingStartTime(System.currentTimeMillis());
        gameData.setPlaySubstate(Substate);
        // 顺序，轮到下一个玩家行动
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.getPokerOpPlayerIndex(), 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
    }

    private void player_op_jiaZhu(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--加注--");
        gameData.isJiaZhu = true;
        pl.danZhu = msg.getChouMa();
        gameData.zongZhu += pl.danZhu;
        gameData.danZhu = pl.isKanPai? ((pl.danZhu/2 == 5 && desk.getYaZhu() == PokerConstants.ZJH_ER_FEN_CAHNG) ? 4 : (pl.danZhu/2)) : pl.danZhu;
        pl.chouMa += pl.danZhu;

        for(Object o : desk.getPlayingPlayers()){
            PlayerInfo p = (PlayerInfo) o;
            if(p == null || p.isWait || p.isQiPai || p.isLose || p.position == pl.position) continue;
            p.danZhu = p.isKanPai?gameData.danZhu * 2 : gameData.danZhu;
            if(desk.getYaZhu() == PokerConstants.ZJH_YI_FEN_CAHNG || desk.getYaZhu() == PokerConstants.ZJH_ER_FEN_CAHNG) {
                int a = p.danZhu % 4;
                p.danZhu = (a == 0 && p.isKanPai) ? (p.danZhu/4) * 5 : p.danZhu;
            }
        }

        gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),-1,-1,pl.danZhu,pl.chouMa,gameData.zongZhu,-1,gameData.danZhu,gameData.lunNum);

        List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.getPokerOpPlayerIndex(), 1, 0);
        List<PlayerInfo> lunPlayer = desk.loopGetPlayerZJH(gameData.robIndex, 1, 0);
        if(nextPlayer.get(0).position == lunPlayer.get(0).position) gameData.lunNum ++;

        msg.setLunNum(gameData.lunNum);
        msg.setPlayerZongZhu(pl.chouMa);
        msg.setDeskZongZhu(gameData.zongZhu);
        msg.setPlayerDanZhu(pl.danZhu);
        mergePlayerData(desk,msg);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_ALL);
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    private void player_op_qiPai(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }

        if(pl.isQiPai){
            log.info("桌子id--"+desk.getDeskID()+"--玩家--"+pl.name+"========已经弃牌========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--弃牌--");
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.getPokerOpPlayerIndex(), 1, 0);
        List<PlayerInfo> lunPlayer = desk.loopGetPlayerZJH(gameData.robIndex, 1, 0);
        if(nextPlayer.size() > 0 && lunPlayer.size() > 0 &&
                nextPlayer.get(0).position == lunPlayer.get(0).position) gameData.lunNum++;

        pl.isQiPai = true;

        msg.setLunNum(gameData.lunNum);
        msg.setPlayerZongZhu(pl.chouMa);
        msg.setDeskZongZhu(gameData.zongZhu);
        msg.setPlayerDanZhu(pl.danZhu);
        mergePlayerData(desk,msg);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_ALL);
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),-1,-1,pl.danZhu,pl.chouMa,gameData.zongZhu,-1,gameData.danZhu,gameData.lunNum);

        if(!checkGameOver(gameData,desk)) resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    private void player_op_genZhu(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--跟注--");

        gameData.zongZhu += pl.danZhu;
        pl.chouMa += pl.danZhu;

        gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),-1,-1,pl.danZhu,pl.chouMa,gameData.zongZhu,-1,gameData.danZhu,gameData.lunNum);

        List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.getPokerOpPlayerIndex(), 1, 0);
        List<PlayerInfo> lunPlayer = desk.loopGetPlayerZJH(gameData.robIndex, 1, 0);
        if(nextPlayer.get(0).position == lunPlayer.get(0).position) gameData.lunNum ++;

        msg.setLunNum(gameData.lunNum);
        msg.setPlayerZongZhu(pl.chouMa);
        msg.setDeskZongZhu(gameData.zongZhu);
        msg.setPlayerDanZhu(pl.danZhu);
        mergePlayerData(desk,msg);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_ALL);
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    private void player_op_seeCard(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        int index = msg.getPosition();
        PlayerInfo plx = gameData.mPlayers[index];
        if (gameData.mPlayerCards.length <= index || gameData.mPlayerCards[index].cardsInHand.isEmpty()) return;
        if(plx.isKanPai){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========玩家已经看牌========");
            return;
        }
        if(plx.isLose){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========玩家已经输了不能看牌========");
            return;
        }

        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--看牌--");

        plx.isKanPai = true;
        plx.danZhu *= 2;

        if(desk.getYaZhu() == PokerConstants.ZJH_YI_FEN_CAHNG || desk.getYaZhu() == PokerConstants.ZJH_ER_FEN_CAHNG) {
            int a = plx.danZhu % 4;
            plx.danZhu = (a == 0 && plx.isKanPai) ? (plx.danZhu/4) * 5 : plx.danZhu;
        }

        gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),-1,-1,-1,pl.chouMa,gameData.zongZhu,-1,gameData.danZhu,gameData.lunNum);

//        if(index == gameData.getPokerOpPlayerIndex()) {
//            List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.getPokerOpPlayerIndex(), 1, 0);
//            List<PlayerInfo> lunPlayer = desk.loopGetPlayerZJH(gameData.robIndex, 1, 0);
//            if (nextPlayer.get(0).position == lunPlayer.get(0).position) gameData.lunNum++;
//        }

        msg.setLunNum(gameData.lunNum);
        msg.setPlayerZongZhu(pl.chouMa);
        msg.setDeskZongZhu(gameData.zongZhu);
        msg.setPlayerDanZhu(plx.danZhu);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_EXCEPT_ONE);

        for(Byte card : gameData.mPlayerCards[index].cardsInHand){
            msg.addCardValue(card);
        }
        msg.setCardType(getCardType(gameData.mPlayerCards[index].cardsInHand,desk));

        mergePlayerData(desk,msg);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_SINGLE);

        if(index == gameData.getPokerOpPlayerIndex()) player_chu_notify(gameData,desk);
    }

    private void mergePlayerData(MJDesk<byte[]> desk,ZJHGameOperPlayerActionSyn.Builder msg) {
        for(Object o : desk.getPlayingPlayers()){
            PlayerInfo p = (PlayerInfo) o;
            if(p == null || p.isWait || p.isQiPai || p.isLose) continue;
            ZJH.ZJHGameOperPlayerData.Builder builder = ZJH.ZJHGameOperPlayerData.newBuilder();
            builder.setPosition(p.position);
            builder.setPlayerDanZhu(p.danZhu);
            msg.addPlayerData(builder.build());
        }
    }


    private void player_op_biPai(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg, PlayerInfo pl) {
        int index = msg.getPosition();
        if (index != gameData.getPokerOpPlayerIndex()) {
            log.info("桌子id--"+desk.getDeskID()+"--"+"========没有轮到当前玩家操作========");
            return;
        }
        if(msg.getBiPaiPos()>=0 && gameData.mPlayers[msg.getBiPaiPos()].isLose){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========被比牌人已经输了不能再比========");
            return;
        }
        if(msg.getPosition()>=0 && gameData.mPlayers[msg.getPosition()].isLose){
            log.info("桌子id--"+desk.getDeskID()+"--"+"========比牌人已经输了不能再比========");
            return;
        }
        log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+pl.name+"--比牌--");

        pl.chouMa += pl.danZhu * (desk.canBiPaiJiaBei()? 2:1);
        gameData.zongZhu += pl.danZhu * (desk.canBiPaiJiaBei()? 2:1);
        ZJHBiPaiResult result = biPai(gameData,desk,msg);
        msg.setWinnerPos(result.winnerPos);

        PlayerInfo winPlayer = desk.getDeskPlayer(result.winnerPos);
        log.info("桌子id--"+desk.getDeskID()+"--"+"比牌结果--"+winPlayer.name+"--赢--座位号--"+winPlayer.position);

        gameData.mPlayers[msg.getPosition()].xiQian = result.xiQian;
        gameData.mPlayers[msg.getBiPaiPos()].xiQian = result.biPaiXiQian;
        
        gameData.mPlayers[msg.getPosition()].isLose = index != result.winnerPos;
        gameData.mPlayers[msg.getBiPaiPos()].isLose = msg.getBiPaiPos() != result.winnerPos;

        gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), pl.position, msg.getAction(),msg.getBiPaiPos(),result.winnerPos,-1,pl.chouMa,gameData.zongZhu,getCardType(gameData.mPlayerCards[pl.position].cardsInHand,desk),gameData.danZhu,gameData.lunNum);

        if(isGameOver(gameData,desk)) {
            msg.clearCardValue();
            for (Byte b : gameData.mPlayerCards[pl.position].cardsInHand) {
                msg.addCardValue(b);
            }
            msg.clearBiPaiCardValue();
            for (Byte b : gameData.mPlayerCards[msg.getBiPaiPos()].cardsInHand) {
                msg.addBiPaiCardValue(b);
            }
        }
        msg.setPlayerZongZhu(pl.chouMa);
        msg.setDeskZongZhu(gameData.zongZhu);
        msg.setPlayerDanZhu(pl.danZhu);
        mergePlayerData(desk,msg);
        ZJHPokerPushHelper.pushActionSyn(desk,index,msg,PokerConstants.SEND_TYPE_ALL);
        gameData.mPlayerAction[gameData.getPokerOpPlayerIndex()].opStartTime = 0L;

        if(!checkGameOver(gameData,desk)) resetNextPlayerOperation(gameData,desk, PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD);
    }

    /**
     * 是否能赢
     */
    private static ZJHBiPaiResult biPai(GameData gameData, MJDesk<byte[]> desk, ZJHGameOperPlayerActionSyn.Builder msg) {
        ZJHBiPaiResult result = new ZJHBiPaiResult();
        result.pos = msg.getPosition();
        result.biPaipos = msg.getBiPaiPos();
        result.cards = gameData.mPlayerCards[result.pos].cardsInHand;
        result.biPaiCards = gameData.mPlayerCards[result.biPaipos].cardsInHand;

        //两人牌都是从大到小排列的
        //先看没有王的情况
        int wangNum = getWangNum(result.cards);
        int biPaiWangNum = getWangNum(result.biPaiCards);
        if(wangNum+biPaiWangNum == 0){
            biPaiWithoutWang(desk.canXiQian(), desk.canDiLong(), desk.canShunThanJin(), desk.can235ThanBaoZi(), desk.can235ThanAAA(), result);
        } else {
            result.cardsInReal = getCardsInReal(result.cards,desk.canDiLong());
            result.biPaiCardsInReal = getCardsInReal(result.biPaiCards,desk.canDiLong());

            //看喜钱
            if(desk.canXiQian()) {
                for (Map.Entry<List<Byte>, Integer> entry : result.cardsInReal.entrySet()) {
                    if (entry.getValue() == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                        result.xiQian = 10;
                        break;
                    }
                    if (entry.getValue() == PokerConstants.ZJH_CARDTYPE_SHUN_JIN) {
                        result.xiQian = 5;
                        break;
                    }
                }
                for (Map.Entry<List<Byte>, Integer> entry : result.biPaiCardsInReal.entrySet()) {
                    if (entry.getValue() == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                        result.biPaiXiQian = 10;
                        break;
                    }
                    if (entry.getValue() == PokerConstants.ZJH_CARDTYPE_SHUN_JIN) {
                        result.biPaiXiQian = 5;
                        break;
                    }
                }
            }

            //双方各拼凑出最大的来比较
            result.cardsInRealMax = getCardsInRealMax(result.cardsInReal,desk.canXiQian(), desk.canDiLong(), desk.canShunThanJin(), desk.can235ThanBaoZi(), desk.can235ThanAAA());
            result.biPaiCardsInRealMax = getCardsInRealMax(result.biPaiCardsInReal,desk.canXiQian(), desk.canDiLong(), desk.canShunThanJin(), desk.can235ThanBaoZi(), desk.can235ThanAAA());
            ZJHBiPaiResult temp = new ZJHBiPaiResult();
            temp.pos = result.pos;
            temp.biPaipos = result.biPaipos;
            temp.cards = result.cardsInRealMax;
            temp.biPaiCards = result.biPaiCardsInRealMax;
            biPaiWithoutWang(desk.canXiQian(), desk.canDiLong(), desk.canShunThanJin(), desk.can235ThanBaoZi(), desk.can235ThanAAA(), temp);
            result.winnerPos = temp.winnerPos;

            //当两人牌一样时,有王者输,如果都有王,谁发起比牌谁输
            if(getType(result.cardsInRealMax) == getType(result.biPaiCardsInRealMax) && isEqualValue(result.cardsInRealMax,result.biPaiCardsInRealMax)){
                result.winnerPos = wangNum >= biPaiWangNum ? result.biPaipos : result.pos;
            }

        }
        return result;
    }

    public static void main(String[] args) {
        ZJHCardLogicTest logic = new ZJHCardLogicTest();
        List<Byte> list = new ArrayList<>();
        list.add((byte)81);
        list.add((byte)30);
        list.add((byte)13);
        List<Byte> list2 = new ArrayList<>();

        list2.add((byte)62);
        list2.add((byte)51);
        list2.add((byte)2);

        ZJHBiPaiResult temp = new ZJHBiPaiResult();
        temp.pos = 1;
        temp.biPaipos = 2;
        temp.cards = getCardsInRealMax(getCardsInReal(list,true), false, true, true, true, false);
        temp.biPaiCards = getCardsInRealMax(getCardsInReal(list2,true), false, true, true, true, false);
        biPaiWithoutWang(false, true, true, true, false, temp);
        System.out.println(temp.winnerPos);
    }

    private static boolean isEqualValue(List<Byte> cardsInRealMax, List<Byte> biPaiCardsInRealMax) {
        List<Byte> list1 = ZJHRule.modular(cardsInRealMax);
        List<Byte> list2 = ZJHRule.modular(biPaiCardsInRealMax);

        return list1 != null && list2 != null
                && list1.size() == 3 && list2.size() == 3
                && list1.get(0).equals(list2.get(0))
                && list1.get(1).equals(list2.get(1))
                && list1.get(2).equals(list2.get(2));
    }

    private static List<Byte> getCardsInRealMax(Map<List<Byte>, Integer> cardsInReal, boolean canXiQian,
                                                boolean canDiLong,
                                                boolean canShunThanJin,
                                                boolean can235ThanBaoZi,
                                                boolean can235ThanAAA) {
        List<Byte> max = new ArrayList<>();
        if(cardsInReal.size() == 0) return max;
        if(cardsInReal.size() == 1) return new ArrayList<>(cardsInReal.keySet()).get(0);
        ZJHBiPaiResult biPaiResult = new ZJHBiPaiResult();
        biPaiResult.pos = 1;
        biPaiResult.biPaipos = 2;

        List<List<Byte>> list = new ArrayList<>(cardsInReal.keySet());
        List<Byte>[] result = list.toArray(new List[list.size()]);
        int size = list.size();
        max = result[0];
//        for(int i = 0 ; i < size-1; i ++) {
//            for(int j = 0 ;j < size-1-i ; j++) {
//                biPaiResult.cards = result[j];
//                biPaiResult.biPaiCards = result[j+1];
//                biPaiWithoutWang(desk,biPaiResult);
//                if (biPaiResult.winnerPos == 2) {
//                    max = result[j+1];
//                }
//            }
//        }
        for (int i = 0; i < size; i++) {
            biPaiResult.cards = max;
            biPaiResult.biPaiCards = result[i];
            biPaiWithoutWang(canXiQian, canDiLong, canShunThanJin, can235ThanBaoZi, can235ThanAAA, biPaiResult);
            if (biPaiResult.winnerPos == 2) {
                max = biPaiResult.biPaiCards;
            }
        }
        return max;
    }

    private static void biPaiWithoutWang(
                                         boolean canXiQian,
                                         boolean canDiLong,
                                         boolean canShunThanJin,
                                         boolean can235ThanBaoZi,
                                         boolean can235ThanAAA,
                                         ZJHBiPaiResult result) {
        int type = getType(result.cards);
        int biPaitype = getType(result.biPaiCards);
        //看喜钱
        if (canXiQian) {
            if (type == PokerConstants.ZJH_CARDTYPE_SHUN_JIN) {
                result.xiQian = 5;
            }
            if (biPaitype == PokerConstants.ZJH_CARDTYPE_SHUN_JIN) {
                result.biPaiXiQian = 5;
            }
            if (type == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                result.xiQian = 10;
            }
            if (biPaitype == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                result.biPaiXiQian = 10;
            }
        }

        if (type == biPaitype) {//两人牌型相同
            result.winnerPos = compareThreeCard(result.cards, result.biPaiCards, type) ? result.pos : result.biPaipos;
            //考虑下地龙
            if (canDiLong && (type == PokerConstants.ZJH_CARDTYPE_SHUN_ZI || type == PokerConstants.ZJH_CARDTYPE_SHUN_JIN)) {
                if (is123(result.cards)) {//比牌人是123
                    if (isQKA(result.biPaiCards) || is123(result.biPaiCards)) {
                        result.winnerPos = result.biPaipos;
                    } else {
                        result.winnerPos = result.pos;
                    }
                } else {
                    if (is123(result.biPaiCards)) {//被比牌人是123
                        if (isQKA(result.cards)) {
                            result.winnerPos = result.pos;
                        } else {
                            result.winnerPos = result.biPaipos;
                        }
                    }
                }
            }
        } else if (type > biPaitype) {//比牌人牌型>被比牌人
            result.winnerPos = result.pos;

            //如果分别是金花,顺子
            if (canShunThanJin && type == PokerConstants.ZJH_CARDTYPE_JIN_HUA
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_SHUN_ZI) {
                result.winnerPos = result.biPaipos;
            }

            if (can235ThanBaoZi && type == PokerConstants.ZJH_CARDTYPE_BAO_ZI
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_DAN) {
                if (is235(result.biPaiCards)) result.winnerPos = result.biPaipos;
            }

            if (can235ThanAAA && type == PokerConstants.ZJH_CARDTYPE_BAO_ZI
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_DAN) {
                if (is235(result.biPaiCards) && isAAA(result.cards)) result.winnerPos = result.biPaipos;
            }
        } else {//比牌人牌型<被比牌人
            result.winnerPos = result.biPaipos;
            if (canShunThanJin && type == PokerConstants.ZJH_CARDTYPE_SHUN_ZI
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_JIN_HUA) {
                result.winnerPos = result.pos;
            }

            if (can235ThanBaoZi && type == PokerConstants.ZJH_CARDTYPE_DAN
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                if (is235(result.cards)) result.winnerPos = result.pos;
            }

            if (can235ThanAAA && type == PokerConstants.ZJH_CARDTYPE_DAN
                    && biPaitype == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                if (is235(result.cards) && isAAA(result.biPaiCards)) result.winnerPos = result.pos;
            }
        }
    }


    //变牌
    //先变A(与较大的那张牌变同色)
    //再变与较大的那张牌(看下是不是金花,是金花的花变金花)
    //再变顺子(与较大的一个变同色)
    private static Map<List<Byte>,Integer> getCardsInReal(List<Byte> cards, boolean canDiLong) {
        Set<List<Byte>> result = new HashSet<>();
        List<Byte> temp = new ArrayList<>(cards);
        temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG));
        temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG));
        
        List<Byte> realCards = new ArrayList<>(temp);
        int wangNum = getWangNum(cards);
        clearTempCard(cards, temp);
        for (int i = 0; i < wangNum; i++) {
            convertA(temp,realCards);
        }
        result.add(ZJHProcessor.sortHandCards(temp));
        clearTempCard(cards, temp);
        for (int i = 0; i < wangNum; i++) {
            convertBiggerCard(temp,realCards);
        }
        result.add(ZJHProcessor.sortHandCards(temp));
        clearTempCard(cards, temp);
        for (int i = 0; i < wangNum; i++) {
            temp = convertShunZi(temp,realCards,canDiLong);
        }
        if(temp.size() == 3) result.add(ZJHProcessor.sortHandCards(temp));
        Map<List<Byte>,Integer> map = new HashMap<>();
        for(List<Byte> list : result){
            map.put(list,getType(list));
        }
        return map;
    }

    private static List<Byte> convertShunZi(List<Byte> temp, List<Byte> realCards, boolean canDiLong) {
        temp = ZJHProcessor.sortHandCards(temp);
        List<Byte> list = ZJHRule.modular(temp);
        if(list.size() == 2){
            int color = temp.get(0) >> 4;
            int card1 = list.get(0);
            int card2 = list.get(1);
            if((card2 - card1) == 2 || (card2-card1) == 1){
                if((card2 + card1) == 5 && canDiLong){
                    temp.add((byte)(color << 4 | 14));
                } else {
                    if((card2-card1) == 1) {
                        if (card2 >= 14) {
                            temp.add((byte)(color << 4 | (card1 - 1)));
                        } else {
                            temp.add((byte)(color << 4 | (card2 + 1)));
                        }
                    }
                    else if((card2 - card1) == 2){
                        temp.add((byte)(color << 4 | (card1 + 1)));
                    }
                }
            } else if((card2 + card1) == 16){
                temp.add((byte)(color << 4 | 3));
            } else if((card2 + card1) == 17){
                temp.add((byte)(color << 4 | 2));
            }
        }else if(list.size() == 1){
            int color = temp.get(0) >> 4;
            int card = list.get(0);
            if(card == 14){
                temp.add((byte)(color << 4 | 13));
            }else if(card == 2){
                if(canDiLong){
                    temp.add((byte)(color << 4 | 14));
                }else {
                    temp.add((byte)(color << 4 | 3));
                }
            }else if(card == 3){
                if(canDiLong){
                    temp.add((byte)(color << 4 | 2));
                }else {
                    temp.add((byte)(color << 4 | 4));
                }
            }else{
                temp.add((byte)(color << 4 | (card + 1)));
            }
        }
        return temp;
    }

    private static void convertBiggerCard(List<Byte> temp, List<Byte> realCards) {
        byte maxCard = getMaxCardValue(realCards);
        List<Byte> list = ZJHRule.modular(realCards);
        if(list.size() == 2 && !list.get(0).equals(list.get(1))){
            int color1 = realCards.get(0) >> 4;
            int color2 = realCards.get(1) >> 4;

            if(color1 == color2){//凑金花
                if(maxCard == 14){
                    temp.add((byte)(color1 << 4 | 13));
                    return;
                }else{
                    temp.add((byte)(color1 << 4 | 14));
                    return;
                }
            }
        }
        temp.add(maxCard);
    }

    private static void clearTempCard(List<Byte> cards, List<Byte> temp) {
        temp.clear();
        temp.addAll(cards);
        temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG));
        temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG));
    }
    
    private static void convertA(List<Byte> temp, List<Byte> realCards) {
        byte maxCardColor = getMaxCardColor(realCards);
        temp.add((byte)(maxCardColor << 4 | 14));
    }

    private static byte getMaxCardValue(List<Byte> realCards) {
        List<Byte> list = ZJHRule.modular(realCards);
        if(realCards.size() == 3){
            return list.get(2);
        }else if(realCards.size() == 2){
            return list.get(1);
        }else if(realCards.size() == 1){
            return list.get(0);
        }else{
            log.error("---数量不对--");
            return 0;
        }
    }

    private static byte getMaxCardColor(List<Byte> realCards) {
        if(realCards.size() == 3){
            return (byte)(realCards.get(0) >> 4);
        }else if(realCards.size() == 2){
            return (byte)(realCards.get(0) >> 4);
        }else if(realCards.size() == 1){
            return (byte)(realCards.get(0) >> 4);
        }else{
            log.error("---数量不对--");
            return 0;
        }
    }

    private static boolean isQKA(List<Byte> cards) {
        List<Byte> list = ZJHRule.modular(cards);
        return list.size() == 3 && list.get(0) == 12 && list.get(1) == 13 && list.get(2) == 14;
    }

    private static boolean is123(List<Byte> cards) {
        List<Byte> list = ZJHRule.modular(cards);
        return list.size() == 3 && list.get(0) == 2 && list.get(1) == 3 && list.get(2) == 14;
    }

    private static int getWangNum(List<Byte> cards) {
        int num = 0;
        byte daWang = PokerConstants.POKER_CODE_DA_WANG;
        byte xiaoWang = PokerConstants.POKER_CODE_XIAO_WANG;
        if(cards.contains(daWang)) num++;
        if(cards.contains(xiaoWang)) num++;
        return num;
    }

    private static boolean isAAA(List<Byte> cards) {
        List<Byte> list = ZJHRule.modular(cards);
        return list.size() == 3 && list.get(0) == 14 && list.get(1) == 14 && list.get(2) == 14;
    }

    private static boolean is235(List<Byte> cards) {
        List<Byte> list = ZJHRule.modular(cards);
        return list.size() == 3 && list.get(0) == 2 && list.get(1) == 3 && list.get(2) == 5;
    }

    private static boolean compareThreeCard(List<Byte> cards, List<Byte> biPaiCards, int type) {
        int card1 = cards.get(0) & 0x0f;
        int card2 = cards.get(1) & 0x0f;
        int card3 = cards.get(2) & 0x0f;
        //考虑顺子123的情况
        if (card1 == 14 && card2 == 3 && card3 == 2) {
            card1 = 3;
            card2 = 2;
            card3 = 1;
        }

        int biPaiCard1 = biPaiCards.get(0) & 0x0f;
        int biPaiCard2 = biPaiCards.get(1) & 0x0f;
        int biPaiCard3 = biPaiCards.get(2) & 0x0f;

        //考虑顺子123的情况
        if (biPaiCard1 == 14 && biPaiCard2 == 3 && biPaiCard3 == 2) {
            biPaiCard1 = 3;
            biPaiCard2 = 2;
            biPaiCard3 = 1;
        }
        if (type == PokerConstants.ZJH_CARDTYPE_DUI_ZI) {
            int duiZi = (card1 == card2) ? card1 : ((card2 == card3) ? card2 : 0);
            int danPai = (card1 == card2) ? card3 : ((card2 == card3) ? card1 : 0);
            int biPaiduiZi = (biPaiCard1 == biPaiCard2) ? biPaiCard1 : ((biPaiCard2 == biPaiCard3) ? biPaiCard2 : 0);
            int biPaidanPan = (biPaiCard1 == biPaiCard2) ? biPaiCard3 : ((biPaiCard2 == biPaiCard3) ? biPaiCard1 : 0);
            return duiZi > biPaiduiZi || (duiZi == biPaiduiZi && danPai > biPaidanPan);
        }

        if (card1 > biPaiCard1) return true;
        if (card1 < biPaiCard1) return false;
        return card1 == biPaiCard1 && card2 > biPaiCard2 || card2 == biPaiCard2 && card3 > biPaiCard3;
    }

    private static int getType(List<Byte> cardsInHand) {
        if(checkBaoZi(cardsInHand)) return PokerConstants.ZJH_CARDTYPE_BAO_ZI;
        if(checkShunJin(cardsInHand)) return PokerConstants.ZJH_CARDTYPE_SHUN_JIN;
        if(checkJinHua(cardsInHand)) return PokerConstants.ZJH_CARDTYPE_JIN_HUA;
        if(checkShunZi(cardsInHand)) return PokerConstants.ZJH_CARDTYPE_SHUN_ZI;
        if(checkDuiZi(cardsInHand)) return PokerConstants.ZJH_CARDTYPE_DUI_ZI;
        return PokerConstants.ZJH_CARDTYPE_DAN;
    }

    private static boolean checkDuiZi(List<Byte> cardsInHand) {
        if(checkBaoZi(cardsInHand)) return false;
        int card1 = cardsInHand.get(0) & 0x0f;
        int card2 = cardsInHand.get(1) & 0x0f;
        int card3 = cardsInHand.get(2) & 0x0f;
        return card1 == card2 || card2 == card3 || card1 == card3;
    }

    private static boolean checkShunZi(List<Byte> list) {
        if(checkShunJin(list)) return false;
        int card1 = list.get(0) & 0x0f;
        int card2 = list.get(1) & 0x0f;
        int card3 = list.get(2) & 0x0f;
        return card1 == (card2 + 1) && card2 == (card3 + 1) || ((card1 + card2 + card3) == 19 && card1 == 14);
    }

    private static boolean checkJinHua(List<Byte> cardsInHand) {
        if(checkDuiZi(cardsInHand)) return false;
        if(checkShunJin(cardsInHand)) return false;
        int color1 = cardsInHand.get(0) >> 4;
        int color2 = cardsInHand.get(1) >> 4;
        int color3 = cardsInHand.get(2) >> 4;
        return color1 == color2 && color2 == color3;
    }

    private static boolean checkShunJin(List<Byte> list) {
        int card1 = list.get(0) & 0x0f;
        int card2 = list.get(1) & 0x0f;
        int card3 = list.get(2) & 0x0f;
        int color1 = list.get(0) >> 4;
        int color2 = list.get(1) >> 4;
        int color3 = list.get(2) >> 4;
        return color1 == color2 && color2 == color3 &&
                (card1 == (card2 + 1) && card2 == (card3 + 1) || ((card1 + card2 + card3) == 19) && card1 == 14);

    }

    private static boolean checkBaoZi(List<Byte> cardsInHand) {
        int card1 = cardsInHand.get(0) & 0x0f;
        int card2 = cardsInHand.get(1) & 0x0f;
        int card3 = cardsInHand.get(2) & 0x0f;
        return card1 == card2 && card2 == card3;
    }

    private boolean isGameOver(GameData gameData,MJDesk<byte[]> desk){
        int num = 0;
        for(PlayerInfo p : gameData.mPlayers){
            if(p == null) continue;
            if(p.isLose || p.isQiPai || p.isWait)
                num++;
        }
        return desk.getPlayerCount() == (num + 1);
    }

    private boolean checkGameOver(GameData gameData,MJDesk<byte[]> desk) {
        int num = 0;
        PlayerInfo winPlayer = new PlayerInfo();
        for(PlayerInfo p : gameData.mPlayers){
            if(p == null) continue;
            if(p.isLose || p.isQiPai || p.isWait)
                num++;
            else
                winPlayer = p;
        }
        if(desk.getPlayerCount() == (num + 1)) {//结束了
            log.info("桌子id--"+desk.getDeskID()+"--"+"--打牌结束,赢牌人--" + winPlayer.name);

            ZJH.ZJHGameOperPlayerActionNotify.Builder notify = ZJH.ZJHGameOperPlayerActionNotify.newBuilder();
            notify.setPosition(winPlayer.position);
            notify.setActions(PokerConstants.POKER_OPERTAION_GAME_OVER);
            //消息推送
            ZJHPokerPushHelper.pushActionNofity(gameData,desk,0,notify, PokerConstants.SEND_TYPE_ALL);

            gameData.recorder.recordZJHPlayerAction(gameData.genSeq(), winPlayer.position, PokerConstants.POKER_OPERTAION_GAME_OVER,-1,-1,-1,winPlayer.chouMa,gameData.zongZhu,-1,gameData.danZhu,gameData.lunNum);

            //设置胡牌人信息
            gameData.mGameWin.position = winPlayer.position;
            gameData.handEndTime = System.currentTimeMillis();

            // 结算番型和金币
            settlement(gameData, desk, winPlayer);

            //设置下一个庄家的下标
            gameData.robIndex = gameData.mGameWin.position;

            //设置一局结束的状态,循环获取状态后结束这局游戏
            gameData.setState(PokerConstants.GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN);

            return true;
        }

        return false;
    }

    /**
     *  计算喜钱
     *  赢的人拿总注
     */
    private void settlement(GameData gameData, MJDesk<byte[]> desk, PlayerInfo winPlayer) {
        gameData.zjhResult.endTime = System.currentTimeMillis();
        gameData.zjhResult.juNum = gameData.handNum;
        gameData.zjhResult.zongZhu = gameData.zongZhu;
        gameData.zjhResult.winnerIndex = winPlayer.position;

        int  playerCount = desk.getPlayingPlayers().size();

        int score = 0;
        for (PlayerInfo px : desk.getPlayingPlayers()) {
            gameData.zjhResult.Result.get(px.playerId).cardType = getCardType(gameData.mPlayerCards[px.position].cardsInHand,desk);
            if (gameData.zjhResult.Result.get(px.playerId).cardType == PokerConstants.ZJH_CARDTYPE_SHUN_JIN) {
                px.xiQian = 5;
            }
            if (gameData.zjhResult.Result.get(px.playerId).cardType == PokerConstants.ZJH_CARDTYPE_BAO_ZI) {
                px.xiQian = 10;
            }
        }
        for (PlayerInfo px : desk.getPlayingPlayers()) {
            gameData.zjhResult.Result.get(px.playerId).pos = px.position;
            gameData.zjhResult.Result.get(px.playerId).playerId = px.playerId;
            gameData.zjhResult.Result.get(px.playerId).playerName = px.name;
            gameData.zjhResult.Result.get(px.playerId).lastScore = px.score;
            gameData.zjhResult.Result.get(px.playerId).isKanPai = px.isKanPai;
            gameData.zjhResult.Result.get(px.playerId).isBanker = px.position == gameData.robIndex;
            gameData.zjhResult.Result.get(px.playerId).result = px.position == winPlayer.position ?PokerConstants.GAME_RESULT_WIN : PokerConstants.GAME_RESULT_LOSE;
            int otherPlayerXiQian = 0;
            for (PlayerInfo player : desk.getPlayingPlayers()) {
                if(player.position != px.position){
                    otherPlayerXiQian += player.xiQian;
                }
            }
            int xiQian = px.xiQian * (playerCount-1);
            px.realXiQian = xiQian - otherPlayerXiQian;

            gameData.zjhResult.Result.get(px.playerId).xiQian = px.realXiQian;
            if (px.position != winPlayer.position) {
                gameData.zjhResult.Result.get(px.playerId).score = (px.realXiQian-px.chouMa);
                if(desk.isClubJiFenDesk() && desk.getCanFufen() == 1 &&
                        (gameData.zjhResult.Result.get(px.playerId).score+ gameData.zjhResult.Result.get(px.playerId).allSocre) <0 ){
                    gameData.zjhResult.Result.get(px.playerId).score = -gameData.zjhResult.Result.get(px.playerId).allSocre;
                }
                px.curJuScore = gameData.zjhResult.Result.get(px.playerId).score;
                score -= gameData.zjhResult.Result.get(px.playerId).score;
                gameData.zjhResult.Result.get(px.playerId).allSocre += gameData.zjhResult.Result.get(px.playerId).score;
                gameData.zjhFinalResult.finalResults.get(px.playerId).allScore = gameData.zjhResult.Result.get(px.playerId).allSocre;
                gameData.zjhFinalResult.finalResults.get(px.playerId).score += gameData.zjhResult.Result.get(px.playerId).score;

                gameData.zjhFinalResult.finalResults.get(px.playerId).loseNum += 1;
                gameData.zjhResult.Result.get(px.playerId).maxScore =
                        (gameData.zjhResult.Result.get(px.playerId).score > gameData.zjhResult.Result.get(px.playerId).maxScore)?
                                gameData.zjhResult.Result.get(px.playerId).score : gameData.zjhResult.Result.get(px.playerId).maxScore;
                gameData.zjhResult.Result.get(px.playerId).maxCardType =
                        (gameData.zjhResult.Result.get(px.playerId).cardType > gameData.zjhResult.Result.get(px.playerId).maxCardType)?
                                gameData.zjhResult.Result.get(px.playerId).cardType : gameData.zjhResult.Result.get(px.playerId).maxCardType;
                gameData.zjhFinalResult.finalResults.get(px.playerId).maxScore = gameData.zjhResult.Result.get(px.playerId).maxScore;
                gameData.zjhFinalResult.finalResults.get(px.playerId).maxCardType = gameData.zjhResult.Result.get(px.playerId).maxCardType;
                gameData.zjhResult.Result.get(px.playerId).isQiPai = px.isQiPai;
            }else{
                gameData.zjhFinalResult.finalResults.get(px.playerId).winNum += 1;
            }
        }

        winPlayer.curJuScore = score;
        gameData.zjhResult.Result.get(winPlayer.playerId).score = score;
        gameData.zjhResult.Result.get(winPlayer.playerId).allSocre = gameData.zjhResult.Result.get(winPlayer.playerId).lastScore+score;
        gameData.zjhFinalResult.finalResults.get(winPlayer.playerId).allScore = gameData.zjhResult.Result.get(winPlayer.playerId).allSocre;
        gameData.zjhFinalResult.finalResults.get(winPlayer.playerId).score += score;

        gameData.zjhResult.Result.get(winPlayer.playerId).maxScore =
                (gameData.zjhResult.Result.get(winPlayer.playerId).score > gameData.zjhResult.Result.get(winPlayer.playerId).maxScore)?
                        gameData.zjhResult.Result.get(winPlayer.playerId).score : gameData.zjhResult.Result.get(winPlayer.playerId).maxScore;
        gameData.zjhResult.Result.get(winPlayer.playerId).maxCardType =
                (gameData.zjhResult.Result.get(winPlayer.playerId).cardType > gameData.zjhResult.Result.get(winPlayer.playerId).maxCardType)?
                        gameData.zjhResult.Result.get(winPlayer.playerId).cardType : gameData.zjhResult.Result.get(winPlayer.playerId).maxCardType;
        gameData.zjhFinalResult.finalResults.get(winPlayer.playerId).maxScore = gameData.zjhResult.Result.get(winPlayer.playerId).maxScore;
        gameData.zjhFinalResult.finalResults.get(winPlayer.playerId).maxCardType = gameData.zjhResult.Result.get(winPlayer.playerId).maxCardType;

        for (PokerZJHResult r : gameData.zjhResult.Result.values()) {
            if(r.playerName == null) continue;
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+r.playerName+"--当前牌型--"+r.cardType);
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+r.playerName+"--当前分数--"+r.score);
        }
        for (PokerZJHFinalResult r : gameData.zjhFinalResult.finalResults.values()) {
            if(r.playerName == null) continue;
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+r.playerName+"--最大牌型--"+r.maxCardType);
            log.info("桌子id--"+desk.getDeskID()+"--"+"玩家--"+r.playerName+"--最大分数--"+r.maxScore);
        }
    }

    private int getCardType(List<Byte> cardsInHand, MJDesk<byte[]> desk) {
        if(cardsInHand.contains(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG))
                || cardsInHand.contains(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG))){
            return getType(getCardsInRealMax(getCardsInReal(cardsInHand,desk.canDiLong()),desk.canXiQian(), desk.canDiLong(), desk.canShunThanJin(), desk.can235ThanBaoZi(), desk.can235ThanAAA()));
        } else {
           return getType(cardsInHand);
        }
    }


    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, PlayerInfo info) {

    }

    @Override
    public void repushGameData(GameData gameData, MJDesk<byte[]> desk, int position) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"act=repushGameData; position={};deskId={};", position, desk.getDeskID());
        //把当前桌子的状况发给重连玩家
        ZJH.ZJHGameOperStartSyn.Builder zjh = ZJH.ZJHGameOperStartSyn.newBuilder();
        zjh.setJuNum(gameData.handNum);//桌子当前圈数
        zjh.setBankerPos(gameData.robIndex);//当前地主的下标
        zjh.setReconnect(true);//表示断线重连
        zjh.setSeq(gameData.gameSeq);
        zjh.setLunNum(gameData.lunNum);
        zjh.setZongZhu(gameData.zongZhu);
        gameData.recorder.seq = zjh.getSeq(); // 记录序列号

        PlayerInfo p = desk.getDeskPlayer(position);
        for (PlayerInfo pl : desk.getPlayers()) {
            boolean showHandCardVal = (p.position == pl.position && pl.isKanPai)
                    || (gameData.fuliPlayerMap.containsKey(p.playerId));
            ZJH.ZJHGameOperHandCardSyn.Builder dz = ZJH.ZJHGameOperHandCardSyn.newBuilder();
            if(!pl.isWait) {
                for (int card : gameData.getCardsInHand(pl.position)) {
                    dz.addHandCards(showHandCardVal ? card : -1);//如果是掉线玩家就给牌 否则是-1
                }
            }
            if(gameData.zjhFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.zjhFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            dz.setPosition(pl.position);
            dz.setChouMa(pl.chouMa);
            dz.setIsKanPai(pl.isKanPai);
            dz.setIsQiPai(pl.isQiPai);
            dz.setIsWait(pl.isWait);
            dz.setCanKanPai(gameData.lunNum > desk.getMenNum() && !p.isKanPai);
            dz.setPlayerDanZhu(pl.danZhu);
            zjh.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.ZJHGameOperStartSyn);
        gb.setContent(zjh.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"重连,发送消息为---"+ JsonFormat.printToString(zjh.build()));

        desk.sendMsg2Player(p.position, gb.build().toByteArray());
        // 发送公告信息
        ZJHPokerPushHelper.pushPublicInfoMsg2Single(desk, position, gameData);

        // 发送当前操作人
        ZJHPokerPushHelper.pushActorSyn(desk, position, gameData.getPokerOpPlayerIndex(), 15, PokerConstants.SEND_TYPE_SINGLE);

        // 如果是当前人操作，重新通知他进行操作
        this.re_notify_current_operation_player(gameData, desk, position);
    }


    @Override
    public void pushDeskInfo(GameData mGameData, MJDesk<byte[]> desk, PlayerInfo p) {
        log.info("桌子id--"+desk.getDeskID()+"--"+"推送桌子信息给新来玩家");

        //把当前桌子的状况发给重连玩家
        ZJH.ZJHGameOperStartSyn.Builder zjh = ZJH.ZJHGameOperStartSyn.newBuilder();
        zjh.setJuNum(gameData.handNum);//桌子当前圈数
        zjh.setBankerPos(gameData.robIndex);//当前地主的下标
        zjh.setReconnect(true);//表示断线重连
        zjh.setSeq(gameData.gameSeq);
        zjh.setLunNum(gameData.lunNum);
        zjh.setZongZhu(gameData.zongZhu);
        gameData.recorder.seq = zjh.getSeq(); // 记录序列号

        for (PlayerInfo pl : desk.getPlayers()) {
            ZJH.ZJHGameOperHandCardSyn.Builder dz = ZJH.ZJHGameOperHandCardSyn.newBuilder();
            if(gameData.zjhFinalResult.finalResults.get(pl.playerId) != null) {
                dz.setSocre(gameData.zjhFinalResult.finalResults.get(pl.playerId).allScore);//设置玩家的分数
            }
            dz.setPosition(pl.position);
            dz.setChouMa(pl.chouMa);
            dz.setIsKanPai(pl.isKanPai);
            dz.setIsQiPai(pl.isQiPai);
            dz.setIsWait(pl.isWait);
            dz.setCanKanPai(gameData.lunNum > desk.getMenNum() && !pl.isKanPai);
            dz.setPlayerDanZhu(pl.danZhu);
            zjh.addPlayerHandCards(dz);
        }

        MJBase.GameOperation.Builder gb = MJBase.GameOperation.newBuilder();
        gb.setOperType(MJBase.GameOperType.ZJHGameOperStartSyn);
        gb.setContent(zjh.build().toByteString());
        gb.setType(1);
        log.info("桌子id--"+desk.getDeskID()+"--"+"--玩家--"+p.name+"新加入,发送消息为---"+ JsonFormat.printToString(zjh.build()));

        desk.sendMsg2Player(p, gb.build().toByteArray());
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
                case PokerConstants.POKER_TABLE_SUB_STATE_CHU_CARD:
                    //提醒玩家重新出
                    log.info("桌子id--"+desk.getDeskID()+"--"+"提醒玩家重新出");
                    player_chu_notify(gameData, desk);
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
        List<PlayerInfo> nextPlayer = desk.loopGetPlayerZJH(gameData.robIndex, 1, 0);
        gameData.setPokerOpPlayerIndex(nextPlayer.get(0).position);
    }

    private List<Byte> chineseName2CardList(String name){
        String[] cardListChinese = name.split(" ");
        List<Byte> cardList = new ArrayList<>();
        for(String card : cardListChinese){
            cardList.add(ZJHHelper.singleCardMapChinese.get(card));
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
