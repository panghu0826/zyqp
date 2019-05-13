package com.buding.hall.config;

import java.util.List;

import com.buding.hall.module.common.constants.GameStartType;


public class RoomConfig {
	//30桌
	public int deskCountLimit = 3000;
		
	public String comment;
	public String roomName;
	public String roomId;
	
	public int basePoint;
	public int low;
	public int high;
	public List<DeskFee> fee;
		
	/**
	 * @see GameStartType
	 */
	public int startType = GameStartType.QDZ;
	
	public String roomClassFullName;
	
	public boolean vistorFree = false; //客人免费
	
	public String roomType = "";
}
