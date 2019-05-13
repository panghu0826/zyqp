var md = angular.module('module_mj/clust/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.queryLl = function () {
            var p = _.clone($scope.query);

            var url = '/a/mj/clust/srv/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    data = data.data;
                    $scope.ll = data;
                } else {
                    uiTips.alert(data.message);
                }
            });
        };
        $scope.queryLl();


        $scope.start = function(one) {
            var url = '/a/mj/clust/srv/start';
            uiTips.loading();
            var p = {}
            $http.get(url, {params:{instanceId:one.instanceId,serverType:one.serverType}}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("已请求启动,请等待");
					$scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.stop = function(one) {
            var url = '/a/mj/clust/srv/stop';
            uiTips.loading();
            var p = {}
            $http.get(url, {params:{instanceId:one.instanceId,serverType:one.serverType}}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("已请求停止,请等待");
					$scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.viewSrv = function() {
            var url = '/a/mj/clust/srv/detail';
            uiTips.loading();
            var p = {}
            $http.get(url, {params:{instanceId:one.instanceId,serverType:one.serverType}}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.tmp.srvDetail = data.data;
                    $scope.ctrl.isShowDetail = true;
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

    }
);