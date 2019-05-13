package com.buding.db.model;

import com.buding.common.db.model.BaseModel;

import java.io.Serializable;
import java.util.Date;

public class TVersion extends BaseModel<Integer> implements Serializable {
    private Integer id;
    private String serverVersion;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public String toString() {
        return "TVersion{" +
                "id=" + id +
                ", serverVersion='" + serverVersion + '\'' +
                '}';
    }
}