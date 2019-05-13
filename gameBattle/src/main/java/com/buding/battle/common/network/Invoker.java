package com.buding.battle.common.network;

import com.buding.battle.common.network.session.BattleSession;


public abstract interface Invoker<Req>
{
  public abstract void invoke(BattleSession session, Req msg) throws Exception;
}