package com.buding.api.context;

/**
 * @author  chen
 *
 */
public class PokerDDZResult {
    public static int GAME_RESULT_WIN = 1;
    public static int GAME_RESULT_LOSE = 2;
    public static int GAME_RESULT_EVEN = 3;

    public int pos = -1;
    public int playerId = 0;
    public String playerName = "";
    public int isDouble = 0;           //是否加倍 0不加倍 1为加倍
    public int multiple = 1;           //总倍数
    public int score = 0;              //当局得分
    public int allScore = 0;           //总分
    public int isDiZhu = 0;            //是否地主 0表示不是 1表示地主
    public int result = 3; // 1 win 2 lose 3 even
}
