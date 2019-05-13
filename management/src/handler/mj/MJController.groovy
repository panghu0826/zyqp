package handler.taoke

import com.alibaba.fastjson.JSONObject
import com.buding.common.result.Result
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.BattleProxy
import org.slf4j.Logger
import service.SkipLoginMapping

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.post('/a/mj/paixing/save') { req, resp, ctx ->
    def id = -1;
    req.jsonHandler { json ->
        def user = req.getLocal('login_user')
        D d = ctx.d;
        if(json.id) {
            d.update([id:json.id, data:json.toString()], "mj_tpl")
        } else {
            json.id = d.add([data:json.toString()], "mj_tpl");
        }
        resp.ok([id:json.id])
    }
}


h.get('/a/mj/paixing/list/:pageNum') { req, resp, ctx ->
    D d = ctx.d;
    int pageNum = req.params.pageNum as int;
    def pager= d.pagi("select * from mj_tpl", [], pageNum, 20)
    pager.ll.each{
        it.data = JSONObject.parse(it.data)
    }
    resp.ok([pager:pager])
}

h.get('/a/mj/paixing/detail/:id') { req, resp, ctx ->
    D d = ctx.d;
    int id = req.params.id as int;
    def map = d.queryMap("select * from mj_tpl where id = ? ", [id])
    map.data = JSONObject.parse(map.data)
    resp.ok(map)
}

h.get('/a/mj/paixing/replay') { req, resp, ctx ->
    int id = req.params.id as int
    int playerId = req.params.playerId as int;
    D d = ctx.d;
    def map = d.queryMap("select * from mj_tpl where id = ? ", [id])
    def data = map.data as String;
    BattleProxy srv = ctx.battleProxy;
    Result ret = srv.replay(playerId, data)
    if(ret.code == 0) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/paixing/del') { req, resp, ctx ->
    D d = ctx.d;
    int id = req.params.id as int
    d.del(id, "mj_tpl")
    resp.ok()
}

SkipLoginMapping.inst.skipLogin=true;
h.get('/a/mj/paixing/test') { req, resp, ctx ->
    BattleProxy srv = ctx.battleProxy;
    Result ret = srv.replay(111, null)
    if(ret.code == 0) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

