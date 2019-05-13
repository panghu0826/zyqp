package com.buding.common.db.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface CommonDao {

	@SuppressWarnings("unchecked")
	public <T> void save(T... paramArrayOfT);

	@SuppressWarnings("unchecked")
	public <T> void update(T... paramArrayOfT);

	public <T> void update(Collection<T> paramCollection);

	public <T> T get(Serializable paramSerializable, Class<T> paramClass);

	public <T> void delete(Serializable paramSerializable, Class<T> paramClass);
	
	public <T> T selectOne(String sql, Class<T> resultType, Object... args);
	
	public <T> List<T> selectList(String sql, Class<T> resultType, Object... args);
	
	public <T> int count(String sql, Object... args) ;
}