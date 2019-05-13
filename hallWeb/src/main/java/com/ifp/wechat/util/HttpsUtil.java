package com.ifp.wechat.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 无视Https证书是否正确的Java Http Client
 * 
 * 
 * @author huangxuebin
 * 
 * @create 2012.8.17
 * @version 1.0
 */
public class HttpsUtil {

	/**
	 * 忽视证书HostName
	 */
	private static HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
		public boolean verify(String s, SSLSession sslsession) {
			System.out.println("WARNING: Hostname is not matched for cert.");
			return true;
		}
	};

	/**
	 * Ignore Certification
	 */
	private static TrustManager ignoreCertificationTrustManger = new X509TrustManager() {

		private X509Certificate[] certificates;

		@Override
		public void checkClientTrusted(X509Certificate certificates[], String authType) throws CertificateException {
			if (this.certificates == null) {
				this.certificates = certificates;
				System.out.println("init at checkClientTrusted");
			}
		}

		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
			if (this.certificates == null) {
				this.certificates = ax509certificate;
				System.out.println("init at checkServerTrusted");
			}
			// for (int c = 0; c < certificates.length; c++) {
			// X509Certificate cert = certificates[c];
			// System.out.println(" Server certificate " + (c + 1) + ":");
			// System.out.println("  Subject DN: " + cert.getSubjectDN());
			// System.out.println("  Signature Algorithm: "
			// + cert.getSigAlgName());
			// System.out.println("  Valid from: " + cert.getNotBefore());
			// System.out.println("  Valid until: " + cert.getNotAfter());
			// System.out.println("  Issuer: " + cert.getIssuerDN());
			// }

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static String getMethod(String urlString, String requestMethod, String outputStr) {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
		try {
			URL url = new URL(urlString);
			/*
			 * use ignore host name verifier
			 */
			HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			// Prepare SSL Context
			TrustManager[] tm = { ignoreCertificationTrustManger };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());

			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			connection.setSSLSocketFactory(ssf);

			// 设置请求方式（GET/POST）
			connection.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod)) {
				connection.connect();
			}

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = connection.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			InputStream reader = connection.getInputStream();
			byte[] bytes = new byte[512];
			int length = reader.read(bytes);

			do {
				buffer.write(bytes, 0, length);
				length = reader.read(bytes);
			} while (length > 0);

			// result.setResponseData(bytes);
			System.out.println(buffer.toString());
			reader.close();

			connection.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		String repString = new String(buffer.toByteArray());
		return repString;
	}

	public static void main(String[] args) {
		String urlString = "https://218.202.0.241:8081/XMLReceiver";
		String output = new String(HttpsUtil.getMethod("https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxcac9625c1385e1b3&secret=90dd20711e1f118b8588f9715cd032b7&code=031d7763af7b77108dc19111f2111f2A&grant_type=authorization_code", "GET", null));
		System.out.println(output);
	}
}
