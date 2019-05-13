package com.buding.api.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  chen
 *
 */
public class DDZResult {
    public long startTime;
    public long endTime;
    public int friedKing;   //王炸次数
    public int bomb;        //炸弹次数
    public int spring;      //春天
    public int endPoints;   //底分
    public int innings;     //局
    public Map<Integer,PokerDDZResult> Result = new HashMap<>();
}
