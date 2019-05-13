package com.buding.hall.network.cmd;

import com.buding.common.result.TResult;
import com.buding.db.model.User;
import com.buding.db.model.UserOrder;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.constants.Constants;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.order.dao.UserOrderDao;
import com.buding.hall.module.shop.OrderStatus;
import com.buding.hall.module.shop.service.ShopService;
import com.buding.hall.module.user.dao.UserDao;
import com.google.protobuf.ByteString;
import com.ifp.wechat.entity.user.UserWeiXin;
import com.ifp.wechat.service.OAuthService;
import com.ifp.wechat.util.FenXiaoUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase;
import packet.game.Hall.PocketPay;
import packet.msgbase.MsgBase.PacketType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class PocketPayCmd extends HallCmd{
    protected Logger logger = LogManager.getLogger(getClass());

    @Autowired
    ShopService shopService;

    @Autowired
    protected HallPushHelper pushHelper;

    @Autowired
    ConfigManager configManager;

    @Autowired
    UserOrderDao userOrderDao;

    @Autowired
    UserDao userDao;

    @Autowired
    ItemService itemService;

    @Override
    public void execute(CmdData data) throws Exception {
        PocketPay req = PocketPay.parseFrom(data.packet.getData());
        int userId = req.getPlayerId();

        String orderId = shopService.genOrderId(userId + "-pocket-");

        if (orderId == null) {
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "生成订单失败");
            return;
        }

        String type = Integer.valueOf(req.getProductId())<31009?"0":"1";
        ProductConfig conf = configManager.getItemConf(req.getProductId());
        User user = userDao.getUser(userId);
        Map<String, String> m = new HashMap<>();
        m.put("unionid", user.getWxunionid());
        m.put("money", conf.price.currenceCount*100+"");
        m.put("pay_style", "pocketpay");
        m.put("item_type", type);
        JSONObject result = FenXiaoUtil.pay(m);
        boolean isSuccess = !result.isEmpty()&& StringUtils.equals(result.getString("code"),"200");
        if(isSuccess){
            if(StringUtils.equals(req.getProductId(),"31014")){
                user.setShouchong2(1);
                userDao.updateUser(user);
                Hall.ShouChongSyn.Builder syn = Hall.ShouChongSyn.newBuilder();
                syn.setShouchong1(user.getShouchong1());
                syn.setShouchong2(user.getShouchong2());
                syn.setShouchong3(user.getShouchong3()==null ? 0 :1);
                pushHelper.pushPBMsg(userId, MsgBase.PacketType.ShouChongSyn,syn.build().toByteString());
            }else if(StringUtils.equals(req.getProductId(),"31015")){
                user.setShouchong3(1);
                userDao.updateUser(user);
                Hall.ShouChongSyn.Builder syn = Hall.ShouChongSyn.newBuilder();
                syn.setShouchong1(user.getShouchong1());
                syn.setShouchong2(user.getShouchong2());
                syn.setShouchong3(user.getShouchong3()==null ? 0 :1);
                pushHelper.pushPBMsg(userId, MsgBase.PacketType.ShouChongSyn,syn.build().toByteString());
            }
            itemService.addItem(data.session.userId, ItemChangeReason.UserBuyItem, "UserBuy:" + orderId, conf.items);
            for (ItemPkg d : conf.items) {
                logger.info("playerId={};id={};itemType={};args={};count={};act=addItemOk;", data.session.userId, orderId, d.baseConf.itemType, d.baseConf.getArgument(), d.count);
            }
        }else{
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, StringUtils.isBlank(result.getString("message"))?"系统繁忙":result.getString("message"));
        }
        int orderStatus = isSuccess?OrderStatus.PAY:OrderStatus.FAIL;
        UserOrder order = new UserOrder();
        order.setCtime(new Date());
        order.setMtime(new Date());
        order.setOrderId(orderId);
        order.setOrderStatus(orderStatus);
        order.setProductId(req.getProductId());
        order.setUserId(userId);
        ProductConfig config = configManager.getItemConf(req.getProductId());
        order.setPrice(Double.valueOf(config.price.currenceCount));
        userOrderDao.insert(order);

    }

    @Override
    public MsgBase.PacketType getKey() {
        return PacketType.PocketPay;
    }
}

