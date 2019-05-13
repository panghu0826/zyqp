package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class NNResult {
    public long startTime;
    public long endTime;
    public int juNum;     //局
    public int winnerIndex; //赢家的下标
    public Map<Integer,PokerNNResult> Result = new HashMap<>();
}
