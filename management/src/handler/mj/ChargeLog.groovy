package handler.mj

import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.slf4j.Logger

import java.text.SimpleDateFormat

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/log/charge/list/:pageNum') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    D d = ctx.d;
    def sql = "select *  "
	def where = " from user_order where 1=1 ";
    def args = []
    if(req.params.userId) {
        where += " and user_id = ?"
        args << (req.params.userId as long)
    }
    def type = req.params.type
    if("2".equals(type)) {
        where += " and product_id >= ? and product_id <= ? "
        args << 31001
        args << 31008
    }
    if("1".equals(type)) {
        where += " and product_id >= ? and product_id <= ? "
        args << 31009
        args << 31014
    }
    if(req.params.startOrderTime) {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(req.params.startOrderTime)
        where += " and mtime >= ?"
        args << date.format("yyyy-MM-dd")
    }
	if(req.params.endOrderTime) {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(req.params.endOrderTime)
        where += " and mtime <= ?"
        args << date.format("yyyy-MM-dd")
    }
	if(req.params.orderStatus){
		int status = req.params.orderStatus as int
		if(status == 1){
			where += " and order_status = 4 "
		} else {
			where += " and order_status <> 4 "
		}
	}
    sql = sql + where + " order by mtime desc"
	l.info sql
    def pager = d.pagi(sql, args, pageNum, 20)
    FieldMerger merge = ctx.fieldMerger
    merge.merge(pager.ll, "userId", "nickname as user_name", "user")
    merge.merge(pager.ll, "productId", "name as product_name", "mall_conf", "productCode")

	def totalMoney = d.queryMap("SELECT SUM(IFNULL(price,0)) as total " + where + " and order_status = 4", args).total as double;
	def totalUser = d.queryMap("SELECT IFNULL(COUNT(DISTINCT user_id),0) as total " + where + " and order_status = 4", args).total as int;
	def avgBuy = totalUser == 0? 0 : totalMoney/totalUser;

    resp.ok([pager:pager, totalMoney:totalMoney, totalUser:totalUser, avgBuy:avgBuy.round(2)])
}