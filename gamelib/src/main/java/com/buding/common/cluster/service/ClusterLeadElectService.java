package com.buding.common.cluster.service;

import org.I0Itec.zkclient.IZkDataListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;

import com.buding.common.server.ServerConfig;
import com.buding.common.zk.client.ZkClientBase;
import com.buding.common.zk.serializer.ZkStringSerializer;

/**
 * @author jaime qq_1094086610
 * @Description: 选举master(比如有多个战斗服组成的集群，只能有一个服作为主服务器，通过zk实现选举)
 * 
 */
public class ClusterLeadElectService extends ZkClientBase implements InitializingBean,IZkDataListener {
	private Logger logger = LogManager.getLogger(getClass());

	public String leadType;
	public String serverInstance;

	@Override
	public void handleDataChange(String dataPath, Object data) throws Exception {
		logger.info(dataPath + " has set val: " + data + ", curServer:" + serverInstance);
		if(serverInstance.equals(data)) {
			logger.info(serverInstance + " is mainServer-----");
			ServerConfig.mainServer = true;
		} else {
			ServerConfig.mainServer = false;
		}
	}

	@Override
	public void handleDataDeleted(String dataPath) throws Exception {
		tryAsLead();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initZkClient(new ZkStringSerializer());

		subscribeNodeChange();
		
		tryAsLead();
	}

	private void subscribeNodeChange() {
		zkClient.subscribeDataChanges("/lead/" + leadType, this);
	}

	private void tryAsLead() {
		ServerConfig.mainServer = false;
		try {
			if(zkClient.exists("/lead") == false) {
				zkClient.createPersistent("/lead");	
			}
			zkClient.createEphemeral("/lead/" + leadType, serverInstance);
			logger.info("create node : " + leadType);
		} catch (Exception e) {
			logger.error("", e);
		}
		
		if(zkClient.exists("/lead/" + leadType) && serverInstance.equals(zkClient.readData("/lead/" + leadType))) {
			ServerConfig.mainServer = true;
			logger.info(serverInstance + " is mainServer-----");
		}
	}

	public void setLeadType(String leadType) {
		this.leadType = leadType;
	}

	public void setServerInstance(String serverInstance) {
		this.serverInstance = serverInstance;
	}
}
