package com.buding.common.serialize;

import java.util.List;
import java.util.Map;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface Serializer {
	public <T> byte[] serialize(T obj) throws Exception;
	public <T> T deserialize(byte[] data, Class<T> cls) throws Exception;
	

	public <T> byte[] serializeList(List<T> obj, Class<T> cls) throws Exception;
	public <T> List<T> deserializeList(byte[] data, Class<T> cls) throws Exception;
	
	public <K,V> byte[] serializeMap(Map<K,V> obj, Class<K> keyClas, Class<V> valCls) throws Exception;
	public <K,V> Map<K,V> deserializeMap(byte[] data, Class<K> keyCls, Class<V> valCls) throws Exception;
}
