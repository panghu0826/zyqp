package service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.guosen.webx.beans.Context
import com.guosen.webx.d.D
import com.guosen.webx.web.HttpServerRequest
import com.guosen.webx.web.HttpServerResponse
import comp.Constants
import comp.common.RedisCacher
import comp.user.User
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import utils.Utils

/**
 * Created by jaime on 2016/6/13.
 */
class UserService {
    private static Logger log = LoggerFactory.getLogger(UserService.class);
    private static String aesKey = "test1234test1234";
    private static ShieldSessionPrefix = "ShieldSession_";
    private static String UserCookie = 'UserCookie_'

    private static RANDOM_NAME_CONF = [
            [name: "HK", len: 7],
            [name: "SZ", len: 7],
            [name: "SH", len: 7],
            [name: "DJI", len: 6],
            [name: "NASDAQ", len: 3],
            [name: "DAX", len: 6],
            [name: "CAC", len: 6],
            [name: "SMI", len: 6],
            [name: "FRK", len: 6],
            [name: "JP", len: 7],
    ]

    public static String randomUniqNickName(String phone) {
        for (int i = 0; i <= 10; i++) {
            def nickname = randomNickName()
            def user = Context.inst.d.queryMap("select * from tj_user where nickname = ?", [nickname])
            if (!user) {
                return nickname
            }
        }

        def tmp = phone.substring(8)
        tmp = tmp.padLeft(7, '*')
        tmp = phone.substring(0, 3) + tmp
        return tmp
    }

    public static String randomNickName() {
        Random r = new Random()
        int ind = r.nextInt(RANDOM_NAME_CONF.size())
        def conf = RANDOM_NAME_CONF[ind]
        String name = "";
        int len = conf.len
        (1..len).each {
            name += r.nextInt(9)
        }
        name = name + "." + conf.name
    }

/**
 * 将用户信息写入cookie
 * @param user
 * @param httpRsp
 */
    public static void addUserInfoCookieYc(Map user, HttpServerResponse httpRsp) {
        //添加了用户的 UUID和手机号码到本地cookie by caodx
        def tmp = [id: user.id, uuid: user.uuid, jjrdm:user.jjrdm,phone: user.mobile, role: user.role, label: user.label, riskBearing: user.riskBearing, time: System.currentTimeMillis()]

        def val = new Gson().toJson(tmp)
        val = Base64.encodeBase64String(Utils.aesEncrypt(val, aesKey))
        def key = "login_user"
        httpRsp.addCookie([name: key, value: val, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])

        try {
            //刷新客户端用的明文cookie
            def uinfoKey = "uinfo"
            if (user) {
                def uinfo = new Gson().toJson([userId: user.id])
                httpRsp.addCookie([name: uinfoKey, value: uinfo, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])
            }
        } catch (Exception e) {
            log.error("addUserInfoCookie", e)
        }
    }

    /**
     * H5微信审批入口登录cookie
     * @param user
     * @param httpRsp
     */
    public static void addUserInfoCookieYcH5(Map user, HttpServerResponse httpRsp) {
        //添加了用户的 UUID和手机号码到本地cookie by caodx
        def tmp = [id:user.uuid,uuid: user.uuid, username : user.usename, time: System.currentTimeMillis()]
        try {
            def val = new Gson().toJson(tmp)
            val = Base64.encodeBase64String(Utils.aesEncrypt(val, aesKey))
            def key = "H5_login_user"
            httpRsp.addCookie([name: key, value: val, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])
        } catch (Exception e) {
            log.error("addUserInfoCookie", e)
        }
    }



    public static void addUserInfoCookie(Map user, HttpServerResponse httpRsp) {
        //添加了用户的 UUID和手机号码到本地cookie by caodx
        def tmp = [id: user.id, uuid: user.uuid, jjrdm:user.jjrdm,phone: user.mobile, role: user.role, label: user.label, riskBearing: user.riskBearing, time: System.currentTimeMillis()]

        def val = new Gson().toJson(tmp)
        val = Base64.encodeBase64String(Utils.aesEncrypt(val, aesKey))
        def key = "login_user"
        httpRsp.addCookie([name: key, value: val, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])

        try {
            //刷新客户端用的明文cookie
            def uinfoKey = "uinfo"
            if (user) {
                def one = Context.inst.d.queryMap("select * from tj_user where id = ? ", [user.id])
                boolean isCustomer = (one.label & User.UserLabel.Customer.value) ? true : false
                def uinfo = new Gson().toJson([userId: user.id, isCustomer: isCustomer])
                httpRsp.addCookie([name: uinfoKey, value: uinfo, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])
            }
        } catch (Exception e) {
            log.error("addUserInfoCookie", e)
        }
    }

    /**
     * 写入临时客户cookie,登陆时绑定到具体用户身上
     * @param user
     * @param httpRsp
     */
    public static void addTempCustomerCookie(HttpServerResponse httpRsp) {
        def tmp = [time: System.currentTimeMillis()]
        def val = new Gson().toJson(tmp)
        val = Base64.encodeBase64String(Utils.aesEncrypt(val, aesKey))
        def key = "tmp_customer"
        httpRsp.addCookie([name: key, value: val, path: '/', maxAge: String.valueOf(Constants.SESSION_USER_TIMEOUT)])
    }

    /**
     * 处理临时客户绑定逻辑
     * @param httpRsp
     */
    public static void processTempCustomerBinding(HttpServerRequest req, HttpServerResponse resp) {
        def key = "tmp_customer"
        String val = req.getCookie(key)
        if (!val) {
            return;
        }
        resp.addCookie([name: key, value: null, path: '/', maxAge: String.valueOf(0)]) //cookie失效

        String cookieVal = new String(Utils.aesDecrypt(Base64.decodeBase64(val), aesKey) ?: '', 'utf-8')
        if (cookieVal) {
            JSONObject json = JSONObject.parse(cookieVal)
            long time = Long.valueOf(json.getString("time"))
            if (System.currentTimeMillis() - time >= 6 * 3600 * 1000) {//TODO
                return
            }

            def user = req.getLocal("login_user")
            def isCustomer = ((user.label & User.UserLabel.Customer.value) == User.UserLabel.Customer.value)
            if (!user.openAccountTime && !isCustomer) {//未开户
                user.label = (user.label | User.UserLabel.Customer.value)
                Context.inst.d.update([id: user.id, openAccountTime: new Date(), label: user.label], "tj_user", "id")
            }
        }
    }

/**
 * 资金账号验证成功后写入cookie
 * @param cashAccount
 * @param httpRsp
 */
    public static void addCookieAfterCheckCashAccount(String cashAccount, HttpServerResponse resp) {
        def tmp = [time: System.currentTimeMillis(), account: cashAccount]
        def val = new Gson().toJson(tmp)
        val = Base64.encodeBase64String(Utils.aesEncrypt(val, aesKey))
        def key = "bindedInfo"
        resp.addCookie([name: key, value: val, path: '/', maxAge: String.valueOf(24 * 3600)])
    }

/**
 * 将用户更新成客户
 * @param user
 */
    public static void updateUserAsCustomer(Map user) {
        int label = user.label as int
        user.label = label | User.UserLabel.Customer.value
        Context.inst.d.update([id: user.id, label: user.label, cashAccount: user.cashAccount], "tj_user", "id")
        UserService.delUserCookie(user.id as long)
    }

/**
 * 登陆成功后,处理绑定资金账号逻辑
 * @param cashAccount
 * @param req
 * @param resp
 */
    public static void processCashBindAfterLogin(String cashAccount, HttpServerResponse resp, HttpServerRequest req) {
        if (!cashAccount) {
            return;
        }

        def key = "bindedInfo"
        def sql = "select * from tj_customer where cash = ?"
        def map = Context.inst.d.queryMap(sql, [cashAccount])
        if (map) {
            def user = req.getLocal("login_user")
            user.cashAccount = cashAccount
            updateUserAsCustomer(user)
            Map tmp = req.getLocal("apiCallParam")
            if (tmp) tmp.put("bindCashAccount", cashAccount)
        }
        //删除cookie
        resp.addCookie([name: key, value: null, path: '/', maxAge: String.valueOf(0)]) //cookie失效
    }

    /**
     * 从http request获取资金账号
     * @param req
     * @param c
     * @return
     */
    public static String getCashAccountFromCookie(HttpServerRequest req, Properties c) {
        def key = "bindedInfo"
        String val = req.getCookie(key)
        if (!val) {
            return null;
        }
        String aesKey = c.getProperty('cookie-aes-key') ?: 'test1234test1234'
        String cookieVal = new String(Utils.aesDecrypt(Base64.decodeBase64(val), aesKey) ?: '', 'utf-8')
        if (cookieVal) {
            JSONObject json = JSONObject.parse(cookieVal)
            String cashAccount = json.getString("account")
            return cashAccount;
        }
        return null;
    }

    /**
     * 从http request获取openid
     * @param req
     * @param c
     * @return
     */
    public static String getOpenidFromCookie(HttpServerRequest req, Properties c) {
        def key = "user-login-openid"
        String val = req.getCookie(key)
        if (!val) {
            return null;
        }
        return val
    }

    /**
     * 更新用户的客户权限(如果资金账号不存在,则回收客户权限)
     * @param user
     */
    public static void checkInvalidCashAccount(Map user) {
        if (!user.cashAccount) {
            return
        }

        def sql = "select * from tj_customer where cash = ? "
        def map = Context.inst.d.queryMap(sql, [user.cashAccount])
        if (!map) {
            user.cashAccount = null;
            user.label = (user.label | User.UserLabel.Customer.value) - User.UserLabel.Customer.value
            Context.inst.d.update([id: user.id, cashAccount: null, label: user.label], "tj_user", "id")
        }
    }

    public static Map getUserByUuid(String uuid) {
        Map user = Context.inst.d.queryMap("SELECT * FROM tj_user where uuid = ? ", [uuid])
        return user;
    }

    public static Map addUser(String phone, int role = 0) {
        def user = [username: phone, password: '', phone: phone, status: Constants.USER_STATUS_VALID, role: role, label: User.UserLabel.Normal.value, grade: 0, riskBearing: 0, createTime: new Date(), updateTime: new Date()]
        user.id = Context.inst.user.add(user)
        //更新昵称(唯一)
        //user.nickname = "u" + new SimpleDateFormat("yyyyMMdd").format(new Date())+""+user.id
        user.nickname = UserService.randomUniqNickName(phone)
        Context.inst.d.update([id: user.id, nickname: user.nickname], "tj_user", "id")
        return user
    }

    /**
     * 添加默认关注人
     * @param userId
     * @return
     */
    public static void addDefaultFocus(long userId) {
        D d = Context.inst.d;
        def userList = d.query('select user_id from tj_default_following')
        userList.each {
            def followRec = d.queryMap("select * from tj_following where follower_id = ? and followed_id = ? ", [userId, it.userId])
            if(followRec) {
                if(followRec.statue == Constants.STATUS_NO) {
                    d.update([id:followRec.id, status: Constants.STATUS_YES], "tj_following", "id")
                }
            } else {
                d.add([followerId:userId, followedId:it.userId,status:Constants.STATUS_YES, updateTime: new Date()], "tj_following")
            }
        }
    }

    /**
     * 检查会话
     */
    public static boolean checkSessionKickout(long uid, HttpServerResponse resp) {
        RedisCacher cacher = Context.inst.redisCacher
        boolean ret = false;
        cacher.doSth {Jedis jedis->
            ret = jedis.exists(ShieldSessionPrefix + uid);
        }
        if(ret) {
            //剔除会话
            resp.addCookie([name: "login_user", value: null, path: '/', maxAge: String.valueOf(0)])
            resp.addCookie([name: "uinfo", value: null, path: '/', maxAge: String.valueOf(0)])
        }
        return ret;
    }

    /**
     * 剔除会话
     * @param uid
     * @param time
     * @return
     */
    public boolean kickoutSession(long uid, long time) {
        RedisCacher cacher = Context.inst.redisCacher
        cacher.doSth {Jedis jedis->
            jedis.setex(ShieldSessionPrefix + uid, time, "true")
        }
    }

    /*
    *获取用户信息
    *
    * */
    public static Map getUserCookie(long uid) {
        RedisCacher cacher = Context.inst.redisCacher
        Map one
        String key = UserCookie + uid
        cacher.doSth {Jedis jedis->
            /*if(jedis.exists(UserCookie + uid)){
                String oneStr = jedis.get(UserCookie + uid)
                one = JSON.parse(oneStr)
            }else {
                one = Utils.getMapInKeys(Context.inst.user.one(uid),"id,role,label,riskBearing")
                jedis
            }*/
            String oneStr = jedis.get(key)
            if(oneStr){
                one = JSON.parse(oneStr)
            }else {
                one = Utils.getMapInKeys(Context.inst.user.one(uid as int),"id,role,label,riskBearing")
                jedis.setex(key,172800,JSON.toJSONString(one))//有效期为两天
            }
        }
        one;
    }
    /*
    * DEL
    * */
    public static void delUserCookie(long uid) {
        RedisCacher cacher = Context.inst.redisCacher

        String key = UserCookie + uid
        cacher.doSth {Jedis jedis->
            jedis.del(key)
        }
    }

    /**
     * 获取客户的真实姓名
     * @param custid
     * @return
     */
    public static String getCustomerName(String custid) {
        if(!custid) return " ";
        D d = Context.inst.d;
        def map = d.queryMap("select * from tj_customer where cash = ? ", [custid])
        if(map && map.custname) {
            return map.custname;
        }
        return '***'
    }

    public static updateCashAccount(def cust) {
        D d  = Context.inst.d;
        def id = cust.id
        def mobileno = cust.mobileno
        if(!mobileno) {
            mobileno = DigestUtils.md5Hex('guosen1!' + cust.phone)
        }
        def cash = cust.cashAccount;
        def mutiCash = cust.mutiCashAccount
        def label = cust.label
        if(!label) {
            label = 0;
        }
        label = (label | User.UserLabel.Customer.value)
        def cashList = d.query("select cash from tj_customer where mobileno = ? ", [mobileno])
        def allJoins = cashList.collect {
            it.cash
        }.join(",").trim()
        if(!cash && cashList) {
            d.update([id:id, label: label, cashAccount:cashList[0].cash, mobileno:mobileno], "tj_user", "id")
            cust.label = label
            cust.cashAccount = cashList[0].cash
        }
        if(allJoins.length() > 10) {
            d.update([id:id, label: label, mutiCashAccount:allJoins, mobileno:mobileno], "tj_user", "id")
            cust.label = label
            cust.mutiCashAccount = allJoins
        }
    }
}