var md = angular.module('module_mj/player/detail', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var id = params.id;
        console.log(id)

        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
       $scope.ctrl.userType = [{code:11, name:'安卓游客登录'},{code:12, name:'安卓微信登录'},{code:13, name:'安卓帐号登录'},{code:21, name:'ios游客登录'}
                          ,{code:22, name:'ios微信登录'},{code:23, name:'ios帐号登录'},{code:31, name:'pc游客登录'},{code:33, name:'pc帐号登录'}]

        $scope.load = function () {

            var url = '/a/mj/player/detail/' + id;

            uiTips.loading();
            $http.get(url, {}).success(function (data) {
                data = data.data;
                $scope.user = data;
            });
        };
        $scope.load();

         $scope.getAuthText = function(one) {
            if(!one) return ""
            return (one.role & 2) == 2 ? "已授权" : "未授权"
         }

         $scope.isAuth = function(one) {
             if(!one) return false;
             return one && (one.role & 2) == 2 ;
         }

         $scope.cancelAuth = function (one) {
             var url = '/a/mj/player/auth/cancel';
             uiTips.loading();
             $http.get(url, {params: {userId:one.id}}).success(function (data) {
                 if(data.statusCode == 0) {
                     one.role = one.role - 2;
                 } else {
                     uiTips.alert(data.message);
                 }
             });
         };

        $scope.setCoin = function (one, coin) {
             var url = '/a/mj/player/coin/edit';
             uiTips.loading();
             $http.get(url, {params: {userId:one.id, coin:coin}}).success(function (data) {
                 if(data.statusCode == 0) {
                     uiTips.alert("修改成功");
                 } else {
                     uiTips.alert(data.message);
                 }
             });
         };

         $scope.setFanka = function (one, fanka) {
              var url = '/a/mj/player/fanka/edit';
              uiTips.loading();
              $http.get(url, {params: {userId:one.id, fanka:fanka}}).success(function (data) {
                  if(data.statusCode == 0) {
                      uiTips.alert("修改成功");
                  } else {
                      uiTips.alert(data.message);
                  }
              });
          };

         $scope.setDiamond = function (one, diamond) {
              var url = '/a/mj/player/diamond/edit';
              uiTips.loading();
              $http.get(url, {params: {userId:one.id, diamond:diamond}}).success(function (data) {
                  if(data.statusCode == 0) {
                      uiTips.alert("修改成功");
                  } else {
                      uiTips.alert(data.message);
                  }
              });
          };

         $scope.resetPwd = function (one, passwd) {
              var url = '/a/mj/player/passwd/change';
              uiTips.loading();
              $http.get(url, {params: {userId:one.id, passwd:passwd}}).success(function (data) {
                  if(data.statusCode == 0) {
                      uiTips.alert("修改成功");
                  } else {
                      uiTips.alert(data.message);
                  }
              });
          };

         $scope.clearDesk = function (one, passwd) {
              var url = '/a/mj/player/desk/clear';
              uiTips.loading();
              $http.get(url, {params: {userId:one.id}}).success(function (data) {
                  if(data.statusCode == 0) {
                      uiTips.alert("清理成功");
                  } else {
                      uiTips.alert(data.message);
                  }
              });
          };
          $scope.changeUserType = function (one) {
               var url = '/a/mj/player/desk/clear';
               uiTips.loading();
               $http.get(url, {params: {userId:one.id}}).success(function (data) {
                   if(data.statusCode == 0) {
                       uiTips.alert("清理成功");
                   } else {
                       uiTips.alert(data.message);
                   }
               });
           };

           $scope.getUserTypeTxt = function(type) {
             return {1:'游客登录', 2:'帐号登录', 3:'微信登录', 0:'未知'}[type]
           }

    }
);