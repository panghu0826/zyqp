var md = angular.module('module_mj/agent/month-report', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/agent/month_report';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                $scope.ll = data.data
            });
        };
        $scope.queryLl();
    }
);