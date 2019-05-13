package com.buding.db.dao;

import java.util.Date;
import java.util.List;

import com.buding.db.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.hall.module.msg.dao.MsgDao;

public class MsgDaoImpl extends CachedServiceAdpter implements MsgDao {
	@Autowired
	private DbService dbService;
	
	@Override
	public UserMsg getUserMsg(int userId, long msgId) {
		List<UserMsg> list = this.getList("select * from user_msg where user_id = ? and msg_id = ? ", UserMsg.class, userId, msgId);
		return list.isEmpty()? null : list.get(0);
	}

	@Override
	public List<UserMsg> getUserMsg(int userId) {
		List<UserMsg> list = this.getList("select * from user_msg where user_id = ?", UserMsg.class, userId);
		return list;
	}

	@Override
	public void update(UserMsg msg) {
		this.put2EntityCache(msg);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(msg);
		} else {
			this.dbService.submitUpdate2Queue(msg);
		}
	}

	@Override
	public void update(Msg msg) {
		this.put2EntityCache(msg);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(msg);
		} else {
			this.dbService.submitUpdate2Queue(msg);
		}
	}

	@Override
	public long insert(UserMsg msg) {
		this.commonDao.save(msg);
		return msg.getId();
	}

	@Override
	public Msg getMsg(long msgId) {
		return this.get(msgId, Msg.class);
	}

	@Override
	public long insertMsg(Msg msg) {
		this.commonDao.save(msg);
		return msg.getId();
	}

	@Override
	public List<Msg> listShareMsg(Date date) {
		return this.getList("select * from msg where stop_date_time > ? and target_type = 2 ", Msg.class, date);
	}

	@Override
	public List<ActNotice> getActAndNoticeList() {
		return this.commonDao.selectList("select * from act_notice where status = 1  order by id desc ", ActNotice.class);
	}

	@Override
	public List<Marquee> getMarqueeList() {
		return this.getList("select * from marquee where end_time > ? and status = 1 ", Marquee.class, new Date());
	}

	@Override
	public long insertMarquee(Marquee msg) {
		this.commonDao.save(msg);
		return msg.getId();
	}

	@Override
	public List<UserMsg> getAllUserMsg(int userId) {
		List<UserMsg> list = this.getList("select * from user_msg where user_id = ?", UserMsg.class, userId);
		return list;
	}

	@Override
	public void removeAllMarquee() {
		this.commonDao.delete(null,Marquee.class);
	}

	@Override
	public void delMsgByDay() {
		this.commonDao.delete(null,Msg.class);
		this.commonDao.delete(null,UserMsg.class);
	}

	@Override
	public Marquee getMarquee(long id) {
		return this.commonDao.get(id, Marquee.class);
	}
	
}
