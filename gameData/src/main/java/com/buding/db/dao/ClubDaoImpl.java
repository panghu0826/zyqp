package com.buding.db.dao;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.*;
import com.buding.hall.module.club.dao.ClubDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClubDaoImpl extends CachedServiceAdpter implements ClubDao {

	@Autowired
	DbService dbService;

	@Override
	public long insertClub(Club club) {
		this.commonDao.save(club);
        return club.getId();
    }

	@Override
	public void deleteClub(long clubId) {
	    this.commonDao.delete(clubId, Club.class);
	}

	@Override
	public void updateClub(Club club) {
		this.put2EntityCache(club);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(club);
		} else {
			this.dbService.submitUpdate2Queue(club);
		}
	}

	@Override
	public List<Club> selectClubList(int userId) {
        List<ClubUser> list = selectUserClub(userId);
        List<Club> result = new ArrayList<>();
        for(ClubUser clubUser : list){
            result.add(selectClub(clubUser.getClubId()));
        }
		return result;
	}

	@Override
	public Club selectClub(long clubId) {
		return this.commonDao.get(clubId, Club.class);
	}

	@Override
	public void insertClubApply(ClubApply clubApply) {
		this.commonDao.save(clubApply);
	}

	@Override
	public void deleteClubApply(long clubId,int userId) {
        List<ClubApply> clubApplyList = this.commonDao.selectList("select * from t_club_apply where club_id = ? and apply_user_id = ? ", ClubApply.class, clubId,userId);
        if(clubApplyList == null || clubApplyList.isEmpty()) return;
        for(ClubApply clubApply : clubApplyList) {
            this.commonDao.delete(clubApply.getId(), ClubApply.class);
        }
	}

	@Override
	public void deleteClubApplyById(long clubApplyId) {
        this.commonDao.delete(clubApplyId, ClubApply.class);
	}

	@Override
	public void deleteClubAllApply(long clubId) {
        List<ClubApply> clubApplyList = this.commonDao.selectList("select * from t_club_apply where club_id = ? ", ClubApply.class, clubId);
        if(clubApplyList == null || clubApplyList.isEmpty()) return;
        for(ClubApply clubApply : clubApplyList) {
            this.commonDao.delete(clubApply.getId(), ClubApply.class);
        }
	}

	@Override
	public void deleteClubBadApply() {
	    String sql = "select b.* from t_club_user a,t_club_apply b where a.club_id = b.club_id and a.club_member_id = b.apply_user_id\n" +
                "union " +
                "select c.* from t_club_apply c where c.apply_user_id = 0";
        List<ClubApply> clubApplyList = this.commonDao.selectList(sql, ClubApply.class);
        if(clubApplyList == null || clubApplyList.isEmpty()) return;
        for(ClubApply clubApply : clubApplyList) {
            this.commonDao.delete(clubApply.getId(), ClubApply.class);
        }
	}

	@Override
	public void updateClubApply(ClubApply clubApply) {
		this.put2EntityCache(clubApply);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(clubApply);
		} else {
			this.dbService.submitUpdate2Queue(clubApply);
		}
	}

	@Override
	public List<ClubApply> selectClubALLApply(long clubId) {
        return this.commonDao.selectList("select a.*,b.nickname as apply_user_name,b.head_img as apply_user_img from t_club_apply a ,user b where a.apply_user_id = b.id and a.club_id = ? ", ClubApply.class, clubId);
	}

	@Override
	public ClubApply selectClubApply(long clubId, int applyUserId) {
        return this.commonDao.selectOne("select a.*,b.nickname as apply_user_name,b.head_img as apply_user_img from t_club_apply a,user b where a.apply_user_id =b.id and a.club_id = ? and a.apply_user_id = ? ", ClubApply.class, clubId,applyUserId);
	}

	@Override
	public void insertClubUser(ClubUser clubUser) {
		this.commonDao.save(clubUser);
	}

	@Override
	public void deleteClubUser(long clubId,int memberId) {
        ClubUser clubUser = this.commonDao.selectOne("select * from t_club_user where club_id = ? and club_member_id = ? ", ClubUser.class, clubId,memberId);
        if(clubUser != null) this.commonDao.delete(clubUser.getId(), ClubUser.class);
	}

	@Override
	public void deleteClubAllUser(long clubId) {
        List<ClubUser> clubUserList = this.commonDao.selectList("select * from t_club_user where club_id = ?", ClubUser.class, clubId);
        if(clubUserList == null || clubUserList.isEmpty()) return;
        for(ClubUser clubUser : clubUserList) {
            this.commonDao.delete(clubUser.getId(), ClubUser.class);
        }
	}

	@Override
	public void updateClubUser(ClubUser clubUser) {
//		this.put2EntityCache(clubUser);
//		if(ServerConfig.immediateSave) {
			this.commonDao.update(clubUser);
//		} else {
//			this.dbService.submitUpdate2Queue(clubUser);
//		}
	}

	@Override
	public ClubUser selectClubUser(long clubId,int memberId) {
		String sql = "select a.*, b.nickname as club_member_name, b.head_img as club_member_img from t_club_user a,user b where a.club_member_id = b.id and a.club_id = ? and a.club_member_id = ? ";
        return this.commonDao.selectOne(sql, ClubUser.class, clubId,memberId);
	}

    @Override
    public ClubUser selectClubOwnerUser(long clubId) {
	    String sql = "select a.*, b.nickname as club_member_name, b.head_img as club_member_img from t_club_user a,user b where a.club_member_id = b.id and a.club_id = ? and a.club_member_type = 1 ";
        return this.commonDao.selectOne(sql, ClubUser.class, clubId);

    }

    @Override
	public List<ClubUser> selectClubAllUser(long clubId) {
	    String sql ="select a.*, b.nickname as club_member_name, b.head_img as club_member_img from t_club_user a,user b where a.club_member_id = b.id and a.club_id = ? ";
        return this.commonDao.selectList(sql, ClubUser.class, clubId);
	}

	@Override
	public List<ClubUser> selectClubAllManageUser(long clubId) {
	    String sql = "select a.*, b.nickname as club_member_name, b.head_img as club_member_img from t_club_user a,user b where a.club_member_id = b.id and a.club_id = ? and (a.club_member_type = 1 or a.club_member_type = 2)";
        return this.commonDao.selectList(sql, ClubUser.class, clubId);
	}

	@Override
	public List<ClubUser> selectUserClub(int userId) {
        return this.commonDao.selectList("select a.*, b.nickname as club_member_name, b.head_img as club_member_img from t_club_user a,user b where a.club_member_id = b.id and a.club_member_id = ? ", ClubUser.class, userId);
	}

	@Override
	public long insertClubScoreLog(ClubScoreLog clubScoreLog) {
		this.commonDao.save(clubScoreLog);
		return clubScoreLog.getId();
	}

	@Override
	public void deleteClubScoreLog(long id) {
		this.commonDao.delete(id, ClubScoreLog.class);
	}

	@Override
	public void updateClubScoreLog(ClubScoreLog clubScoreLog) {
		this.put2EntityCache(clubScoreLog);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(clubScoreLog);
		} else {
			this.dbService.submitUpdate2Queue(clubScoreLog);
		}
	}

	@Override
	public ClubScoreLog selectClubScoreLog(long id) {
		return this.commonDao.get(id, ClubScoreLog.class);
	}

	@Override
    public List<ClubScoreLog> selectClubScoreLogList(long clubId, int playerId, long startTime, long endTime, int type){
	    String sql = "select a.*, b.nickname as player_name from t_clubscore_log a,user b where a.player_id = b.id ";
	    List<Object> params = new ArrayList<>();
	    if(playerId > 0){
	        sql += "and a.player_id = ? ";
	        params.add(playerId);
        }
        if(clubId > 0){
            sql += "and a.club_id = ? ";
            params.add(clubId);
        }
        if(startTime > 0){
            sql += "and a.mtime >= ? ";
            params.add(startTime);
        }
        if(endTime > 0){
            sql += "and a.mtime <= ? ";
            params.add(endTime);
        }
        if(type > 0){
            sql += "and a.type = ? ";
            params.add(type);
        }
        sql += " order by mtime desc";
        return this.commonDao.selectList(sql,ClubScoreLog.class,params.toArray());
    }


    @Override
    public long insertUserRemark(UserRemark userRemark) {
        this.commonDao.save(userRemark);
        return userRemark.getId();
    }

    @Override
    public void deleteUserRemark(long id) {
        this.commonDao.delete(id, UserRemark.class);
    }

    @Override
    public void updateUserRemark(UserRemark userRemark) {
        this.put2EntityCache(userRemark);
        if(ServerConfig.immediateSave) {
            this.commonDao.update(userRemark);
        } else {
            this.dbService.submitUpdate2Queue(userRemark);
        }
    }

    @Override
    public UserRemark selectUserRemark(int userId,int remarkUserId) {
        String sql = "select * from t_user_remark where user_id = ? and remark_user_id = ? ";
        return this.commonDao.selectOne(sql,UserRemark.class,userId,remarkUserId);
    }

    @Override
    public List<UserRemark> selectUserRemarkByUserId(int userId) {
	    String sql = "select * from t_user_remark where user_id = ? ";
        return this.commonDao.selectList(sql,UserRemark.class,userId);
    }

}
