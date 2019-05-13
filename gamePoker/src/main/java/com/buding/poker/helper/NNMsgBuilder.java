package com.buding.poker.helper;

import packet.mj.MJBase;
import packet.nn.NN;

import java.util.List;

/**
 *
 * @author chen 2017 - 12 - 20
 *
 */
public class NNMsgBuilder {

    /**
     *
     *手牌
     *
     * @param NN
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForHandcardSyn (NN.NNGameOperHandCardSyn.Builder NN) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(NN.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperHandCardSyn);
        return tt;
    }

    /**
     *
     * 通知玩家操作
     *
     * @param NN
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionNofity (NN.NNGameOperPlayerActionNotify.Builder NN) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(NN.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperPlayerActionNotify);
        return tt;
    }

    /**
     *
     *客户端请求服务器某个操作(出,过,抢地主,加倍等)，服务器向其他人同步玩家的这个操作也是用这个编码
     *
     * @param NN
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionSyn (NN.NNGameOperPlayerActionSyn.Builder NN) {

        MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
        tt.setContent(NN.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperPlayerActionSyn);
        return tt;
    }

    /**
     *
     *广播当前正在操作的玩家
     *
     * @param position
     * @param timeout  大于10 表示超时
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActorSyn (int position, int timeout) {
       NN.NNGameOperActorSyn.Builder ctt = NN.NNGameOperActorSyn.newBuilder();
        ctt.setPosition(position);
        ctt.setTimeLeft(timeout);

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ctt.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperActorSyn);
        return tt;
    }

    /**
     *
     * 单局结算
     *
     * @param NN
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPlayerWin (NN.NNGameOperPlayerHuSyn.Builder NN) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(NN.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperPlayerHuSyn);
        return tt;
    }

    /**
     *
     * 总结算
     *
     * @param NN
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForFinalSettle (NN.NNGameOperFinalSettleSyn.Builder NN) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(NN.build().toByteString());
        tt.setOperType( MJBase.GameOperType.NNGameOperFinalSettleSyn);
        return tt;
    }




}
