import java.security.SecureRandom;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;


public class DES {
public DES() {
}

public static void main(String args[]) {

String str = "asdfasdfasd";

String password = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";

byte[] result = DES.encrypt(str.getBytes(),password);
System.out.println("aftter："+new String(result));


try {
byte[] decryResult = DES.decrypt(result, password);
System.out.println("before："+new String(decryResult));
} catch (Exception e1) {
e1.printStackTrace();
}

}


public static byte[] encrypt(byte[] datasource, String password) { 
try{
SecureRandom random = new SecureRandom();
DESKeySpec desKey = new DESKeySpec(password.getBytes());

SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
SecretKey securekey = keyFactory.generateSecret(desKey);

Cipher cipher = Cipher.getInstance("DES");

cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

return cipher.doFinal(datasource);
}catch(Throwable e){
e.printStackTrace();
}
return null;
}

public static byte[] decrypt(byte[] src, String password) throws Exception {

SecureRandom random = new SecureRandom();

DESKeySpec desKey = new DESKeySpec(password.getBytes());

SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

SecretKey securekey = keyFactory.generateSecret(desKey);

Cipher cipher = Cipher.getInstance("DES");

cipher.init(Cipher.DECRYPT_MODE, securekey, random);

return cipher.doFinal(src);
}
}