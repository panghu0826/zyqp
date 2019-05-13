package com.buding.hall.module.shop.channel;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ChannelRepostory {
	public Map<Integer, PayChannel> channelMap = new HashMap<Integer, PayChannel>();

	public void register(PayChannel channel) {
		this.channelMap.put(channel.getName(), channel);
	}
	
	public PayChannel getChannel(int name) {
		return this.channelMap.get(name);
	}
}
