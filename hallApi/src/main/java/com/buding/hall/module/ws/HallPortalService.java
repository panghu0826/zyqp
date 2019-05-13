package com.buding.hall.module.ws;

import java.util.Date;
import java.util.List;

import com.buding.common.cluster.model.ServerModel;
import com.buding.common.result.Result;
import com.buding.db.model.*;
import com.buding.hall.module.cluster.model.ServerState;
import com.buding.hall.module.cluster.model.UserGaming;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.ConsumeGamePlayingVo;
import com.buding.hall.module.task.vo.GamePlayingVo;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface HallPortalService {
	//更新用户正在游戏的数据
	public void updateUserGaming(int userId, String gameId, String serverInstanceId);

	//移除
	public void removeUserGaming(int userId, String gameId, String serverInstanceId);

	//分布式环境下更新服务器在线状态
	public void updateServerStatus(ServerState server);

	//获取用户正在游戏的数据
	public UserGaming getUserGaming(int userId);

	// 获取用户
	public User getUser(int userId);

	void register(User var1);

	// 初始化新用户
	public User initUser();

	// 是否可以领取破产救济
	public boolean isCanReceiveBankAssist(int userId);

	// 改变金币
	public Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason);

	// 记录游戏结果
	public User addGameResult(GamePlayingVo gameResult);

	//添加游戏记录
	public void addGameLog(GameLog log);

	//是否有足够金币
	public Result hasEnoughCurrency(int userId, int currenceType, int count);

	// 是否有足够的道具
	public Result hasEnoughItem(int userId, String itemId, int count);

	// 改变房卡
	public Result changeFangka(int userId, int coin, boolean check, ItemChangeReason reason);

	//改变钻石
	public Result changeDiamond(int userId, int diamond, boolean check, ItemChangeReason reason);

	public boolean payWithFenXiao(int userId, int number ,String type);

	// 用户是否在线
	public boolean isUserOnline(int userId);

	//授权
	public Result auth(int userId);

	//取消授权
	public Result cancelAuth(int userId);
	
	//重新加载配置
	public Result reloadMallConf(String serverPattern);

	//添加房间
	public Result addRoom(UserRoom room);

	//更新插入房间
	public Result upsertRoom(UserRoom room);

	//获取我的房间
	public UserRoom getMyRoom(int userId, String matchId);

	//获取我的房间 
	public UserRoom getMyRoom(int userId, long roomId);

	//获取房间
	public UserRoom getByRoomCode(String roomCode);

	//生成房间编号
	public String genRoomUniqCode();
	
	//添加用户排行榜积分
	public void addUserRankPoint(int userId, String userName, String gameId, int rankPoint, int rankType, Date date) throws Exception;
	
	//重置密码
	public Result resetPasswd(int userId, String passwd);
	
	//改变用户类型
	public Result changeUserType(int userId, int type);
	
	//获取所有服务列表
	public List<ServerModel> getAllServer(String serverType);
	
	//启动服务
	public void stopService(String instanceId);
	
	//停止服务
	public void startService(String instanceId);
	
	//微信支付回调
	public String onWxPayCallback(String xml) throws Exception;

	String processWxPayCallbackTest(String xml) throws Exception;

	public String onAliPayCallback(String xml) throws Exception;

//    void addFangKaGameResult(ConsumeGamePlayingVo ret);

    String getQunZhuRoom(String playerId);

    void delQunZhuRoom(String deskID);
    //获取在线人数
	int getCurrentOnlineCount();

	public long addAward(Award award);

	public long insertMsg(Msg msg);

	long insertUserMsg(UserMsg userMsg);

    void sendMultiMatchPlayerNum(int i, int size);

	public void closeSocket(int playerId);

	void addUserConsumeDiamondLog(UserConsumeDiamond userConsumeDiamond);

    void insertOrdertest(UserOrder order);

    long genVideoId();
	
	void addSecretKey(Integer playerId,String secretKey,long key,String wxunionid);
}
