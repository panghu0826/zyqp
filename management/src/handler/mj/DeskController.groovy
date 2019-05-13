package handler.mj

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.buding.common.cluster.model.ServerModel
import com.buding.common.result.Result
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.guosen.webx.d.Pager
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/game/desk/list2/:pageNum') { req, resp, ctx ->
    HallProxy proxy = ctx.hallProxy;
    BattlePortalBroadcastService srv = proxy.getBattleService(c.zkHost)
    def list = srv.getDeskList("all");
    JSONArray retList = JSONObject.parse(list)
    Pager pager = new Pager(1, 100)
    pager.totalCount = retList.size();
    pager.ll = retList
    resp.ok([pager:pager])
}


h.get('/a/mj/game/desk/list/:pageNum') { req, resp, ctx ->
//    HallProxy proxy = ctx.hallProxy;
//    BattlePortalBroadcastService srv = proxy.getBattleService1("dubbo://183.60.106.182:6670/com.buding.hall.module.ws.BattlePortalBroadcastService")
//    def list = srv.getDeskList("all");
//    JSONArray retList = JSONObject.parse(list)
//    Pager pager = new Pager(1, 100)
//    pager.ll = retList
//    resp.ok([pager:pager])

    int pageNum = req.params.pageNum as int
    int pageSize = 100;
    HallProxy proxy = ctx.hallProxy;
    List<ServerModel> battleServerList = proxy.getHallService(c.zkHost).getAllServer("battle")
    List deskList = []
    for(ServerModel sm : battleServerList) {
        BattlePortalBroadcastService srv = proxy.getDirectBattleService("dubbo://${sm.dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService")
        String ret = srv.getDeskList("all")
        JSONArray retList = JSONObject.parse(ret)
        deskList.addAll(retList)
    }
    def playerId = req.params.playerId
    def deskId = req.params.deskId
    deskList = deskList.grep{
        if(!playerId && !deskId) return true;
        if(deskId && it.deskId != deskId) return false;
        if(playerId) {
            if(!it.players){
                return false;
            }
            boolean find = false;
            it.players.each{
                if(it.playerId+"" == playerId) {
                    find = true;
                }
            }
            return find;
        }
        return true;
    }
    deskList.each {
        it->
            it.players.each{
                it2->
                    it2.online = ctx.hallProxy.getHallService(c.zkHost).isUserOnline(it2.playerId) ? "在线" : "离线"
            }
    }
    //deskList.sort {a,b-> return a.createTime.compareTo(b.createTime)}
    Pager pager = new Pager(pageNum, pageSize)
    pager.totalCount = deskList.size();
    def start = (pageNum - 1) * pageSize
    def end = pageNum * pageSize
    start = start >= deskList.size() ? deskList.size() - 1 : start;
    end = end > deskList.size() ? deskList.size() : end;
    pager.ll = deskList? deskList.subList(start, end) : deskList;
    resp.ok([pager:pager])
}

h.get('/a/mj/desk/dismiss') { req, resp, ctx ->
    def gameId = req.params.gameId
    def matchId = req.params.matchId
    def deskId = req.params.deskId
    def instanceId = req.params.instanceId
    HallProxy proxy = ctx.hallProxy;
    List<ServerModel> battleServerList = proxy.getHallService(c.zkHost).getAllServer("battle")
    battleServerList = battleServerList.grep{it.instanceId == instanceId}
    if(!battleServerList) {
        resp.fail("游戏服实例不存在")
        return;
    }
    def sm = battleServerList.get(0)
    BattlePortalBroadcastService srv = proxy.getDirectBattleService("dubbo://${sm.dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService");
    def ret = srv.dismissDesk(instanceId, gameId, matchId, deskId)
    if(ret.code == 0) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/desk/dump') { req, resp, ctx ->
    def gameId = req.params.gameId
    def matchId = req.params.matchId
    def deskId = req.params.deskId
    def instanceId = req.params.instanceId
    HallProxy proxy = ctx.hallProxy;
    List<ServerModel> battleServerList = proxy.getHallService(c.zkHost).getAllServer("battle")
    battleServerList = battleServerList.grep{it.instanceId == instanceId}
    if(!battleServerList) {
        resp.fail("游戏服实例不存在")
        return;
    }
    def sm = battleServerList.get(0)
    BattlePortalBroadcastService srv = proxy.getDirectBattleService("dubbo://${sm.dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService");
    def ret = srv.dump(instanceId+"", gameId+"", matchId+"", deskId+"")
    if(ret.code == 0) {
        resp.headers.set('Content-Type', 'application/json;charset=utf8')
        resp.end(ret.t)
    } else {
        resp.fail(ret.msg)
    }
}