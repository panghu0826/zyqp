package handler.mj

import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.MsgPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/msg/marquee/list') { req, resp, ctx ->
    D d = ctx.d;
    def list = d.query("select * from marquee where status = 1 and end_time > ? ", [new Date()])
    resp.ok(list)
}

h.post('/a/mj/msg/marquee/save') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d
        def map = Utils.getMapInKeys(json, "id,msgContent,loopPlayCount,startTime,endTime,marqueeType,loopPushCount,loopPushInterval,userGroup,pushOnLogin")
        map.loopPushCount = map.loopPushCount ? map.loopPushCount : 1;
        map.loopPushInterval = map.loopPushInterval ? map.loopPushInterval : 1;
        map.userGroup = map.userGroup ? map.userGroup : 0;
        map.pushOnLogin = map.pushOnLogin? map.pushOnLogin : 0
        map.status = 1;
        map.id = d.add(map, "marquee")

        HallProxy proxy = ctx.hallProxy;
        MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
        portal.sendMarquee(map.id, true)

        resp.ok()
    }
}

h.get('/a/mj/msg/marquee/del') { req, resp, ctx ->
    long id = req.params.id as long
    D d = ctx.d;
    d.update([id:id, status:10], "marquee")

    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    portal.removeMarquee(id)
    resp.ok()
}

h.get('/a/mj/msg/marquee/send') { req, resp, ctx ->
    long id = req.params.id as long
    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    portal.sendMarquee(id, false)
    resp.ok()
}



h.get('/a/mj/msg/mail/list') { req, resp, ctx ->
    //只返回系统发送的
    D d = ctx.d;
    def list = d.query("select * from msg where status = 1 and stop_date_time > ? and sender_id = -1 order by id desc ", [new Date()])
    resp.ok(list)
}

h.post('/a/mj/msg/mail/save') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,title,msg,stopDateTime,targetType,targetId,itemId,itemCount")
        map.senderId = -1;
        map.senderName = "系统"
        map.msgMainType=0
        map.attachNum = map.itemId ? 1 : 0
        map.status = 1;
        map.startDateTime = new Date();

        if(map.itemId) {
            def award = [items:"[{itemId:'${map.itemId}',count:${map.itemCount}}]" as String, invalidTime:map.stopDateTime, srcSystem:'mailSys', awardNote:'后台邮件赠送', awardType:map.targetType, receiverId:map.targetId, ctime:new Date(), awardReason:ItemChangeReason.ADMIN_MAIL_ATTACH.toString()]
            map.rewardId = d.add(award, "award")
        }
        map.id = d.add(map, "msg")
        if(map.targetType==1) { //单个玩家的邮件
            def userMsg = [msgId:map.id, userId:map.targetId, received:false, deled:false, readed:false, awardId:map.rewardId, ctime:new Date(), mtime:new Date()]
            d.add(userMsg, "user_msg")
        }
        resp.ok()
    }
}

h.get('/a/mj/msg/marquee/del') { req, resp, ctx ->
    long id = req.params.id as long
    D d = ctx.d;
    d.update([id:id, status:10], "marquee")

    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    portal.removeMarquee(id)
    resp.ok()
}

h.get('/a/mj/msg/mail/del') { req, resp, ctx ->
    long id = req.params.id as long

    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    portal.removeMail(id)
    resp.ok()
}

h.get('/a/mj/msg/mail/send') { req, resp, ctx ->
    long id = req.params.id as long
    HallProxy proxy = ctx.hallProxy;
    MsgPortalService portal = proxy.getMsgServicePortal(c.zkHost);
    portal.sendMail(id)
    resp.ok()
}