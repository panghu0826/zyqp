package com.buding.hall.network.cmd;

import com.buding.common.cache.RedisClient;
import com.buding.common.result.Result;
import com.buding.common.token.TokenClient;
import com.buding.common.token.TokenServer;
import com.buding.db.model.User;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.common.constants.ClientType;
import com.buding.hall.module.common.constants.UserType;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.network.HallSessionManager;
import com.ifp.wechat.entity.user.UserWeiXin;
import com.ifp.wechat.service.OAuthService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.ReconnetLogin;

import java.util.Set;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class ReconnectCmd extends HallCmd {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	UserService userService;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	HallSessionManager hallSessionManager;
	
	@Autowired
	HallPushHelper pushHelper;

	@Autowired
	TokenClient tokenClient;


	@Override
	public void execute(CmdData data) throws Exception {		
		PacketBase packet = data.packet;
        ReconnetLogin ur = ReconnetLogin.parseFrom(packet.getData());
		
		logger.info("Reconnect cmd, userid={};", ur.getUserId());
        int userid = ur.getUserId();
		String token = ur.getToken();

		User user = userService.getUser(userid);
		// 会话验证
		if (!tokenClient.verifyToken(userid, token)) {
			pushHelper.pushErrorMsg(data.session, PacketType.AuthRequest, "token无效");
			return;
		}
		
		data.session.userId = userid;
		
		hallSessionManager.removeFromAnonymousList(data.session.getSessionId());
		hallSessionManager.put2OnlineList(data.session.userId, data.session);

		pushHelper.pushUserInfoSyn(userid);

		Hall.ShouChongSyn.Builder syn = Hall.ShouChongSyn.newBuilder();
		syn.setShouchong1(user.getShouchong1());
		syn.setShouchong2(user.getShouchong2());
		syn.setShouchong3(user.getShouchong3()==null ? 0 :1);
		pushHelper.pushPBMsg(data.session,PacketType.ShouChongSyn,syn.build().toByteString());
		userService.onUserLogin(user);
	}

	@Override
	public PacketType getKey() {
		return PacketType.ReconnetLogin;
	}

}
