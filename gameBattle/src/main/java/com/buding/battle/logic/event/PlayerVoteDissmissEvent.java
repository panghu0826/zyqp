package com.buding.battle.logic.event;

public class PlayerVoteDissmissEvent extends DeskEvent {
	public int playerId;
	public int position;
	public boolean agree;
}
