package com.buding.api.player;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JACKPLayerHandRecordData implements Serializable {
	public int position = -1; //玩家在桌子上的位置
	public List<Integer> cardsInHand = new ArrayList<>();
	public int xiaZhu = -1;
	public int cardNum = -1;
	public int cardType = -1;
}