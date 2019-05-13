package com.buding.db.model;

import java.util.Objects;

public class FuliCountNum {
    private String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "FuliCountNum{" +
                "detail='" + detail + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuliCountNum that = (FuliCountNum) o;
        return Objects.equals(detail, that.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detail);
    }
}
