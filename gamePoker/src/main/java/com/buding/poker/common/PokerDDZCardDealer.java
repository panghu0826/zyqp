package com.buding.poker.common;

import com.buding.common.CardDealer;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.DDZHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * poker 洗牌
 *
 * @author Jaime
 */
public class PokerDDZCardDealer extends CardDealer {

    public static void main(String[] args) {
        PokerDDZCardDealer z = new PokerDDZCardDealer();
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

        // 大小王
        byte b1 = PokerConstants.POKER_CODE_XIAO_WANG;
        byte b2 = PokerConstants.POKER_CODE_DA_WANG;
        int num = 3;
        if(this.mDesk.getPlayerCount() == 2) num = 5;

        List<Byte> cards = new ArrayList<Byte>();
        for (int j = 0; j < 4; j++) {
            for (int i = num; i <= 15; i++) {
                int ib = (j << PokerConstants.POKER_CODE_COLOR_SHIFTS) + i;
                byte b = (byte) (ib & 0xff);
                cards.add(b);
            }
        }


        // 加入大王
        cards.add(b1);
        //加入小王
        cards.add(b2);

        cards = new CardWasher().wash(cards);
        this.mGameData.mDeskCard.cards.addAll(cards);
        System.out.println("2017/12/24 : " + DDZHelper.getSingleCardListName(this.mGameData.mDeskCard.cards));
    }



}
