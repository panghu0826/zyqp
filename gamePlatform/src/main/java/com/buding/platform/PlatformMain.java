package com.buding.platform;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PlatformMain {
	private static final Logger LOG = LogManager.getLogger(PlatformMain.class);
	
	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("platform-server.xml");
		System.out.println("======================");
		System.out.println("PlatformServer server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info("PlatformServer server start...............");
		LOG.info("===============================");
		
		System.in.read();
		
		LOG.info("===============================");
		LOG.info("PlatformServer server stoped...............");
		LOG.info("===============================");
	}
}
