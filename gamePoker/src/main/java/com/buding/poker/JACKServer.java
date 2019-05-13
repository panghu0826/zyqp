package com.buding.poker;

import com.buding.game.GameLogiController;

/**
 *
 * 炸金花
 *
 * @author chen
 *
 */
public class JACKServer extends GameLogiController {
    public JACKServer() {
        this.m_dispatcher = new JACKStateDispatcher();
    }
}
