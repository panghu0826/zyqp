package com.buding.hall.module.status.model;

import java.util.ArrayList;
import java.util.List;

import com.buding.common.network.model.BaseRsp;

public class StatusRsp extends BaseRsp {
	public List<GameStatus> data = new ArrayList<GameStatus>();
}
