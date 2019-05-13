package com.buding.hall.module.msg.vo;

import java.util.List;


public class GameChatMsg extends BaseMsg implements Cloneable {	
	 private static final long serialVersionUID = 1L;
	 
	 public byte[] data;
	 public List<Integer> receiverIds;
	 public int senderPosition;
	 public String deskId;
	
	public GameChatMsg copy() throws Exception {
		return (GameChatMsg)this.clone();
	}
}
