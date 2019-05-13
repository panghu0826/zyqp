var md = angular.module('module_mj/player/rank', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params()
        var contentType = 1
        $scope.query = {rankType:'2',gameType: '1'}
        $scope.ctrl = {}
        $scope.tmp={
            rankTypeList:[{code:'2',name:'周开局'}, {code:'3',name:"月开局"}, {code:'1',name:"财富榜"}],
            gameTypeList:[{code:'1',name:'大庆'}, {code:'2',name:"绥化"}, {code:'3',name:"齐市"}]
        }

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/player/rank/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.rankShu = data.rankShu;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.view = function (one, isCopy) {
            Page.go('/page/mj_player_rankdetail', {
                // {gameId:one.gameId, rankType:one.pointType,groupDatetime:one.groupDatetime}
                gameId:one.gameId, rankType:one.pointType, groupDatetime:one.groupDatetime
            });
        };

        $scope.getRankTypeTxt = function(type) {
            return {1:'财富榜', 2:'周开局', 3:'月开局'}[type]
        }

        $scope.getGameTypeTxt = function(type) {
            if(type=="G_DQMJ") return '大庆麻将';
            if(type=="G_SHMJ") return '绥化麻将';
            if(type=="G_QSMJ") return '齐齐哈尔麻将';
        }
    }
);