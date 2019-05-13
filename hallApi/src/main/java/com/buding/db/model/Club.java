package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Club extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String clubName;

    private String clubWanfa;

    private String clubNotice;

    private Integer createRoomMode;

    private Date ctime;

    private Integer enterScore;

    private Integer canFufen;

    private Integer choushuiScore;

    private Integer choushuiNum;

    private Integer zengsongNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getClubWanfa() {
        return clubWanfa;
    }

    public void setClubWanfa(String clubWanfa) {
        this.clubWanfa = clubWanfa;
    }

    public String getClubNotice() {
        return clubNotice;
    }

    public void setClubNotice(String clubNotice) {
        this.clubNotice = clubNotice;
    }

    public Integer getCreateRoomMode() {
        return createRoomMode;
    }

    public void setCreateRoomMode(Integer createRoomMode) {
        this.createRoomMode = createRoomMode;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Integer getEnterScore() {
        return enterScore;
    }

    public void setEnterScore(Integer enterScore) {
        this.enterScore = enterScore;
    }

    public Integer getCanFufen() {
        return canFufen;
    }

    public void setCanFufen(Integer canFufen) {
        this.canFufen = canFufen;
    }

    public Integer getChoushuiScore() {
        return choushuiScore;
    }

    public void setChoushuiScore(Integer choushuiScore) {
        this.choushuiScore = choushuiScore;
    }

    public Integer getChoushuiNum() {
        return choushuiNum;
    }

    public void setChoushuiNum(Integer choushuiNum) {
        this.choushuiNum = choushuiNum;
    }

    public Integer getZengsongNum() {
        return zengsongNum;
    }

    public void setZengsongNum(Integer zengsongNum) {
        this.zengsongNum = zengsongNum;
    }

    @Override
    public String toString() {
        return "Club{" +
                "id=" + id +
                ", clubName='" + clubName + '\'' +
                ", clubWanfa='" + clubWanfa + '\'' +
                ", clubNotice='" + clubNotice + '\'' +
                ", createRoomMode=" + createRoomMode +
                ", ctime=" + ctime +
                ", enterScore=" + enterScore +
                ", canFufen=" + canFufen +
                ", choushuiScore=" + choushuiScore +
                ", choushuiNum=" + choushuiNum +
                ", zengsongNum=" + zengsongNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return Objects.equals(id, club.id) &&
                Objects.equals(clubName, club.clubName) &&
                Objects.equals(clubWanfa, club.clubWanfa) &&
                Objects.equals(clubNotice, club.clubNotice) &&
                Objects.equals(createRoomMode, club.createRoomMode) &&
                Objects.equals(ctime, club.ctime) &&
                Objects.equals(enterScore, club.enterScore) &&
                Objects.equals(canFufen, club.canFufen) &&
                Objects.equals(choushuiScore, club.choushuiScore) &&
                Objects.equals(choushuiNum, club.choushuiNum) &&
                Objects.equals(zengsongNum, club.zengsongNum);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, clubName, clubWanfa, clubNotice, createRoomMode, ctime, enterScore, canFufen, choushuiScore, choushuiNum, zengsongNum);
    }
}