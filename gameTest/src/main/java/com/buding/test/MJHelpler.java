package com.buding.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.game.MsgGame.PlayerSitSyn;
import packet.mj.MJ.GameOperChiArg;
import packet.mj.MJ.GameOperHandCardSyn;
import packet.mj.MJ.GameOperPlayerActionSyn;

import com.buding.mj.helper.MJHelper;
import com.google.gson.Gson;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public class MJHelpler {
	Logger logger = LoggerFactory.getLogger(getClass());
	boolean isSaveCard = false;

	public GamingData data;
	public MJHelpler() {
		
	}

	public void porcessActionSyn(GameOperPlayerActionSyn syn) {
		data.posSynMap.put(syn.getPosition(), syn);
		if (syn.getPosition() == data.position) {
			// data.downCards = syn.getDownCardsList();
			// data.handCards = syn.getHandCardsList();
			// data.cardsBefore = syn.getBeforeCardsList();
		}
	}

	public void processHandcardSyn(GameOperHandCardSyn syn) {
		// data.posSynMap.put(syn.getPosition(), syn);
		if (syn.getPosition() == data.position) {
			data.handCards.clear();
			data.handCards.addAll(syn.getHandCardsList());
			
			if(isSaveCard) {
				data.downCards.clear();			
				data.downCards.addAll(syn.getDownCardsList());
				data.cardsBefore.clear();			
				data.cardsBefore.addAll(syn.getCardsBeforeList());
				
				List<Integer> list = new ArrayList<Integer>();
				list.addAll(data.downCards);
				data.downCardHistory.add(0, list);
				
				list = new ArrayList<Integer>();
				list.addAll(data.handCards);
				data.handCardHistory.add(0, list);
			}
		}
	}

	public PlayerSitSyn getById(int playerId) {
		return data.idMap.get(playerId);
	}
	
	public String getMyLable() {
		return getByPos(data.position).getNickName()+"["+data.position+"]";
	}

	public PlayerSitSyn getByPos(int pos) {
		return data.posMap.get(pos);
	}

	public String getHandCardStr(int level) {
		if(level > data.handCardHistory.size() - 1) {
			return "";
		}
		return MJHelper.getCompositeCardListName(data.handCardHistory.get(level));
	}

	public String getDownCardStr(int level) {
		if(level > data.downCardHistory.size() - 1) {
			return "";
		}
		return MJHelper.getCompositeCardListName(data.downCardHistory.get(level));
	}

	public void onPlayerSit(PlayerSitSyn syn) {
		data.idMap.put(syn.getPlayerId(), syn);
		data.posMap.put(syn.getPosition(), syn);
	}
}
