package handler.fenxiao

import com.buding.hall.module.ws.HallPortalService
import com.buding.hall.module.ws.TaskPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config
int zengSongDiamond = 60
int initDiamond = 0

h.post('/a/wx/fenxiao/openid') { req, resp, ctx ->
    l.info("fenxiao/wx/openid post callback request in")
    l.info("params"+req.params)

    String sql = 'select wxopenid from user where 1 = 1'
    List args = []

    String unionid = req.params.unionid
    if (unionid) {
        sql += ' and wxunionid =? '
        args << unionid
    }else{
        resp.json([code: 500,openid:"", msg:"unionid不能为空"])
        return ;
    }
    D d = ctx.d
    def result = d.queryMap(sql,args)
    if(result&&result.wxopenid){
        resp.json([code: 200,openid:result.wxopenid, msg:"成功"])
    }else{
        resp.json([code: 501,openid:"", msg:"未找到对应openid"])
    }
}

h.post('/a/wx/fenxiao/createuser') { req, resp, ctx ->
    l.info("/a/wx/fenxiao/createuser post callback request in")
    l.info("params"+req.params)
    String unionid = req.params.unionid
    if (!unionid){
        resp.json([code: 500,openid:"", msg:"unionid不能为空"])
        return
    }

    D d = ctx.d
    def result = d.queryMap('select id from user where wxunionid = ?',[unionid])

    if(result&&result.id){
        resp.json([code: 200,id:result.id, msg:"成功"])
    }else{//没有就新建用户
        HallProxy proxy = ctx.hallProxy;
        HallPortalService hall = proxy.getHallService(c.zkHost)
        def user = hall.initUser()
        user.userType = 3
        user.wxunionid = unionid
        user.ctime = new Date()
        user.mtime = new Date()
        user.passwd = "123456"
        hall.register(user)
        def id = d.queryMap('select * from user where wxunionid = ?',[unionid]).id
        resp.json([code: 200,id:id, msg:"成功"])
    }
}

h.post('/a/wx/fenxiao/submitInviteCode') { req, resp, ctx ->
    l.info("/a/wx/fenxiao/submitInviteCode post callback request in")
    l.info("params"+req.params)
    String unionid = req.params.unionid
    if (!unionid){
        resp.json([code: 500,openid:"", msg:"unionid不能为空"])
        return
    }

    D d = ctx.d
    def result = d.queryMap('select * from user where wxunionid = ?',[unionid])

    if(result&&result.id){
        if(result.hasInvitecode && result.hasInvitecode == 1){
            resp.json([code: 501,id:result.id, msg:"您已经绑定优惠码了"])
            return
        }
        result.hasInvitecode = 1
        def currentDiamond = result.diamond
        if(!currentDiamond){
            currentDiamond = 0
        }
        result.diamond = (int)currentDiamond + zengSongDiamond
        d.update(result,"user")
        resp.json([code: 200,id:result.id, msg:"成功"])
    }else{//没有就新建用户
        HallProxy proxy = ctx.hallProxy;
        HallPortalService hall = proxy.getHallService(c.zkHost)
        def user = hall.initUser()
        user.diamond = initDiamond+zengSongDiamond
        user.userType = 3
        user.passwd = "123456"
        user.wxunionid = unionid
        user.ctime = new Date()
        user.mtime = new Date()
        user.hasInvitecode = 1
        user.headImg = "portrait_img_0" + (new Random().nextInt(8) + 1)
        hall.register(user)
        def id = d.queryMap('select * from user where wxunionid = ?',[unionid]).id
        resp.json([code: 200,id:id, msg:"成功"])
    }
}

h.post('/a/wx/fenxiao/applyClub') { req, resp, ctx ->
    l.info("/a/wx/fenxiao/applyClub post callback request in")
    l.info("params"+req.params)
    resp.getHeaders().set("Access-Control-Allow-Origin","*")

    String unionid = req.params.unionid
    String clubidStr = req.params.clubid
    if (!unionid || !clubidStr){
        l.info("applyClub传参不对")
        resp.json([rspMsg:"传参不对"])
        return
    }

    long clubId = Long.valueOf(clubidStr)

    D d = ctx.d
    def result = d.queryMap('select * from user where wxunionid = ?',[unionid])
    if(!result){
        l.info("applyClub未找到玩家信息,请先下载游戏登录")
        resp.json([rspMsg:"未找到玩家信息,请先下载游戏登录"])
        return
    }
    if(!result.id){
        l.info("applyClub未找到玩家Id,请先下载游戏登录")
        resp.json([rspMsg:"未找到玩家Id,请先下载游戏登录"])
        return
    }
    if(!result.nickname){
        l.info("applyClub未找到玩家微信昵称,请先下载游戏登录")
        resp.json([rspMsg:"未找到玩家微信昵称,请先下载游戏登录"])
        return
    }
    HallProxy proxy = ctx.hallProxy;
    TaskPortalService task = proxy.getTaskService(c.zkHost as String)
    String msg = task.applyClub(result.id as int,clubId)
    l.info("applyClub"+msg)
    resp.json([rspMsg:msg])
}