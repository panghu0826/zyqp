package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.PlayerCoinVo;

public class CoinChangeEvent extends Event<PlayerCoinVo> {
	public CoinChangeEvent(PlayerCoinVo vo) {
		super(EventType.COIN_CHANGE, vo);
	}
}
