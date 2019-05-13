var md = angular.module('module_mj/history/vip-record', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.getPlayerName = function(one, ind) {
            return one.detail && one.detail.length > ind? one.detail[ind].playerName : ""
        }

        $scope.getPlayerScore = function(one, ind) {
            return one.detail && one.detail.length > ind? one.detail[ind].score : ""
        }

        $scope.queryLl = function (pageNum) {
            var p = _.clone($scope.query);
            p.type = 1;
            pageNum = pageNum || 1;

            var url = '/a/mj/game/vip/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    data = data.data;
                    $scope.ll = data.pager.ll;
                    $scope.pager = uiPager.create(data.pager);
                } else {
                    uiTips.alert(data.message);
                }
            });
        };
        $scope.queryLl();
    }
);