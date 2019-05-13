package handler.mj

import com.buding.common.result.Result
import com.buding.db.model.User
import com.buding.hall.module.common.constants.CurrencyType
import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.HallPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import service.AdminLogService

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/player/list/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def registerStartTime = req.params.registerStartTime
    def registerEndTime = req.params.registerEndTime
    def auth = req.params.auth as int ;
    def userType = req.params.userType
    def deviceType = req.params.deviceType
    def online = req.params.online

    D d = ctx.d;
    def sql = "select *, '获取失败' as online from user where binded_match is null "
    def args = []
    if(name) {
        sql += " and (user_name like ? or nickname like ? or id like ? ) "
        def key = "%"  + name + "%"
        args << key
        args << key
        args << key
    }
    if(registerStartTime) {
        sql += " and ctime >= ? "
        args << registerStartTime
    }
    if(registerEndTime) {
        sql += " and ctime <= ? "
        args << registerEndTime
    }
    if(userType) {
        sql += " and user_type = ? "
        args << userType
    }
    if(deviceType) {
        sql += " and device_type =  ? "
        args << deviceType
    }

    if(auth == 1) {
        sql += " and (role & 2) = 2 " //已授权
    }
    if(auth == 2) {
        sql += " and (role & 2) <> 2 "; //未授权
    }
    sql += " order by ctime desc"
    def pager = d.pagi(sql, args, page, 20)
    def onlineList = []
    def offlineList = []
    try {
        pager.ll.each {
            HallProxy hallProxy = ctx.hallProxy
            boolean b = hallProxy.getHallService(c.zkHost).isUserOnline(it.id)
            it.online = b ? "在线" : "离线"
        }
    } catch (Exception e) {
        l.error("", e)
    }
    int onlinePlayerNum=0
    List<Map<String, Object>> list = d.query("select id,nickname,diamond,coin from user where wxunionid is not null")
    list.each{
        if(ctx.hallProxy.getHallService(c.zkHost).isUserOnline(it.id)){
            onlinePlayerNum++;
            it.online ="在线"
            onlineList << it
        }else{
            it.online ="离线"
            offlineList << it
        }
    }
    if("1".equals(online)) {
        pager.totalCount = onlineList.size()
        pager.pageNum = 1
        pager.pageSize = onlineList.size()
        pager.ll = onlineList
    }
//    else if("2".equals(online)){
//        pager.totalCount = offlineList.size()
//        pager.pageNum = (pager.totalCount%20>0?1:0)+pager.totalCount/20
//        pager.pageSize = 20
//        pager.ll = offlineList
//    }
    resp.ok([pager:pager,onlinePlayerNum:onlinePlayerNum+""])
}

h.get('/a/mj/player/clublist/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def registerStartTime = req.params.registerStartTime
    def registerEndTime = req.params.registerEndTime

    D d = ctx.d;
    def sql = "select * from t_club where 1=1 "
    def args = []
    if(name) {
        sql += " and (club_name like ? or id like ? ) "
        def key = "%"  + name + "%"
        args << key
        args << key
    }
    if(registerStartTime) {
        sql += " and ctime >= ? "
        args << registerStartTime
    }
    if(registerEndTime) {
        sql += " and ctime <= ? "
        args << registerEndTime
    }
    def pager = d.pagi(sql, args, page, 20)

    resp.ok([pager:pager])
}

h.get('/a/mj/player/clubmember/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    int id = req.params.id as int

    D d = ctx.d;
    def sql = "select a.*,b.nickname as club_member_name from t_club_user a,user b where a.club_member_id = b.id and a.club_id=? "
    def args = []
    args << id

    def pager = d.pagi(sql, args, page, 20)

    resp.ok([pager:pager])
}
h.get('/a/mj/player/clubapply/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    int id = req.params.id as int

    D d = ctx.d;
    def sql = "select a.*,b.nickname as apply_user_name from t_club_apply a,user b where a.apply_user_id = b.id and a.club_id=? "
    def args = []
    args << id

    def pager = d.pagi(sql, args, page, 20)

    resp.ok([pager:pager])
}

h.get('/a/mj/player/rankdetail/') { req, resp, ctx ->
    int pointType = req.params.rankType as int
    long groupDatetime = req.params.groupDatetime as long
    def gameId = req.params.gameId
    D d = ctx.d
    def sql = "select a.rank_num,concat(a.user_id ,'_', b.nickname) as user,a.point,a.game_id,a.group_datetime,a.ctime from user_rank_detail a,user b where a.user_id = b.id and point_type = ? and group_datetime = ? and game_id = ?"
    def list = d.query(sql, [pointType,groupDatetime,gameId])
    resp.ok(list)
}

h.get('/a/mj/player/detail/:id') { req, resp, ctx ->
    int userId = req.params.id as int
    D d = ctx.d;
    def map = d.queryMap("select * from user where id = ? ", [userId])
    resp.ok(map)
}

h.get('/a/mj/player/coin/edit') { req, resp, ctx ->
    int userId = req.params.userId as int
    int coin = req.params.coin as int
    HallProxy hallProxy = ctx.hallProxy
    User user = hallProxy.getHallService(c.zkHost).getUser(userId)
    int change = coin - user.getCoin()
    Result ret = hallProxy.getHallService(c.zkHost).changeCoin(userId, change, false, ItemChangeReason.ADMIN_CHANGE)
    if(ret.isOk()) {
        def str = "给玩家${user.nickname}(${user.id})增加金币${change}个"
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_COIN", userId, 0, 0, change, new Date(), str)
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/fanka/edit') { req, resp, ctx ->
    int userId = req.params.userId as int
    int fanka = req.params.fanka as int
    HallProxy proxy = ctx.hallProxy
    def srv = proxy.getHallService(c.zkHost)
    User user = srv.getUser(userId)
    int change = fanka - user.getFanka()
    int from = user.getFanka()
    int to = fanka
    Result ret = srv.changeFangka(userId, change, false, ItemChangeReason.ADMIN_CHANGE)
    if(ret.isOk()) {
        def str = "给玩家${user.nickname}(${user.id})增加房卡${change}个"
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_FANGKA", userId, from, to, change, new Date(), str)
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/diamond/edit') { req, resp, ctx ->
    int userId = req.params.userId as int
    int diamond = req.params.diamond as int
    HallProxy proxy = ctx.hallProxy
    def srv = proxy.getHallService(c.zkHost)
    User user = srv.getUser(userId)
    int change = diamond - user.getDiamond()
    int from = user.getDiamond()
    int to = diamond
    Result ret = srv.changeDiamond(userId, change, false, ItemChangeReason.ADMIN_CHANGE)
    if(ret.isOk()) {
        def str = "给玩家${user.nickname}(${user.id})增加钻石${change}个"
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_FANGKA", userId, from, to, change, new Date(), str)
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/coin/move') { req, resp, ctx ->
    int fromUser = req.params.fromUser as int
    int toUser = req.params.toUser as int
    int coin = req.params.coin as int
    HallProxy hallProxy = ctx.hallProxy
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    User fromUserModel = userService.getUser(fromUser)
    User toUserModel = userService.getUser(toUser)
    if(!fromUserModel || !toUserModel) {
        resp.fail("玩家不存在")
        return;
    }

    Result ret = userService.hasEnoughCurrency(fromUser, CurrencyType.coin, coin)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }

    ret = userService.changeCoin(fromUser, -coin, true, ItemChangeReason.MOVE)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }
    ret = userService.changeCoin(toUser, coin, false, ItemChangeReason.MOVE)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }
    resp.ok()
}

h.post('/a/mj/player/fangka/batch_add') { req, resp, ctx ->
    req.jsonHandler {json->
        def users = json.users
        def fangka = json.fangka as int
        HallProxy hallProxy = ctx.hallProxy;
        HallPortalService userService = hallProxy.getHallService(c.zkHost)
        users.each {
            def userId = it.userId as int
            def user = ctx.d.queryMap("select * from user where id = ?", [userId])
            Result ret = userService.changeFangka(userId, fangka, false, ItemChangeReason.ADMIN_CHANGE)
            it.result = ret.isOk()
            it.msg = ret.msg;
            if(ret.isOk()) {
                int from = user.fanka;
                int to = from + fangka;
                def str = "给玩家${user.nickname}(${user.id})增加房卡${fangka}张"
                AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_FANGKA", userId, from, to, fangka, new Date(), str)
            }
        }
        resp.ok(json)
    }
}

h.post('/a/mj/player/diamond/batch_add') { req, resp, ctx ->
    req.jsonHandler {json->
        def users = json.users
        def diamond = json.diamond as int
        HallProxy hallProxy = ctx.hallProxy;
        HallPortalService userService = hallProxy.getHallService(c.zkHost)
        users.each {
            def userId = it.userId as int
            def user = ctx.d.queryMap("select * from user where id = ?", [userId])
            Result ret = userService.changeDiamond(userId, diamond, false, ItemChangeReason.ADMIN_CHANGE)
            it.result = ret.isOk()
            it.msg = ret.msg;
            if(ret.isOk()) {
                if(diamond>0){
                    userService.payWithFenXiao(userId,diamond,"1");
                }
                int from = user.diamond;
                int to = from + diamond;
                def str = "给玩家${user.nickname}(${user.id})增加钻石${diamond}个"
                AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_DIAMOND", userId, from, to, diamond, new Date(), str)
            }
        }
        resp.ok(json)
    }
}

h.get('/a/mj/player/auth/cancel') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.cancelAuth(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/auth/add') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.auth(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/passwd/change') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    def passwd = req.params.passwd
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.resetPasswd(userId, passwd)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/desk/clear') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    BattlePortalBroadcastService srv = hallProxy.getBattleService(c.zkHost)
    Result ret = srv.clearDesk("all", userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/user_type/change') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService srv = hallProxy.getHallService(c.zkHost);
    Result ret = srv.changeUserType(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/rank/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def rankType = req.params.rankType as int
    def gameType = req.params.gameType as int

    D d = ctx.d
    def sql = "select * from user_rank_detail where 1=1"
    def args = []
    if(rankType) {
        sql += " and point_type = ?  "
        args << rankType
    }
    if(gameType) {
        sql += " and game_id = ? "
        def key
        if(gameType == 1){
            key = "G_DQMJ"
        }else if(gameType == 2){
            key = "G_SHMJ"
        }else if(gameType == 3){
            key = "G_QSMJ"
        }
        args << key
    }

    sql += " group by group_datetime"
    def pager = d.pagi(sql, args, page, 20)
//    pager.ll.eachWithIndex  {it,i ->
//        it.rankId = page*20+i-19
//    }
//    def rankShu = rankType.equals("0")?"开局数":(rankType.equals("1")?"开房数":"财富值")
    resp.ok([pager:pager])
}