var md = angular.module('module_mj/paixing/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp={}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/paixing/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
                console.log($scope.ll)
            });
        };
        $scope.queryLl();

        $scope.goToEdit = function (one, isCopy) {
            Page.go('/page/mj_paixing_add/id_'+one.id, {});
        };

        $scope.push2Desk = function(id) {
            $scope.tmp.id = id ;
            $scope.ctrl.isShowPush = true;
        }

        $scope.doPush2Desk = function () {
            var url = '/a/mj/paixing/replay';
            var id = $scope.tmp.id;
            var playerId = $scope.tmp.playerId
            uiTips.loading();
            $http.get(url, {params:{id:id, playerId: playerId || 1}}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("推送成功");
                    $scope.ctrl.isShowPush = false;
                } else {
                    uiTips.alert(data.message);
                }
            });
        };

        $scope.del = function(one) {
            var cb = function(){
                var url = '/a/mj/paixing/del';
                uiTips.loading();
                var p = {id:one.id, status:10}
                $http.get(url, {params: p}).success(function (data) {
                    if(data.statusCode == 0) {
                        $scope.queryLl();
                    }else {
                         uiTips.alert(data.message);
                     }
                });
            }
            uiTips.confirm('确定要删除吗？', function () {
                cb();
            }, null);
        }

        $scope.addPaixing = function(){
           Page.go('/page/mj_paixing_add', {});
        }
    }
);