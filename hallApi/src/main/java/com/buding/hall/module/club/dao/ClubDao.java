package com.buding.hall.module.club.dao;

import com.buding.db.model.*;

import java.util.Date;
import java.util.List;

public interface ClubDao {

    long insertClub(Club club);

    void deleteClub(long clubId);

    void updateClub(Club club);

    List<Club> selectClubList(int userId);

    Club selectClub(long clubId);

    void insertClubApply(ClubApply clubApply);

    void deleteClubApply(long clubId,int userId);

    void deleteClubApplyById(long clubApplyId);

    void deleteClubAllApply(long clubId);

    void deleteClubBadApply();

    void updateClubApply(ClubApply clubApply);

    List<ClubApply> selectClubALLApply(long clubId);

    ClubApply selectClubApply(long clubId, int applyUserId);

    void insertClubUser(ClubUser clubUser);

    void deleteClubUser(long clubId,int memberId);

    void deleteClubAllUser(long clubId);

    void updateClubUser(ClubUser clubUser);

    ClubUser selectClubUser(long clubId,int memberId);

    ClubUser selectClubOwnerUser(long clubId);

    List<ClubUser> selectClubAllUser(long clubId);

    List<ClubUser> selectClubAllManageUser(long clubId);

    List<ClubUser> selectUserClub(int userId);

    long insertClubScoreLog(ClubScoreLog clubScoreLog);

    void deleteClubScoreLog(long id);

    void updateClubScoreLog(ClubScoreLog clubScoreLog);

    ClubScoreLog selectClubScoreLog(long id);

    List<ClubScoreLog> selectClubScoreLogList(long clubId, int playerId, long startTime, long endTime, int type);

    long insertUserRemark(UserRemark clubScoreLog);

    void deleteUserRemark(long id);

    void updateUserRemark(UserRemark clubScoreLog);

    UserRemark selectUserRemark(int userId,int remarkUserId);

    List<UserRemark> selectUserRemarkByUserId(int userId);
}
