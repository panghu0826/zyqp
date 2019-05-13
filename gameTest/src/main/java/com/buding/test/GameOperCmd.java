package com.buding.test;

import org.kohsuke.args4j.Option;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GameOperCmd {
	@Option(name = "-chu")
	public int chu = 0;

	@Option(name = "-zd")
	public int zd = 0;
	
	@Option(name = "-peng")
	public int peng = 0;

	@Option(name = "-chi")
	public int chi = 0;

	@Option(name = "-card2")
	public int card2 = 0;

	@Option(name = "-cancel")
	public boolean cancel = false;

	@Option(name = "-hu")
	public boolean hu = false;

	@Option(name = "-ting")
	public boolean ting = false;

	@Option(name = "-gang")
	public boolean gang = false;

	@Option(name = "-back2hall")
	public boolean back2hall = false;

	@Option(name = "-count")
	public boolean count = false;

	@Option(name = "-dcard")
	public int dcard = -1;

	@Option(name = "-hcard")
	public int hcard = -1;

	@Option(name = "-dumpcard")
	public boolean dumpcard = false;

	@Option(name = "-dumpgame")
	public boolean dumpgame = false;
	
	@Option(name = "-go")
	public boolean go = false;

	@Option(name = "-actpath")
	public String actpath = "/home/game/data/replay.json";

	@Option(name = "-auto")
	public int auto = 0;
	
	@Option(name = "-enroll")
	public boolean enroll = false;
	
	@Option(name = "-match")
	public String match = "G_DQMJ_MATCH_TOP";
	
	@Option(name = "-code")
	public String code = null;
	
	@Option(name = "-ready")
	public boolean ready = false;
	
	@Option(name = "-newvip")
	public int newvip = -1; //2 人  4人
	
	@Option(name = "-quan")
	public int quan = 4;
	
	@Option(name = "-viplist")
	public boolean viplist;
	
	@Option(name = "-kick")
	public boolean kick;
	
	@Option(name = "-desk")
	public String desk;
	
	@Option(name = "-pid")
	public int pid;
	
	@Option(name = "-dismiss")
	public boolean dismiss;
	
	@Option(name = "-reg")
	public boolean reg;
	
	@Option(name = "-user")
	public String user;
	
	@Option(name = "-passwd")
	public String passwd;
	
	@Option(name = "-military")
	public boolean military;
	
	@Option(name = "-id")
	public long id;
	
	@Option(name = "-exit")
	public boolean exit;
	
	@Option(name = "-away")
	public boolean away;
	
	@Option(name = "-order")
	public boolean order;
	
	@Option(name = "-channel")
	public int channel = 3;
	
	@Option(name = "-prd")
	public String prd;
	
	@Option(name = "-pay")
	public String pay;
	
	@Option(name = "-mlist")
	public boolean mallList;
	
	@Option(name = "-chat")
	public String chat;
	
	@Option(name = "-head")
	public String head;
	
	@Option(name = "-nickname")
	public String nickname;
	
	@Option(name = "-rank")
	public boolean rank;
	
	@Option(name = "-roomconf")
	public boolean roomconf;
	
	@Option(name = "-getMailAttch")
	public long getMailAttch ;
	
	@Option(name = "-hangup")
	public boolean hangup ;
	
	@Option(name = "-nohangup")
	public boolean nohangup ;
	
	@Option(name = "-wanfa")
	public int wanfa = 0 ;
	
	@Option(name = "-vote")
	public int vote = -1 ;
	
	@Option(name = "-isGaming")
	public int isGaming = -1 ;
}
