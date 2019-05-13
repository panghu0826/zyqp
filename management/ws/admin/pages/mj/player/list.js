var md = angular.module('module_mj/player/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp={
            userTypeList:[{code:'3',name:'微信用户'}, {code:'2',name:"注册用户"}, {code:'1',name:"游客"}, {code:'',name:"不限"}],
            deviceTypeList:[{code:'1',name:'ios'}, {code:'2',name:"android"}, {code:'3',name:"winphone"}, {code:'4',name:"其它"},{code: '',name:'不限'}],
            onlineList:[{code:'1',name:'在线'}, {code:'2',name:"离线"}]
        }
        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/player/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.onlinePlayerNum = data.onlinePlayerNum;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();

        $scope.view = function (one, isCopy) {
            Page.go('/page/mj_player_detail', {
                id: one.id
            });
        };

        $scope.delete = function(one) {
            var cb = function(){

            }
            uiTips.confirm('确定要删除吗？', function () {
                cb();
            }, null);
        }

         $scope.getAuthText = function(one) {
            return (one.role & 2) == 2 ? "已授权" : "未授权"
         }

         $scope.isAuth = function(one) {
             return (one.role & 2) == 2 ;
         }

         $scope.cancelAuth = function (one) {
             var url = '/a/mj/player/auth/cancel';
             uiTips.loading();
             $http.get(url, {params: {userId:one.id}}).success(function (data) {
                 if(data.statusCode == 0) {
                     one.role = one.role - 2;
                     uiTips.alert("取消授权成功,玩家重新登录即可生效");
                 } else {
                     uiTips.alert(data.message);
                 }
             });
         };
    }
);