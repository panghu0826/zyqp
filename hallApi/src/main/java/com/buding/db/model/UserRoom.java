package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRoom extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer ownerId;

    private String roomCode;

    private String roomPwd;

    private String roomName;

    private Integer roomState;

    private String gameId;

    private String matchId;

    private String wanfa;

    private String roomConfId;

    private Date lastActiveTime;

    private Integer usedCount;

    private Integer totalCount;

    private String params;

    private Date ctime;

    private Date mtime;

    private Integer limitMax;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode == null ? null : roomCode.trim();
    }

    public String getRoomPwd() {
        return roomPwd;
    }

    public void setRoomPwd(String roomPwd) {
        this.roomPwd = roomPwd == null ? null : roomPwd.trim();
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : roomName.trim();
    }

    public Integer getRoomState() {
        return roomState;
    }

    public void setRoomState(Integer roomState) {
        this.roomState = roomState;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId == null ? null : gameId.trim();
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId == null ? null : matchId.trim();
    }

    public String getWanfa() {
        return wanfa;
    }

    public void setWanfa(String wanfa) {
        this.wanfa = wanfa == null ? null : wanfa.trim();
    }

    public String getRoomConfId() {
        return roomConfId;
    }

    public void setRoomConfId(String roomConfId) {
        this.roomConfId = roomConfId == null ? null : roomConfId.trim();
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params == null ? null : params.trim();
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Integer getLimitMax() {
        return limitMax;
    }

    public void setLimitMax(Integer limitMax) {
        this.limitMax = limitMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoom userRoom = (UserRoom) o;

        if (id != null ? !id.equals(userRoom.id) : userRoom.id != null) return false;
        if (ownerId != null ? !ownerId.equals(userRoom.ownerId) : userRoom.ownerId != null) return false;
        if (roomCode != null ? !roomCode.equals(userRoom.roomCode) : userRoom.roomCode != null) return false;
        if (roomPwd != null ? !roomPwd.equals(userRoom.roomPwd) : userRoom.roomPwd != null) return false;
        if (roomName != null ? !roomName.equals(userRoom.roomName) : userRoom.roomName != null) return false;
        if (roomState != null ? !roomState.equals(userRoom.roomState) : userRoom.roomState != null) return false;
        if (gameId != null ? !gameId.equals(userRoom.gameId) : userRoom.gameId != null) return false;
        if (matchId != null ? !matchId.equals(userRoom.matchId) : userRoom.matchId != null) return false;
        if (wanfa != null ? !wanfa.equals(userRoom.wanfa) : userRoom.wanfa != null) return false;
        if (roomConfId != null ? !roomConfId.equals(userRoom.roomConfId) : userRoom.roomConfId != null) return false;
        if (lastActiveTime != null ? !lastActiveTime.equals(userRoom.lastActiveTime) : userRoom.lastActiveTime != null)
            return false;
        if (usedCount != null ? !usedCount.equals(userRoom.usedCount) : userRoom.usedCount != null) return false;
        if (totalCount != null ? !totalCount.equals(userRoom.totalCount) : userRoom.totalCount != null) return false;
        if (params != null ? !params.equals(userRoom.params) : userRoom.params != null) return false;
        if (ctime != null ? !ctime.equals(userRoom.ctime) : userRoom.ctime != null) return false;
        if (mtime != null ? !mtime.equals(userRoom.mtime) : userRoom.mtime != null) return false;
        return limitMax != null ? limitMax.equals(userRoom.limitMax) : userRoom.limitMax == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        result = 31 * result + (roomCode != null ? roomCode.hashCode() : 0);
        result = 31 * result + (roomPwd != null ? roomPwd.hashCode() : 0);
        result = 31 * result + (roomName != null ? roomName.hashCode() : 0);
        result = 31 * result + (roomState != null ? roomState.hashCode() : 0);
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (matchId != null ? matchId.hashCode() : 0);
        result = 31 * result + (wanfa != null ? wanfa.hashCode() : 0);
        result = 31 * result + (roomConfId != null ? roomConfId.hashCode() : 0);
        result = 31 * result + (lastActiveTime != null ? lastActiveTime.hashCode() : 0);
        result = 31 * result + (usedCount != null ? usedCount.hashCode() : 0);
        result = 31 * result + (totalCount != null ? totalCount.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (ctime != null ? ctime.hashCode() : 0);
        result = 31 * result + (mtime != null ? mtime.hashCode() : 0);
        result = 31 * result + (limitMax != null ? limitMax.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserRoom{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", roomCode='" + roomCode + '\'' +
                ", roomPwd='" + roomPwd + '\'' +
                ", roomName='" + roomName + '\'' +
                ", roomState=" + roomState +
                ", gameId='" + gameId + '\'' +
                ", matchId='" + matchId + '\'' +
                ", wanfa='" + wanfa + '\'' +
                ", roomConfId='" + roomConfId + '\'' +
                ", lastActiveTime=" + lastActiveTime +
                ", usedCount=" + usedCount +
                ", totalCount=" + totalCount +
                ", params='" + params + '\'' +
                ", ctime=" + ctime +
                ", mtime=" + mtime +
                ", limitMax=" + limitMax +
                '}';
    }
}