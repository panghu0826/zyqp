package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserGameOutline extends BaseModel<Integer> implements Serializable {
    private Integer id;

    private Integer winCount;

    private Integer loseCount;

    private Integer totalCount;

    private Integer continueWinCount;

    private Integer maxFanType;

    private String maxFanDesc;

    private Integer maxFanNum;

    private String maxFanHandcards;

    private String maxFanDowncards;

    private Date lastGameTime;

    private String lastGameMatch;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWinCount() {
        return winCount;
    }

    public void setWinCount(Integer winCount) {
        this.winCount = winCount;
    }

    public Integer getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(Integer loseCount) {
        this.loseCount = loseCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getContinueWinCount() {
        return continueWinCount;
    }

    public void setContinueWinCount(Integer continueWinCount) {
        this.continueWinCount = continueWinCount;
    }

    public Integer getMaxFanType() {
        return maxFanType;
    }

    public void setMaxFanType(Integer maxFanType) {
        this.maxFanType = maxFanType;
    }

    public String getMaxFanDesc() {
        return maxFanDesc;
    }

    public void setMaxFanDesc(String maxFanDesc) {
        this.maxFanDesc = maxFanDesc == null ? null : maxFanDesc.trim();
    }

    public Integer getMaxFanNum() {
        return maxFanNum;
    }

    public void setMaxFanNum(Integer maxFanNum) {
        this.maxFanNum = maxFanNum;
    }

    public String getMaxFanHandcards() {
        return maxFanHandcards;
    }

    public void setMaxFanHandcards(String maxFanHandcards) {
        this.maxFanHandcards = maxFanHandcards == null ? null : maxFanHandcards.trim();
    }

    public String getMaxFanDowncards() {
        return maxFanDowncards;
    }

    public void setMaxFanDowncards(String maxFanDowncards) {
        this.maxFanDowncards = maxFanDowncards == null ? null : maxFanDowncards.trim();
    }

    public Date getLastGameTime() {
        return lastGameTime;
    }

    public void setLastGameTime(Date lastGameTime) {
        this.lastGameTime = lastGameTime;
    }

    public String getLastGameMatch() {
        return lastGameMatch;
    }

    public void setLastGameMatch(String lastGameMatch) {
        this.lastGameMatch = lastGameMatch == null ? null : lastGameMatch.trim();
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
        UserGameOutline other = (UserGameOutline) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWinCount() == null ? other.getWinCount() == null : this.getWinCount().equals(other.getWinCount()))
            && (this.getLoseCount() == null ? other.getLoseCount() == null : this.getLoseCount().equals(other.getLoseCount()))
            && (this.getTotalCount() == null ? other.getTotalCount() == null : this.getTotalCount().equals(other.getTotalCount()))
            && (this.getContinueWinCount() == null ? other.getContinueWinCount() == null : this.getContinueWinCount().equals(other.getContinueWinCount()))
            && (this.getMaxFanType() == null ? other.getMaxFanType() == null : this.getMaxFanType().equals(other.getMaxFanType()))
            && (this.getMaxFanDesc() == null ? other.getMaxFanDesc() == null : this.getMaxFanDesc().equals(other.getMaxFanDesc()))
            && (this.getMaxFanNum() == null ? other.getMaxFanNum() == null : this.getMaxFanNum().equals(other.getMaxFanNum()))
            && (this.getMaxFanHandcards() == null ? other.getMaxFanHandcards() == null : this.getMaxFanHandcards().equals(other.getMaxFanHandcards()))
            && (this.getMaxFanDowncards() == null ? other.getMaxFanDowncards() == null : this.getMaxFanDowncards().equals(other.getMaxFanDowncards()))
            && (this.getLastGameTime() == null ? other.getLastGameTime() == null : this.getLastGameTime().equals(other.getLastGameTime()))
            && (this.getLastGameMatch() == null ? other.getLastGameMatch() == null : this.getLastGameMatch().equals(other.getLastGameMatch()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWinCount() == null) ? 0 : getWinCount().hashCode());
        result = prime * result + ((getLoseCount() == null) ? 0 : getLoseCount().hashCode());
        result = prime * result + ((getTotalCount() == null) ? 0 : getTotalCount().hashCode());
        result = prime * result + ((getContinueWinCount() == null) ? 0 : getContinueWinCount().hashCode());
        result = prime * result + ((getMaxFanType() == null) ? 0 : getMaxFanType().hashCode());
        result = prime * result + ((getMaxFanDesc() == null) ? 0 : getMaxFanDesc().hashCode());
        result = prime * result + ((getMaxFanNum() == null) ? 0 : getMaxFanNum().hashCode());
        result = prime * result + ((getMaxFanHandcards() == null) ? 0 : getMaxFanHandcards().hashCode());
        result = prime * result + ((getMaxFanDowncards() == null) ? 0 : getMaxFanDowncards().hashCode());
        result = prime * result + ((getLastGameTime() == null) ? 0 : getLastGameTime().hashCode());
        result = prime * result + ((getLastGameMatch() == null) ? 0 : getLastGameMatch().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", winCount=").append(winCount);
        sb.append(", loseCount=").append(loseCount);
        sb.append(", totalCount=").append(totalCount);
        sb.append(", continueWinCount=").append(continueWinCount);
        sb.append(", maxFanType=").append(maxFanType);
        sb.append(", maxFanDesc=").append(maxFanDesc);
        sb.append(", maxFanNum=").append(maxFanNum);
        sb.append(", maxFanHandcards=").append(maxFanHandcards);
        sb.append(", maxFanDowncards=").append(maxFanDowncards);
        sb.append(", lastGameTime=").append(lastGameTime);
        sb.append(", lastGameMatch=").append(lastGameMatch);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}