package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class FriendApply extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer userId;

    private Integer applyUserId;
    
    private String applyUserName;
    
    private String applyUserImg;

    private Date ctime;


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

    public Integer getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Integer applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public String getApplyUserImg() {
        return applyUserImg;
    }

    public void setApplyUserImg(String applyUserImg) {
        this.applyUserImg = applyUserImg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendApply that = (FriendApply) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(applyUserId, that.applyUserId) &&
                Objects.equals(applyUserName, that.applyUserName) &&
                Objects.equals(applyUserImg, that.applyUserImg) &&
                Objects.equals(ctime, that.ctime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, userId, applyUserId, applyUserName, applyUserImg, ctime);
    }

    @Override
    public String toString() {
        return "FriendApply{" +
                "id=" + id +
                ", userId=" + userId +
                ", applyUserId=" + applyUserId +
                ", applyUserName='" + applyUserName + '\'' +
                ", applyUserImg='" + applyUserImg + '\'' +
                ", ctime=" + ctime +
                '}';
    }
}