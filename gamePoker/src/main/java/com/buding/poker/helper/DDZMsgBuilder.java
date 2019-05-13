package com.buding.poker.helper;

import packet.ddz.DDZ;
import packet.mj.MJBase;

import java.util.List;

/**
 *
 * @author chen 2017 - 12 - 20
 *
 */
public class DDZMsgBuilder {
    /**
     *
     *
     * @param cardLeft 剩下的底牌
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPublicInfo (List<Byte> cardLeft) {
        DDZ.DDZGameOperPublicInfoSyn.Builder ddz = DDZ.DDZGameOperPublicInfoSyn.newBuilder();
        for (Byte card : cardLeft) {
            ddz.addCardLeft(card);
        }

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperPublicInfoSyn);
        return tt;
    }

    /**
     *
     *手牌
     *
     * @param ddz
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForHandcardSyn (DDZ.DDZGameOperHandCardSyn.Builder ddz) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperHandCardSyn);
        return tt;
    }

    /**
     *
     * 通知玩家操作
     *
     * @param ddz
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionNofity (DDZ.DDZGameOperPlayerActionNotify.Builder ddz) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperPlayerActionNotify);
        return tt;
    }

    /**
     *
     *客户端请求服务器某个操作(出,过,抢地主,加倍等)，服务器向其他人同步玩家的这个操作也是用这个编码
     *
     * @param ddz
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForActionSyn (DDZ.DDZGameOperPlayerActionSyn.Builder ddz) {

        MJBase.GameOperation.Builder tt = MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperPlayerActionSyn);
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
       DDZ.DDZGameOperActorSyn.Builder ctt = DDZ.DDZGameOperActorSyn.newBuilder();
        ctt.setPosition(position);
        ctt.setTimeLeft(timeout);

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ctt.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperActorSyn);
        return tt;
    }

    /**
     *
     * 单局结算
     *
     * @param ddz
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForPlayerWin (DDZ.DDZGameOperPlayerHuSyn.Builder ddz) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperPlayerHuSyn);
        return tt;
    }

    /**
     *
     * 总结算
     *
     * @param ddz
     * @return
     */
    public static  MJBase.GameOperation.Builder getPacketForFinalSettle (DDZ.DDZGameOperFinalSettleSyn.Builder ddz) {

        MJBase.GameOperation.Builder tt =  MJBase.GameOperation.newBuilder();
        tt.setContent(ddz.build().toByteString());
        tt.setOperType( MJBase.GameOperType.DDZGameOperFinalSettleSyn);
        return tt;
    }




}
