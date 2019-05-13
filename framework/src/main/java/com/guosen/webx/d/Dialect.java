package com.guosen.webx.d;

/**
 * @author kerry
 */
public class Dialect {
    public static final String RS_COLUMN = "NUMS";

    public void setLimitSupport(boolean limitSupport) {
        this.limitSupport = limitSupport;
    }

    // mysql style limit 0, 100
    private boolean limitSupport = true;

    // postgresql style limit 100 offset 0
    private boolean offsetSupport = false;

    public void setOffsetSupport(boolean offsetSupport) {
        this.offsetSupport = offsetSupport;
    }

    public String getPagiSql(String sql, int offset, int limit) {
        sql = sql.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        if (!this.limitSupport) {
            sb.append("select * from (select xxx.*, rownum as rrr from (");
            sb.append(sql);
            sb.append(") xxx) ttt where ttt.rrr > ");
            sb.append(offset);
            sb.append(" and ttt.rrr <= ");
            sb.append(offset + limit);
        } else {
            sb.append("select ttt.* from (");
            sb.append(sql);
            sb.append(") ttt limit ");

            if (this.offsetSupport) {
                sb.append(limit);
                sb.append(" offset ");
                sb.append(offset);
            } else {
                sb.append(offset);
                sb.append(",");
                sb.append(limit);
            }
        }
        return sb.toString();
    }

    public String getCountSql(String sql) {
        sql = sql.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();

        sb.append("select count(*) as ");
        sb.append(RS_COLUMN);
        sb.append(" from (");
        sb.append(sql);
        sb.append(") ttt");

        return sb.toString();
    }
}
