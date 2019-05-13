package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class RankAudit extends BaseModel<Long> implements Serializable {
    private Long id;

    private Long planSettleDate;

    private Long actualSettleDate;

    private Integer awardStatus;

    private String groupId;

    private Date ctime;

    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanSettleDate() {
        return planSettleDate;
    }

    public void setPlanSettleDate(Long planSettleDate) {
        this.planSettleDate = planSettleDate;
    }

    public Long getActualSettleDate() {
        return actualSettleDate;
    }

    public void setActualSettleDate(Long actualSettleDate) {
        this.actualSettleDate = actualSettleDate;
    }

    public Integer getAwardStatus() {
        return awardStatus;
    }

    public void setAwardStatus(Integer awardStatus) {
        this.awardStatus = awardStatus;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
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
        RankAudit other = (RankAudit) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanSettleDate() == null ? other.getPlanSettleDate() == null : this.getPlanSettleDate().equals(other.getPlanSettleDate()))
            && (this.getActualSettleDate() == null ? other.getActualSettleDate() == null : this.getActualSettleDate().equals(other.getActualSettleDate()))
            && (this.getAwardStatus() == null ? other.getAwardStatus() == null : this.getAwardStatus().equals(other.getAwardStatus()))
            && (this.getGroupId() == null ? other.getGroupId() == null : this.getGroupId().equals(other.getGroupId()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanSettleDate() == null) ? 0 : getPlanSettleDate().hashCode());
        result = prime * result + ((getActualSettleDate() == null) ? 0 : getActualSettleDate().hashCode());
        result = prime * result + ((getAwardStatus() == null) ? 0 : getAwardStatus().hashCode());
        result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
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
        sb.append(", planSettleDate=").append(planSettleDate);
        sb.append(", actualSettleDate=").append(actualSettleDate);
        sb.append(", awardStatus=").append(awardStatus);
        sb.append(", groupId=").append(groupId);
        sb.append(", ctime=").append(ctime);
        sb.append(", mtime=").append(mtime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}