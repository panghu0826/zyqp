var md = angular.module('module_mj/player/clubapply', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var id = params.id
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.queryLl = function () {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;
            p.id = id
            var url = '/a/mj/player/clubapply/' + pageNum;

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