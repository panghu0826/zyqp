package com.buding.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buding.retry.BattleConnectAction;
import com.buding.retry.EnrollAction;
import com.buding.retry.GameAuthAction;
import com.buding.retry.HallConnectAction;
import com.buding.retry.LoadAccountAction;
import com.buding.retry.LoginHallAction;
import com.buding.retry.MsgAuthAction;
import com.buding.retry.MsgConnectAction;
import com.buding.retry.RetryResult;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class Looper implements Runnable {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public Player player;
	protected String serverIp;
	protected int serverPort;
	protected String userName;
	protected String password;

	protected boolean shouldInitCmd = true;

	private HallConnectAction hallConnectAction;
	private BattleConnectAction battleConnectAction;
	private EnrollAction enrollAction;
	private GameAuthAction gameAuthAction;
	private LoadAccountAction loadAccountAction;
	private LoginHallAction loginHallAction;
	private MsgAuthAction msgAuthAction;
	private MsgConnectAction msgConnectAction;
	
	public boolean loginFinish = false;

	public Looper(String serverIp, int serverPort, String userName, String password) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.userName = userName;
		this.password = password;
	}

	public void reset() {
		logger.info("重置所有动作>>>>>>>>>>>");
		
		player = null;
		if (hallConnectAction != null) {
			hallConnectAction.reset();
			hallConnectAction = null;
		}
		if (loadAccountAction != null) {
			loadAccountAction.reset();
			loadAccountAction = null;
		}
		if (loginHallAction != null) {
			loginHallAction.reset();
			loginHallAction = null;
		}
		if (msgConnectAction != null) {
			msgConnectAction.reset();
			msgConnectAction = null;
		}
		if (msgAuthAction != null) {
			msgAuthAction.reset();
			msgAuthAction = null;
		}
		if (battleConnectAction != null) {
			battleConnectAction.reset();
			battleConnectAction = null;
		}
		if (gameAuthAction != null) {
			gameAuthAction.reset();
			gameAuthAction = null;
		}
		if (enrollAction != null) {
			enrollAction.reset();
			enrollAction = null;
		}
	}

	public RetryResult loop() {
		if (player == null) {
			player = new PlayerImpl();
			return RetryResult.DOING;
		}

		RetryResult result = null;
		do {
			// 连接大厅
			{
				if (hallConnectAction == null) {
					hallConnectAction = new HallConnectAction(player, serverIp, serverPort);
				}

				result = hallConnectAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

//			player.setAccount(userName, password);
//			加载帐号
			{
				if (loadAccountAction == null) {
					loadAccountAction = new LoadAccountAction(player);
				}

				result = loadAccountAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//登录大厅
			{
				if (loginHallAction == null) {
					loginHallAction = new LoginHallAction(player);
				}

				result = loginHallAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//连接消息服
			{
				if (msgConnectAction == null) {
					msgConnectAction = new MsgConnectAction(player);
				}

				result = msgConnectAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//连接认证
			{
				if (msgAuthAction == null) {
					msgAuthAction = new MsgAuthAction(player);
				}

				result = msgAuthAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//连接游戏
			{
				if (battleConnectAction == null) {
					battleConnectAction = new BattleConnectAction(player);
				}

				result = battleConnectAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//游戏认证
			{
				if (gameAuthAction == null) {
					gameAuthAction = new GameAuthAction(player);
				}

				result = gameAuthAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}

			//报名
			{
				if (enrollAction == null) {
					enrollAction = new EnrollAction(player);
				}

				result = enrollAction.act();
				if (result == RetryResult.DOING) {
					return result;
				}
				if(result == RetryResult.ABORT) {
					break;
				}
			}
		} while(false);
		
		if(result == RetryResult.ABORT) {
			reset();
			return result;
		}
		
		loginFinish = true;

		if (shouldInitCmd && !player.isInitInteractCmd()) {
			player.initInteractCmd();
			return RetryResult.DOING;
		}
		
		// player.gameHeatbeat();
		// player.hallHeatbeat();
		// player.msgHeatbeat();

		// logger.info("无事可做");
		return RetryResult.DOING;
	}

	public void run() {
		// while (true) {
		try {
			RetryResult r = loop();
			// Thread.sleep(new Random().nextInt(1000) + 300);
		} catch (Exception e) {
			logger.error("", e);
		}
		// }
	}
}
