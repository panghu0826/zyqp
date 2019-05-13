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
public class NNHelper {
    public static Map<Integer, String>  singleCardMap = new HashMap<Integer, String>();
    public static Map<String, Byte>  singleCardMapChinese = new HashMap();

    static {
        singleCardMap.put((int) PokerConstants.POKER_CODE_DA_WANG, "大王");
        singleCardMap.put((int) PokerConstants.POKER_CODE_XIAO_WANG, "小王");

        singleCardMapChinese.put("大王", PokerConstants.POKER_CODE_DA_WANG);
        singleCardMapChinese.put("小王", PokerConstants.POKER_CODE_XIAO_WANG);
        for (int j = 0; j < 4; j++) {
            for (int i = 1; i <= 13; i++) {
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
            case 1:
                return "A";
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

}
