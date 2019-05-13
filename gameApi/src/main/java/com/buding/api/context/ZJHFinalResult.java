package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class ZJHFinalResult {
    public long startTime = System.currentTimeMillis();
    public long endTime;
    public int roomId;      //房间号
    public Map<Integer,PokerZJHFinalResult> finalResults = new HashMap<>();
}
