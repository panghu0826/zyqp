package com.buding.hall.module.ws;

import com.buding.api.desk.Desk;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.hall.module.game.model.DeskModel;

import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface BattlePortalBroadcastService {
	// 设置回放数据
	public Result setDeskRelayData(String instanceId, int playerId, String data);

	// 停止服务
	public void stopService(String instanceId);

	// 恢复服务
	public void startService(String instanceId);

	// 剔除玩家
	public void kickPlayer(String instanceId, int playerId);

	// 获取桌子列表
	public String getDeskList(String instanceId);

	List<DeskModel> getClubCommonDesk(long clubId);

	List<DeskModel> getClubJiFenDesk(long clubId);

	// 重新加载配置
	public void reloadMatchConf(String serverPattern);

	// 停服
	public void stopServer(String instanceId);

	// 清除卡桌
	public Result clearDesk(String instanceId, int userId);

	// 获取单个玩家已坐下俱乐部桌子
	public DeskModel getUserReadyClubJifenDesk(String instanceId, long clubId,int userId);

	// 强制解散桌子
	public Result destroyDesk(String instanceId, String gameId, String matchId, String deskId);

	//结算桌子
	public Result dismissDesk(String instanceId, String gameId, String matchId, String deskId);
	
	public TResult<String> dump(String instanceId, String gameId, String matchId, String deskId);
	
	public String getStatus();
	
	public String searchDesk(int playerId);
	
	public void closeSocket(int playerId);

	void addSecretKey(Integer playerId,String secretKey,long key,String wxunionid);
}
