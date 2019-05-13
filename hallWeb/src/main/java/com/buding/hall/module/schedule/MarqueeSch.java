package com.buding.hall.module.schedule;

import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.ws.MsgPortalService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

public class MarqueeSch {
    @Autowired
    MsgDao msgDao;

    @Autowired
    MsgPortalService msgPortalService;

    public void removeAllMarquee() {
        msgDao.removeAllMarquee();
        msgPortalService.removeAllMarquee();
    }

}
