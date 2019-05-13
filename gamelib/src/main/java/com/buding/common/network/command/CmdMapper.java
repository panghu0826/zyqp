package com.buding.common.network.command;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public abstract class CmdMapper<KEY, DATA> {
	public Map<KEY, Cmd<KEY, DATA>> map = new HashMap<KEY, Cmd<KEY,DATA>>();
	
	public void register(Cmd<KEY, DATA> cmd){
		map.put(cmd.getKey(), cmd);
	}
	
	public Cmd<KEY, DATA> get(KEY key){
		return map.get(key);
	}
}
