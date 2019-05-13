package com.buding.hall.module.user.helper;

import com.buding.api.player.PlayerInfo;
import com.buding.db.model.User;

public class UserHelper {
	public static void copyUser2Player(User user, PlayerInfo p) {
		p.playerId = user.getId();
		p.coin = user.getCoin();
		p.name = user.getNickname();
		p.headImg = user.getHeadImg();
		p.gender = user.getGender();
		p.fanka = user.getFanka();
		p.diamond = user.getDiamond()==null?0:user.getDiamond();
		p.bindedMobile = 0;
		p.userType = user.getUserType();
	}
}
