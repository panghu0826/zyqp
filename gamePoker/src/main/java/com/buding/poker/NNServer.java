package com.buding.poker;

import com.buding.game.GameLogiController;

/**
 * @author jaime qq_1094086610
 * @Description: 斗地主
 */
public class NNServer extends GameLogiController {
    public NNServer() {
        this.m_dispatcher = new NNStateDispatcher();
    }
}
