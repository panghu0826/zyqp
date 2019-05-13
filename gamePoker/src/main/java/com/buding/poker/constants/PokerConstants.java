package com.buding.poker.constants;

public class PokerConstants {

    //最大人数
    public static final int MYGAME_MAX_PLAYERS_COUNT = 10;
    public static final int DDZPlayerCount = 3;
    public static final int ZJHPlayerCount = 8;
    public static final int JACKPlayerCount = 8;
    public static final int NNPlayerCount = 10;

    //扎金花轮数封顶(必需比牌或者弃牌)
    public static final int ZJH_MAX_LUN = 10;

    //扎金花压注选择
    public static final int ZJH_YI_FEN_CAHNG = 1;
    public static final int ZJH_ER_FEN_CAHNG = 2;
    public static final int ZJH_WU_FEN_CAHNG = 5;
    public static final int ZJH_ZI_YOU_CAHNG = -1;

    //jack压注选择
    public static final int JACK_WU_FEN_CAHNG = 5;
    public static final int JACK_SHI_FEN_CAHNG = 10;
    public static final int JACK_ER_SHI_FEN_CAHNG = 20;

    //28压注选择
    public static final int ErBa_WU_FEN_CAHNG = 5;
    public static final int ErBa_YI_BAI_LIU_SHI_FEN_CAHNG = 160;
    public static final int ErBa_ER_SHI_FEN_CAHNG = 20;
    public static final int ERBA_ZI_YOU_CAHNG = -1;

    //传统28四门
    public static final int ErBa_SIMEN_ZHUANG_JIA = 0;
    public static final int ErBa_SIMEN_TIAN_MEN = 1;
    public static final int ErBa_SIMEN_GUO_MEN = 2;
    public static final int ErBa_SIMEN_KANG_MEN = 3;

    //牛牛压注选择
    public static final int NN_YI_FEN_CAHNG = 1;
    public static final int NN_WU_FEN_CAHNG = 5;
    public static final int NN_SHI_FEN_CAHNG = 10;
    public static final int NN_ER_SHI_FEN_CAHNG = 20;
    public static final int NN_ER_WU_ER_SHI_CAHNG = 520;

    //牛牛抢庄倍数
    public static final int NN_MEI_QIANG_ZHUANG = -2;//还没抢
    public static final int NN_BU_QIANG_ZHUANG = -1;
    public static final int NN_QIANG_ZHUANG = 0;

    //28游戏类型,1:经典,2:疯狂,3:传统
    public static final int ERBA_GAME_TYPE_JING_DIAN = 1;
    public static final int ERBA_GAME_TYPE_FENG_KUANG = 2;
    public static final int ERBA_GAME_TYPE_CHUAN_TONG = 3;


    //胜,负,和
    public static int GAME_RESULT_WIN = 1;
    public static int GAME_RESULT_LOSE = 2;
    public static int GAME_RESULT_EVEN = 3;

    //状态值
    public static final int PokerStateReady = 1; //准备
    public static final int PokerStateDeal = 2; //洗牌、发牌、处理底分、台费、宝牌
    public static final int PokerStateRun = 3; //开始牌局
    public static final int PokerStateFinish = 4; //结算

    //桌子状态
    public static final int GAME_TABLE_STATE_PLAYING = 1;//玩家玩牌中
    public static final int GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN = 2; //游戏结束
    public static final int GAME_TABLE_STATE_RE_SEND_CARD = 3; //游戏结束

    /**桌子状态非法*/
    public static final int TABLE_STATE_INVALID= 0;

    //斗地主玩家子原因状态
    public static final int POKER_TABLE_SUB_STATE_INIT_CARDS = 0;//扎金花和斗地主发牌,jack的下注
    public static final int POKER_TABLE_SUB_STATE_ROBOT_BANKER = 1;//抢地主,28杠抢庄
    public static final int POKER_TABLE_SUB_STATE_DOUBLE = 2;//选择是否加倍
    public static final int POKER_TABLE_SUB_STATE_CHU_CARD = 3;//出牌
    public static final int POKER_TABLE_SUB_STATE_NONE = 4;//无状态(此时检测超时操作)
    public static final int POKER_TABLE_SUB_STATE_SEND_CARD = 5;//jack发牌
    public static final int POKER_TABLE_SUB_STATE_XIA_ZHU = 6;//牛牛下注,28下注

    //发送类型
    public static final int SEND_TYPE_SINGLE = 1;//发给单人
    public static final int SEND_TYPE_ALL = 2;//发给所有人
    public static final int SEND_TYPE_EXCEPT_ONE = 3;//发给除某人外其他人

    //玩家的斗地主操作
    public static final int POKER_OPERTAION_CHU = 0x1;//出牌
    public static final int POKER_OPERTAION_CANCEL = 0x2;//不出
    public static final int POKER_OPERTAION_ROBOT_BANKER = 0x4;//抢地主
    public static final int POKER_OPERTAION_NOT_ROBOT_BANKER = 0x8;//不抢地主
    public static final int POKER_OPERTAION_DOUBLE = 0x10;//加倍
    public static final int POKER_OPERTAION_NOT_DOUBLE = 0x20;//不加倍
    public static final int POKER_OPERTAION_PROMPT = 0x40;//提示
    public static final int POKER_OPERTAION_PLAY_ROBOT = 0X80;//播放地主动画
    public static final int POKER_OPERTAION_GAME_OVER = 0X100;//GameOver
    public static final int POKER_OPERTAION_CLEAR_CARDS_IN_DESK = 0X200;//清除桌面牌(服务器单向推送)
    public static final int POKER_OPERTAION_JIAO_DIZHU = 0X400;//叫地主
    public static final int POKER_OPERTAION_BU_JIAO_DIZHU = 0X800;//不叫地主

    // 红中
    public static final byte MJ_CODE_HONG_ZHONG = 0X41;

    //扑克编码(方块:0x1X,梅花:0x2X,红桃:0x3X,黑桃:0x4X)
    public static final byte POKER_CODE_XIAO_WANG = 0x51;//小王
    public static final byte XIAO_WANG_COLOR_VALUE = 17;//小王
    public static final byte POKER_CODE_DA_WANG = 0x52;//大王
    public static final byte DA_WANG_COLOR_VALUE = 18;//大王
    public static final int POKER_CODE_COLOR_SHIFTS = 4;//花色部分的移位，花色，

    //斗地主牌的类型
    public static final int DDZ_CARDTYPE_DAN = 0x1; //单牌
    public static final int DDZ_CARDTYPE_DUI_ZI = 0x2;//对子
    public static final int DDZ_CARDTYPE_SAN_BU_DAI = 0x4;//三张
    public static final int DDZ_CARDTYPE_SAN_DAI_YI = 0x8;//三带一
    public static final int DDZ_CARDTYPE_SAN_DAI_ER = 0x10;//三带二
    public static final int DDZ_CARDTYPE_SHUN_ZI = 0x20;//顺子
    public static final int DDZ_CARDTYPE_ZHA_DAN = 0x40;//炸弹
    public static final int DDZ_CARDTYPE_WANG_ZHA = 0x80;//王炸
    public static final int DDZ_CARDTYPE_LIAN_DUI = 0x100;//连对
    public static final int DDZ_CARDTYPE_SI_DAI_ER = 0x200;//四带二
    public static final int DDZ_CARDTYPE_SI_DAI_ER_DUI = 0x400;//四带二
    public static final int DDZ_CARDTYPE_FEI_JI_BU_DAI = 0x800;//飞机不带
    public static final int DDZ_CARDTYPE_FEI_JI_DAI_DUI = 0x1000;//飞机带对
    public static final int DDZ_CARDTYPE_FEI_JI_DAI_DAN = 0x2000;//飞机带单


    /*炸金花的牌型*/
    public static final int ZJH_CARDTYPE_DAN = 0x1;//散牌
    public static final int ZJH_CARDTYPE_DUI_ZI = 0x2;//对子
    public static final int ZJH_CARDTYPE_SHUN_ZI = 0x4;//顺子
    public static final int ZJH_CARDTYPE_JIN_HUA = 0x8;//金花
    public static final int ZJH_CARDTYPE_SHUN_JIN = 0x10;//顺金
    public static final int ZJH_CARDTYPE_BAO_ZI = 0x20;//豹子

    //玩家的炸金花操作
    public static final int ZJH_OPERTAION_SEE = 0x1;//看牌
    public static final int ZJH_OPERTAION_BI_PAI = 0x2;//比牌
    public static final int ZJH_OPERTAION_GEN_ZHU = 0x4;//跟注
    public static final int ZJH_OPERTAION_QI_PAI = 0x8;//弃牌
    public static final int ZJH_OPERTAION_JIA_ZHU = 0x10;//加注

    //JACK的牌型
    public static final int JACK_CARDTYPE_BAO_PAI = 0x1;//爆牌
    public static final int JACK_CARDTYPE_COMMON = 0x2;//1-21点
    public static final int JACK_CARDTYPE_JACK = 0x4;//杰克(2倍)
    public static final int JACK_CARDTYPE_WU_XIAO_LONG = 0x8;//五小龙(3倍)
    public static final int JACK_CARDTYPE_BAO_ZI = 0x10;//豹子(5倍)
    public static final int JACK_CARDTYPE_SHUANG_LONG = 0x20;//双龙(5倍)
    public static final int JACK_CARDTYPE_SHUANG_WANG = 0x40;//双王(8倍)
    public static final int JACK_CARDTYPE_BAO_QI = 0x80;//豹七(10倍)

    //玩家的杰克操作
    public static final int JACK_OPERTAION_YAO_PAI = 0x1;//要牌
    public static final int JACK_OPERTAION_TING_PAI = 0x2;//停牌
    public static final int JACK_OPERTAION_YAN_PAI = 0x4;//验牌
    public static final int JACK_OPERTAION_XIA_ZHU = 0x8;//下注
    public static final int JACK_OPERTAION_YA_WU_XIAO_LONG = 0x10;//压五小龙
    public static final int JACK_OPERTAION_BU_YA_WU_XIAO_LONG = 0x20;//不压五小龙
    public static final int JACK_OPERTAION_SEND_CARD = 0x40;//发牌

    //玩家的28杠操作
    public static final int ErBa_OPERTAION_QIANG_ZHUANG = 0x1;//抢庄
    public static final int ErBa_OPERTAION_BU_QIANG_ZHUANG = 0x2;//不抢庄
    public static final int ErBa_OPERTAION_XIA_ZHU = 0x4;//下注
    public static final int ErBa_OPERTAION_SEND_CARD = 0x8;//发牌
    public static final int ErBa_OPERTAION_SEE_CARD = 0x10;//看牌
    public static final int ErBa_OPERTAION_KAI_CARD = 0x20;//开牌
    public static final int ErBa_OPERTAION_CONFIRM_BANKER = 0x40;//确认庄家

    //玩家的28杠牌型
    public static final int ERBA_CARDTYPE_DUI_ZI = 0x1;//对子
    public static final int ERBA_CARDTYPE_ER_BA = 0x2;//28杠
    public static final int ERBA_CARDTYPE_SAN_PAI = 0x4;//散排
    //玩家的牛牛操作
    public static final int NN_OPERTAION_QIANG_ZHUANG = 0x1;//抢庄
    public static final int NN_OPERTAION_XIA_ZHU = 0x2;//下注
    public static final int NN_OPERTAION_CUO_PAI = 0x4;//搓牌
    public static final int NN_OPERTAION_SEE_CARD = 0x8;//看牌
    public static final int NN_OPERTAION_CONFIRM_BANKER = 0x10;//确认地主

    //牛牛牌型(无牛-牛牛分别是0-10)
    public static final int NN_CARDTYPE_WU_HUA_NIU = 11;//五花牛
    public static final int NN_CARDTYPE_SHUN_ZI_NIU = 12;//顺子牛
    public static final int NN_CARDTYPE_TONG_HUA_NIU = 13;//同花牛
    public static final int NN_CARDTYPE_HU_LU_NIU = 14;//葫芦牛
    public static final int NN_CARDTYPE_ZHA_DAN_NIU = 15;//炸弹牛
    public static final int NN_CARDTYPE_SHUN_JIN_NIU = 16;//顺金牛
    public static final int NN_CARDTYPE_WU_XIAO_NIU = 17;//五小牛
}
