package handler.mj

import com.buding.common.cluster.model.ServerModel
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.slf4j.Logger
import java.util.Calendar
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.HallPortalService
import comp.battle.HallProxy
import java.text.SimpleDateFormat

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/clust/srv/list') { req, resp, ctx ->
    D d = ctx.d;
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService srv = hallProxy.getHallService(c.zkHost)
    def list = srv.getAllServer()
	resp.ok(list);
}


h.get('/a/mj/clust/srv/start') { req, resp, ctx ->
    D d = ctx.d;
    def instanceId = req.params.instanceId
	if(!instanceId){
	   resp.fail("缺少参数容器实例id");
	   return;
	}
	def serverType = req.params.serverType;
	if(!serverType){
	   resp.fail("缺少参数服务类型");
	   return;
	}
    HallProxy hallProxy = ctx.hallProxy;
	if(serverType != 'battle'){
		HallPortalService srv = hallProxy.getHallService(c.zkHost);
		srv.startService(instanceId);
	} else {
		BattlePortalBroadcastService srv = hallProxy.getBattleService(c.zkHost);
		srv.startService(instanceId);
	}
    
	resp.ok();
}


h.get('/a/mj/clust/srv/stop') { req, resp, ctx ->
    D d = ctx.d;
    def instanceId = req.params.instanceId
	if(!instanceId){
	   resp.fail("缺少参数容器实例id");
	   return;
	}
	def serverType = req.params.serverType;
	if(!serverType){
	   resp.fail("缺少参数服务类型");
	   return;
	}
    HallProxy hallProxy = ctx.hallProxy;
	List<ServerModel> serverList = hallProxy.getHallService(c.zkHost).getAllServer(serverType)
	serverList = serverList.grep{it.instanceId == instanceId && it.serverType == serverType}
	if(!serverList) {
		resp.fail("服务没有找到")
		return;
	}
	def dubboAddr = serverList.get(0).dubboAddr
	if(serverType != 'battle'){
		HallPortalService srv = hallProxy.getDirectHallService("dubbo://${dubboAddr}/com.buding.hall.module.ws.HallPortalService")
		srv.stopService(instanceId)
	} else {
		BattlePortalBroadcastService srv = hallProxy.getDirectBattleService("dubbo://${dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService")
		srv.stopService(instanceId)
	}
	resp.ok();
}

h.get('/a/mj/clust/srv/detail') { req, resp, ctx ->
	D d = ctx.d;
	def instanceId = req.params.instanceId
	if(!instanceId){
		resp.fail("缺少参数容器实例id");
		return;
	}
	def serverType = req.params.serverType;
	if(!serverType){
		resp.fail("缺少参数服务类型");
		return;
	}
	HallProxy hallProxy = ctx.hallProxy;
	List<ServerModel> serverList = hallProxy.getHallService(c.zkHost).getAllServer(serverType)
	serverList = serverList.grep{it.instanceId == instanceId && it.serverType == serverType}
	if(!serverList) {
		resp.fail("服务没有找到")
		return;
	}
	def dubboAddr = serverList.get(0).dubboAddr
	if(serverType != 'battle'){
		HallPortalService srv = hallProxy.getDirectHallService("dubbo://${dubboAddr}/com.buding.hall.module.ws.HallPortalService")

	} else {
		BattlePortalBroadcastService srv = hallProxy.getDirectBattleService("dubbo://${dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService")
		def status = srv.getStatus();
		resp.ok(status)
		return;
	}
	resp.ok();
}

h.get('/a/mj/clust/srv/detail') { req, resp, ctx ->
	D d = ctx.d;
	def instanceId = req.params.instanceId
	if(!instanceId){
		resp.fail("缺少参数容器实例id");
		return;
	}
	def serverType = req.params.serverType;
	if(!serverType){
		resp.fail("缺少参数服务类型");
		return;
	}
	HallProxy hallProxy = ctx.hallProxy;
	List<ServerModel> serverList = hallProxy.getHallService(c.zkHost).getAllServer(serverType)
	serverList = serverList.grep{it.instanceId == instanceId && it.serverType == serverType}
	if(!serverList) {
		resp.fail("服务没有找到")
		return;
	}
	def dubboAddr = serverList.get(0).dubboAddr
	if(serverType != 'battle'){
		HallPortalService srv = hallProxy.getDirectHallService("dubbo://${dubboAddr}/com.buding.hall.module.ws.HallPortalService")

	} else {
		BattlePortalBroadcastService srv = hallProxy.getDirectBattleService("dubbo://${dubboAddr}/com.buding.hall.module.ws.BattlePortalBroadcastService")
		def status = srv.getStatus();
		resp.ok(status)
		return;
	}
	resp.ok();
}