package com.ifp.wechat.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ifp.wechat.constant.ConstantWeChat;
import com.ifp.wechat.entity.AccessToken;
import com.ifp.wechat.entity.JsSign;
import com.ifp.wechat.util.WeixinUtil;

/**
 * 验证签名
 * @author caspar.chen
 * @version 1.0
 * 
 */
public class SignService {
	static Map<String, TokenCache> tokenCache = new HashMap<String, TokenCache>();

	/**
	 * 认证微信签名
	 * @param request
	 * @return	是否成功
	 */
	public static boolean checkSignature(HttpServletRequest request) {
		// 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		
		return checkSignature(signature,timestamp,nonce);
	}

	public static String generateNonceStr(int len) {
		// 密码字符集，可任意添加你需要的字符
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String str = "";
        for (int i = 0; i < len; i++) {
            str += chars.charAt(new Random().nextInt(chars.length()));
        }
        return str;
	}
	
	public static JsSign genJsSign(String url, String appid, String appsecret) throws Exception {	
		TokenCache cache = tokenCache.get(appid);
		AccessToken token = null;
		if(cache == null || (System.currentTimeMillis() - cache.time > 3600000)) {
			token = WeixinUtil.getAccessToken(appid, appsecret);
			token = WeixinUtil.getJSTicket(token.getToken());
			cache = new TokenCache();
			cache.token = token;
			cache.time = System.currentTimeMillis();
			tokenCache.put(appid, cache);
		} else {
			token = cache.token;
		}
		
		System.out.println("appid=" + appid+"\r\nsecret:" + appsecret);
		
		long timestamp = System.currentTimeMillis()/1000;
		String noncestr = generateNonceStr(16);
		int pos = url.indexOf("#");
		if(pos > -1) {
			url = url.substring(0, pos);
		}
		url = url.trim();
		if(StringUtils.isBlank(url)) {
			return null;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("timestamp", timestamp+"");
		params.put("noncestr", noncestr);
		params.put("url", url);
		params.put("jsapi_ticket", token.getToken());
		String sign = getSignature(params);
//		sign = SHA1.getSHA1(token.getToken(), timestamp+"", noncestr, url);
		if(sign == null) {
			return null;
		}
		JsSign s = new JsSign();
		s.appid = appid;
		s.noncestr = noncestr;
		s.timestamp = timestamp;
		s.url = url;
		s.signature = sign;
		return s;
	}
	
	public static String getSignature(Map<String, String> params) {
		String[] arr = new String[params.size()];
		params.keySet().toArray(arr);
		Arrays.sort(arr);
		StringBuilder sb = new StringBuilder();
		for(String key : arr) {
			String val = params.get(key);
			sb.append(key).append("=").append(val).append("&");
			System.out.println(key + "=" + val);
		}
		sb.deleteCharAt(sb.length() - 1);
		MessageDigest md = null;
		String tmpStr = null;
		
//		System.out.println(sb.toString());

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(sb.toString().getBytes("utf-8"));
			tmpStr = byteToStr(digest).toLowerCase();
			System.out.println(tmpStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmpStr;
	}
	
	/**
	 * 验证签名
	 * 
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return 是否验证成功
	 */
	public static boolean checkSignature(String signature, String timestamp,
			String nonce) {
		String[] arr = new String[] { ConstantWeChat.TOKEN, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		content = null;
		// 将sha1加密后的字符串可与signature对比
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
	}

	/**
	 * 将字节数组转换为十六进制字符串
	 * 
	 * @param byteArray
	 * @return
	 */
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 * 
	 * @param mByte
	 * @return
	 */
	private static String byteToHexStr(byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}
	
	public static void main(String[] args) throws Exception {
		JsSign sign = SignService.genJsSign("http://hall.budinggames.com", "wxc04caaaeff586684", "d4624c36b6795d1d99dcf0547af5443d");
	}
}
