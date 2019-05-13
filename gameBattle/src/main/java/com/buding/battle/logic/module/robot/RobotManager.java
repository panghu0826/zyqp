package com.buding.battle.logic.module.robot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.util.Util;
import com.buding.common.admin.component.BaseComponent;
import com.buding.common.randomName.RandomNameService;
import com.buding.db.model.RobotSetting;
import com.buding.db.model.User;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.robot.dao.RobotDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class RobotManager extends BaseComponent implements InitializingBean, RobotGenerator {
	private Logger logger = LogManager.getLogger(getClass());

	public Map<String, RobotPool> poolMap = new HashMap<String, RobotPool>();
//	Map<String, RobotSetting> settingMap = new HashMap<String, RobotSetting>();

	@Autowired
	HallPortalService hallPortalService;

	@Autowired
	RandomNameService randomNameService;

	@Autowired
	RobotDao robotDao;

	@Autowired
	UserDao userDao;
	
	@Autowired
	ServiceRepo serviceRepo;

	AtomicInteger robotId = new AtomicInteger(10001);

	@Override
	public Robot generate(RobotSetting setting, RobotPool pool) {
		User user = registerRobot(setting);
		Robot robot = initRobot(user);
		pool.add(robot);
		
		return robot;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					initAll();
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	public void initAll() throws Exception {
		List<RobotSetting> list = robotDao.loadRobotSettingList();
		
		for (RobotSetting model : list) {			
			RobotPool pool = new RobotPool(model, this);
			poolMap.put(model.getMatchId(), pool);
			List<User> userList = userDao.getRobotListByIdRange(pool.getMinId(), pool.getMaxId());
			
			for (User user : userList) {
				if(!model.getMatchId().equals(user.getBindedMatch())) {
					logger.info("居然id不对"+model.getMatchId());
					user.setBindedMatch(model.getMatchId());
					userDao.updateUser(user);
				}
				if(StringUtils.isBlank(user.getHeadImg()) || "img01.jpg".equals(user.getHeadImg())) {
					user.setHeadImg("portrait_img_0" + (new Random().nextInt(8) + 1));
					userDao.updateUser(user);
				}				
				Robot robot = initRobot(user);
				pool.add(robot);
			}
			
			if(userList.size() < model.getMinInit()) {
				logger.info("居然配置不对"+model.getMatchId());
				int i = 0;
				List<Robot> robots = new ArrayList<Robot>();
				while(i++ < model.getMinInit()) {
					robots.add(pool.borrow());
				}
				for(Robot robot : robots) {
					pool.ret(robot);
				}
			}
		}
	}

	private Robot initRobot(User user) {
		Robot robot = new Robot();
		UserHelper.copyUser2Player(user, robot);
		robot.robot = 1;

		BattleSession session = new BattleSession();
		session.initTime = System.currentTimeMillis();
		session.currentModule = ServiceRepo.matchModule;

		Util.initSession(session, robot, user);
		Util.userHasLogin(session);
		return robot;
	}

	private User registerRobot(RobotSetting setting) {
		RobotPool pool = poolMap.get(setting.getMatchId());
		if (pool == null) {
			logger.error("场[{}]机器人池无法找到", setting.getMatchId());
			return null;
		}

		int playerId = pool.getNextRobotId();
		if (playerId == -1) {
			logger.error("场[{}]无法产生机器人id, 已满", setting.getMatchId());
			return null;
		}

		User user = hallPortalService.getUser(playerId);
		if(user == null) {
			user = hallPortalService.initUser();
			user.setId(playerId);
			user.setCtime(new Date());
			user.setUserType(0);
			user.setGender((int) (System.currentTimeMillis() % 2));
			user.setNickname(randomNameService.randomName(user.getGender()));
			user.setBindedMatch(setting.getMatchId());
			user.setFanka(0);
			user.setCoin(2200);
			user.setDiamond(0);

			userDao.insert(user);
		}
		return user;
	}

	public Robot borrowRobot(RobotContext conf) throws Exception {
		RobotPool pool = poolMap.get(conf.matchConfig.matchID);
		if (pool == null) {
			logger.error("场[{}]机器人池无法找到", conf.matchConfig.matchID);
			return null;
		}
		Robot robot = pool.borrow();

		// 金币校正
		RoomConfig c = conf.roomConfig;
		int low = c.low;
		int high = c.high;

		if(low <= 0 && high <= 0) {
			robot.coin = 10000 + (int) (System.currentTimeMillis() % (10000));
		} else if (low > 0 && high <= 0) {
			robot.coin = low + (int) (System.currentTimeMillis() % (1000));
		} else if (robot.coin < low || robot.coin > high) {
			robot.coin = low + (int) (System.currentTimeMillis() % (high - low));
		}
		
		if(robot.coin < 0) {
			throw new RuntimeException("金币是负数:" + robot.coin);
		}
		
		logger.info("robot coin is " + robot.coin + " for match {} room {}", conf.matchConfig.matchID, conf.roomConfig.roomId);

		return robot;
	}

	public void returnRobot(Robot robot) {
		robot.pool.ret(robot);
	}

	public void robotSettle(PlayerInfo player, String matchId, int coin) {
		RobotSetting setting = this.robotDao.getByMatchId(matchId);
		if(setting != null) {
			setting.setTotalPlayed(setting.getTotalPlayed() + 1);
			if(coin > 0) {
				setting.setTotalEarnCoin(setting.getTotalEarnCoin() + coin);				
				setting.setTotalWined(setting.getTotalWined() + 1);
			} else {
				setting.setTotalLostCoin(setting.getTotalLostCoin() - coin);
			}
			robotDao.update(setting);
		}
	}
	

	@Override
	public String getComponentName() {
		return "robotmanager";
	}
	
	
}
