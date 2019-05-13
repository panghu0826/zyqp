package com.buding.common.cluster.model;

import java.io.Serializable;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class ServerModel implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;

	public String instanceId;
	public String serverType; // 比如战斗服务器,消息服务器,大厅服务器
	public String addr;
	public int onlineNum;
	public String status;
	public long lastUpdate; // 最后更新时间，精确到毫秒
	public String dubboAddr; // dubbo服务暴露的地址

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getOnlineNum() {
		return onlineNum;
	}

	public void setOnlineNum(int onlineNum) {
		this.onlineNum = onlineNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDubboAddr() {
		return dubboAddr;
	}

	public void setDubboAddr(String dubboAddr) {
		this.dubboAddr = dubboAddr;
	}

}
