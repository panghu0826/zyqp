package com.buding.hall.module.msg.dao;

import java.util.Date;
import java.util.List;

import com.buding.db.model.ActNotice;
import com.buding.db.model.Marquee;
import com.buding.db.model.Msg;
import com.buding.db.model.UserMsg;

public interface MsgDao {
	public UserMsg getUserMsg(int userId, long msgId);
	public List<UserMsg> getUserMsg(int userId);
	public List<ActNotice> getActAndNoticeList();
	public void update(UserMsg msg);
	public long insert(UserMsg msg);
	public long insertMsg(Msg msg);
	public void update(Msg msg);
	public Msg getMsg(long msgId);
	public List<Msg> listShareMsg(Date date);

	public List<Marquee> getMarqueeList();
	public Marquee getMarquee(long id);
	public long insertMarquee(Marquee msg);

	List<UserMsg> getAllUserMsg(int userId);

	void removeAllMarquee();

    void delMsgByDay();
}
