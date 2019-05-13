var md = angular.module('module_mj/statis/ddzdiamondcount', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {};
        $scope.ctrl = {};
        $scope.tmp={
            matchIdList:[{code:'2',name:'2人房(VIP)'}, {code:'3',name:"3人房(VIP)"}, {code:'4',name:"4人房(VIP)"}]
        }

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;
            p.type = 5;

            var url = '/a/mj/statis/diamondcount';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.allNum = data.allNum;
                $scope.ll = data.list;
            });
        };
        $scope.queryLl();
    }
);