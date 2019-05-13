package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ClubApply extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clubId;

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

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
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
        ClubApply clubApply = (ClubApply) o;
        return Objects.equals(id, clubApply.id) &&
                Objects.equals(clubId, clubApply.clubId) &&
                Objects.equals(applyUserId, clubApply.applyUserId) &&
                Objects.equals(applyUserName, clubApply.applyUserName) &&
                Objects.equals(applyUserImg, clubApply.applyUserImg) &&
                Objects.equals(ctime, clubApply.ctime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, clubId, applyUserId, applyUserName, applyUserImg, ctime);
    }

    @Override
    public String toString() {
        return "ClubApply{" +
                "id=" + id +
                ", clubId=" + clubId +
                ", applyUserId=" + applyUserId +
                ", applyUserName='" + applyUserName + '\'' +
                ", applyUserImg='" + applyUserImg + '\'' +
                ", ctime=" + ctime +
                '}';
    }
}