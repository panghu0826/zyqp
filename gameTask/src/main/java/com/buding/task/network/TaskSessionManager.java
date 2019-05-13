package com.buding.task.network;

import com.buding.common.network.session.SessionManager;
import com.buding.common.network.session.SessionStatus;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.ws.TaskPortalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskSessionManager extends SessionManager<TaskSession> {
    @Autowired
    TaskPortalService taskPortalService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected boolean cleanSession(TaskSession session) {
        super.cleanSession(session);
        taskPortalService.pushMemberInfoSyn(session.userId);
        return true;
    }

    @Override
    public void put2OnlineList(int playerId, TaskSession session) {
        super.put2OnlineList(playerId,session);
        taskPortalService.pushMemberInfoSyn(session.userId);
    }
}
