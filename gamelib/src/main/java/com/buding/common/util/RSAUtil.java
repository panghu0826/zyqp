package com.buding.common.util;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

public class RSAUtil {

	/**
	 * 生成RSA的公钥和私钥
	 * @param pubkeyfile
	 * @param privatekeyfile
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void makekeyfile(String pubkeyfile, String privatekeyfile) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// 初始化密钥对生成器，密钥大小为1024位
		keyPairGen.initialize(1024);
		// 生成一个密钥对，保存在keyPair中
		KeyPair keyPair = keyPairGen.generateKeyPair();

		// 得到私钥
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		// 得到公钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// 生成私钥
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(privatekeyfile));
		oos.writeObject(privateKey);
		oos.flush();
		oos.close();

		oos = new ObjectOutputStream(new FileOutputStream(pubkeyfile));
		oos.writeObject(publicKey);
		oos.flush();
		oos.close();

		System.out.println("make file ok!");
	}

	/**
	 * 
	 * @param k
	 * @param data
	 * @param encrypt
	 *            1 加密 0解密
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws Exception
	 */
	public static byte[] handleData(Key k, byte[] data, int encrypt) throws Exception {

		if (k != null) {

			Cipher cipher = Cipher.getInstance("RSA");

			if (encrypt == 1) {
				cipher.init(Cipher.ENCRYPT_MODE, k);
				byte[] resultBytes = cipher.doFinal(data);
				return resultBytes;
			} else if (encrypt == 0) {
				cipher.init(Cipher.DECRYPT_MODE, k);
				byte[] resultBytes = cipher.doFinal(data);
				return resultBytes;
			} else {
				System.out.println("参数必须为: 1 加密 0解密");
			}
		}
		return null;
	}

	public static PrivateKey initPrivateKey(String privateKeyFile) throws Exception {
    	byte[] key = IOUtil.getFileData(privateKeyFile);
		Security.addProvider(new BouncyCastleProvider());
		ByteArrayInputStream bais = new ByteArrayInputStream(key);
		PEMReader reader = new PEMReader(new InputStreamReader(bais), new PasswordFinder() {
			
			@Override
			public char[] getPassword() {
				return "".toCharArray();
			}
		});
		KeyPair keyPair = (KeyPair) reader.readObject();
		reader.close();
		PublicKey pubk = keyPair.getPublic();
		System.out.println(pubk);
		System.out.println("----------------------------");
		PrivateKey prik = keyPair.getPrivate();		
		System.out.println(prik);
		System.out.println("----------------------------");
		
		KeySpec keySpec = new X509EncodedKeySpec(pubk.getEncoded());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		System.out.println(keyFactory.generatePublic(keySpec));
		System.out.println("----------------------------");
		
		KeySpec keySpec2 = new PKCS8EncodedKeySpec(prik.getEncoded());
		System.out.println(keyFactory.generatePrivate(keySpec2));
		System.out.println("----------------------------");
		return keyPair.getPrivate();
	}
	    
	public static PublicKey initPublicKey(String pubKeyFile) throws Exception {
    	byte[] key = IOUtil.getFileData(pubKeyFile);
    	Security.addProvider(new BouncyCastleProvider());
		ByteArrayInputStream bais = new ByteArrayInputStream(key);
		PEMReader reader = new PEMReader(new InputStreamReader(bais), new PasswordFinder() {
			
			@Override
			public char[] getPassword() {
				return "".toCharArray();
			}
		});
		
		Object obj = reader.readObject();
		System.out.println(obj.getClass());
    	return (PublicKey)obj;
	}
	    
	public static void main(String[] args) throws Exception {

		String pubfile = "D:/ras_public_key.pem";
		String prifile = "D:/rsa_private_key.pem";

		 PrivateKey privateKey = initPrivateKey(prifile);
		 PublicKey publicKey = initPublicKey(pubfile);
////		 makekeyfile(pubfile, prifile);
//
//		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pubfile));
//		RSAPublicKey pubkey = (RSAPublicKey) ois.readObject();
//		ois.close();
//
//		ois = new ObjectInputStream(new FileInputStream(prifile));
//		RSAPrivateKey prikey = (RSAPrivateKey) ois.readObject();
//		ois.close();

		// 使用公钥加密
		String msg = "~O(∩_∩)O哈哈~";
		String enc = "UTF-8";
//
//		// 使用公钥加密私钥解密
		System.out.println("原文: " + msg);
		byte[] result = handleData(privateKey, msg.getBytes(enc), 1);
		System.out.println("加密: " + new String(result, enc));
		byte[] deresult = handleData(publicKey, result, 0);
		System.out.println("解密: " + new String(deresult, enc));

//		msg = "jaime";
//		// 使用私钥加密公钥解密
		System.out.println("原文: " + msg);
		byte[] result2 = handleData(publicKey, msg.getBytes(enc), 1);
		System.out.println("加密: " + new String(result2, enc));
		byte[] deresult2 = handleData(privateKey, result2, 0);
		System.out.println("解密: " + new String(deresult2, enc));
		
//		String str = "mYFS8z3jk+tJWftE5uA1ZlHEinyJEVkl7puw+OvFcym6JaWtM/SV6a3jj6wBqUQh"+
//"ESx7pzlCOnPePUf3Ds2Z/8HDPRv0beljjUT08iydLCU9bhjWJiEs/cZkuwXmTZrI"+
//"5Th61i6BT0DEhe0sM0ldTh/JiVlX+t7idW68C7hJLwtrcRdw8gUiyUOOp+v/FpJ5"+
//"QkNgIxHlPTfj5A==";
//		byte[] result3 = org.bouncycastle.util.encoders.Base64.decode(str);
//		byte[] deresult3 = handleData(privateKey, result3, 0);
//		System.out.println("解密: " + new String(deresult3, enc));
		
//		byte data[] = IOUtil.getFileData("C:/Users/Administrator/Desktop/themsg.txt");
//		data = Base64.decode(data);
//		byte[] deresult4 = handleData(privateKey, data, 0);
//		System.out.println("解密: " + new String(deresult4, enc));
	}
}