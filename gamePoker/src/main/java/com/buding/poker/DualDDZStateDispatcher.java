package com.buding.poker;

import com.buding.api.desk.MJDesk;
import com.buding.card.ICardLogic;
import com.buding.common.CardDealer;
import com.buding.game.GameStateDispatcher;
import com.buding.game.events.DispatchEvent;
import com.buding.poker.common.PokerDDZCardDealer;
import com.buding.poker.constants.PokerConstants;
import com.buding.poker.dualddz.DualDDZCardLogic;
import com.buding.poker.states.DDZStateFinish;
import com.buding.poker.states.DDZStateReady;
import com.buding.poker.states.DDZStateRun;
import com.buding.poker.states.PokerStateDeal;

@SuppressWarnings("all")
public class DualDDZStateDispatcher extends GameStateDispatcher<MJDesk> {
    //准备阶段
    private DDZStateReady mStateReady = new DDZStateReady();
    //发牌阶段
    private PokerStateDeal mStateDeal = new PokerStateDeal();
    //游戏阶段
    private DDZStateRun mStateRun = new DDZStateRun();
    //结算阶段
    private DDZStateFinish mStateFinish = new DDZStateFinish();

    private CardDealer mCardDealer = new PokerDDZCardDealer();
    private ICardLogic mCardLogic = new DualDDZCardLogic();


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
