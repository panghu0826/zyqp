var md = angular.module('module_mj/notice/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {type:0};
        $scope.ctrl = {};
        $scope.tmp = {}
        $scope.pageNum = 1;

        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;

            var url = '/a/mj/actnotice/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data;
            });
        };
        $scope.queryLl();

        $scope.save = function() {
            var url = '/a/mj/actnotice/save';
            uiTips.loading();
            $http.post(url, $scope.tmp.editOne).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.ctrl.isShowAdd = false;
                    $scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.del = function(one, status) {
            var url = '/a/mj/actnotice/del';
            uiTips.loading();
            var p = {id:one.id, status:status}
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.push = function(){
             var url = '/a/mj/actnotice/push';
            uiTips.loading();
            var p = {}
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("推送成功");
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }
    }
);