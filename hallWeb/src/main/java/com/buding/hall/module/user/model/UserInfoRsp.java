package com.buding.hall.module.user.model;

import com.buding.api.player.PlayerInfo;
import com.buding.common.network.model.BaseRsp;

public class UserInfoRsp extends BaseRsp {
	public PlayerInfo player = new PlayerInfo();
}
