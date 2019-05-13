package com.buding.common.db.dao;


public interface DaoCommonBehavior<MODEL,PK> {
	
    int deleteByPrimaryKey(PK id);

    int insert(MODEL record);

    int insertSelective(MODEL record);

    MODEL selectByPrimaryKey(PK id);
    
    int updateByPrimaryKeySelective(MODEL record);

    int updateByPrimaryKey(MODEL record);
}
