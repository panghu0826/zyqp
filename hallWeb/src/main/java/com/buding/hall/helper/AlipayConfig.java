package com.buding.hall.helper;

public class AlipayConfig {
	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String partner = "2088221765409891";

	// 商户的私钥,需要PKCS8格式，RSA公私钥生成：https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.nBDxfy&treeId=58&articleId=103242&docType=1
	public static String private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0ROBai3w2QIunc71ouvXxMYkM6r";
	// 沙箱环境
	// public static String private_key="xxxxxxxxx" ;

	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	public static String seller_id = "2088221765409891";

	// 支付宝的公钥,查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmyF9k0FpbOTEdhlbHLyqiRwZ2K+HEtA7xqpcKaXwzMfPAplJV4wcPRlxOnJi8ZloncTcN95l2U0sim3iKej0ek2Q3Ib3ckF63msgtzsvAoJ81Y4k7J+pJS7nWgiRi7ozB2S1tfNixUdj1cMm4UtrfaapjnH3IV662v7gEQnCo6EVKpk4CpMQyK2x0H1wIX6SgpHS08hF+gm61VDsQGFvPxlnQqS7mDTDDB5bStxWRKu7WPyGf9sX7LM9SBQYa1e3i2NjKCKo/ECewsoS7JosKD/l23iETA0uIb3K9Ebzi0+P3cQHhxbthxWaZk3TxVZbQSj+d7G3u+S6JrsZobU++wIDAQAB";
//	public static String alipay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";

	// 签名方式 不需修改
	public static String sign_type = "RSA2";

	// 支付类型 ，无需修改
	public static String payment_type = "1";

	// 调用的接口名，无需修改
	public static String service = "create_direct_pay_by_user";

	public static String notify_url = "http://xxxxxxxx/xxxxxxx/notify/payNotify";
}
