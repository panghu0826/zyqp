package com.buding.hall.module.user.dao;

import java.util.List;

import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.db.model.UserConsumeDiamond;
import com.buding.db.model.UserGameOutline;
import com.buding.hall.module.task.vo.GamePlayingVo;

public interface UserDao {
	public User insert(User user);
	public User getUser(int userId);
	public void updateUser(User user);
	public User getUserByUserName(String userName);
	public List<User> getRobotListByMatchId(String matchId);
	public List<User> getRobotListByIdRange(int start, int end);
	public User getUserByOpenId(String openId);
	public void addGameResult(GamePlayingVo gameResult) throws Exception;
	public UserGameOutline getUserGameOutline(int userId);
	
	public void addGameLog(GameLog log);
	public void addUserOnlineData(int userId, int day, int minute);

	List<User> getAllRealUser();

	User getByUnionId(String unionid);

	String getVersion();


	void addUserConsumeDiamondLog(UserConsumeDiamond userConsumeDiamond);
}
