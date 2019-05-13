package com.buding.api.context;

import com.sun.org.apache.bcel.internal.generic.FADD;

/**
 * @author  chen
 *
 */
public class PokerJACKResult {
    public int pos = 1;
    public int playerId = 0;
    public String playerName = "";
    public int lastScore = 0;          //上局得分
    public int score = 0;              //当局得分
    public int allSocre = 0;           //总分
    public int cardType = 0;             //牌型
    public int cardNum = 0;             //牌型
    public int maxScore = -1000000;    //单局最高得分
    public int maxCardType = 0;    //最大牌型
    public boolean isBanker = false;       //是否庄家
    public int result = 3;             // 1 win 2 lose 3 even
    public boolean isZanLi;             // 1 win 2 lose 3 even
}
