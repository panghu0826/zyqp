var md = angular.module('module_mj/player/add-diamond', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.tmp = {};
        $scope.ctrl = {};

        $scope.addDiamond = function () {
            var users = $scope.tmp.users;
            var diamond = $scope.tmp.diamond;
            if(!users) {
                uiTips.alert("填写用户id");
                return;
            }
            if(!diamond) {
                uiTips.alert("填写房卡");
                return;
            }


            var ua = [];
            var uaa = users.split(",")
            for(var i = 0; i < uaa.length; i++) {
                ua.push({userId:uaa[i]});
            }
            var p = {
                diamond : diamond,
                users: ua
            }

            var url = '/a/mj/player/diamond/batch_add';
            uiTips.loading();
            $http.post(url, p).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.results = data.data.users;
                } else {
                    $scope.msg = data.message;
                }
            });
        };
    }
);