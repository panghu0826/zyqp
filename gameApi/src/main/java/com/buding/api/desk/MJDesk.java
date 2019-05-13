package com.buding.api.desk;

import java.util.Map;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface MJDesk<T> extends Desk<T> {

    boolean canTuoGuan();
	boolean canForceChuPai();

	boolean canXiaManZhu();

	boolean canLunLiuZhuang();

    boolean canNiuNiuShangZhuang();

	boolean canTongBiNiuNiu();

	boolean canJingDianQiangZhuang();

	boolean canMingPaiQiangZhuang();

    boolean canFangZhuKaiFang();

    boolean isVipTable();
	int getWanfa();//获取玩法
	int getRoomType(); // 1:2人麻将 2:4人麻将
	int getTotalQuan();
	int getLimitMax();
	int getMenNum();
	int getYaZhu();
	int getDanZhuLimix();
	long getClubId();
	int getClubRoomType();
	int getEnterScore();
	int getCanFufen();
	int getChoushuiScore();
	int getChoushuiNum();
	int getZengsongNum();
	public int getQiangZhuangNum();
	public Map<Integer, Integer> getNiuFanStr();
	public int getErBaGameType();
	public void playerExitPosNotExitRoom(int playerId, int deskPos);
	//--------------------------------ddz-----------------------------

	boolean canDouble();//是否可以加倍
	boolean canRoundPile();//轮庄
	boolean canYellowPile();//是否比优

	//--------------------------------zjh-----------------------------

	boolean canShunThanJin();//顺>金花
	boolean canDiLong();//地龙
	boolean canFengKuangMode();//疯狂模式
	boolean canBiPaiJiaBei();//比牌加倍
	boolean can235ThanBaoZi();//235>豹子
	boolean can235ThanAAA();//235>AAA
	boolean canWangLaiZi();//王癞子
	boolean canXiQian();//豹子同花顺喜钱
	boolean canTongPaiBiHuaSe();//同牌比花色
	boolean canAutoQiPai();
}
