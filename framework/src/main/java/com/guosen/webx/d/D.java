package com.guosen.webx.d;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guosen.webx.beans.Context;

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;

/**
 * @author kerry
 */
public class D {
    // for short build
    public static D gen() {
        D d = new D();
        d.setDialect(new Dialect());
        DataSource ds = Context.getInst().getDs();
        // no singleton as transaction
        d.setDb(new Sql(ds));
        return d;
    }

    private Logger log = LoggerFactory.getLogger(D.class);

    private Dialect dialect;
    private Sql db;

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public void setDb(Sql db) {
        this.db = db;
    }

    public Sql getDb() {
        return this.db;
    }

    private String uuid() {
        Random rand = new Random();
        return "" + System.currentTimeMillis() + "_" + rand.nextInt(10000) + "_" + rand.nextInt(10000);
    }

    public List<Map<String, Object>> query(String sql, List<Object> args) throws SQLException {
        String uuid = uuid() + " - ";
        long begin = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug(uuid + sql);
            log.debug(args == null ? "" : args.toString());
        }

        List<GroovyRowResult> ll = args == null ? db.rows(sql) : db.rows(sql, args);
        if (log.isDebugEnabled()) {
            log.debug(uuid + " query size : " + ll.size());
            log.debug(uuid + " query cost : " + (System.currentTimeMillis() - begin));
        }

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for (GroovyRowResult one : ll)
            resultList.add(NamingStyleUtils.transform(one));

        return resultList;
    }

    public List<Map<String, Object>> query(String sql) throws SQLException {
        return query(sql, null);
    }

    public Map<String, Object> queryMap(String sql, List<Object> args) throws SQLException {
        List<Map<String, Object>> r = query(sql, args);
        if (r != null && r.size() > 0) {
            return r.get(0);
        }
        return null;
    }

    public Map<String, Object> queryMap(String sql) throws SQLException {
        return queryMap(sql, null);
    }

    public Pager pagi(String sql, List<Object> args, int pageNum, int pageSize) throws SQLException {
        Pager pi = new Pager(pageNum, pageSize);

        List<Map<String, Object>> resultList = query(dialect.getPagiSql(sql, pi.getStart(), pageSize), args);
        pi.setLl(resultList);
        if (resultList != null && resultList.size() > 0) {
            Map<String, Object> countItem = queryMap(dialect.getCountSql(sql), args);

            if (log.isDebugEnabled()) {
                log.debug(countItem == null ? "" : countItem.toString());
            }

            int cc = 0;
            Object totalCount = countItem.get(NamingStyleUtils.toCamel(Dialect.RS_COLUMN, true));
            if (totalCount instanceof BigDecimal)
                cc = ((BigDecimal) totalCount).intValue();
            else
                cc = Integer.parseInt("" + totalCount);

            pi.setTotalCount(cc);
        }
        return pi;
    }

    private String genSqlKeys(Set<String> set, String replaced) {
        StringBuilder sb = new StringBuilder();

        int cc = 0;
        for (String it : set) {
            String key = NamingStyleUtils.toUnderline(it);
            if (replaced != null) {
                if (replaced.indexOf("$1") != -1)
                    key = replaced.replace("$1", key);
                else
                    key = replaced;
            }
            sb.append(key);
            if (cc != set.size() - 1)
                sb.append(",");
            cc++;
        }

        return sb.toString();
    }

    private String concatStr(String... strs) {
        StringBuilder sb = new StringBuilder();

        for (String str : strs) {
            sb.append(str);
        }

        return sb.toString();
    }

    public Object add(Map<String, Object> ext, String table) throws SQLException {
        if (ext == null)
            return null;

        String sql = concatStr("insert into ", table, " (", genSqlKeys(ext.keySet(), null), ") values (",
                genSqlKeys(ext.keySet(), "?"), ")");

        List<Object> args = new ArrayList<Object>(ext.values());

        if (log.isDebugEnabled()) {
            log.debug(sql);
            log.debug(args == null ? "" : args.toString());
        }

        List<List<Object>> ll = db.executeInsert(sql, args);
        if (ll != null && ll.size() > 0)
            return ll.get(0).get(0);

        return null;
    }

    public int update(Map<String, Object> ext, String table, String pkCol) throws SQLException {
        if (ext == null)
            return 0;

        if (pkCol == null)
            pkCol = "id";

        Object pkVal = ext.remove(pkCol);

        String sql = concatStr("update ", table, " set ", genSqlKeys(ext.keySet(), "$1 = ?"), " where ",
                NamingStyleUtils.toUnderline(pkCol), " = ?");

        List<Object> args = new ArrayList<Object>(ext.values());
        args.add(pkVal);

        if (log.isDebugEnabled()) {
            log.debug(sql);
            log.debug(args == null ? "" : args.toString());
        }

        return db.executeUpdate(sql, args);
    }

    public int update(Map<String, Object> ext, String table) throws SQLException {
        return update(ext, table, null);
    }

    public int del(int id, String table) throws SQLException {
        String sql = "delete from " + table + " where id = ?";
        List args = new ArrayList();
        args.add(id);

        if (log.isDebugEnabled()) {
            log.debug(sql);
            log.debug(args == null ? "" : args.toString());
        }

        return db.executeUpdate(sql, args);
    }

    public boolean exe(String sql, List<Object> args) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug(sql);
            log.debug(args == null ? "" : args.toString());
        }

        return db.execute(sql, args);
    }

    public boolean exe(String sql) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }

        return db.execute(sql);
    }
}
