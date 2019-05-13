package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserTask extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private String taskId;

    private Integer taskType;

    private Integer day;

    private Integer completedCount;

    private Integer requiredCount;

    private Boolean finish;

    private Boolean close;

    private Boolean award;

    private Integer promptCount;

    private Date mtime;

    private Date ctime;

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

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }

    public Boolean getFinish() {
        return finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public Boolean getClose() {
        return close;
    }

    public void setClose(Boolean close) {
        this.close = close;
    }

    public Boolean getAward() {
        return award;
    }

    public void setAward(Boolean award) {
        this.award = award;
    }

    public Integer getPromptCount() {
        return promptCount;
    }

    public void setPromptCount(Integer promptCount) {
        this.promptCount = promptCount;
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
        UserTask other = (UserTask) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
            && (this.getTaskType() == null ? other.getTaskType() == null : this.getTaskType().equals(other.getTaskType()))
            && (this.getDay() == null ? other.getDay() == null : this.getDay().equals(other.getDay()))
            && (this.getCompletedCount() == null ? other.getCompletedCount() == null : this.getCompletedCount().equals(other.getCompletedCount()))
            && (this.getRequiredCount() == null ? other.getRequiredCount() == null : this.getRequiredCount().equals(other.getRequiredCount()))
            && (this.getFinish() == null ? other.getFinish() == null : this.getFinish().equals(other.getFinish()))
            && (this.getClose() == null ? other.getClose() == null : this.getClose().equals(other.getClose()))
            && (this.getAward() == null ? other.getAward() == null : this.getAward().equals(other.getAward()))
            && (this.getPromptCount() == null ? other.getPromptCount() == null : this.getPromptCount().equals(other.getPromptCount()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getTaskType() == null) ? 0 : getTaskType().hashCode());
        result = prime * result + ((getDay() == null) ? 0 : getDay().hashCode());
        result = prime * result + ((getCompletedCount() == null) ? 0 : getCompletedCount().hashCode());
        result = prime * result + ((getRequiredCount() == null) ? 0 : getRequiredCount().hashCode());
        result = prime * result + ((getFinish() == null) ? 0 : getFinish().hashCode());
        result = prime * result + ((getClose() == null) ? 0 : getClose().hashCode());
        result = prime * result + ((getAward() == null) ? 0 : getAward().hashCode());
        result = prime * result + ((getPromptCount() == null) ? 0 : getPromptCount().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
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
        sb.append(", taskId=").append(taskId);
        sb.append(", taskType=").append(taskType);
        sb.append(", day=").append(day);
        sb.append(", completedCount=").append(completedCount);
        sb.append(", requiredCount=").append(requiredCount);
        sb.append(", finish=").append(finish);
        sb.append(", close=").append(close);
        sb.append(", award=").append(award);
        sb.append(", promptCount=").append(promptCount);
        sb.append(", mtime=").append(mtime);
        sb.append(", ctime=").append(ctime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}