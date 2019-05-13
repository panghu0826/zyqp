package handler.wx

import com.buding.common.result.Result
import com.buding.db.model.User
import com.buding.hall.module.common.constants.CurrencyType
import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.HallPortalService
import com.google.gson.Gson
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import service.AdminLogService

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.post('/a/wx/order_notify') { req, resp, ctx ->
    l.info("wx post callback request in")

    HallProxy proxy = ctx.hallProxy;
    req.bodyHandler {body->
        l.info "body: $body"
        String ret = proxy.getHallService(c.zkHost).onWxPayCallback(body)
        l.info("wx order resp: " + ret)
        resp.end(ret)
    }
}

h.post('/a/ali/order_notify') { req, resp, ctx ->
    l.info("ali post callback request in")

    def body = new Gson().toJson(req.params)
    l.info "params: " + body

    l.info "body::::" + body
    HallProxy proxy = ctx.hallProxy;
    String ret = proxy.getHallService(c.zkHost).onAliPayCallback(body)
    l.info("ali order resp: " + ret)
    resp.end(ret)
}