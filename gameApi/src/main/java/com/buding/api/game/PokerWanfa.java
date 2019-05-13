package com.buding.api.game;

/**
 * @author jaime qq_1094086610
 */
public class PokerWanfa {
    public static int DDZ_WHEEL_BANKER = 0X1;//斗地主轮庄
    public static int DDZ_WINNER_BANKER = 0x2;//斗地主赢家庄
    public static int DDZ_DOUBLE = 0X4;//斗地主加倍
    public static int DDZ_BETTER = 0x8;//斗地主比优

    public static int ZJH_SHUN_THAN_JIN = 0x10;//顺>金花
    public static int ZJH_DI_LONG = 0x20;//地龙(123 比QKA小 比其余顺子大,不选是最小的)
    public static int ZJH_FENG_KUANG = 0x40;//疯狂玩法(只有10JQKA)
    public static int ZJH_BI_PAI_JIA_BEI = 0x80;//比牌加倍
    public static int ZJH_ZHONG_TU_JIN_RU = 0x100;//中途禁入
    public static int ZJH_235_THAN_BAO_ZI = 0x200;//散235大豹子
    public static int ZJH_235_THAN_AAA = 0x400;//散235大AAA
    public static int ZJH_WANG_LAI_ZI = 0x800;//王癞子
    public static int ZJH_XI_QIAN = 0x1000;//豹子同花顺加分
    public static int ZJH_TONG_PAI_BI_HUA_SE = 0x2000;//同大小比花色
    public static int ZJH_AUTO_QI_PAI = 0x4000;//自动弃牌
    public static int ZJH_CAN_CUO_PAI = 0x8000;//可以搓牌
    public static int BI_PAI_DONG_HUA = 0x100000;//比牌动画

    public static int JACK_TUO_GUAN = 0x10000;//离线托管
    public static int JACK_FORCE_CHU_PAI = 0x20000;//强制出牌
    public static int JACK_XIA_MAN_ZHU = 0x40000;//下满注
    public static int JACK_LUN_LIU_ZHUANG = 0x80000;//轮流庄

    public static int NN_NIU_NIU_SHANG_ZHUANG = 0x100000;//牛牛上庄
    public static int NN_TONG_BI_NIU_NIU = 0x200000;//通比牛牛
    public static int NN_JING_DIAN_QIANG_ZHUANG = 0x400000;//经典抢庄
    public static int NN_MING_PAI_QIANG_ZHUANG = 0x800000;//明牌抢庄
    public static int FANG_ZHU_KAI_FANG = 0x1000000;//明牌抢庄

    public static String getWanFaString(int wanfa) {
        String result = "";
        if ((wanfa & DDZ_BETTER) == DDZ_BETTER) {
            result += "比优 ";
        }
        if ((wanfa & DDZ_DOUBLE) == DDZ_DOUBLE) {
            result += "踢 ";
        }
        if ((wanfa & DDZ_WHEEL_BANKER) == DDZ_WHEEL_BANKER) {
            result += "轮庄 ";
        }
        if ((wanfa & DDZ_WINNER_BANKER) == DDZ_WINNER_BANKER) {
            result += "赢家庄 ";
        }
        if ((wanfa & ZJH_SHUN_THAN_JIN) == ZJH_SHUN_THAN_JIN) {
            result += "顺>金花 ";
        }
        if ((wanfa & ZJH_DI_LONG) == ZJH_DI_LONG) {
            result += "地龙 ";
        }
        if ((wanfa & ZJH_FENG_KUANG) == ZJH_FENG_KUANG) {
            result += "疯狂模式 ";
        }
        if ((wanfa & ZJH_BI_PAI_JIA_BEI) == ZJH_BI_PAI_JIA_BEI) {
            result += "比牌加倍 ";
        }
        if ((wanfa & ZJH_ZHONG_TU_JIN_RU) == ZJH_ZHONG_TU_JIN_RU) {
            result += "中途禁入 ";
        }
        if ((wanfa & ZJH_235_THAN_BAO_ZI) == ZJH_235_THAN_BAO_ZI) {
            result += "散235大豹子 ";
        }
        if ((wanfa & ZJH_235_THAN_AAA) == ZJH_235_THAN_AAA) {
            result += "散235大AAA ";
        }
        if ((wanfa & ZJH_WANG_LAI_ZI) == ZJH_WANG_LAI_ZI) {
            result += "王癞子 ";
        }
        if ((wanfa & ZJH_XI_QIAN) == ZJH_XI_QIAN) {
            result += "豹子同花喜钱 ";
        }
        if ((wanfa & ZJH_TONG_PAI_BI_HUA_SE) == ZJH_TONG_PAI_BI_HUA_SE) {
            result += "同大小比花色 ";
        }
        if ((wanfa & ZJH_AUTO_QI_PAI) == ZJH_AUTO_QI_PAI) {
            result += "自动弃牌 ";
        }
        if ((wanfa & ZJH_CAN_CUO_PAI) == ZJH_CAN_CUO_PAI) {
            result += "看牌可搓牌 ";
        }
        if ((wanfa & JACK_TUO_GUAN) == JACK_TUO_GUAN) {
            result += "托管 ";
        }
        if ((wanfa & JACK_FORCE_CHU_PAI) == JACK_FORCE_CHU_PAI) {
            result += "强制出牌 ";
        }
        if ((wanfa & JACK_XIA_MAN_ZHU) == JACK_XIA_MAN_ZHU) {
            result += "下满注 ";
        }
        if ((wanfa & JACK_LUN_LIU_ZHUANG) == JACK_LUN_LIU_ZHUANG) {
            result += "轮流庄 ";
        }
        if ((wanfa & BI_PAI_DONG_HUA) == BI_PAI_DONG_HUA) {
            result += "比牌动画 ";
        }
        if ((wanfa & NN_NIU_NIU_SHANG_ZHUANG) == NN_NIU_NIU_SHANG_ZHUANG) {
            result += "牛牛上庄 ";
        }
        if ((wanfa & NN_TONG_BI_NIU_NIU) == NN_TONG_BI_NIU_NIU) {
            result += "通比牛牛 ";
        }
        if ((wanfa & NN_JING_DIAN_QIANG_ZHUANG) == NN_JING_DIAN_QIANG_ZHUANG) {
            result += "经典抢庄 ";
        }
        if ((wanfa & NN_MING_PAI_QIANG_ZHUANG) == NN_MING_PAI_QIANG_ZHUANG) {
            result += "明牌抢庄 ";
        }
        if ((wanfa & FANG_ZHU_KAI_FANG) == FANG_ZHU_KAI_FANG) {
            result += "房主开房 ";
        }

        return result;
    }

}
