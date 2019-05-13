package com.buding.battle.logic.network.module;

import io.netty.channel.ChannelHandlerContext;

import com.buding.battle.common.network.session.BattleSession;

public interface Module<Key,Packet> {
	public void handleMsg(ChannelHandlerContext ctx, BattleSession session, Key key, Packet packet) throws Exception;
}
