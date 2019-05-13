package com.buding.db;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DataMain {
	private static final Logger LOG = LogManager.getLogger(DataMain.class);

	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("data-server.xml");
		System.out.println("======================");
		System.out.println("GameData server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" GameData server start...............");
		LOG.info("===============================");
		
		 System.in.read();
		
	}
}
