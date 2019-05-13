package com.buding.common.serialize.json;

import java.util.List;
import java.util.Map;

import com.buding.common.serialize.Serializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class JsonSerializer implements Serializer {

	@Override
	public <T> byte[] serialize(T obj)  throws Exception{
		return new Gson().toJson(obj).getBytes("UTF8");
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T> cls) throws Exception {
		return new Gson().fromJson(new String(data, "UTF8"), cls);
	}

	@Override
	public <T> byte[] serializeList(List<T> obj, Class<T> cls) throws Exception {
		return new Gson().toJson(obj).getBytes("UTF8");
	}

	@Override
	public <T> List<T> deserializeList(byte[] data, Class<T> cls) throws Exception {
		return new Gson().fromJson(new String(data, "UTF8"), new TypeToken<List<T>>(){}.getType());
	}

	@Override
	public <K, V> byte[] serializeMap(Map<K, V> obj, Class<K> keyClas, Class<V> valCls) throws Exception {
		return new Gson().toJson(obj).getBytes("UTF8");
	}

	@Override
	public <K, V> Map<K, V> deserializeMap(byte[] data, Class<K> keyCls, Class<V> valCls) throws Exception {
		return new Gson().fromJson(new String(data, "UTF8"), new TypeToken<Map<K,V>>(){}.getType());
	}

}
