package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ClubUser extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clubId;

    private Integer clubMemberId;

    private Integer clubMemberType;

    private Integer clubMemberScore;

    private String clubMemberName;

    private String clubMemberImg;

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

    public Integer getClubMemberId() {
        return clubMemberId;
    }

    public void setClubMemberId(Integer clubMemberId) {
        this.clubMemberId = clubMemberId;
    }

    public Integer getClubMemberType() {
        return clubMemberType;
    }

    public void setClubMemberType(Integer clubMemberType) {
        this.clubMemberType = clubMemberType;
    }

    public Integer getClubMemberScore() {
        return clubMemberScore;
    }

    public void setClubMemberScore(Integer clubMemberScore) {
        this.clubMemberScore = clubMemberScore;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getClubMemberName() {
        return clubMemberName;
    }

    public void setClubMemberName(String clubMemberName) {
        this.clubMemberName = clubMemberName;
    }

    public String getClubMemberImg() {
        return clubMemberImg;
    }

    public void setClubMemberImg(String clubMemberImg) {
        this.clubMemberImg = clubMemberImg;
    }

    @Override
    public String toString() {
        return "ClubUser{" +
                "id=" + id +
                ", clubId=" + clubId +
                ", clubMemberId=" + clubMemberId +
                ", clubMemberType=" + clubMemberType +
                ", clubMemberScore=" + clubMemberScore +
                ", clubMemberName='" + clubMemberName + '\'' +
                ", clubMemberImg='" + clubMemberImg + '\'' +
                ", ctime=" + ctime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClubUser clubUser = (ClubUser) o;
        return Objects.equals(id, clubUser.id) &&
                Objects.equals(clubId, clubUser.clubId) &&
                Objects.equals(clubMemberId, clubUser.clubMemberId) &&
                Objects.equals(clubMemberType, clubUser.clubMemberType) &&
                Objects.equals(clubMemberScore, clubUser.clubMemberScore) &&
                Objects.equals(clubMemberName, clubUser.clubMemberName) &&
                Objects.equals(clubMemberImg, clubUser.clubMemberImg) &&
                Objects.equals(ctime, clubUser.ctime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, clubId, clubMemberId, clubMemberType, clubMemberScore, clubMemberName, clubMemberImg, ctime);
    }
}