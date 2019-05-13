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
 *
 */
class AdminLogService {
    private static Logger log = LoggerFactory.getLogger(AdminLogService.class);

    public static void addAdminLog(int userId, def operType, def operTarget, def changeFrom, def changeTo, def change_val, def operTime, String operDesc) {
        def map =[userId:userId, operType:operType, operTarget:operTarget, changeFrom:changeFrom, changeTo:changeTo, changeVal:change_val, operTime:operTime, operDesc:operDesc];
        D d = Context.inst.d;
        d.add(map, "admin_log")
    }

    public static void addAgentLog(def fromUserId, def fromUserName, def toUserId, def toUserName, def dealAmount, def dealType, def dealDate, def dealDesc, def agentId) {
        def map =[fromUserId:fromUserId, fromUserName:fromUserName, toUserId:toUserId, toUserName:toUserName, dealAmount:dealAmount, dealType:dealType, dealDate:dealDate, dealDesc:dealDesc, agentId:agentId];
        D d = Context.inst.d;
        d.add(map, "agent_fangka_log")
    }
}