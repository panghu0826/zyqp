package com.buding.db.dao;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.Friend;
import com.buding.db.model.FriendApply;
import com.buding.hall.module.friend.dao.FriendDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FriendDaoImpl extends CachedServiceAdpter implements FriendDao {

	@Autowired
	DbService dbService;

	@Override
	public void insertFriend(Friend friend) {
		this.commonDao.save(friend);
	}

	@Override
	public void deleteFriend(int user1Id,int user2Id) {
		Friend friend1 = this.commonDao.selectOne("select * from t_friend where user_id = ? and friend_user_id = ? ", Friend.class, user1Id,user2Id);
		this.commonDao.delete(friend1.getId(),Friend.class);
		Friend friend2 = this.commonDao.selectOne("select * from t_friend where user_id = ? and friend_user_id = ? ", Friend.class, user2Id,user1Id);
		this.commonDao.delete(friend2.getId(),Friend.class);
	}

	@Override
	public void updateFriend(Friend friend) {
		this.put2EntityCache(friend);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(friend);
		} else {
			this.dbService.submitUpdate2Queue(friend);
		}
	}

	@Override
	public List<Friend> selectAllFriend(int playerId) {
		return this.commonDao.selectList("select a.*,b.nickname as friend_user_name,b.head_img as friend_user_img from t_friend a,user b where a.friend_user_id = b.id and a.user_id = ? ", Friend.class, playerId);
	}

	@Override
	public Friend selectFriend(int playerId, int friendId) {
	    String sql = "select a.*,b.nickname as friend_user_name,b.head_img as friend_user_img from t_friend a,user b where a.friend_user_id = b.id and ((a.user_id = ? and a.friend_user_id = ?) or (a.friend_user_id = ? and  a.user_id = ?)) ";
		return this.commonDao.selectOne(sql, Friend.class, playerId,friendId,playerId,friendId);
	}

	@Override
	public void insertFriendApply(FriendApply friendApply) {
		this.commonDao.save(friendApply);
	}

	@Override
	public void deleteFriendApply(int userId, int applyUserId) {
		List<FriendApply> friendApplyList = this.commonDao.selectList("select a.*,b.nickname as apply_user_name,b.head_img as apply_user_img from t_friend_apply a,user b where a.apply_user_id = b.id and a.user_id = ? and a.apply_user_id = ? ", FriendApply.class, userId,applyUserId);
		if(friendApplyList == null || friendApplyList.isEmpty()) return;
		for(FriendApply friendApply : friendApplyList) {
			this.commonDao.delete(friendApply.getId(), FriendApply.class);
		}
	}

	@Override
	public void updateFriendApply(FriendApply friendApply) {
		this.put2EntityCache(friendApply);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(friendApply);
		} else {
			this.dbService.submitUpdate2Queue(friendApply);
		}
	}

	@Override
	public List<FriendApply> selectFriendAllApply(int playerId) {
		return this.commonDao.selectList("select a.*,b.nickname as apply_user_name,b.head_img as apply_user_img from t_friend_apply a,user b where a.apply_user_id = b.id and a.user_id = ? ", FriendApply.class, playerId);
	}

	@Override
	public FriendApply selectFriendApply(int playerId, int applyUserId) {
	    String sql = "select a.*,b.nickname as apply_user_name,b.head_img as apply_user_img from t_friend_apply a,user b where a.apply_user_id = b.id and a.user_id = ? and a.apply_user_id = ?";
		return this.commonDao.selectOne(sql, FriendApply.class, playerId,applyUserId);
	}

}
