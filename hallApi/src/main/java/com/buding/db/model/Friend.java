package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Friend extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long chatId;

    private Integer userId;

    private Integer friendUserId;

    private String friendUserName;

    private String friendUserImg;

    private Date ctime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Integer friendUserId) {
        this.friendUserId = friendUserId;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getFriendUserName() {
        return friendUserName;
    }

    public void setFriendUserName(String friendUserName) {
        this.friendUserName = friendUserName;
    }

    public String getFriendUserImg() {
        return friendUserImg;
    }

    public void setFriendUserImg(String friendUserImg) {
        this.friendUserImg = friendUserImg;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", userId=" + userId +
                ", friendUserId=" + friendUserId +
                ", friendUserName='" + friendUserName + '\'' +
                ", friendUserImg='" + friendUserImg + '\'' +
                ", ctime=" + ctime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return Objects.equals(id, friend.id) &&
                Objects.equals(chatId, friend.chatId) &&
                Objects.equals(userId, friend.userId) &&
                Objects.equals(friendUserId, friend.friendUserId) &&
                Objects.equals(friendUserName, friend.friendUserName) &&
                Objects.equals(friendUserImg, friend.friendUserImg) &&
                Objects.equals(ctime, friend.ctime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, chatId, userId, friendUserId, friendUserName, friendUserImg, ctime);
    }
}