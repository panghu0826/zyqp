package com.buding.hall.module.task.event;

import com.buding.common.event.Event;
import com.buding.hall.module.task.type.EventType;

public class LoginEvent extends Event<Integer> {
	public LoginEvent(int userId) {
		super(EventType.LOGIN, userId);
	}
}
