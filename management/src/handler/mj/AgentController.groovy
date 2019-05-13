package handler.mj

import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.HallPortalService
import com.guosen.webx.d.D
import com.guosen.webx.util.StatusCode
import com.guosen.webx.web.ChainHandler
import comp.VerifyCodeUtils
import comp.battle.HallProxy
import org.slf4j.Logger
import service.AdminLogService
import service.AgentService
import com.buding.common.result.Result
import com.buding.db.model.User

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/agent/data/ana2/target-set/ngjs') { req, resp, ctx ->
    resp.headers.set('Content-Type', 'application/javascript')
    if (req.params.underscore != null) {

        resp.sendFile(c.webroot + '/m/ng-ext/underscore.min.js')
    } else if (req.params.pace != null) {
        resp.sendFile(c.webroot + '/static/ana/loading.js')
    } else if (req.params.jquery != null) {
        resp.sendFile(c.webroot + '/m/ng-ext/jquery-1.8.3.min.js')
    } else {
        resp.sendFile(c.webroot + '/m/ng-ext/angular.min.js')
    }
}

h.get('/a/agent/home') { req, resp, ctx ->
    resp.headers.set('Content-Type', 'text/html')
    resp.sendFile(c.webroot + '/static/ana/home.html')
}

h.get('/a/agent/get_verify_code') { req, resp, ctx ->
    def code = VerifyCodeUtils.generateVerifyCode(4)
    def output = new ByteArrayOutputStream()
    VerifyCodeUtils.outputImage(200, 50, output, code)
    resp.resp.setContentType("image/jpeg")
    resp.end(output.toByteArray())
    req.session("verifyCode", code)
}

h.get('/a/agent/login') { req, resp, ctx ->
    def username = req.params.username
    def password = req.params.password
//    def verifyCode = req.params.verifyCode
//    if(!username || !password || !verifyCode) {
    if(!username || !password) {
        resp.fail("请正确填写登录信息")
        return;
    }
//    def sessionCode = req.session("verifyCode")
//    if(verifyCode != sessionCode) {
//        resp.fail("验证码错误")
//        return;
//    }

    D d = ctx.d;
    def agent = d.queryMap("select * from agent where username = ? and status != 10", [username])
    if(!agent) {
        resp.fail("代理商不存在")
        return;
    }
    if(agent.passwd != password) {
        resp.fail("密码错误")
        return;
    }
    if(agent.status != 1){
        resp.fail("帐号被冻结")
    }
    req.session("agent", agent)
    resp.ok()
}

h.get('/a/agent/logout') { req, resp, ctx ->
    resp.render("/static/ana/login.html", [:])
}

h.get('/a/agent/sell_fangka') { req, resp, ctx ->
    def agent = req.session('agent')
    if(!agent) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    int userId = req.params.userId as int
    int fangka = req.params.fangka as int
    AgentService.sellFangka(agent.id, userId, fangka, req, resp)
}

h.get('/a/agent/my_info') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    D d = ctx.d;
    int agentId = a.id;
    def agent = d.queryMap("select * from agent where id = ? ", [agentId])
    def thisMonth = new Date().format("yyyy-MM")
    def thisMonthSell = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 2 AND from_user_id = ? AND DATE_FORMAT(deal_date, '%Y-%m') = ? ", [a.id, thisMonth]).c as int
//    def totalSell = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 2 AND from_user_id = ?  ", [a.id]).c as int
    def thisMonthBuy = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 1 AND to_user_id = ? AND DATE_FORMAT(deal_date, '%Y-%m') = ? ", [a.id, thisMonth]).c as int
//    def totalBuy = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 1 AND to_user_id = ?  ", [a.id]).c as int

    agent.monthSalled = thisMonthSell;
    agent.monthBuyed = thisMonthBuy;
    agent.totalSelled = agent.totalSellFangka;
    agent.totalBuyed = agent.totalBuyFangka;
    resp.ok(agent)
}

h.get('/a/agent/modify') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    int agentId = a.id;
    def name = req.params.name;
    def passwd = req.params.newpasswd

    D d = ctx.d;
    def agent = d.queryMap("select * from agent where id = ? ", [agentId])
    if(!agent) {
        resp.fail("代理商不存在");
        return;
    }
    if(name) {
        agent.name = name;
    }
    if(passwd) {
        agent.passwd = passwd;
    }
    d.update(agent, "agent")
    resp.ok();
}

h.get('/a/agent/home/main') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    int agentId = a.id;
    D d = ctx.d;
    def agent = d.queryMap("select * from agent where id = ? ", [a.id])
    def fangka = agent.fangka
    def integral = agent.integral

    def today = new Date().format("yyyy-MM-dd")
    def todaySell = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 2 AND from_user_id = ? AND deal_date = ? ", [a.id, today]).c as int
    def totalSell = d.queryMap("SELECT IFNULL(SUM(deal_amount), 0) AS c FROM agent_fangka_log WHERE deal_type = 2 AND from_user_id = ?  ", [a.id]).c as int
    def money = todaySell*6;
    resp.ok([fangka:fangka, integral:integral, todaySell:todaySell, money:money]);
}

h.get('/a/agent/home/load_charge') { req, resp, ctx ->
//    int userId = req.params.userId as int;
//    HallProxy hallProxy = ctx.hallProxy;
//    User user = hallProxy.getHallService(c.zkHost).getUser(userId)
//    def online = hallProxy.getHallService(c.zkHost).isUserOnline(userId) ? "在线" : "离线"
//    resp.ok([userId:user.id, nickName:user.nickname, currentFangka:user.fanka, online:online])
}

h.get('/a/agent/notice/list') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    D d = ctx.d;
    def list = d.query("select * from agent_notice order by ctime desc")
    resp.ok(list)
}

h.get('/a/agent/user_info') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }

    if(!req.params.id) {
        resp.fail("请提供用户id")
        return
    }

    D d = ctx.d;
    def user = d.queryMap("select * from user where id = ?", [req.params.id as int])
    if(!user) {
        resp.fail("用户不存在")
        return;
    }
    resp.ok([id:user.id, nickName:user.nickname, fangka:user.fanka])
}

h.get('/a/agent/user_charge') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }

    if(!req.params.id || !req.params.chargeCount) {
        resp.fail("请提供用户id或充值数量")
        return
    }

    int userId = req.params.id as int
    int chargeCount = req.params.chargeCount as int;

    if(chargeCount <= 0) {
        resp.fail("充值数量错误")
        return;
    }

    def agent = ctx.d.queryMap("select * from agent where id = ? ", [a.id])
    if(!agent) {
        resp.fail("代理商不存在")
        return;
    }
    if(agent.fangka < chargeCount) {
        resp.fail("房卡不足")
        return;
    }

    HallProxy proxy =  ctx.hallProxy;
    HallPortalService srv = proxy.getHallService(c.zkHost)
    User user = srv.getUser(userId)
    if(!user) {
        resp.fail("用户不存在")
        return;
    }

    int from = user.fanka;
    int to = from + chargeCount;
    Result ret = srv.changeFangka(userId, chargeCount, false, ItemChangeReason.AGENT_CHARGE);
    if(ret.isOk()) {
        D d = ctx.d;
        agent.totalSellFangka += chargeCount
        agent.fangka -= chargeCount
        agent.mtime = new Date()
        d.update(agent,"agent")
        def str = "代理商[${a.name}(${a.id})]给玩家${user.userName}充值房卡${chargeCount}个" as String
        AdminLogService.addAgentLog(a.id, a.name, user.id, user.userName,chargeCount, 2, new Date(), str, a.id);
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/agent/user_charge_log') { req, resp, ctx ->
    def a = req.session('agent')
    if(!a) {
        resp.fail(StatusCode.NEED_LOGIN, "没有登录")
        return;
    }
    D d = ctx.d;
    def sql = "select a.*,b.nickname from agent_fangka_log a,user b where a.to_user_id=b.id and a.agent_id = ? and a.deal_type = 2 order by a.deal_date desc"
    def args = [a.id]
    if(req.params.searchUser){
        sql += " and to_user_id = ?"
        args << (req.params.searchUser as int)
    }
    def list = d.query(sql, args)
    resp.ok(list)
}