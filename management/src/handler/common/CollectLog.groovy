package handler.common

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.guosen.webx.d.D
import com.guosen.webx.d.NamingStyleUtils
import com.guosen.webx.d.Pager
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.slf4j.Logger

// handler is GroovyShell binding variable
ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

// 搜集机器agent日志发送，并保存在数据库中
h.post('/a/m/collect-log/:ns') { req, resp, ctx ->
    // 命名空间，用于区分产生日志的保存数据库的位置
    String ns = req.params.ns
    D d = ctx.d

    def genSqlKeys = { List set, String replaced ->
        StringBuilder sb = new StringBuilder();

        int cc = 0;
        for (String it : set) {
            String key = NamingStyleUtils.toUnderline(it);
            if (replaced != null) {
                if (replaced.indexOf('$1') != -1)
                    key = replaced.replace('$1', key);
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

    req.bodyHandler { String body ->
        try {
            List keys = 'reqId,appVer,appInfo,tssign,deviceId,ip,userId,shareUserId,shareApp'.split(',')
            d.db.withBatch { stmt ->
                def pat = ~/^(\d{4}\-\d{2}\-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([^\]]+)\]\[([^\]]+)\](.+)$/
                List lines = body.readLines()
                lines.each {
                    // 2016-07-14 00:13:25.083 [ALL][/a/tj/user/info]{"appVer":"2.0","appInfo":"djsq_web","tssign":"1468426405095","deviceId":"2d57f95e-7bbf-42b1-ac49-8a260f4ef184","userId":3102}
                    def mat = it =~ pat
                    if (!mat) {
                        l.warn('collect log not match - ' + it)
                        return
                    }

                    def g = mat[0]
                    String dat = g[1]
                    String module = g[2]
                    String action = g[3]
                    String jsonStr = g[4]

                    JSONObject json = JSON.parseObject(jsonStr)
                    json.reqId = json['_reqId']

                    // 耗时记录
                    if (json.startTime && json.costTime != null) {
                        String sqlCost = "insert into tj_log_cost(req_id,start_time,cost_time) values('${json.reqId}', ${json.startTime}, ${json.costTime})"
                        stmt.addBatch(sqlCost)
                    } else {
                        // 请求信息的记录
                        String uuid = UUID.randomUUID().toString()
                        List args = [uuid, dat, ns, module, action]

                        args += keys.collect { String key ->
                            json.getString(key)
                        }

                        // 除去刚才那几个字段剩余的json数据，是业务数据，也保存
                        String other = JSON.toJSONString(json.grep { !(it.key in keys) && it.key != '_reqId' })
                        args << other

                        String join = args.collect { it == null ? 'null' : ("'" + it + "'") }.join(',')
                        String sql = "insert into tj_log(uuid,dat,ns,module,action,${genSqlKeys(keys, null)},other) values(${join})"
                        stmt.addBatch(sql)
                    }
                }
                l.info('collect log batch size - ' + lines.size())
            }
            l.info('collect log batch done')
            resp.end 'ok'
        } catch (Exception e) {
            l.error('collect log error', e)
            resp.end 'fail'
        }
    }
}

h.get('/a/m/collect-log/:ns/query/:pageNum') { req, resp, ctx ->
    String ns = req.params.ns

    String module = req.params.module
    String action = req.params.action
    String beginDat = req.params.beginDat
    String endDat = req.params.endDat

    // 表已经根据时间分区
    if (!beginDat || !endDat) {
        resp.json([error: 'dat-required'])
        return
    }
    // 相差不能多于1个月
    String fmt = 'yyyy-MM-dd HH:mm:ss'
    Date begin = Date.parse(fmt, beginDat)
    Date end = Date.parse(fmt, endDat)
    if ((end - begin) > 31) {
        resp.json([error: 'dat-range-out'])
        return
    }

    String sql = 'select * from tj_log where dat >= ? and dat <= ?'
    List args = [beginDat, endDat]

    if (ns) {
        sql += ' and ns = ?'
        args << ns
    }
    // 两个是组合索引
    if (module) {
        sql += ' and module = ?'
        args << module
    }
    if (action) {
        sql += ' and action = ?'
        args << action
    }

    int pageNum = req.params.pageNum as int
    int pageSize = req.params.pageSize ? req.params.pageSize as int : 20

    D d = ctx.d

    Pager pager = d.pagi(sql, args, pageNum, pageSize)
    if (pager.ll) {
        String reqIds = pager.ll.collect { it.reqId }.grep { it }.collect { "'" + it + "'" }.join(',')
        List costLl = d.query("select * from tj_log_cost where req_id in (${reqIds})")
        for (one in costLl) {
            Map item = pager.ll.find { it.reqId == one.reqId }
            if (item)
                item.putAll(one)
        }
    }

    resp.json([pager: pager])
}

// 异常日志通知 TODO
h.post('/a/m/collect-log-error/:ns') { req, resp, ctx ->
    String ns = req.params.ns
    String ip = req.params.ip

    D d = ctx.d

    req.bodyHandler { String body ->
        try {

            l.info('collect log batch done')
            resp.end 'ok'
        } catch (Exception e) {
            l.error('collect log error', e)
            resp.end 'fail'
        }
    }
}