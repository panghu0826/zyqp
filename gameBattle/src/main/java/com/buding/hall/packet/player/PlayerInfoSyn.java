package com.buding.hall.packet.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerInfoSyn {
	public int playerId;
	public Map<String, Object> updateDict = new HashMap<String, Object>();
}
