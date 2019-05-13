package com.buding.poker.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class CardWasher {
	/**
	 * 洗牌
	 * @param list
	 * @return
	 */
	public List<Byte> wash(List<Byte> list) {
		Byte[] data = new Byte[list.size()];
		list.toArray(data);
		
		int i = data.length;
		while(i > 0) {
			int rand = (int)(System.nanoTime()%i);
			
			byte tmp = data[i-1];
			data[i-1] = data[rand];
			data[rand] = tmp;			
			i--;
		}
		
		List<Byte> ret = new ArrayList<>();
		for(byte b : data) {
			ret.add(b);
		}
		
		//二次打乱
		Collections.shuffle(ret);
		
		return ret;
	}
}
