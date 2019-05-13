var md = angular.module('module_mj/agent/notice-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/agent/notice/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.del = function(one) {
            var cb = function(){
                var url = '/a/mj/agent/notice/del'
                uiTips.loading();
                $http.get(url, {params: {id:one.id}}).success(function (data) {
                    $scope.queryLl();
                });
            }
            uiTips.confirm('确定要删除吗？', function () {
                cb();
            }, null);
        }

        $scope.doSaveNotice = function() {
            var url = '/a/mj/agent/notice/save';
            uiTips.loading();
            $http.post(url, $scope.tmp.notice).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.queryLl()
                    $scope.ctrl.isShowAdd=false
                } else {
                    uiTips.alert(data.message);
                }
            });
        }
    }
);