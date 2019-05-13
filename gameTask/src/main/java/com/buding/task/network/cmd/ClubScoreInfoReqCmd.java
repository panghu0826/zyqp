package com.buding.task.network.cmd;

import com.buding.db.model.*;
import com.buding.hall.module.chat.dao.ChatDao;
import com.buding.hall.module.club.dao.ClubDao;
import com.buding.hall.module.friend.dao.FriendDao;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.common.TaskConstants;
import com.buding.task.helper.TaskPushHelper;
import com.buding.task.network.TaskSessionManager;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.msgbase.MsgBase.PacketType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * 积分详情查询Req
 */
@Component
public class ClubScoreInfoReqCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
    UserDao userDao;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
    FriendDao friendDao;

    @Autowired
    ClubDao clubDao;

    @Autowired
    ChatDao chatDao;

    @Autowired
    TaskSessionManager sessionManager;

	@Override
	public void execute(CmdData data) throws Exception {
        CLUB.ClubScoreInfoReq ur = CLUB.ClubScoreInfoReq.parseFrom(data.packet.getData());
        logger.info("积分详情请求参数--"+ JsonFormat.printToString(ur.toBuilder().build()));
        ClubUser clubUser = clubDao.selectClubUser(ur.getClubId(),data.session.userId);
        if(clubUser == null){
            pushHelper.pushErrorMsg(data.session, PacketType.GlobalMsgSyn, "你不是该俱乐部成员");
            return;
		}
		long clubId = ur.getClubId();
		int playerId = (int) ur.getPlayerId();
		long startTime = ur.getStartTime();
        long endTime = ur.getEndTime();
		int type = ur.getType();
		int pageNum = ur.getPageNum();

        if(ur.getPlayerId() <= 0 && clubUser.getClubMemberType() <= TaskConstants.CLUB_MEMEBER_TYPE_COMMON){
            playerId = data.session.userId;
        }
		List<ClubScoreLog> list = clubDao.selectClubScoreLogList(clubId,playerId,startTime,endTime,type);
        logger.info("积分详情查询结果--"+list);

        List<ClubScoreLog> result = fenye(list,pageNum);
        logger.info("积分详情分页结果--"+result);

        CLUB.ClubScoreInfoRsp.Builder bu = CLUB.ClubScoreInfoRsp.newBuilder();
        for(ClubScoreLog log : result){
            CLUB.ClubScoreInfo.Builder i = CLUB.ClubScoreInfo.newBuilder();
            i.setPlayerId(log.getPlayerId());
            i.setPlayerName(log.getPlayerName());
            i.setModifyScore(log.getScoreModify());
            i.setLeftScore(log.getScoreLeft());
            i.setTime(log.getMtime().getTime());
            i.setType(log.getType());
            i.setInfo(log.getInfo());
            bu.addInfos(i.build());
        }
        logger.info("积分详情回复参数--"+JsonFormat.printToString(bu.build()));
        pushHelper.pushClubScoreInfoRsp(data.session,bu.build());
    }

    private List<ClubScoreLog> fenye(List<ClubScoreLog> roomResult, int pageNum) {
        List<ClubScoreLog> list = new ArrayList<>();
        if(roomResult == null || roomResult.isEmpty()) return list;
        int size = roomResult.size();
        int start = (pageNum - 1) * 20;
        int end = (start+20) < size ? (start+20) : size;
        for (int i = start; i < end; i++) {
            list.add(roomResult.get(i));
        }
        return list;
    }

	@Override
	public PacketType getKey() {
		return PacketType.ClubScoreInfoReq;
	}

}
