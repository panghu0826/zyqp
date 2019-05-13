package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class AdminLog extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private String operTarget;

    private String operType;

    private String changeFrom;

    private String changeTo;

    private String changeVal;

    private Date operTime;

    private String operDesc;

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

    public String getOperTarget() {
        return operTarget;
    }

    public void setOperTarget(String operTarget) {
        this.operTarget = operTarget == null ? null : operTarget.trim();
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType == null ? null : operType.trim();
    }

    public String getChangeFrom() {
        return changeFrom;
    }

    public void setChangeFrom(String changeFrom) {
        this.changeFrom = changeFrom == null ? null : changeFrom.trim();
    }

    public String getChangeTo() {
        return changeTo;
    }

    public void setChangeTo(String changeTo) {
        this.changeTo = changeTo == null ? null : changeTo.trim();
    }

    public String getChangeVal() {
        return changeVal;
    }

    public void setChangeVal(String changeVal) {
        this.changeVal = changeVal == null ? null : changeVal.trim();
    }

    public Date getOperTime() {
        return operTime;
    }

    public void setOperTime(Date operTime) {
        this.operTime = operTime;
    }

    public String getOperDesc() {
        return operDesc;
    }

    public void setOperDesc(String operDesc) {
        this.operDesc = operDesc == null ? null : operDesc.trim();
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
        AdminLog other = (AdminLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getOperTarget() == null ? other.getOperTarget() == null : this.getOperTarget().equals(other.getOperTarget()))
            && (this.getOperType() == null ? other.getOperType() == null : this.getOperType().equals(other.getOperType()))
            && (this.getChangeFrom() == null ? other.getChangeFrom() == null : this.getChangeFrom().equals(other.getChangeFrom()))
            && (this.getChangeTo() == null ? other.getChangeTo() == null : this.getChangeTo().equals(other.getChangeTo()))
            && (this.getChangeVal() == null ? other.getChangeVal() == null : this.getChangeVal().equals(other.getChangeVal()))
            && (this.getOperTime() == null ? other.getOperTime() == null : this.getOperTime().equals(other.getOperTime()))
            && (this.getOperDesc() == null ? other.getOperDesc() == null : this.getOperDesc().equals(other.getOperDesc()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getOperTarget() == null) ? 0 : getOperTarget().hashCode());
        result = prime * result + ((getOperType() == null) ? 0 : getOperType().hashCode());
        result = prime * result + ((getChangeFrom() == null) ? 0 : getChangeFrom().hashCode());
        result = prime * result + ((getChangeTo() == null) ? 0 : getChangeTo().hashCode());
        result = prime * result + ((getChangeVal() == null) ? 0 : getChangeVal().hashCode());
        result = prime * result + ((getOperTime() == null) ? 0 : getOperTime().hashCode());
        result = prime * result + ((getOperDesc() == null) ? 0 : getOperDesc().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", operTarget=").append(operTarget);
        sb.append(", operType=").append(operType);
        sb.append(", changeFrom=").append(changeFrom);
        sb.append(", changeTo=").append(changeTo);
        sb.append(", changeVal=").append(changeVal);
        sb.append(", operTime=").append(operTime);
        sb.append(", operDesc=").append(operDesc);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}