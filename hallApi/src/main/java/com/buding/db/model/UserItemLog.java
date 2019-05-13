package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserItemLog extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private String itemId;

    private Long itemInsId;

    private String itemName;

    private Integer itemCount;

    private Integer propsCount;

    private Date effectEndTime;

    private Boolean isAdd;

    private String changeReason;

    private String batch;

    private Date logTime;

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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId == null ? null : itemId.trim();
    }

    public Long getItemInsId() {
        return itemInsId;
    }

    public void setItemInsId(Long itemInsId) {
        this.itemInsId = itemInsId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName == null ? null : itemName.trim();
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Integer getPropsCount() {
        return propsCount;
    }

    public void setPropsCount(Integer propsCount) {
        this.propsCount = propsCount;
    }

    public Date getEffectEndTime() {
        return effectEndTime;
    }

    public void setEffectEndTime(Date effectEndTime) {
        this.effectEndTime = effectEndTime;
    }

    public Boolean getIsAdd() {
        return isAdd;
    }

    public void setIsAdd(Boolean isAdd) {
        this.isAdd = isAdd;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason == null ? null : changeReason.trim();
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch == null ? null : batch.trim();
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
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
        UserItemLog other = (UserItemLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getItemId() == null ? other.getItemId() == null : this.getItemId().equals(other.getItemId()))
            && (this.getItemInsId() == null ? other.getItemInsId() == null : this.getItemInsId().equals(other.getItemInsId()))
            && (this.getItemName() == null ? other.getItemName() == null : this.getItemName().equals(other.getItemName()))
            && (this.getItemCount() == null ? other.getItemCount() == null : this.getItemCount().equals(other.getItemCount()))
            && (this.getPropsCount() == null ? other.getPropsCount() == null : this.getPropsCount().equals(other.getPropsCount()))
            && (this.getEffectEndTime() == null ? other.getEffectEndTime() == null : this.getEffectEndTime().equals(other.getEffectEndTime()))
            && (this.getIsAdd() == null ? other.getIsAdd() == null : this.getIsAdd().equals(other.getIsAdd()))
            && (this.getChangeReason() == null ? other.getChangeReason() == null : this.getChangeReason().equals(other.getChangeReason()))
            && (this.getBatch() == null ? other.getBatch() == null : this.getBatch().equals(other.getBatch()))
            && (this.getLogTime() == null ? other.getLogTime() == null : this.getLogTime().equals(other.getLogTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getItemId() == null) ? 0 : getItemId().hashCode());
        result = prime * result + ((getItemInsId() == null) ? 0 : getItemInsId().hashCode());
        result = prime * result + ((getItemName() == null) ? 0 : getItemName().hashCode());
        result = prime * result + ((getItemCount() == null) ? 0 : getItemCount().hashCode());
        result = prime * result + ((getPropsCount() == null) ? 0 : getPropsCount().hashCode());
        result = prime * result + ((getEffectEndTime() == null) ? 0 : getEffectEndTime().hashCode());
        result = prime * result + ((getIsAdd() == null) ? 0 : getIsAdd().hashCode());
        result = prime * result + ((getChangeReason() == null) ? 0 : getChangeReason().hashCode());
        result = prime * result + ((getBatch() == null) ? 0 : getBatch().hashCode());
        result = prime * result + ((getLogTime() == null) ? 0 : getLogTime().hashCode());
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
        sb.append(", itemId=").append(itemId);
        sb.append(", itemInsId=").append(itemInsId);
        sb.append(", itemName=").append(itemName);
        sb.append(", quanCount=").append(itemCount);
        sb.append(", propsCount=").append(propsCount);
        sb.append(", effectEndTime=").append(effectEndTime);
        sb.append(", isAdd=").append(isAdd);
        sb.append(", changeReason=").append(changeReason);
        sb.append(", batch=").append(batch);
        sb.append(", logTime=").append(logTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}