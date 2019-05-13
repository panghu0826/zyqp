var md = angular.module('module_mj/mall/list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp = {catagoryList:[{code:0, name:"金币栏目"}, {code:1, name:"钻石栏目"}], itemType:[{code:'D001',name:"金币"},{code:'A001',name:'钻石'}]}
        $scope.pageNum = 1;

        $scope.queryLl = function (pageNum) {
            $scope.pageNum = pageNum || $scope.pageNum;
            var p = _.clone($scope.query);
            p.pageNum = $scope.pageNum;

            var url = '/a/mj/conf/shop/list';

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data;
            });
        };
        $scope.queryLl();

        $scope.save = function() {
            var url = '/a/mj/conf/shop/save';
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

        $scope.changeStatus = function(one, status) {
            var url = '/a/mj/conf/shop/status';
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

        $scope.publish = function(one, status) {
            var url = '/a/mj/conf/shop/publish';
            uiTips.loading();
            var p = {}
            $http.get(url, {}).success(function (data) {
                if(data.statusCode == 0) {
                    uiTips.alert("发布成功");
                }else {
                     uiTips.alert(data.message);
                 }
            });
        }

        $scope.getCategory = function(category) {
            console.log($scope.tmp.catagoryList)
            var item = _.find($scope.tmp.catagoryList, function(item){
                return item.code == category;
            });
            return item? item.name : "";
        }

        $scope.getItemName = function(category) {
            var item = _.find($scope.tmp.itemType, function(item){
                return item.code == category;
            });
            return item ? item.name : "";
        }

        $scope.getStatus = function(status) {
            return status == 1 ? "上架":"下架";
        }
    }
);