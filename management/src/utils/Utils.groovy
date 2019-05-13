package utils

import com.alibaba.fastjson.JSON
import com.github.kevinsawicki.http.HttpRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.GeneralSecurityException
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.security.SecureRandom;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;

/**
 * Created by Kerry on 2016/5/5.
 * update panjintao 2016/07/11 增加敏感词替换
 */
class Utils {
    // 固定collect的logger
    private static Logger logCollect = LoggerFactory.getLogger('collect')
    private static List sensitiveList = ["坐享财富增长", "安心享受成长", "尽享牛市", "欲购从速", "申购良机",
                                         "认购良机", "莫失良机", "名额有限", "份额有限", "低风险高收益", "黑马", "黑马推荐",
                                         "股票都没有问题", "肯定赚钱", "操作指令", "请跟随", "操作纪律",
                                         "关注幕后信息", "关注内部信息", "可靠内部消息", "通过内部人士了解到",
                                         "坚定持有", "抓紧购买", "抓紧申购", "抓紧认购", "抓紧买入", "请买入",
                                         "请卖出", "预购从速", "安全收益高", "安全可靠收益高", "超低风险", "超高收益",
                                         "持股待涨", "持股看涨", "铁定上涨", "等待上涨", "等待涨停", "阿扁", "陈水扁总统",
                                         "马英九总统", "八九", "六四", "学潮", "民运", "专政", "专治", "被中共", "被合谐",
                                         "贪官", "官商", "情人", "成人", "法车仑", "法轮", "维权", "法一轮", "宗教", "公安",
                                         "共狗", "躲猫猫", "红色恐怖", "胡江内斗", "胡紧套", "胡錦濤", "胡耀邦", "华国锋",
                                         "江胡内斗", "江贼民", "拉登", "李洪志", "疆独", "藏独"]
    private
    static List holidays = ['2016-10-01', '2016-10-02', '2016-10-03', '2016-10-04', '2016-10-05', '2016-10-06', '2016-10-07']
    private static String sensitiveStr = sensitiveList.join('|')
    private static
    def Pattern sensitiveMat = ~/(\#[^\#\s]*($sensitiveStr)+[^\#\s]*\#)|$sensitiveStr|(\@[^\@\s]*($sensitiveStr)+[^\@\s]* )/


    static String uuid(String suf = null) {
        String r = '' + System.currentTimeMillis() + '_' + new Random().nextInt(10000)
        if (suf != null)
            r += suf
        r
    }

    static Map getMapInKeys(Object obj, String keys) {
        Map r = [:]

        for (key in keys.split(','))
            r[key] = obj.get(key)

        r
    }

    static String transferSql(String str) {
        str.replaceAll(".*([';]+|(--)+).*", " ")
    }

    static long now() {
        System.currentTimeMillis()
    }

    /**
     * 记录需要搜集并统计的日志
     *
     * @param module 功能模块
     * @param action 事件
     * @param msg 具体内容
     */
    static void i(String module, String action, String msg) {
        StringBuilder sb = new StringBuilder()
        sb << '['
        sb << module
        sb << ']'
        sb << '['
        sb << action
        sb << ']'
        sb << msg
        String str = sb.toString()
        logCollect.info(str)
    }

    // 短域名
    @Deprecated
    static Map shortUrl(String longUrl, String alias) {
        longUrl = URLEncoder.encode(longUrl)

        final String url = 'http://dwz.cn/create.php'
        String body = "url=${longUrl}&alias=${alias}&access_type=web"

        def req = HttpRequest.post(url).connectTimeout(3 * 1000).send(body)
        String str = req.body()

// {"tinyurl":"http:\/\/dwz.cn\/test123_1234","status":0,"longurl":"http://mt.sohu.com/20160531/n4521373282.shtml","err_msg":""}
// status = -1 err_msg
        Map obj = JSON.parse(str)
        obj
    }

    /**
     * 秘钥必须是16位
     * @param content
     * @param keyData
     * @return
     */
    static byte[] aesEncrypt(String content, String keyData) {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(keyData.getBytes("UTF-8"), "AES");
        // 加密
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(content.getBytes("UTF-8"));
    }

    /**
     * 秘钥必须是16位
     * @param byteData
     * @param keyData
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] aesDecrypt(byte[] byteData, String keyData)
            throws GeneralSecurityException {
        // 处理密钥
        SecretKeySpec key = new SecretKeySpec(keyData.getBytes("UTF-8"), "AES");
        // 解密
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(byteData);
    }

    /**
     * 判断i是否对应，j的任何一个位为1的，业务逻辑中就是i角色列表有一个在j角色列表中，位有交集
     * j == 0的时候说明不做限制
     * @param i
     * @param j
     * @return
     */
    public static Boolean isBitMatch(Integer i, Integer j) {
        if (i == null || j == null) {
            //i或者j为空，则直接返回false
            return false;
        }
        if (j == 0) {
            //文章或者，帖子，或者活动的label为0时，表明所有人都有权限，所以直接返回true
            return true
        }
        return (i & j) != 0
    }

    /**
     * 是否包含敏感词
     * @param content
     * @return
     */
    public static boolean isSensitive(String content) {
        return content != null && sensitiveList.any { content.contains(it) }
    }

    /**
     * 替换敏感词
     * @param content
     * @return
     * panjintao
     */
    public static String replaceSensitive(String content) {
        if (!content) {
            return ""
        }
        return content.replaceAll(sensitiveMat, '***')
    }

    /**
     * 是否是开盘日
     * @param date
     * @return
     */
    public static boolean isStockDealDay(Date date) {
        Calendar c = Calendar.getInstance()
        c.setTime(date)
        int day = c.get(Calendar.DAY_OF_WEEK)
        if (day == 6 || day == 7) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd")
        String d = format.format(date)
        return !holidays.contains(d)
    }

    /**
     * 是否是交易时间
     * @param date
     * @return
     */
    public static boolean isStockDealTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HHmm")
        int time = format.format(date) as int;
        return (time > 929 && time < 1131) || (time > 1259 && time < 1501)
    }

    public static String transStockNum(double num, int scale) {
        if (num < 10000) {
            return num + "";
        }
        num = num/10000;
        if(num < 10000) {
            return num.round(scale) + "万"
        }

        num = num/10000;
        return num.round(scale) + "亿"
    }

    public static String transStockTime(String timeStr, String outputFormat) {
        Date d = new Date(timeStr)
        return new SimpleDateFormat(outputFormat).format(d)
    }
	
	/**
     * 加密
     * @param datasource byte[]
     * @param password String
     * @return byte[]
     */
    public static byte[] desEncrypt(byte[] datasource, String password) {            
        try{
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        //创建一个密匙工厂，然后用它把DESKeySpec转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");
        //用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
        //现在，获取数据并加密
        //正式执行加密操作
        return cipher.doFinal(datasource);
        }catch(Throwable e){
                e.printStackTrace();
        }
        return null;
}
    /**
     * 解密
     * @param src byte[]
     * @param password String
     * @return byte[]
     * @throws Exception
     */
    public static byte[] desDecrypt(byte[] src, String password) throws Exception {
		// DES算法要求有一个可信任的随机数源
		SecureRandom random = new SecureRandom();
		// 创建一个DESKeySpec对象
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		// 创建一个密匙工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// 将DESKeySpec对象转换成SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(desKey);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES");
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		// 真正开始解密操作
		return cipher.doFinal(src);
	}
}