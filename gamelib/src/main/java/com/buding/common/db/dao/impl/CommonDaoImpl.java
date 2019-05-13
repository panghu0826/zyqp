package com.buding.common.db.dao.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.buding.common.db.dao.CommonDao;

public class CommonDaoImpl implements CommonDao {
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public <T> void save(T... paramArrayOfT) {
		if(paramArrayOfT != null) {
			for(T obj : paramArrayOfT) {
				sqlSessionTemplate.insert(obj.getClass().getName()+".insert", obj);
			}
		}
	}

	@Override
	public <T> void update(T... paramArrayOfT) {
		if(paramArrayOfT != null) {
			for(T obj : paramArrayOfT) {
				sqlSessionTemplate.update(obj.getClass().getName()+".updateByPrimaryKey", obj);
			}
		}
	}

	@Override
	public <T> void update(Collection<T> paramCollection) {
		if(paramCollection != null) {
			for(T obj : paramCollection) {
				sqlSessionTemplate.update(obj.getClass().getName()+".updateByPrimaryKey", obj);
			}
		}
	}

	@Override
	public <T> T get(Serializable paramSerializable, Class<T> paramClass) {
		return sqlSessionTemplate.selectOne(paramClass.getName()+".selectByPrimaryKey", paramSerializable);
	}

	@Override
	public <T> void delete(Serializable paramSerializable, Class<T> paramClass) {
		sqlSessionTemplate.delete(paramClass.getName()+".deleteByPrimaryKey", paramSerializable);
	}

	@Override
	public <T> T selectOne(String sql, Class<T> resultType, Object... args) {
		List<T> list = jdbcTemplate.query(sql, args,ParameterizedBeanPropertyRowMapper.newInstance(resultType));
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public <T> List<T> selectList(String sql, Class<T> resultType, Object... args) {
		List<T> list = jdbcTemplate.query(sql, args,ParameterizedBeanPropertyRowMapper.newInstance(resultType));
		return list;
	}

	@Override
	public <T> int count(String sql, Object... args) {
		return jdbcTemplate.queryForInt(sql, args);
	}
}