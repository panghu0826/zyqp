var md = angular.module('module_mj/agent/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
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

            var url = '/a/mj/agent/list/' + $scope.pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.fangKaNum = data.fangKaNum;
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
                    $scope.queryLl();
                    $scope.ctrl.isShowAdd=false;
                } else {
                    uiTips.alert(data.message);
                }
            });
        };

        $scope.loginAgentSys = function(){
            window.open("/static/ana/login.html");
        }

        $scope.chageStatus = function(one, status) {
            var url = '/a/mj/agent/status';
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
                    $scope.tmp.editOne.totalBuyFangka = data.data.totalBuyFangka;
                    $scope.ctrl.isShowAddFangka = false;
                    $scope.queryLl();
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
                    $scope.queryLl();
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }
    }
);