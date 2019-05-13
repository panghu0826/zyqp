package com.buding.db.model;

import java.io.Serializable;
import java.util.Date;

import com.buding.common.db.model.BaseModel;

public class RobotSetting extends BaseModel<Integer> implements Serializable {
    private Integer id;

    private String matchId;

    private Integer robotPlaying;

    private Integer robotWaiting;

    private Long totalPlayed;

    private Long totalWined;

    private Long totalEarnCoin;

    private Long totalLostCoin;

    private Integer minInit;

    private Integer totalRobot;

    private String idRange;

    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId == null ? null : matchId.trim();
    }

    public Integer getRobotPlaying() {
        return robotPlaying;
    }

    public void setRobotPlaying(Integer robotPlaying) {
        this.robotPlaying = robotPlaying;
    }

    public Integer getRobotWaiting() {
        return robotWaiting;
    }

    public void setRobotWaiting(Integer robotWaiting) {
        this.robotWaiting = robotWaiting;
    }

    public Long getTotalPlayed() {
        return totalPlayed;
    }

    public void setTotalPlayed(Long totalPlayed) {
        this.totalPlayed = totalPlayed;
    }

    public Long getTotalWined() {
        return totalWined;
    }

    public void setTotalWined(Long totalWined) {
        this.totalWined = totalWined;
    }

    public Long getTotalEarnCoin() {
        return totalEarnCoin;
    }

    public void setTotalEarnCoin(Long totalEarnCoin) {
        this.totalEarnCoin = totalEarnCoin;
    }

    public Long getTotalLostCoin() {
        return totalLostCoin;
    }

    public void setTotalLostCoin(Long totalLostCoin) {
        this.totalLostCoin = totalLostCoin;
    }

    public Integer getMinInit() {
        return minInit;
    }

    public void setMinInit(Integer minInit) {
        this.minInit = minInit;
    }

    public Integer getTotalRobot() {
        return totalRobot;
    }

    public void setTotalRobot(Integer totalRobot) {
        this.totalRobot = totalRobot;
    }

    public String getIdRange() {
        return idRange;
    }

    public void setIdRange(String idRange) {
        this.idRange = idRange == null ? null : idRange.trim();
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
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
        RobotSetting other = (RobotSetting) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMatchId() == null ? other.getMatchId() == null : this.getMatchId().equals(other.getMatchId()))
            && (this.getRobotPlaying() == null ? other.getRobotPlaying() == null : this.getRobotPlaying().equals(other.getRobotPlaying()))
            && (this.getRobotWaiting() == null ? other.getRobotWaiting() == null : this.getRobotWaiting().equals(other.getRobotWaiting()))
            && (this.getTotalPlayed() == null ? other.getTotalPlayed() == null : this.getTotalPlayed().equals(other.getTotalPlayed()))
            && (this.getTotalWined() == null ? other.getTotalWined() == null : this.getTotalWined().equals(other.getTotalWined()))
            && (this.getTotalEarnCoin() == null ? other.getTotalEarnCoin() == null : this.getTotalEarnCoin().equals(other.getTotalEarnCoin()))
            && (this.getTotalLostCoin() == null ? other.getTotalLostCoin() == null : this.getTotalLostCoin().equals(other.getTotalLostCoin()))
            && (this.getMinInit() == null ? other.getMinInit() == null : this.getMinInit().equals(other.getMinInit()))
            && (this.getTotalRobot() == null ? other.getTotalRobot() == null : this.getTotalRobot().equals(other.getTotalRobot()))
            && (this.getIdRange() == null ? other.getIdRange() == null : this.getIdRange().equals(other.getIdRange()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMatchId() == null) ? 0 : getMatchId().hashCode());
        result = prime * result + ((getRobotPlaying() == null) ? 0 : getRobotPlaying().hashCode());
        result = prime * result + ((getRobotWaiting() == null) ? 0 : getRobotWaiting().hashCode());
        result = prime * result + ((getTotalPlayed() == null) ? 0 : getTotalPlayed().hashCode());
        result = prime * result + ((getTotalWined() == null) ? 0 : getTotalWined().hashCode());
        result = prime * result + ((getTotalEarnCoin() == null) ? 0 : getTotalEarnCoin().hashCode());
        result = prime * result + ((getTotalLostCoin() == null) ? 0 : getTotalLostCoin().hashCode());
        result = prime * result + ((getMinInit() == null) ? 0 : getMinInit().hashCode());
        result = prime * result + ((getTotalRobot() == null) ? 0 : getTotalRobot().hashCode());
        result = prime * result + ((getIdRange() == null) ? 0 : getIdRange().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", matchId=").append(matchId);
        sb.append(", robotPlaying=").append(robotPlaying);
        sb.append(", robotWaiting=").append(robotWaiting);
        sb.append(", totalPlayed=").append(totalPlayed);
        sb.append(", totalWined=").append(totalWined);
        sb.append(", totalEarnCoin=").append(totalEarnCoin);
        sb.append(", totalLostCoin=").append(totalLostCoin);
        sb.append(", minInit=").append(minInit);
        sb.append(", totalRobot=").append(totalRobot);
        sb.append(", idRange=").append(idRange);
        sb.append(", mtime=").append(mtime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}