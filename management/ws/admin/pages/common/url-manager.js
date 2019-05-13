var md = angular.module('module_common/url-manager', ['base', 'ng.ext.uploadify']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiValid) {
    $scope.ctrl = {};
    $scope.queryLl = function (pageNum) {
        pageNum = pageNum || 1;
        var p = _.clone($scope.query);
        uiTips.loading();
        $http.get('/a/admin/user/url/list/'+pageNum, {params: p}).success(function (data) {
            $scope.ll = data.data.ll;
            $scope.pager = {pageNum: pageNum, pageSize: data.data.pageSize, totalCount: data.data.totalCount};
        });
    };
    $scope.queryLl(1);

    $scope.init = function () {
        uiTips.confirm('确定需要更新数据吗?', function () {
        uiTips.loading();
        $http.get('/a/admin/user/url/init').success(function (data) {
            if(data.statusCode==0){
                uiTips.alert("数据已更新")
            }
        });
    }, null);
    };
});