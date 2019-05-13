package test;

import java.util.*;

/**
 * Created by Brioal on 2016/3/26.
 */
public class TreeMapTest {
    public TreeMapTest() {
//        //map按键升序(默认)
//        System.out.println("Map按键升序");
//        Map<Integer, Integer> map1 = new TreeMap<>();
//        map1.put(3, 2);
//        map1.put(4, 6);
//        map1.put(2, 5);
//        map1.put(1, 3);
//        IteratorPrint(map1);
//
//        //map按键降序
//        System.out.println("Map按键降序");
//        Map<Integer, Integer> map2 = new TreeMap<>(new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//
//                return o2 - o1;
//            }
//        });
//        map2.put(3, 2);
//        map2.put(4, 6);
//        map2.put(2, 5);
//        map2.put(1, 3);
//        IteratorPrint(map2);


        //map按value升降序
        System.out.println("Map按值升降序");
        Map<Integer, Integer> map3 = new TreeMap<>();
        map3.put(3, 2);
        map3.put(4, 6);
        map3.put(2, 5);
        map3.put(1, 3);
        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(map3.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                //升序
                return o1.getValue()-o2.getValue();
                //降序
//                return o2.getValue()-o1.getValue();
            }
        });
        ListPrint(list);
        System.out.println("--------");
        IteratorPrint(map3);
    }

    public void ListPrint(List<Map.Entry<Integer,Integer>> list) {
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<Integer, Integer> map = list.get(i);
            System.out.println(map.getKey()+":"+map.getValue());
        }
    }

    public void IteratorPrint(Map map) {
        Set<Integer> set = map.keySet();
        Iterator<Integer> iterator = set.iterator();
        while (iterator.hasNext()) {
            int key = iterator.next();
            int value = (int) map.get(key);
            System.out.println(key + ":" + value);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println((int)(Math.random()*4));
        }

    }
}