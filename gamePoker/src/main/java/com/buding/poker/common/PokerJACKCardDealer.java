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
public class PokerJACKCardDealer extends CardDealer {

    public static void main(String[] args) {
        PokerJACKCardDealer z = new PokerJACKCardDealer();
        z.washCards();
    }

    @Override
    public void dealCard() {
        //洗牌
        this.washCards();
    }

    // 洗牌
    @Override
    public void washCards() {
        this.mGameData.mDeskCard.reset();

        List<Byte> cards = new ArrayList<Byte>();
        for (int j = 0; j < 4; j++) {
            if(mDesk.canFengKuangMode()){
                for (int i = 10; i <= 14; i++) {
                    int ib = (j << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
                    byte b = (byte) (ib & 0xff);
                    cards.add(b);
                }
            }else{
                for (int i = 2; i <= 14; i++) {
                    int ib = (j << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
                    byte b = (byte) (ib & 0xff);
                    cards.add(b);
                }
            }
        }

        if(mDesk.canWangLaiZi()) {
            cards.add(PokerConstants.POKER_CODE_XIAO_WANG);
            cards.add(PokerConstants.POKER_CODE_DA_WANG);
        }

        cards = new CardWasher().wash(cards);
        this.mGameData.mDeskCard.cards.addAll(cards);
    }
}
