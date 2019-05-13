var md = angular.module('module_mj/player/clubwanfa', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.queryLl = function () {
            // console.log(angular.fromJson(params.clubWanfa))
            console.log("params--"+params.one.clubWanfa)
            $scope.one = params.one;
            $scope.one.clubWanfa = angular.fromJson(params.one.clubWanfa);
        };
        $scope.queryLl();

        $scope.getclubWanfaString = function(wanfa) {
            var DDZ_WHEEL_BANKER = 0X1;//斗地主轮庄
            var DDZ_WINNER_BANKER = 0x2;//斗地主赢家庄
            var DDZ_DOUBLE = 0X4;//斗地主加倍
            var DDZ_BETTER = 0x8;//斗地主比优

            var ZJH_SHUN_THAN_JIN = 0x10;//顺>金花
            var ZJH_DI_LONG = 0x20;//地龙(123 比QKA小 比其余顺子大,不选是最小的)
            var ZJH_FENG_KUANG = 0x40;//疯狂玩法(只有10JQKA)
            var ZJH_BI_PAI_JIA_BEI = 0x80;//比牌加倍
            var ZJH_ZHONG_TU_JIN_RU = 0x100;//中途禁入
            var ZJH_235_THAN_BAO_ZI = 0x200;//散235大豹子
            var ZJH_235_THAN_AAA = 0x400;//散235大AAA
            var ZJH_WANG_LAI_ZI = 0x800;//王癞子
            var ZJH_XI_QIAN = 0x1000;//豹子同花顺加分
            var ZJH_TONG_PAI_BI_HUA_SE = 0x2000;//同大小比花色
            var ZJH_AUTO_QI_PAI = 0x4000;//自动弃牌
            var ZJH_CAN_CUO_PAI = 0x8000;//可以搓牌

            var JACK_TUO_GUAN = 0x10000;//离线托管
            var JACK_FORCE_CHU_PAI = 0x20000;//强制出牌
            var JACK_XIA_MAN_ZHU = 0x40000;//下满注
            var JACK_LUN_LIU_ZHUANG = 0x80000;//轮流庄

            var result = "";
            if ((wanfa & DDZ_BETTER) === DDZ_BETTER) {
                result += "比优 ";
            }
            if ((wanfa & DDZ_DOUBLE) === DDZ_DOUBLE) {
                result += "踢 ";
            }
            if ((wanfa & DDZ_WHEEL_BANKER) === DDZ_WHEEL_BANKER) {
                result += "轮庄 ";
            }
            if ((wanfa & DDZ_WINNER_BANKER) === DDZ_WINNER_BANKER) {
                result += "赢家庄 ";
            }
            if ((wanfa & ZJH_SHUN_THAN_JIN) === ZJH_SHUN_THAN_JIN) {
                result += "顺>金花 ";
            }
            if ((wanfa & ZJH_DI_LONG) === ZJH_DI_LONG) {
                result += "地龙 ";
            }
            if ((wanfa & ZJH_FENG_KUANG) === ZJH_FENG_KUANG) {
                result += "疯狂模式 ";
            }
            if ((wanfa & ZJH_BI_PAI_JIA_BEI) === ZJH_BI_PAI_JIA_BEI) {
                result += "比牌加倍 ";
            }
            if ((wanfa & ZJH_ZHONG_TU_JIN_RU) === ZJH_ZHONG_TU_JIN_RU) {
                result += "中途禁入 ";
            }
            if ((wanfa & ZJH_235_THAN_BAO_ZI) === ZJH_235_THAN_BAO_ZI) {
                result += "散235大豹子 ";
            }
            if ((wanfa & ZJH_235_THAN_AAA) === ZJH_235_THAN_AAA) {
                result += "散235大AAA ";
            }
            if ((wanfa & ZJH_WANG_LAI_ZI) === ZJH_WANG_LAI_ZI) {
                result += "王癞子 ";
            }
            if ((wanfa & ZJH_XI_QIAN) === ZJH_XI_QIAN) {
                result += "豹子同花喜钱 ";
            }
            if ((wanfa & ZJH_TONG_PAI_BI_HUA_SE) === ZJH_TONG_PAI_BI_HUA_SE) {
                result += "同大小比花色 ";
            }
            if ((wanfa & ZJH_AUTO_QI_PAI) === ZJH_AUTO_QI_PAI) {
                result += "自动弃牌 ";
            }
            if ((wanfa & ZJH_CAN_CUO_PAI) === ZJH_CAN_CUO_PAI) {
                result += "看牌可搓牌 ";
            }
            if ((wanfa & JACK_TUO_GUAN) === JACK_TUO_GUAN) {
                result += "托管 ";
            }
            if ((wanfa & JACK_FORCE_CHU_PAI) === JACK_FORCE_CHU_PAI) {
                result += "强制出牌 ";
            }
            if ((wanfa & JACK_XIA_MAN_ZHU) === JACK_XIA_MAN_ZHU) {
                result += "下满注 ";
            }
            if ((wanfa & JACK_LUN_LIU_ZHUANG) === JACK_LUN_LIU_ZHUANG) {
                result += "轮流庄 ";
            }

            return result;
        }
    }
);