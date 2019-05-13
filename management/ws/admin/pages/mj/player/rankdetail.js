var md = angular.module('module_mj/player/rankdetail', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var gameId = params.gameId;
        var rankType = params.rankType;
        var groupDatetime = params.groupDatetime;

        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};

        $scope.queryLl = function () {
            var url = '/a/mj/player/rankdetail/' ;

            uiTips.loading();
            $http.get(url, {params: {gameId:gameId, rankType:rankType,groupDatetime:groupDatetime}}).success(function (data) {
                data = data.data;
                $scope.ll = data;
                $scope.rankType = rankType;
            });
        };
        $scope.queryLl();

        $scope.getGameTypeTxt = function(type) {
            if(type=="G_DQMJ") return '大庆麻将';
            if(type=="G_SHMJ") return '绥化麻将';
            if(type=="G_QSMJ") return '齐齐哈尔麻将';
        }

        $scope.getRankTypeTxt = function(type) {
            return {1:'财富榜', 2:'周开局', 3:'月开局'}[type]
        }
    }
);