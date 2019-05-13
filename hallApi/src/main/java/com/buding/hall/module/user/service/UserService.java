package com.buding.hall.module.user.service;

import com.buding.common.result.Result;
import com.buding.db.model.User;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.ConsumeGamePlayingVo;
import com.buding.hall.module.task.vo.GamePlayingVo;

public interface UserService {

	//初始化新用户
	public User initUser();

	//记录游戏结果
	public User addGameResult(GamePlayingVo gameResult);

	//获取用户
	public User getUser(int userId);
	
	//更新用户
	public void updateUser(User user);
	
	//是否可以领取破产救济
	public boolean isCanReceiveBankAssist(int userId);

	//领取破产救济
	public boolean receiveBankAssist(int userId);
	
	//注册
	public Result register(User user);
	
	//绑定手机
	public Result bindMobile(User user, String phone);
	
	//登录
	public User login(String username, String password);
	
	//根据用户名获取
	public User getByUserName(String username);
	
	//登录成功回调
	public void onUserLogin(User user);
	
	public void onUserLogout(int userId);
	
	/**
	 * 是否有足够的货币
	 * @param userId
	 * @param currenceType @see {@link CurrencyType}
	 * @param count
	 * @return
	 */
	public Result hasEnoughCurrency(int userId, int currenceType, int count);
	
	//是否有足够的道具
	public Result hasEnoughItem(int userId, String itemId, int count);
	
	//改变金币
	public Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason);
	
	//改变房卡
	public Result changeFangka(int userId, int coin, boolean check, ItemChangeReason reason);

	//改变钻石
	Result changeDiamond(int userId, int diamond, boolean check, ItemChangeReason reason);


	//用户是否在线
	public boolean isUserOnline(int userId);
	
	//授权
	public Result auth(int userId);
	
	//取消授权
	public Result cancelAuth(int userId);
	
	//重置密码
	public Result resetPasswd(int userId, String passwd);
	
	//改变用户类型
	public Result changeUserType(int userId, int type) ;

//    void addFangKaGameResult(ConsumeGamePlayingVo ret);

    int getCurrentOnlineCount();

    boolean payWithFenXiao(int userId,int number ,String type);

	User getByUnionId(String unionid);

    void closeSocket(int playerId);
}
