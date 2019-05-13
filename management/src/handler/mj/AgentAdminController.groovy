package handler.mj

import com.guosen.webx.d.D
import com.guosen.webx.d.NamingStyleUtils
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import comp.user.User
import org.slf4j.Logger
import service.AdminLogService
import service.UserService
import utils.Utils

import java.text.SimpleDateFormat
import java.util.regex.Pattern

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config


h.get('/a/mj/agent/list/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def registerStartTime = req.params.registerStartTime
    def registerEndTime = req.params.registerEndTime
    def user = req.session('user')

    D d = ctx.d;
    def sql = "select * from agent where status <> 10 "
    def args = []
    if(user.role == 262144){//3代
        sql += " and manager_user_name = ? "
        args << user.phone
    }
    if(user.role == 64 ){//2代
        def list = d.query("select * from tj_agent_ascription where second_agent = ? ", [user.phone+"("+user.username+")"])

        if(list && list.size()>0){
            sql += " and (manager_user_name = ? "
            args << user.phone
            for(def li : list){
                if(li.thirdAgent) {
                    sql += " or manager_user_name = ? "
                    args << li.thirdAgent.toString().substring(0, 11)
                }
            }
            sql += ")"
        }else{
            sql += " and manager_user_name = ? "
            args << user.phone
        }
    }
    if(name) {
        sql += " and (username like ? or name like ? ) "
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
    def fangKaNum
    if(user.role == 64 ||user.role == 262144 ){
        fangKaNum = d.queryMap("select fangka_num from t_sub_fangka where sub_user = ?",[user.phone])["fangkaNum"]
    }else{
        fangKaNum="不限"
    }
    resp.ok([pager:pager,fangKaNum:fangKaNum])
}

h.get('/a/mj/agent/fangka/add') { req, resp, ctx ->
    int id = req.params.id as int
    int count = req.params.fangka as int
    D d = ctx.d;
    def user = req.session('user')
    if(user.role == 64 ||user.role == 262144 ){
        int fangKaNum = d.queryMap("select fangka_num from t_sub_fangka where sub_user = ?",[user.phone])["fangkaNum"]
        if(count> fangKaNum){
            resp.fail("房卡不足")
            return
        }
        d.db.executeUpdate("update t_sub_fangka set fangka_num = ? where sub_user = ? ", [(fangKaNum-count),user.phone])
    }
    def agent = d.queryMap("select * from agent where id = ? ", [id])
    if(agent) {
        d.update([fangka:agent.fangka + count, totalBuyFangka:agent.totalBuyFangka+count, id : id], "agent")
        d.add([fromUserId:-1, fromUserName:'系统管理员', toUserId:agent.id, toUserName:agent.name, dealAmount:count, dealType:1, dealDate:new Date(), dealDesc:"购入房卡${count}张" as String, agentId:agent.id], "agent_fangka_log")
        resp.ok([fangka:agent.fangka + count, totalBuyFangka:agent.totalBuyFangka+count])
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_AGENT_FANGKA", id, 0, 0, count, new Date(), "出售房卡${count}张给代理商${agent.username}")
        return;
    }
    resp.fail("代理商不存在")
}

h.get('/a/mj/agent/integral/add') { req, resp, ctx ->
    int id = req.params.id as int
    int count = req.params.integral as int
    D d = ctx.d;
    def agent = d.queryMap("select * from agent where id = ? ", [id])
    if(agent) {
        d.update([integral:agent.integral + count, id : id, totalBuyFangka:agent.totalBuyFangka+count], "agent")
        resp.ok([integral:agent.integral + count])
        return;
    }
    resp.fail("代理商不存在")
}

h.post('/a/mj/agent/info/add') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def currentUser = req.session("user")
        def map = Utils.getMapInKeys(json, "id,username,passwd,name,managerUserName")
        def user = d.queryMap("select * from tj_user where phone = ? ", [map.managerUserName])
        if(!map.managerUserName){//未填写代理账号
            if(currentUser.role == 65536 || currentUser.role == 4096){
                resp.fail("客服(或客服经理)必须填写代理账号")
                return
            }else{
                map.managerUserName = currentUser.phone
            }
        }else{
            if(currentUser.role == 262144){//3代
                if(map.managerUserName != currentUser.phone){
                    resp.fail("三级代理只能给自己加代理商")
                    return
                }
            }else if(currentUser.role == 64){//2代
                if(map.managerUserName != currentUser.phone){
                    if(!user){
                        resp.fail("代理账号不存在")
                        return
                    }
                    def args = []
                    args << currentUser.phone+"("+currentUser.username+")"
                    args << map.managerUserName+"("+user.username+")"
                    def agentAscription = d.queryMap("select * from tj_agent_ascription where second_agent = ? and third_agent = ? ",args)
                    if(!agentAscription){
                        resp.fail("该代理账号不属于你")
                        return
                    }
                }
            }
        }

        map.mtime = new Date();
        if(map.id) {
            d.update(map, "agent")
        } else {
            map.ctime = new Date();
            map.status = 1;
            if(!user){
                user = currentUser
            }
            map.agentLevel = user.role==64?2:(user.role == 262144?3:1)
            d.add(map, "agent")
            AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_AGENT", map.username, 0, 0, map.username, new Date(), "增加代理商:${map.username}")
        }

        resp.ok()
    }
}

h.get('/a/mj/agent/status') { req, resp, ctx ->
    def id = req.params.id as int
    def status = req.params.status as int
    D d = ctx.d;
    d.update([id:id, status:status], "agent")
    if(status == 10) {
        AdminLogService.addAdminLog(req.getLocal("user").id, "DEL_AGENT", id, 0, 0, id, new Date(), "删除代理商,id:${id}")
    }
    resp.ok()
}


h.get('/a/mj/agent/day_report') { req, resp, ctx ->
    def startDate = req.params.startDate;
    def endDate = req.params.endDate;

    D d = ctx.d;
    def args = []

    def groupByDaySql = "SELECT DATE_FORMAT(deal_date, '%Y-%m-%d') as deal_date, SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell FROM agent_fangka_log where 1=1 "
    def allSql = "SELECT SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell FROM agent_fangka_log where 1=1 ";

    if(startDate) {
        args << startDate
        groupByDaySql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ? "
        allSql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ? "
    }
    if(endDate) {
        args << endDate
        groupByDaySql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
        allSql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
    }

    def user = req.session("user")
    if(user.role==262144){
        allSql += " and to_user_name in (select name from agent where manager_user_name = ? )"
        groupByDaySql += " and to_user_name in (select name from agent where manager_user_name = ? )"
        args << user.phone
    }else if(user.role == 64){
        def list = d.query("select * from tj_agent_ascription where second_agent = ? ", [user.phone+"("+user.username+")"])

        if(list && list.size()>0){
            allSql += " and (to_user_name in (select name from agent where manager_user_name = ?) "
            groupByDaySql += " and (to_user_name in (select name from agent where manager_user_name = ?) "
            args << user.phone
            for(def li : list){
                if(li.thirdAgent) {
                    allSql += " or to_user_name in (select name from agent where manager_user_name = ?) "
                    groupByDaySql += " or to_user_name in (select name from agent where manager_user_name = ?) "
                    args << li.thirdAgent.toString().substring(0, 11)
                }
            }
            allSql += ")"
            groupByDaySql += ")"
        }else{
            allSql += " and to_user_name in (select name from agent where manager_user_name = ? )"
            groupByDaySql += " and to_user_name in (select name from agent where manager_user_name = ? )"
            args << user.phone
        }
    }
    def rows = d.query(groupByDaySql + " GROUP BY DATE_FORMAT(deal_date, '%Y-%m-%d') ORDER BY deal_date desc limit 30", args)

    def total = d.queryMap(allSql, args)

    resp.ok([rows:rows, total:total])
}

h.get('/a/mj/agent/month_report') { req, resp, ctx ->
    D d = ctx.d;
    def args = []

    def sql = "SELECT agent_id, DATE_FORMAT(deal_date, '%Y-%m') AS mon, SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell  FROM agent_fangka_log where 1=1"
    def user = req.session("user")
    if(user.role==262144){
        sql += " and to_user_name in (select name from agent where manager_user_name = ? )"
        args << user.phone
    }else if(user.role == 64){
        def list = d.query("select * from tj_agent_ascription where second_agent = ? ", [user.phone+"("+user.username+")"])

        if(list && list.size()>0){
            sql += " and (to_user_name in (select name from agent where manager_user_name = ?) "
            args << user.phone
            for(def li : list){
                if(li.thirdAgent) {
                    sql += " or to_user_name in (select name from agent where manager_user_name = ?) "
                    args << li.thirdAgent.toString().substring(0, 11)
                }
            }
            sql += ")"
        }else{
            sql += " and to_user_name in (select name from agent where manager_user_name = ? )"
            args << user.phone
        }
    }
    def startDate = req.params.startDate
    def endDate = req.params.endDate
    if(startDate){
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ?";
        args << startDate
    }
    if(endDate){
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
        args << endDate
    }
    sql += " GROUP BY DATE_FORMAT(deal_date, '%Y-%m') ,agent_id "
    if(startDate||endDate){
        sql += "ORDER BY SUM(IF(deal_type=1, 0, deal_amount)) desc"
    }else{
        sql += "ORDER BY mon desc"
    }
    def list = d.query(sql, args)

    FieldMerger merger = ctx.fieldMerger;
    merger.merge(list, "agentId", "username,name", "agent")
    resp.ok(list)
}

h.get('/a/mj/agent/deal/logs') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    int dealType = req.params.dealType as int
    def username = req.params.username
    def startDate = req.params.startDate
    def endDate = req.params.endDate

    D d = ctx.d;
    def args = [dealType]

    def sql = "select * from agent_fangka_log a where 1=1 and deal_type = ? "

    if(username) {
        sql += " and exists (select 1 from agent b where a.agent_id = b.id and b.username like ?) ";
        args << "%"+username+"%"
    }

    if(startDate) {
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ?";
        args << startDate
    }

    if(endDate) {
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
        args << endDate
    }
    def user = req.session("user")
    if(user.role==262144){
        sql += " and to_user_name in (select name from agent where manager_user_name = ? )"
        args << user.phone
    }else if(user.role == 64){
        def list = d.query("select * from tj_agent_ascription where second_agent = ? ", [user.phone+"("+user.username+")"])

        if(list && list.size()>0){
            sql += " and (to_user_name in (select name from agent where manager_user_name = ?) "
            args << user.phone
            for(def li : list){
                if(li.thirdAgent) {
                    sql += " or to_user_name in (select name from agent where manager_user_name = ?) "
                    args << li.thirdAgent.toString().substring(0, 11)
                }
            }
            sql += ")"
        }else{
            sql += " and to_user_name in (select name from agent where manager_user_name = ? )"
            args << user.phone
        }
    }
    sql += " order by deal_date desc "

    def pager = d.pagi(sql, args, pageNum, 20)

    FieldMerger merger = ctx.fieldMerger;
    merger.merge(pager.ll, "agentId", "username,name", "agent")
    resp.ok([pager:pager])
}

h.get('/a/mj/agent/notice/list/:pageNum') { req, resp, ctx ->
    int pageNum =  req.params.pageNum as int
    D d = ctx.d;
    def args = []

    def sql = "select * from agent_notice"
    def list = d.pagi(sql, args, pageNum, 20)

    resp.ok([pager:list])
}

h.get('/a/mj/agent/notice/del') { req, resp, ctx ->
    int id = req.params.id as int
    D d = ctx.d;
    d.del(id, "agent_notice")
    resp.ok()
}

h.post('/a/mj/agent/notice/save') { req, resp, ctx ->
   req.jsonHandler {json->
       def map = Utils.getMapInKeys(json, "title,content")
       map.ctime = new Date()
       D d = ctx.d;
       d.add(map, "agent_notice")
       resp.ok()
   }
}


h.post('/a/mj/agent/addSubuser/add') { req, resp, ctx ->
    req.jsonHandler { json ->
        def currentUser = req.session("user")
        if(currentUser.role!=1024&&currentUser.role!=64){
            resp.fail("只能总代和二代创建")
            return
        }
        User u = ctx.user
        Map user = Utils.getMapInKeys(json, 'username,phone,nickname,email,idCard,signature,password')
        Map existsUser = u.getByPhone(user.phone)
        String id = json.id
        if(user.username.matches("[0-9]+")){
            resp.fail("用户名不能纯数字")
            return
        }
        if(!user.password){
            resp.fail("请输入密码")
            return
        }
        if (existsUser != null && (existsUser.id + "") != id) {
            resp.fail("电话号码已存在")
            return
        }

        if (user.nickname) {
            existsUser = u.getByNickName(user.nickname)
            if (existsUser != null && (existsUser.id + "") != id) {
                resp.fail("昵称已存在")
                return
            }
        } else {
            user.nickname = UserService.randomNickName()
        }

        if (id) {
            u.update(id as int, user)
        } else {
            user.role = currentUser.role == 1024?64:262144
            user.createTime = new Date()
            id = u.add(user)
        }
        D d = ctx.d
        if(currentUser.role == 262144) {//3代

        }else if(currentUser.role == 64) {//2代
            def exist = d.query("select * from tj_agent_ascription where second_agent = ? and third_agent = ? ", [currentUser.phone+"("+currentUser.username+")",user.phone+"("+user.username+")"])
            if(exist && exist.size()>0){
                resp.fail("已存在该手机号")
                return
            }
            List exist2 = d.query("select * from tj_agent_ascription where second_agent = ? ", [currentUser.phone+"("+currentUser.username+")"])
            if(exist2 && exist2.size()==1 && !exist2.get(0)["thirdAgent"]){
                d.db.executeUpdate("update tj_agent_ascription set third_agent = ? where second_agent = ?",[user.phone+"("+user.username+")",currentUser.phone+"("+currentUser.username+")"])
            }else{
                def firstAgent = d.queryMap("select * from tj_user where phone = ? ", [exist2.get(0)["firstAgent"].substring(0,11)])
                d.db.executeInsert("insert into tj_agent_ascription (first_agent,second_agent,third_agent)  values (?,?,?)", [firstAgent.phone+"("+firstAgent.username+")",currentUser.phone+"("+currentUser.username+")",user.phone+"("+user.username+")"])
                d.db.executeInsert("insert into t_sub_fangka (sub_user,sub_level,fangka_num) values (?,?,?)", [user.phone, "3", 0])
            }
        }else if(currentUser.role == 1024) {//总代
            def exist = d.query("select * from tj_agent_ascription where second_agent = ? and second_agent = ? ", [currentUser.phone + "(" + currentUser.username + ")", user.phone + "(" + user.username + ")"])
            if (exist && exist.size() > 0) {
                resp.fail("已存在该手机号")
                return
            }
            d.db.executeInsert("insert into tj_agent_ascription (first_agent,second_agent) values (?,?)", [currentUser.phone + "(" + currentUser.username + ")", user.phone + "(" + user.username + ")"]);
            d.db.executeInsert("insert into t_sub_fangka (sub_user,sub_level,fangka_num) values (?,?,?)", [user.phone, "2", 0])
        }
        resp.ok()
    }
}


h.get('/a/mj/agent/manager_subuser/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def user = req.session('user')
    D d = ctx.d;
    def args = []
    def sql = ""
    if(user.role == 262144) {//3代

    }else if(user.role == 64){//2代
        sql = "select second_agent,third_agent from tj_agent_ascription where 1=1 "
        sql += " and second_agent = ? order by third_agent"
        args << user.phone+"("+user.username+")"
    }else if(user.role == 1024){//总代
        sql = "select first_agent,second_agent,third_agent from tj_agent_ascription where 1=1  "
        sql += " and first_agent = ? order by second_agent"
        args << user.phone+"("+user.username+")"
    }else{
        sql = "select first_agent,second_agent,third_agent from tj_agent_ascription where 1=1 "
    }
    def pager = d.pagi(sql, args, page, 20)
    resp.ok(pager:pager)
}
h.get('/a/mj/agent/qrySubFangka/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def user = req.session('user')
    D d = ctx.d;
    def args = []
    def sql = ""
    if(user.role == 262144) {//3代

    }else if(user.role == 64){//2代
        sql = "select sub_user,sub_level,fangka_num from t_sub_fangka where 1=1 "

        def list = d.query("select * from tj_agent_ascription where second_agent = ? ", [user.phone+"("+user.username+")"])

        if(list && list.size()>0){
            sql += " and (sub_user = ? "
            args << user.phone
            for(def li : list){
                if(li.thirdAgent) {
                    sql += " or sub_user = ? "
                    args << li.thirdAgent.toString().substring(0, 11)
                }
            }
            sql += ")"
        }else{
            sql += " and sub_user = ? "
            args << user.phone
        }
    }else{
        sql = "select sub_user,sub_level,fangka_num from t_sub_fangka where 1=1 "
    }
    if(name){
        sql += " and sub_user like ?  "
        def key = "%"  + name + "%"
        args << key
    }
    def pager = d.pagi(sql, args, page, 20)
    resp.ok(pager:pager)
}
h.get('/a/mj/agent/querySaleFangka/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def currentUser = req.session('user')
    D d = ctx.d;
    def args = []
    def sql = "select * from t_sub_salefangka where 1=1"
    if(currentUser.role == 262144) {//3代

    }else if(currentUser.role == 64){//2代
        sql += " and sale_user = ? "
        args << currentUser.phone
    }
    if(name){
        sql += " and sale_user like ?  "
        def key = "%"  + name + "%"
        args << key
    }
    sql += " order by ctime desc"
    def pager = d.pagi(sql, args, page, 20)
    resp.ok(pager:pager)
}

h.post('/a/mj/agent/subSaleKa') { req, resp, ctx ->
    req.jsonHandler { json ->
        D d = ctx.d
        Map user = Utils.getMapInKeys(json, 'saleUser,subUser,saleFangka')
        def saleFangka = d.query("select * from t_sub_fangka where sub_user = ?",[user.saleUser])
        def subFangka = d.query("select * from t_sub_fangka where sub_user = ?",[user.subUser])
        def sale = d.queryMap("select * from tj_user where phone = ? ", [user.saleUser])
        def sub = d.queryMap("select * from tj_user where phone = ? ", [user.subUser])
        def args = []
        args << user.saleUser+"("+sale.username+")"
        args << user.subUser+"("+sub.username+")"
        args << user.saleUser+"("+sale.username+")"
        args << user.subUser+"("+sub.username+")"
        args << user.saleUser+"("+sale.username+")"
        args << user.subUser+"("+sub.username+")"

        def role = d.query("select * from tj_agent_ascription where 1=1 and (first_agent = ? and second_agent = ?) or (second_agent = ? and third_agent = ?) or (first_agent = ? and third_agent = ?)",args)
        Pattern pattern = Pattern.compile("[0-9]*")
        //判断数字
        if(!pattern.matcher(user.saleFangka)){
            resp.fail("出售房卡必须为数字")
            return
        }
        if(sale.role!=1024){
            if(Integer.valueOf(user.saleFangka)>saleFangka.get(0)["fangkaNum"]){
                resp.fail("出售人房卡不足")
                return
            }
        }
        if(!role || role.size()==0){
            resp.fail("出售人不是接收人的上级代理")
            return
        }
        user.ctime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())
        d.add(user,"t_sub_salefangka")
        if(sale.role!=1024) {
            d.db.executeUpdate("update t_sub_fangka set fangka_num = ? where sub_user = ? ", [(int)saleFangka.get(0)["fangkaNum"]-Integer.valueOf(user.saleFangka),user.saleUser])
        }
        d.db.executeUpdate("update t_sub_fangka set fangka_num = ? where sub_user = ? ", [(int)subFangka.get(0)["fangkaNum"]+Integer.valueOf(user.saleFangka),user.subUser])
        resp.ok()
    }
}