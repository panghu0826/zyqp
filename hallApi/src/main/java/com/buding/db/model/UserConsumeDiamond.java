package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;

public class UserConsumeDiamond extends BaseModel<Long> implements Serializable {
    private Long id;
    private String matchId;
    private int diamondNum;
    private Date ctime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getDiamondNum() {
        return diamondNum;
    }

    public void setDiamondNum(int diamondNum) {
        this.diamondNum = diamondNum;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }
}