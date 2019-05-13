package com.buding.task.network.cmd;

import com.buding.common.token.TokenClient;
import com.buding.db.model.User;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.ProductConfig;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.task.helper.TaskPushHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall.MallProductModel;
import packet.game.Hall.MallProductResponse;
import packet.msgbase.MsgBase.PacketType;

@Component
public class MallProductCmd extends TaskBaseCmd {
	protected Logger logger = LogManager.getLogger(getClass());

	@Autowired
	protected TokenClient tokenClient;
	
	@Autowired
	protected TaskPushHelper pushHelper;
	
	@Autowired
	ConfigManager configManager;


	@Autowired
	UserDao userDao;

	@Override
	public void execute(CmdData data) throws Exception {
		int userId = data.session.userId;
		if(userId == 0) {
			pushHelper.pushErrorMsg(data.session, PacketType.GenOrderResponse, "用户未登录");
			return;
		}

		User user = userDao.getUser(userId);
		MallProductResponse.Builder mb = MallProductResponse.newBuilder();
		if(user==null || user.getHasInvitecode()==null || user.getHasInvitecode()==0) {
			mb.setHasInviteCode(0);
		}else{
			mb.setHasInviteCode(1);
		}

		for(ProductConfig prd : configManager.shopItemConfMap.values()) {			
			if(prd.status == 1
					&& !StringUtils.equals(prd.id,"31014")
					&& !StringUtils.equals(prd.id,"31015")) {
				MallProductModel.Builder m = MallProductModel.newBuilder();
				m.setCategory(prd.category);
				m.setId(prd.id);
				m.setImage(prd.img);
				m.setItemCount(prd.cItemCount);
				m.setName(prd.name);
				m.setPrice(prd.price.currenceCount);
				mb.addProducts(m);
			}
		}
		
		pushHelper.pushPBMsg(data.session, PacketType.MallProductResponse, mb.build().toByteString());
	}

	@Override
	public PacketType getKey() {
		return PacketType.MallProductRequest;
	}	
}
