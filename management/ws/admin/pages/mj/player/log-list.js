var md = angular.module('module_mj/player/log-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp={operMainType:[
                {code:'coin', name:'金币'},{code:'fangka', name:"房卡"},
            ],
            operSubType:[
                {code:'UserBuyItem',name:'商城购买'},{code:'TaskFinish',name:'任务奖励'},{code:'RankAward',name:'排行榜奖励'},
                {code:'Exchange',name:'兑换所得'},{code:'ENROLL',name:'开局服务费'},{code:'GAME_WIN_LOSE',name:'游戏输赢'},
                {code:'BANK_ASSIST',name:'破产救济'},{code:'LOGIN_AWARD',name:'登录奖励'},{code:'CREATE_ROOM',name:'开房消耗'},
                {code:'ADMIN_CHANGE',name:'管理员修改'},{code:'MOVE',name:'转赠'},{code:'VIP_EXIT_ROOM',name:'vip房未开始离开桌子'},{code:'ENROLL_MULTI_MATCH',name:'比赛场报名'}
                ,{code:'EXIT_MULTI_MATCH',name:'退赛比赛场'},{code:'QUICK_RECEIVE_AWARD',name:'快速领取邮件奖励'},{code:'LUNPAN_AWARD',name:'轮盘奖励'},{code:'PRAY',name:'女神祈福'}
                ,{code:'SIGN',name:'签到'} ,{code:'BIND_INVITE_CODE',name:'绑定邀请码'}
            ]}

        $scope.getOperMainType = function(type) {
            var item = _.find($scope.tmp.operMainType, function(it){return it.code == type})
            return item ? item.name : ""
        }

        $scope.getOperSubType = function(type) {
            var item = _.find($scope.tmp.operSubType, function(it){return it.code == type})
            return item ? item.name : ""
        }

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/log/player/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();
    }
);