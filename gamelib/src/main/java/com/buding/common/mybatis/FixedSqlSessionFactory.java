package com.buding.common.mybatis;

import java.io.IOException;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;

public class FixedSqlSessionFactory extends SqlSessionFactoryBean {
	@Override
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        try {
            return super.buildSqlSessionFactory();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ErrorContext.instance().reset();
        }
        return null;
    }
}
