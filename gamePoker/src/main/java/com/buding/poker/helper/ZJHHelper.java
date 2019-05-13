package com.buding.poker.helper;

import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  chen 2017 -12 - 20
 *
 */
public class ZJHHelper {
    public static Map<Integer, String>  singleCardMap = new HashMap<Integer, String>();
    public static Map<String, Byte>  singleCardMapChinese = new HashMap();

    static {
        singleCardMap.put(PokerConstants.POKER_CODE_DA_WANG + 0, "大王");
        singleCardMap.put(PokerConstants.POKER_CODE_XIAO_WANG + 0, "小王");

        singleCardMapChinese.put("大王", PokerConstants.POKER_CODE_DA_WANG);
        singleCardMapChinese.put("小王", PokerConstants.POKER_CODE_XIAO_WANG);
        for (int j = 0; j < 4; j++) {
            for (int i = 2; i <= 14; i++) {
                int ib = (j << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
                int b = (ib & 0xff);
                if (j == 0) {
                    singleCardMap.put(b, "方块♦" + swichPokerName(b));
                    singleCardMapChinese.put("方" + swichPokerName(b), (byte)b);
                } else if (j == 1) {
                    singleCardMap.put(b, "梅花♣" + swichPokerName(b));
                    singleCardMapChinese.put("梅" + swichPokerName(b), (byte)b);
                } else if (j == 2) {
                    singleCardMap.put(b, "红桃♥" + swichPokerName(b));
                    singleCardMapChinese.put("红" + swichPokerName(b),(byte) b);
                } else {
                    singleCardMap.put(b, "黑桃♠" + swichPokerName(b));
                    singleCardMapChinese.put("黑" + swichPokerName(b), (byte)b);
                }
            }
        }
        System.out.println(new Gson().toJson(singleCardMapChinese));
    }

    public static String swichPokerName(int num) {
        num = num & 0x0f;

        switch (num) {
            case 2:
                return "2";
            case 3:
                return "3";
            case 4:
                return "4";
            case 5:
                return "5";
            case 6:
                return "6";
            case 7:
                return "7";
            case 8:
                return "8";
            case 9:
                return "9";
            case 10:
                return "10";
            case 11:
                return "J";
            case 12:
                return "Q";
            case 13:
                return "K";
            case 14:
                return "A";

        }

        return "";
    }

    public static String getSingleCardListName(List<Byte> cards) {

        List<String> ret = new ArrayList<String>();
        for (int card : cards) {
            ret.add(getSingleCardName(card));
        }
        return new Gson().toJson(ret);
    }

    public static String getSingleCardName(int card) {
        if (card == -1) {
            return "背面";
        }
        if (card == 0) {
            return "";
        }
        return singleCardMap.get(card);
    }


    /**
     * 选择抢地主的枫提示分数
     *
     * @param num
     * @return
     */
    public static List<Integer> switchRobotNum (int num) {
        List<Integer> list = new ArrayList<>();
        switch (num) {
            case 0 :
                list.add(0);
                list.add(1);
                list.add(2);
                list.add(3);
                return list;
            case 1 :
                list.add(0);
                list.add(2);
                list.add(3);
                return list;
            case 2 :
                list.add(0);
                list.add(3);
                return list;
            case 3 :
                list.add(3);
                return list;
        }
        return  list;
    }
    /**
     * 带比优
     * 第一家可以叫0,1,2,3
     * 第二家只能叫0,2,3
     * 第三家只能叫0,3
     * @param num
     * @return
     */
    public static List<Integer> switchRobotNumWithBiYou (int num) {
        List<Integer> list = new ArrayList<>();
        switch (num) {
            case 1 :
                list.add(0);
                list.add(1);
                list.add(2);
                list.add(3);
                return list;
            case 2 :
                list.add(0);
                list.add(2);
                list.add(3);
                return list;
            case 3 :
                list.add(0);
                list.add(3);
                return list;
        }
        return  list;
    }

    private static int convertAct(List<String> ret, int multiAct, int singleAct, String actName) {
        if ((multiAct & singleAct) == singleAct) {
            ret.add(actName);
            return multiAct - singleAct;
        }
        return multiAct;
    }

    public static String getActionName(int act) {
        List<String> ret = new ArrayList<String>();
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_CANCEL, "取消");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_CHU, "出");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_DOUBLE, "加倍");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_NOT_DOUBLE, "不加倍");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_NOT_ROBOT_BANKER, "不抢地主");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_ROBOT_BANKER, "抢地主");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_PLAY_ROBOT, "确认地主是谁");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_PROMPT, "提示");
        act = convertAct(ret, act, PokerConstants.POKER_OPERTAION_GAME_OVER, "结束");


        if (act > 0) {
            throw new RuntimeException("0x" + Integer.toHexString(act) + "未知牌操作");
        }
        return new Gson().toJson(ret);
    }

}
