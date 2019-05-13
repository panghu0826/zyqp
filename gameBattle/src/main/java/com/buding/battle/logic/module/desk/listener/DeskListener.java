package com.buding.battle.logic.module.desk.listener;

import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.logic.module.desk.bo.CommonDesk;

public interface DeskListener {
	public void onPlayerSit(CommonDesk<?> desk, PlayerInfo player);
	public void onPlayerReady(CommonDesk<?> desk, PlayerInfo player);
	public void onPlayerLeave(CommonDesk<?> desk, PlayerInfo player);
	public void onDeskGameStart(CommonDesk<?> desk, Game game);
	public void onDeskGameFinish(CommonDesk<?> desk, Game game);
	public void onDeskDestroy(CommonDesk<?> desk);
	public void onDeskCreate(CommonDesk<?> desk);
}
