package com.buding.battle.logic.module.desk.bo;

import com.buding.api.desk.MJDesk;
import com.buding.api.game.PokerWanfa;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.hall.config.DeskConfig;

import java.util.HashMap;
import java.util.Map;

import static com.buding.api.game.PokerWanfa.*;
/**
 * @author jaime qq_1094086610
 * @Description: 麻将桌
 * 
 */
public abstract class MJDeskImpl extends RobotSupportDeskImpl implements MJDesk<byte[]> {
	protected int wanfa = 0;
	
	public MJDeskImpl(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
		super(listener, room, deskConf, deskId);
		wanfa = DDZ_DOUBLE;
	}

	// true 加倍 false 不加倍
	@Override
	public boolean canDouble() {
		return (wanfa & PokerWanfa.DDZ_DOUBLE) == PokerWanfa.DDZ_DOUBLE;
	}


	// 是轮桩 false 是上把谁先赢谁先喊
	@Override
	public boolean canRoundPile() {
		return (wanfa & PokerWanfa.DDZ_WHEEL_BANKER) == PokerWanfa.DDZ_WHEEL_BANKER;
	}

	//true 黄桩直接发牌 false 比大小(两王四二谁多谁输 如果一样多就看先后顺序)
	@Override
	public boolean canYellowPile() {
		return (wanfa & PokerWanfa.DDZ_BETTER) == PokerWanfa.DDZ_BETTER;
	}

	@Override
	public boolean canShunThanJin() {
		return (wanfa & PokerWanfa.ZJH_SHUN_THAN_JIN) == PokerWanfa.ZJH_SHUN_THAN_JIN;
	}

	@Override
	public boolean canDiLong() {
		return (wanfa & PokerWanfa.ZJH_DI_LONG) == PokerWanfa.ZJH_DI_LONG;
	}

	@Override
	public boolean canFengKuangMode() {
		return (wanfa & PokerWanfa.ZJH_FENG_KUANG) == PokerWanfa.ZJH_FENG_KUANG;
	}

	@Override
	public boolean canBiPaiJiaBei() {
		return (wanfa & PokerWanfa.ZJH_BI_PAI_JIA_BEI) == PokerWanfa.ZJH_BI_PAI_JIA_BEI;
	}


	@Override
	public boolean can235ThanBaoZi() {
		return (wanfa & PokerWanfa.ZJH_235_THAN_BAO_ZI) == PokerWanfa.ZJH_235_THAN_BAO_ZI;
	}

	@Override
	public boolean can235ThanAAA() {
		return (wanfa & PokerWanfa.ZJH_235_THAN_AAA) == PokerWanfa.ZJH_235_THAN_AAA;
	}

	@Override
	public boolean canWangLaiZi() {
		return (wanfa & PokerWanfa.ZJH_WANG_LAI_ZI) == PokerWanfa.ZJH_WANG_LAI_ZI;
	}

	@Override
	public boolean canXiQian() {
		return (wanfa & PokerWanfa.ZJH_XI_QIAN) == PokerWanfa.ZJH_XI_QIAN;
	}

	@Override
	public boolean canTongPaiBiHuaSe() {
		return (wanfa & PokerWanfa.ZJH_TONG_PAI_BI_HUA_SE) == PokerWanfa.ZJH_TONG_PAI_BI_HUA_SE;
	}

	@Override
	public boolean canAutoQiPai() {
		return (wanfa & PokerWanfa.ZJH_AUTO_QI_PAI) == PokerWanfa.ZJH_AUTO_QI_PAI;
	}

	@Override
	public boolean canTuoGuan() {
		return (wanfa & PokerWanfa.JACK_TUO_GUAN) == PokerWanfa.JACK_TUO_GUAN;
	}

	@Override
	public boolean canForceChuPai() {
		return (wanfa & PokerWanfa.JACK_FORCE_CHU_PAI) == PokerWanfa.JACK_FORCE_CHU_PAI;
	}

	@Override
	public boolean canXiaManZhu() {
		return (wanfa & PokerWanfa.JACK_XIA_MAN_ZHU) == PokerWanfa.JACK_XIA_MAN_ZHU;
	}

	@Override
	public boolean canLunLiuZhuang() {
		return (wanfa & PokerWanfa.JACK_LUN_LIU_ZHUANG) == PokerWanfa.JACK_LUN_LIU_ZHUANG;
	}
	@Override
	public boolean canNiuNiuShangZhuang() {
		return (wanfa & PokerWanfa.NN_NIU_NIU_SHANG_ZHUANG) == PokerWanfa.NN_NIU_NIU_SHANG_ZHUANG;
	}
	@Override
	public boolean canTongBiNiuNiu() {
		return (wanfa & PokerWanfa.NN_TONG_BI_NIU_NIU) == PokerWanfa.NN_TONG_BI_NIU_NIU;
	}
	@Override
	public boolean canJingDianQiangZhuang() {
		return (wanfa & PokerWanfa.NN_JING_DIAN_QIANG_ZHUANG) == PokerWanfa.NN_JING_DIAN_QIANG_ZHUANG;
	}
	@Override
	public boolean canMingPaiQiangZhuang() {
		return (wanfa & PokerWanfa.NN_MING_PAI_QIANG_ZHUANG) == PokerWanfa.NN_MING_PAI_QIANG_ZHUANG;
	}
	@Override
	public boolean canFangZhuKaiFang() {
		return (wanfa & PokerWanfa.FANG_ZHU_KAI_FANG) == PokerWanfa.FANG_ZHU_KAI_FANG;
	}

	@Override
	public boolean isVipTable() {
		return false;
	}

	@Override
	public int getWanfa() {
		return wanfa;
	}

	@Override
	public int getRoomType() {
		return 2;
	}

	@Override
	public int getTotalQuan() {
		return 1;
	}

	public int getLimitMax(){
		return -1;
	}

	public int getMenNum(){
		return -1;
	}

	public int getYaZhu() {
		return -1;
	}
	public int getDanZhuLimix() {
		return -1;
	}

	public long getClubId(){
		return -1;
	}

	public int getClubRoomType(){
		return -1;
	}

	public int getEnterScore(){
		return -1;
	}
	public int getCanFufen(){
		return -1;
	}
	public int getChoushuiScore(){
		return -1;
	}
	public int getChoushuiNum(){
		return -1;
	}
	public int getZengsongNum(){
		return -1;
	}
	public int getQiangZhuangNum(){
		return -1;
	}

	public int getErBaGameType() {
		return -1;
	}


	public Map<Integer, Integer> getNiuFanStr() {
		return new HashMap<>();
	}
}
