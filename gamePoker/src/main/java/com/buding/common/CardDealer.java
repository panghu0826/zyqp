package com.buding.common;

import com.buding.api.desk.MJDesk;
import com.buding.game.GameCardDealer;

/**
 * @author Jaime
 *
 */
public class CardDealer extends GameCardDealer<MJDesk<byte[]>> {

	@Override
	public void dealCard() {
		//洗牌
		this.washCards();
	}

	// 洗牌
	public void washCards() {

	}

	@Override
	public void dealPublicCard() {
		
	}
}
