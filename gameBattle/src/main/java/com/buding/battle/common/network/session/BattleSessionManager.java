package com.buding.battle.common.network.session;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import org.springframework.stereotype.Component;

import com.buding.battle.logic.module.common.OnlineStatus;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.common.network.session.SessionManager;

import java.util.List;

@Component
public class BattleSessionManager extends SessionManager<BattleSession> {
	@Override
	public void schedule2Remove(BattleSession session) {
		log.info("mark session as offline:{}", session.userId);
		session.onlineStatus = OnlineStatus.OFFLINE;
		if(cleanSession(session)) { //尝试立刻清除
			return;
		}
		//放入定时计划里面清除
		super.schedule2Remove(session);
	}

	/**
	 * 1.不在游戏或者未准备的玩家若之前状态是在赛场中,强制退出赛场返回大厅,同时清除session和连接释放资源
	 * 2.在游戏中的提示掉线,不清除老session状态,关闭老session连接释放资源
	 */
	@Override
	public boolean cleanSession(BattleSession session) {
		BattleSession currentSession = getIoSession(session.userId);

		if((currentSession == null || !currentSession.channel.isOpen())){
            List<CommonDesk> deskList = session.getNotSitDesk(session.userId);
            if(deskList != null && !deskList.isEmpty()) {
                for (CommonDesk desk : deskList) {
                    if (desk != null) {
                        if (session.player != null)
                            log.info("--桌子id--" + desk.getDeskID() + "玩家--" + session.player.name + "----离线,离开之前观战桌子--");
                        desk.playerExit(session.userId, PlayerExitType.REQUEST_EXIT);
//                        session.getDeskMap().remove(desk.getDeskID());
                    }
                }
            }
			CommonDesk desk = session.getPlayingOrReadyDesk(session.userId);
			if(desk != null) {
				PlayerInfo p = desk.getDeskPlayerById(session.userId);
				if(p!=null) {
                    log.info("--桌子id--" + desk.getDeskID() + "玩家--" + p.name + "----离线,session状态--" + session.getStatus());
                }
                if(p != null && p.position >= 0) desk.onPlayerOfflinePacketReceived(session.userId);
			}
			if(session.channel.isOpen()){
				session.channel.close();
			}
			return false;
		}
		return super.cleanSession(session);
	}
}
