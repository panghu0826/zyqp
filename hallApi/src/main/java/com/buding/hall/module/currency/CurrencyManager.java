package com.buding.hall.module.currency;

import java.util.HashMap;
import java.util.Map;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CurrencyManager {
	public Map<Integer, CurrencyProcessor> processorMap = new HashMap<Integer, CurrencyProcessor>();
	
	public Result checkEnough(int currenceType, int userId, int amount) {
		CurrencyProcessor p = processorMap.get(currenceType);
		if(p == null) {
			return Result.fail("无效货币");
		}
		return p.checkEnough(userId, amount);
	}
	
	public Result add(int currenceType, int userId, int amount, String changeReason) {
		CurrencyProcessor p = processorMap.get(currenceType);
		if(p == null) {
			return Result.fail("无效货币");
		}
		return p.add(userId, amount, changeReason);
	}
	
	public Result sub(int currenceType, int userId, int amount, String changeReason) {
		CurrencyProcessor p = processorMap.get(currenceType);
		if(p == null) {
			return Result.fail("无效货币");
		}
		return p.sub(userId, amount, changeReason);
	}
	
	public Result change(int currenceType, int userId, int amount, String changeReason) {
		CurrencyProcessor p = processorMap.get(currenceType);
		if(p == null) {
			return Result.fail("无效货币");
		}
		return p.change(userId, amount, changeReason);
	}
	
	public TResult<Integer> get(int currenceType,int userId) {
		CurrencyProcessor p = processorMap.get(currenceType);
		if(p == null) {
			return TResult.fail1("无效货币");
		}
		return p.get(userId);
	}
}
