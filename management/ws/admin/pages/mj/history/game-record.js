var md = angular.module('module_mj/history/game-record', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.queryLl = function (pageNum) {
            var p = _.clone($scope.query);
            p.type = 1;
            pageNum = pageNum || 1;

            var url = '/a/mj/game/history/list/' + pageNum;

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