package handler.mj

import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.slf4j.Logger

import java.text.SimpleDateFormat

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/log/player/list/:pageNum') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "select * from user_currency_log where 1=1 "
    def args = []
    if(req.params.userId) {
        sql += " and user_id = ?"
        args << (req.params.userId as long)
    }
    if(req.params.operMainType) {
        sql += " and oper_main_type = ? "
        args << req.params.operMainType
    }
    if(req.params.operSubType) {
        sql += " and oper_sub_type = ? "
        args << req.params.operSubType
    }
    if(req.params.operTime) {
        sql += " and DATE_FORMAT(oper_time, '%Y-%m-%d') = ? "
        args << req.params.operTime
    }
    sql += " order by id desc"
    def pager = d.pagi(sql, args, pageNum, 20)
    FieldMerger merge = ctx.fieldMerger
    merge.merge(pager.ll, "userId", "nickname as user_name", "user")
    resp.ok([pager:pager])
}

h.get('/a/mj/log/admin/list/:pageNum') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "select * from admin_log where 1=1 "
    def args = []
    if(req.params.userId) {
        sql += " and user_id = ?"
        args << (req.params.userId as long)
    }
    if(req.params.operType) {
        sql += " and oper_type = ? "
        args << req.params.operType
    }
    if(req.params.operTime) {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.params.operTime)
        Date date1 = new Date(date.getTime() * 24*3600*1000)
        sql += " and oper_time >= ? and oper_time <= ?"
        args << date.format("yyyy-MM-dd")
        args << date1.format("yyyy-MM-dd")
    }
    sql += " order by oper_time desc"
    def pager = d.pagi(sql, args, pageNum, 20)
    FieldMerger merge = ctx.fieldMerger
    merge.merge(pager.ll, "userId", "username as user_name", "tj_user")
    resp.ok([pager:pager])
}