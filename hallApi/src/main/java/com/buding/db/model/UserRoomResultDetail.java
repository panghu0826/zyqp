package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class UserRoomResultDetail extends BaseModel<Long> implements Serializable {
    private Long id;

    private String gameId;

    private Long roomId;

    private String roomName;

    private Date startTime;

    private Date endTime;

    private Integer bankerPos;

    private Integer winerPos;

    private Integer gameCount;

    private Long videoId;

    private String detail;

    private String videoDetail;

    private Long clubId;

    private Integer clubRoomType;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : roomName.trim();
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

    public Integer getBankerPos() {
        return bankerPos;
    }

    public void setBankerPos(Integer bankerPos) {
        this.bankerPos = bankerPos;
    }

    public Integer getWinerPos() {
        return winerPos;
    }

    public void setWinerPos(Integer winerPos) {
        this.winerPos = winerPos;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public Integer getGameCount() {
        return gameCount;
    }

    public void setGameCount(Integer gameCount) {
        this.gameCount = gameCount;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getVideoDetail() {
        return videoDetail;
    }

    public void setVideoDetail(String videoDetail) {
        this.videoDetail = videoDetail;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Integer getClubRoomType() {
        return clubRoomType;
    }

    public void setClubRoomType(Integer clubRoomType) {
        this.clubRoomType = clubRoomType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoomResultDetail that = (UserRoomResultDetail) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(gameId, that.gameId) &&
                Objects.equals(roomId, that.roomId) &&
                Objects.equals(roomName, that.roomName) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(bankerPos, that.bankerPos) &&
                Objects.equals(winerPos, that.winerPos) &&
                Objects.equals(gameCount, that.gameCount) &&
                Objects.equals(videoId, that.videoId) &&
                Objects.equals(detail, that.detail) &&
                Objects.equals(videoDetail, that.videoDetail) &&
                Objects.equals(clubId, that.clubId) &&
                Objects.equals(clubRoomType, that.clubRoomType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, gameId, roomId, roomName, startTime, endTime, bankerPos, winerPos, gameCount, videoId, detail, videoDetail, clubId, clubRoomType);
    }

    @Override
    public String toString() {
        return "UserRoomResultDetail{" +
                "id=" + id +
                ", gameId='" + gameId + '\'' +
                ", roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", bankerPos=" + bankerPos +
                ", winerPos=" + winerPos +
                ", gameCount=" + gameCount +
                ", videoId=" + videoId +
                ", detail='" + detail + '\'' +
                ", videoDetail='" + videoDetail + '\'' +
                ", clubId=" + clubId +
                ", clubRoomType=" + clubRoomType +
                '}';
    }

}