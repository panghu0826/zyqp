package com.buding.poker.common;

import com.buding.poker.ddz.DDZProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 斗地主的规则
 *
 * @author  chen
 */
public class DDZRule {

    /**
     * 判断牌是否为单
     *
     * @param myCard 牌的集合
     * @return 如果为单，返回true；否则，返回false。
     */
    public static boolean isDan(List<Byte> myCard) {
        // 默认不是单
        boolean flag = false;
        if (myCard != null && myCard.size() == 1) {
            flag = true;
        }
        return flag;
    }


    /**
     * 判断牌是否为对子
     *
     * @param myCard 牌的集合
     * @return 如果为对子，返回true；否则，返回false。
     */
    public static boolean isDuiZi(List<Byte> myCard) {
        // 默认不是对子
        boolean flag = false;

        if (myCard != null && myCard.size() == 2) {
            // 对牌进行排序
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            myCards = modular(myCards);

            int grade1 = myCards.get(0);
            int grade2 = myCards.get(1);
            if (grade1 == grade2) {
                flag = true;
            }
        }

        return flag;

    }


    /**
     * 判断牌是否为3带1
     *
     * @param myCard 牌的集合
     * @return 如果为3带1，被带牌的位置，0或3，否则返回-1。炸弹返回-1。
     */
    public static int isSanDaiYi(List<Byte> myCard) {
        int flag = -1;
        // 默认不是3带1
        if (myCard != null && myCard.size() == 4) {
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);

            int[] grades = new int[4];
            grades[0] = myCards.get(0);
            grades[1] = myCards.get(1);
            grades[2] = myCards.get(2);
            grades[3] = myCards.get(3);

            // 暂时认为炸弹不为3带1
            if ((grades[1] == grades[0]) && (grades[2] == grades[0])
                    && (grades[3] == grades[0])) {
                return -1;
            }
            // 3带1，被带的牌在牌头
            else if ((grades[1] == grades[0] && grades[2] == grades[0])) {
                return 0;
            }
            // 3带1，被带的牌在牌尾
            else if (grades[1] == grades[3] && grades[2] == grades[3]) {
                return 3;
            }
        }
        return flag;
    }


    /**
     * 判断牌是否为3带2
     *
     * @param myCard 牌的集合
     * @return 如果为3带2，返回true
     */
    public static boolean isSanDaiEr(List<Byte> myCard) {

        boolean flag = false;

        if (myCard == null || myCard.size() != 5) {
            return flag;
        }

        // 对牌进行排序
        List<Byte> myCards = new ArrayList<Byte>(myCard);
        myCards = modular(myCards);

        //对子在前面
        if (myCards.get(0) == myCards.get(1) && myCards.get(2) == myCards.get(3) && myCards.get(3) == myCards.get(4)) {
            flag = true;
        } else if (myCards.get(0) == myCards.get(1) && myCards.get(1) == myCards.get(2) && myCards.get(3) == myCards.get(4)) {
            flag = true;
        }

        return flag;
    }


    /**
     * 判断牌是否为3不带
     *
     * @param myCard 牌的集合
     * @return 如果为3不带，返回true；否则，返回false。
     */
    public static boolean isSanBuDai(List<Byte> myCard) {
        // 默认不是3不带
        boolean flag = false;

        if (myCard != null && myCard.size() == 3) {
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            int grade0 = myCards.get(0);
            int grade1 = myCards.get(1);
            int grade2 = myCards.get(2);

            if (grade0 == grade1 && grade2 == grade0) {
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 判断牌是否为顺子
     *
     * @param myCard 牌的集合
     * @return 如果为顺子，返回true；否则，返回false。
     */
    public static boolean isShunZi(List<Byte> myCard) {
        // 默认是顺子
        boolean flag = true;

        if (myCard != null) {

            int size = myCard.size();
            // 顺子牌的个数在5到12之间
            if (size < 5 || size > 12) {
                return false;
            }

            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);

            for (int n = 0; n < size - 1; n++) {
                int prev = myCards.get(n);
                int next = myCards.get(n + 1);
                // 小王、大王、2不能加入顺子
                if (prev == 17 || prev == 18 || prev == 15 || next == 18
                        || next == 18 || next == 15) {
                    flag = false;
                    break;
                } else {
                    if (prev - next != -1) {
                        flag = false;
                        break;
                    }

                }
            }
        }

        return flag;
    }


    /**
     * 判断牌是否为炸弹
     *
     * @param myCard 牌的集合
     * @return 如果为炸弹，返回true；否则，返回false。
     */
    public static boolean isZhaDan(List<Byte> myCard) {
        // 默认不是炸弹
        boolean flag = false;
        if (myCard != null && myCard.size() == 4) {

            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            int[] grades = new int[4];
            grades[0] = myCards.get(0);
            grades[1] = myCards.get(1);
            grades[2] = myCards.get(2);
            grades[3] = myCards.get(3);
            if ((grades[1] == grades[0]) && (grades[2] == grades[0])
                    && (grades[3] == grades[0])) {
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 判断牌是否为王炸
     *
     * @param myCard 牌的集合
     * @return 如果为王炸，返回true；否则，返回false。
     */
    public static boolean isDuiWang(List<Byte> myCard) {
        // 默认不是对王
        boolean flag = false;

        if (myCard != null && myCard.size() == 2) {
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            // 只有小王17 和大王18
            if ((myCards.get(0) == 17 && myCards.get(1) == 18) || (myCards.get(1) == 17 && myCards.get(0) == 18)) {
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 判断牌是否为连对
     *
     * @param myCard 牌的集合
     * @return 如果为连对，返回true；否则，返回false。
     */
    public static boolean isLianDui(List<Byte> myCard) {
        // 默认是连对
        boolean flag = true;
        if (myCard == null) {
            flag = false;
            return flag;
        }

        int size = myCard.size();
        if (size < 6 || size % 2 != 0) {
            flag = false;
        } else {
            // 对牌进行排序
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            //牌是从小到大排序的如果最后的那张是 2 说明不是连对
            if (myCards.get(myCards.size() - 1) == 15) {
                return false;
            }
            for (int i = 0; i < size; i = i + 2) {
                if (myCards.get(i) != myCards.get(i + 1)) {
                    flag = false;
                    break;
                }

                if (i < size - 2) {
                    if (myCards.get(i) - myCards.get(i + 2) != -1) {
                        flag = false;
                        break;
                    }
                }
            }
        }

        return flag;
    }


    /**
     * 判断牌是否为飞机
     *
     * @param myCard
     *            牌的集合
     * @return 如果为飞机，返回true；否则，返回false。
     */
//    public static boolean isFeiJi(List<Byte> myCard) {
//        boolean flag = false;
//        // 默认不是单
//        if (myCard != null) {
//
//            int size = myCard.size();
//            if (size >= 6) {
//                // 对牌进行排序
//                List<Byte> myCards = new ArrayList<Byte>();
//                myCards.addAll(myCard);
//                // 对牌进行排序
//                myCards = modular(myCards);
//
//                if (size % 3 == 0 && size % 4 != 0) {
//                    flag = isFeiJiBuDai(myCards);
//                } else if (size % 3 != 0 && size % 4 == 0) {
//                    flag = isFeiJiDai(myCards);
//                } else if (size == 12) {
//                    flag = isFeiJiBuDai(myCards) || isFeiJiDai(myCards);
//                }
//            }
//        }
//        return flag;
//    }


    /**
     * 判断牌是否为飞机不带
     *
     * @param myCard 牌的集合
     * @return 如果为飞机不带，返回true；否则，返回false。
     */
    public static boolean isFeiJiBuDai(List<Byte> myCard) {
        if (myCard == null) {
            return false;
        }

        List<Byte> myCards = new ArrayList<Byte>(myCard);
        // 对牌进行排序
        myCards = modular(myCards);
        int size = myCards.size();
        int n = size / 3;

        int[] grades = new int[n];

        if (size < 6 || size % 3 != 0) {
            return false;
        } else {
            for (int i = 0; i < n; i++) {
                if (!isSanBuDai(myCards.subList(i * 3, i * 3 + 3))) {
                    return false;
                } else {
                    // 如果连续的3张牌是一样的，记录其中一张牌的grade
                    grades[i] = myCards.get(i * 3);
                }
            }
        }

        for (int i = 0; i < n - 1; i++) {
            if (grades[n - 1] == 15) {// 不允许出现2
                return false;
            }

            if (grades[i + 1] - grades[i] != 1) {
                System.out.println("等级连续,如 333444"
                        + (grades[i + 1] - grades[i]));
                return false;// grade必须连续,如 333444
            }
        }

        return true;
    }


    /**
     * 判断牌是否为飞机带
     *
     * @param myCard
     *            牌的集合
     * @return 如果为飞机带，返回true；否则，返回false。
     */
//    public static boolean isFeiJiDai(List<Byte> myCard) {
//        List<Byte> myCards = new ArrayList<Byte>();
//        myCards.addAll(myCard);
//        // 对牌进行排序
//        myCards = modular(myCards);
//        int size = myCards.size();
//       if (size > 6) {
//           int n = size / 4;// 此处为“除”，而非取模
//           int i = 0;
//           for (i = 0; i < size - 3; i++) {
//               int grade1 = myCards.get(i);
//               int grade2 = myCards.get(i + 1);
//               int grade3 = myCards.get(i + 2);
//               if (grade1 == grade2 && grade3 == grade1) {
//
//                   ArrayList<Byte> cards = new ArrayList<Byte>();
//                   for (int j = i; j < i + 3 * n; j++) {// 取字串
//                       cards.add(myCards.get(j));
//                   }
//                   return isFeiJiBuDai(cards);
//               }
//
//           }
//       }
//
//        return false;
//    }


    /**
     * 飞机带单牌
     *
     * @param myCard
     * @return 如果为飞机带单，返回true；否则，返回false。
     */
    public static boolean isFeiJiDaiDan(List<Byte> myCard) {
        List<Byte> myCards = new ArrayList<Byte>(myCard);
        // 对牌进行排序取余
        myCards = modular(myCards);
        Map<Byte, List<Byte>> countMap = DDZProcessor.getListCountMap(myCards);
        if(countMap.get((byte)1).isEmpty()) return false;
        int size = myCards.size();
        //飞机带单牌的
        if (size >= 8 && size % 4 == 0) {

            int n = size / 4;
            ArrayList<Byte> cards = new ArrayList<Byte>();
            for (int i = 0; i < size - 2; i++) {
                byte grade1 = myCards.get(i);
                byte grade2 = myCards.get(i + 1);
                byte grade3 = myCards.get(i + 2);
                if (grade1 == grade2 && grade3 == grade1) {
                    if (!cards.contains(grade1)) {
                        for (int j = i; j < i + 3; j++) {// 取字串
                            cards.add(myCards.get(j));
                        }
                    }
                }
            }
            return isFeiJiBuDai(cards) && (cards.size() / 3) == (size / 4);
        }

        return false;
    }

    /**
     * 飞机带对子
     *
     * @param myCard
     * @return 如果为飞机带双，返回true；否则，返回false。
     */
    public static boolean isFeiJiDaiDui(List<Byte> myCard) {
        List<Byte> myCards = new ArrayList<Byte>(myCard);
        // 对牌进行排序取余
        myCards = modular(myCards);
        int size = myCards.size();
        //飞机带单牌的
        if (size >= 10 && size % 5 == 0) {
            int n = size / 5;
            ArrayList<Byte> cards = new ArrayList<Byte>();
            for (int i = 0; i < size - 2; i++) {
                byte grade1 = myCards.get(i);
                byte grade2 = myCards.get(i + 1);
                byte grade3 = myCards.get(i + 2);
                if (grade1 == grade2 && grade3 == grade1) {
                    if (!cards.contains(grade1)) {
                        for (int j = i; j < i + 3; j++) {// 取字串
                            cards.add(myCards.get(j));
                        }
                    }
                }
            }

            if (isFeiJiBuDai(cards)) {//如果是飞机 则判断剩下的牌是不是全为对子
                List<Byte> list = new ArrayList<>(myCards);
                for (int i = 0; i < cards.size(); i++) {
                    if (list.contains(cards.get(i))) {
                        list.remove(cards.get(i));
                    }
                }
                for (int l = 0; l < list.size(); l += 2) {
                    if (list.get(l) != list.get(l + 1)) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }


    /**
     * 判断牌是否为4带2单
     *
     * @param myCard 牌的集合
     * @return 如果为4带2，返回true；否则，返回false。
     */
    public static boolean isSiDaiErDan(List<Byte> myCard) {
        if (myCard != null && myCard.size() == 6) {
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            if(new HashSet<>(myCards).size() == 2) return false;
            for (int i = 0; i < 3; i++) {
                List<Byte> list = new ArrayList<>();
                list.add(myCards.get(i));
                list.add(myCards.get(i + 1));
                list.add(myCards.get(i + 2));
                list.add(myCards.get(i + 3));
                if (isZhaDan(list)) {
                    myCards.removeAll(list);
                    if (myCards.size() != 2) {
                        return false;
                    }
                    if (!myCards.get(0).equals(myCards.get(1))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * 判断牌是否为4带2对
     *
     * @param myCard 牌的集合
     * @return 如果为4带2，返回true；否则，返回false。
     */
    public static boolean isSiDaiErDui(List<Byte> myCard) {
        if (myCard != null && myCard.size() == 8) {
            List<Byte> myCards = new ArrayList<Byte>(myCard);
            // 对牌进行排序
            myCards = modular(myCards);
            for (int i = 0; i < myCards.size() - 3; i++) {
                List<Byte> list = new ArrayList<>();
                list.add(myCards.get(i));
                list.add(myCards.get(i + 1));
                list.add(myCards.get(i + 2));
                list.add(myCards.get(i + 3));

                if (isZhaDan(list)) {
                    myCards.removeAll(list);
                    if (myCards.size() != 4) {
                        return false;
                    }
                    if (myCards.get(0).equals(myCards.get(1)) && myCards.get(2).equals(myCards.get(3)) && !myCards.get(1).equals(myCards.get(2))){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检测玩家手里是否有王炸 和 四个二
     *
     * @param handCards
     * @return
     */
    public static boolean isBoomOrFourTwo (List<Byte> handCards) {
        if (handCards == null) return false;
        List<Byte> myCards = new ArrayList<Byte>(handCards);
        // 对牌进行排序
        myCards = modular(myCards);
        int towNum = 0 , boomNum = 0;
        for (byte card : myCards) {
            if (card == (byte) 15) {
                towNum++;
            }
            if (card == (byte) 17) {
                boomNum++;
            }
            if (card == (byte) 18) {
                boomNum++;
            }
        }

        if (towNum == 4 || boomNum == 2) return true;


        return false;
    }


    /**
     * @param list
     * @return 取模加排序后的集合
     */
    public static List<Byte> modular(List<Byte> list) {
        List<Byte> ls = new ArrayList<>();

        for (byte card : list) {
            if ((byte) (card & 0x0f) == 1) {//小王
                ls.add((byte) 17);
            } else if ((byte) (card & 0x0f) == 2) {//大王
                ls.add((byte) 18);
            } else {
                ls.add((byte) (card & 0x0f));
            }
        }
        ls = DDZProcessor.sortInShunXu(ls);
        return ls;
    }


    public static void main(String[] args) {

        List<Byte> ls = new ArrayList<Byte>();

//        ls.add((byte) 4);
//        ls.add((byte) 13);
//        ls.add((byte) 13);
//        ls.add((byte) 9);
//        ls.add((byte) 11);
//        ls.add((byte) 11);
//        ls.add((byte) 11);
        ls.add((byte) 10);
        ls.add((byte) 10);
        ls.add((byte) 10);
//        ls.add((byte) 9);
//        ls.add((byte) 9);
//        ls.add((byte) 8);
//        ls.add((byte) 8);
//        ls.add((byte) 12);
//        ls.add((byte) 4);
//        ls.add((byte) 4);
        ls.add((byte) 3);
        ls.add((byte) 3);
        ls.add((byte) 10);



        System.out.println(isSiDaiErDui(ls));
        System.out.println(isSiDaiErDan(ls));
    }

}
