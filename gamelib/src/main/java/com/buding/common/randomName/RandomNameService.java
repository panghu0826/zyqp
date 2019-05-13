package com.buding.common.randomName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;


public class RandomNameService implements InitializingBean {
	@Autowired
	RandomNameConfigManager randomNameConfig;
	
	@Override
	public void afterPropertiesSet() throws Exception {
//		System.out.println("++++++++++++++");
	}

	public String randomName() {
		int gender = (int)(System.currentTimeMillis()%2);
		return randomName(gender);
	}
	
	public String randomName(int sex) {
		int len = ((int)(System.currentTimeMillis()%2))+1;
		String name = randomNameConfig.getFirstName();
		
		if(sex != 0) { //男
			if(len > 1) {
				name += randomNameConfig.getManMidName();
			}
			name += randomNameConfig.getManLastName();
		} else {
			if(len > 1) {
				name += randomNameConfig.getWomenMidName();
			}
			name += randomNameConfig.getWomenLastName();
		}
		return name;
	}
}
