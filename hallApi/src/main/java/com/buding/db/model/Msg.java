package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class Msg extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer targetId;

    private Integer targetType;

    private Integer senderId;

    private String senderName;

    private Boolean popup;

    private Integer priority;

    private String strPos;

    private String strClientType;

    private String title;

    private String msg;

    private Date startDateTime;

    private String msgType;

    private Long rewardId;

    private Date stopDateTime;

    private String img;

    private String strParams;

    private String playSetting;

    private Integer msgMainType;

    private Integer attachNum;

    private Integer status;

    private String itemId;

    private Integer itemCount;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName == null ? null : senderName.trim();
    }

    public Boolean getPopup() {
        return popup;
    }

    public void setPopup(Boolean popup) {
        this.popup = popup;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStrPos() {
        return strPos;
    }

    public void setStrPos(String strPos) {
        this.strPos = strPos == null ? null : strPos.trim();
    }

    public String getStrClientType() {
        return strClientType;
    }

    public void setStrClientType(String strClientType) {
        this.strClientType = strClientType == null ? null : strClientType.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType == null ? null : msgType.trim();
    }

    public Long getRewardId() {
        return rewardId;
    }

    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }

    public Date getStopDateTime() {
        return stopDateTime;
    }

    public void setStopDateTime(Date stopDateTime) {
        this.stopDateTime = stopDateTime;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img == null ? null : img.trim();
    }

    public String getStrParams() {
        return strParams;
    }

    public void setStrParams(String strParams) {
        this.strParams = strParams == null ? null : strParams.trim();
    }

    public String getPlaySetting() {
        return playSetting;
    }

    public void setPlaySetting(String playSetting) {
        this.playSetting = playSetting == null ? null : playSetting.trim();
    }

    public Integer getMsgMainType() {
        return msgMainType;
    }

    public void setMsgMainType(Integer msgMainType) {
        this.msgMainType = msgMainType;
    }

    public Integer getAttachNum() {
        return attachNum;
    }

    public void setAttachNum(Integer attachNum) {
        this.attachNum = attachNum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId == null ? null : itemId.trim();
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
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
        Msg other = (Msg) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTargetId() == null ? other.getTargetId() == null : this.getTargetId().equals(other.getTargetId()))
            && (this.getTargetType() == null ? other.getTargetType() == null : this.getTargetType().equals(other.getTargetType()))
            && (this.getSenderId() == null ? other.getSenderId() == null : this.getSenderId().equals(other.getSenderId()))
            && (this.getSenderName() == null ? other.getSenderName() == null : this.getSenderName().equals(other.getSenderName()))
            && (this.getPopup() == null ? other.getPopup() == null : this.getPopup().equals(other.getPopup()))
            && (this.getPriority() == null ? other.getPriority() == null : this.getPriority().equals(other.getPriority()))
            && (this.getStrPos() == null ? other.getStrPos() == null : this.getStrPos().equals(other.getStrPos()))
            && (this.getStrClientType() == null ? other.getStrClientType() == null : this.getStrClientType().equals(other.getStrClientType()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getMsg() == null ? other.getMsg() == null : this.getMsg().equals(other.getMsg()))
            && (this.getStartDateTime() == null ? other.getStartDateTime() == null : this.getStartDateTime().equals(other.getStartDateTime()))
            && (this.getMsgType() == null ? other.getMsgType() == null : this.getMsgType().equals(other.getMsgType()))
            && (this.getRewardId() == null ? other.getRewardId() == null : this.getRewardId().equals(other.getRewardId()))
            && (this.getStopDateTime() == null ? other.getStopDateTime() == null : this.getStopDateTime().equals(other.getStopDateTime()))
            && (this.getImg() == null ? other.getImg() == null : this.getImg().equals(other.getImg()))
            && (this.getStrParams() == null ? other.getStrParams() == null : this.getStrParams().equals(other.getStrParams()))
            && (this.getPlaySetting() == null ? other.getPlaySetting() == null : this.getPlaySetting().equals(other.getPlaySetting()))
            && (this.getMsgMainType() == null ? other.getMsgMainType() == null : this.getMsgMainType().equals(other.getMsgMainType()))
            && (this.getAttachNum() == null ? other.getAttachNum() == null : this.getAttachNum().equals(other.getAttachNum()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getItemId() == null ? other.getItemId() == null : this.getItemId().equals(other.getItemId()))
            && (this.getItemCount() == null ? other.getItemCount() == null : this.getItemCount().equals(other.getItemCount()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTargetId() == null) ? 0 : getTargetId().hashCode());
        result = prime * result + ((getTargetType() == null) ? 0 : getTargetType().hashCode());
        result = prime * result + ((getSenderId() == null) ? 0 : getSenderId().hashCode());
        result = prime * result + ((getSenderName() == null) ? 0 : getSenderName().hashCode());
        result = prime * result + ((getPopup() == null) ? 0 : getPopup().hashCode());
        result = prime * result + ((getPriority() == null) ? 0 : getPriority().hashCode());
        result = prime * result + ((getStrPos() == null) ? 0 : getStrPos().hashCode());
        result = prime * result + ((getStrClientType() == null) ? 0 : getStrClientType().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getMsg() == null) ? 0 : getMsg().hashCode());
        result = prime * result + ((getStartDateTime() == null) ? 0 : getStartDateTime().hashCode());
        result = prime * result + ((getMsgType() == null) ? 0 : getMsgType().hashCode());
        result = prime * result + ((getRewardId() == null) ? 0 : getRewardId().hashCode());
        result = prime * result + ((getStopDateTime() == null) ? 0 : getStopDateTime().hashCode());
        result = prime * result + ((getImg() == null) ? 0 : getImg().hashCode());
        result = prime * result + ((getStrParams() == null) ? 0 : getStrParams().hashCode());
        result = prime * result + ((getPlaySetting() == null) ? 0 : getPlaySetting().hashCode());
        result = prime * result + ((getMsgMainType() == null) ? 0 : getMsgMainType().hashCode());
        result = prime * result + ((getAttachNum() == null) ? 0 : getAttachNum().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getItemId() == null) ? 0 : getItemId().hashCode());
        result = prime * result + ((getItemCount() == null) ? 0 : getItemCount().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", targetId=").append(targetId);
        sb.append(", targetType=").append(targetType);
        sb.append(", senderId=").append(senderId);
        sb.append(", senderName=").append(senderName);
        sb.append(", popup=").append(popup);
        sb.append(", priority=").append(priority);
        sb.append(", strPos=").append(strPos);
        sb.append(", strClientType=").append(strClientType);
        sb.append(", title=").append(title);
        sb.append(", msg=").append(msg);
        sb.append(", startDateTime=").append(startDateTime);
        sb.append(", msgType=").append(msgType);
        sb.append(", rewardId=").append(rewardId);
        sb.append(", stopDateTime=").append(stopDateTime);
        sb.append(", img=").append(img);
        sb.append(", strParams=").append(strParams);
        sb.append(", playSetting=").append(playSetting);
        sb.append(", msgMainType=").append(msgMainType);
        sb.append(", attachNum=").append(attachNum);
        sb.append(", status=").append(status);
        sb.append(", itemId=").append(itemId);
        sb.append(", quanCount=").append(itemCount);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}