package com.buding.poker.helper;

import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErBaHelper {
    public static Map<Integer, String> singleCardMap = new HashMap<>();
    public static Map<Integer, String> siMenMap = new HashMap<>();

    static {
        singleCardMap.put((int) PokerConstants.MJ_CODE_HONG_ZHONG, "红中");

        for (int i = 1; i <= 9; i++) {
            int ib = (0x2 << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
            int b = (ib & 0xff);
            singleCardMap.put(b, i + "筒");
        }

        siMenMap.put(PokerConstants.ErBa_SIMEN_ZHUANG_JIA, "庄家");
        siMenMap.put(PokerConstants.ErBa_SIMEN_TIAN_MEN, "天门");
        siMenMap.put(PokerConstants.ErBa_SIMEN_GUO_MEN, "过门");
        siMenMap.put(PokerConstants.ErBa_SIMEN_KANG_MEN, "亢门");
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
