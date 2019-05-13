package com.buding.poker.zjh;

import java.util.*;

public class ZJHUtil {

    public static TreeMap<Integer,List<List<Byte>>> resultMap = new TreeMap<>();
    public static HashMap<List<Byte>,Integer> paiMap = new HashMap<>();

    static {
        resultMap = getAllMap();
        for(Map.Entry<Integer,List<List<Byte>>> entry : resultMap.entrySet()){
            for(List<Byte> cards : entry.getValue()){
                paiMap.put(cards,entry.getKey());
            }
        }
        System.out.println(paiMap.size());
    }

    public static TreeMap<Integer,List<List<Byte>>> getAllMap() {
        long time = System.currentTimeMillis();
        TreeMap<Integer,List<List<Byte>>> resultMap = new TreeMap<>();
        List<Byte> cardValueList = new ArrayList<>();
        List<Byte> colorList = new ArrayList<>();
        for (byte i = 2; i <= 14; i++) {
            cardValueList.add(i);
        }
        for (byte i = 0; i <= 3; i++) {
            colorList.add(i);
        }

        //豹子,选择一个牌值(13),选择去掉一个颜色(4)
//        List<List<Byte>> baoZiList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> baoZiMap = new HashMap<>();
        int num_baozi = 14+156+274+12+274+12;
        List<Byte> colorListTemp = new ArrayList<>();
        for (int j = 14; j >= 2; j--) {
            num_baozi--;
            baoZiMap.put(num_baozi,new ArrayList<>());
            for (int i = 0; i <= 3; i++) {
                colorListTemp.clear();
                colorListTemp.addAll(colorList);
                colorListTemp.remove(Byte.valueOf(i+""));
                List<Byte> combo = new ArrayList<>();
                for(Byte color : colorListTemp){
                    combo.add((byte) ((color << 4) | j));
                }
//                baoZiList.add(combo);
                baoZiMap.get(num_baozi).add(combo);
            }
        }
        System.out.println("豹子牌值:"+(156+274+12+274+12+1)+"-"+(156+274+12+274+12+13));

        //顺金
//        int num_shunjin = 156+274+12+274+12;
//        List<List<Byte>> shunJinList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> shunJinMap = new HashMap<>();
        for (int j = 0; j <= 3; j++) {
            for (int i = 2; i <= 12; i++) {
                List<Byte> combo = new ArrayList<>();
                combo.add((byte) ((j << 4) | i));
                combo.add((byte) ((j << 4) | (i + 1)));
                combo.add((byte) ((j << 4) | (i + 2)));
//                Collections.sort(combo);
//                shunJinList.add(cardsWithoutColorCombo);
                if(shunJinMap.get(i+156+274+12+274) == null){
                    shunJinMap.put(i+156+274+12+274,new ArrayList<>());
                }
                shunJinMap.get(i+156+274+12+274).add(combo);
            }
            List<Byte> combo = new ArrayList<>();
            combo.add((byte) ((j << 4) | 14));
            combo.add((byte) ((j << 4) | 2));
            combo.add((byte) ((j << 4) | 3));
//            Collections.sort(combo);
            if(shunJinMap.get(1+156+274+12+274) == null){
                shunJinMap.put(1+156+274+12+274,new ArrayList<>());
            }
            shunJinMap.get(1+156+274+12+274).add(combo);
//            shunJinList.add(cardsWithoutColorCombo);
        }
        System.out.println("顺金牌值:"+(156+274+12+274+1)+"-"+(156+274+12+274+12));

//        System.out.println(shunJinMap.size());
//        System.out.println(shunJinMap.size()*shunJinMap.get(720).size());
        //金花
//        List<List<Byte>> jinHuaList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> jinHuaMap = new HashMap<>();
        int num_jinHua = 156+274+12+275;
        for (int i =14; i >= 2; i--) {//最大的牌
            for (int j = i - 1; j >= 2; j--) {//次大的牌
                for (int k = j - 1; k >=2; k--) {//最小的牌
                    if( i == 14 && j == 3 && k == 2) continue;//排除1.2.3,特殊的
                    if( i == j + 1 && i == k + 2) continue;//排除顺子
                    num_jinHua--;
                    jinHuaMap.put(num_jinHua,new ArrayList<>());
                    for (int l = 0; l <= 3; l++) {
                        List<Byte> combo = new ArrayList<>();
                        combo.add((byte)((l << 4) | i));
                        combo.add((byte)((l << 4) | j));
                        combo.add((byte)((l << 4) | k));
//                        Collections.sort(combo);
//                        jinHuaList.add(combo);
                        jinHuaMap.get(num_jinHua).add(combo);
                    }
                }
            }
        }
//        System.out.println(156+274+12+275-num_jinHua);
//        System.out.println(jinHuaMap);
//        System.out.println(jinHuaList);
        System.out.println("金花牌值:"+(156+274+12+1)+"-"+(156+274+12+274));

        //顺子
//        List<List<Byte>> shunZiList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> shunZiMap = new HashMap<>();
        for (int i = 0; i <=3 ; i++) {
            for (int j = 0; j <=3 ; j++) {
                for (int k = 0; k <=3 ; k++) {
                    if(i == k && k == j) continue;
                    List<Byte> combo = new ArrayList<>();
                    combo.add((byte)((i << 4) | 14));
                    combo.add((byte)((j << 4) | 2));
                    combo.add((byte)((k << 4) | 3));
//                    Collections.sort(combo);
                    if(shunZiMap.get(1+156+274) == null){
                        shunZiMap.put(1+156+274,new ArrayList<>());
                    }
                    shunZiMap.get(1+156+274).add(combo);
                    for (int l = 2; l <= 12; l++) {
                        List<Byte> combo2 = new ArrayList<>();
                        combo2.add((byte)((i << 4) | l));
                        combo2.add((byte)((j << 4) | (l + 1)));
                        combo2.add((byte)((k << 4) | (l + 2)));
//                        Collections.sort(combo2);
//                        if(!shunJinList.contains(combo2)) {
                        if(shunZiMap.get(l+156+274) == null){
                            shunZiMap.put(l+156+274,new ArrayList<>());
                        }
                        shunZiMap.get(l+156+274).add(combo2);
//                        }
                    }
                }
            }
        }
        System.out.println("顺子牌值:"+(156+274+1)+"-"+(156+274+12));

//        System.out.println(shunZiMap.size());
//        System.out.println(shunZiMap.size() * shunZiMap.get(432).size());
        //对子
        int num_duiZi = 157+274;
//        List<List<Byte>> duiZiList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> duiZiMap = new HashMap<>();
        for (int i = 14; i >= 2; i--) {//对子值
            for (int j = 14; j >=2 ; j--) {//单牌值
                if(i == j) continue;//三个值一样是豹子,过虑
                num_duiZi--;
                duiZiMap.put(num_duiZi,new ArrayList<>());
                List<List<Byte>> duiZiColorList = new ArrayList<>();
                for (int k = 0; k <=3 ; k++) {//对子第一个牌花色
                    for (int l = 0; l <=3 ; l++) {//对子第二个牌花色
                        List<Byte> duiZicolorCombo = new ArrayList<>();
                        duiZicolorCombo.add((byte) k);
                        duiZicolorCombo.add((byte) l);
                        Collections.sort(duiZicolorCombo);
                        if(duiZiColorList.contains(duiZicolorCombo)) continue;
                        duiZiColorList.add(duiZicolorCombo);//两个值一样花色组合一样的,比如黑桃,红桃和红桃,黑桃一样的,过滤
                        for (int m = 0; m <=3 ; m++) {//单牌花色
                            if(k == l) continue;//两张牌花色,肯定不一样
                            List<Byte> combo = new ArrayList<>();
                            combo.add((byte)((k << 4) | i));
                            combo.add((byte)((l << 4) | i));
                            combo.add((byte)((m << 4) | j));
//                            Collections.sort(combo);
//                            duiZiList.add(combo);
                            duiZiMap.get(num_duiZi).add(combo);
                        }
                    }
                }
            }
        }
        System.out.println("对子牌值:"+(274+1)+"-"+(274+156));
//        System.out.println(num_duiZi);
//        System.out.println(duiZiMap.size());
//        System.out.println(duiZiMap);
        //散牌
//        List<List<Byte>> sanPaiList = new ArrayList<>();
        Map<Integer,List<List<Byte>>> sanPaiMap = new HashMap<>();
        int num = 275;
        for (int i = 14; i >= 2; i--) {//第一个牌,最大的牌
            for (int j = i - 1; j >=2 ; j--) {//第二个牌,次大的牌
                for (int k = j - 1; k >=2 ; k--) {//第三个牌,最xiao的牌
                    if( i == 14 && j == 3 && k == 2) continue;//排除1.2.3,特殊的
                    if( i == j + 1 && i == k + 2) continue;//排除顺子
                    num--;
                    sanPaiMap.put(num,new ArrayList<>());
                    for (int l = 0; l <=3 ; l++) {//第一个花色
                        for (int m = 0; m <=3 ; m++) {//第二个花色
                            for (int n = 0; n <=3 ; n++) {//第三个花色
                                if(l == m && m == n) continue;//排除同花
                                List<Byte> combo = new ArrayList<>();
                                combo.add((byte)((l << 4) | i));
                                combo.add((byte)((m << 4) | j));
                                combo.add((byte)((n << 4) | k));
//                                Collections.sort(combo);
//                                sanPaiList.add(combo);
                                sanPaiMap.get(num).add(combo);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("散牌牌值:"+(0+1)+"-"+(0+274));

//        System.out.println(num);
//        Collections.reverse(sanPaiMap);
//        System.out.println(sanPaiMap);
//        System.out.println(sanPaiList);
        resultMap.putAll(baoZiMap);
        resultMap.putAll(shunJinMap);
        resultMap.putAll(shunZiMap);
        resultMap.putAll(jinHuaMap);
        resultMap.putAll(duiZiMap);
        resultMap.putAll(sanPaiMap);
//        List<Integer> list = new ArrayList<>(resultMap.keySet());
//        System.out.println(list.get(list.size()-1));
//        System.out.println(list.size());
        long end = System.currentTimeMillis();
        System.out.println("花费时间--"+(end - time)+"ms");
        return resultMap;
    }

    public static List<Byte> fuli(int playerNum, List<Byte> cards) {
        Map<List<Byte>,Integer> result = new HashMap<>();
        for (int i = 0; i < playerNum * 3; i++) {
            if(i % 3 != 0) continue;
            List<Byte> key = new ArrayList<>();
            key.add(cards.get(i));
            key.add(cards.get(i+1));
            key.add(cards.get(i+2));
            List<List<Byte>> keys = getComboKeys(key);
            for(List<Byte> k : keys){
                if(paiMap.get(k) != null) {
                    result.put(k, paiMap.get(k));
                    break;
                }
            }
        }
        for (int i = 0; i < playerNum * 3; i++) {
            cards.remove(0);
        }

        List<Map.Entry<List<Byte>,Integer>> list = new ArrayList<>(result.entrySet());
        list.sort((o1, o2) -> {
            //降序
            return o2.getValue() - o1.getValue();
        });
        List<Byte> resultList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map.Entry<List<Byte>,Integer> entry = list.get(i);
            System.out.println(entry.getKey()+ ":"+entry.getValue());
            resultList.addAll(entry.getKey());
        }
        resultList.addAll(cards);
        System.out.println(resultList);
        return resultList;
    }

    private static List<List<Byte>> getComboKeys(List<Byte> key) {
        List<List<Byte>> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(i == j) continue;
                for (int k = 0; k < 3; k++) {
                    if(i == k || j == k) continue;
                    List<Byte> l = new ArrayList<>();
                    l.add(key.get(i));
                    l.add(key.get(j));
                    l.add(key.get(k));
                    result.add(l);
                }
            }
        }
        return result;
    }
}
