package com.buding.api.context;

/**
 * @author  chen
 *
 */
public class PokerZJHFinalResult {
    public int pos = -1;
    public int playerId;
    public String playerName;
    public String headImg;
    public int allScore = 0;    //总分
    public int maxScore = 0;    //单局最高得分
    public int maxCardType = 0;    //最大牌型
    public int winNum = 0;  //胜局
    public int loseNum = 0;  //胜局
    public int score = 0;//总输赢积分
}
