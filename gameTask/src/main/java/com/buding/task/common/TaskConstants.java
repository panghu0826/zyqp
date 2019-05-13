package com.buding.task.common;

import com.buding.hall.module.game.model.CLubWanfaModel;
import com.google.gson.Gson;

public class TaskConstants {
    public static final int SYN_TYPE_ALL = 0;
    public static final int SYN_TYPE_DELETE = 1;
    public static final int SYN_TYPE_ADD = 2;
    public static final int SYN_TYPE_MODIFY = 3;

    public static final int APPLY_TYPE_CLUB = 0;
    public static final int APPLY_TYPE_FRIEND = 1;

    public static final int CLUB_MEMEBER_TYPE_COMMON = 0;
    public static final int CLUB_MEMEBER_TYPE_OWNER = 1;
    public static final int CLUB_MEMEBER_TYPE_MANAGER = 2;

    public static final int CHAT_TYPE_HALL = 2;
    public static final int CHAT_TYPE_PRIVATE = 1;
    public static final int CHAT_TYPE_CLUB = 0;

    public static final int AUTH_TYPE_TRANSFER_OWNER = 0;
    public static final int AUTH_TYPE_ADD_MANAGER = 1;
    public static final int AUTH_TYPE_DELETE_MANAGER = 2;

    public static final int SCORE_LOG_INIT = 1;
    public static final int SCORE_LOG_MANAGER_MODIFY = 2;
    public static final int SCORE_LOG_GAME = 3;
    public static final int SCORE_LOG_BIAO_QING = 4;
    public static final int SCORE_LOG_TRANSFER_OWNER = 5;

    public static final int CLUB_CREATE_CREATE_ROOM_MODE_MANAGE = 1;//管理员开房
    public static final int CLUB_CREATE_CREATE_ROOM_MODE_ALL = 2;//所有人开房

    public static final int CLUB_CAN_FU_FEN = 2;//可负分
    public static final int CLUB_CAN_NOT_FU_FEN = 1;//不可负分


    public static final String CLUB_CREATE_DEFAULT_WANFA_DDZ =
            new Gson().toJson(new CLubWanfaModel("G_DDZ","G_DDZ_MATCH_3VIP",-1,16,-1,3,14,48,-1,"",1));
    public static final String CLUB_CREATE_DEFAULT_NOTICE = "新俱乐部,快来发布公告吧";
    public static final int CLUB_DEFAULT_CREATE_ROOM_MODE = CLUB_CREATE_CREATE_ROOM_MODE_ALL;
    public static final int CLUB_DEFAULT_ENTER_SCORE = 100;
    public static final int CLUB_DEFAULT_CHOU_SHUI_SCORE = 100;
    public static final int CLUB_DEFAULT_CAN_FU_FEN = CLUB_CAN_NOT_FU_FEN;//不可以
    public static final int CLUB_DEFAULT_CHOU_SHUI_NUM = 100;//所有赢家都抽水
    public static final int CLUB_DEFAULT_ZENG_SONG_NUM = 1;//抽水比例
}
