package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Chat extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer chatType;

    private Long clubId;

    private Integer user1Id;

    private String user1Name;

    private String user1Img;

    private Integer user2Id;

    private String user2Name;

    private String user2Img;

    private Date ctime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getChatType() {
        return chatType;
    }

    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public Integer getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }

    public Integer getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public String getUser1Img() {
        return user1Img;
    }

    public void setUser1Img(String user1Img) {
        this.user1Img = user1Img;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
    }

    public String getUser2Img() {
        return user2Img;
    }

    public void setUser2Img(String user2Img) {
        this.user2Img = user2Img;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", chatType=" + chatType +
                ", clubId=" + clubId +
                ", user1Id=" + user1Id +
                ", user1Name='" + user1Name + '\'' +
                ", user1Img='" + user1Img + '\'' +
                ", user2Id=" + user2Id +
                ", user2Name='" + user2Name + '\'' +
                ", user2Img='" + user2Img + '\'' +
                ", ctime=" + ctime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id) &&
                Objects.equals(chatType, chat.chatType) &&
                Objects.equals(clubId, chat.clubId) &&
                Objects.equals(user1Id, chat.user1Id) &&
                Objects.equals(user1Name, chat.user1Name) &&
                Objects.equals(user1Img, chat.user1Img) &&
                Objects.equals(user2Id, chat.user2Id) &&
                Objects.equals(user2Name, chat.user2Name) &&
                Objects.equals(user2Img, chat.user2Img) &&
                Objects.equals(ctime, chat.ctime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, chatType, clubId, user1Id, user1Name, user1Img, user2Id, user2Name, user2Img, ctime);
    }
}