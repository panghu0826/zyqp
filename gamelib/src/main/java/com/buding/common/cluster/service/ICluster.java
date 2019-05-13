package com.buding.common.cluster.service;

import java.util.List;

import com.buding.common.cluster.model.ServerModel;


/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface ICluster {
	/**
	 * 获取所有服务列表
	 * @param serverType
	 * @return
	 */
	public List<ServerModel> getAllServer(String serverType);
	
	/**
	 * 开始对外服务
	 * @param instanceId
	 */
	public void stop(String instanceId);
	
	/**
	 * 停止对外服务
	 * @param instanceId
	 */
	public void start(String instanceId);
	
	/**
	 * 获取空闲服务器
	 * @param serverType
	 * @return
	 */
	public ServerModel getFreeServer(String serverType);
	
	public String getStatus();
}
