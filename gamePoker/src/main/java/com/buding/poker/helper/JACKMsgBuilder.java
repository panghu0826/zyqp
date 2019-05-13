package com.buding.poker.helper;

import packet.mj.MJBase;
import packet.jack.JACK;

import java.util.List;

/**
 *
 * @author chen 2017 - 12 - 20
 *
 */
public class JACKMsgBuilder {

    /**
     * 桌子同步数据
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPublicInfo (JACK.JACKGameOperPublicInfoSyn.Builder JACK) {


        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperPublicInfoSyn);
        return tt;
    }

    /**
     *
     *手牌
     *
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForHandcardSyn (JACK.JACKGameOperHandCardSyn.Builder JACK) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperHandCardSyn);
        return tt;
    }

    /**
     *
     * 通知玩家操作
     *
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionNofity (JACK.JACKGameOperPlayerActionNotify.Builder JACK) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperPlayerActionNotify);
        return tt;
    }

    /**
     *
     *客户端请求服务器某个操作(出,过,抢地主,加倍等)，服务器向其他人同步玩家的这个操作也是用这个编码
     *
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionSyn (JACK.JACKGameOperPlayerActionSyn.Builder JACK) {

        MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperPlayerActionSyn);
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
       JACK.JACKGameOperActorSyn.Builder ctt = JACK.JACKGameOperActorSyn.newBuilder();
        ctt.setPosition(position);
        ctt.setTimeLeft(timeout);

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ctt.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperActorSyn);
        return tt;
    }

    /**
     *
     * 单局结算
     *
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPlayerWin (JACK.JACKGameOperPlayerHuSyn.Builder JACK) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperPlayerHuSyn);
        return tt;
    }

    /**
     *
     * 总结算
     *
     * @param JACK
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForFinalSettle (JACK.JACKGameOperFinalSettleSyn.Builder JACK) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(JACK.build().toByteString());
        tt.setOperType( MJBase.GameOperType.JACKGameOperFinalSettleSyn);
        return tt;
    }




}
