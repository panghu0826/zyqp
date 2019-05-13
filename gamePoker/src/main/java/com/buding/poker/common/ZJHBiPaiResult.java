package com.buding.poker.common;

import java.util.List;
import java.util.Map;

public class ZJHBiPaiResult {
    public int pos;//发起比牌人座位号
    public int biPaipos;//被比牌人的座位号
    public int winnerPos;//赢的人座位号
    public List<Byte> cards;//发起比牌人手牌(大>小排列)
    public List<Byte> biPaiCards;//被比牌人手牌
    public Map<List<Byte>,Integer> cardsInReal;//(如果有王)变换后的真实手牌
    public Map<List<Byte>,Integer> biPaiCardsInReal;//(如果有王)变换后的真实手牌
    public List<Byte> cardsInRealMax;//(如果有王)变换后的真实手牌最大的那种变法
    public List<Byte> biPaiCardsInRealMax;//(如果有王)变换后的真实手牌最大的那种变法
    public int xiQian = 0;//如果有金花/豹子,喜钱多少
    public int biPaiXiQian = 0;//如果有金花/豹子,喜钱多少
}
