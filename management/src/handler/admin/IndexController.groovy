package handler.admin

import com.google.gson.Gson
import com.guosen.webx.web.ChainHandler
import com.guosen.webx.web.HttpServerRequest
import comp.user.User
import comp.user.User.UserRole
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import service.RoleMapping
import service.UserService
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

// 运营后台的登录状态过滤
h.filter('m-login-filter', h.PRE, /^\/a\/.*$/, 2) { req, resp, ctx ->
    String uri = req.path
    if(uri.startsWith("/a/agent/")){
        return false;
    }
	if(uri.startsWith("/a/m/")){
        return false;
    }
    if(uri.startsWith("/a/pay/")){
        return false;
    }
    if(uri.startsWith("/a/ali/")){
        return false;
    }
    if(uri.startsWith("/a/wx/")){
        return false;
    }

    // must login
    def user = req.session('user')
    if (!user) {
        l.info('user not login 4 ' + uri)
        resp.json([needLogin: true])
        return true
    } else {
        req.setLocal('user', user)

        // reset cookie again, keep 1hour max age
        String cookieVal = req.getCookie()

        Map cookie = [:]
        cookie.name = HttpServerRequest.SESSION_ID
        cookie.value = cookieVal
        cookie.path = '/'
        cookie.secure = false

        // default 1 hour, need a pre filter to refresh cookie max age
        String cookieMaxAge = c.cookieMaxAge ?: ('' + 60 * 60 *60)
        cookie.maxAge = cookieMaxAge
        resp.addCookie(cookie)
    }

    false
}

h.post('/a/m/login') { req, resp, ctx ->
    String phone = req.params.phone
    String pwd = req.params.pwd

    l.info('user login - ' + phone + ' - ' + DigestUtils.md5Hex(pwd))

    User user = ctx.user
    int id = user.check(phone, pwd)
    if (id == -1) {
        resp.render('/admin/login.html', [error: 'invalid'])
        return
    }

    Map u = user.one(id, true, true)
    u.id = id;//TODO 上一句有bug导致id被覆盖
    int role = u.role ?: 0
//    if (!User.isRole(role, UserRole.Manager)) {
//        log.info('user login as manager fail - ' + role)
//        resp.render('/admin/login.html', [error: 'invalid'])
//        return
//    }

    // cookie也放冗余的用户信息
    String aesKey = c.getProperty('cookie-aes-key') ?: 'test1234test1234'
    String cookieValRaw = '' + u.id + '_' + u.role + '_' + u.label + '_' + System.currentTimeMillis()
    String cookieVal = Base64.encodeBase64String(Utils.aesEncrypt(cookieValRaw, aesKey))
    l.info("login user is : " + new Gson().toJson(u))
    req.session('user', u, cookieVal)

    //add by vince, 后台登陆同时前台也登陆
    UserService.addUserInfoCookie(u, resp)

    resp.redirect('/admin/index.html')
}

h.get('/a/m/logout') { req, resp, ctx ->
    req.clearSession()

    if (req.isAjax()) {
        resp.end ''
    } else {
        resp.render('/admin/login.html', [:])
    }
}

// 登录后的页面获取用户信息，权限等
RoleMapping.inst.role = 'manager'
h.get('/a/admin/user/get-login-user') { req, resp, ctx ->
    Map u = req.getLocal('user')
    int role = u.role

    def user = ctx.user
    List menus = user.getMenusByRole(role)
    resp.json([user: u, menus: menus])
}

h.get('/a/admin/user/get-role-label-list') { req, resp, ctx ->
    Map r = [:]

    User u = ctx.user
    r.roleList = u.getAllRoleList()
    r.labelList = u.getAllLabelList()
    resp.json(r)
}