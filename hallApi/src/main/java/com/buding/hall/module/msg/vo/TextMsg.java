package com.buding.hall.module.msg.vo;

public class TextMsg extends BaseMsg implements Cloneable {
	private static final long serialVersionUID = 1L;
	public String key;
	
	public TextMsg copy() throws Exception {
		return (TextMsg)this.clone();
	}
}
