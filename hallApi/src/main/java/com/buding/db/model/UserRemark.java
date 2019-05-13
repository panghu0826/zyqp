package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Objects;

public class UserRemark extends BaseModel<Long> implements Serializable {
    private Long id;
    private Integer userId;
    private Integer remarkUserId;
    private String remarkUserName;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRemarkUserId() {
        return remarkUserId;
    }

    public void setRemarkUserId(Integer remarkUserId) {
        this.remarkUserId = remarkUserId;
    }

    public String getRemarkUserName() {
        return remarkUserName;
    }

    public void setRemarkUserName(String remarkUserName) {
        this.remarkUserName = remarkUserName;
    }

    @Override
    public String toString() {
        return "UserRemark{" +
                "id=" + id +
                ", userId=" + userId +
                ", remarkUserId=" + remarkUserId +
                ", remarkUserName='" + remarkUserName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRemark that = (UserRemark) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(remarkUserId, that.remarkUserId) &&
                Objects.equals(remarkUserName, that.remarkUserName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, userId, remarkUserId, remarkUserName);
    }
}