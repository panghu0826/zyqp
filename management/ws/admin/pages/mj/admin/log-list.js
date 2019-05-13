var md = angular.module('module_mj/admin/log-list', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiPager, Page) {
        var params = Page.params();
        var contentType = 1;
        $scope.query = {auth:0};
        $scope.ctrl = {};
        $scope.tmp={operType:[
                {code:'ADD_USER_FANGKA', name:'给用户添加房卡'},{code:'ADD_AGENT', name:"添加代理商"},
                {code:'DEL_AGENT', name:'删除代理商'},{code:'ADD_AGENT_FANGKA', name:"代理商进货"},
                {code:'PUBLISH_MALL_CONF', name:'发布商城配置'},{code:'EDIT_MALL_CONF', name:"修改商城配置"}
            ]}

        $scope.queryLl = function (pageNum) {
            var pageNum = pageNum || 1;
            var p = _.clone($scope.query);
            p.pageNum = pageNum;

            var url = '/a/mj/log/admin/list/' + pageNum;

            uiTips.loading();
            $http.get(url, {params: p}).success(function (data) {
                data = data.data;
                $scope.ll = data.pager.ll;
                $scope.pager = uiPager.create(data.pager);
            });
        };
        $scope.queryLl();
    }
);