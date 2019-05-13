package com.buding.common.network.command;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface Cmd<KEY,DATA> {
	public void execute(DATA data) throws Exception;
	public KEY getKey();
}
