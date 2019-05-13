package com.buding.hall.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.GsonBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.conf.ValVerifyUtil;
import com.buding.common.util.IOUtil;
import com.buding.db.model.MallConf;
import com.buding.db.model.RoomConf;
import com.buding.hall.config.task.BankruptTaskConf;
import com.buding.hall.config.task.BindMobileConf;
import com.buding.hall.config.task.DayLoginTaskConf;
import com.buding.hall.config.task.FirstLoginTaskConf;
import com.buding.hall.config.task.GamePlayedTaskConf;
import com.buding.hall.config.task.GameWinedTaskConf;
import com.buding.hall.config.task.MakeBankruptTaskConf;
import com.buding.hall.config.task.RatingConf;
import com.buding.hall.config.task.ShareConf;
import com.buding.hall.config.task.TaskConf;
import com.buding.hall.module.conf.ConfDao;
import com.buding.hall.module.task.type.TaskType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfigManager implements InitializingBean {
	@Autowired
	ConfDao confDao;
	
	public Map<String, MatchConfig> matchConfMap = new HashMap<String, MatchConfig>();//比赛配置(单机场,vip场,普通场/金币场)
	public Map<String, ProductConfig> shopItemConfMap = new HashMap<String, ProductConfig>();
	public Map<String, PropsConfig> propsConfigMap = new HashMap<String, PropsConfig>();
	private Map<String, TaskConf> taskConfMap = new HashMap<String, TaskConf>();
	private List<TaskConf> taskChain = new ArrayList<TaskConf>();
	public Map<String, RankConfig> rankConfMap = new HashMap<String, RankConfig>();
	public Map<String, MarqueeMsgConfig> marqueeMsgConfMap = new HashMap<String, MarqueeMsgConfig>();
	public Map<String, BoxMsgConfig> boxMsgConfMap = new HashMap<String, BoxMsgConfig>();
	public Map<String, ExchangeConfig> exchangeConfMap = new HashMap<String, ExchangeConfig>();
	public Map<String, GameConfig> gameMap = new HashMap<String, GameConfig>();
	public Map<String, String> msgTplMap = new HashMap<String, String>();
	public Map<String, StatusConf> statusConfMap = new HashMap<String, StatusConf>();
	public Map<String, LunPanConfig> lunPanConfigMap = new HashMap<>();
	public Map<String, SignConfig> signConfigMap = new HashMap<>();
	public Map<String, Integer> discountConfigMap = new HashMap<>();


	private String matchConfigFolder;

	private String shopConfigFolder;

	private String taskConfigFolder;

	private String propsConfigFolder;

	private String rankConfigFolder;

	private String marqueeMsgConfig;

	private String boxMsgConfig;

	private String exchangeConfigFolder;

	private String gameConfigFolder;

	private String msgTplPath;

	private String privateKeyFile;

	private String statusConfFolder;

	public String gmPath;

	public String userinitTplPath;

	private String lunPanConfigFolder;

	private String signConfigFolder;

	private String discountConfigFolder;


	@Override
	public void afterPropertiesSet() throws Exception {
		loadConfig();
	}

	private void loadConfig() throws Exception {
		initGameConfig();

		initMatchConfigFromDB();

		initPropsConfig();

		initShopConfig();

		// initRankGroupConfig();
		//
	 	initRankConfig();

		initLunPanConfigFromFile();

		initSignConfigFromFile();
		//
		// initMarqueeMsgConfig();
		//
		// initBoxMsgConfig();
		//
		// initExchangeConfig();

		initMsgTplConfig();

		initDiscountMapConfig();

		// initTaskConf();

		// initStatusConfig();
	}

	private void initDiscountMapConfig() {
		if (discountConfigFolder != null) {
			Map<String, Integer> discountConfigMap = new HashMap();
			for (File file : IOUtil.listFiles(discountConfigFolder)) {
				try {
					String json = IOUtil.getFileResourceAsString(file, "utf-8");
					List<DiscountConfig> items = new Gson().fromJson(json, new TypeToken<List<DiscountConfig>>() {}.getType());
					for (DiscountConfig itemConf : items) {
						ValVerifyUtil.check(itemConf);
						discountConfigMap.put(itemConf.itemid, itemConf.count);
					}
				} catch (Exception e) {
					throw new RuntimeException("ParseFileError:" + file.getAbsolutePath(), e);
				}
			}
			this.discountConfigMap = discountConfigMap;
		}
	}

	public void initGameConfig() throws Exception {
		if (gameConfigFolder != null) {
			Map<String, GameConfig> gameMap = new HashMap<String, GameConfig>();
			for (File file : IOUtil.listFiles(gameConfigFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8");
				List<GameConfig> items = new Gson().fromJson(json, new TypeToken<List<GameConfig>>() {
				}.getType());
				for (GameConfig itemConf : items) {
					ValVerifyUtil.check(itemConf);
					gameMap.put(itemConf.gameId, itemConf);
				}
			}
			this.gameMap = gameMap;
		}
	}

	private void initExchangeConfig() throws Exception {
		if (exchangeConfigFolder != null) {
			Map<String, ExchangeConfig> exchangeConfMap = new HashMap<String, ExchangeConfig>();
			for (File file : IOUtil.listFiles(exchangeConfigFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8");
				List<ExchangeConfig> items = new Gson().fromJson(json, new TypeToken<List<ExchangeConfig>>() {
				}.getType());
				for (ExchangeConfig itemConf : items) {
					ValVerifyUtil.check(itemConf);

					exchangeConfMap.put(itemConf.id, itemConf);

					for (ItemPkg conf : itemConf.items) {
						PropsConfig pc = getPropsConfigById(conf.itemId);
						if (pc == null) {
							throw new RuntimeException("无效的商品配置，道具不存在:" + conf.itemId);
						}
						conf.baseConf = pc;
					}
				}
			}
			this.exchangeConfMap = exchangeConfMap;
		}
	}

	private void initMarqueeMsgConfig() throws Exception {
		if (marqueeMsgConfig != null) {
			Map<String, MarqueeMsgConfig> marqueeMsgConfMap = new HashMap<String, MarqueeMsgConfig>();
			String json = IOUtil.getFileResourceAsString(new File(marqueeMsgConfig), "utf-8");
			List<MarqueeMsgConfig> items = new Gson().fromJson(json, new TypeToken<List<MarqueeMsgConfig>>() {
			}.getType());
			for (MarqueeMsgConfig conf : items) {
				ValVerifyUtil.check(conf);

				String msgType = conf.msgType;
				marqueeMsgConfMap.put(msgType, conf);
			}
			this.marqueeMsgConfMap = marqueeMsgConfMap;
		}
	}

	private void initMsgTplConfig() throws Exception {
		if (msgTplPath != null) {
			Map<String, String> msgTplMap = new HashMap<String, String>();
			Properties p = new Properties();
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(msgTplPath)), "UTF-8"));
			p.load(bf);
			for (Object key : p.keySet()) {
				String val = p.getProperty(key.toString());
				msgTplMap.put(key.toString(), val);
			}
			this.msgTplMap = msgTplMap;
		}
	}

	private void initBoxMsgConfig() throws Exception {
		if (boxMsgConfig != null) {
			Map<String, BoxMsgConfig> boxMsgConfMap = new HashMap<String, BoxMsgConfig>();
			String json = IOUtil.getFileResourceAsString(new File(boxMsgConfig), "utf-8");
			List<BoxMsgConfig> items = new Gson().fromJson(json, new TypeToken<List<BoxMsgConfig>>() {
			}.getType());
			for (BoxMsgConfig conf : items) {
				ValVerifyUtil.check(conf);
				boxMsgConfMap.put(conf.msgType, conf);
			}
			this.boxMsgConfMap = boxMsgConfMap;
		}
	}

	private void initStatusConfig() throws Exception {
		if (statusConfFolder != null) {
			Map<String, StatusConf> statusConfMap = new HashMap<String, StatusConf>();
			for (File file : IOUtil.listFiles(statusConfFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8");
				List<StatusConf> items = new Gson().fromJson(json, new TypeToken<List<StatusConf>>() {
				}.getType());
				for (StatusConf conf : items) {
					ValVerifyUtil.check(conf);
					conf.init();
					statusConfMap.put(conf.gameId, conf);
				}
			}
			this.statusConfMap = statusConfMap;
		}
	}

	public void initPropsConfig() throws Exception {
		if (propsConfigFolder != null) {
			Map<String, PropsConfig> propsConfigMap = new HashMap<String, PropsConfig>();
			for (File file : IOUtil.listFiles(propsConfigFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8").trim();
				List<PropsConfig> items = new Gson().fromJson(json, new TypeToken<List<PropsConfig>>() {
				}.getType());
				for (PropsConfig itemConf : items) {
					ValVerifyUtil.check(itemConf);
					propsConfigMap.put(itemConf.getItemId(), itemConf);
				}
			}
			this.propsConfigMap = propsConfigMap;
		}
	}

	private void initTaskConf() throws Exception {
		if (taskConfigFolder != null) {
			Map<String, TaskConf> taskConfMap = new HashMap<String, TaskConf>();
			for (File file : IOUtil.listFiles(taskConfigFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8").trim();
				if (json.startsWith("[")) {
					JSONArray ja = JSONArray.fromObject(json);
					for (int i = 0; i < ja.size(); i++) {
						JSONObject obj = ja.getJSONObject(i);
						parseTaskConf(obj, taskConfMap);
					}
				} else {
					JSONObject obj = JSONObject.fromObject(json);
					parseTaskConf(obj, taskConfMap);
				}
			}

			for (TaskConf conf : taskConfMap.values()) {
				if (conf.preTaskId != null) {
					TaskConf pre = taskConfMap.get(conf.preTaskId);
					conf.preTask = pre;
					pre.nextTask = conf;
				}
			}

			for (TaskConf conf : taskConfMap.values()) {
				if (conf.preTask == null) {
					taskChain.add(conf);
				}
			}

			for (TaskConf conf : taskConfMap.values()) {
				if (conf.awardItems != null) {
					for (ItemPkg c : conf.awardItems) {
						PropsConfig pc = getPropsConfigById(c.itemId);
						if (pc == null) {
							throw new RuntimeException("无效的任务奖励配置，道具不存在:" + c.itemId);
						}
						c.baseConf = pc;
					}
				}
			}
			this.taskConfMap = taskConfMap;
		}
	}

	private void parseTaskConf(JSONObject obj, Map<String, TaskConf> taskConfMap) {
		int taskType = Integer.valueOf(obj.getString("taskType"));
		Class<? extends TaskConf> cls = null;
		switch (taskType) {
		case TaskType.FIRST_LOGIN:
			cls = FirstLoginTaskConf.class;
			break;
		case TaskType.DAILY_LOGIN:
			cls = DayLoginTaskConf.class;
			break;
		case TaskType.CORUPT_ASSIST:
			cls = BankruptTaskConf.class;
			break;
		case TaskType.TREASURE_BOX:
			throw new RuntimeException();
		case TaskType.SHARE:
			cls = ShareConf.class;
			break;
		case TaskType.RATING:
			cls = RatingConf.class;
			break;
		case TaskType.TOTAL_WIN:
			cls = GameWinedTaskConf.class;
			break;
		case TaskType.TOTAL_GAMING:
			cls = GamePlayedTaskConf.class;
			break;
		case TaskType.MAKE_CORRUPTION:
			cls = MakeBankruptTaskConf.class;
			break;
		case TaskType.FIRST_CHARGE:
			throw new RuntimeException();
		case TaskType.BIND_MOBILE:
			cls = BindMobileConf.class;
			break;
		default:
			break;
		}

		try {
			TaskConf conf = new Gson().fromJson(obj.toString(), cls);
			ValVerifyUtil.check(conf);
			taskConfMap.put(conf.taskId, conf);
		} catch (Exception e) {
			System.out.println(obj.toString());
			throw new RuntimeException(e);
		}
	}
	
	public void initShopConfig()  {
		if(shopConfigFolder != null) {
			Map<String, ProductConfig> shopItemConfMap = new HashMap<String, ProductConfig>();
			
			List<MallConf> list = confDao.getMallConfList();
			for(MallConf item : list) {
				Gson gson = new GsonBuilder()
						.setPrettyPrinting()
						.disableHtmlEscaping()
						.create();
				ProductConfig itemConf = gson.fromJson(item.getConfJson(), ProductConfig.class);
				ValVerifyUtil.check(itemConf);

				shopItemConfMap.put(itemConf.id, itemConf);
				
				for (ItemPkg conf : itemConf.items) {
					PropsConfig pc = getPropsConfigById(conf.itemId);
					if (pc == null) {
						throw new RuntimeException("无效的商品配置，道具不存在:" + conf.itemId);
					}
					conf.baseConf = pc;
				}
			}
			this.shopItemConfMap = shopItemConfMap;
		}
	}

	public void initRankConfig() throws Exception {
		if (rankConfigFolder != null) {
			for (File file : IOUtil.listFiles(rankConfigFolder)) {
				String json = IOUtil.getFileResourceAsString(file, "utf-8");
				List<RankConfig> list = new Gson().fromJson(json, new TypeToken<List<RankConfig>>(){}.getType());
				for(RankConfig conf : list) {
					this.rankConfMap.put(conf.id, conf);
				}
			}			
		}
	}

	public void initMatchConfigFromDB() throws Exception {
		if (matchConfigFolder != null) {
			Map<String, MatchConfig> matchConfMap = new HashMap<String, MatchConfig>();
			
			List<RoomConf> list = confDao.getRoomConfList();
			for(RoomConf model : list) {
				MatchConfig config = new Gson().fromJson(model.getConfJson(), MatchConfig.class);
				ValVerifyUtil.check(config);
				addMatchConfig(config, matchConfMap);
				if (!gameMap.containsKey(config.gameID)) {
					throw new RuntimeException("无法识别的游戏:" + config.gameID);
				}
				gameMap.get(config.gameID).matchs.add(config);
				config.game = gameMap.get(config.gameID);
			}
			this.matchConfMap = matchConfMap;
		}

		for (GameConfig gc : gameMap.values()) {
			Collections.sort(gc.matchs, new Comparator<MatchConfig>() {
				@Override
				public int compare(MatchConfig m1, MatchConfig m2) {
					EnterCondition e1 = m1.conditionInfo.enterCondition;
					EnterCondition e2 = m2.conditionInfo.enterCondition;
					if (e1.maxCoinLimit <= e2.minCoinLimit) {
						return -1;
					}
					if (e2.maxCoinLimit <= e1.minCoinLimit) {
						return 1;
					}
					return 1;
//					throw new RuntimeException("赛场报名条件区间交叉");
				}
			});
		}
	}
	
	public void initMatchConfigFromFile() throws Exception {
		if (matchConfigFolder != null) {
			Map<String, MatchConfig> matchConfMap = new HashMap<String, MatchConfig>();
			for (File file : IOUtil.listFiles(matchConfigFolder)) {
				try {
					String json = IOUtil.getFileResourceAsString(file, "utf-8");
					List<MatchConfig> configList = new Gson().fromJson(json, new TypeToken<List<MatchConfig>>() {
					}.getType());
					for (MatchConfig config : configList) {
						ValVerifyUtil.check(config);
					}

					for (MatchConfig config : configList) {
						addMatchConfig(config, matchConfMap);
						if (!gameMap.containsKey(config.gameID)) {
							throw new RuntimeException("无法识别的游戏:" + config.gameID);
						}
						gameMap.get(config.gameID).matchs.add(config);
						config.game = gameMap.get(config.gameID);
					}
				} catch (Exception e) {
					throw new RuntimeException("ParseFileError:" + file.getAbsolutePath(), e);
				}
			}
			this.matchConfMap = matchConfMap;
		}

		for (GameConfig gc : gameMap.values()) {
			Collections.sort(gc.matchs, new Comparator<MatchConfig>() {
				@Override
				public int compare(MatchConfig m1, MatchConfig m2) {
					EnterCondition e1 = m1.conditionInfo.enterCondition;
					EnterCondition e2 = m2.conditionInfo.enterCondition;
					if (e1.maxCoinLimit <= e2.minCoinLimit) {
						return -1;
					}
					if (e2.maxCoinLimit <= e1.minCoinLimit) {
						return 1;
					}
					throw new RuntimeException("区间交叉");
				}
			});
		}
	}

	private void addMatchConfig(MatchConfig conf, Map<String, MatchConfig> matchConfMap) {
		String matchId = conf.matchID;
		if (matchConfMap.containsKey(matchId)) {
			throw new RuntimeException("赛场配置已经存在:" + matchId);
		}

		matchConfMap.put(matchId, conf);
		initRoomMap(conf);
	}

	private void initRoomMap(MatchConfig conf) {
		HashMap<String, RoomConfig> roomMap = new HashMap<String, RoomConfig>();
		conf.conditionInfo.roomConf = roomMap;
		for (RoomConfig r : conf.conditionInfo.roomArray) {
			if (roomMap.containsKey(r.roomId)) {
				throw new RuntimeException("房间id重复" + conf.gameID + " " + conf.matchID);
			}
			roomMap.put(r.roomId, r);
		}
	}

	public Collection<MatchConfig> getMatchConfs() {
		return matchConfMap.values();
	}

	public MatchConfig getMatchConfById(String matchConfId) {
		return matchConfMap.get(matchConfId);
	}

	public PropsConfig getPropsConfigById(String itemId) {
		return propsConfigMap.get(itemId);
	}

	public String getMatchConfigFolder() {
		return matchConfigFolder;
	}

	public void setMatchConfigFolder(String matchConfigFolder) {
		this.matchConfigFolder = matchConfigFolder;
	}

	public String getShopConfigFolder() {
		return shopConfigFolder;
	}

	public void setShopConfigFolder(String shopConfigFolder) {
		this.shopConfigFolder = shopConfigFolder;
	}

	public ProductConfig getItemConf(String id) {
		return shopItemConfMap.get(id);
	}

	/**
	 * 如果有多个任务配置的taskType是一样的，只返回找到的第一个.
	 * 
	 * @param taskType
	 * @return
	 */
	public TaskConf getTaskConfByType(int taskType) {
		for (TaskConf conf : taskConfMap.values()) {
			if (conf.taskType == taskType) {
				return conf;
			}
		}
		return null;
	}

	public TaskConf getTaskConfById(String taskId) {
		return taskConfMap.get(taskId);
	}

	public String getTaskConfigFolder() {
		return taskConfigFolder;
	}

	public void setTaskConfigFolder(String taskConfigFolder) {
		this.taskConfigFolder = taskConfigFolder;
	}

	public List<TaskConf> getTaskChain() {
		return taskChain;
	}

	public String getPropsConfigFolder() {
		return propsConfigFolder;
	}

	public void setPropsConfigFolder(String propsConfigFolder) {
		this.propsConfigFolder = propsConfigFolder;
	}

	public void setRankConfigFolder(String rankConfigFolder) {
		this.rankConfigFolder = rankConfigFolder;
	}

	public void setMarqueeMsgConfig(String marqueeMsgConfig) {
		this.marqueeMsgConfig = marqueeMsgConfig;
	}

	public void setBoxMsgConfig(String boxMsgConfig) {
		this.boxMsgConfig = boxMsgConfig;
	}

	public void setExchangeConfigFolder(String exchangeConfigFolder) {
		this.exchangeConfigFolder = exchangeConfigFolder;
	}

	public MatchConfig getMatchConfig(String gameId, int coin) {
		GameConfig gameConfig = gameMap.get(gameId);
		for (MatchConfig match : gameConfig.matchs) {
			int minCoin = match.conditionInfo.enterCondition.minCoinLimit;
			int maxCoin = match.conditionInfo.enterCondition.maxCoinLimit;
				if(match.matchID.equalsIgnoreCase("G_DQMJ_MATCH_MULTI")) continue;
			if (minCoin != -1 && coin < minCoin) {
				continue;
			}
			if (maxCoin != -1 && coin >= maxCoin) {
				continue;
			}
			return match;
		}
		return null;
	}

	public boolean isUserBankRupt(int userId, int coin) {
		for (GameConfig conf : gameMap.values()) {
			MatchConfig matchConf = getMatchConfig(conf.gameId, coin);
			if (matchConf != null) {
				return false;
			}
		}
		return true;
	}

	// TODO
	// public TimeConfModel searchLatestSettleConf(String groupId, Date date,
	// TimeResolver timeResolver) {
	// Map<Integer, RankConfig> map = this.rankConfMap.get(groupId).rankItemMap;
	// if(map == null) {
	// return null;
	// }
	//
	// RankConfig temp = null;
	// TimeStruct tt = null;
	// RankConfig lastConf = null;
	// for(RankConfig conf : map.values()) {
	// TimeStruct ts = timeResolver.resolve(conf, date);
	// if(ts.settleDateTime.before(date)) {
	// if(tt == null || tt.settleDateTime.before(ts.settleDateTime)) {
	// tt = ts;
	// temp = conf;
	// }
	// }
	// lastConf = conf;
	// }
	//
	// if(tt != null) {
	// return new TimeConfModel(temp, tt);
	// } else {
	// return new TimeConfModel(lastConf, timeResolver.resolve(lastConf, new
	// Date(date.getTime() - 24*3600*1000)));
	// }
	// }

	// public TimeConfModel searchRankConfig(String groupId, Date date,
	// TimeResolver timeResolver) {
	// Map<Integer, RankConfig> map = this.rankConfMap.get(groupId).rankItemMap;
	// if(map == null) {
	// return null;
	// }
	//
	// for(RankConfig conf : map.values()) {
	// TimeStruct ts = timeResolver.resolve(conf, date);
	// if(ts.isTimeHit(date)) {
	// return new TimeConfModel(conf, ts);
	// }
	// }
	// return null;
	// }

	public StatusItemConf getStatusConf(String gameId, int time) {
		StatusConf conf = statusConfMap.get(gameId);
		if (conf != null) {
			for (StatusItemConf item : conf.rankNum) {
				if (item.startIntTime <= time && item.endIntTime > time) {
					return item;
				}
			}
		}
		return null;
	}


	public void initLunPanConfigFromFile() throws Exception {
		if (lunPanConfigFolder != null) {
			Map<String, LunPanConfig> lunPanConfigMap = new HashMap();
			for (File file : IOUtil.listFiles(lunPanConfigFolder)) {
				try {
					String json = IOUtil.getFileResourceAsString(file, "utf-8");
					List<LunPanConfig> items = new Gson().fromJson(json, new TypeToken<List<LunPanConfig>>() {}.getType());
					int sum = 0;
					for (LunPanConfig itemConf : items) {
						ValVerifyUtil.check(itemConf);
						itemConf.min = sum;
						sum += itemConf.ratio*10000;
						itemConf.max = sum;
						lunPanConfigMap.put(itemConf.id, itemConf);
					}
				} catch (Exception e) {
					throw new RuntimeException("ParseFileError:" + file.getAbsolutePath(), e);
				}
			}
			this.lunPanConfigMap = lunPanConfigMap;
		}
	}

	public void initSignConfigFromFile() throws Exception {
		if (signConfigFolder != null) {
			Map<String, SignConfig> signConfigMap = new HashMap();
			for (File file : IOUtil.listFiles(signConfigFolder)) {
				try {
					String json = IOUtil.getFileResourceAsString(file, "utf-8");
					List<SignConfig> items = new Gson().fromJson(json, new TypeToken<List<SignConfig>>() {}.getType());
					for (SignConfig itemConf : items) {
						ValVerifyUtil.check(itemConf);
						signConfigMap.put(itemConf.id, itemConf);
					}
				} catch (Exception e) {
					throw new RuntimeException("ParseFileError:" + file.getAbsolutePath(), e);
				}
			}
			this.signConfigMap = signConfigMap;
		}
	}

	public void setGameConfigFolder(String gameConfigFolder) {
		this.gameConfigFolder = gameConfigFolder;
	}

	public void setMsgTplPath(String msgTplPath) {
		this.msgTplPath = msgTplPath;
	}

	public String getPrivateKeyFile() {
		return privateKeyFile;
	}

	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}

	public void setStatusConfFolder(String statusConfFolder) {
		this.statusConfFolder = statusConfFolder;
	}

	public void setGmPath(String gmPath) {
		this.gmPath = gmPath;
	}

	public String getUserinitTplPath() {
		return userinitTplPath;
	}

	public void setUserinitTplPath(String userinitTplPath) {
		this.userinitTplPath = userinitTplPath;
	}

	public String getLunPanConfigFolder() {
		return lunPanConfigFolder;
	}

	public void setLunPanConfigFolder(String lunPanConfigFolder) {
		this.lunPanConfigFolder = lunPanConfigFolder;
	}

	public String getSignConfigFolder() {
		return signConfigFolder;
	}

	public void setSignConfigFolder(String signConfigFolder) {
		this.signConfigFolder = signConfigFolder;
	}

	public void setDiscountConfigFolder(String discountConfigFolder) {
		this.discountConfigFolder = discountConfigFolder;
	}

	public String getDiscountConfigFolder() {
		return discountConfigFolder;
	}
}