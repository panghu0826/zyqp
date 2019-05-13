var md = angular.module('module_mj/mail/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp = {targetTypeList:[{code:1,name:"单个玩家"}, {code:2, name:"全服玩家"}],  //{code:3,name:"分组玩家"}
            userGroupList:[{code:0,name:"不限"},{code:1,name:"安卓用户"}, {code:2, name:"ios用户"}],
            itemList:[{code:'D001',name:'金币'},{code:'A001',name:'钻石'}]}
        $scope.pageNum = 1;

        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;

            var url = '/a/mj/msg/mail/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data;
            });
        };
        $scope.queryLl();

        $scope.save = function() {
            var url = '/a/mj/msg/mail/save';
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
            var url = '/a/mj/msg/mail/del';
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

        $scope.getTargetType = function(type) {
            var item = _.find($scope.tmp.targetTypeList, function(item){
                return item.code == type
            });
            return item ?  item.name : "未知"
        }

        $scope.send = function(one, status) {
            var url = '/a/mj/msg/mail/send';
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