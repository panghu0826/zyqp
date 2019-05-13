var md = angular.module('module_mj/player/auth-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};

        $scope.doAuth = function () {
            var p = _.clone($scope.query);

            var url = '/a/mj/player/auth/add';
            $scope.msg = "提交中"
            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.msg = "授权成功,玩家重新登录即可生效";
                    uiTips.alert("授权成功,玩家重新登录即可生效");
                } else {
                    $scope.msg = data.message;
                }
            });
        };
    }
);