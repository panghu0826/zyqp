package com.buding.poker;

import com.buding.game.GameLogiController;

/**
 * @author jaime qq_1094086610
 * @Description: 斗地主
 */
public class DDZServer extends GameLogiController {
    public DDZServer() {
        this.m_dispatcher = new DDZStateDispatcher();
    }
}
