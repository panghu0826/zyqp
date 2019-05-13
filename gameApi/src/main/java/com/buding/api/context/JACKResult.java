package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class JACKResult {
    public long startTime;
    public long endTime;
    public int juNum;     //局
    public Map<Integer,PokerJACKResult> Result = new HashMap<>();
}
