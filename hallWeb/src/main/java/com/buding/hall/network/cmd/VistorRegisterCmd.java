package com.buding.hall.network.cmd;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.VistorRegisterRequest;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.common.randomName.RandomNameService;
import com.buding.common.result.Result;
import com.buding.common.token.TokenServer;
import com.buding.common.util.DateUtil;
import com.buding.db.model.User;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.common.constants.UserType;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.user.service.UserService;

@Component
public class VistorRegisterCmd extends HallCmd {
	@Autowired
	HallPushHelper pushHelper;
	
	@Autowired
	UserService userService;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	@Autowired
	RandomNameService randomNameService;
	
	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;
		VistorRegisterRequest ur = VistorRegisterRequest.parseFrom(packet.getData());
		String deviceId = ur.getDeviceId();
		
		if(StringUtils.isBlank(deviceId)) {
			pushHelper.pushErrorMsg(data.session, packet.getPacketType(), "缺少参数deviceId");
			return;
		}
		
		String passwd = "123456";
		
		User dbUser = userService.getByUserName(deviceId);
		if(dbUser != null) {
			pushHelper.pushVistorRsp(data.session, dbUser.getUserName(), passwd);
			return;
		}
		
		String subffix = System.currentTimeMillis()+"";
		String nickName = DateUtil.format(new Date(), "yyyyMMdd" + subffix.substring(subffix.length() - 4));

		User user = userService.initUser();
		user.setUserName(deviceId);
		user.setPasswd(passwd);
		user.setUserType(UserType.VISITOR);
		user.setNickname(nickName);
		user.setGender((int) (System.currentTimeMillis() % 2));
		user.setNickname(randomNameService.randomName(user.getGender()));
		user.setDeviceType(ur.getDeviceFlag());
		Result result = userService.register(user);
		if (result.isFail()) {
			pushHelper.pushErrorMsg(data.session, packet.getPacketType(), result.msg);
			return;
		}
		
		pushHelper.pushVistorRsp(data.session, deviceId, passwd);
	}

	@Override
	public PacketType getKey() {
		return PacketType.VistorRegisterRequest;
	}
	
}
