package handler.mj

import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.MsgPortalService
import com.google.gson.Gson
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/match/list') { req, resp, ctx ->
    def roomType = req.params.roomType
    D d = ctx.d;
    def list = d.query("select * from room_conf where status = 1 and room_type = ? ", [roomType])
    resp.ok(list)
}

h.post('/a/mj/match/save') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,roomId,roomName,roomType," +
                "matchId,juNum,baseScore,minCoinLimit,maxCoinLimit,icon,matchClassFullName," +
                "gameParamOperTimeOut,gameParamAutoOperWhenTimeout,gameParamThinkMillsWhenAutoOper," +
                "gameParamChiPengPlayMills,gameParamChuPlayMills,seatSizeLower,seatSizeUpper,autoStartGame," +
                "autoChangeDesk,gameClassFullName,deskClassFullName,roomClassFullName,supportRobot,secondsAddFirstRobot," +
                "addRobotRate,autoReady,secondsBeforKickout,fee,srvFee," +
                "eightJuDiamond,sixteenJuDiamond,twentyfourJuDiamond,twoQuanDiamond,fourQuanDiamond," +
                "guo100Diamond,guo200Diamond,guo300Diamond")
        map.status = 1;

        def dbModel = d.queryMap("select * from room_conf where room_id = ? ", [map.roomId])
        if(dbModel) {
            map.id = dbModel.id;
            d.update(map, "room_conf")
        } else {
            map.id = d.add(map, "room_conf")
        }
        resp.ok()
    }
}
//发布
h.get('/a/mj/match/publish') { req, resp, ctx ->
    D d = ctx.d;
    def list = d.query("select * from room_conf where status = 1")
    list.each {
        def fee = """
            [
                {
                    "currenceType":'2',
                    "currenceCount":20,
                    "gameCount":1
                }
            ]
        """
        if(it.roomType == 'VIP') {
            List li = []
            if(it.eightJuDiamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = 8
                m["diamondCount"] = it.eightJuDiamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.sixteenJuDiamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = 16
                m["diamondCount"] = it.sixteenJuDiamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.twentyfourJuDiamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = 24
                m["diamondCount"] = it.twentyfourJuDiamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.twoQuanDiamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = 2
                m["diamondCount"] = it.twoQuanDiamond

//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.fourQuanDiamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = 4
                m["diamondCount"] = it.fourQuanDiamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.guo100Diamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = -1
                m["diamondCount"] = it.guo100Diamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.guo200Diamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = -2
                m["diamondCount"] = it.guo200Diamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            if(it.guo300Diamond){
                Map m = [:]
                m["itemId"] = 'A001'
                m["quanCount"] = -3
                m["diamondCount"] = it.guo300Diamond
//                m["itemCount"] = 4
//                m["gameCount"] = 24
                li << m
            }
            def json = new Gson().toJson(li)
            fee = """${json}"""
           /* fee = """
            [
                {
                    "itemId":'A001',
                    "itemCount":4,
                    "gameCount":${it.gameCountLow}
                },
                {
                    "itemId":'A001',
                    "itemCount":8,
                    "gameCount":${it.gameCountHigh}
                }
            ]
        """;*/
        }
        String matchId = it.matchId
        String[] st = matchId.split ("_")
        String gameId = st[0]+"_"+st[1]
        it.confJson =
                """
                    {
                        "comment": "${st[1]}${it.roomName}",
                        "matchID": "${it.matchId}",
                        "gameID": "${gameId}",
                        "juNum": ${it.juNum},
                        "matchClassFullName":"${it.matchClassFullName}",
                        "conditionInfo": {
                            "comment": "这是房间信息,roomArray有不同底分来区分房间, enterCondition是入场条件,deskConf是桌子人数和自动开赛的设置",
                            "version": 1,
                            "enterCondition": {
                                "minCoinLimit": ${it.minCoinLimit},
                                "maxCoinLimit": ${it.maxCoinLimit},
                                "currencyType":2
                            },
                            "deskConf": {
                                "allowExitWhenGaming":${it.allowExitWhenGaming},
                                "awayIsGaming":${it.awayIsExit},
                                "seatSizeLower": ${it.seatSizeLower},
                                "seatSizeUpper": ${it.seatSizeLower},
                                "autoStartGame": ${it.autoStartGame},
                                "autoChangeDesk": ${it.autoChangeDesk},
                                "gameClassFullName": "${it.gameClassFullName}",
                                "deskClassFullName": "${it.deskClassFullName}",
                                "supportRobot":${it.supportRobot},
                                "secondsAddFirstRobot": ${it.secondsAddFirstRobot},
                                "addRobotRate": ${it.addRobotRate},
                                "autoReady":${it.autoReady},
                                "secondsBeforKickout":${it.secondsBeforKickout},
                                "gameParam":"{'autoOperWhenTimeout':${it.gameParamAutoOperWhenTimeout}, 'chiPengPlayMills':${it.gameParamChiPengPlayMills},chuPlayMills:${it.gameParamChuPlayMills},operTimeOutSeconds:${it.gameParamOperTimeOut},thinkMills4AutoOper:${it.gameParamThinkMillsWhenAutoOper}}"
                            },
                            "roomArray": [
                                {
                                    "comment": "roomName用于打印区分,basepoint为底分,roomId是房间配置id必须全局唯一, 这是是否需要添加人数限制,settleCurrenceType是结算货币,feeCurrenType是收费货币",
                                    "roomName": "${it.roomName}",
                                    "roomId": "${it.roomId}",
                                    "basePoint": ${it.baseScore},
                                    "low": ${it.minCoinLimit},
                                    "high": ${it.maxCoinLimit},
                                    "fee": ${fee},
                                    "currencyType":2,
                                    "roomClassFullName":"${it.roomClassFullName}",
                                    "roomType":${it.roomType}
                                }
                            ]
                        }
                    }
                """ as String

        d.update(it, "room_conf");
    }
        try{
            HallProxy proxy = ctx.hallProxy
            proxy.getBattleService(c.zkHost).reloadMatchConf("all");
            resp.ok()
        }catch (Exception e){
            e.printStackTrace()
        }

}