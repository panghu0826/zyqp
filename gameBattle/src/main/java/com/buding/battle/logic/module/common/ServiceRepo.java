package com.buding.battle.logic.module.common;

import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.ws.TaskPortalService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.cluster.ClusterStubService;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.battle.logic.module.game.service.VipService;
import com.buding.battle.logic.module.robot.RobotManager;
import com.buding.battle.logic.network.module.GameModule;
import com.buding.battle.logic.network.module.MatchModule;
import com.buding.common.server.ServerConfig;
import com.buding.common.util.ReflectionUtil;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.hall.module.ws.HallPortalService;
import com.buding.hall.module.ws.MsgPortalService;

@Component
public class ServiceRepo implements InitializingBean {
	@Autowired
	GameService _matchService;	
	@Autowired
	GameModule _gameModule;	
//	@Autowired
//	public HallModule _hallModule;	
//	@Autowired
//	public LoginModule _loginModule;	
	@Autowired
	public MatchModule _matchModule;
	@Autowired
	public RobotManager _robotManager;	
	@Autowired
	public HallPortalService _hallPortalService;	
	@Autowired
	public ConfigManager _configManager;
	@Autowired
	public BattleSessionManager _sessionManager;
	@Autowired
	public ClusterStubService _clusterStubService;
	@Autowired
	public ServerConfig _serverConfig;
	@Autowired
	public UserRoomDao _userRoomDao;
	@Autowired
	public VipService _vipService;
	@Autowired
	public MsgPortalService _msgServicePortal;
	@Autowired
	public TaskPortalService _taskPortalService;
	@Autowired
	public FriendDao _friendDao;
	@Autowired
	public ClubDao _clubDao;
	
//	public static HallModule hallModule;
//	public static LoginModule loginModule;
	public static MatchModule matchModule;
	public static GameService matchService;
	public static GameModule gameModule;
	public static RobotManager robotManager;
	public static ConfigManager configManager;
	public static BattleSessionManager sessionManager;
	public static ClusterStubService clusterStubService;
	public static VipService vipService;
	public static HallPortalService hallPortalService;
	public static ServerConfig serverConfig;
	public static UserRoomDao userRoomDao;
	public static MsgPortalService msgServicePortal;
	public static FriendDao friendDao;
	public static ClubDao clubDao;
	public static TaskPortalService taskPortalService;

	@Override
	public void afterPropertiesSet() throws Exception {
		ReflectionUtil.copyInstanceVar2StaticVar(this);
	}
}
