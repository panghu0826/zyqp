package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class Award extends BaseModel<Long> implements Serializable {
    private Long id;

    private String items;

    private Date invalidTime;

    private String srcSystem;

    private String awardNote;

    private Integer awardType;

    private Integer receiverId;

    private Date ctime;

    private String awardReason;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items == null ? null : items.trim();
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public String getSrcSystem() {
        return srcSystem;
    }

    public void setSrcSystem(String srcSystem) {
        this.srcSystem = srcSystem == null ? null : srcSystem.trim();
    }

    public String getAwardNote() {
        return awardNote;
    }

    public void setAwardNote(String awardNote) {
        this.awardNote = awardNote == null ? null : awardNote.trim();
    }

    public Integer getAwardType() {
        return awardType;
    }

    public void setAwardType(Integer awardType) {
        this.awardType = awardType;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getAwardReason() {
        return awardReason;
    }

    public void setAwardReason(String awardReason) {
        this.awardReason = awardReason == null ? null : awardReason.trim();
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
        Award other = (Award) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getItems() == null ? other.getItems() == null : this.getItems().equals(other.getItems()))
            && (this.getInvalidTime() == null ? other.getInvalidTime() == null : this.getInvalidTime().equals(other.getInvalidTime()))
            && (this.getSrcSystem() == null ? other.getSrcSystem() == null : this.getSrcSystem().equals(other.getSrcSystem()))
            && (this.getAwardNote() == null ? other.getAwardNote() == null : this.getAwardNote().equals(other.getAwardNote()))
            && (this.getAwardType() == null ? other.getAwardType() == null : this.getAwardType().equals(other.getAwardType()))
            && (this.getReceiverId() == null ? other.getReceiverId() == null : this.getReceiverId().equals(other.getReceiverId()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getAwardReason() == null ? other.getAwardReason() == null : this.getAwardReason().equals(other.getAwardReason()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getItems() == null) ? 0 : getItems().hashCode());
        result = prime * result + ((getInvalidTime() == null) ? 0 : getInvalidTime().hashCode());
        result = prime * result + ((getSrcSystem() == null) ? 0 : getSrcSystem().hashCode());
        result = prime * result + ((getAwardNote() == null) ? 0 : getAwardNote().hashCode());
        result = prime * result + ((getAwardType() == null) ? 0 : getAwardType().hashCode());
        result = prime * result + ((getReceiverId() == null) ? 0 : getReceiverId().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getAwardReason() == null) ? 0 : getAwardReason().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", items=").append(items);
        sb.append(", invalidTime=").append(invalidTime);
        sb.append(", srcSystem=").append(srcSystem);
        sb.append(", awardNote=").append(awardNote);
        sb.append(", awardType=").append(awardType);
        sb.append(", receiverId=").append(receiverId);
        sb.append(", ctime=").append(ctime);
        sb.append(", awardReason=").append(awardReason);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}