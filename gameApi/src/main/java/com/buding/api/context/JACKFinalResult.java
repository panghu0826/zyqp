package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class JACKFinalResult {
    public long startTime = System.currentTimeMillis();
    public long endTime;

    public int roomId;      //房间号

    public Map<Integer,PokerJACKFinalResult> finalResults = new HashMap<>();

}
