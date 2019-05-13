package com.buding.poker.common;

import com.buding.poker.constants.PokerConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 炸金花的Rule
 *
 * @author chen
 */
public class JACKRule {


    /**
     * 判断是否金花
     *
     * @param handCard
     * @return
     */
    public static boolean isFlush(List<Byte>  handCard){
        int a = 0 , b = 0 , c = 0 , d = 0;
        for (int i = 0 ; i < handCard.size(); i++) {
            if (handCard.get(i) <= 15) {
                a++;
            } else if (handCard.get(i) <=31) {
                b++;
            } else if (handCard.get(i) <= 47) {
                c++;
            } else if (handCard.get(i) <= 63) {
                d++;
            }
        }

        if (a >= 3) {
            return true;
        } else if (b >= 3) {
            return true;
        } else if (c >= 3) {
            return true;
        } else if (d >= 3) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为豹子
     *
     * @param handCard
     * @return
     */
    public static boolean isBmob(List<Byte>  handCard) {
        List<Byte> card = new ArrayList<>(handCard);
        card = modular(card);
        if (card.get(0) == card.get(1) && card.get(1) == card.get(card.size() - 1)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否对子
     *
     * @param handCard
     * @return
     */
    public static boolean isDouble(List<Byte>  handCard) {
        List<Byte> card = new ArrayList<>(handCard);
        card = modular(card);
        if (card.get(0) == card.get(1) || card.get(1) == card.get(card.size() - 1)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否顺子
     *
     * @param handCard
     * @return
     */
    public static boolean isStraight(List<Byte>  handCard) {
        List<Byte> card = new ArrayList<>(handCard);
        card = modular(card);
        boolean flag = true;
        for (int n = 0; n < card.size() - 1; n++) {
            int prev = card.get(n);
            int next = card.get(n + 1);
            if (prev - next != -1) {
                flag = false;
                break;
            }
        }
        if (card.get(0) == 2 && card.get(1) == 3 && card.get(card.size() - 1) == 14) {
            return true;
        }
        return flag;
    }

    /**
     * 判断是否为235
     *
     * @param handCard
     * @return
     */
    public static boolean isTwoThreeFive(List<Byte>  handCard) {
        List<Byte> card = new ArrayList<>(handCard);
        card = modular(card);
        if (card.get(0) == 2 && card.get(1) == 3 && card.get(card.size() - 1) == 5) {
            return true;
        }
        return false;
    }


    /**
     * 对牌进行取模
     *
     * @param handCard
     * @return
     */
    public static List<Byte> modular(List<Byte>  handCard) {
        List<Byte> ls = new ArrayList<Byte>();
        for (byte card : handCard) {
            if (card == PokerConstants.POKER_CODE_XIAO_WANG) {//小王
                ls.add(PokerConstants.XIAO_WANG_COLOR_VALUE);
            } else if (card == PokerConstants.POKER_CODE_DA_WANG) {//大王
                ls.add(PokerConstants.DA_WANG_COLOR_VALUE);
            } else {
                ls.add((byte) (card & 0x0f));
            }
        }
        Collections.sort(ls);
        return ls;
    }



    /**
     * 王金花的判断
     *
     * @param handCard
     * @return
     */
    public static boolean isKingFlush(List<Byte> handCard) {
        int a = 0 , b = 0 , c = 0 , d = 0;
        for (int i = 0 ; i < handCard.size(); i++) {
            if (handCard.get(i) <= 15) {
                a++;
            } else if (handCard.get(i) <=31) {
                b++;
            } else if (handCard.get(i) <= 47) {
                c++;
            } else if (handCard.get(i) <= 63) {
                d++;
            }
        }

        if (a >= 2) {
            return true;
        } else if (b >= 2) {
            return true;
        } else if (c >= 2) {
            return true;
        } else if (d >= 2) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        List<Byte> l = new ArrayList<>();
        l.add((byte) 14);
        l.add((byte)15);
        l.add((byte)3);
        System.out.println(isStraight(l));
        System.out.println(l);
    }

}
