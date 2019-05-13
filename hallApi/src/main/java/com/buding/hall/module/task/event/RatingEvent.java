package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;
import com.buding.hall.module.task.vo.RatingVo;

public class RatingEvent extends Event<RatingVo> {
	public RatingEvent(RatingVo vo) {
		super(EventType.RATING, vo);
	}
}
