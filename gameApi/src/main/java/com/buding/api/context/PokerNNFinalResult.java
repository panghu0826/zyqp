package com.buding.api.context;

/**
 * @author  chen
 *
 */
public class PokerNNFinalResult {
    public int pos = -1;
    public int playerId;
    public String playerName;
    public String headImg;
    public int allScore = 0;    //总分(带基础分,如俱乐部积分)
    public int winNum = 0;  //胜局
    public int loseNum = 0;  //胜局
    public int score = 0;//总输赢积分
}
