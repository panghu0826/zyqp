package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;

public class ShareEvent extends Event<Integer> {
	public ShareEvent(int userId) {
		super(EventType.SHARE, userId);
	}
}
