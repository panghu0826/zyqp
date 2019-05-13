package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class GameLog extends BaseModel<Long> implements Serializable {
    private Long id;

    private String gameId;

    private String matchId;

    private String roomId;

    private String deskId;

    private Integer user1Id;

    private Integer user2Id;

    private Integer user3Id;

    private Integer user4Id;

    private Integer user1Score;

    private Integer user2Score;

    private Integer user3Score;

    private Integer user4Score;

    private Integer user1FanNum;

    private Integer user2FanNum;

    private Integer user3FanNum;

    private Integer user4FanNum;

    private String user1FanDesc;

    private String user2FanDesc;

    private String user3FanDesc;

    private String user4FanDesc;

    private Date gameStartTime;

    private Date gameEndTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId == null ? null : roomId.trim();
    }

    public String getDeskId() {
        return deskId;
    }

    public void setDeskId(String deskId) {
        this.deskId = deskId == null ? null : deskId.trim();
    }

    public Integer getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }

    public Integer getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }

    public Integer getUser3Id() {
        return user3Id;
    }

    public void setUser3Id(Integer user3Id) {
        this.user3Id = user3Id;
    }

    public Integer getUser4Id() {
        return user4Id;
    }

    public void setUser4Id(Integer user4Id) {
        this.user4Id = user4Id;
    }

    public Integer getUser1Score() {
        return user1Score;
    }

    public void setUser1Score(Integer user1Score) {
        this.user1Score = user1Score;
    }

    public Integer getUser2Score() {
        return user2Score;
    }

    public void setUser2Score(Integer user2Score) {
        this.user2Score = user2Score;
    }

    public Integer getUser3Score() {
        return user3Score;
    }

    public void setUser3Score(Integer user3Score) {
        this.user3Score = user3Score;
    }

    public Integer getUser4Score() {
        return user4Score;
    }

    public void setUser4Score(Integer user4Score) {
        this.user4Score = user4Score;
    }

    public Integer getUser1FanNum() {
        return user1FanNum;
    }

    public void setUser1FanNum(Integer user1FanNum) {
        this.user1FanNum = user1FanNum;
    }

    public Integer getUser2FanNum() {
        return user2FanNum;
    }

    public void setUser2FanNum(Integer user2FanNum) {
        this.user2FanNum = user2FanNum;
    }

    public Integer getUser3FanNum() {
        return user3FanNum;
    }

    public void setUser3FanNum(Integer user3FanNum) {
        this.user3FanNum = user3FanNum;
    }

    public Integer getUser4FanNum() {
        return user4FanNum;
    }

    public void setUser4FanNum(Integer user4FanNum) {
        this.user4FanNum = user4FanNum;
    }

    public String getUser1FanDesc() {
        return user1FanDesc;
    }

    public void setUser1FanDesc(String user1FanDesc) {
        this.user1FanDesc = user1FanDesc == null ? null : user1FanDesc.trim();
    }

    public String getUser2FanDesc() {
        return user2FanDesc;
    }

    public void setUser2FanDesc(String user2FanDesc) {
        this.user2FanDesc = user2FanDesc == null ? null : user2FanDesc.trim();
    }

    public String getUser3FanDesc() {
        return user3FanDesc;
    }

    public void setUser3FanDesc(String user3FanDesc) {
        this.user3FanDesc = user3FanDesc == null ? null : user3FanDesc.trim();
    }

    public String getUser4FanDesc() {
        return user4FanDesc;
    }

    public void setUser4FanDesc(String user4FanDesc) {
        this.user4FanDesc = user4FanDesc == null ? null : user4FanDesc.trim();
    }

    public Date getGameStartTime() {
        return gameStartTime;
    }

    public void setGameStartTime(Date gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    public Date getGameEndTime() {
        return gameEndTime;
    }

    public void setGameEndTime(Date gameEndTime) {
        this.gameEndTime = gameEndTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        GameLog other = (GameLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getMatchId() == null ? other.getMatchId() == null : this.getMatchId().equals(other.getMatchId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getDeskId() == null ? other.getDeskId() == null : this.getDeskId().equals(other.getDeskId()))
            && (this.getUser1Id() == null ? other.getUser1Id() == null : this.getUser1Id().equals(other.getUser1Id()))
            && (this.getUser2Id() == null ? other.getUser2Id() == null : this.getUser2Id().equals(other.getUser2Id()))
            && (this.getUser3Id() == null ? other.getUser3Id() == null : this.getUser3Id().equals(other.getUser3Id()))
            && (this.getUser4Id() == null ? other.getUser4Id() == null : this.getUser4Id().equals(other.getUser4Id()))
            && (this.getUser1Score() == null ? other.getUser1Score() == null : this.getUser1Score().equals(other.getUser1Score()))
            && (this.getUser2Score() == null ? other.getUser2Score() == null : this.getUser2Score().equals(other.getUser2Score()))
            && (this.getUser3Score() == null ? other.getUser3Score() == null : this.getUser3Score().equals(other.getUser3Score()))
            && (this.getUser4Score() == null ? other.getUser4Score() == null : this.getUser4Score().equals(other.getUser4Score()))
            && (this.getUser1FanNum() == null ? other.getUser1FanNum() == null : this.getUser1FanNum().equals(other.getUser1FanNum()))
            && (this.getUser2FanNum() == null ? other.getUser2FanNum() == null : this.getUser2FanNum().equals(other.getUser2FanNum()))
            && (this.getUser3FanNum() == null ? other.getUser3FanNum() == null : this.getUser3FanNum().equals(other.getUser3FanNum()))
            && (this.getUser4FanNum() == null ? other.getUser4FanNum() == null : this.getUser4FanNum().equals(other.getUser4FanNum()))
            && (this.getUser1FanDesc() == null ? other.getUser1FanDesc() == null : this.getUser1FanDesc().equals(other.getUser1FanDesc()))
            && (this.getUser2FanDesc() == null ? other.getUser2FanDesc() == null : this.getUser2FanDesc().equals(other.getUser2FanDesc()))
            && (this.getUser3FanDesc() == null ? other.getUser3FanDesc() == null : this.getUser3FanDesc().equals(other.getUser3FanDesc()))
            && (this.getUser4FanDesc() == null ? other.getUser4FanDesc() == null : this.getUser4FanDesc().equals(other.getUser4FanDesc()))
            && (this.getGameStartTime() == null ? other.getGameStartTime() == null : this.getGameStartTime().equals(other.getGameStartTime()))
            && (this.getGameEndTime() == null ? other.getGameEndTime() == null : this.getGameEndTime().equals(other.getGameEndTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getMatchId() == null) ? 0 : getMatchId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getDeskId() == null) ? 0 : getDeskId().hashCode());
        result = prime * result + ((getUser1Id() == null) ? 0 : getUser1Id().hashCode());
        result = prime * result + ((getUser2Id() == null) ? 0 : getUser2Id().hashCode());
        result = prime * result + ((getUser3Id() == null) ? 0 : getUser3Id().hashCode());
        result = prime * result + ((getUser4Id() == null) ? 0 : getUser4Id().hashCode());
        result = prime * result + ((getUser1Score() == null) ? 0 : getUser1Score().hashCode());
        result = prime * result + ((getUser2Score() == null) ? 0 : getUser2Score().hashCode());
        result = prime * result + ((getUser3Score() == null) ? 0 : getUser3Score().hashCode());
        result = prime * result + ((getUser4Score() == null) ? 0 : getUser4Score().hashCode());
        result = prime * result + ((getUser1FanNum() == null) ? 0 : getUser1FanNum().hashCode());
        result = prime * result + ((getUser2FanNum() == null) ? 0 : getUser2FanNum().hashCode());
        result = prime * result + ((getUser3FanNum() == null) ? 0 : getUser3FanNum().hashCode());
        result = prime * result + ((getUser4FanNum() == null) ? 0 : getUser4FanNum().hashCode());
        result = prime * result + ((getUser1FanDesc() == null) ? 0 : getUser1FanDesc().hashCode());
        result = prime * result + ((getUser2FanDesc() == null) ? 0 : getUser2FanDesc().hashCode());
        result = prime * result + ((getUser3FanDesc() == null) ? 0 : getUser3FanDesc().hashCode());
        result = prime * result + ((getUser4FanDesc() == null) ? 0 : getUser4FanDesc().hashCode());
        result = prime * result + ((getGameStartTime() == null) ? 0 : getGameStartTime().hashCode());
        result = prime * result + ((getGameEndTime() == null) ? 0 : getGameEndTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", gameId=").append(gameId);
        sb.append(", matchId=").append(matchId);
        sb.append(", roomId=").append(roomId);
        sb.append(", deskId=").append(deskId);
        sb.append(", user1Id=").append(user1Id);
        sb.append(", user2Id=").append(user2Id);
        sb.append(", user3Id=").append(user3Id);
        sb.append(", user4Id=").append(user4Id);
        sb.append(", user1Score=").append(user1Score);
        sb.append(", user2Score=").append(user2Score);
        sb.append(", user3Score=").append(user3Score);
        sb.append(", user4Score=").append(user4Score);
        sb.append(", user1FanNum=").append(user1FanNum);
        sb.append(", user2FanNum=").append(user2FanNum);
        sb.append(", user3FanNum=").append(user3FanNum);
        sb.append(", user4FanNum=").append(user4FanNum);
        sb.append(", user1FanDesc=").append(user1FanDesc);
        sb.append(", user2FanDesc=").append(user2FanDesc);
        sb.append(", user3FanDesc=").append(user3FanDesc);
        sb.append(", user4FanDesc=").append(user4FanDesc);
        sb.append(", gameStartTime=").append(gameStartTime);
        sb.append(", gameEndTime=").append(gameEndTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}