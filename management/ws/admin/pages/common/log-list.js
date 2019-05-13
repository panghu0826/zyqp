var md = angular.module('module_common/log-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager) {
    var d = new Date();
    var lastWeek = d.add(-7);
    $scope.query = {
        beginDat: lastWeek.format('yyyy-MM-dd 00:00:00'),
        endDat: d.format('yyyy-MM-dd HH:mm:00')
    };
    $scope.queryLl = function (pageNum) {
        pageNum = pageNum || 1;

        var p = _.clone($scope.query);
        uiTips.loading();
        $http.get('/a/m/collect-log/' + (p.ns || 'default') + '/query/' + pageNum, {params: p}).success(function (data) {
            uiTips.unloading();
            $scope.ll = data.pager.ll;
            $scope.pager = uiPager.create(data.pager);
        });
    };
    $scope.queryLl();

    $scope.showOther = function (one) {
        if (!one.other) {
            uiTips.alert('');
            return;
        }

        var arr = JSON.parse(one.other);
        var str = _.map(arr, function (it) {
            return it.key + ' = ' + it.value;
        }).join('<br />');
        uiTips.alert(str);
    };
});