package com.buding.hall.module.schedule;

import com.buding.hall.module.award.dao.AwardDao;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.vip.dao.UserRoomDao;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRoomSch {

    @Autowired
    UserRoomDao userRoomDao;
    /**
     * 只保留2天数据
     */
    public void delUserRoomByDay() {
        //删除 user_room user_room_game_track user_room_result user_room_result_detail
        userRoomDao.delUserRoomByDay();
    }
}
