var md = angular.module('module_mj/statis/daycount', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp={}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/statis/daycount';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.data = data
            });
        };
        $scope.queryLl();
    }
);