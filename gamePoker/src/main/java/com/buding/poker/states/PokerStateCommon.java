package com.buding.poker.states;

import com.buding.api.context.PokerErBaFinalResult;
import com.buding.api.desk.MJDesk;
import com.buding.api.player.PlayerInfo;
import com.buding.game.GameData;
import com.buding.game.GameState;
import com.buding.game.events.*;
import com.buding.poker.constants.PokerConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class PokerStateCommon extends GameState<MJDesk<byte[]>> {

    public static Map<Integer, String> eventMap = new HashMap<>();

    static {
        eventMap.put(GameLogicEvent.Player_Sit, "坐下");
        eventMap.put(GameLogicEvent.Player_Agree, "准备");
        eventMap.put(GameLogicEvent.Player_Exit, "离开房间");
        eventMap.put(GameLogicEvent.Player_Offline, "离线");
        eventMap.put(GameLogicEvent.Player_Reconnect, "重连");
        eventMap.put(GameLogicEvent.Player_Away, "标记离开");
        eventMap.put(GameLogicEvent.Player_ComeBack, "重新回桌子");
        eventMap.put(GameLogicEvent.Player_HangUp, "托管");
        eventMap.put(GameLogicEvent.Player_Cancel_Hangup, "取消托管");
        eventMap.put(GameLogicEvent.Player_Exit_Pos_Not_Room, "离座");
        eventMap.put(GameLogicEvent.Push_Desk_Info, "玩家进入推送桌子消息");
        eventMap.put(GameLogicEvent.Player_Zan_Li, "暂离");
        eventMap.put(GameLogicEvent.Player_Enter, "进入房间");
    }

    @Override
    public void onPlayer(PlayerEvent event) {
        this.logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"玩家:" +event.info.name+"(id:" +event.info.playerId +",pos:" +
                "" + event.info.position + ")--"+ eventMap.get(event.eventID) );

        switch (event.eventID) {

            case GameLogicEvent.Player_Sit: {
                playerSit(event.info);
            }
            break;
            case GameLogicEvent.Player_Enter: {
                this.mGameData.mPlayersMap.put(event.info.playerId, event.info);
                if (mDesk.getErBaGameType() > 0) {
                    PokerErBaFinalResult finalResult = this.mGameData.erBaFinalResult.finalResults.get(event.info.playerId);
                    event.info.score =  finalResult == null ? 0 : finalResult.allScore;
                }
            }
            break;
            case GameLogicEvent.Player_Agree: {
                int position = event.info.position;
                if (position < PokerConstants.MYGAME_MAX_PLAYERS_COUNT && position >= 0) {
                    this.mGameData.mPlayers[position] = event.info;
                    if(this.mGameData.jackResult.Result.get(event.info.playerId)!=null)
                        this.mGameData.jackResult.Result.get(event.info.playerId).isZanLi = false;
                }
                this.mGameData.mPlayersMap.put(event.info.playerId, event.info);
            }
            break;
            case GameLogicEvent.Player_Exit_Pos_Not_Room: {
                int position = event.info.position;
                if (position >= 0) {
                    this.mGameData.mPlayers[position] = null;
                } else {
                    int j = -100;
                    for (int i = 0; i < mGameData.mPlayers.length; i++) {
                        PlayerInfo p = mGameData.mPlayers[i];
                        if(p == null || p.playerId != event.info.playerId) continue;
                        j = i;
                    }
                    if(j != -100) this.mGameData.mPlayers[j] = null;
                }
                handleExitPos(event.info);
            }
            break;
            case GameLogicEvent.Push_Desk_Info: {
                PlayerInfo pl = event.info;

                this.mCardLogic.pushDeskInfo(this.mGameData, this.mDesk, pl);
            }
            break;

            case GameLogicEvent.Player_HangUp: {
                int position = event.info.position;
                this.mGameData.mPlayerAction[position].autoOperation = 1;
                handlePlayerHangup(position);
                this.mDesk.onPlayerHangup(position);
            }
            break;

            case GameLogicEvent.Player_Cancel_Hangup: {
                int position = event.info.position;
                this.mGameData.mPlayerAction[position].autoOperation = 0;
                this.mDesk.onPlayerCancelHangup(position);
            }
            break;

            case GameLogicEvent.Player_Reconnect: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"handle reconnect for {}, name: {}", event.info.name, this.getClass().getName());
                this.handleReconnectFor(event.info);

            }
            break;
            case GameLogicEvent.Player_Exit: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"player {}  exit", event.info.position);
                if (event.info.position >= 0) this.mGameData.mPlayers[event.info.position] = null;
                mGameData.mPlayersMap.remove(event.info.playerId);
                handlePlayerExit(event.info);
            }
            break;
            case GameLogicEvent.Player_Away: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"ignore player {}  away", event.info.name);
                if (!mDesk.getErBaXiaZhuOrBankerPlayerIds().contains(event.info.playerId)) mGameData.mPlayersMap.remove(event.info.playerId);
            }
            break;
            case GameLogicEvent.Player_ComeBack: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"player {} comback", event.info.name);
                this.handleReconnectFor(event.info);
            }
            break;
            case GameLogicEvent.Player_Offline: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"player {} offline", event.info.name);
                this.handleOffline(event.info);
            }
            break;
            case GameLogicEvent.Player_Zan_Li: {
                logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"player {} zanli", event.info.name);
                int position = event.info.position;
                if (position >= 0) {
                    this.mGameData.mPlayers[position].isZanLi = true;
//                    this.mGameData.mPlayers[position].isWait = true;
                    if(this.mGameData.jackResult.Result.get(event.info.playerId)!=null)
                        this.mGameData.jackResult.Result.get(event.info.playerId).isZanLi = true;
                } else {
                    int j = -100;
                    for (int i = 0; i < mGameData.mPlayers.length; i++) {
                        PlayerInfo p = mGameData.mPlayers[i];
                        if(p == null || p.playerId != event.info.playerId) continue;
                        j = i;
                    }
                    if(j != -100) {
                        this.mGameData.mPlayers[j].isZanLi = true;
//                        this.mGameData.mPlayers[j].isWait = true;
                        if(this.mGameData.jackResult.Result.get(event.info.playerId)!=null)
                            this.mGameData.jackResult.Result.get(this.mGameData.mPlayers[j].playerId).isZanLi = true;
                    }
                }
            }
            break;
            default:
                break;
        }

    }

    public  void playerSit(PlayerInfo info) {

    }

    protected void handlePlayerExit(PlayerInfo info) {

    }

    public void handleOffline(PlayerInfo info) {
        if (mDesk.getErBaGameType() > 0 && !mGameData.chouMaMap.containsKey(info.playerId) && !mGameData.trandition28UserChouMaMap.containsKey(info.playerId)) {
            // 没有下注且不是庄家就离座观战
            if (info.playerId != mGameData.robIndex) mDesk.playerExitPosNotExitRoom(info.playerId, info.position);
        }
    }

    @Override
    public void onPlatform(PlatformEvent event) {
        switch (event.eventID) {

            case GameLogicEvent.Game_Dismiss: {

                this.mGameData.dismissing = true;
                // //1秒后状态跳转
                this.mGameTimer.KillDeskTimer();

                DispatchEvent e = new DispatchEvent();
                e.eventID = PokerConstants.PokerStateFinish;
                this.mDispatcher.StateDispatch(e);
                break;
            }
            case GameLogicEvent.Game_Pause: {
                this.mGameData.pause = true;
                break;
            }
            case GameLogicEvent.Game_Resume: {
                this.mGameData.pause = false;
                break;
            }
            default: {

            }
            break;
        }
    }

    @Override
    public void setGamingDate(String json) {
        mCardLogic.handleSetGamingData(mCardDealer, this.mGameData, this.mDesk, json);
    }

    @Override
    public void onNet(NetEvent event) {

    }

    /*
     * 处理玩家状态变更
     */
    public abstract void handlePlayerStatusChange(int position);

    public void handleExitPos(PlayerInfo info) {

    }

    /*
     * 处理玩家重连
     */
    public void handleReconnectFor(PlayerInfo info){
        if(info.position >= 0) this.mDesk.sendDissmissVoteMsg();
        this.mGameData.mPlayersMap.put(info.playerId,info );
    };

    public abstract void handlePlayerHangup(int position);

    public void dumpGameData() {
        try {
            String data = new GsonBuilder().setPrettyPrinting().create().toJson(this.mGameData);
            File file = new File("/home/game/game.json");
            if (file.getParentFile() != null && file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fout = new FileOutputStream(file.getAbsolutePath());
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            writer.println(data);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error("桌子ID--"+this.mDesk.getDeskID()+"--"+"act=dumpGameDataError;deskId=" + mDesk.getDeskID(), e);
        }
    }

    public void relaodGameData() {
        try {
            FileInputStream fin = new FileInputStream("/home/game/game.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                int size = 1024;
                byte buff[] = new byte[size];
                while ((size = fin.read(buff)) != -1) {
                    out.write(buff, 0, size);
                }
            } finally {
                fin.close();
            }

            String json = new String(out.toByteArray(), "UTF8");
            GameData data = new Gson().fromJson(json, GameData.class);
            this.mGameData = data;

            logger.info("桌子ID--"+this.mDesk.getDeskID()+"--"+"reload GameData OK, json:{}", json);
        } catch (Exception e) {
            logger.error("桌子ID--"+this.mDesk.getDeskID()+"--"+"act=dumpGameDataError;deskId=" + mDesk.getDeskID() + ";", e);
        }
    }
}
