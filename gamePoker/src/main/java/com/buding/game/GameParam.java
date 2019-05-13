package com.buding.game;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GameParam {
	public boolean autoOperWhenTimeout = false; //实际超时时间
	public int chiPengGangPlayMills = 300; //吃碰动画播放时间(毫秒)
	public int chuPlayMills = 300;  //出牌动画播放时间(毫秒)
	public int operTimeOutSeconds = 9; //界面提示用户操作时间(秒)
	public int thinkMills4AutoOper = 300;// 自动托管时，每次出牌思考时间(毫秒)
	public int sendCardPlayMills = 500; //发牌动画播放时间
	public int changeBaoMills = 500; //换宝动画播放时间
	public int totalQuan = 0;
}