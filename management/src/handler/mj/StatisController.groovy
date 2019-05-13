package handler.mj


import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties cc = (Properties) config

h.get('/a/mj/statis/playcount') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "SELECT COUNT(id) AS play_count, DATE_FORMAT(game_start_time, '%Y-%m-%d') AS game_day FROM game_log" +
			" where match_id in ('G_DQMJ_MATCH_2VIP','G_DQMJ_MATCH_4VIP','G_DQMJ_MATCH_3VIP','G_SHMJ_MATCH_4VIP','G_SHMJ_MATCH_3VIP','G_SHMJ_MATCH_2VIP','G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP')"+
			" GROUP BY DATE_FORMAT(game_start_time, '%Y-%m-%d') ORDER BY id DESC LIMIT 30"

    def args = []
    def list = d.query(sql, args)
    resp.ok(list)
}



h.get('/a/mj/statis/playerscorecount') { req, resp, ctx ->
	D d = ctx.d;
	def sql = "SELECT player_id, player_name ,sum(num) as num  from t_fuli_count where player_id !=20 "

	def startTime = req.params.startTime
	def endTime = req.params.endTime
	def name = req.params.name
	if (!startTime && !endTime) return
	def args = []
    if (name) {
        sql += " AND player_id = ? "
        args << name
    }

	if (startTime) {
		sql += " AND count_date >= ? "
		args << startTime
	}
	if (endTime) {
		sql += " AND count_date <= ? "
		args << endTime
	}

    sql += " group by player_id"

	List<Map<String, Object>> list = d.query(sql, args)
	resp.ok([list:list])

}


h.get('/a/mj/statis/diamondcount') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "SELECT sum(diamond_num) AS diamond_count, DATE_FORMAT(ctime, '%Y-%m-%d') AS game_day FROM user_consume_diamond where 1=1"

	def startTime = req.params.startTime
	def endTime = req.params.endTime
	def matchId = req.params.matchId

	def type = req.params.type as int
	def args = []

	if(startTime){
		sql += " and ctime >= ? "
		args << startTime
	}
	if(endTime){
		sql += " and ctime <= ? "
		args << endTime
	}


	if(type == 1){
		if(matchId) {
			sql += " and match_id = ? "
			String match = StringUtils.equals("2",matchId)?"G_DQMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_DQMJ_MATCH_3VIP":"G_DQMJ_MATCH_4VIP")
			args << match
		}else {
			sql += " and match_id in ('G_DQMJ_MATCH_2VIP','G_DQMJ_MATCH_3VIP','G_DQMJ_MATCH_4VIP') "
		}
	}else if(type == 2){
		if(matchId) {
			sql += " and match_id = ? "
			String match = StringUtils.equals("2",matchId)?"G_SHMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_SHMJ_MATCH_3VIP":"G_SHMJ_MATCH_4VIP")
			args << match
		}else {
			sql += " and match_id in ('G_SHMJ_MATCH_4VIP','G_SHMJ_MATCH_3VIP','G_SHMJ_MATCH_2VIP') "
		}
	}else if(type == 3){
		if(matchId) {
			sql += " and match_id = ? "
			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
			args << match
		}else {
			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
		}
	}else if(type == 4){
		if(matchId) {
			sql += " and match_id = ? "
			String match = StringUtils.equals("2",matchId)?"G_SYMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_SYMJ_MATCH_3VIP":"G_SYMJ_MATCH_4VIP")
			args << match
		}else {
			sql += " and match_id in ('G_SYMJ_MATCH_4VIP','G_SYMJ_MATCH_3VIP','G_SYMJ_MATCH_2VIP') "
		}
	}else if(type == 5){
//		if(matchId) {
		sql += " and match_id in ('G_DDZ_MATCH_3VIP') "
//			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
//			args << match
//		}else {
//			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
//		}
	}else if(type == 6){
//		if(matchId) {
		sql += " and match_id in ('G_ZJH_MATCH_5VIP','G_ZJH_MATCH_8VIP') "
//			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
//			args << match
//		}else {
//			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
//		}
	}else if(type == 7){
//		if(matchId) {
		sql += " and match_id in ('G_JACK_MATCH_5VIP','G_JACK_MATCH_8VIP') "
//			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
//			args << match
//		}else {
//			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
//		}
	}else if(type == 8){
//		if(matchId) {
		sql += " and match_id in ('G_NN_MATCH_10VIP') "
//			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
//			args << match
//		}else {
//			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
//		}
	}else if(type == 9){
//		if(matchId) {
		sql += " and match_id in ('G_ErBa_MATCH_10VIP') "
//			String match = StringUtils.equals("2",matchId)?"G_QSMJ_MATCH_2VIP":(StringUtils.equals("3",matchId)?"G_QSMJ_MATCH_3VIP":"G_QSMJ_MATCH_4VIP")
//			args << match
//		}else {
//			sql += " and match_id in ('G_QSMJ_MATCH_4VIP','G_QSMJ_MATCH_3VIP','G_QSMJ_MATCH_2VIP') "
//		}
	}
	def allNum = d.queryMap(sql,args).diamondCount
	Map<String, Object> m = [:]
	if(!allNum){
		allNum = 0
	}
	sql +=" GROUP BY DATE_FORMAT(ctime, '%Y-%m-%d') ORDER BY id DESC LIMIT 30"
    def list = d.query(sql, args)
//	list.add(m)
    resp.ok([list:list,allNum:allNum])
}


h.get('/a/mj/statis/playfangkacount') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "SELECT COUNT(id) AS play_count, DATE_FORMAT(end_time, '%Y-%m-%d') AS game_day FROM user_room_result GROUP BY DATE_FORMAT(end_time, '%Y-%m-%d') ORDER BY id DESC LIMIT 30 "
    def args = []
    def list = d.query(sql, args)
    resp.ok(list)
}

h.get('/a/mj/statis/daycount') { req, resp, ctx ->
    D d = ctx.d;
	//今日
	def today = new Date().format('YYYY-MM-dd')
	Calendar c= Calendar.getInstance();
	c.add(Calendar.DAY_OF_YEAR, -1);
	//昨日
	def yesterday = c.getTime().format('YYYY-MM-dd')

    c= Calendar.getInstance();
	c.add(Calendar.DAY_OF_YEAR, -2);
	//三日前
	def threeDay = c.getTime().format('YYYY-MM-dd')

	c= Calendar.getInstance();
	c.add(Calendar.DAY_OF_YEAR, -6);
	//七日前
	def sevenDay = c.getTime().format('YYYY-MM-dd')

	l.info "$today,$threeDay,$sevenDay"

    def sql = "SELECT IFNULL(count(*), 0) as c from user_order where DATE_FORMAT(ctime, '%Y-%m-%d')  =  ? AND order_status = 4"
    def payUserCount = d.queryMap(sql, [today]).c as int; //今日付费玩家数
	sql = "SELECT IFNULL(sum(price), 0) as c from user_order where DATE_FORMAT(ctime, '%Y-%m-%d')  =  ? AND order_status = 4"
	def payMoney = d.queryMap(sql, [today]).c as double; // 今日付费总额
	def arpu = payUserCount == 0 ? 0 : (payMoney/payUserCount).round(2); //arpu
	def totalUser = d.queryMap("select count(*) as c from user").c as int;

	def registerCount = d.queryMap("select count(*) as c from user where DATE_FORMAT(ctime, '%Y-%m-%d')  =  ? and binded_match is null", [today]).c as int;
	def loginCount = d.queryMap("select count(*) as c from user where DATE_FORMAT(last_login, '%Y-%m-%d')  =  ?", [today]).c as int;

	//次日留存
	def oneDayNewCount = d.queryMap("select count(*) as c from user where DATE_FORMAT(ctime, '%Y-%m-%d') = ? ", [yesterday]).c as int;
	def oneDayNewSurvive = d.queryMap("select count(*) as c from user where DATE_FORMAT(last_login, '%Y-%m-%d')  =  ? and DATE_FORMAT(ctime, '%Y-%m-%d') = ?", [today, yesterday]).c as int;
	def oneDaySurviveRate = oneDayNewCount == 0 ? 0 : (oneDayNewSurvive*100.0)/oneDayNewCount;

	//三日留存
	def threeDayNewCount = d.queryMap("select count(*) as c from user where DATE_FORMAT(ctime, '%Y-%m-%d') = ? ", [yesterday]).c as int;
	def threeDayNewSurvive = d.queryMap("select count(*) as c from user where DATE_FORMAT(last_login, '%Y-%m-%d')  =  ? and DATE_FORMAT(ctime, '%Y-%m-%d') = ? ", [today, threeDay]).c as int;
	def threeDaySurviveRate = threeDayNewCount == 0 ? 0 : (threeDayNewSurvive*100.0)/threeDayNewCount;

	//7日留存
	def sevenDayNewCount = d.queryMap("select count(*) as c from user where DATE_FORMAT(ctime, '%Y-%m-%d') = ? ", [yesterday]).c as int;
	def sevenDayNewSurvive = d.queryMap("select count(*) as c from user where DATE_FORMAT(last_login, '%Y-%m-%d')  =  ? and DATE_FORMAT(ctime, '%Y-%m-%d') = ? ", [today, sevenDay]).c as int;
	def sevenDaySurviveRate = sevenDayNewCount == 0 ? 0 : (sevenDayNewSurvive*100.0)/sevenDayNewCount;

	//在线时长
	def totalMin = d.queryMap("select ifnull(sum(online_minutes), 0) as c from user_day_report where day = ? ", [new Date().format("yyyyMMdd")]).c as int
	def userCount = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? ", [new Date().format("yyyyMMdd")]).c as int
	def avgOnlineMinute = userCount == 0? 0 : totalMin/userCount
	def userCountLess5Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes <= 5",[new Date().format("yyyyMMdd")]).c as int;
	def userCountLess30Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes > 5 and online_minutes <= 30",[new Date().format("yyyyMMdd")]).c as int;
	def userCountLess60Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes > 30 and online_minutes <= 60",[new Date().format("yyyyMMdd")]).c as int;
	def userCountLess120Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes > 60 and online_minutes <= 120",[new Date().format("yyyyMMdd")]).c as int;
	def userCountLess300Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes > 120 and online_minutes <= 300",[new Date().format("yyyyMMdd")]).c as int;
	def userCountMoreThan300Min = d.queryMap("select ifnull(count(id), 0) as c from user_day_report where day = ? and online_minutes > 300 ",[new Date().format("yyyyMMdd")]).c as int;

	//今日道具出售情况
	def itemBuyList = d.query("SELECT product_id, SUM(price) AS total_money, COUNT(id) AS total_count FROM user_order where DATE_FORMAT(ctime, '%Y-%m-%d') = ? and order_status = 4 GROUP BY product_id", [today])
	FieldMerger merge = ctx.fieldMerger
    merge.merge(itemBuyList, "productId", "name as product_name,price", "mall_conf", "productCode")

	def payData = [payUserCount:payUserCount, payMoney: payMoney, arpu:arpu];
	def surviveData = [registerCount:registerCount, loginCount:loginCount, oneDayNewCount:oneDayNewCount, oneDayNewSurvive:oneDayNewSurvive, oneDaySurviveRate:oneDaySurviveRate,
		threeDayNewCount:threeDayNewCount, threeDayNewSurvive:threeDayNewSurvive, threeDaySurviveRate:threeDaySurviveRate, sevenDayNewCount:sevenDayNewCount,
		sevenDayNewSurvive:sevenDayNewSurvive, sevenDaySurviveRate:sevenDaySurviveRate]
	def onlineData = [avgOnlineMinute:avgOnlineMinute, userCountLess5Min:userCountLess5Min, userCountLess30Min:userCountLess30Min,
					  userCountLess60Min:userCountLess60Min, userCountLess120Min:userCountLess120Min, userCountLess300Min:userCountLess300Min, userCountMoreThan300Min:userCountMoreThan300Min]
    resp.ok([payData:payData, surviveData:surviveData, itemBuyList:itemBuyList, onlineData:onlineData])
}