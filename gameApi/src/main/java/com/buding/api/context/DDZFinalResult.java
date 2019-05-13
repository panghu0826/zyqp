package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class DDZFinalResult {
    public long startTime;
    public long endTime;
    public int roomId;      //房间号
    public Map<Integer,PokerDDZFinalResult> finalResults = new HashMap<>();
}
