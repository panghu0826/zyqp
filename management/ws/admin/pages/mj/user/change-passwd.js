var md = angular.module('module_mj/user/change-passwd', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiValid) {
    $scope.ctrl = {};
    $scope.query = {};
    $scope.tmp = {editOne: {}};

    $scope.changePasswd = function (one, passwd) {
        var cb = function () {
            uiTips.loading();
            $http.get('/a/admin/user/change_passwd/', {params: {oldPasswd:$scope.tmp.oldPasswd, newPasswd:$scope.tmp.newPasswd}}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("修改成功");
                } else {
                    uiTips.alert("修改失败");
                }
            });
        };
        uiTips.confirm('确定要修改密码？', function () {
            cb();
        }, null);
    };
});