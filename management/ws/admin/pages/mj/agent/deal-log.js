var md = angular.module('module_mj/agent/deal-log', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {dealType:params.dealType};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/agent/deal/logs';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                $scope.ll = data.data.pager.ll;
                $scope.total = data.data.pager;
            });
        };
        $scope.queryLl();
    }
);