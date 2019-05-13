package com.buding.poker.common;

import java.util.ArrayList;
import java.util.List;

public class NNBiPaiResult {
    public List<Byte> cardsInHand = new ArrayList<>();//乱序手牌
    public List<Byte> cardsSort = new ArrayList<>();//按照牌值和花色排列的手牌
    public List<Byte> cardsSortAndFenCha = new ArrayList<>();//按照牌值和花色排列的手牌,牛几分叉显示
    public List<Integer> cardsReal = new ArrayList<>();//手牌牌的牌实际牌,没有王时就是cardsSort,比如10,J,Q,K,王,实际牌是10,11,12,13,14,又如7,8,9,10,王,实际牌是7.8.9.10.11,此参数不带花色,纯牌值
    public Integer cardType = -1;//手牌类型
    public Integer cardMultiple = 1;//手牌倍数
    public String cardTypeStr = "";//手牌倍数
}
