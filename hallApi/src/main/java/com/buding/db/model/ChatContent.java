package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ChatContent extends BaseModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long chatId;

    private Date chatTime;

    private Integer playerId;

    private String playerName;

    private String playerImg;

    private String content;

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

    public Date getChatTime() {
        return chatTime;
    }

    public void setChatTime(Date chatTime) {
        this.chatTime = chatTime;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerImg() {
        return playerImg;
    }

    public void setPlayerImg(String playerImg) {
        this.playerImg = playerImg;
    }

    @Override
    public String toString() {
        return "ChatContent{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", chatTime=" + chatTime +
                ", playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", playerImg='" + playerImg + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatContent content1 = (ChatContent) o;
        return Objects.equals(id, content1.id) &&
                Objects.equals(chatId, content1.chatId) &&
                Objects.equals(chatTime, content1.chatTime) &&
                Objects.equals(playerId, content1.playerId) &&
                Objects.equals(playerName, content1.playerName) &&
                Objects.equals(playerImg, content1.playerImg) &&
                Objects.equals(content, content1.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, chatId, chatTime, playerId, playerName, playerImg, content);
    }
}