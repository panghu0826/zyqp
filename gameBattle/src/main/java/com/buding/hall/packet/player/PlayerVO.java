package com.buding.hall.packet.player;

public class PlayerVO {
	public int position;
	public String name;
	public int playerId;
	public String headImg;
	public int gender; // 0 女 1男
	public int coin; 
	public int gold;
	public int bindedMobile; // 0 没有 1已绑定
	public int userType; // 0 游客, 其它待定
	public int winCount;
	public int loseCount;
	public int threeRank;
	public int lzRank;
	public int feeTicket;
	public int vipType; // 0普通用户,其它待定
	public int cardRecorder;
	public int threeRankPoint;
	public int lzRankPoint;
	public int status = -1;
}
