import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
public class AESCode {
/**
	 * 提供密钥和向量进行加密
	 * 
	 * @param sSrc
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String Encrypt(String sSrc, byte[] key, byte[] iv) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec _iv = new IvParameterSpec(iv);// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, _iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
		return Base64.encodeBase64String(encrypted);
	}
	/**
	 * 提供密钥和向量进行解密
	 * 
	 * @param sSrc
	 * @param key
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static String Decrypt(String sSrc, byte[] key, byte[] iv) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec _iv = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, _iv);
		byte[] encrypted = Base64.decodeBase64(sSrc);
		byte[] original = cipher.doFinal(encrypted);
		return new String(original, "utf-8");
	}
	/**
	 * 使用密钥进行加密
	 * 
	 * @param sSrc
	 * @param keyStr
	 * @return
	 * @throws Exception
	 */
	public static String Encrypt(String sSrc, String keyStr) throws Exception {
		byte[] key = GeneralKey(keyStr);
		byte[] iv = GeneralIv(keyStr);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec _iv = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, _iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
		return Base64.encodeBase64String(encrypted);
	}
	/**
	 * 使用密钥进行解密
	 * 
	 * @param sSrc
	 * @param keyStr
	 * @return
	 * @throws Exception
	 */
	public static String Decrypt(String sSrc, String keyStr) throws Exception {
		byte[] key = GeneralKey(keyStr);
		byte[] iv = GeneralIv(keyStr);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec _iv = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, _iv);
		byte[] encrypted = Base64.decodeBase64(sSrc);// 先用base64解码
		byte[] original = cipher.doFinal(encrypted);
		return new String(original, "utf-8");
	}
	/**
	 * 构建密钥字节码
	 * 
	 * @param keyStr
	 * @return
	 * @throws Exception
	 */
	private static byte[] GeneralKey(String keyStr) throws Exception {
		byte[] bytes = keyStr.getBytes("utf-8");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(bytes);
		return md.digest();
	}
	/**
	 * 构建加解密向量字节码
	 * 
	 * @param keyStr
	 * @return
	 * @throws Exception
	 */
	private static byte[] GeneralIv(String keyStr) throws Exception {
		byte[] bytes = keyStr.getBytes("utf-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(bytes);
		return md.digest();
	}
}