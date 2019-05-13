package com.buding.hall.module.friend.dao;

import com.buding.db.model.Friend;
import com.buding.db.model.FriendApply;

import java.util.List;

public interface FriendDao {
    void insertFriend(Friend friend);

    void deleteFriend(int user1Id, int user2Id);

    void updateFriend(Friend friend);

    List<Friend> selectAllFriend(int playerId);

    Friend selectFriend(int playerId, int friendId);

    void insertFriendApply(FriendApply friend);

    void deleteFriendApply(int userId, int applyUserId);

    void updateFriendApply(FriendApply friendApply);

    List<FriendApply> selectFriendAllApply(int playerId);

    FriendApply selectFriendApply(int playerId, int applyUserId);
}
