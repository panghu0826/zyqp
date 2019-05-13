package com.buding.game;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.ddz.DDZProcessor;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class GameData extends GameDataBase {
	private Logger log = LogManager.getLogger(getClass());

	public GameData() {
		super();
		this.Reset();
	}

	/*
	 * 除了玩家信息的其它所有数据重置
	 */
	public void Reset() {
		handStartTime = 0;
		handEndTime = 0;
		baoChangeNum = 0;
		currentOpertaionPlayerIndex = 0;
		currentCard = 0;
		cardOpPlayerIndex = 0;
		waitingStartTime = 0;
		tingpls.clear();
		state = PokerConstants.TABLE_STATE_INVALID;
		playSubstate = -1;
		dice1 = 0;
		dice2 = 0;
		gameSeq = 0;
		sleepTo = 0;
		seq = new AtomicInteger(1);

		//-------------------------Poker------------------------
		prevCardType = -1;
		prevIndex = - 1;
		prevCards = new ArrayList<>();
		pokerOpPlayerIndex = -1;
		countNum = 0;
		currentRobIndex = -1;
		unnatural = 0;
		yellowPile = 0;
		bottomFraction = 0;
		spring = 1;
		robOutCard = 0;
		currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
		cardMap = new HashMap<>();
		cardNum = new HashMap<>();
		cardInDeskMap = initCardInDeskMap();
		canAutoOper = true;
		lastplaySubstate = -1;
		netxDoublePlayerIsDiZhu = false;
		lunNum = 1;
		danZhu = 0;
		zongZhu = 0;
        lastActionPlayerPos = -2;
		isJiaZhu = false;
		isJiaoDiZhu =  false;
		jiaoOrQiangDiZhuNum =  0;
		notJiaoDiZhuNum =  0;
		currentJiaoOrRobIndex = -1;
		qiangZhuangNum = 0;
		onlyZhuangYanPai = false;
		erBaCurrentGamingPlayers.clear();
        qiangZhuangMap.clear();
        chouMaMap.clear();
        trandition28UserChouMaMap.clear();
		erBaLiangPaiMap.clear();
        siMenChouMaMap.clear();
		erBaSettleType = true;
        super.Reset();
		
		
	}


	//------------------------------------牌类-------------------------------------

	//上家的牌型(具体看....)
	public boolean canAutoOper = true;
	//上家的牌型(具体看....)
	public int prevCardType = -1;
	//上家的牌
	public List<Byte> prevCards = new ArrayList<>();
	//上家出牌玩家的下标
	public int prevIndex = - 1;
	//当前打牌的玩家
	private int pokerOpPlayerIndex = -1;
	//当前打牌的玩家ID,28杠用
	private int pokerOpPlayerId = -1;
	//记录是否过牌 (连续过牌就++ 下一次出牌就置为零)(抢地主阶段也可以复用)
	public int countNum = 0;
	//地主当前的坐标
	public int currentRobIndex = -1;
	//上个操作的玩家位置
	public int lastActionPlayerPos = -2;
	//下一个是地主加倍
	public boolean netxDoublePlayerIsDiZhu = false;
	//记录下把游戏谁先开始喊地主(此参数不用每次洗牌初始化)
	//扎金花,记录庄家位置
	//28杠,记录庄家id
	public int robIndex = 0;
	//扎金花,第几轮
	public int lunNum = 1;
	//扎金花,当前桌子单注
	public int danZhu = 0;
	//扎金花,当前桌子单注
	public int zongZhu = 0;
	//扎金花,当前桌子是否有人加注过
	public boolean isJiaZhu = false;
	//记录当前牌型是否符合
	public int unnatural = 0;
	//是否黄庄
	public int yellowPile =  0;
	//底分
	public int bottomFraction = 0;
	//春天
	public int spring = 1;
	//统计地主出牌次数
	public int robOutCard = 0;
	//当前桌子的操作 用于断线重连
	public int currentDeskState = PokerConstants.POKER_TABLE_SUB_STATE_ROBOT_BANKER;
	//存放玩家具体大小王和二的张数
	public Map<Integer,Map<Integer,Integer>> cardMap = new HashMap<>();
	//存放玩家手牌大小王和二的张数总和
	public Map<Integer,Integer> cardNum = new HashMap<>();
	//存放当前桌面牌,玩家位置--牌值
	public Map<Integer,List<Integer>> cardInDeskMap = initCardInDeskMap();
	//福利玩家
	public Map<Integer,List<Integer>> fuliPlayerMap = new HashMap<>();

	//叫/抢地主人位置(二人)
	public int currentJiaoOrRobIndex = -1;
	//是否叫过地主(二人)
	public boolean isJiaoDiZhu =  false;
	//叫/抢地主次数(二人)
	public int jiaoOrQiangDiZhuNum =  0;
	//不叫地主次数(二人)
	public int notJiaoDiZhuNum =  0;

	//牛牛抢庄倍数
	public int qiangZhuangNum = 0;

	// jack庄停牌直接到结账单
	public boolean onlyZhuangYanPai = false;

	public List<PlayerInfo> erBaCurrentGamingPlayers = new ArrayList<>();

	public List<Integer> erBaAllGamingPlayers = new ArrayList<>();

	// 28杠,记录玩家抢庄操作 玩家id - 操作(1:抢,2:不抢)
    public ConcurrentHashMap<Integer, Integer> qiangZhuangMap = new ConcurrentHashMap<>();

	// 经典28+疯狂28 ,记录玩家下注 玩家id - 筹码
    public ConcurrentHashMap<Integer, Integer> chouMaMap = new ConcurrentHashMap<>();

	// 传统28 ,记录玩家下注 玩家id - 筹码map(四门类型, 筹码)
    public ConcurrentHashMap<Integer, Map<Integer, Integer>> trandition28UserChouMaMap = new ConcurrentHashMap<>();

	// 传统28 ,记录四门筹码 四门类型, 筹码
    public Map<Integer, Integer> siMenChouMaMap = new ConcurrentHashMap<>();

    // 28亮牌情况,看牌看哪个位置亮哪个 玩家id(传统的是四门类型) - (牌位置(0,1) - 亮牌情况)
	public ConcurrentHashMap<Integer,Map<Integer,Boolean>> erBaLiangPaiMap = new ConcurrentHashMap<>();

	// 28是否正常结算
	public boolean erBaSettleType = true;


    // 28扣钻石情况,在第一次下注时扣钻石
	public ConcurrentHashMap<Integer,Boolean> erBaDiamondMap = new ConcurrentHashMap<>();

	public Map<Integer,List<Integer>> initCardInDeskMap() {
        Map<Integer,List<Integer>> cardInDeskMap = new HashMap<>();
        cardInDeskMap.put(0,new ArrayList<>());
        cardInDeskMap.put(1,new ArrayList<>());
        cardInDeskMap.put(2,new ArrayList<>());
        return cardInDeskMap;
    }
    //-----------------------------------------------------------------------------


	// 已玩局数
	public int handNum = 0;

	// 当前第几圈了
	public int quanNum = 1;//
	
	// 总共多少圈的房间
//	public int quanTotal = 0;

	// 本局开始时间-结束时间
	public long handStartTime = 0L;
	public long handEndTime = 0L;
	private int baoChangeNum = 0;// 宝牌换了多少次了；

	private int currentOpertaionPlayerIndex;// 当前操作玩家位置 (准确定义是 检测到有人有操作时,按顺序检测提醒当前玩家操作的位置)

	public byte currentCard = 0; // 当前打/自明杠出来的牌(准确定义是 检测别人是否有操作的牌)
	private int cardOpPlayerIndex = 0; // 当前打牌/自明杠牌的玩家(准确定义是 检测别人是否有操作时,当前打牌玩家)
	private long waitingStartTime = 0L;
	public long startChangeBaoTime = 0;

	// 听牌的玩家列表按听牌顺序存放
	private List<PlayerInfo> tingpls = new ArrayList<PlayerInfo>();

	private int state = PokerConstants.TABLE_STATE_INVALID;
	private int playSubstate = -1;// 玩牌的时候的一些子状态，比如等待客户播动画，或者无操作
	private int lastplaySubstate = -1;// 有玩家离线时记录桌子的最后一个状态,方便恢复
	public boolean replaying = false; //是否是回放局
	public int dice1 = 0; //骰子1
	public int dice2 = 0; //骰子2
	public int gameSeq = 0;
	public long showInitCardTime = 0;

	public long sleepTo = 0;
	public AtomicInteger seq = new AtomicInteger(1);
	
	public int genSeq() {
		return seq.getAndIncrement(); 
	}

	public void add_Down_cards(byte card) {
		for (int i = 0; i < this.mDeskCard.down_cards.size(); i++) {
			int cardvalueandnum = this.mDeskCard.down_cards.get(i).intValue();
			int cardvalue = (cardvalueandnum & 0xff);
			if (((byte) (cardvalue & 0xff)) == card) {
				int num = (cardvalueandnum >> 8) + 1;
				cardvalueandnum = (num << 8) | cardvalue;
				this.mDeskCard.down_cards.set(i, cardvalueandnum);
				break;
			}
		}
	}

    public int getPokerOpPlayerId() {
        return pokerOpPlayerId;
    }

    public void setPokerOpPlayerId(int pokerOpPlayerId) {
        this.pokerOpPlayerId = pokerOpPlayerId;
    }

    public int getPokerOpPlayerIndex() {
		return pokerOpPlayerIndex;
	}

	public void setPokerOpPlayerIndex(int pokerOpPlayerIndex) {
		this.pokerOpPlayerIndex = pokerOpPlayerIndex;
	}

	public void setWaitingStartTime(long waitingStartTime) {
		this.waitingStartTime = waitingStartTime;
	}

	public long getWaitingStartTime() {
		return waitingStartTime;
	}

	public int getPlaySubstate() {
		return playSubstate;
	}

	public void setPlaySubstate(int playSubstate) {
		this.playSubstate = playSubstate;
	}

	public int getLastplaySubstate() {
		return lastplaySubstate;
	}

	public void setLastplaySubstate(int lsatplaySubstate) {
		this.lastplaySubstate = lsatplaySubstate;
	}

	public Byte getCurrentCard() {
		return currentCard;
	}

	public int getCardOpPlayerIndex() {
		return cardOpPlayerIndex;
	}

	public void move2NextPlayer(MJDesk desk) {
		List<PlayerInfo> loopGetPlayer = desk.loopGetPlayer(currentOpertaionPlayerIndex, 1, 0);
		currentOpertaionPlayerIndex = loopGetPlayer.get(0).position;
	}
	
	public int getBaoChangeNum() {
		return baoChangeNum;
	}

	public void setBaoChangeNum(int baoChangeNum) {
		this.baoChangeNum = baoChangeNum;
	}

	// 最后一个牌能胡
	public boolean isNextInFinalStage() {
		int iNum = 12;

		// 如果是换过宝，就多一只
		if (this.getBaoChangeNum() % 2 == 1) {
			iNum = 13;
		}

		if (getCardLeftNum() <= (iNum - 1))
			return true;

		return false;
	}

	// 是否已经进入最后阶段，最后要剩8，9张牌
	public boolean isInFinalStage() {
		int iNum = 12;

		// 如果是换过宝，就多一只
		if (this.getBaoChangeNum() % 2 == 1) {
			iNum = 13;
		}

		if (getCardLeftNum() <= iNum)
			return true;

		return false;
	}

	// 还剩多少张牌
	public int getCardLeftNum() {
		int num = this.mDeskCard.cards.size();
		return num;
	}

	public void setCurrentCard(Byte currentCard) {
		this.currentCard = currentCard;
	}

	public void setCardOpPlayerIndex(int cardOpPlayerIndex) {
		this.cardOpPlayerIndex = cardOpPlayerIndex;
	}

	// 摸一张牌给玩家
	public byte popCard() {
		Byte b = 0;
		if (this.mDeskCard.cards.size() > 0)
			b = this.mDeskCard.cards.remove(0);

		return b;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}


	public List<Byte> getCardsInHand(int position) {
		return this.mPlayerCards[position].cardsInHand;
	}

	public List<Integer> getCardsDown(int position) {
		return this.mPlayerCards[position].cardsDown;
	}

	public boolean isPengCard(byte b, int position) {
		for (int i = 0; i < getCardsDown(position).size(); i++) {
			int bb = getCardsDown(position).get(i);
			byte b1 = (byte) (bb & 0xff);
			byte b2 = (byte) ((bb >> 8) & 0xff);
			byte b3 = (byte) ((bb >> 16) & 0xff);

			if (b1 == b && b2 == b && b3 == b) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 增加一组牌
	 *
	 * @param listCard
	 * @param position
	 *
	 */
	public void addCardsInHand(List<Byte> listCard, int position) {
		if (listCard == null) return;
		log.info("act=addCardsInHand;position={};card={}", position, listCard);
		List<Byte> cardsInHand = new ArrayList<>(getCardsInHand(position));
		cardsInHand.addAll(listCard);
        this.mPlayerCards[position].cardsInHand = DDZProcessor.sortHandCards(cardsInHand);
	}

	//从玩家手中删除一组牌
	public boolean removeCardInHand(List<Byte> cardsList, int position, CardChangeReason reason) {
		if (cardsList == null) {
			return false;
		}

		log.info("act=removeCardInHand;reason={};position={};cards={}", reason, position, cardsList);
		List<Byte> cardsInHand = new ArrayList<>(getCardsInHand(position));
		if (!cardsInHand.containsAll(cardsList)) {
			return false;
		}

		for (Byte b : cardsList) {
			cardsInHand.remove(b);
		}

        this.mPlayerCards[position].cardsInHand = DDZProcessor.sortHandCards(cardsInHand);
		return true;
	}

	// 玩家手里有几张牌
	public int getCardNumInHand(int position) {
		return getCardsInHand(position).size();
	}


	// 在玩家门前放一张牌
	public void addCardBefore(Byte b, int position) {
		getCardsBefore(position).add(b);
	}

	public List<Byte> getCardsBefore(int position) {
		return this.mPlayerCards[position].cardsBefore;
	}

	public String dump() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(recorder);
	}

}
