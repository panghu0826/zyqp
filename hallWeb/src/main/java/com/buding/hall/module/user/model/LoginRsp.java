package com.buding.hall.module.user.model;

import java.util.HashMap;
import java.util.Map;

import com.buding.common.network.model.BaseRsp;

public class LoginRsp extends BaseRsp {
	public String token;
	public int userId;
	public Map<String, String> server = new HashMap<String, String>();
}
