package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class FuLi extends BaseModel<Long> implements Serializable {
    private Long id;

    private String gameId;

    private Integer playerId;


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

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuLi fuLi = (FuLi) o;
        return Objects.equals(id, fuLi.id) &&
                Objects.equals(gameId, fuLi.gameId) &&
                Objects.equals(playerId, fuLi.playerId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, gameId, playerId);
    }

    @Override
    public String toString() {
        return "FuLi{" +
                "id=" + id +
                ", gameId='" + gameId + '\'' +
                ", playerId=" + playerId +
                '}';
    }
}