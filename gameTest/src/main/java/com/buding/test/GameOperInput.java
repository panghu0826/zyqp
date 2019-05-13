package com.buding.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.kohsuke.args4j.CmdLineParser;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GameOperInput implements Runnable {
	GameServerProxy gameProxy;
	BufferedReader reader = null;
	MsgServerProxy msgProxy;
	HallServerProxy hallProxy;

	public GameOperInput(GameServerProxy proxy, MsgServerProxy msgProxy, HallServerProxy hallProxy) {
		this.gameProxy = proxy;
		this.msgProxy = msgProxy;
		this.hallProxy = hallProxy;
		reader = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() {
		try {
			String line = reader.readLine();
			if(org.apache.commons.lang.StringUtils.isBlank(line)) {
				return;
			}
			GameOperCmd cmd = new GameOperCmd();
			CmdLineParser parser = new CmdLineParser(cmd);
			parser.parseArgument(line.split(" "));
			if (cmd.chu > 0) {
				gameProxy.chu(cmd);
				return;
			}
			if (cmd.zd > 0) {
				gameProxy.zd(cmd);
				return;
			}
			if(cmd.peng > 0) {
				gameProxy.peng(cmd);
				return;
			}
			if(cmd.chi > 0) {
				gameProxy.chi(cmd);
				return;
			}
			if(cmd.hu) {
				gameProxy.hu(cmd);
				return;
			}
			if(cmd.cancel) {
				gameProxy.cancel(cmd);
				return;
			}
			if(cmd.ting) {
				gameProxy.ting(cmd);
				return;
			}
			if(cmd.gang) {
				gameProxy.gang(cmd);
				return;
			}
			if(cmd.hcard > -1) {
				gameProxy.viewHandCard(cmd);
				return;
			}
			if(cmd.dcard > -1) {
				gameProxy.viewDownCard(cmd);
				return;
			}
			if(cmd.count) {
				gameProxy.count();
				return;
			}
			if(cmd.dumpcard) {
				gameProxy.dumpCard();
				return;
			}
			if(cmd.back2hall) {
				gameProxy.back2hall(cmd);
				return;
			}
			if(cmd.auto > 0) {				
				gameProxy.loadActs(cmd.actpath, cmd.auto, cmd.go);
				return;
			}
			if(cmd.dumpgame) {
				gameProxy.dumpgame();
				return;
			}
			if(cmd.enroll) {
				gameProxy.enroll(cmd.match, cmd.code);
				return;
			}
			if(cmd.ready) {
				gameProxy.ready();
				return;
			}
			if(cmd.newvip > -1) {
				gameProxy.createVipRoom(cmd.quan, cmd.newvip,cmd.wanfa);
			}
			if(cmd.viplist) {
				gameProxy.viplist();
			}
			if(cmd.kick) {
				gameProxy.kick(cmd.desk, cmd.pid);
			}
			if(cmd.dismiss) {
				gameProxy.dismiss(cmd.desk);
			}
			if(cmd.military) {
				hallProxy.roomResultRequest(cmd.id);
			}
			if(cmd.away) {
				gameProxy.awayGame();
			}
			if(cmd.exit) {
				gameProxy.exitGame();
			}
			if(cmd.order) {
				hallProxy.genOrderRequest(cmd.prd, cmd.channel);
			}
			if(cmd.pay != null) {
				hallProxy.confirmOrderRequest(cmd.pay);
			}
			if(cmd.mallList) {
				hallProxy.mallProductRequest();
			}
			if(cmd.chat != null) {
				gameProxy.chat(cmd.chat);
			}
			if(cmd.head != null || cmd.nickname != null) {
				hallProxy.modifyUserInfo(cmd.head, cmd.nickname);
			}
			if(cmd.rank) {
				hallProxy.loadRank();
			}
			if(cmd.roomconf) {
				hallProxy.loadRoomConf();
			}
			if(cmd.getMailAttch > 0) {
				hallProxy.getMailAttch(cmd.getMailAttch);
			}
			if(cmd.hangup) {
				gameProxy.hangup();
			}
			if(cmd.nohangup) {
				gameProxy.nohangup();
			}
			if(cmd.vote > -1) {
				gameProxy.voteDissmiss(cmd.vote == 1);
			}
			if(cmd.isGaming > 0) {
				gameProxy.isGaming();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
