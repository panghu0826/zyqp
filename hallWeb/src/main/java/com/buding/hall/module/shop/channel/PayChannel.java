package com.buding.hall.module.shop.channel;

import packet.game.Hall.ConfirmOrderRequest;
import packet.game.Hall.GenOrderRequest;
import packet.game.Hall.GenOrderResponse;

import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.hall.config.ProductConfig;

public interface PayChannel {
	public TResult<GenOrderResponse> createOrder(int userId, GenOrderRequest req, ProductConfig prd) throws Exception;
	public Result confirmOrder(int userId, ConfirmOrderRequest req) throws Exception;
	public int getName();
}
