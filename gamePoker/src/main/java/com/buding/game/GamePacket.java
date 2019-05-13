package com.buding.game;

import com.buding.poker.common.NNBiPaiResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class GamePacket {
	static public class MyGame_Actor {
		// 当前游戏状态
		public int gameState = -1;
		public int sequence = -1;
		public int lastActor = -1; // 最近一次操作玩家
		public int lastOutCardType = -1;
		public int currentActor = -1; // 当前出牌玩家
		public int timerInterval = -1; // 操作超时时间
	}

	static public class MyGame_PublicInfo {
		public int mbankerPos = -1; // 庄家位置
		public int mBankerUserId = -1;// 庄家id
		public boolean isContinueBanker = false; // 是否连庄
	}

	static public class MyGame_DeskCard {
		// 未摸的牌
		public List<Byte> cards = new ArrayList<Byte>();

		// 打下来的牌
		public List<Integer> down_cards = new ArrayList<Integer>();
        public List<Byte> ddzCards = new ArrayList<>();

        public void reset() {
			cards.clear();
			down_cards.clear();
			ddzCards.clear();
		}
	}

	static public class MyGame_Player_Cards {
		public int position = -1;
		public List<Byte> cardsInHand = new ArrayList<>();// 手牌
		public NNBiPaiResult nnBiPaiResult = new NNBiPaiResult();
		public int cardType = -1;// 牌类型
		public int cardNum = -1;// 牌值
		public List<Byte> cardsBefore = new ArrayList<>();// 打出的牌
		public transient List<Integer> cardsDown = new ArrayList<>();// 吃碰杠的牌
	}

	static public class MyGame_Player_Action {
		public byte cardGrab = 0;
		public int autoOperation = 0;
		public long opStartTime = 0;

		public void reset() {
			this.cardGrab = 0;
			this.opStartTime = 0;
		}
	}

	static public class MyGame_Player_Win {
		// 胡牌/赢得人玩家位置
		public int position = -1;
		public int playerId = -1;
		public byte huCard = -1;

		public void reset() {
			position = -1;
			huCard = -1;
		}
	}
}
