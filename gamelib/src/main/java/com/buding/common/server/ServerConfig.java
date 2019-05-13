package com.buding.common.server;

import java.util.HashMap;
import java.util.Map;

import com.buding.common.cluster.model.ServerModel;



public class ServerConfig {
	public String instanceId;
//	
//	@Autowired
//	@Qualifier("server.info.ip")
//	public String serverIp;
//	
//	@Autowired
//	@Qualifier("server.info.port")
//	public Integer serverPort;
	
	//1 通过线程池处理 2 直接在io线程处理
	public int dispatcherMode = 1;
	
	public static Boolean enableNetpacketCompress = true;
	
	public static boolean immediateSave = true;
	
	public static boolean mainServer = false;
	
	public volatile static NodeState serverState = NodeState.STARTING;
	
	//key 为服务的类型, 比如battle, msg
	public Map<String, ServerModel> serverMap = new HashMap<String, ServerModel>();
	
	public static boolean isAllowMisIp(String ip) {
		return false;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setServerMap(Map<String, ServerModel> serverMap) {
		this.serverMap = serverMap;
	}
}
