package com.buding.api.context;

/**
 * @author  chen
 *
 */
public class PokerZJHResult {
    public int pos = -1;
    public int playerId = 0;
    public String playerName = "";
    public int lastScore = 0;          //上局得分
    public int score = 0;              //当局得分
    public int allSocre = 0;           //总分
    public int xiQian = 0;             //喜钱
    public boolean isQiPai;             //是否弃牌
    public boolean isKanPai;             //是否弃牌
    public int cardType;             //牌型
    public int maxScore = -1000000;    //单局最高得分
    public int maxCardType = 0;    //最大牌型
    public boolean isBanker;       //是否庄家
    public int result = 3;             // 1 win 2 lose 3 even
}
