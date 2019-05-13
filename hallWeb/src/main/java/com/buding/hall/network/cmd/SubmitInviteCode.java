package com.buding.hall.network.cmd;

import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.service.UserService;
import com.googlecode.protobuf.format.JsonFormat;
import com.ifp.wechat.entity.user.UserWeiXin;
import com.ifp.wechat.service.OAuthService;
import com.ifp.wechat.util.FenXiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.game.Hall.SubmitInviteCodeRequest;
import packet.game.Hall.SubmitInviteCodeResponse;
import packet.msgbase.MsgBase.PacketType;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubmitInviteCode extends HallCmd {

    @Autowired
    UserDao userDao;

    @Autowired
    HallPushHelper pushHelper;

    @Autowired
    ConfigManager configManager;

    @Autowired
    UserService userService;

    private Logger logger = LogManager.getLogger(getClass());

    @Override
    public void execute(CmdData data) throws Exception {
        SubmitInviteCodeRequest ur = SubmitInviteCodeRequest.parseFrom(data.packet.getData());
        User user = userDao.getUser(data.session.userId);
        if(user == null) return;
        if(user.getHasInvitecode() != null && user.getHasInvitecode() == 1) {
            SubmitInviteCodeResponse.Builder response = SubmitInviteCodeResponse.newBuilder();
            response.setCode(1);
            response.setMsg("您已添加了优惠码,无需再次输入");
            pushHelper.pushPBMsg(data.session, PacketType.SubmitInviteCodeResponse, response.build().toByteString());
            return;
        }
        String unionid = null;
        unionid = user.getWxunionid();
        if(StringUtils.isBlank(unionid)){
            unionid = OAuthService.getUserInfoOauth(ur.getToken(),ur.getOpenid()).getUnionid();
        }

        Map<String, String> map = new HashMap<>();
        map.put("unionid",unionid);
        map.put("user_code",ur.getInviteCode());
        String code = FenXiaoUtil.submitInviteCode(map);

        logger.info("分销返回码======="+code);
        int result = StringUtils.equals("200",code)?0:1;
        String msg = "";

        if((StringUtils.equals("200",code))){
            msg = "成功";
        } else if((StringUtils.equals("40010",code))){
            msg = "游戏ID必填";
        } else if((StringUtils.equals("40011",code))){
            msg = "优惠码不存在,请核对";
        } else if((StringUtils.equals("40024",code))){
            msg = "您已添加了优惠码,无需再次输入";
        } else if((StringUtils.equals("40023",code))){
            msg = "您是初始玩家,无需输入优惠码";
        } else if((StringUtils.equals("40033",code))){
            msg = "该优惠码不是代理的，无效";
        }else{
            logger.error("分销出问题了=========");
        }

        SubmitInviteCodeResponse.Builder response = SubmitInviteCodeResponse.newBuilder();
        response.setCode(result);
        response.setMsg(msg);
        pushHelper.pushPBMsg(data.session,PacketType.SubmitInviteCodeResponse,response.build().toByteString());

        //绑定优惠码送10钻石
        if((user.getHasInvitecode() == null || user.getHasInvitecode() == 0) && StringUtils.equals("200",code)) {
            userService.changeDiamond(user.getId(), 60, false, ItemChangeReason.BIND_INVITE_CODE);
        }
        if(StringUtils.equals("200", code) || StringUtils.equals("40023", code) || StringUtils.equals("40024", code)){
            user.setHasInvitecode(1);
            userDao.updateUser(user);
        }
        Hall.MallProductResponse.Builder mb = Hall.MallProductResponse.newBuilder();
        if(user.getHasInvitecode() == null || user.getHasInvitecode() == 0) {
            mb.setHasInviteCode(0);
        }else{
            mb.setHasInviteCode(1);
        }
        for(ProductConfig prd : configManager.shopItemConfMap.values()) {
            if(prd.status == 1) {
                Hall.MallProductModel.Builder m = Hall.MallProductModel.newBuilder();
                m.setCategory(prd.category);
                m.setId(prd.id);
                m.setImage(prd.img);
                m.setItemCount(prd.cItemCount);
                m.setName(prd.name);
                m.setPrice(prd.price.currenceCount);
                mb.addProducts(m);
            }
        }
        logger.info("绑定上级之后商品消息--"+ JsonFormat.printToString(mb.build()));
        pushHelper.pushPBMsg(data.session, PacketType.MallProductResponse, mb.build().toByteString());
    }

    @Override
    public PacketType getKey() {
        return PacketType.SubmitInviteCodeRequest;
    }
}
