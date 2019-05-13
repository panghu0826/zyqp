package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;

public class BindMobileEvent extends Event<Integer> {
	public BindMobileEvent(int userId) {
		super(EventType.BIND_MOBILE, userId);
	}
}
