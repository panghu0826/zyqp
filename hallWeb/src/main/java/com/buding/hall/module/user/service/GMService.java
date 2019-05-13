package com.buding.hall.module.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.buding.common.admin.component.BaseComponent;
import com.buding.common.util.IOUtil;
import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.item.service.ItemService;
import com.google.gson.Gson;

@Component
public class GMService extends BaseComponent {
	@Autowired
	UserService userService;
	
	@Autowired
	ItemService itemService;
	
	@Autowired
	ConfigManager configManager;
	
	public void setItem() throws Exception {
		String path = configManager.gmPath;
		if(path == null) {
			throw new RuntimeException("GMPath not set");
		}
		String json = IOUtil.getFileResourceAsString(path+"/SetCoin.json", "utf-8");
		User copy = new Gson().fromJson(json, User.class);
		User user = userService.getUser(copy.getId());
		if(copy.getCoin() != null) {
			user.setCoin(copy.getCoin());
		}
		
		userService.updateUser(user);
	}

	@Override
	public String getComponentName() {
		return "gm";
	}
	
}
