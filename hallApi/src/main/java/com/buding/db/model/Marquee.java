package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class Marquee extends BaseModel<Long> implements Serializable {
    private Long id;

    private String msgContent;

    private Integer loopPlayCount;

    private Date startTime;

    private Date endTime;

    private Integer marqueeType;

    private Integer loopPushCount;

    private Integer loopPushInterval;

    private Integer userGroup;

    private Boolean pushOnLogin;

    private Integer status;

    private Integer level;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent == null ? null : msgContent.trim();
    }

    public Integer getLoopPlayCount() {
        return loopPlayCount;
    }

    public void setLoopPlayCount(Integer loopPlayCount) {
        this.loopPlayCount = loopPlayCount;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getMarqueeType() {
        return marqueeType;
    }

    public void setMarqueeType(Integer marqueeType) {
        this.marqueeType = marqueeType;
    }

    public Integer getLoopPushCount() {
        return loopPushCount;
    }

    public void setLoopPushCount(Integer loopPushCount) {
        this.loopPushCount = loopPushCount;
    }

    public Integer getLoopPushInterval() {
        return loopPushInterval;
    }

    public void setLoopPushInterval(Integer loopPushInterval) {
        this.loopPushInterval = loopPushInterval;
    }

    public Integer getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(Integer userGroup) {
        this.userGroup = userGroup;
    }

    public Boolean getPushOnLogin() {
        return pushOnLogin;
    }

    public void setPushOnLogin(Boolean pushOnLogin) {
        this.pushOnLogin = pushOnLogin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Marquee marquee = (Marquee) o;

        if (id != null ? !id.equals(marquee.id) : marquee.id != null) return false;
        if (msgContent != null ? !msgContent.equals(marquee.msgContent) : marquee.msgContent != null) return false;
        if (loopPlayCount != null ? !loopPlayCount.equals(marquee.loopPlayCount) : marquee.loopPlayCount != null)
            return false;
        if (startTime != null ? !startTime.equals(marquee.startTime) : marquee.startTime != null) return false;
        if (endTime != null ? !endTime.equals(marquee.endTime) : marquee.endTime != null) return false;
        if (marqueeType != null ? !marqueeType.equals(marquee.marqueeType) : marquee.marqueeType != null) return false;
        if (loopPushCount != null ? !loopPushCount.equals(marquee.loopPushCount) : marquee.loopPushCount != null)
            return false;
        if (loopPushInterval != null ? !loopPushInterval.equals(marquee.loopPushInterval) : marquee.loopPushInterval != null)
            return false;
        if (userGroup != null ? !userGroup.equals(marquee.userGroup) : marquee.userGroup != null) return false;
        if (pushOnLogin != null ? !pushOnLogin.equals(marquee.pushOnLogin) : marquee.pushOnLogin != null) return false;
        if (status != null ? !status.equals(marquee.status) : marquee.status != null) return false;
        return level != null ? level.equals(marquee.level) : marquee.level == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (msgContent != null ? msgContent.hashCode() : 0);
        result = 31 * result + (loopPlayCount != null ? loopPlayCount.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (marqueeType != null ? marqueeType.hashCode() : 0);
        result = 31 * result + (loopPushCount != null ? loopPushCount.hashCode() : 0);
        result = 31 * result + (loopPushInterval != null ? loopPushInterval.hashCode() : 0);
        result = 31 * result + (userGroup != null ? userGroup.hashCode() : 0);
        result = 31 * result + (pushOnLogin != null ? pushOnLogin.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Marquee{" +
                "id=" + id +
                ", msgContent='" + msgContent + '\'' +
                ", loopPlayCount=" + loopPlayCount +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", marqueeType=" + marqueeType +
                ", loopPushCount=" + loopPushCount +
                ", loopPushInterval=" + loopPushInterval +
                ", userGroup=" + userGroup +
                ", pushOnLogin=" + pushOnLogin +
                ", status=" + status +
                ", level=" + level +
                '}';
    }
}