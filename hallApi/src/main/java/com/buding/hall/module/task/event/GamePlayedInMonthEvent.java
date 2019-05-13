package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.GamePlayingVo;

public class GamePlayedInMonthEvent extends Event<GamePlayingVo> {
	public GamePlayedInMonthEvent(GamePlayingVo vo) {
		super(EventType.PLAYED_GAME_MONTH, vo);
	}
}
