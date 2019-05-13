package com.buding.common.serialize.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buding.common.serialize.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class KryoSerializer implements Serializer {

	@Override
	public <T> byte[] serialize(T obj) {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		kryo.register(obj.getClass(), new JavaSerializer());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Output output = new Output(baos);
		kryo.writeClassAndObject(output, obj);
		output.flush();
		output.close();

		byte[] b = baos.toByteArray();
		return b;
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T> clazz) {
		Kryo kryo = new Kryo(); 
        kryo.setReferences(false); 
        kryo.register(clazz, new JavaSerializer()); 
  
        ByteArrayInputStream bais = new ByteArrayInputStream(data); 
        Input input = new Input(bais); 
        return (T) kryo.readClassAndObject(input);
	}

	@Override
	public <T> byte[] serializeList(List<T> obj, Class<T> cls) {
		Kryo kryo = new Kryo(); 
        kryo.setReferences(false); 
        kryo.setRegistrationRequired(true); 
  
        CollectionSerializer serializer = new CollectionSerializer(); 
        serializer.setElementClass(cls, new JavaSerializer()); 
        serializer.setElementsCanBeNull(false); 
  
        kryo.register(cls, new JavaSerializer()); 
        kryo.register(ArrayList.class, serializer); 
  
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        Output output = new Output(baos); 
        kryo.writeObject(output, obj); 
        output.flush(); 
        output.close(); 
  
        byte[] b = baos.toByteArray(); 
        return b;
	}

	@Override
	public <T> List<T> deserializeList(byte[] data, Class<T> cls) {
		Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
 
        CollectionSerializer serializer = new CollectionSerializer();
        serializer.setElementClass(cls, new JavaSerializer());
        serializer.setElementsCanBeNull(false);
 
        kryo.register(cls, new JavaSerializer());
        kryo.register(ArrayList.class, serializer);
 
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Input input = new Input(bais);
        return (List<T>) kryo.readObject(input, ArrayList.class, serializer);
	}

	@Override
	public <K,V> byte[] serializeMap(Map<K,V> obj, Class<K> keyClas, Class<V> valCls) {
		Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
 
        MapSerializer serializer = new MapSerializer();
        serializer.setKeyClass(keyClas, new JavaSerializer());
        serializer.setKeysCanBeNull(false);
        serializer.setValueClass(valCls, new JavaSerializer());
        serializer.setValuesCanBeNull(true);
 
        kryo.register(valCls, new JavaSerializer());
        kryo.register(HashMap.class, serializer);
 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
 
        byte[] b = baos.toByteArray();
        return b;
	}

	@Override
	public <K,V> Map<K,V> deserializeMap(byte[] data, Class<K> keyCls, Class<V> valCls) {
		Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
 
        MapSerializer serializer = new MapSerializer();
        serializer.setKeyClass(keyCls, new JavaSerializer());
        serializer.setKeysCanBeNull(false);
        serializer.setValueClass(valCls, new JavaSerializer());
        serializer.setValuesCanBeNull(true);
 
        kryo.register(valCls, new JavaSerializer());
        kryo.register(HashMap.class, serializer);
 
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Input input = new Input(bais);
        return (Map<K,V>) kryo.readObject(input, HashMap.class,
                serializer);
	}

}
