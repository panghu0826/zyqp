package com.buding.hall.network.cmd;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ifp.wechat.util.FenXiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall;
import packet.msgbase.MsgBase;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.LoginRequest;

import com.buding.common.cache.RedisClient;
import com.buding.common.result.Result;
import com.buding.common.token.TokenServer;
import com.buding.db.model.User;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.common.constants.ClientType;
import com.buding.hall.module.common.constants.UserType;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.helper.UserSecurityHelper;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.network.HallSessionManager;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
@Component
public class LoginCmd extends HallCmd {
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserDao userDao;

	@Autowired
	TokenServer tokenServer;

	@Autowired
	UserSecurityHelper userSecurityHelper;
	
	@Autowired
	HallSessionManager hallSessionManager;
	
	@Autowired
	HallPushHelper pushHelper;
	
	@Autowired
	RedisClient redisClient;

	@Override
	public void execute(CmdData data) throws Exception {		
		PacketBase packet = data.packet;
		LoginRequest ur = LoginRequest.parseFrom(packet.getData());

		logger.info("login cmd, username={}; passwd={};", ur.getUsername(), ur.getPassward());

		User user ;
		
		//微信登录
		if (ur.getType() == ClientType.WEIXIN) {
			logger.info("wx login ");
			packet.user.User.WeiXinUser wxUser = ur.getWxUser();

			user = userService.getByUnionId(wxUser.getUnionid());

			if(user == null) {
				user = userService.initUser();
				user.setUserName(wxUser.getUnionid());
				user.setPasswd(ur.getPassward());
				user.setUserType(UserType.WX_USER);
				user.setNickname("微信用户");
				user.setDeviceType(ur.getDeviceFlag());
				if(StringUtils.isNotBlank(wxUser.getHeadimgurl())) {
					user.setHeadImg(wxUser.getHeadimgurl());
				}
				if(wxUser.getSex() != 0) {
					user.setGender(wxUser.getSex());
				}
				Result result = userService.register(user);
				if(result.isFail()) {
					logger.info("register wx user null ");
					pushHelper.pushErrorMsg(data.session, packet.getPacketType(), "微信登录失败,用户不存在");
					return;
				}
			}
			user.setDeviceType(ur.getDeviceFlag());
			user.setUserName(wxUser.getUnionid());
			user.setPasswd(ur.getPassward());
			if(user.getFirstLogin()==null){
				user.setFirstLogin(new Date());
			}
			if(user.getSignWeekFirstDay()==null){
				user.setSignWeekFirstDay(new Date());
			}
			if(StringUtils.isNotBlank(wxUser.getHeadimgurl())) {
				user.setHeadImg(wxUser.getHeadimgurl());
			}
			if(wxUser.getSex() != 0) {
				user.setGender(wxUser.getSex());
			}
			if(StringUtils.isNotBlank(wxUser.getNickname())) {
				user.setNickname(wxUser.getNickname());
			}else{
				user.setNickname("翻遍火星也木有你的名字");
			}
			if(StringUtils.isNotBlank(wxUser.getOpenid())) {
				user.setWxopenid(wxUser.getOpenid());
			}
			if(StringUtils.isNotBlank(wxUser.getUnionid())) {
				user.setWxunionid(wxUser.getUnionid());
			}
			userService.updateUser(user);

			if(user.getHasInvitecode() == null || user.getHasInvitecode() == 0) {
				Map<String, String> map = new HashMap();
				map.put("unionid", wxUser.getUnionid());
				try {
					String code = FenXiaoUtil.login(map);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			logger.info("login wx ok");
		} else {
			//用户名密码登录
			user = userService.login(ur.getUsername(), ur.getPassward());
			if (user == null) {
				logger.error("act=hallLoginFailUserNotFound;username={}", ur.getUsername());
				pushHelper.pushErrorMsg(data.session, packet.getPacketType(), "登录失败,用户名或密码错误");
				return;
			}

			if (StringUtils.isNotBlank(user.getBindedMatch())) {
				logger.error("act=hallLoginFailRobotNotAllow;username={}", ur.getUsername());
				pushHelper.pushErrorMsg(data.session, packet.getPacketType(), "非法的用户登录");
				return;
			}
		}

		// 更新token
		String token = tokenServer.updateToken(user.getId(), false);

		data.session.userId = user.getId();
		
		hallSessionManager.removeFromAnonymousList(data.session.getSessionId());
		hallSessionManager.put2OnlineList(data.session.userId, data.session);

//		if(!StringUtils.equals(ur.getVersion(),userDao.getVersion())){
//			NeedUpdate.Builder syn = NeedUpdate.newBuilder();
//			pushHelper.pushPBMsg(data.session, PacketType.NeedUpdate,syn.build().toByteString());
//			return;
//		}
		Set<String> hallServer = redisClient.zrange("serverSet_hall", 0, 0);
		Set<String> msgServer = redisClient.zrange("serverSet_msg", 0, 0);
		Set<String> battleServer = redisClient.zrange("serverSet_battle", 0, 0);
		Set<String> taskServer = redisClient.zrange("serverSet_task", 0, 0);
		Set<String> rankServer = redisClient.zrange("serverSet_rank", 0, 0);

		String hallAddr = hallServer.iterator().next();
		String msgAddr = msgServer.iterator().next();
		String battleAddr = battleServer.iterator().next();
		String taskAddr = taskServer.iterator().next();
		String rankAddr = rankServer.iterator().next();
		pushHelper.pushLoginRsp(data.session, user, token, msgAddr.split("_")[1], battleAddr.split("_")[1], taskAddr.split("_")[1], rankAddr.split("_")[1]);

		pushHelper.pushUserInfoSyn(user.getId());

		Hall.ShouChongSyn.Builder syn = Hall.ShouChongSyn.newBuilder();
		syn.setShouchong1(user.getShouchong1());
		syn.setShouchong2(user.getShouchong2());
		syn.setShouchong3(user.getShouchong3() == null ? 0 :user.getShouchong3());
		pushHelper.pushPBMsg(data.session,PacketType.ShouChongSyn,syn.build().toByteString());

		long now = System.currentTimeMillis();
		long min = DateUtils.parseDate(DateFormatUtils.format(new Date(),"yyyyMMdd")+" 20:00:00",new String[]{"yyyyMMdd hh:mm:ss"}).getTime();
		long max = DateUtils.parseDate(DateFormatUtils.format(new Date(),"yyyyMMdd")+" 20:10:00",new String[]{"yyyyMMdd hh:mm:ss"}).getTime();

		if(now>=min && now <= max){
			Hall.ActivityStartNotify.Builder syn2 = Hall.ActivityStartNotify.newBuilder();
			syn2.setActivityType(0);
			pushHelper.pushPBMsg(data.session, MsgBase.PacketType.ActivityStartNotify,syn2.build().toByteString());
		}

		userService.onUserLogin(user);
	}

	@Override
	public PacketType getKey() {
		return PacketType.LoginRequest;
	}

}
