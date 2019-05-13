package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import org.apache.zookeeper.data.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ClubScoreLog extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clubId;

    private Integer playerId;

    private String playerName;

    private Integer scoreModify;

    private Integer scoreLeft;

    private Date mtime;

    private Integer type;

    private String info;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getScoreModify() {
        return scoreModify;
    }

    public void setScoreModify(Integer scoreModify) {
        this.scoreModify = scoreModify;
    }

    public Integer getScoreLeft() {
        return scoreLeft;
    }

    public void setScoreLeft(Integer scoreLeft) {
        this.scoreLeft = scoreLeft;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return "ClubScoreLog{" +
                "id=" + id +
                ", clubId=" + clubId +
                ", playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", scoreModify=" + scoreModify +
                ", scoreLeft=" + scoreLeft +
                ", mtime=" + mtime +
                ", type=" + type +
                ", info='" + info + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClubScoreLog that = (ClubScoreLog) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(clubId, that.clubId) &&
                Objects.equals(playerId, that.playerId) &&
                Objects.equals(playerName, that.playerName) &&
                Objects.equals(scoreModify, that.scoreModify) &&
                Objects.equals(scoreLeft, that.scoreLeft) &&
                Objects.equals(mtime, that.mtime) &&
                Objects.equals(type, that.type) &&
                Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, clubId, playerId, playerName, scoreModify, scoreLeft, mtime, type, info);
    }

}