package com.buding.poker.helper;

import packet.erba.ErBa;
import packet.mj.MJBase;

/**
 *
 * @author chen 2017 - 12 - 20
 *
 */
public class ErBaMsgBuilder {

    /**
     * 桌子同步数据
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPublicInfo (ErBa.ErBaGameOperPublicInfoSyn.Builder ErBa) {


        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperPublicInfoSyn);
        return tt;
    }

    /**
     *
     *手牌
     *
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForHandcardSyn (ErBa.ErBaGameOperHandCardSyn.Builder ErBa) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperHandCardSyn);
        return tt;
    }

    /**
     *
     * 通知玩家操作
     *
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionNofity (ErBa.ErBaGameOperPlayerActionNotify.Builder ErBa) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperPlayerActionNotify);
        return tt;
    }

    /**
     *
     *客户端请求服务器某个操作(出,过,抢地主,加倍等)，服务器向其他人同步玩家的这个操作也是用这个编码
     *
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionSyn (ErBa.ErBaGameOperPlayerActionSyn.Builder ErBa) {

        MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperPlayerActionSyn);
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
    public static  MJBase.GameOperation.Builder getPacketForActorSyn (int playerId, int timeout) {
       ErBa.ErBaGameOperActorSyn.Builder ctt = ErBa.ErBaGameOperActorSyn.newBuilder();
        ctt.setPlayerId(playerId);
        ctt.setTimeLeft(timeout);

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ctt.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperActorSyn);
        return tt;
    }

    /**
     *
     * 单局结算
     *
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPlayerWin (ErBa.ErBaGameOperPlayerHuSyn.Builder ErBa) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperPlayerHuSyn);
        return tt;
    }

    /**
     *
     * 总结算
     *
     * @param ErBa
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForFinalSettle (ErBa.ErBaGameOperFinalSettleSyn.Builder ErBa) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ErBa.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ErBaGameOperFinalSettleSyn);
        return tt;
    }




}
