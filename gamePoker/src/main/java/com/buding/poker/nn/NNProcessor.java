package com.buding.poker.nn;

import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.poker.common.NNBiPaiResult;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.helper.NNHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@SuppressWarnings("all")
public class NNProcessor {
    private static Logger logger = LogManager.getLogger(NNProcessor.class);


    public NNProcessor() {

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

    public static Map<Byte, Byte> getListCountMap(List<Byte> cardListTemp) {
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
        return maps;
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

    //经典抢庄和明牌抢庄可以抢庄
    public boolean canQiangZhuang(MJDesk<byte[]> desk) {
        return desk.canMingPaiQiangZhuang() || desk.canJingDianQiangZhuang();
    }

    public List<Byte> chineseName2CardList(String name){
        String[] cardListChinese = name.split(" ");
        List<Byte> cardList = new ArrayList<>();
        for(String card : cardListChinese){
            cardList.add(NNHelper.singleCardMapChinese.get(card));
        }
        return cardList;
    }

    public NNBiPaiResult getCardsResult (List<Byte> cardsOrignal, MJDesk<byte[]> desk) {
        if(cardsOrignal == null || cardsOrignal.isEmpty() || cardsOrignal.size() != 5) return null;
        List<Byte> cards = new ArrayList<>(cardsOrignal);
        NNBiPaiResult result = getSpecialNiuList(cards,desk);
        if(result != null ) return result;

        if(desk.canWangLaiZi()
                && (cards.contains(PokerConstants.POKER_CODE_DA_WANG)
                || cards.contains(PokerConstants.POKER_CODE_XIAO_WANG))){//带王癞子并且有王
            //这时候找出两张牌做牛值
            result = new NNBiPaiResult();
            List<Byte> cardsTemp = new ArrayList<>(cards);
            List<Byte> cardsTemp2 = new ArrayList<>();
            cardsTemp.remove(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG));
            cardsTemp.remove(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG));
            List<List<Integer>> comboList = new ArrayList<>();
            int niuFan = 0;
            List<Byte> cardsSortAndFenCha = new ArrayList<>();
            List<Byte> comboNiuFanTemp = new ArrayList<>();
            for (int i = 0; i < cardsTemp.size(); i++) {
                for (int j = 0; j < cardsTemp.size(); j++) {
                    if(i == j) continue;
                    List<Integer> combo = new ArrayList<>();
                    combo.add(i);
                    combo.add(j);
                    Collections.sort(combo);
                    if(comboList.contains(combo)) continue;
                    int a = (cardsTemp.get(i) & 0x0f) > 10 ? 10 : (cardsTemp.get(i) & 0x0f);
                    int b = (cardsTemp.get(j) & 0x0f) > 10 ? 10 : (cardsTemp.get(j) & 0x0f);
                    int total = a + b;
                    int mod = total % 10;
                    if(mod == 0) mod = 10;
                    comboNiuFanTemp.clear();
                    comboNiuFanTemp.add(cardsTemp.get(i));
                    comboNiuFanTemp.add(cardsTemp.get(j));
                    cardsTemp2.clear();
                    cardsTemp2.addAll(cards);
                    cardsTemp2.removeAll(comboNiuFanTemp);
                    if(niuFan < mod) {
                        niuFan = mod;
                        cardsSortAndFenCha.clear();
                        cardsSortAndFenCha.addAll(sortHandCards(cardsTemp2));
                        cardsSortAndFenCha.addAll(sortHandCards(comboNiuFanTemp));
                    }
                }
            }
            logger.error("cardsSortAndFenCha--"+cardsSortAndFenCha);
            result.cardsInHand = cards;
            result.cardsSort = sortHandCards(cards);
            result.cardsSortAndFenCha = cardsSortAndFenCha;
            result.cardType = niuFan;
            result.cardMultiple = desk.getNiuFanStr().get(result.cardType);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
        }else {
            result = new NNBiPaiResult();
            List<Byte> cardsTemp = new ArrayList<>(cards);
            int total = 0;
            for(Byte b : cardsTemp){
                int card = 0;
                if(b >= PokerConstants.POKER_CODE_XIAO_WANG){
                    card = 10;
                }else{
                    card = (b & 0x0f) > 10 ? 10: (b & 0x0f) ;
                }
                total += card;
            }
            int mod = total % 10;
            List<List<Byte>> list = new ArrayList<>();
            List<List<Integer>> comboList = new ArrayList<>();
            List<Byte> cardsSortAndFenCha = new ArrayList<>();
            boolean hasNiu = false;
            breakPoint: for (int i = 0; i < cards.size(); i++) {
                for (int j = 0; j < cards.size(); j++) {
                    if (i == j) continue;
                    List<Integer> combo = new ArrayList<>();
                    combo.add(i);
                    combo.add(j);
                    Collections.sort(combo);
                    if(comboList.contains(combo)) continue;
                    cardsTemp.clear();
                    cardsTemp.addAll(cards);
                    int a = (cardsTemp.get(i) >= PokerConstants.POKER_CODE_XIAO_WANG ||(cardsTemp.get(i) & 0x0f) > 10) ? 10 : (cardsTemp.get(i) & 0x0f);
                    int b = (cardsTemp.get(j) >= PokerConstants.POKER_CODE_XIAO_WANG ||(cardsTemp.get(j) & 0x0f) > 10) ? 10 : (cardsTemp.get(j) & 0x0f);
                    if ((a + b) % 10 == mod) {
                        hasNiu = true;
                        List<Byte> cardsTemp2 = new ArrayList<>(cards);
                        cardsTemp2.remove(Byte.valueOf(cardsTemp.get(i)));
                        cardsTemp2.remove(Byte.valueOf(cardsTemp.get(j)+""));

                        List<Byte> list2 = new ArrayList<>();
                        list2.add(cardsTemp.get(i));
                        list2.add(cardsTemp.get(j));

                        cardsSortAndFenCha.clear();
                        cardsSortAndFenCha.addAll(sortHandCards(cardsTemp2));
                        cardsSortAndFenCha.addAll(sortHandCards(list2));
                        break breakPoint;
                    }
                }
            }
            logger.error("cardsSortAndFenCha--"+cardsSortAndFenCha);

            if(hasNiu){
                result.cardsInHand = cards;
                result.cardsSort = sortHandCards(cards);
                result.cardsSortAndFenCha = cardsSortAndFenCha;
                result.cardType = mod == 0 ? 10 : mod;
                result.cardMultiple = desk.getNiuFanStr().get(result.cardType);
                result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            }else{
                result.cardsInHand = cards;
                result.cardsSort = sortHandCards(cards);
                result.cardsSortAndFenCha = result.cardsSort;
                result.cardType = 0;
                result.cardMultiple = 1;
                result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            }
        }
        for(byte b : result.cardsSort){
            result.cardsReal.add(getCardValue(b));
        }
        return result;
    }

    private NNBiPaiResult getSpecialNiuList(List<Byte> cardsOrignal, MJDesk<byte[]> desk) {
        List<Byte> cards = new ArrayList<>(cardsOrignal);
        NNBiPaiResult result;
        result = getWuXiaoNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getShunJinNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getZhaDanNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getHuLuNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getTongHuaNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getShunZiNiuList(cards,desk);
        if(result != null) return result;
        cards.clear();
        cards.addAll(cardsOrignal);
        result = getWuHuaNiuList(cards,desk);
        if(result != null) return result;
        return result;
    }

    private NNBiPaiResult getWuHuaNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_WU_HUA_NIU)) return null;
        NNBiPaiResult result = null;
        boolean isWuHuaNiu = true;
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG) continue;
            if((b & 0x0f) <= 10){
               isWuHuaNiu = false;
               break;
           }
        }
        if(isWuHuaNiu) {
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(sortHandCards(cards));
            for(byte b : result.cardsSort){
                result.cardsReal.add(getCardValue(b));
            }
            result.cardType = PokerConstants.NN_CARDTYPE_WU_HUA_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_WU_HUA_NIU);
            result.cardsSortAndFenCha.addAll(result.cardsSort);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
        }
        return result;
    }

    private NNBiPaiResult getShunZiNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_SHUN_ZI_NIU)) return null;
        NNBiPaiResult result = null;
        List<Integer> cardTemp = new ArrayList<>();
        int wangNum = 0;
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG){
                if(desk.canWangLaiZi()) {
                    wangNum++;
                    continue;
                }else{
                    return null;
                }
            }
            cardTemp.add(b & 0x0f);
        }
        List<Integer> shunZi = comboShunZi(cardTemp,wangNum);
        if(shunZi != null) {
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(sortHandCards(cards));
            result.cardsReal = shunZi;
            result.cardType = PokerConstants.NN_CARDTYPE_SHUN_ZI_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_SHUN_ZI_NIU);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            result.cardsSortAndFenCha.addAll(result.cardsSort);
        }
        return result;
    }

    private NNBiPaiResult getTongHuaNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_TONG_HUA_NIU)) return null;
        NNBiPaiResult result;
        int color = (cards.get(0) >> 4) & 0xff;
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG){
                if(desk.canWangLaiZi()) {
                    continue;
                }else{
                    return null;
                }
            }
            if(((b >> 4) & 0xff) != color) return null;
        }
        result = new NNBiPaiResult();
        result.cardsInHand.addAll(cards);
        result.cardsSort.addAll(sortHandCards(cards));
        for(byte b : result.cardsSort){
            result.cardsReal.add(getCardValue(b));
        }
        result.cardType = PokerConstants.NN_CARDTYPE_TONG_HUA_NIU;
        result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_TONG_HUA_NIU);
        result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
        result.cardsSortAndFenCha.addAll(result.cardsSort);
        return result;
    }

    private NNBiPaiResult getHuLuNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_HU_LU_NIU)) return null;
        NNBiPaiResult result = null;
        int wangNum = 0;
        List<Byte> cardTemp = new ArrayList<>();
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG) {
                if(desk.canWangLaiZi()) {
                    wangNum++;
                    continue;
                }else{
                    return null;
                }
            }
            cardTemp.add((byte) (b & 0x0f));
        }
        Map<Byte,Byte> countMap = getListCountMap(cardTemp);
        List<Byte> countList = new ArrayList<>(countMap.values());
        Collections.sort(countList);
        if(countList.size() == 2 && (countList.get(1) + wangNum) == 3 && countList.get(0) == 2) {
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(mergeHuLuSortCardshand(sortHandCards(cards),wangNum));
            for (int i = 0; i < result.cardsSort.size(); i++) {
                byte b = result.cardsSort.get(i);
                if(i == 0 && (b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG)){
                    result.cardsReal.add(getCardValue(result.cardsSort.get(1)));
                }else {
                    result.cardsReal.add(getCardValue(b));
                }
            }
            result.cardType = PokerConstants.NN_CARDTYPE_HU_LU_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_HU_LU_NIU);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            result.cardsSortAndFenCha.addAll(result.cardsSort);
        }
        return result;
    }

    private NNBiPaiResult getZhaDanNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_ZHA_DAN_NIU)) return null;
        NNBiPaiResult result = null;
        int wangNum = 0;
        List<Byte> cardTemp = new ArrayList<>();
        for(byte b : cards){
//            cardTemp.add((byte) (b & 0x0f));
            cardTemp.add((byte)getCardValue(b));
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG) {
                if(desk.canWangLaiZi()) {
                    wangNum++;
                    continue;
                }else{
                    return null;
                }
            }
        }
        Map<Byte,Byte> countMap = getListCountMap(cardTemp);
        List<Byte> countList = new ArrayList<>(countMap.values());
        Collections.sort(countList);
        int maxCount = countList.get(countList.size() - 1);
        if(maxCount + wangNum >= 4) {
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(mergeZhaDanSortCardshand(sortHandCards(cards)));
            for (int i = 0; i < 4; i++) {
                result.cardsReal.add(getCardValue(result.cardsSort.get(3)));
            }
            result.cardsReal.add(getCardValue(result.cardsSort.get(4)));
            result.cardType = PokerConstants.NN_CARDTYPE_ZHA_DAN_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_ZHA_DAN_NIU);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            result.cardsSortAndFenCha.addAll(result.cardsSort);
        }
        return result;
    }

    private NNBiPaiResult getShunJinNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_SHUN_JIN_NIU)) return null;
        NNBiPaiResult result = null;
        List<Integer> cardTemp = new ArrayList<>();
        int color = (cards.get(0) >> 4) & 0xff;
        int wangNum = 0;
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG) {
                if(desk.canWangLaiZi()) {
                    wangNum++;
                    continue;
                }else {
                    return null;
                }
            }
            if(((b >> 4) & 0xff) != color) return null;
            cardTemp.add(b & 0x0f);
        }

        List<Integer> shunZi = comboShunZi(cardTemp,wangNum);
        if(shunZi != null) {
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(sortHandCards(cards));
            result.cardsReal = shunZi;
            result.cardType = PokerConstants.NN_CARDTYPE_SHUN_JIN_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_SHUN_JIN_NIU);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            result.cardsSortAndFenCha.addAll(result.cardsSort);
        }
        return result;
    }

    private NNBiPaiResult getWuXiaoNiuList(List<Byte> cards, MJDesk<byte[]> desk) {
        if(!desk.getNiuFanStr().containsKey(PokerConstants.NN_CARDTYPE_WU_XIAO_NIU)) return null;
        NNBiPaiResult result = null;
        boolean allCardLess5 = true;
        int total = 0;
        int wangNum = 0;
        for(byte b : cards){
            if(b == PokerConstants.POKER_CODE_DA_WANG || b == PokerConstants.POKER_CODE_XIAO_WANG) {
                if(desk.canWangLaiZi()) {
                    wangNum++;
                    continue;
                }else{
                    return null;
                }
            }
            int value = b & 0x0f;
            if(value >= 5) allCardLess5 = false;
            total += value;
        }
        if(allCardLess5 && (total + wangNum) <= 10){
            result = new NNBiPaiResult();
            result.cardsInHand.addAll(cards);
            result.cardsSort.addAll(sortHandCards(cards));
            for(byte b : result.cardsSort){
                result.cardsReal.add(getCardValue(b));
            }
            result.cardType = PokerConstants.NN_CARDTYPE_WU_XIAO_NIU;
            result.cardMultiple = desk.getNiuFanStr().get(PokerConstants.NN_CARDTYPE_WU_XIAO_NIU);
            result.cardTypeStr = result.cardType + "X"+result.cardMultiple;
            result.cardsSortAndFenCha.addAll(result.cardsSort);
        }
        return result;
    }

    private int getCardValue(Byte card) {
        int cardValue;
        if(card == PokerConstants.POKER_CODE_DA_WANG){
            cardValue = PokerConstants.DA_WANG_COLOR_VALUE;
        } else if(card == PokerConstants.POKER_CODE_XIAO_WANG){
            cardValue = PokerConstants.XIAO_WANG_COLOR_VALUE;
        } else {
            cardValue = card & 0x0f;
        }
        return cardValue;
    }

    private List<Integer> comboShunZi(List<Integer> cardsValueList, int wangNum) {
        if(cardsValueList == null || cardsValueList.size() + wangNum != 5) return null;
        Collections.sort(cardsValueList);
        int min = cardsValueList.get(0);
        if(min == 1){//考虑下10,J,Q,K,A,此时A是1
            List<Integer> shunZi = new ArrayList<>(Arrays.asList(min+4, min + 3, min + 2, min + 1, min));
            List<Integer> temp = new ArrayList<>(cardsValueList);
            shunZi.removeAll(temp);
            if(shunZi.size() == wangNum){
                return shunZi;
            }else{
                temp.remove(0);
                temp.add(14);
                min = 10;
                shunZi = new ArrayList<>(Arrays.asList(min+4, min + 3, min + 2, min + 1, min));
                shunZi.removeAll(temp);
                if(shunZi.size() == wangNum){
                    return new ArrayList<>(Arrays.asList(min+4, min + 3, min + 2, min + 1, min));
                }
            }
        }else {
            if(min > 10) min = 10;
            List<Integer> shunZi = new ArrayList<>(Arrays.asList(min+4, min + 3, min + 2, min + 1, min));
            List<Integer> temp = new ArrayList<>(cardsValueList);
            shunZi.removeAll(temp);
            if(shunZi.size() == wangNum){
                return new ArrayList<>(Arrays.asList(min+4, min + 3, min + 2, min + 1, min));
            }
        }
        return null;
    }

    private List<Byte> mergeHuLuSortCardshand(List<Byte> cards, int wangNum) {
        if(wangNum >=2 || cards==null || cards.size() != 5) return null;
        if(wangNum == 1){
            return cards;
        } else {
            Byte a1 = cards.get(0);
            Byte a2 = cards.get(1);
            Byte a3 = cards.get(2);
            Byte a4 = cards.get(3);
            Byte a5 = cards.get(4);
            if((a1 & 0x0f) == (a3 & 0x0f)){//前面三个一样
                return new ArrayList<>(Arrays.asList(a1,a2,a3,a4,a5));
            }else{//后面三个一样
                return new ArrayList<>(Arrays.asList(a3,a4,a5,a1,a2));
            }
        }
    }

    public List<Byte> mergeZhaDanSortCardshand(List<Byte> cards) {
        List<Byte> temp = new ArrayList<>(cards);
        boolean hasDaWang = temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_DA_WANG));
        boolean hasXiaoWang = temp.remove(Byte.valueOf(PokerConstants.POKER_CODE_XIAO_WANG));
        List<Byte> valueList = new ArrayList<>();

        for(Byte b : temp){
            valueList.add((byte)getCardValue(b));
        }

        boolean isAllValueSame = true;
        byte first = (byte)getCardValue(valueList.get(0));

        for(Byte b : valueList){
            if (b != first) {
                isAllValueSame = false;
                break;
            }
        }

        if (isAllValueSame) return cards;

        Map<Byte,Byte> countMap = getListCountMap(valueList);
        int four = 0;
        int one = 0;
        for(Map.Entry<Byte,Byte> entry : countMap.entrySet()){
            if(entry.getValue() >= 2) four = entry.getKey();
            if(entry.getValue() == 1) one = entry.getKey();
        }
        List<Byte> result = new ArrayList<>();
        if (hasDaWang) result.add((byte)PokerConstants.POKER_CODE_DA_WANG);
        if (hasXiaoWang) result.add((byte)PokerConstants.POKER_CODE_XIAO_WANG);
        for(Byte b : cards){
            if(getCardValue(b)== four){
                result.add(b);
            }
        }
        for(Byte b : cards){
            if(getCardValue(b) == one){
                result.add(b);
            }
        }
        return result;
    }

    public int isFuliPlayerAndFaPai(MJDesk<byte[]> desk, GameData gameData) {
        List<Integer> positionList = new ArrayList<>();
        for(PlayerInfo pl : desk.getPlayingPlayers()) {
            List<Integer> juNumList = gameData.fuliPlayerMap.get(pl.playerId);
            if (juNumList != null && juNumList.contains(gameData.handNum))
                positionList.add(pl.position);
        }
        if(positionList.isEmpty()) return -1;
        Collections.shuffle(positionList);
        return positionList.get(0);
    }

    public List<Byte> fuli(int playerNum, List<Byte> cards,int everyPlayerCardNum,MJDesk<byte[]> desk) {
        Map<List<Byte>,NNBiPaiResult> map = new HashMap<>();
        for (int i = 0; i < playerNum * everyPlayerCardNum; i++) {
            if(i % everyPlayerCardNum != 0) continue;
            List<Byte> key = new ArrayList<>();
            for (int j = 0; j < everyPlayerCardNum; j++) {
                key.add(cards.get(i+j));
            }
            map.put(key,getCardsResult(key,desk));
        }
        for (int i = 0; i < playerNum * everyPlayerCardNum; i++) {
            cards.remove(0);
        }
        List<Map.Entry<List<Byte>,NNBiPaiResult>> list = new ArrayList<>(map.entrySet());
        list.sort((o1,o2) -> {
            return biPai(o1.getValue(),o2.getValue());
        });

        List<Byte> resultList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<List<Byte>,NNBiPaiResult> entry = list.get(i);
            resultList.addAll(entry.getKey());
        }
        return resultList;
    }


    //第二个大 为正数,相等为0,第二个小 为负数(牛牛之中木有相等)
    public int biPai(NNBiPaiResult biPai1, NNBiPaiResult biPai2) {
        if(biPai1.cardType != biPai2.cardType) return biPai2.cardType - biPai1.cardType;
        if(biPai1.cardType >= PokerConstants.NN_CARDTYPE_WU_HUA_NIU) {
            int biPaiWithVaule = biPaiWithValue(biPai1.cardsReal, biPai2.cardsReal);
            if (biPaiWithVaule != 0) return biPaiWithVaule;
        }
        return biPaiWithColorAndMaxPaiZhi(biPai1.cardsSort,biPai2.cardsSort);
    }

    private int biPaiWithValue(List<Integer> cardsReal, List<Integer> cardsReal1) {
        for (int i = 0; i < cardsReal.size(); i++) {
            int a = cardsReal.get(i);
            int b = cardsReal1.get(i);
            if(a != b) return b-a;
        }
        return 0;
    }

    private int biPaiWithColorAndMaxPaiZhi(List<Byte> cardsSort, List<Byte> cardsSort1) {
        int maxValue1 = getCardValue(cardsSort.get(0));
        int maxValue2 = getCardValue(cardsSort1.get(0));
        if(maxValue1 != maxValue2) return maxValue2 - maxValue1;
        int color1 = cardsSort.get(0) >> 4 & 0xff;
        int color2 = cardsSort1.get(0) >> 4 & 0xff;
        return color2 - color1;
    }
}