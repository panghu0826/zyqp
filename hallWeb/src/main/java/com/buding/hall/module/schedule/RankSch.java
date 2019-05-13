package com.buding.hall.module.schedule;

import com.buding.common.cache.RedisClient;
import com.buding.db.model.UserRankDetail;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.RankConfig;
import com.buding.hall.module.msg.dao.MsgDao;
import com.buding.hall.module.rank.dao.UserRankDao;
import com.buding.hall.module.ws.MsgPortalService;
import com.buding.rank.model.RankModel;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class RankSch {
    @Autowired
    MsgDao msgDao;

    @Autowired
    MsgPortalService msgPortalService;

    @Autowired
    RedisClient redisClient;

    @Autowired
    ConfigManager configManager;

    @Autowired
    protected UserRankDao userRankDao;

    public void doRankInSunDay() {
        for(RankConfig rankConfig:configManager.rankConfMap.values()) {
            if(rankConfig.pointType==1){//财富榜
                String key = rankConfig.pointType +"ALL"+ "_rank";
                Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
                for (String item : items) {
                    RankModel model = new Gson().fromJson(item, RankModel.class);
                    userRankDao.insert(model2Detail(model,rankConfig.pointType,"ALL"));
                }
            }else{//周开
                for(String gameid:configManager.gameMap.keySet()){
                    if(rankConfig.pointType == 3) continue;
                    String key = rankConfig.pointType + gameid + "_rank";
                    Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
                    for (String item : items) {
                        RankModel model = new Gson().fromJson(item, RankModel.class);
                        userRankDao.insert(model2Detail(model,rankConfig.pointType,gameid));
                    }
                }
            }

        }
    }

    private long getRankGroupTimeInWeek(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int week = c.get(Calendar.WEEK_OF_YEAR);
        int dayWeek = c.get(Calendar.DAY_OF_WEEK);
        if(dayWeek==1) week--;
        return year * 1000 + week;
    }

    private long getRankGroupTimeInMonth(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        return year * 1000 + month;
    }

    private UserRankDetail model2Detail(RankModel model,int rankPoint,String gameId){
        UserRankDetail detail = new UserRankDetail();
        detail.setUserId(model.playerId);
        detail.setGameId(gameId);
        detail.setPoint(model.rankPoint);
        detail.setPointType(rankPoint);
        detail.setRankNum(model.rank);
        Long groupTime = 1l;
        if(rankPoint == 2){
            groupTime = getRankGroupTimeInWeek();
        }else if(rankPoint == 3){
            groupTime = getRankGroupTimeInMonth();
        }
        detail.setGroupDatetime(groupTime);
        detail.setCtime(new Date());
        detail.setMtime(new Date());
        return detail;
    }

    public void doRankInMonth() {
        for(RankConfig rankConfig:configManager.rankConfMap.values()) {
            if(rankConfig.pointType==1){//财富榜
                String key = rankConfig.pointType +"ALL"+ "_rank";
                Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
                for (String item : items) {
                    RankModel model = new Gson().fromJson(item, RankModel.class);
                    userRankDao.insert(model2Detail(model,rankConfig.pointType,"ALL"));
                }
            }else{//月开
                for(String gameid:configManager.gameMap.keySet()){
                    if(rankConfig.pointType == 2) continue;
                    String key = rankConfig.pointType + gameid + "_rank";
                    Set<String> items = redisClient.zrange(key, 0, rankConfig.rankLimit);
                    for (String item : items) {
                        RankModel model = new Gson().fromJson(item, RankModel.class);
                        userRankDao.insert(model2Detail(model,rankConfig.pointType,gameid));
                    }
                }
            }

        }
    }
}
