package com.buding.game;

import com.buding.api.player.JACKPLayerHandRecordData;
import com.buding.api.player.PlayerSiMenChouMa;
import com.buding.api.player.SiMenChouMa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 *  碰杠只传card1,  card2,card3传0
 *  吃3个都传
 *  只有吃碰杠的操作时传cardFromPosition,其余传-1
 *  direct:0,客户端-->服务端,1:服务端-->客户端
 */
public class Action {
	public int position;
	public int playerId;
	public String name;
	public int code;
	public int seq;

	//ddz
	public int robNum = 1;//底分
	public int cardType = -1;//斗地主等牌类出牌类型/扎金花的牌类型
	public List<Integer> cards; //斗地主出的牌

	//zjh
	public int biPaiPos = -1;//被比牌玩家座位
	public int biPaiWinnerPos = -1;//比牌赢家位置
	public int chouMa = -1;//跟注/加注筹码
	public int playerZongZhu = -1;//玩家总注
	public int deskZongZhu = -1;//桌子总注
	public int deskDanZhu = -1;//桌子单注
	public int lunNum = -1;//桌子轮数

	//jack
	public int cardNum = -1;//当前操作人牌值
	public List<Integer> cardsInHand = new ArrayList<>();//当前操作人牌值
	public List<JACKPLayerHandRecordData> playerHandCards = new ArrayList<>();//当前操作人牌值
	public int qiangZhuangNum = -1;//牛牛抢庄倍数

	// erba
	private List<PlayerSiMenChouMa> chuanTongErBaChouMa = new ArrayList<>();
	private List<SiMenChouMa> siMenChouMas = new ArrayList<>();
	private int siMen;
	private int cardValuePos;

	//ddz
	public Action(int seq,int position, int code,List<Integer> cards,int robNum,int cardType) {
		this.position = position;
		this.code = code;
		this.seq = seq;
		this.cards = cards;
		this.robNum = robNum;
		this.cardType = cardType;
	}

	//zjh
	public Action(int seq,int position, int code,int biPaiPos,int biPaiWinnerPos,int chouMa,int playerZongZhu,int deskZongZhu,int cardType,int deskDanZhu,int lunNum) {
		this.position = position;
		this.code = code;
		this.seq = seq;
		this.biPaiPos = biPaiPos;
		this.biPaiWinnerPos = biPaiWinnerPos;
		this.chouMa = chouMa;
		this.playerZongZhu = playerZongZhu;
		this.deskZongZhu = deskZongZhu;
		this.cardType = cardType;
		this.deskDanZhu = deskDanZhu;
		this.lunNum = lunNum;
	}

	//jack
	public Action(int seq,int position, int code,int biPaiPos,int biPaiWinnerPos,int chouMa,int cardNum,int cardType,List<Integer> cardsInHand,List<JACKPLayerHandRecordData> playerHandCards) {
		this.position = position;
		this.code = code;
		this.seq = seq;
		this.biPaiPos = biPaiPos;
		this.biPaiWinnerPos = biPaiWinnerPos;
		this.chouMa = chouMa;
		this.cardNum = cardNum;
		this.cardsInHand = cardsInHand;
		this.cardType = cardType;
		this.playerHandCards = playerHandCards;
	}

	//erba
	public Action(int seq, int playerId, int code, int chouMa, List<PlayerSiMenChouMa> chuanTongErBaChouMa, List<SiMenChouMa> siMenChouMas, int siMen, int cardValuePos) {
		this.playerId = playerId;
		this.code = code;
		this.seq = seq;
		this.chouMa = chouMa;
		this.chuanTongErBaChouMa = chuanTongErBaChouMa;
		this.siMenChouMas = siMenChouMas;
		this.siMen = siMen;
		this.cardValuePos = cardValuePos;
	}

	//nn
	public Action(int seq,int position, int code,int qiangZhuangNum,int chouMa) {
		this.position = position;
		this.code = code;
		this.seq = seq;
		this.chouMa = chouMa;
		this.qiangZhuangNum = qiangZhuangNum;
	}
}
