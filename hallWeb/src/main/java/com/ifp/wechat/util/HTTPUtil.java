package com.ifp.wechat.util;

import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jaime
 * 以下4种方式发送post,效率由快到慢为
 * httpByPostInMapWithJava>
 * httpByPostInStringWithJava=
 * httpByPostInStringWithApache>
 * httpByPostInMapWithApache
 */
public class HTTPUtil {
    public static Logger log = LogManager.getLogger(HTTPUtil.class);
    public static int TIMEOUT = 30 * 1000;
    public static String ENCODING = "UTF-8";


    public static JSONObject httpByPostInMapWithApache(String requestUrl, Map<String,String> params){
        JSONObject jsonObject = null;
        HttpClientUtil h = HttpClientUtil.getInstance();
        String responseStr = h.sendHttpPost(requestUrl,params);
        return JSONObject.fromObject(responseStr);
    }

    public static JSONObject httpByPostInStringWithApache(String requestUrl, String params){
        JSONObject jsonObject = null;
        HttpClientUtil h = HttpClientUtil.getInstance();
        String responseStr = h.sendHttpPost(requestUrl,params);
        return JSONObject.fromObject(responseStr);
    }

    public static JSONObject httpByPostInMapWithJava(String requestUrl, Map<String,String> params){
        JSONObject jsonObject = null;
        try{
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //POST Request Define:
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("POST");

            // POST params
            StringBuilder sbd = new StringBuilder();
            for(Map.Entry<String,String> entry:params.entrySet()){
                sbd.append(entry.getKey()+"=").append(entry.getValue()+"&");
            }
            sbd.deleteCharAt(sbd.length()-1);
            connection.getOutputStream().write(sbd.toString().getBytes());
            connection.connect();

            // response string
            JSONObject responseStr = getResponseStr(connection);

            // process response
            return responseStr;

        }catch(IOException e){
            log.error("http request error:" + requestUrl, e);
            return jsonObject;
        }
    }

    public static JSONObject httpByPostInStringWithJava(String requestUrl,String params){
        JSONObject jsonObject = null;
        try{
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //POST Request Define:
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("POST");

            // POST params
            connection.getOutputStream().write(params.getBytes());
            connection.connect();

            // response string
            JSONObject responseStr = getResponseStr(connection);

            // process response
            return responseStr;

        }catch(IOException e){
            log.error("http request error:" + requestUrl, e);
            return jsonObject;
        }
    }

    public static JSONObject httpByGETInStringWithJava(String requestUrl,String params){
        JSONObject jsonObject = null;
        try{
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //POST Request Define:
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("POST");

            // POST params
            connection.getOutputStream().write(params.getBytes());
            connection.connect();

            // response string
            JSONObject responseStr = getResponseStr(connection);

            // process response
            return responseStr;

        }catch(IOException e){
            log.error("http request error:" + requestUrl, e);
            return jsonObject;
        }
    }

    /**
     * 通过HttpConnection 获取返回的字符串
     * @param connection
     * @return
     * @throws IOException
     */
    public static JSONObject getResponseStr(HttpURLConnection connection)
            throws IOException{
        StringBuffer responseStr = new StringBuffer();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
            String inputLine = "";
            while ((inputLine = reader.readLine()) != null) {
                responseStr.append(inputLine);
            }
        }
        return JSONObject.fromObject(responseStr.toString());
    }

    public static void main(String[] args) {
//        String unionid = "onnkgwEEIjEn1ld0xSocMtpafX7A";
        Map<String,String> m = new HashMap<>();
//        m.put("unionid","onnkgwNu1gNaea5IZrVyaqqnQN70");
//        m.put("access_token","lAvQsWHoElh5EBMMVjgRBwkGrz4GEBmFhl1aXQJPHI_tb35o12XJ6AKZK5Ii9qyKAyLJCgpkcFUXsPZu6zo0eenWo_GQIFoCrFDCkVoKgZA");
        m.put("unionid","6666");
//        m.put("lang","zh_CN");
//        m.put("money","6");
//        m.put("pay_style","pocketpay");
//        m.put("item_type","0");
//        {
//            long t = System.currentTimeMillis();
//            JSONObject jsonObject = httpByPostInMapWithApache("http://p024.zhenlutech.com/index.php/api/addUser", m);
//            long t1 = System.currentTimeMillis();
//            System.out.println("httpByPostInMapWithApache====="+(t1 - t));
//        }
//        {
//            long t = System.currentTimeMillis();
//            JSONObject jsonObject = httpByPostInStringWithApache("http://p024.zhenlutech.com/index.php/api/addUser", "unionid=onnkgwOxlK6oCfFSQ3sfoR8xBv6A");
//            long t1 = System.currentTimeMillis();
//            System.out.println("httpByPostInStringWithApache==="+(t1 - t));
//        }
//        {
//            long t = System.currentTimeMillis();
//            JSONObject jsonObject = httpByPostInStringWithJava("http://p024.zhenlutech.com/index.php/api/addUser", "unionid=onnkgwOxlK6oCfFSQ3sfoR8xBv6A");
//            long t1 = System.currentTimeMillis();
//            System.out.println("httpByPostInStringWithJava====="+(t1 - t));
//        }
        {
            long t = System.currentTimeMillis();
            JSONObject jsonObject = httpByPostInMapWithJava("http://192.168.0.14/a/wx/fenxiao/openid", m);
            long t1 = System.currentTimeMillis();
            System.out.println("httpByPostInMapWithJava====="+(t1 - t));
            System.out.println(jsonObject);
        }


    }
}
