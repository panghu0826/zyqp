package com.buding.common.zk.client;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class ZkClientBase {
	protected Logger logger = LogManager.getLogger(getClass());
	
	public ZkClient zkClient;
	public String zkServer;
	
	protected void initZkClient(ZkSerializer serializer) {
		zkClient = new ZkClient(zkServer, 10000, 10000, serializer);
		logger.info("connect zkclient ok........");
	}
	
	public void setZkServer(String zkServer) {
		this.zkServer = zkServer;
	}
}
