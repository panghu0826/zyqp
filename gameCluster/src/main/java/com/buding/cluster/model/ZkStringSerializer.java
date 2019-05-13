package com.buding.cluster.model;

import java.io.UnsupportedEncodingException;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class ZkStringSerializer implements ZkSerializer {

	@Override
	public byte[] serialize(Object data) throws ZkMarshallingError {
		try {
			return data.toString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		try {
			return new String(bytes, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
