package com.buding.battle;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BattleMain {
	private static final Logger LOG = LogManager.getLogger(BattleMain.class);

	public static void main(String[] args) {
		System.setProperty("dubbo.qos.port","33333");
		new ClassPathXmlApplicationContext("battle-server.xml");
		System.out.println("======================");
		System.out.println("BattleServer server start...");
		System.out.println("======================");

		LOG.info("===============================");
		LOG.info(" BattleServer server start...............");
		LOG.info("===============================");
	}
}
