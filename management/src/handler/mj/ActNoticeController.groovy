package handler.mj

import com.buding.common.result.Result
import com.buding.hall.module.ws.MsgPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/actnotice/list') { req, resp, ctx ->
    int type = req.params.type as int
    D d = ctx.d;
    def list = d.query("select * from act_notice where type = ? and status =1  ", [type])
    resp.ok(list)
}

h.get('/a/mj/actnotice/push') { req, resp, ctx ->
    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    Result r = portal.repushActNotice();
    if(r.isOk()) {
        resp.ok()
        return;
    }
    resp.fail(r.msg)
}


h.post('/a/mj/actnotice/save') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,title,content,type")
        map.status = 1;
        if(map.id) {
            d.update(map, "act_notice")
        } else {
            d.add(map, "act_notice")
        }
        HallProxy proxy = ctx.hallProxy;
        MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
        portal.repushActNotice();

        resp.ok()
    }
}

h.get('/a/mj/actnotice/del') { req, resp, ctx ->
    long id = req.params.id as long
    D d = ctx.d;
    d.update([id:id, status:10], "act_notice")
    resp.ok()
}