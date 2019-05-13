package com.buding.common.model;

import java.util.Arrays;

/**
 * 消息对象
 * @author -jaime-
 *
 */
public class Message {
	/**
	 * 数据
	 */
	private byte[] data;

	public Message(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Message{" +
				"data=" + Arrays.toString(data) +
				'}';
	}
}
