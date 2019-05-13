package com.buding.poker.dualddz;

import com.buding.poker.common.DDZRule;
import com.buding.poker.constants.PokerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DualDDZProcessor {
    private static Logger logger = LogManager.getLogger(DualDDZProcessor.class);


    public DualDDZProcessor() {

    }

    /**
     * 检测牌的类型
     *
     * @param myCards
     *            我出的牌
     * @return 如果遵守规则，返回牌的类型，否则，返回 0 。
     */
    public static int getCardType(List<Byte> myCards) {
        int cardType = 0;
        if (myCards != null) {
            // 大概率事件放前边，提高命中率
            if (DDZRule.isDan(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_DAN;
            } else if (DDZRule.isDuiWang(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_WANG_ZHA;
            } else if (DDZRule.isDuiZi(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_DUI_ZI;
            } else if (DDZRule.isZhaDan(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_ZHA_DAN;
            } else if (DDZRule.isSanDaiYi(myCards) != -1) {
                cardType = PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI;
            } else if (DDZRule.isSanDaiEr(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER;
            } else if (DDZRule.isSanBuDai(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI;
            } else if (DDZRule.isShunZi(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_SHUN_ZI;
            } else if (DDZRule.isLianDui(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_LIAN_DUI;
            } else if (DDZRule.isSiDaiErDan(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_SI_DAI_ER;
            }else if (DDZRule.isSiDaiErDui(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI;
            } else if (DDZRule.isFeiJiBuDai(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI;
            } else if (DDZRule.isFeiJiDaiDan(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN;
            } else if (DDZRule.isFeiJiDaiDui(myCards)) {
                cardType = PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI;
            }
        }
        return cardType;
    }


    /**
     * 比较我的牌和上家的牌的大小，决定是否可以出牌
     *
     * @param myCards
     *            我想出的牌
     *
     * @param //myCardType
     *            我的牌的类型
     * @param prevCards
     *            上家的牌
     * @param prevCardType
     *            上家的牌型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static boolean isOvercomePrev(List<Byte> myCards,int myCardType ,List<Byte> prevCards , int prevCardType) {
        // 我的牌和上家的牌都不能为null
        if (myCards == null || prevCards == null) {
            return false;
        }

        if (myCardType == 0 || prevCardType == 0) {
            logger.info("上家出的牌不合法，所以不能出。");//这儿有问题
            return false;
        }

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = myCards.size();

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0) {
            return true;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            logger.info("上家王炸，肯定不能出。");
            return false;
        } else if (myCardType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            logger.info("我王炸，肯定能出。");
            return true;
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != PokerConstants.DDZ_CARDTYPE_ZHA_DAN && myCardType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            return true;
        }

        // 默认情况：上家和自己想出的牌都符合规则
        myCards = DDZRule.modular(myCards);// 对牌排序
        prevCards = DDZRule.modular(prevCards);// 对牌排序




        int myGrade = myCards.get(0);
        int prevGrade = prevCards.get(0);

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 单
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_DAN && myCardType == PokerConstants.DDZ_CARDTYPE_DAN) {
            // 一张牌可以大过上家的牌
            return compareCard(myGrade, prevGrade);
        }
        // 对子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_DUI_ZI
                && myCardType == PokerConstants.DDZ_CARDTYPE_DUI_ZI) {
            // 2张牌可以大过上家的牌
            return compareCard(myGrade, prevGrade);

        }
        // 3不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI
                && myCardType == PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
            return compareCard(myGrade, prevGrade);
        }
        // 炸弹
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN
                && myCardType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            // 4张牌可以大过上家的牌
            return compareCard(myGrade, prevGrade);

        }
        // 3带1
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI
                && myCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI) {

            // 3带1只需比较第2张牌的大小
            myGrade = myCards.get(1);
            prevGrade = prevCards.get(1);
            return compareCard(myGrade, prevGrade);

        }
        // 3带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER
                && myCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER) {

            // 3带2只需比较第中间张牌的大小
            myGrade = myCards.get(2);
            prevGrade = prevCards.get(2);
            return compareCard(myGrade, prevGrade);

        }
        // 4带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER
                && myCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER) {

            // 4带2只需比较第3张牌的大小
            myGrade = myCards.get(2);
            prevGrade = prevCards.get(2);
            return compareCard(myGrade, prevGrade);
        }
        // 4带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI
                && myCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI) {

            // 4带2对只需比较四张一样的排的大小
            myGrade = threeReturnOne(myCards);
            prevGrade = threeReturnOne(prevCards);
            return compareCard(myGrade, prevGrade);
        }

        // 顺子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI
                && myCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 顺子只需比较最大的1张牌的大小
                myGrade = myCards.get(mySize - 1);
                prevGrade = prevCards.get(prevSize - 1);
                return compareCard(myGrade, prevGrade);
            }
        }
        // 连对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI
                && myCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 连对只需比较最大的1张牌的大小
                myGrade = myCards.get(mySize - 1);
                prevGrade = prevCards.get(prevSize - 1);
                return compareCard(myGrade, prevGrade);
            }
        }
        // 飞机不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI
                && myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 顺子只需比较第5张牌的大小(特殊情况333444555666没有考虑，即12张的飞机，可以有2种出法)
                myGrade = myCards.get(0);
                prevGrade = prevCards.get(0);
                return compareCard(myGrade, prevGrade);
            }
        }
        // 飞机带单
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN
                && myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
            if (mySize != prevSize) {
                return false;
            } else {

                myGrade = threeReturnOne(myCards);

                prevGrade = threeReturnOne(prevCards);

                return compareCard(myGrade, prevGrade);
            }
        }
        // 飞机带对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI
                && myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI) {
            if (mySize != prevSize) {
                return false;
            } else {

                myGrade = threeReturnOne(myCards);

                prevGrade = threeReturnOne(prevCards);

                return compareCard(myGrade, prevGrade);
            }
        }

        // 默认不能出牌
        return false;
    }

    /**
     *
     * @param myGrade 我的手牌
     *
     * @param prevGrade 上家手牌
     *
     * @return 如果我的手牌大于上家返回true;
     */
    private static boolean compareCard(int myGrade, int prevGrade) {

        if (Integer.compare(myGrade,prevGrade) == 1) {
            return true;
        }

        return false;
    }


    /**
     * 判断我所有的牌中，是否存在能够管住上家的牌，决定出牌按钮是否显示
     *
     * @param myCards
     *            我所有的牌 *
     * @param prevCards
     *            上家的牌
     * @param prevCardType
     *            上家牌的类型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static boolean isOverPrompt(List<Byte> myCards,List<Byte> prevCards, int prevCardType) {
        List<List<Byte>> lists = new ArrayList<>();
        // 我的牌和上家的牌都不能为null
        if (myCards == null || prevCards == null) {
            return false;
        }

        if (prevCardType == 0) {
            System.out.println("上家出的牌不合法，所以不能出。");
            return false;
        }

        // 默认情况：上家和自己想出的牌都符合规则
        myCards = DDZRule.modular(myCards);
        prevCards = DDZRule.modular(prevCards);

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = myCards.size();

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0) {
            return true;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            System.out.println("上家王炸，肯定不能出。");
            return false;
        }

        if (mySize >= 2) {
            List<Byte> cards = new ArrayList<Byte>();
            cards.add(myCards.get(mySize -1));
            cards.add(myCards.get(mySize -2));
            if (DDZRule.isDuiWang(cards)) {
                return true;
            }
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            if (mySize < 4) {
//                return false;
            } else {
                for (int i = 0; i < mySize - 3; i++) {
                    int grade0 = myCards.get(i);
                    int grade1 = myCards.get(i + 1);
                    int grade2 = myCards.get(i + 2);
                    int grade3 = myCards.get(i + 3);

                    if (grade1 == grade0 && grade2 == grade0
                            && grade3 == grade0) {
                        return true;
                    }
                }
            }

        }

        int prevGrade = prevCards.get(0);

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 上家出单
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_DAN) {
            // 一张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 0; i--) {
                int grade = myCards.get(i);
                if (compareCard(grade,prevGrade)) {
                    // 只要有1张牌可以大过上家，则返回true
                    return true;
                }
            }

        }
        // 上家出对子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_DUI_ZI) {
            // 2张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 1; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);

                if (grade0 == grade1) {
                    if (compareCard(grade0,prevGrade)) {
                        // 只要有1对牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出3不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);

                if (grade0 == grade1 && grade0 == grade2) {
                    if (compareCard(grade0,prevGrade)) {
                        // 只要3张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出3带1
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI) {
            // 3带1 3不带 比较只多了一个判断条件
            if (mySize < 4) {
                return false;
            }

            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);

                if (grade0 == grade1 && grade0 == grade2) {
                    prevGrade = threeReturnOne(prevCards);
                    if (compareCard(grade0,prevGrade)) {
                        // 只要3张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }
        }
        // 上家出3带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER) {
            // 3带2 3不带 比较只多了一个判断条件
            if (mySize < 5) {
                return false;
            }
            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);
                //三张一样的牌 然后看剩下的牌中有没有对子 有返回true 没有返回false
                if (grade0 == grade1 && grade0 == grade2) {
                    List<Byte> list = new ArrayList<>(myCards);
                    list.remove(i);
                    list.remove(i-1);
                    list.remove(i-2);
                    for (int j = 0; j < list.size() - 1; j++) {
                        int grade3 = list.get(j);
                        int grade4 = list.get(j + 1);
                        prevGrade = threeReturnOne(prevCards);

                        if (grade3 == grade4 && compareCard(grade0,prevGrade)) {
                            // 只要3张牌可以大过上家，且有对子则返回true
                            return true;
                        }
                    }
                }
            }

        }
        // 上家出炸弹
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);
                int grade3 = myCards.get(i - 3);

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    if (compareCard(grade0,prevGrade)) {
                        // 只要有4张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出4带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);
                int grade3 = myCards.get(i - 3);

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    // 只要有炸弹，则返回true
                    return true;
                }
            }
        }
        // 上家出4带2对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i);
                int grade1 = myCards.get(i - 1);
                int grade2 = myCards.get(i - 2);
                int grade3 = myCards.get(i - 3);

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    // 只要有炸弹，则返回true
                    return true;
                }
            }
        }
        // 上家出顺子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
            if (mySize < prevSize) {
                return false;
            } else {
                HashSet<Byte> card = new HashSet<>(myCards);//去除重复的
                List<Byte> list = new ArrayList<>(card);//变回ArrayList 便于查询
                for (int i = 0 ; i <= card.size() - prevSize; i++) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(list.get(j + i));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
                        int myGrade2 = cards.get(cards.size() - 1);// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1);// 最大的牌在最后
                        if (compareCard(myGrade2,prevGrade2)) {
                            return true;
                        }
                    }
                }
            }
        }
        // 上家出连对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
            if (mySize < prevSize) {
                return false;
            } else {
                List<Byte> list = new ArrayList<Byte>();
                for (int i = 0 ; i < mySize - 1 ; i++) {
                    if (myCards.get(i) == myCards.get(i + 1)) {
                        if (!list.contains(myCards.get(i))) {
                            list.add(myCards.get(i));
                            list.add(myCards.get(i + 1));
                        }
                    }
                }

                for (int i = 0 ; i <= list.size() - prevSize ; i += 2) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(list.get(j+i));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
                        int myGrade2 = cards.get(cards.size() - 1);// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1);// 最大的牌在最后
                        if (compareCard(myGrade2,prevGrade2)) {
                            return true;
                        }
                    }
                }
            }
        }
        // 上家出飞机不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(myCards.get(i - j));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
                        int myGrade2 = cards.get(0);
                        int prevGrade2 = prevCards.get(0);
                        if (compareCard(myGrade2,prevGrade2)) {
                            return true;
                        }
                    }
                }
            }
        }
        // 上家出飞机带单
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
            int index = prevSize / 4 ;
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(myCards.get(i - j));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
                        int myGrade2 = cards.get(0);
                        int prevGrade2 = threeReturnOne(prevCards);
                        if (compareCard(myGrade2,prevGrade2)) {
                            return true;
                        }
                    }
                }
            }
        }
        // 上家出飞机带对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI) {
            int index = prevSize / 5;
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - (index * 2) - 1; i--) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize - (index * 2); j++) {
                        cards.add(myCards.get(i - j));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
                        int myGrade2 = cards.get(0);
                        int prevGrade2 = threeReturnOne(prevCards);
                        List<Byte> list = new ArrayList<>(myCards);
                        for (int j = 0; j < cards.size(); j++) {
                            if (list.contains(cards.get(j))) {
                                list.remove(cards.get(j));
                            }
                        }
                        int num = 0;
                        for (int l = 0 ; l < list.size() - 2; l += 2) {
                            if (list.get(l) == list.get(l+1)) {
                                num++;
                            }
                        }
                        if (compareCard(myGrade2,prevGrade2) && num >= index) {
                            return true;
                        }
                    }
                }
            }
        }
        // 默认不能出牌
        return false;
    }

    public static Map<Byte, List<Byte>> getListCountMap(List<Byte> cardListTemp) {
        Map<Byte, Byte> maps = new HashMap<>();
        for (Byte b : cardListTemp) {
            Byte t = maps.get(b);
            if (t == null) {
                maps.put(b, (byte) 1);
            } else {
                t++;
                maps.put(b, t);
            }
        }

        Map<Byte, List<Byte>> result = new HashMap<>();
        for (byte i = 1; i < 5; i++) {
            List<Byte> list = new ArrayList<>();
            for(Map.Entry<Byte,Byte> entry : maps.entrySet()){
                if(entry.getValue() != i) continue;
                list.add(entry.getKey());
            }
            Collections.sort(list);
            result.put(i,list);
        }
        return result;
    }

    /**
     * 判断我所有的牌中，是否存在能够管住上家的牌，决定出牌按钮是否显示
     *
     * @param cardsInHand
     *            我所有的牌 *
     * @param prevCards
     *            上家的牌
     * @param prevCardType
     *            上家牌的类型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static List<List<Byte>> isOverBigReturnList(List<Byte> cardsInHand,List<Byte> prevCards, int prevCardType) {
        List<List<Byte>> lists = new ArrayList<>();
        // 我的牌和上家的牌都不能为null
        if (cardsInHand == null || prevCards == null) {
            System.out.println("handCards == null || prevCards == null");
            return lists;
        }

        if (prevCardType == 0) {
            System.out.println("上家出的牌不合法，所以不能出。");
            return lists;
        }
        List<Byte> handCards = new ArrayList<>(cardsInHand);
        List<Byte> handCard = new ArrayList<>(handCards);
        // 默认情况：上家和自己想出的牌都符合规则
        handCards = DDZRule.modular(handCards);
        prevCards = DDZRule.modular(prevCards);

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = handCards.size();

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0) {
//            TODO
            System.out.println(" 我先出牌，上家没有牌 ");
            return lists;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            System.out.println("上家王炸，肯定不能出。");
            return lists;
        }

        List<List<Byte>> zhaDanList = new ArrayList<>();

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            if (mySize >= 4) {
                for (int i = 0; i < mySize - 3; i++) {
                    int grade0 = handCards.get(i);
                    int grade1 = handCards.get(i + 1);
                    int grade2 = handCards.get(i + 2);
                    int grade3 = handCards.get(i + 3);

                    if (grade1 == grade0 && grade2 == grade0
                            && grade3 == grade0) {
                        List<Byte>  list = new ArrayList<>();
                        list.add((byte)grade0);
                        list.add((byte)grade1);
                        list.add((byte)grade2);
                        list.add((byte)grade3);
                        // 2018 / 1 / 13
                        list = initList(list,handCard);
                        if (!lists.contains(list)){
                            zhaDanList.add(list);
                        }
                    }
                }
            }
        }

        if (mySize >= 2) {
            List<Byte> cards = new ArrayList<Byte>();
            cards.add(handCards.get(mySize -1));
            cards.add(handCards.get(mySize -2));
            if (DDZRule.isDuiWang(cards)) {
                // 2018 / 1 / 13
                cards = initList(cards,handCard);
                if (!lists.contains(cards)){
                    zhaDanList.add(cards);
                }
            }
        }

        int prevGrade = prevCards.get(0);

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        //获取牌的数量
        Map<Byte, List<Byte>> countMap = getListCountMap(handCards);
        // 上家出单
        if (prevCardType == PokerConstants.DDZ_CARDTYPE_DAN) {
            // 一张牌可以大过上家的牌
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Collections.singletonList(b),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }
        }
        // 上家出对子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_DUI_ZI) {
            // 2张牌可以大过上家的牌
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 2) continue;
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Arrays.asList(b,b),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }

        }
        // 上家出3不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 2; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//
//                if (grade0 == grade1 && grade0 == grade2) {
//                    if (compareCard(grade0,prevGrade)) {
//                        // 只要3张牌可以大过上家，则返回true
//                        List<Byte> list = new ArrayList<>();
//                        list.add((byte)grade0);
//                        list.add((byte)grade1);
//                        list.add((byte)grade2);
//                        // 2018 / 1 / 13
//                        list = initList(list,handCard);
//                        if (!lists.contains(list)){
//                            lists.add(list);
//                        }
//                    }
//                }
//            }
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 3) continue;
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Arrays.asList(b,b,b),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }
        }
        // 上家出3带1
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI) {
            // 3带1 3不带 比较只多了一个判断条件
            if (mySize < 4) return zhaDanList;

            // 3张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 2; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//
//                if (grade0 == grade1 && grade0 == grade2) {
//                    prevGrade = threeReturnOne(prevCards);
//                    if (compareCard(grade0,prevGrade)) {
//                        // 只要3张牌可以大过上家，则返回true
//                        if (i == mySize - 1) {
//                            List<Byte> list = new ArrayList<>();
//                            list.add((byte)grade0);
//                            list.add((byte)grade1);
//                            list.add((byte)grade2);
//                            list.add((byte)handCards.get(i-3));
//                            // 2018 / 1 / 13
//                            list = initList(list,handCard);
//                            if (!lists.contains(list)){
//                                lists.add(list);
//                            }
//                        } else {
//                            List<Byte> list = new ArrayList<>();
//                            list.add((byte)grade0);
//                            list.add((byte)grade1);
//                            list.add((byte)grade2);
//                            list.add((byte)handCards.get(mySize - 1));
//                            // 2018 / 1 / 13
//                            list = initList(list,handCard);
//                            if (!lists.contains(list)){
//                                lists.add(list);
//                            }
//                        }
//                    }
//                }
//            }

            prevGrade = threeReturnOne(prevCards);
            byte minCard = getMinCard(countMap);
            if(minCard < 0) return zhaDanList;
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 3) continue;
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Arrays.asList(b,b,b,minCard),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }
        }
        // 上家出3带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER) {
            // 3带2 3不带 比较只多了一个判断条件
            if (mySize < 5) {
                return zhaDanList;
            }
            // 3张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 2; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//                //三张一样的牌 然后看剩下的牌中有没有对子 有返回true 没有返回false
//                if (grade0 == grade1 && grade0 == grade2) {
//                    List<Byte> list = new ArrayList<>(handCards);
//                    list.remove(i);
//                    list.remove(i-1);
//                    list.remove(i-2);
//                    for (int j = 0; j < list.size() - 1; j++) {
//                        int grade3 = list.get(j);
//                        int grade4 = list.get(j + 1);
//                        prevGrade = threeReturnOne(prevCards);
//
//                        if (grade3 == grade4 && compareCard(grade0,prevGrade)) {
//                            // 只要3张牌可以大过上家，且有对子则返回true
//                            List<Byte> ls = new ArrayList<>();
//                            ls.add((byte)grade0);
//                            ls.add((byte)grade1);
//                            ls.add((byte)grade2);
//                            ls.add((byte)grade3);
//                            ls.add((byte)grade4);
//                            // 2018 / 1 / 13
//                            list = initList(list,handCard);
//                            if (!lists.contains(list)){
//                                lists.add(list);
//                            }
//                        }
//                    }
//                }
//            }

            prevGrade = threeReturnOne(prevCards);
            byte min2Card = getMin2Card(countMap);
            if(min2Card < 0) return zhaDanList;
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 3) continue;
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Arrays.asList(b,b,b,min2Card,min2Card),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }

        }
        // 上家出炸弹
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            // 4张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 3; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//                int grade3 = handCards.get(i - 3);
//
//                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
//                    if (compareCard(grade0,prevGrade)) {
//                        // 只要有4张牌可以大过上家，则返回true
//                        List<Byte> list = new ArrayList<>();
//                        list.add((byte)grade0);
//                        list.add((byte)grade1);
//                        list.add((byte)grade2);
//                        list.add((byte)grade3);
//                        // 2018 / 1 / 13
//                        list = initList(list,handCard);
//                        if (!lists.contains(list)){
//                            lists.add(list);
//                        }
//                    }
//                }
//            }
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 4) continue;
                for(Byte b : entry.getValue()){
                    if(compareCard(b,prevGrade)){
                        List<Byte> list = initList(Arrays.asList(b,b,b,b),handCard);
                        if (!lists.contains(list)) {
                            lists.add(list);
                        }
                    }
                }
            }

        }
        // 上家出4带2
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER) {
            // 4张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 3; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//                int grade3 = handCards.get(i - 3);
//
//                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
//                    // 只要有炸弹，则返回true
//                    List<Byte> list = new ArrayList<>();
//                    list.add((byte)grade0);
//                    list.add((byte)grade1);
//                    list.add((byte)grade2);
//                    list.add((byte)grade3);
//                    // 2018 / 1 / 13
//                    list = initList(list,handCard);
//                    if (!lists.contains(list)){
//                        lists.add(list);
//                    }
//                }
//            }
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 4) continue;
                for(Byte b : entry.getValue()){
                    List<Byte> list = initList(Arrays.asList(b,b,b,b),handCard);
                    if (!lists.contains(list)) {
                        lists.add(list);
                    }
                }
            }
        }
        // 上家出4带2对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI) {
            // 4张牌可以大过上家的牌
//            for (int i = mySize - 1; i >= 3; i--) {
//                int grade0 = handCards.get(i);
//                int grade1 = handCards.get(i - 1);
//                int grade2 = handCards.get(i - 2);
//                int grade3 = handCards.get(i - 3);
//
//                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
//                    // 只要有炸弹，则返回true
//                    List<Byte> list = new ArrayList<>();
//                    list.add((byte)grade0);
//                    list.add((byte)grade1);
//                    list.add((byte)grade2);
//                    list.add((byte)grade3);
//                    // 2018 / 1 / 13
//                    list = initList(list,handCard);
//                    if (!lists.contains(list)){
//                        lists.add(list);
//                    }
//                }
//            }
            for(Map.Entry<Byte,List<Byte>> entry : countMap.entrySet()){
                if(entry.getKey() < 4) continue;
                for(Byte b : entry.getValue()){
                    List<Byte> list = initList(Arrays.asList(b,b,b,b),handCard);
                    if (!lists.contains(list)) {
                        lists.add(list);
                    }
                }
            }
        }
        // 上家出顺子
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
            if (mySize < prevSize) {
                return zhaDanList;
            } else {
                HashSet<Byte> card = new HashSet<>(handCards);//去除重复的
                List<Byte> list = new ArrayList<>(card);//变回ArrayList 便于查询
                for (int i = 0 ; i <= card.size() - prevSize; i++) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(list.get(j + i));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
                        int myGrade2 = cards.get(cards.size() - 1);// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1);// 最大的牌在最后
                        if (compareCard(myGrade2,prevGrade2)) {
                            // 2018 / 1 / 13
                            cards = initList(cards,handCard);
                            if (!lists.contains(cards)){
                                lists.add(cards);
                            }
                        }
                    }
                }
            }
        }
        // 上家出连对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
            if (mySize < prevSize) {
                return zhaDanList;
            } else {
                List<Byte> list = new ArrayList<Byte>();
                for (int i = 0 ; i < mySize - 1 ; i++) {
                    if (handCards.get(i) == handCards.get(i + 1)) {
                        if (!list.contains(handCards.get(i))) {
                            list.add(handCards.get(i));
                            list.add(handCards.get(i + 1));
                        }
                    }
                }

                for (int i = 0 ; i <= list.size() - prevSize ; i += 2) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(list.get(j+i));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
                        int myGrade2 = cards.get(cards.size() - 1);// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1);// 最大的牌在最后
                        if (compareCard(myGrade2,prevGrade2)) {
                            // 2018 / 1 / 13
                            cards = initList(cards,handCard);
                            if (!lists.contains(cards)){
                                lists.add(cards);
                            }
//                           lists.add(cards);
                        }
                    }
                }
            }
        }
        // 上家出飞机不带
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
            if (mySize < prevSize) {
                return zhaDanList;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(handCards.get(i - j));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
                        int myGrade2 = cards.get(0);
                        int prevGrade2 = prevCards.get(0);
                        if (compareCard(myGrade2,prevGrade2)) {
                            // 2018 / 1 / 13
                            cards = initList(cards,handCard);
                            if (!lists.contains(cards)){
                                lists.add(cards);
                            }
//                            lists.add(cards);
                        }
                    }
                }
            }
        }
        // 上家出飞机带单
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
            int index = prevSize / 4 ;
            if (mySize < prevSize) {
                return zhaDanList;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Byte> cards = new ArrayList<Byte>();
                    for (int j = 0; j < prevSize ; j++) {
                        cards.add(handCards.get(i - j));
                    }
                    int myCardType = getCardType(cards);
                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
                        int myGrade2 = cards.get(0);
                        int prevGrade2 = threeReturnOne(prevCards);
                        if (compareCard(myGrade2,prevGrade2)) {
                            // 2018 / 1 / 13
                            cards = initList(cards,handCard);
                            if (!lists.contains(cards)){
                                lists.add(cards);
                            }
//                            lists.add(cards);
                        }
                    }
                }
            }
        }
        // 上家出飞机带对
        else if (prevCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI) {
//            int index = prevSize / 5;
//            if (mySize < prevSize) {
//                return zhaDanList;
//            } else {
//                for (int i = mySize - 1; i >= prevSize - (index * 2) - 1; i--) {
//                    List<Byte> cards = new ArrayList<Byte>();
//                    for (int j = 0; j < prevSize - (index * 2); j++) {
//                        cards.add(handCards.get(i - j));
//                    }
//                    int myCardType = getCardType(cards);
//                    if (myCardType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
//                        int myGrade2 = cards.get(0);
//                        int prevGrade2 = threeReturnOne(prevCards);
//                        List<Byte> list = new ArrayList<>(handCards);
//                        for (int j = 0; j < cards.size(); j++) {
//                            if (list.contains(cards.get(j))) {
//                                list.remove(cards.get(j));
//                            }
//                        }
//                        int num = 0;
//                        for (int l = 0 ; l < list.size() - 2; l += 2) {
//                            if (list.get(l) == list.get(l+1)) {
//                                cards.add(list.get(l));
//                                cards.add(list.get(l + 1));
//                                num++;
//                                if (num == index) break;
//                            }
//                        }
//                        if (compareCard(myGrade2,prevGrade2) && num >= index) {
//                            // 2018 / 1 / 13
//                            cards = initList(cards,handCard);
//                            if (!lists.contains(cards)){
//                                lists.add(cards);
//                            }
////                            lists.add(cards);
//                        }
//                    }
//                }
                // 取出当前手牌有超过3个(包括3个,4个)的牌,作为飞机
                // 去除手牌 >=2个 的牌作为带的对子
                List<Byte> feijiList = new ArrayList<>();
                feijiList.addAll(countMap.getOrDefault((byte)3, new ArrayList<>()));
                feijiList.addAll(countMap.getOrDefault((byte)4, new ArrayList<>()));

                List<Byte> duiziList = new ArrayList<>();
                duiziList.addAll(countMap.getOrDefault((byte)2, new ArrayList<>()));
                duiziList.addAll(countMap.getOrDefault((byte)3, new ArrayList<>()));
                duiziList.addAll(countMap.getOrDefault((byte)4, new ArrayList<>()));

                if(duiziList.size() < 4 || feijiList.size() < 2) return zhaDanList;
                Collections.sort(feijiList);
                Collections.sort(duiziList);

                List<List<Byte>> comboFeiji = new ArrayList<>();
                for (int i = 0; i < feijiList.size()-1; i++) {
                    if(feijiList.get(i) + 1 == feijiList.get(i+1)) {
                        List<Byte> list = new ArrayList<>();
                        list.add(feijiList.get(i));
                        list.add(feijiList.get(i + 1));
                        comboFeiji.add(list);
                    }
                }
                if(comboFeiji.isEmpty()) return zhaDanList;
                List<Byte> duiziListTemp = new ArrayList<>();
                for(List<Byte> feiji : comboFeiji) {
                    duiziListTemp.clear();
                    duiziListTemp.addAll(duiziList);
                    duiziListTemp.remove(feiji.get(0));
                    duiziListTemp.remove(feiji.get(1));
                    if(duiziListTemp.size() < 2) continue;
                    byte a1 = feiji.get(0);
                    byte a2 = feiji.get(1);
                    byte b1 = duiziListTemp.get(0);
                    byte b2 = duiziListTemp.get(1);
                    List<Byte> list = initList(Arrays.asList(a1,a1,a1,a2,a2,a2,b1,b1,b2,b2),handCard);
                    if (!lists.contains(list)) lists.add(list);
                }
//            }
        }
        // 默认不能出牌
        lists.addAll(zhaDanList);
        return lists;
    }

    private static byte getMin2Card(Map<Byte, List<Byte>> countMap) {
        for (byte i = 2; i < 5; i++) {
            if(!countMap.get(i).isEmpty()) return countMap.get(i).get(0);
        }
        return -1;
    }

    private static byte getMinCard(Map<Byte, List<Byte>> countMap) {
        for (byte i = 1; i < 5; i++) {
            if(!countMap.get(i).isEmpty()) return countMap.get(i).get(0);
        }
        return -1;
    }


    /**
     *
     * 三张 飞机 返回最小的那种牌
     *
     * @param myCards
     *
     * @return 返回三张一样中最小的那张牌
     */
    public static int threeReturnOne(List<Byte> myCards) {
        for (int  i = 0; i < myCards.size() - 2; i++) {
            byte grade1 = myCards.get(i);
            byte grade2 = myCards.get(i + 1);
            byte grade3 = myCards.get(i + 2);
            if (grade1 == grade2 && grade3 == grade1) {
                return grade1;
            }
        }
        return 0 ;
    }

    /**
     *
     * 获取原始牌
     *
     * @param cards     取模后能出的牌
     * @param handCards 原始手牌
     * @return
     */
    public static List<Byte> initList(List<Byte> cards,List<Byte> handCards) {
        List<Byte> list = new ArrayList<>();
        List<Byte> handCard = new ArrayList<>(handCards);
//        handCard =
        for (int i = 0; i < cards.size(); i++) {
            handCard.removeAll(list);
            for (int j = 0; j < handCard.size(); j++) {
                if (cards.get(i) == 17) {
                    if ((handCard.get(j) & 0x0f) == 1) {
                        list.add(handCard.get(j));
                        break;
                    }
                }
                if (cards.get(i) == 18) {
                    if ((handCard.get(j) & 0x0f) == 2) {
                        list.add(handCard.get(j));
                        break;
                    }
                }
                if ((handCard.get(j) & 0x0f) == cards.get(i)) {
                    list.add(handCard.get(j));
                    break;
                }
            }
        }
        return list;
    }

    /**
     * 对手牌排序
     * 条件是
     *      1.大小王在左边
     *      2.去除花色后大的在左边
     *      3.去除花色后相等的按照花色排列,黑桃:3>红桃:2>梅花:1>方块:0
     * @param handCards
     * @return
     */
    public static List<Byte> sortHandCards(List<Byte> handCards){
        if(handCards.size() <=1 ) return handCards;
        boolean hasDaWang = handCards.remove(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG));
        boolean hasXiaoWang = handCards.remove(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG));

        Byte[] result = handCards.toArray(new Byte[handCards.size()]);
        byte temp = 0;
        int size = result.length;
        for(int i = 0 ; i < size-1; i ++) {
            for(int j = 0 ;j < size-1-i ; j++) {
                int color1 = result[j] >> 4;
                int color2 = result[j + 1] >> 4;
                int a = result[j] & 0x0f;
                int b = result[j + 1] & 0x0f;
                if (((a < b) || (a == b && color1 < color2))) {
                    temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        }
        List<Byte> list = new ArrayList<>();
        if(hasDaWang) list.add(PokerConstants.POKER_CODE_DA_WANG);
        if(hasXiaoWang) list.add(PokerConstants.POKER_CODE_XIAO_WANG);
        list.addAll(Arrays.asList(result));
        return list;
    }

    /**
     * 对手牌(已经去除花色的手牌)从小到大排序
     * 简单冒泡排序
     * @param handCards
     * @return
     */
    public static List<Byte> sortInShunXu(List<Byte> handCards){
        if(handCards.size() <=1 ) return handCards;
        Byte[] result = handCards.toArray(new Byte[handCards.size()]);
        byte temp = 0;
        int size = result.length;
        for(int i = 0 ; i < size-1; i ++) {
            for(int j = 0 ;j < size-1-i ; j++) {
                if (result[j] > result[j+1]) {
                    temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        }
        return new ArrayList<>(Arrays.asList(result));
    }

    /**
     * 对手牌(已经去除花色的手牌)从大到小排序
     * 简单冒泡排序
     * @param handCards
     * @return
     */
    public static List<Byte> sortInDaoXu(List<Byte> handCards){
        if(handCards.size() <=1 ) return handCards;
        Byte[] result = handCards.toArray(new Byte[handCards.size()]);
        byte temp = 0;
        int size = result.length;
        for(int i = 0 ; i < size-1; i ++) {
            for(int j = 0 ;j < size-1-i ; j++) {
                if (result[j] < result[j+1]) {
                    temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        }
        return new ArrayList<>(Arrays.asList(result));
    }

   public static void main(String[] args) {
       List<Byte> list = DualDDZProcessor.int2ByteList(new ArrayList<>(Arrays.asList(81,47,62,46,61,28,43,27,11,58,42,26,56,40,7,6,53)));
       List<Byte> l = DDZRule.modular(list);
       System.out.println(l);



       System.out.println(DualDDZProcessor.getListCountMap(l));

    }

    private static Map<Integer, List<Integer>> getListCountMap2(List<Integer> cardListTemp) {
        Map<Integer, Byte> maps = new HashMap<>();
        for (Integer b : cardListTemp) {
            Byte t = maps.get(b);
            if (t == null) {
                maps.put(b, (byte) 1);
            } else {
                t++;
                maps.put(b, t);
            }
        }

        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int i = 1; i < 5; i++) {
            List<Integer> list = new ArrayList<>();
            for(Map.Entry<Integer,Byte> entry : maps.entrySet()){
                if(entry.getValue() != i) continue;
                list.add(entry.getKey());
            }
            Collections.sort(list);
            result.put(i,list);
        }
        return result;
    }

    public static List<Integer> sortOutCards(List<Integer> outCards, int pokerType) {
        List<Integer> result = new ArrayList<>();
        if (pokerType == PokerConstants.DDZ_CARDTYPE_DAN) {
            return outCards;
        } else if (pokerType == PokerConstants.DDZ_CARDTYPE_WANG_ZHA) {
            return new ArrayList<>(Arrays.asList((int) PokerConstants.POKER_CODE_DA_WANG, (int) PokerConstants.POKER_CODE_XIAO_WANG));
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_DUI_ZI) {
            return outCards;
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_ZHA_DAN) {
            return outCards;
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_YI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            result = byte2IntList(initList(Arrays.asList(map.get((byte)3).get(0),map.get((byte)3).get(0),map.get((byte)3).get(0)),list));
            result.addAll(byte2IntList(initList(map.get((byte)1),list)));

        } else if (pokerType == PokerConstants.DDZ_CARDTYPE_SAN_DAI_ER) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            result = byte2IntList(initList(Arrays.asList(map.get((byte)3).get(0),map.get((byte)3).get(0),map.get((byte)3).get(0)),list));
            result.addAll(byte2IntList(initList(Arrays.asList(map.get((byte)2).get(0),map.get((byte)2).get(0)),list)));

        } else if (pokerType == PokerConstants.DDZ_CARDTYPE_SAN_BU_DAI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            result = byte2IntList(initList(Arrays.asList(map.get((byte)3).get(0),map.get((byte)3).get(0),map.get((byte)3).get(0)),list));
        } else if (pokerType == PokerConstants.DDZ_CARDTYPE_SHUN_ZI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            result = byte2IntList(initList(sortInDaoXu(map.get((byte)1)),list));
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_LIAN_DUI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : sortInDaoXu(map.get((byte)2))) {
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : map.get((byte)4)) {
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
            if(map.get((byte)1).isEmpty()){
                result.addAll(byte2IntList(initList(Arrays.asList(map.get((byte)2).get(0),map.get((byte)2).get(0)),list)));
            }else{
                result.addAll(byte2IntList(initList(sortInDaoXu(map.get((byte)1)),list)));
            }

        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_SI_DAI_ER_DUI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : map.get((byte)4)) {
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
            List<Byte> byteList2 = new ArrayList<>();
            if(!map.get((byte)2).isEmpty()) {
                for (byte b : sortInDaoXu(map.get((byte) 2))) {
                    byteList2.add(b);
                    byteList2.add(b);
                }
            }
            result.addAll(byte2IntList(initList(byteList2,list)));

        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_FEI_JI_BU_DAI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : sortInDaoXu(map.get((byte)3))) {
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DAN) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : sortInDaoXu(map.get((byte)3))) {
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
            if(!map.get((byte)1).isEmpty()) {
                result.addAll(byte2IntList(initList(sortInDaoXu(map.get((byte) 1)), list)));
            }else{
                List<Byte> byteList2 = new ArrayList<>();
                for (byte b : sortInDaoXu(map.get((byte)2))) {
                    byteList2.add(b);
                    byteList2.add(b);
                }
                result.addAll(byte2IntList(initList(byteList2, list)));

            }
        }  else if (pokerType == PokerConstants.DDZ_CARDTYPE_FEI_JI_DAI_DUI) {
            List<Byte> list = int2ByteList(new ArrayList<>(outCards));
            Map<Byte, List<Byte>> map = getListCountMap(DDZRule.modular(list));
            List<Byte> byteList = new ArrayList<>();
            for (byte b : sortInDaoXu(map.get((byte)3))) {
                byteList.add(b);
                byteList.add(b);
                byteList.add(b);
            }
            result = byte2IntList(initList(byteList,list));
            List<Byte> byteList2 = new ArrayList<>();
            if(!map.get((byte)2).isEmpty()){
                for (byte b : sortInDaoXu(map.get((byte)2))) {
                    byteList2.add(b);
                    byteList2.add(b);
                }
            }else{
                for (byte b : map.get((byte)4)) {
                    byteList2.add(b);
                    byteList2.add(b);
                    byteList2.add(b);
                    byteList2.add(b);
                }
            }
            result.addAll(byte2IntList(initList(byteList2,list)));
        }
        System.out.println("result---------------"+result);
        return result;
    }

    public static List<Byte> int2ByteList(List<Integer> outCards) {
        List<Byte> list = new ArrayList<>();
        for(int b : outCards){
            list.add((byte)b);
        }
        return list;
    }
    public static List<Integer> byte2IntList(List<Byte> outCards) {
        List<Integer> list = new ArrayList<>();
        for(Byte b : outCards){
            list.add(Integer.valueOf(b));
        }
        return list;
    }
}