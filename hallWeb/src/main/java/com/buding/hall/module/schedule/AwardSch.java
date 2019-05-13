package com.buding.hall.module.schedule;

import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.msg.dao.MsgDao;
import org.springframework.beans.factory.annotation.Autowired;

public class AwardSch {

    @Autowired
    MsgDao msgDao;

    @Autowired
    AwardDao awardDao;

    /**
     * 只保留2天数据
     */
    public void delPrayAwardByDay() {
        //删除 award user_award
        awardDao.delAwardByDay();

        //删除 msg user_msg.
        msgDao.delMsgByDay();

    }
}
