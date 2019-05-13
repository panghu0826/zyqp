package com.buding.hall.module.share;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.hall.module.event.EventService;

@Component
public class ShareService {
	@Autowired
	EventService eventService;
	
	public void onShareFinish(int userId, int rating) {
		eventService.postShareEvent(userId, rating);
	}
}
