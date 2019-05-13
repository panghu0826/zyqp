package com.buding.hall.module.ws;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.ClubUser;
import com.buding.hall.module.game.model.DeskModel;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface TaskPortalService {
	// 停止服务
	public void stopService(String instanceId);

	// 恢复服务
	public void startService(String instanceId);
	
	public void closeSocket(int playerId);

    void startPray();

    void closePray();

    void pushClubRoomModelSyn(int synType,long clubId,int roomType,DeskModel deskModel);

    void pushMemberInfoSyn(ClubUser clubUser);

	void pushMemberInfoSyn(int userId);

    String applyClub(int userId, long clubId);
}
