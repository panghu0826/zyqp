package com.buding.hall.module.game.model;

public class CLubWanfaModel {
    public String gameID;   //游戏id
    public String matchID;   //游戏id
	public int yazhu;		//押注
	public int juNum;		//局数
	public int biMen;		//必闷
	public int playerNum;	//人数
	public int wanfa;		//玩法
	public int fengDing;	//封顶
	public int qiangZhuangNum;	//牛牛抢庄倍数,经典抢庄传0
    public String niuFanStr;   //牛番
    public int erBaGameType;	//1: 经典28,2:疯狂28,3:传统28

    public CLubWanfaModel() {
    }

    public CLubWanfaModel(String gameID, String matchID, int yazhu, int juNum, int biMen, int playerNum, int wanfa, int fengDing, int qiangZhuangNum, String niuFanStr, int erBaGameType) {
        this.gameID = gameID;
        this.matchID = matchID;
        this.yazhu = yazhu;
        this.juNum = juNum;
        this.biMen = biMen;
        this.playerNum = playerNum;
        this.wanfa = wanfa;
        this.fengDing = fengDing;
        this.qiangZhuangNum = qiangZhuangNum;
        this.niuFanStr = niuFanStr;
        this.erBaGameType = erBaGameType;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public int getYazhu() {
        return yazhu;
    }

    public void setYazhu(int yazhu) {
        this.yazhu = yazhu;
    }

    public int getJuNum() {
        return juNum;
    }

    public void setJuNum(int juNum) {
        this.juNum = juNum;
    }

    public int getBiMen() {
        return biMen;
    }

    public void setBiMen(int biMen) {
        this.biMen = biMen;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public int getWanfa() {
        return wanfa;
    }

    public void setWanfa(int wanfa) {
        this.wanfa = wanfa;
    }

    public int getFengDing() {
        return fengDing;
    }

    public void setFengDing(int fengDing) {
        this.fengDing = fengDing;
    }

    public int getQiangZhuangNum() {
        return qiangZhuangNum;
    }

    public void setQiangZhuangNum(int qiangZhuangNum) {
        this.qiangZhuangNum = qiangZhuangNum;
    }

    public String getNiuFanStr() {
        return niuFanStr;
    }

    public void setNiuFanStr(String niuFanStr) {
        this.niuFanStr = niuFanStr;
    }

    public int getErBaGameType() {
        return erBaGameType;
    }

    public void setErBaGameType(int erBaGameType) {
        this.erBaGameType = erBaGameType;
    }
}