package handler.mj

import com.buding.hall.module.ws.HallPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import service.AdminLogService
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/conf/shop/list') { req, resp, ctx ->
    D d = ctx.d;
    def list = d.query("select * from mall_conf where status <> 10")
    list.each {

    }
    resp.ok(list)
}

h.post("/a/mj/conf/shop/save") {req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,productCode,name,category,img,itemId,itemCount,price")
        if(map.id) {
            d.update(map, "mall_conf")
            AdminLogService.addAdminLog(req.getLocal("user").id, "EDIT_MALL_CONF", map.productCode, 0, 0, 1, new Date(), "修改了商品${map.productCode}配置")
        } else {
            map.status = 0;
            map.publish=false;
            d.add(map, "mall_conf")
            AdminLogService.addAdminLog(req.getLocal("user").id, "EDIT_MALL_CONF", map.productCode, 0, 0, 1, new Date(), "修改了商品${map.productCode}配置")
        }
        resp.ok()
    }
}

h.get('/a/mj/conf/shop/status') { req, resp, ctx ->
    int id = req.params.id as int
    int status = req.params.status as int
    D d = ctx.d;
    d.update([id:id, status:status], "mall_conf")
    resp.ok()
}

h.get('/a/mj/conf/shop/publish') { req, resp, ctx ->
    D d = ctx.d;
    def list = d.query("select * from mall_conf where status = 1")
    list.each {
        it.confJson = """
            {
            "id":"${it.productCode}",
            "name":"${it.name}",
            "category":${it.category},
            "img":"${it.img}",
            "clientType":[1,2,3,4],
            "desc":"${it.itemCount}${it.name}",
            "cItemCount":${it.itemCount},
            "price":{
               "currenceType":"4",
               "currenceCount":${it.price}
             },
            "status":1,
            "items":[
             {
                "itemId":"${it.itemId}",
                "count":${it.itemCount}
             }
            ]
            }
        """ as String;
        d.update([id:it.id, confJson:it.confJson, publish:true], "mall_conf")
        AdminLogService.addAdminLog(req.getLocal("user").id, "PUBLISH_MALL_CONF", "", 0, 0, 1, new Date(), "发布了商品配置")
    }

    HallProxy hallProxy = ctx.hallProxy
    HallPortalService hallService = hallProxy.getHallService(c.zkHost)
    hallService.reloadMallConf();
    resp.ok()
}