package com.buding.hall.module.schedule;

import com.buding.common.cache.RedisClient;
import com.buding.db.model.Award;
import com.buding.db.model.Msg;
import com.buding.db.model.User;
import com.buding.db.model.UserMsg;
import com.buding.hall.config.ItemPkg;
import com.buding.hall.module.award.service.AwardService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.ws.HallPortalService;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSession;
import com.buding.task.network.TaskSessionManager;
import com.google.gson.Gson;
import com.ifp.wechat.util.FenXiaoUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import packet.game.Hall;
import packet.msgbase.MsgBase;

import java.util.*;

public class SignAndLunPanSch {
    private static HallPortalService hallPortalService;
    private static int id1 = 54321;
    private static int id2 = 34321;
    private static int id3 = 24321;
    @Autowired
    UserService userService;
    @Autowired
    UserDao userDao;
    @Autowired
    TaskSessionManager taskSessionManager;
    @Autowired
    TaskPushHelper pushHelper;
    @Autowired
    RedisClient redisClient;
    @Autowired
    AwardService awardService;
    @Autowired
    MsgDao msgDao;
    @Autowired
    MsgPortalService portal;
    private Logger logger = LogManager.getLogger(getClass());

    private static void test0() {
        Map<String, String> m = new HashMap<>();
        m.put("unionid", "onnkgwEEIjEn1ld0xSocMtpafX7A");
        m.put("money", "1000");
        m.put("pay_style", "wxpay");
        m.put("item_type", "1");
        m.put("order_num", "4319-wx-1511144274055");
        try {
            JSONObject jsonObject = FenXiaoUtil.pay(m);
        } catch (Exception e) {
            System.out.println("啦啦啦阿---------");
        }
    }

    private static void test1() {
        Map<String, String> m = new HashMap<>();
        m.put("unionid", "onnkgwEEIjEn1ld0xSocMtpafX7A");
        m.put("money", "1000");
        m.put("pay_style", "wxpay");
        m.put("item_type", "1");
        m.put("order_num", "4319-wx-1511144274055");
        try {
            JSONObject jsonObject = FenXiaoUtil.pay(m);
        } catch (Exception e) {
            System.out.println("啦啦啦阿---------");
        }
    }

    private static void test2() {
        Map<String, String> m = new HashMap<>();
        m.put("unionid", "onnkgwEEIjEn1ld0xSocMtpafX7A");
        m.put("money", "1000");
        m.put("pay_style", "wxpay");
        m.put("item_type", "1");
        m.put("order_num", "4319-wx-1511144274055");
        try {
            JSONObject jsonObject = FenXiaoUtil.pay(m);
        } catch (Exception e) {
            System.out.println("啦啦啦阿---------");
        }
    }

    private static void test3() {
//        System.out.println("模拟充值初始化----------");
//        ApplicationConfig app = new ApplicationConfig();
//        app.setName("BattleServerConsumer");
//        RegistryConfig registry = new RegistryConfig();
//        registry.setAddress("zookeeper://"+"192.168.0.14"+":2181");
//        ReferenceConfig<HallPortalService> reference = new ReferenceConfig<HallPortalService>();
//        reference.setApplication(app);
//        reference.setRegistry(registry);
//        reference.setInterface(HallPortalService.class);
//        hallPortalService = reference.get();
//
//        {
//            UserOrder order = new UserOrder();
//            order.setCtime(new Date());
//            order.setMtime(new Date());
//
//            order.setOrderId((++id1) + "");
//            order.setOrderStatus(OrderStatus.WAITING);
//            order.setProductId("31009");
//            order.setUserId(9787);
//            order.setPrice(1000.0);
//            hallPortalService.insertOrdertest(order);
//        }
//        {
//            UserOrder order = new UserOrder();
//            order.setCtime(new Date());
//            order.setMtime(new Date());
//
//            order.setOrderId((++id2) + "");
//            order.setOrderStatus(OrderStatus.WAITING);
//            order.setProductId("31009");
//            order.setUserId(9786);
//            order.setPrice(1000.0);
//            hallPortalService.insertOrdertest(order);
//        }
//        {
//            UserOrder order = new UserOrder();
//            order.setCtime(new Date());
//            order.setMtime(new Date());
//
//            order.setOrderId((++id3) + "");
//            order.setOrderStatus(OrderStatus.WAITING);
//            order.setProductId("31009");
//            order.setUserId(9785);
//            order.setPrice(1000.0);
//            hallPortalService.insertOrdertest(order);
//        }
    }

    public void start() {
        for(Integer playeId: taskSessionManager.getOnlinePlayerIdList()){
            TaskSession session = taskSessionManager.getIoSession(playeId);
            Hall.ActivityStartNotify.Builder syn = Hall.ActivityStartNotify.newBuilder();
            syn.setActivityType(0);
            pushHelper.pushPBMsg(session, MsgBase.PacketType.ActivityStartNotify,syn.build().toByteString());
        }
    }

    private void finish() {
        for (Integer playeId : taskSessionManager.getOnlinePlayerIdList()) {
            TaskSession session = taskSessionManager.getIoSession(playeId);
            Hall.ActivityFinishNotify.Builder syn = Hall.ActivityFinishNotify.newBuilder();
            syn.setActivityType(0);
            pushHelper.pushPBMsg(session, MsgBase.PacketType.ActivityFinishNotify, syn.build().toByteString());
        }

        String key = "PRAY_" + DateFormatUtils.format(new Date(), "yyyyMMdd");
        Set<String> playerList = redisClient.zrevrange(key, 0, 3);
        int i = 0;
        for (String playerId : playerList) {
            if (playerId == null) continue;
            i++;
            User user = userDao.getUser(Integer.valueOf(playerId));
            List<ItemPkg> itemlist = new ArrayList<>();
            ItemPkg pkg = new ItemPkg();
            pkg.itemId = "A001";
            pkg.count = i == 1 ? 50 : (i == 2 ? 30 : 10);
            itemlist.add(pkg);
            sendMail(user, itemlist, i);
            String msg = "感天动地,玩家<color=red>" + user.getNickname() + "</color>虔诚膜拜,在祈福活动中拿下第" + i + "名,苍天不负有心人,付出就有回报,请收好邮件奖励";
            sendMarquee(user, msg);
        }
    }

    private void sendMarquee(User user, String s) {
        MarqueeMsg msg = new MarqueeMsg();
        msg.loopPushInterval = 1;
        msg.loopPushCount = 1;
        msg.marqueeType = 1;
        msg.msg = s;
        msg.playSetting = "1x1";
        msg.receiver = -1;
        msg.senderId = -1;
        msg.senderName = "系统管理员";
        msg.startTime = System.currentTimeMillis();
        msg.stopTime = System.currentTimeMillis();
        msg.pushOnLogin = false;
        msg.level = 1;
        try {
            portal.sendMarqueeMsg(msg);
        } catch (Exception e) {
            logger.info("error======跑马灯发送失败" + user.getNickname());
        }
    }

    private void sendMarquee(User user, String s, int level) {
        MarqueeMsg msg = new MarqueeMsg();
        msg.loopPushInterval = 1;
        msg.loopPushCount = 1;
        msg.marqueeType = 1;
        msg.msg = s;
        msg.playSetting = "1x1";
        msg.receiver = -1;
        msg.senderId = -1;
        msg.senderName = "系统管理员";
        msg.startTime = System.currentTimeMillis();
        msg.stopTime = System.currentTimeMillis();
        msg.pushOnLogin = false;
        msg.level = level;
        try {
            portal.sendMarqueeMsg(msg);
        } catch (Exception e) {
            logger.info("error======跑马灯发送失败" + user.getNickname());
        }
    }

    private void sendMail(User user, List<ItemPkg> itemlist, int i) {
        Award award = new Award();
        award.setItems(new Gson().toJson(itemlist));
        award.setInvalidTime(new Date(getCurrYearLast()));
        award.setSrcSystem("mailSys");
        award.setAwardNote("祈福奖励");
        award.setAwardType((1));
        award.setReceiverId(user.getId());
        award.setCtime(new Date());
        award.setAwardReason(ItemChangeReason.PRAY.toString());
        long awardId = awardService.addAward(award);

        Msg a = new Msg();
        a.setMsg("您在祈福活动中获得了第" + i + "名,特为您准备了一份薄礼,请大侠收下");
        a.setMsgMainType(0);
        a.setPriority(0);
        a.setRewardId(awardId);
        a.setSenderId(-1);
        a.setSenderName("系统");
        a.setStartDateTime(new Date());
        a.setStopDateTime(new Date(getCurrYearLast()));
        a.setTargetType(1);
        a.setTitle("祈福第" + i + "名奖励");
        a.setAttachNum(1);
        a.setTargetId(user.getId());
        a.setStatus(1);
        a.setItemCount(itemlist.get(0).count);
        a.setItemId(itemlist.get(0).itemId);
        a.setId(msgDao.insertMsg(a));

        UserMsg userMsg = new UserMsg();
        userMsg.setAwardId(awardId);
        userMsg.setDeled(false);
        userMsg.setReaded(false);
        userMsg.setMsgId(a.getId());
        userMsg.setReceived(false);
        userMsg.setUserId(user.getId());
        userMsg.setMtime(new Date());
        userMsg.setCtime(new Date());
        long id = msgDao.insert(userMsg);

        try {
            portal.sendMail(a.getId());
        } catch (Exception e) {
            logger.info("奖励邮件发送失败" + user.getId() + "玩家为===" + user.getNickname() + "=====" + user.getId());
        }
    }

    private long getCurrYearLast() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.clear();
        calendar.set(Calendar.YEAR, currentYear + 3);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime().getTime();
    }

    private void notifyStart() {
        for (Integer playeId : taskSessionManager.getOnlinePlayerIdList()) {
            sendMarquee(userDao.getUser(playeId), "女神祈福活动将在20:00开始,各位大侠请做好准备,越虔诚,越能感动女神,活动结束前三名将分别获得50钻石,30钻石,10钻石奖励");
        }
    }
}
