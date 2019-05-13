var md = angular.module('module_common/base-config', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager) {
    $scope.queryLl = function () {
        uiTips.loading();
        $http.get('/a/admin/config/list').success(function (data) {
            var ll = [];
            var props = data.props;
            for (var k in props) {
                ll.push({key: k, value: props[k]});
            }
            $scope.ll = ll;
        });
    };
    $scope.queryLl();

    $scope.save = function (isFlushFromDb) {
        uiTips.loading();
        $http.post('/a/admin/config/update?isFlushFromDb=' + (isFlushFromDb || ''), {ll: $scope.ll}).success(function (data) {
            _.each($scope.ll, function (it) {
                it.isEdit = false;
            });
        });
    };
});