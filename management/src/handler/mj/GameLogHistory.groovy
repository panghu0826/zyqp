package handler.mj

import com.alibaba.fastjson.JSONObject
import com.buding.common.result.Result
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.BattleProxy
import comp.common.FieldMerger
import org.slf4j.Logger
import service.SkipLoginMapping

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/game/history/list/:pageNum') { req, resp, ctx ->
    D d = ctx.d;
    int pageNum = req.params.pageNum as int;
    def sql = "select * from game_log where 1=1 "
    def args = []
    sql += " and match_id not like ? "
    args << '%VIP%'

    if(req.params.userId) {
        sql += " and (user1_id = ? or user2_id = ? or user3_id = ? or user4_id = ?) ";
        args << req.params.userId;
        args << req.params.userId;
        args << req.params.userId;
        args << req.params.userId;
    }
    if(req.params.startTime) {
        sql += " and DATE_FORMAT(game_start_time, '%Y-%m-%d') >= ? "
        args << req.params.startTime
    }
    if(req.params.endTime){
        sql += " and DATE_FORMAT(game_end_time, '%Y-%m-%d') <= ? ";
        args << req.params.endTime;
    }

    sql += ' order by game_start_time desc'
    def pager= d.pagi(sql, args, pageNum, 20)

    FieldMerger fieldMerger = ctx.fieldMerger
    fieldMerger.merge(pager.ll, "matchId", "room_name as match_name", "room_conf", "matchId")

    resp.ok([pager:pager])
}


h.get('/a/mj/game/vip/list/:pageNum') { req, resp, ctx ->
    D d = ctx.d;
    int pageNum = req.params.pageNum as int;
    def sql = "SELECT a.*, c.owner_id FROM user_room_result a LEFT JOIN user_room_game_track b ON a.room_id = b.room_id LEFT JOIN user_room c ON b.room_id = c.id   "
    def args = []

    if(req.params.userId) {
        sql += " and b.user_id = ? ";
        args << req.params.userId as int;
    }
    if(req.params.startTime) {
        sql += " and DATE_FORMAT(a.start_time, '%Y-%m-%d') >= ? "
        args << req.params.startTime
    }
    if(req.params.endTime){
        sql += " and DATE_FORMAT(a.end_time, '%Y-%m-%d') <= ? ";
        args << req.params.endTime;
    }

    sql += " ORDER BY a.end_time desc"

    def pager = d.pagi(sql, args, pageNum, 20)

    FieldMerger merger = ctx.fieldMerger;
    merger.merge(pager.ll, "owner_id", "nickname as owner_name", "user")

    pager.ll.each {
        it.detail = JSONObject.parse(it.detail)
    }

    resp.ok([pager:pager])
}