package com.buding.hall.module.user.helper;

import java.security.Key;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.util.RSAUtil;
import com.buding.hall.config.ConfigManager;

public class UserSecurityHelper implements InitializingBean {
	@Autowired
	ConfigManager configManager;
	
	private Key pwdKey;

	@Override
	public void afterPropertiesSet() throws Exception {
//		this.pwdKey = RSAUtil.initPrivateKey(configManager.getPrivateKeyFile());
	}
	
	public String decrypt(String text) throws Exception {		
		return new String(decrypt(text.getBytes("UTF8")), "UTF8");
	}
	
	public String encrypt(String text) throws Exception {		
		return new String(encrypt(text.getBytes("UTF8")), "UTF8");
	}
	
	public byte[] decrypt(byte[] data) throws Exception {
		data = Base64.decode(data);
		data = RSAUtil.handleData(pwdKey, data, 0);
		return data;
	}
	
	public byte[] encrypt(byte[] bytes) throws Exception {		
		byte[] data = RSAUtil.handleData(pwdKey, bytes, 1);
		data = Base64.encode(data);
		return data;
	}

	public Key getPwdKey() {
		return pwdKey;
	}

	public void setPwdKey(Key pwdKey) {
		this.pwdKey = pwdKey;
	}
}
