package com.buding.hall.module.task.vo;

public class PlayerCoinVo {
	private int userId;
	private String gameId;
	private int coin;

	public PlayerCoinVo(int userId, int coin,String gameId) {
		this.userId = userId;
		this.gameId = gameId;
		this.coin = coin;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
}
