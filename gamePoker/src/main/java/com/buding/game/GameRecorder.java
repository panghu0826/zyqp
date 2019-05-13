package com.buding.game;

import com.buding.api.player.*;
import com.buding.poker.helper.*;
import com.buding.poker.model.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRecorder {
	public List<Card> initCards = new ArrayList<Card>();
	public Map<Integer, List<Card>> playerInitCards = new HashMap<Integer, List<Card>>();
	public List<Action> actionList = new ArrayList<Action>();
	public boolean isContinueBanker = false;
	public int bankerPos = -1;
	public int bankerUserId = -1;
	public int seq = -1;
	public String deskId;
	public String gameId;
	public String matchId;
	public int juNum;
	public int wanfa;
	public int limitMax;
	public int menNum;
	public int yaZhu;
	public int erBaGameType;
	public PlayerInfo[] players;
	public List<RecordData> data;

	public void reset() {
		this.initCards.clear();
		this.playerInitCards.clear();
		this.actionList.clear();
	}
		
	public void recordBasicInfo(GameData gt) {
		isContinueBanker = gt.mPublic.isContinueBanker;
		bankerPos = gt.mPublic.mbankerPos;
		bankerUserId = gt.mPublic.mBankerUserId;
	}

	//============================================   DDZ  ========================================================
	public void recordDDZPlayerAction(int seq, int position, int code, List<Integer> cards, int robNum, int cardType) {
		Action a = new Action(seq, position, code,cards,robNum,cardType);
		actionList.add(a);
	}

    public void recordDDZGameStart(PlayerInfo[] players, List<Byte> initCards) {
        for(byte b : initCards) {
            this.initCards.add(new Card(b, DDZHelper.getSingleCardName(b)));
        }
        this.players = players;
    }

    public void recordDDZPlayerCard(List<Byte> cards , int position) {
        List<Card> initCards = new ArrayList<Card>();
        for(byte b : cards) {
            initCards.add(new Card(b, DDZHelper.getSingleCardName(b)));
        }
        playerInitCards.put(position, initCards);
    }

	//============================================   ZJH  ========================================================
	public void recordZJHPlayerAction(int seq,int position, int code,int biPaiPos,int biPaiWinnerPos,int chouMa,int playerZongZhu,int deskZongZhu,int cardType,int deskDanZhu,int lunNum) {
		Action a = new Action(seq,position,code,biPaiPos,biPaiWinnerPos,chouMa,playerZongZhu,deskZongZhu,cardType,deskDanZhu,lunNum);
		actionList.add(a);
	}

    public void recordZJHGameStart(PlayerInfo[] players,List<RecordData> data, List<Byte> initCards) {
        for(byte b : initCards) {
            this.initCards.add(new Card(b, ZJHHelper.getSingleCardName(b)));
        }
        this.players = players;
        this.data = data;
    }

    public void recordZJHPlayerCard(List<Byte> cards , int position) {
        List<Card> initCards = new ArrayList<Card>();
        for(byte b : cards) {
            initCards.add(new Card(b, ZJHHelper.getSingleCardName(b)));
        }
        playerInitCards.put(position, initCards);
    }

    //============================================   JACK  ========================================================
	public void recordJACKPlayerAction(int seq,int position, int code,int biPaiPos,int biPaiWinnerPos,int chouMa,int cardNum,int cardType,List<Integer> cardsInHand,List<JACKPLayerHandRecordData> playerHandCards) {
		Action a = new Action(seq,position,code,biPaiPos,biPaiWinnerPos,chouMa,cardNum,cardType,cardsInHand,playerHandCards);
		actionList.add(a);
	}

    public void recordJACKGameStart(PlayerInfo[] players, List<RecordData> data, List<Byte> initCards) {
        for(byte b : initCards) {
            this.initCards.add(new Card(b, JACKHelper.getSingleCardName(b)));
        }
        this.players = players;
        this.data = data;
    }

    public void recordJACKPlayerCard(List<Byte> cards , int position) {
        List<Card> initCards = new ArrayList<>();
        for(byte b : cards) {
            initCards.add(new Card(b, JACKHelper.getSingleCardName(b)));
        }
        playerInitCards.put(position, initCards);
    }

    //============================================   28  ========================================================
	public void recordErBaPlayerAction(int seq, int playerId, int code, int chouMa, List<PlayerSiMenChouMa> chuanTongErBaChouMa, List<SiMenChouMa> siMenChouMas, int siMen, int cardValuePos) {
		Action a = new Action(seq,playerId,code,chouMa,chuanTongErBaChouMa, siMenChouMas, siMen,cardValuePos);
		actionList.add(a);
	}

    public void recordErBaGameStart(PlayerInfo[] players, List<RecordData> data, List<Byte> initCards) {
        for(byte b : initCards) {
            this.initCards.add(new Card(b, ErBaHelper.getSingleCardName(b)));
        }
        this.players = players;
        this.data = data;
    }

    public void recordErBaPlayerCard(List<Byte> cards , int playerId) {
        List<Card> initCards = new ArrayList<>();
        for(byte b : cards) {
            initCards.add(new Card(b, ErBaHelper.getSingleCardName(b)));
        }
        playerInitCards.put(playerId, initCards);
    }


    //============================================   NN  ========================================================
    public void recordNNPlayerAction(int seq,int position, int code,int qiangZhuangNum,int chouMa) {
        Action a = new Action(seq,position,code,qiangZhuangNum,chouMa);
        actionList.add(a);
    }

    public void recordNNGameStart(PlayerInfo[] players,List<RecordData> data, List<Byte> initCards) {
        for(byte b : initCards) {
            this.initCards.add(new Card(b, NNHelper.getSingleCardName(b)));
        }
        this.players = players;
        this.data = data;
    }

    public void recordNNPlayerCard(List<Byte> cards , int position) {
        List<Card> initCards = new ArrayList<Card>();
        for(byte b : cards) {
            initCards.add(new Card(b, NNHelper.getSingleCardName(b)));
        }
        playerInitCards.put(position, initCards);
    }


}