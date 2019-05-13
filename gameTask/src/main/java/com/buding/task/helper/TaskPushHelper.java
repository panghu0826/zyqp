package com.buding.task.helper;

import com.buding.task.network.TaskSession;
import com.buding.task.network.TaskSessionManager;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.club.CLUB;
import packet.game.Hall;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

/**
 * @author jaime qq_1094086610
 * @Description:
 */
@Component
public class TaskPushHelper {
    @Autowired
    TaskSessionManager msgSessionManager;

    private Logger logger = LogManager.getLogger(getClass());

    public void pushErrorMsg(TaskSession session, PacketType type, String msg) {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(-1);
        pb.setPacketType(type);
        pb.setMsg(msg);
        msgSessionManager.write(session, pb.build().toByteArray());
    }

    public void pushPBMsg(int userId, PacketType type, ByteString data) {
        TaskSession session = msgSessionManager.getIoSession(userId);
        if (session != null) {
            pushPBMsg(session, type, data);
        }
    }

    public void pushPBMsg(TaskSession session, PacketType type, ByteString data) {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(0);
        pb.setPacketType(type);
        if (data != null) {
            pb.setData(data);
        }
        msgSessionManager.write(session, pb.build().toByteArray());
    }

    public void pushRoomResultResponse(TaskSession session, Hall.RoomResultResponse bb) {
        pushPBMsg(session, PacketType.RoomResultResponse, bb.toByteString());
    }

    public void pushAuthRsp(TaskSession session, PacketType type) {
        pushPBMsg(session, PacketType.AuthRequest, null);
    }

    public void pushClubInfoRsp(TaskSession session, CLUB.ClubInfoRsp build) {
        pushPBMsg(session, PacketType.ClubInfoRsp, build.toByteString());
    }

    public void pushClubSyn(int userId, CLUB.ClubSyn build) {
        pushPBMsg(userId, PacketType.ClubSyn, build.toByteString());
    }

    public void pushClubMemberRsp(TaskSession session, CLUB.ClubMemberRsp build) {
        pushPBMsg(session, PacketType.ClubMemberRsp, build.toByteString());
    }

    public void pushClubMemberRsp(int userId, CLUB.ClubMemberRsp build) {
        pushPBMsg(userId, PacketType.ClubMemberRsp, build.toByteString());
    }

    public void pushClubMemberSyn(int userId, CLUB.ClubMemberSyn build) {
        pushPBMsg(userId, PacketType.ClubMemberSyn, build.toByteString());
    }

    public void pushClubRoomListRsp(TaskSession session, CLUB.ClubRoomListRsp build) {
        pushPBMsg(session, PacketType.ClubRoomListRsp, build.toByteString());
    }

    public void pushClubConfigSyn(int userId, CLUB.ClubConfigSyn build) {
        pushPBMsg(userId, PacketType.ClubConfigSyn, build.toByteString());
    }

    public void pushApplyInfo(int userId, CLUB.ApplyInfo build) {
        pushPBMsg(userId, PacketType.ApplyInfo, build.toByteString());
    }

    public void pushChatListRsp(TaskSession session, CLUB.ChatListRsp build) {
        pushPBMsg(session, PacketType.ChatListRsp, build.toByteString());
    }

    public void pushChatListSyn(int userId, CLUB.ChatListSyn build) {
        pushPBMsg(userId, PacketType.ChatListSyn, build.toByteString());
    }

    public void pushChatContentRsp(TaskSession session, CLUB.ChatContentRsp build) {
        pushPBMsg(session, PacketType.ChatContentRsp, build.toByteString());
    }

    public void pushChatContentSyn(int userId, CLUB.ChatContentSyn build) {
        pushPBMsg(userId, PacketType.ChatContentSyn, build.toByteString());
    }

    public void pushFriendListRsp(TaskSession session, CLUB.FriendListRsp build) {
        pushPBMsg(session, PacketType.FriendListRsp, build.toByteString());
    }

    public void pushFriendSyn(int userId, CLUB.FriendSyn build) {
        pushPBMsg(userId, PacketType.FriendSyn, build.toByteString());
    }

    public void pushFriendSearchRsp(TaskSession session, CLUB.FriendSearchRsp build) {
        pushPBMsg(session, PacketType.FriendSearchRsp, build.toByteString());

    }

    public void pushApplyInfoRsp(TaskSession session, CLUB.ApplyInfoRsp build) {
        pushPBMsg(session, PacketType.ApplyInfoRsp, build.toByteString());
    }

    public void pushApplyInfoRsp(int userid, CLUB.ApplyInfoRsp build) {
        pushPBMsg(userid, PacketType.ApplyInfoRsp, build.toByteString());
    }

    public void pushClubRoomModelSyn(int userId, CLUB.ClubRoomModelSyn build) {
        pushPBMsg(userId, PacketType.ClubRoomModelSyn, build.toByteString());
    }

    public void pushClubScoreInfoRsp(TaskSession session, CLUB.ClubScoreInfoRsp build) {
        pushPBMsg(session, PacketType.ClubScoreInfoRsp, build.toByteString());
    }

    public void pushRemarkSyn(TaskSession session, CLUB.RemarkSyn build) {
        pushPBMsg(session, PacketType.RemarkSyn, build.toByteString());
    }

    public void pushUpdateRemarkRsp(TaskSession session, CLUB.UpdateRemarkRsp build) {
        pushPBMsg(session, PacketType.UpdateRemarkRsp, build.toByteString());
    }
}
