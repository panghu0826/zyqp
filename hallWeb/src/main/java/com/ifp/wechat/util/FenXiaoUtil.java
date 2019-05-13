package com.ifp.wechat.util;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FenXiaoUtil {
    private static  final String pay_url = "http://www.xskqpd.com/zywxfx/index.php/api/addRecord";
    private static  final String login_url = "http://www.xskqpd.com/zywxfx/index.php/api/addUser";
    private static  final String invite_url = "http://www.xskqpd.com/zywxfx/index.php/api/updateUserCode";
    public static BlockingQueue<Map<String, String>> msgQueue = new LinkedBlockingQueue<>();

    public static JSONObject pay(Map<String,String> map){
        JSONObject jsonObject = HTTPUtil.httpByPostInMapWithJava(pay_url,map);
        return jsonObject;
    }

    public static String login(Map<String,String> map){
        JSONObject jsonObject = HTTPUtil.httpByPostInMapWithJava(login_url,map);
        if(jsonObject==null){
            return "";
        }
        String code = jsonObject.getString("code");
        return code;
    }


    public static String submitInviteCode(Map<String,String> map) {
        JSONObject jsonObject = HTTPUtil.httpByPostInMapWithJava(invite_url,map);
        return jsonObject.getString("code");
    }

    public void payLoop(){
        try {
//            System.out.println("-----------------------------------------------------------------aaaa");
            Map<String, String> map = msgQueue.poll(100, TimeUnit.MILLISECONDS);
            if(map != null && !map.isEmpty()){
                System.out.println("----pay------");
//                Thread.sleep(30000);
                pay(map);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("pay fail===========");
        }
    }

    public static void main(String[] args) {
        Map<String, String> m = new HashMap<>();
//        m.put("unionid", "onnkgwNj7NVZW3hSIH0rADzpMMoA");
        m.put("clubid", "176");
        String url = "http://www.xskqpd.com/zywxfx/majiang_wx/julebu.html";
        JSONObject jsonObject = HTTPUtil.httpByPostInMapWithJava(url,m);
        if(jsonObject==null){
            return;
        }
        String code = jsonObject.getString("rspMsg");
        System.out.println(code);
        System.out.println();
    }
}
