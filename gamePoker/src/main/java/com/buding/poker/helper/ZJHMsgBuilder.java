package com.buding.poker.helper;

import packet.zjh.ZJH;
import packet.mj.MJBase;

import java.util.List;

/**
 *
 * @author chen 2017 - 12 - 20
 *
 */
public class ZJHMsgBuilder {
    /**
     *
     *
     * @param cardLeft 剩下的底牌
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPublicInfo (List<Byte> cardLeft) {
        ZJH.ZJHGameOperPublicInfoSyn.Builder ZJH =  packet.zjh.ZJH.ZJHGameOperPublicInfoSyn.newBuilder();
        for (Byte card : cardLeft) {
            ZJH.addCardLeft(card);
        }

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperPublicInfoSyn);
        return tt;
    }

    /**
     *
     *手牌
     *
     * @param ZJH
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForHandcardSyn (ZJH.ZJHGameOperHandCardSyn.Builder ZJH) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperHandCardSyn);
        return tt;
    }

    /**
     *
     * 通知玩家操作
     *
     * @param ZJH
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionNofity (ZJH.ZJHGameOperPlayerActionNotify.Builder ZJH) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperPlayerActionNotify);
        return tt;
    }

    /**
     *
     *客户端请求服务器某个操作(出,过,抢地主,加倍等)，服务器向其他人同步玩家的这个操作也是用这个编码
     *
     * @param ZJH
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionSyn (ZJH.ZJHGameOperPlayerActionSyn.Builder ZJH) {

        MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperPlayerActionSyn);
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
       ZJH.ZJHGameOperActorSyn.Builder ctt = ZJH.ZJHGameOperActorSyn.newBuilder();
        ctt.setPosition(position);
        ctt.setTimeLeft(timeout);

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ctt.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperActorSyn);
        return tt;
    }

    /**
     *
     * 单局结算
     *
     * @param ZJH
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPlayerWin (ZJH.ZJHGameOperPlayerHuSyn.Builder ZJH) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperPlayerHuSyn);
        return tt;
    }

    /**
     *
     * 总结算
     *
     * @param ZJH
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForFinalSettle (ZJH.ZJHGameOperFinalSettleSyn.Builder ZJH) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ZJH.build().toByteString());
        tt.setOperType( MJBase.GameOperType.ZJHGameOperFinalSettleSyn);
        return tt;
    }




}
