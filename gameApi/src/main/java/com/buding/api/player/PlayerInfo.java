package com.buding.api.player;

import java.io.Serializable;


public class PlayerInfo implements Serializable {
	private static final long serialVersionUID = -8011061375263995942L;
	
	public int playerId;// /玩家唯一id
	public int position = -1; // /玩家在桌子上的位置
	public int coin = 10000; // 玩家金币
	public String name;
	public String headImg;
	public int gender; // 0 女 1男
	public int fanka;
	public int bindedMobile; // 0 没有 1已绑定
	public int userType; // 0 游客, 其它待定
	public int vipType; // 0普通用户,其它待定
	public int status = 0;
	public int integral = 0; // 积分
	public int zongzi = 0; // 粽子
	public String roomId; // 当前房间id
	public String privateRoomId;// 专属房间id
	public int diamond;//钻石
	
	public transient int robot = 0; // //1表示机器人
	public transient int score = 0; //积分, 多局游戏计分用到,总积分
	public transient int curJuScore = 0; //单局积分, 多局游戏计分用到

	public int kpzCount = 0; // 开牌炸的次数
	public int mobaoCount = 0; // 摸宝的次数
	public int baozhongbaoCount = 0; // 宝中宝次数
	public int zhuangCount = 0; // 做庄次数
	public int dianpaoCount = 0; // 点炮次数
	public boolean hangup = false;

	//-----------------poker---------------------
	public int robNum;//玩家抢地主多少分
	public int multiple = 1; //玩家倍数
	public boolean isOnline = true; //玩家是否在线

	public boolean isQiPai = false; //玩家是否弃牌
	public boolean isKanPai = false; //玩家是否看牌
	public int danZhu = 0; //扎金花 玩家的单注(看牌了 单注翻倍)
	public int xiQian = 0; //玩家豹子的喜钱
	public int realXiQian = 0; //玩家真是喜钱(减去别人的喜钱)
	public boolean isLose = false; //玩家扎金花比牌输了
	public int chouMa = 0; //玩家下注
	public boolean isWait = true;//是否等待下一把开始
    public int gameCount = 0;//每个人的游戏局数,每局结算时增加
    public int startGameCount = 0;//每个人的游戏局数,每局发牌时增加
    public boolean isZanLi = false;//玩家暂离
    public boolean isXiaZhu = false;//杰克/牛牛是否下注
    public int yanPaiResult = -1;//杰克/牛牛闲家验牌结果 -1:未验牌,1:赢,2:输,3:平局
    public int yaWuXiaoLong = -1;//杰克闲家压五小龙统计 -1:不需要压或未曾押,0:压,1:不压
    public boolean isTingPai = false;//杰克是否停牌
    public boolean isBeiYanPai = false;//杰克是否被庄家验牌
    public boolean isKaiPai = false;//28是否开牌
	public int nnRobotNum = -2;//牛牛是否抢地主,-2:还没提示抢,-1:不抢,0:抢(经典抢),1~n:抢的倍数(明牌抢)


	public void Reset() {
		robNum = 0;//玩家抢地主多少分
		multiple = 1; //玩家倍数
		isOnline = true; //玩家是否在线
		danZhu = 0;
		isQiPai = false; //玩家是否弃牌
		isKanPai = false; //玩家是否看牌
		xiQian = 0; //玩家豹子的喜钱
		realXiQian = 0; //玩家豹子的喜钱
		isLose = false; //玩家扎金花比牌输了
		chouMa = 0; //玩家下注
		isWait = true;//是否等待下一把开始
		gameCount = 0;
		startGameCount = 0;
		score = 0;
		isZanLi = false;
		isXiaZhu = false;
		yanPaiResult = -1;
		yaWuXiaoLong = -1;
		isTingPai = false;
		isBeiYanPai = false;
		nnRobotNum = -2;
		curJuScore = 0;
		isKaiPai = false;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHeadImg() {
		return headImg;
	}

	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getFanka() {
		return fanka;
	}

	public void setFanka(int fanka) {
		this.fanka = fanka;
	}

	public int getBindedMobile() {
		return bindedMobile;
	}

	public void setBindedMobile(int bindedMobile) {
		this.bindedMobile = bindedMobile;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public int getVipType() {
		return vipType;
	}

	public void setVipType(int vipType) {
		this.vipType = vipType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public int getZongzi() {
		return zongzi;
	}

	public void setZongzi(int zongzi) {
		this.zongzi = zongzi;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getPrivateRoomId() {
		return privateRoomId;
	}

	public void setPrivateRoomId(String privateRoomId) {
		this.privateRoomId = privateRoomId;
	}

	public int getDiamond() {
		return diamond;
	}

	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}

	public int getRobot() {
		return robot;
	}

	public void setRobot(int robot) {
		this.robot = robot;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getKpzCount() {
		return kpzCount;
	}

	public void setKpzCount(int kpzCount) {
		this.kpzCount = kpzCount;
	}

	public int getMobaoCount() {
		return mobaoCount;
	}

	public void setMobaoCount(int mobaoCount) {
		this.mobaoCount = mobaoCount;
	}

	public int getBaozhongbaoCount() {
		return baozhongbaoCount;
	}

	public void setBaozhongbaoCount(int baozhongbaoCount) {
		this.baozhongbaoCount = baozhongbaoCount;
	}

	public int getZhuangCount() {
		return zhuangCount;
	}

	public void setZhuangCount(int zhuangCount) {
		this.zhuangCount = zhuangCount;
	}

	public int getDianpaoCount() {
		return dianpaoCount;
	}

	public void setDianpaoCount(int dianpaoCount) {
		this.dianpaoCount = dianpaoCount;
	}

	public boolean isHangup() {
		return hangup;
	}

	public void setHangup(boolean hangup) {
		this.hangup = hangup;
	}

	public int getRobNum() {
		return robNum;
	}

	public void setRobNum(int robNum) {
		this.robNum = robNum;
	}

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean online) {
		isOnline = online;
	}

	public boolean isQiPai() {
		return isQiPai;
	}

	public void setQiPai(boolean qiPai) {
		isQiPai = qiPai;
	}

	public boolean isKanPai() {
		return isKanPai;
	}

	public void setKanPai(boolean kanPai) {
		isKanPai = kanPai;
	}

	public int getDanZhu() {
		return danZhu;
	}

	public void setDanZhu(int danZhu) {
		this.danZhu = danZhu;
	}

	public int getXiQian() {
		return xiQian;
	}

	public void setXiQian(int xiQian) {
		this.xiQian = xiQian;
	}

	public int getRealXiQian() {
		return realXiQian;
	}

	public void setRealXiQian(int realXiQian) {
		this.realXiQian = realXiQian;
	}

	public boolean isLose() {
		return isLose;
	}

	public void setLose(boolean lose) {
		isLose = lose;
	}

	public int getChouMa() {
		return chouMa;
	}

	public void setChouMa(int chouMa) {
		this.chouMa = chouMa;
	}

	public boolean isWait() {
		return isWait;
	}

	public void setWait(boolean wait) {
		isWait = wait;
	}

	public int getGameCount() {
		return gameCount;
	}

	public void setGameCount(int gameCount) {
		this.gameCount = gameCount;
	}

	public boolean isZanLi() {
		return isZanLi;
	}

	public void setZanLi(boolean zanLi) {
		isZanLi = zanLi;
	}

	public boolean isXiaZhu() {
		return isXiaZhu;
	}

	public void setXiaZhu(boolean xiaZhu) {
		isXiaZhu = xiaZhu;
	}

	public int getYanPaiResult() {
		return yanPaiResult;
	}

	public void setYanPaiResult(int yanPaiResult) {
		this.yanPaiResult = yanPaiResult;
	}

	public int getYaWuXiaoLong() {
		return yaWuXiaoLong;
	}

	public void setYaWuXiaoLong(int yaWuXiaoLong) {
		this.yaWuXiaoLong = yaWuXiaoLong;
	}

	public boolean isTingPai() {
		return isTingPai;
	}

	public void setTingPai(boolean tingPai) {
		isTingPai = tingPai;
	}

	public boolean isBeiYanPai() {
		return isBeiYanPai;
	}

	public void setBeiYanPai(boolean beiYanPai) {
		isBeiYanPai = beiYanPai;
	}

	public int getCurJuScore() {
		return curJuScore;
	}

	public void setCurJuScore(int curJuScore) {
		this.curJuScore = curJuScore;
	}

	public int getStartGameCount() {
		return startGameCount;
	}

	public void setStartGameCount(int startGameCount) {
		this.startGameCount = startGameCount;
	}

	public int getNnRobotNum() {
		return nnRobotNum;
	}

	public void setNnRobotNum(int nnRobotNum) {
		this.nnRobotNum = nnRobotNum;
	}

	public boolean isKaiPai() {
		return isKaiPai;
	}

	public void setKaiPai(boolean kaiPai) {
		isKaiPai = kaiPai;
	}

	@Override
	public String toString() {
		return "PlayerInfo{" +
				"playerId=" + playerId +
				", position=" + position +
				", coin=" + coin +
				", name='" + name + '\'' +
				", headImg='" + headImg + '\'' +
				", gender=" + gender +
				", fanka=" + fanka +
				", bindedMobile=" + bindedMobile +
				", userType=" + userType +
				", vipType=" + vipType +
				", status=" + status +
				", integral=" + integral +
				", zongzi=" + zongzi +
				", roomId='" + roomId + '\'' +
				", privateRoomId='" + privateRoomId + '\'' +
				", diamond=" + diamond +
				", robot=" + robot +
				", score=" + score +
				", curJuScore=" + curJuScore +
				", kpzCount=" + kpzCount +
				", mobaoCount=" + mobaoCount +
				", baozhongbaoCount=" + baozhongbaoCount +
				", zhuangCount=" + zhuangCount +
				", dianpaoCount=" + dianpaoCount +
				", hangup=" + hangup +
				", robNum=" + robNum +
				", multiple=" + multiple +
				", isOnline=" + isOnline +
				", isQiPai=" + isQiPai +
				", isKanPai=" + isKanPai +
				", danZhu=" + danZhu +
				", xiQian=" + xiQian +
				", realXiQian=" + realXiQian +
				", isLose=" + isLose +
				", chouMa=" + chouMa +
				", isWait=" + isWait +
				", gameCount=" + gameCount +
				", startGameCount=" + startGameCount +
				", isZanLi=" + isZanLi +
				", isXiaZhu=" + isXiaZhu +
				", yanPaiResult=" + yanPaiResult +
				", yaWuXiaoLong=" + yaWuXiaoLong +
				", isTingPai=" + isTingPai +
				", isBeiYanPai=" + isBeiYanPai +
				", isKaiPai=" + isKaiPai +
				", nnRobotNum=" + nnRobotNum +
				'}';
	}

}