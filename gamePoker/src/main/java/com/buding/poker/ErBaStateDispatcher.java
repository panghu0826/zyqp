package com.buding.poker;

import com.buding.api.desk.MJDesk;
import com.buding.card.ICardLogic;
import com.buding.common.CardDealer;
import com.buding.game.GameStateDispatcher;
import com.buding.game.events.DispatchEvent;
import com.buding.poker.common.PokerErBaCardDealer;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.erba.ErBaCardLogic;
import com.buding.poker.states.ErBaStateFinish;
import com.buding.poker.states.ErBaStateReady;
import com.buding.poker.states.ErBaStateRun;
import com.buding.poker.states.PokerStateDeal;

public class ErBaStateDispatcher extends GameStateDispatcher<MJDesk> {
    //准备阶段
    private ErBaStateReady mStateReady = new ErBaStateReady();
    //发牌阶段
    private PokerStateDeal mStateDeal = new PokerStateDeal();
    //游戏阶段
    private ErBaStateRun mStateRun = new ErBaStateRun();
    //结算阶段
    private ErBaStateFinish mStateFinish = new ErBaStateFinish();

    private CardDealer mCardDealer = new PokerErBaCardDealer();
    private ICardLogic mCardLogic = new ErBaCardLogic();


    @Override
    public void StateDispatch(DispatchEvent event) {
        switch (event.eventID) {
            case PokerConstants.PokerStateReady: {
                this.goTo(this.mStateReady);
            }
            break;

            case PokerConstants.PokerStateDeal: {
                this.goTo(this.mStateDeal);
            }
            break;

            case PokerConstants.PokerStateRun: {
                this.goTo(this.mStateRun);
            }
            break;

            case PokerConstants.PokerStateFinish: {
                this.goTo(mStateFinish);
            }
            break;

            default: {
                this.logger.error("no state to goto:" + event.eventID);
            }
            break;
        }
    }


    @Override
    public void SetDesk(MJDesk desk) {
        this.mTimerMgr.Init(desk);
        this.mCardLogic.init(this.mGameData, desk);
        this.mCardDealer.Init(this.mGameData, desk, this.mCardLogic);

        this.mStateReady.Init(desk, this.mTimerMgr, this, this.mGameData, this.mCardLogic, this.mCardDealer);
        this.mStateDeal.Init(desk, this.mTimerMgr, this, this.mGameData, this.mCardLogic, this.mCardDealer);
        this.mStateRun.Init(desk, this.mTimerMgr, this, this.mGameData, this.mCardLogic, this.mCardDealer);
        this.mStateFinish.Init(desk, this.mTimerMgr, this, this.mGameData, this.mCardLogic, this.mCardDealer);

        //初始状态
        this.goTo(this.mStateReady);
    }

}
