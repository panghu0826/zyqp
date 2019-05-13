package com.buding.hall;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HallMain {
	private static final Logger LOG = LogManager.getLogger(HallMain.class);
	
	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("hall-server.xml");
		System.out.println("======================");
		System.out.println("HallServer server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" HallServer server start...............");
		LOG.info("===============================");
		
		System.in.read();
	}
}
