package com.ifp.wechat.test;
import java.util.HashMap;  
import java.util.Map;  
//对接口进行测试  
public class TestMain {  
    private String charset = "utf-8";  
    private HttpClientUtil httpClientUtil = null;  
      
    public TestMain(){  
        httpClientUtil = new HttpClientUtil();  
    }  
      
    public void test(){  
    	System.setProperty( "javax.net.ssl.trustStore",   "/data/server/buding/server/projectDir/gamewxtomcat.keystore"); 
    	  System.setProperty( "javax.net.ssl.trustStorePassword",   "123456");
        String httpOrgCreateTest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxcac9625c1385e1b3&secret=90dd20711e1f118b8588f9715cd032b7&code=031d7763af7b77108dc19111f2111f2A&grant_type=authorization_code"; 
        String httpOrgCreateTestRtn = httpClientUtil.doPost(httpOrgCreateTest, new HashMap<String, String>(),charset);  
        System.out.println("result:"+httpOrgCreateTestRtn);  
    }  
      
    public static void main(String[] args){  
        TestMain main = new TestMain();  
        main.test();  
    }  
}  