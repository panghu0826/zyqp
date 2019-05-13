package com.buding.poker.common;

import com.buding.common.CardDealer;
import com.buding.poker.constants.PokerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * poker 洗牌
 *
 * @author Jaime
 */
public class PokerErBaCardDealer extends CardDealer {

    public static void main(String[] args) {
//        PokerErBaCardDealer z = new PokerErBaCardDealer();
//        z.mGameData = new GameData();
//        z.mGameData.mDeskCard = new GamePacket.MyGame_DeskCard();
//        z.washCards();
        List<Byte> l1 = new ArrayList<>();
        l1.add((byte)0x28);
        l1.add((byte)0x28);
        l1.add((byte)0x29);
        l1.add((byte)0x29);

        List<Byte> l2 = new ArrayList<>();
        l2.add((byte)0x28);
        l2.add((byte)0x29);

        l1.remove(Byte.valueOf((byte) 0x29));

        System.out.println(l1);
    }

    @Override
    public void dealCard() {
        //洗牌
        this.washCards();
    }

    // 洗牌
    @Override
    public void washCards() {
        List<Byte> cards = new ArrayList<Byte>();
        for (int j = 1; j <= 9; j++) {
            for (int i = 0; i < 4; i++) {
                int ib = (0x2 << PokerConstants.POKER_CODE_COLOR_SHIFTS) + j;
                byte b = (byte) (ib & 0xff);
                cards.add(b);
            }
        }

        for (int i = 0; i < 4; i++) {
            cards.add(PokerConstants.MJ_CODE_HONG_ZHONG);
        }

        cards = new CardWasher().wash(cards);

        if (mDesk.getErBaGameType() == PokerConstants.ERBA_GAME_TYPE_CHUAN_TONG) {
            for (Byte card : this.mGameData.mDeskCard.cards) {
                cards.remove(card);
            }
        } else {
            this.mGameData.mDeskCard.reset();
        }

        this.mGameData.mDeskCard.cards.addAll(cards);
    }
}
