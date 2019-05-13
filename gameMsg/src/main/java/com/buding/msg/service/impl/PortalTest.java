package com.buding.msg.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.util.IOUtil;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgReq;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.msg.vo.MarqueeMsgReq;
import com.buding.hall.module.ws.MsgPortalService;
import com.google.gson.Gson;

public class PortalTest {
	@Autowired
	MsgPortalService portal;

	private String marqueeTplMsgFile;
	private String boxTplMsgFile;
	private String marqueeMsgFile;
	private String boxMsgFile;

	public void sendMarqueeTplMsg() throws Exception {
		String content = IOUtil.getFileResourceAsString(marqueeTplMsgFile, "UTF-8");
		MarqueeMsgReq marqueeTplMsg = new Gson().fromJson(content, MarqueeMsgReq.class);
		portal.sendTplMarqueeMsg(marqueeTplMsg);
	}

	public void sendBoxTplMsg() throws Exception {
		String content = IOUtil.getFileResourceAsString(boxTplMsgFile, "UTF-8");
		BoxMsgReq boxTplMsg = new Gson().fromJson(content, BoxMsgReq.class);
		portal.sendTplBoxMsg(boxTplMsg);
	}

	public void sendMarqueeMsg() throws Exception {
		String content = IOUtil.getFileResourceAsString(marqueeMsgFile, "UTF-8");
		MarqueeMsg marqueeMsg = new Gson().fromJson(content, MarqueeMsg.class);
		portal.sendMarqueeMsg(marqueeMsg);
	}
	
	public void sendMarqueeMsg(int userId) throws Exception {
		String content = IOUtil.getFileResourceAsString(marqueeMsgFile, "UTF-8");
		MarqueeMsg marqueeMsg = new Gson().fromJson(content, MarqueeMsg.class);
		marqueeMsg.receiver = userId;
		portal.sendMarqueeMsg(marqueeMsg);
	}
	
	public void sendBoxMsg(int userId) throws Exception {
		String content = IOUtil.getFileResourceAsString(boxMsgFile, "UTF-8");
		BoxMsg boxMsg = new Gson().fromJson(content, BoxMsg.class);
		boxMsg.receiver = userId;
		portal.sendBoxMsg(boxMsg);
	}

	public void sendBoxMsg() throws Exception {
		String content = IOUtil.getFileResourceAsString(boxMsgFile, "UTF-8");
		BoxMsg boxMsg = new Gson().fromJson(content, BoxMsg.class);
		portal.sendBoxMsg(boxMsg);
	}

	public void setMarqueeTplMsgFile(String marqueeTplMsgFile) {
		this.marqueeTplMsgFile = marqueeTplMsgFile;
	}

	public void setBoxTplMsgFile(String boxTplMsgFile) {
		this.boxTplMsgFile = boxTplMsgFile;
	}

	public void setMarqueeMsgFile(String marqueeMsgFile) {
		this.marqueeMsgFile = marqueeMsgFile;
	}

	public void setBoxMsgFile(String boxMsgFile) {
		this.boxMsgFile = boxMsgFile;
	}
}
