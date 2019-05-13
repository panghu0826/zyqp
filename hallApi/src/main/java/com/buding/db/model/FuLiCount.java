package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class FuLiCount extends BaseModel<Long> implements Serializable {

    private Long id;
    private Integer playerId;
    private String playerName;
    private Date countDate;
    private Integer num;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Date getCountDate() {
        return countDate;
    }

    public void setCountDate(Date countDate) {
        this.countDate = countDate;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuLiCount fuLiCount = (FuLiCount) o;
        return Objects.equals(id, fuLiCount.id) &&
                Objects.equals(playerId, fuLiCount.playerId) &&
                Objects.equals(playerName, fuLiCount.playerName) &&
                Objects.equals(countDate, fuLiCount.countDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, playerId, playerName, countDate);
    }
}
