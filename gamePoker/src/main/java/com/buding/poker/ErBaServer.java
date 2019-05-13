package com.buding.poker;

import com.buding.game.GameLogiController;

/**
 *
 * 炸金花
 *
 * @author chen
 *
 */
public class ErBaServer extends GameLogiController {
    public ErBaServer() {
        this.m_dispatcher = new ErBaStateDispatcher();
    }
}
