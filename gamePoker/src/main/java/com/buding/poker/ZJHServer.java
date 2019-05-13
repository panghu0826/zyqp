package com.buding.poker;

import com.buding.game.GameLogiController;

/**
 *
 * 炸金花
 *
 * @author chen
 *
 */
public class ZJHServer extends GameLogiController {
    public ZJHServer() {
        this.m_dispatcher = new ZJHStateDispatcher();
    }
}
