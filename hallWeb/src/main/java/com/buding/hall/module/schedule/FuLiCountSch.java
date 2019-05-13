package com.buding.hall.module.schedule;

import com.buding.db.model.FuLiCount;
import com.buding.hall.module.vip.dao.UserRoomDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FuLiCountSch {
    protected Logger logger = LogManager.getLogger(getClass());

    @Autowired
    UserRoomDao userRoomDao;

    public void count() {
        logger.info("开始统计福利玩家");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = new Date();
            d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-09-27 01:00:00");
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(d);
            rightNow.add(Calendar.DAY_OF_MONTH,-1);
            d = rightNow.getTime();

            String stdate = sdf.format(d);
            String endDate = stdate + " 23:59:59";

            Date date = sdf.parse(stdate);
            List<Integer> fuLiUsers = userRoomDao.getfuLiPlayerList("G_ZJH");
            for (Integer userId : fuLiUsers) {
                FuLiCount count = new FuLiCount();
                count.setPlayerId(userId);
                count.setCountDate(date);
                count.setNum(userRoomDao.getFuliCount(userId,stdate,endDate));
                userRoomDao.insertFuliCount(count);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
