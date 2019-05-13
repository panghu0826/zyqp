package com.buding.common.network.codec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * DES加密 解密算法
 */
public class DesUtil {

    private final static String DES = "DES";
    private final static String ENCODE = "UTF-8";
    private final static String defaultKey = "test1234";
    public static void main(String[] args) throws Exception {
        String data = "测试ss";
        // System.err.println(encrypt(data, secretKey));
        // System.err.println(decrypt(encrypt(data, secretKey), secretKey));
        byte[] b = data.getBytes(ENCODE);
        System.out.println(b);
//        System.out.println(decrypt(encrypt(b,defaultKey.getBytes(ENCODE))).getBytes(ENCODE));

    }

    /**
     * 使用 默认key 加密
     * 
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        byte[] bt = encrypt(data, defaultKey.getBytes(ENCODE));
//        String strs = new BASE64Encoder().encode(bt);
        return bt;
    }

    /**
     * 使用 默认key 解密
     *
     */
    public static byte[] decrypt(byte[] data) throws  Exception {
        if (data == null) return null;
        byte[] bt = decrypt(data, defaultKey.getBytes(ENCODE));
        return bt;
    }


    public static byte[] encrypt(byte[] data,String secretKey) throws Exception {
        byte[] bt = encrypt(data, secretKey.getBytes(ENCODE));
        return bt;
    }

    public static byte[] decrypt(byte[] data, String secretKey) throws Exception {
        if (data == null) return null;
        byte[] bt = decrypt(data, secretKey.getBytes(ENCODE));
        return bt;
    }

    /**
     * Description 根据键值进行加密
     * 
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     * 
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }
}