var md = angular.module('module_mj/msg/marquee-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {marqueeTypeList:[{code:1,name:"一次性推送"}, {code:2, name:"定时推送"}], userGroupList:[{code:1,name:"安卓用户"}, {code:2, name:"ios用户"}]}
        $scope.pageNum = 1;

        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;

            var url = '/a/mj/msg/marquee/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data;
            });
        };
        $scope.queryLl();

        $scope.save = function() {
            var url = '/a/mj/msg/marquee/save';
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
            var url = '/a/mj/msg/marquee/del';
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

        $scope.send = function(one, status) {
            var url = '/a/mj/msg/marquee/send';
            uiTips.loading();
            var p = {id:one.id, status:status}
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("已通知服务器推送");
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }
    }
);