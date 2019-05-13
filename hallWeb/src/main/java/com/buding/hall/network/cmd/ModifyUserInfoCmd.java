package com.buding.hall.network.cmd;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.msgbase.MsgBase.PacketType;
import packet.user.User.ModifyUserInfoRequest;

import com.buding.db.model.User;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.shop.channel.ChannelRepostory;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.network.HallSession;

@Component
public class ModifyUserInfoCmd extends HallCmd {

	protected Logger logger = LogManager.getLogger(getClass());

	@Autowired
	protected ChannelRepostory channelRepostory;
	
	@Autowired
	UserService userService;
	
	@Autowired
	HallPushHelper pushHelper;
	
	@Override
	public void execute(CmdData data) throws Exception {
		ModifyUserInfoRequest req = ModifyUserInfoRequest.parseFrom(data.packet.getData());
		
		int userId = data.session.userId;
		HallSession session = data.session;
		if(userId == 0) {			
			pushHelper.pushErrorMsg(session, PacketType.GlobalMsgSyn, "没有登录");
			return;
		}
				
		User user = userService.getUser(userId);
		if(user == null) {
			pushHelper.pushErrorMsg(session, PacketType.GlobalMsgSyn, "用户不存在");
			return;
		}
		
		if(StringUtils.isNotBlank(req.getNickName())) {
			user.setNickname(req.getNickName());
		}
		
		if(StringUtils.isNotBlank(req.getHeadImg())) {
			user.setHeadImg(req.getHeadImg());
		}
		
		if(req.getSex() > 0) {
			user.setGender(req.getSex());
		}
		
		userService.updateUser(user);
		
		pushHelper.pushUserInfoSyn(userId);
	}

	@Override
	public PacketType getKey() {
		return PacketType.ModifyUserInfoRequest;
	}	
}
