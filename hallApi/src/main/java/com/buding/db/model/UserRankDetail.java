package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRankDetail extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private String gameId;

    private Integer rankNum;

    private Integer point;

    private Integer pointType;

    private Long groupDatetime;

    private Date ctime;

    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId == null ? null : gameId.trim();
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getPointType() {
        return pointType;
    }

    public void setPointType(Integer pointType) {
        this.pointType = pointType;
    }

    public Long getGroupDatetime() {
        return groupDatetime;
    }

    public void setGroupDatetime(Long groupDatetime) {
        this.groupDatetime = groupDatetime;
    }

    public Integer getRankNum() {
        return rankNum;
    }

    public void setRankNum(Integer rankNum) {
        this.rankNum = rankNum;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRankDetail detail = (UserRankDetail) o;

        if (id != null ? !id.equals(detail.id) : detail.id != null) return false;
        if (userId != null ? !userId.equals(detail.userId) : detail.userId != null) return false;
        if (gameId != null ? !gameId.equals(detail.gameId) : detail.gameId != null) return false;
        if (rankNum != null ? !rankNum.equals(detail.rankNum) : detail.rankNum != null) return false;
        if (point != null ? !point.equals(detail.point) : detail.point != null) return false;
        if (pointType != null ? !pointType.equals(detail.pointType) : detail.pointType != null) return false;
        if (groupDatetime != null ? !groupDatetime.equals(detail.groupDatetime) : detail.groupDatetime != null)
            return false;
        if (ctime != null ? !ctime.equals(detail.ctime) : detail.ctime != null) return false;
        return mtime != null ? mtime.equals(detail.mtime) : detail.mtime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (rankNum != null ? rankNum.hashCode() : 0);
        result = 31 * result + (point != null ? point.hashCode() : 0);
        result = 31 * result + (pointType != null ? pointType.hashCode() : 0);
        result = 31 * result + (groupDatetime != null ? groupDatetime.hashCode() : 0);
        result = 31 * result + (ctime != null ? ctime.hashCode() : 0);
        result = 31 * result + (mtime != null ? mtime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserRankDetail{" +
                "id=" + id +
                ", userId=" + userId +
                ", gameId='" + gameId + '\'' +
                ", rankNum='" + rankNum + '\'' +
                ", point=" + point +
                ", pointType=" + pointType +
                ", groupDatetime=" + groupDatetime +
                ", ctime=" + ctime +
                ", mtime=" + mtime +
                '}';
    }
}