package handler.user

import com.alibaba.fastjson.JSONObject
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import comp.user.User
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import service.RoleMapping
import service.SrvDescriptor
import service.UserService
import service.UserTree
import utils.JxlUtils
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
RoleMapping rm = RoleMapping.inst

// 只有系统管理员才可以进行用户操作 x
rm.role = 'sys'

// 用户列表
h.get('/a/admin/user/list/:pageNum') { req, resp, ctx ->
    String sql = 'select * from tj_user where 1 = 1'
    List args = []

    String role = req.params.role
    if (role) {
        sql += ' and (role & ? != 0)'
        int i = role as int
        args << i
    }

    String label = req.params.label
    if (label) {
        sql += ' and (label & ? != 0)'
        int i = label as int
        args << i
    }

    String username = req.params.username
    if (username) {
        sql += ' and (username like ?)'
        args << ("%" + username + "%")
    }

    String phone = req.params.phone
    if (phone) {
        sql += ' and (phone like ?)'
        args << ("%" + phone + "%")
    }

    String status = req.params.status
    if (status) {
        sql += ' and (status = ?)'
        args << status
    }
    sql += ' order by create_time desc '
    boolean needQueryRoleList = 'true' == req.params.needQueryRoleList
    boolean needQueryLabelList = 'true' == req.params.needQueryLabelList

    int pageNum = req.params.pageNum as int

    def d = ctx.d
    def pager = d.pagi(sql, args, pageNum, 10)

    Map r = [pager: pager]
    if (needQueryRoleList) {
        User u = ctx.user
        r.roleList = u.getAllRoleList()
    }
    if (needQueryLabelList) {
        User u = ctx.user
        r.labelList = u.getAllLabelList()
    }
    resp.json(r)
}

h.post('/a/admin/user/save') { req, resp, ctx ->
    req.jsonHandler { json ->
        User u = ctx.user
        Map user = Utils.getMapInKeys(json, 'username,phone,nickname,email,idCard,headimgurl,signature')
        Map existsUser = u.getByPhone(user.phone)
        String id = json.id

        if (existsUser != null && (existsUser.id + "") != id) {
            resp.json([flag: false, error: "电话号码已存在"])
            return;
        }

        if (user.nickname) {
            existsUser = u.getByNickName(user.nickname)
            if (existsUser != null && (existsUser.id + "") != id) {
                resp.json([flag: false, error: "昵称已存在"])
                return;
            }
        } else {
            user.nickname = UserService.randomNickName()
        }

        if (id) {
            u.update(id as int, user)
        } else {
            final String defaultPwd = '123456'
            user.password = defaultPwd
            user.createTime = new Date();
            id = u.add(user)
        }

        resp.json([flag: true, id: id])
    }
}

// 修改用户状态
h.get('/a/admin/user/status/:userId') { req, resp, ctx ->
    int userId = req.params.userId as int
    int status = req.params.status as int

    User u = ctx.user
    u.updateStatus(userId, status)

    resp.json([flag: true])
}

h.post('/a/admin/user/pwd') { req, resp, ctx ->
    req.jsonHandler { json ->
        int id = json.id
        String pwd = json.pwd

        User u = ctx.user
        u.updatePwd(id, pwd)

        resp.json([flag: true])
    }
}

// 修改用户角色
h.post('/a/admin/user/role/update') { req, resp, ctx ->
    req.jsonHandler { json ->
        User u = ctx.user

        String ids = json.userId
        int role = json.role
        int label = json.label

        for (id in ids.split(',')) {

            if (!(label & User.UserLabel.Normal.value)) {
                label += User.UserLabel.Normal.value
            }

            UserTree ut = UserTree.getInstance()
            label = ut.setLabelCode(label)

            u.updateRole(id as int, role)
            u.updateLabel(id as int, label)

            //更新redis中用户缓存
            UserService.delUserCookie(id as int)
        }

        resp.json([flag: true])
    }
}

// 新增一个角色或标签
h.post('/a/admin/user/role/add') { req, resp, ctx ->
    req.jsonHandler { json ->
        def d = ctx.d
        int id = json.id

        boolean isLabel = json.isLabel
        String table = isLabel ? 'tj_label' : 'tj_user_role'

        if (id != 0) {
            d.update([id: id, name: json.name, code: json.code], table)
        } else {
            id = d.add([name: json.name, code: json.code], table)
        }
        resp.json([flag: true, id: id])
    }
}

// 角色、标签列表
h.get('/a/admin/user/role/list') { req, resp, ctx ->
    Map r = [:]

    User u = ctx.user
    r.roleList = u.getAllRoleList()
    r.labelList = u.getAllLabelList()

    resp.json(r)
}

// 删除一个角色或标签
h.get('/a/admin/user/role/del/:id') { req, resp, ctx ->
    def d = ctx.d
    int id = req.params.id as int

    boolean isLabel = req.params.isLabel != null
    String table = isLabel ? 'tj_label' : 'tj_user_role'

    d.del(id, table)
    resp.json([flag: true])
}

// 角色对应的菜单修改
h.post('/a/admin/user/role/menus/update') { req, resp, ctx ->
    req.jsonHandler { json ->
        def d = ctx.d
        int id = json.id
        String menus = json.menus

        d.update([id: id, menus: menus], 'tj_user_role')
        resp.json([flag: true])
    }
}

// 批量导入，模板下载
h.get('/a/admin/user/import/excel-template') { req, resp, ctx ->
    String fileName = new String("用户批量导入模板.xls".getBytes('UTF-8'), 'ISO-8859-1')
    resp.headers.set('Content-type', 'application/vnd.ms-excel')
    resp.headers.set('pragma', 'no-cache')
    resp.headers.set('Content-Disposition', "attachment;filename=\"${fileName}\"")

    def os = new ByteArrayOutputStream()
    List headers = ['用户名', '电话', 'email', '身份证号码', '昵称', '角色', '层级']
    List samples = '张三,13800138000,test@163.com,44022219901010121x,Hi,2,7'.split(',')
//    String fields = 'username,phone,email,idCard,nickname,role,label'

    List ll = [headers, samples]
    byte[] bytes = JxlUtils.write('用户导入列表', ll)
    resp.end(bytes)
}

// 用户excel批量添加
h.post('/a/admin/user/import') { req, resp, ctx ->
    boolean flag = true
    req.uploadHandler(false) { upload ->
        InputStream is = upload.inputStream
        List cols = 'username,phone,email,idCard,nickname,role,label'.split(',')
        List ll = JxlUtils.read(is, cols)
        if (!ll) {
            l.info('upload excel parse fail')
            flag = false
        } else {
            l.info('upload excel parse ok ' + ll.size())
            for (one in ll) {
                User u = ctx.user
                final String defaultPwd = '123456'
                one.password = defaultPwd
                int id = u.add(one)
                l.info('add user ok ' + id + ' 4 ' + one.phone)
            }
        }
    }

    req.endHandler {
        Map r = [:]
        r.flag = flag
        resp.json(r)
    }
}

h.get('/a/admin/user/url/list/:pageNum') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int

    String sql = 'select * from tj_url_manager where 1 = 1 '
    List args = []

    Map p = req.params
    if (p.module) {
        sql += ' and module like ? '
        args << ("%" + p.module + "%")
    }
    if (p.srvName) {
        sql += ' and srvName like ? '
        args << ("%" + p.srvName + "%")
    }
    if (p.verFrom) {
        sql += ' and verFrom = ? '
        args << p.verFrom
    }
    if (p.verTo) {
        sql += ' and verTo = ? '
        args << p.verTo
    }

    def d = ctx.d
    def pager = d.pagi(sql, args, pageNum, 10)
    resp.ok(pager)
}

h.get('/a/admin/user/url/init') { req, resp, ctx ->
    //先清空数据库
    D d = ctx.d
    d.exe("delete from tj_url_manager where id > ?" ,[0])

    //然后插入缓存里面的数据
    List list  = SrvDescriptor.inst.list;

    String sql = "INSERT INTO tj_url_manager (module,srvName,desc_url,verFrom,verTo) VALUES (?,?,?,?,?)"
    d.db.withBatch(sql) { stmt ->
        for (one in list) {
            stmt.addBatch([one.module,one.srvName,one.desc,one.verFrom,one.verTo])
        }
    }

    //是否清空内存里面的数据  以后待定
    resp.ok()
}


h.get('/a/admin/user/change_passwd/') { req, resp, ctx ->
    def id = req.session('user').id
    def oldPasswd = req.params.oldPasswd;
    def newPasswd = req.params.newPasswd;
    if(!newPasswd || !newPasswd) {
        resp.fail("请输入密码")
        return;
    }

    D d = ctx.d;
    def user = d.queryMap("select * from tj_user where id = ? ", [id])
    if(!user) {
        resp.fail("用户不存在")
        return;
    }
    if(DigestUtils.md5Hex(oldPasswd) != user.password) {
        resp.fail("旧密码错误")
        return;
    }
    user.password = DigestUtils.md5Hex(newPasswd);
    d.update(user, "tj_user")
    //是否清空内存里面的数据  以后待定
    resp.ok()
}