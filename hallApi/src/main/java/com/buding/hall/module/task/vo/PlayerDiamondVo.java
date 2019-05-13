package com.buding.hall.module.task.vo;

public class PlayerDiamondVo {
	private int userId;
	private String gameId;
	private int diamond;

	public PlayerDiamondVo(int userId, int diamond, String gameId) {
		this.userId = userId;
		this.gameId = gameId;
		this.diamond = diamond;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public int getDiamond() {
		return diamond;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
}
