package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.PlayerCoinVo;
import com.buding.hall.module.task.vo.PlayerDiamondVo;

public class DiamondChangeEvent extends Event<PlayerDiamondVo> {
	public DiamondChangeEvent(PlayerDiamondVo vo) {
		super(EventType.DIAMOND_CHANGE, vo);
	}
}
