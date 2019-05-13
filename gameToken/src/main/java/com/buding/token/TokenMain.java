package com.buding.token;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TokenMain {
	private static final Logger LOG = LogManager.getLogger(TokenMain.class);

	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("token-server.xml");
		System.out.println("======================");
		System.out.println("GameToken server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" GameToken server start...............");
		LOG.info("===============================");
		
		 System.in.read();
		
	}
}
