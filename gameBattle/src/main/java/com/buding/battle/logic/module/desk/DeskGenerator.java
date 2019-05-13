package com.buding.battle.logic.module.desk;

import com.buding.battle.logic.module.desk.bo.CommonDesk;


public interface DeskGenerator {
	public CommonDesk<?> genDesk(String id,int wanfa) throws Exception;
}
