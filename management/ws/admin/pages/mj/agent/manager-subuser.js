var md = angular.module('module_mj/agent/manager-subuser', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page , uiValid) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp = {}
        $scope.pageNum = 1;

        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;

            var url = '/a/mj/agent/manager_subuser/' + $scope.pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.addSubUser = function () {
            if (!uiValid.checkForm($scope.tmp.addForm) || !$scope.tmp.addForm.$valid) {
                uiTips.alert('请正确录入信息！');
                return;
            }
            var p = _.clone($scope.tmp.editOne);
            var url = '/a/mj/agent/addSubuser/add';
            uiTips.loading();
            $http.post(url, p).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.queryLl();
                    $scope.ctrl.isShowAdd=false;
                } else {
                    uiTips.alert(data.message);
                }
            });
        };
    }
);