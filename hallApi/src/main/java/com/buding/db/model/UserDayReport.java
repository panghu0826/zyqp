package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;

public class UserDayReport extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private Integer onlineMinutes;

    private Integer day;

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

    public Integer getOnlineMinutes() {
        return onlineMinutes;
    }

    public void setOnlineMinutes(Integer onlineMinutes) {
        this.onlineMinutes = onlineMinutes;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
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
        UserDayReport other = (UserDayReport) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getOnlineMinutes() == null ? other.getOnlineMinutes() == null : this.getOnlineMinutes().equals(other.getOnlineMinutes()))
            && (this.getDay() == null ? other.getDay() == null : this.getDay().equals(other.getDay()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getOnlineMinutes() == null) ? 0 : getOnlineMinutes().hashCode());
        result = prime * result + ((getDay() == null) ? 0 : getDay().hashCode());
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
        sb.append(", onlineMinutes=").append(onlineMinutes);
        sb.append(", day=").append(day);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}