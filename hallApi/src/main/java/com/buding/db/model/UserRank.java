package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRank extends BaseModel<Long> implements Serializable {
    private Long id;

    private Long auditId;

    private Integer userId;

    private String userName;

    private Integer rankPoint;

    private Integer rank;

    private Boolean awardStatus;

    private Integer vipType;

    private String awardDesc;

    private String awards;

    private String groupId;

    private String gameId;

    private Long rankGrpTime;

    private Date mtime;

    private Date ctime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public Integer getRankPoint() {
        return rankPoint;
    }

    public void setRankPoint(Integer rankPoint) {
        this.rankPoint = rankPoint;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Boolean getAwardStatus() {
        return awardStatus;
    }

    public void setAwardStatus(Boolean awardStatus) {
        this.awardStatus = awardStatus;
    }

    public Integer getVipType() {
        return vipType;
    }

    public void setVipType(Integer vipType) {
        this.vipType = vipType;
    }

    public String getAwardDesc() {
        return awardDesc;
    }

    public void setAwardDesc(String awardDesc) {
        this.awardDesc = awardDesc == null ? null : awardDesc.trim();
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards == null ? null : awards.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public Long getRankGrpTime() {
        return rankGrpTime;
    }

    public void setRankGrpTime(Long rankGrpTime) {
        this.rankGrpTime = rankGrpTime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRank userRank = (UserRank) o;

        if (id != null ? !id.equals(userRank.id) : userRank.id != null) return false;
        if (auditId != null ? !auditId.equals(userRank.auditId) : userRank.auditId != null) return false;
        if (userId != null ? !userId.equals(userRank.userId) : userRank.userId != null) return false;
        if (userName != null ? !userName.equals(userRank.userName) : userRank.userName != null) return false;
        if (rankPoint != null ? !rankPoint.equals(userRank.rankPoint) : userRank.rankPoint != null) return false;
        if (rank != null ? !rank.equals(userRank.rank) : userRank.rank != null) return false;
        if (awardStatus != null ? !awardStatus.equals(userRank.awardStatus) : userRank.awardStatus != null)
            return false;
        if (vipType != null ? !vipType.equals(userRank.vipType) : userRank.vipType != null) return false;
        if (awardDesc != null ? !awardDesc.equals(userRank.awardDesc) : userRank.awardDesc != null) return false;
        if (awards != null ? !awards.equals(userRank.awards) : userRank.awards != null) return false;
        if (groupId != null ? !groupId.equals(userRank.groupId) : userRank.groupId != null) return false;
        if (gameId != null ? !gameId.equals(userRank.gameId) : userRank.gameId != null) return false;
        if (rankGrpTime != null ? !rankGrpTime.equals(userRank.rankGrpTime) : userRank.rankGrpTime != null)
            return false;
        if (mtime != null ? !mtime.equals(userRank.mtime) : userRank.mtime != null) return false;
        return ctime != null ? ctime.equals(userRank.ctime) : userRank.ctime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (auditId != null ? auditId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (rankPoint != null ? rankPoint.hashCode() : 0);
        result = 31 * result + (rank != null ? rank.hashCode() : 0);
        result = 31 * result + (awardStatus != null ? awardStatus.hashCode() : 0);
        result = 31 * result + (vipType != null ? vipType.hashCode() : 0);
        result = 31 * result + (awardDesc != null ? awardDesc.hashCode() : 0);
        result = 31 * result + (awards != null ? awards.hashCode() : 0);
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (rankGrpTime != null ? rankGrpTime.hashCode() : 0);
        result = 31 * result + (mtime != null ? mtime.hashCode() : 0);
        result = 31 * result + (ctime != null ? ctime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserRank{" +
                "id=" + id +
                ", auditId=" + auditId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", rankPoint=" + rankPoint +
                ", rank=" + rank +
                ", awardStatus=" + awardStatus +
                ", vipType=" + vipType +
                ", awardDesc='" + awardDesc + '\'' +
                ", awards='" + awards + '\'' +
                ", groupId='" + groupId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", rankGrpTime=" + rankGrpTime +
                ", mtime=" + mtime +
                ", ctime=" + ctime +
                '}';
    }
}