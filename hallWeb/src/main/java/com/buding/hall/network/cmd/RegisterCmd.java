package com.buding.hall.network.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.RegisterRequest;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.result.Result;
import com.buding.common.token.TokenServer;
import com.buding.db.model.User;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.common.constants.UserType;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.user.service.UserService;

@Component
public class RegisterCmd extends HallCmd {
	@Autowired
	HallPushHelper pushHelper;
	
	@Autowired
	UserService userService;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;
		RegisterRequest ur = RegisterRequest.parseFrom(packet.getData());
		String username = ur.getAccount();
		String pass = ur.getPassword();
		String nickName = ur.getNickname();
//		Thread.sleep(2000);
		User dbUser = userService.getByUserName(username);
		if(dbUser != null) {
			pushHelper.pushRegistorRsp(data.session, false, "帐号已存在");
			return;
		}
		
		User user = userService.initUser();
		user.setUserName(username);
		user.setPasswd(pass);
		user.setUserType(UserType.ACCOUNT_USER);
		user.setDeviceType(ur.getDeviceFlag());
		user.setNickname(nickName);
		Result result = userService.register(user);
		if (result.isFail()) {
			pushHelper.pushErrorMsg(data.session, packet.getPacketType(), result.msg);
			return;
		}
		
		pushHelper.pushRegistorRsp(data.session, true, null);
	}

	@Override
	public PacketType getKey() {
		return PacketType.RegisterRequest;
	}
	
}
