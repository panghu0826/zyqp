var md = angular.module('module_mj/match/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var roomType = params.roomType;
        $scope.query = {roomType:roomType};
        $scope.ctrl = {};
        $scope.tmp = {roomTypeList:[{code:"Normal",name:"金币场"},{code:"VIP",name:"VIP场"},{code:"Single",name:"单机场"},{code:"Multi",name:"比赛场"}]}
        $scope.pageNum = 1;


        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;
            p.roomType = roomType;

            var url = '/a/mj/match/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data;
            });
        };
        $scope.queryLl();

        $scope.edit = function(one) {
            $scope.tmp.editOne = one;
            $scope.ctrl.isShowAdd = true;
        }

        $scope.save = function() {
            var url = '/a/mj/match/save';
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

        $scope.publish = function() {
            var url = '/a/mj/match/publish';
            uiTips.loading();
            $http.get(url, {}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("发布成功");
                }else {
                     uiTips.alert(data.message);
                 }
            }).error(function(data){
                uiTips.alert("发布出错");
            });
        }
    }
);