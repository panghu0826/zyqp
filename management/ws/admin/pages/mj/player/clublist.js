var md = angular.module('module_mj/player/clublist', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/player/clublist/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.viewMember = function (one, isCopy) {
            Page.go('/page/mj_player_clubmember', {
                id: one.id
            });
        };
        $scope.viewWanfa = function (one, isCopy) {
            console.log("玩法--"+one.clubWanfa)
            Page.go('/page/mj_player_clubwanfa', {
                one: one
            });
        };
        $scope.viewApply = function (one, isCopy) {
            Page.go('/page/mj_player_clubapply', {
                id: one.id
            });
        };

        $scope.getclubCreateRoomType = function(type) {
            return {1:'管理员开房', 2:'所有人开房'}[type]
        }

        $scope.getcanFuFenType = function(type) {
            return {2:'可负分', 1:'不可负分'}[type]
        }

    }
);