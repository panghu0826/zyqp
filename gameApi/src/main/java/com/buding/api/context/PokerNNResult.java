package com.buding.api.context;

/**
 * @author  chen
 *
 */
public class PokerNNResult {
    public int pos = -1;
    public int playerId = 0;
    public String playerName = "";
    public int lastScore = 0;          //上局总得分
    public int score = 0;              //当局得分
    public int allScore = 0;           //总分
    public String cardType;               //牌型
    public boolean isBanker;           //是否庄家
    public int result = 3;             // 1 win 2 lose 3 even
}
