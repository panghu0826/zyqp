package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class ZJHResult {
    public long startTime;
    public long endTime;
    public int zongZhu;   //总注
    public int juNum;     //局
    public int winnerIndex; //赢家的下标
    public Map<Integer,PokerZJHResult> Result = new HashMap<>();
}
