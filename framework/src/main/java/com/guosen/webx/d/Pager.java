package com.guosen.webx.d;

import java.util.List;
import java.util.Map;

/**
 * @author kerry
 */
public class Pager {
    private int pageNum = 1;
    private int pageSize = 10;
    private int totalCount = 0;
    private List<Map<String, Object>> ll;

    public Pager(int c, int n) {
        this.pageNum = c;
        this.pageSize = n;
    }

    public int getTotalPage() {
        return this.totalCount % this.pageSize == 0 ? this.totalCount / this.pageSize
                : this.totalCount / this.pageSize + 1;
    }

    public int getStart() {
        return (this.pageNum - 1) * this.pageSize;
    }

    public int getEnd() {
        if ((this.totalCount < this.pageSize) || (!hasNext())) {
            return this.totalCount;
        }
        return this.pageNum * this.pageSize;
    }

    public boolean hasNext() {
        return this.pageNum < getTotalPage();
    }

    public boolean hasPre() {
        return (this.pageNum > 1) && (getTotalPage() > 1);
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Map<String, Object>> getLl() {
        return this.ll;
    }

    public void setLl(List<Map<String, Object>> ll) {
        this.ll = ll;
    }
}
