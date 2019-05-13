package NewWXPayment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

	// 灏忓啓鐨勫瓧绗︿覆
	private static char[] DigitLower = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	// 澶у啓鐨勫瓧绗︿覆
	private static char[] DigitUpper = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * 榛樿鏋勯�鍑芥暟
	 *
	 */
	public MD5Utils() {
	}

	/**
	 * 鍔犲瘑涔嬪悗鐨勫瓧绗︿覆鍏ㄤ负灏忓啓
	 *
	 * @param srcStr
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NullPointerException
	 */
	public static String getMD5Lower(String srcStr)
			throws NoSuchAlgorithmException {
		String sign = "lower";
		return processStr(srcStr, sign);
	}

	/**
	 * 鍔犲瘑涔嬪悗鐨勫瓧绗︿覆鍏ㄤ负澶у啓
	 *
	 * @param srcStr
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NullPointerException
	 */
	public static String getMD5Upper(String srcStr)
			throws NoSuchAlgorithmException {
		String sign = "upper";
		return processStr(srcStr, sign);
	}

	private static String processStr(String srcStr, String sign)
			throws NoSuchAlgorithmException, NullPointerException {
		MessageDigest digest;
		// 瀹氫箟璋冪敤鐨勬柟娉�
		String algorithm = "MD5";
		// 缁撴灉瀛楃涓�
		String result = "";
		// 鍒濆鍖栧苟寮�杩涜璁＄畻
		digest = MessageDigest.getInstance(algorithm);
		digest.update(srcStr.getBytes());
		byte[] byteRes = digest.digest();

		// 璁＄畻byte鏁扮粍鐨勯暱搴�
		int length = byteRes.length;

		// 灏哹yte鏁扮粍杞崲鎴愬瓧绗︿覆
		for (int i = 0; i < length; i++) {
			result = result + byteHEX(byteRes[i], sign);
		}

		return result;
	}

	/**
	 * 灏哹tye鏁扮粍杞崲鎴愬瓧绗︿覆
	 *
	 * @param bt
	 * @return
	 */
	private static String byteHEX(byte bt, String sign) {

		char[] temp = null;
		if (sign.equalsIgnoreCase("lower")) {
			temp = DigitLower;
		} else if (sign.equalsIgnoreCase("upper")) {
			temp = DigitUpper;
		} else {
			throw new java.lang.RuntimeException("");
		}
		char[] ob = new char[2];

		ob[0] = temp[(bt >>> 4) & 0X0F];

		ob[1] = temp[bt & 0X0F];

		return new String(ob);
	}

	 public static String getMD5(String content) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(content.getBytes("UTF-8"));
        } catch (Exception e) {
        	e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }


}
