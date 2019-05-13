package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class ErBaResult {
    public long startTime;
    public long endTime;
    public int juNum;     //局
    public Map<Integer,PokerErBaResult> Result = new HashMap<>();
}
