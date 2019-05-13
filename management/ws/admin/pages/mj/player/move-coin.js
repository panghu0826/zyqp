var md = angular.module('module_mj/player/move-coin', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {};

    $scope.doMoveCoin = function () {
            var p = _.clone($scope.tmp);
            var url = '/a/mj/player/coin/move';
            $scope.tmp.msg = "提交中";
            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                   $scope.tmp.msg = "转送成功";
                   uiTips.alert("转送成功");
                } else {
                    $scope.tmp.msg = data.message
                   uiTips.alert(data.message);
                }
            });
        };
    }
);