var md = angular.module('module_mj/desk/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp = {}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/game/desk/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                var data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.getStatusLabel = function(one) {
            var label = {0:"冻结", 1:"激活", 10 : "删除"};
            return label[one.status]
        }

        $scope.addAgent = function () {
            var p = _.clone($scope.tmp.editOne);
            var url = '/a/mj/agent/info/add';
            uiTips.loading();
            $http.post(url, p).success(function (data) {
                if(data.statusCode == 0) {
                    window.location.reload();
                } else {
                    uiTips.alert(data.message);
                }
            });
        };

        $scope.chageStatus = function(one, status) {
            var url = '/a/mj/agent/status';
            uiTips.loading();
            var p = {id:one.id, status:status}
            $http.get(url, {params: p}).success(function (data) {
                if(data.statusCode == 0) {
                    one.status = status;
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.addFangka = function(one) {
            $scope.ctrl.isShowAddFangka=true;
            $scope.tmp.agent={username:one.username, name:one.name, id:one.id}
            $scope.tmp.editOne = one;
        }

         $scope.addIntegral = function(one) {
            $scope.ctrl.isShowAddIntegral=true;
            $scope.tmp.agent={username:one.username, name:one.name, id:one.id}
            $scope.tmp.editOne = one;
        }

        $scope.doAddFangka = function() {
            var url = '/a/mj/agent/fangka/add';
            uiTips.loading();
            $http.get(url, {params: $scope.tmp.agent}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.tmp.editOne.fangka = data.data.fangka;
                    $scope.ctrl.isShowAddFangka = false;
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.doAddIntegral = function() {
            var url = '/a/mj/agent/integral/add';
            uiTips.loading();
            $http.get(url, {params: $scope.tmp.agent}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.tmp.editOne.integral = data.data.integral;
                    $scope.ctrl.isShowAddIntegral = false;
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.dismissDesk = function(one) {
            var url = '/a/mj/desk/dismiss';
            uiTips.loading();
            $http.get(url, {params: {gameId:one.gameId, matchId:one.matchId,deskId:one.deskId, instanceId:one.instanceId}}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.dump = function(one) {
            var url = '/a/mj/desk/dump';
            window.open(url+"?gameId="+one.gameId+"&matchId="+one.matchId+"&deskId="+one.deskId+"&instanceId="+one.instanceId);
        }
    }
);