var md = angular.module('module_mj/player/clubmember', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var id = params.id
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;
            p.id = id
            var url = '/a/mj/player/clubmember/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.getclubMemberType = function(type) {
            return {0:'普通成员', 1:'群主', 2:'管理'}[type]
        }
    }
);