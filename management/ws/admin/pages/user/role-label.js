var md = angular.module('module_user/role-label', ['base']);
md.controller('MainCtrl', function ($scope, $http, uiTips) {
    $scope.ctrl = {};
    $scope.tmp = {targetOne: {}, menuList: []};

    $scope.isCurrentRole = true;
    $scope.changeTab = function (index) {
        $scope.isCurrentRole = index == 0;
        return true;
    };

    $http.get('/a/admin/user/role/list').success(function (data) {
        $scope.roleList = data.roleList;
        $scope.labelList = data.labelList;
    });

    $scope.roleList = [];
    $scope.labelList = [];

    $scope.addOne = function () {
        var list = $scope.isCurrentRole ? $scope.roleList : $scope.labelList;
        list.push({isEdit: true, id: 0});
    };

    $scope.del = function (one) {
        var list = $scope.isCurrentRole ? $scope.roleList : $scope.labelList;

        if (!one.id) {
            list.remove(one);
        } else {
            uiTips.confirm('确定要删除吗？', function () {
                var isLabel = $scope.isCurrentRole ? null : '1';

                uiTips.loading();
                $http.get('/a/admin/user/role/del/' + one.id, {params: {isLabel: isLabel}}).success(function (data) {
                    uiTips.unloading();

                    list.remove(one);

                    // remove storage
                    var store = new Store(true);
                    store.remove('roleList');
                    store.remove('labelList');
                });
            }, null);
        }
    };

    $scope.save = function (one) {
        if (!one.name || !one.code || !('' + one.code).match(/^\d+$/)) {
            uiTips.alert('请输入正确的名称和代码！');
            return;
        }

        var isLabel = !$scope.isCurrentRole;
        uiTips.loading();
        $http.post('/a/admin/user/role/add', {
            isLabel: isLabel,
            name: one.name,
            code: parseInt(one.code),
            id: one.id || 0
        }).success(function (data) {
            uiTips.unloading();

            one.isEdit = false;
            one.id = data.id;

            // remove storage
            var store = new Store(true);
            store.remove('roleList');
            store.remove('labelList');

        });
    };

    // 角色菜单
    $scope.showMenus = function (one) {
        $scope.tmp.targetOne = one;
        var menuTree = one.menus ? JSON.parse(one.menus) : [];
        var menuList = [];
        // tree to list
        if (menuTree.length) {
            _.each(menuTree, function (it) {
                menuList.push({title: it.title, icon: it.icon, page: it.page || ''});
                if (it.list) {
                    _.each(it.list, function (sub) {
                        menuList.push({title: sub.title, icon: sub.icon, page: sub.page || '', isSub: true, params: sub.params});
                    });
                }
            });
        }
        $scope.tmp.menuList = menuList;
        $scope.ctrl.isShowMenus = true;
    };

    $scope.menuPush=function(one){
        var list=$scope.tmp.menuList;
        var pos=-1;
        for(var i=0;i<list.length;i++){
             if(one==list[i]){//找到该主菜单位置
                 console.log(i)
                 for(var x=i+1;x<list.length;x++){
                     if(list[x].isSub!=true){//找到后一个主菜单位置
                         pos=x;
                         list.splice(pos,0,{isSub:true})
                         return
                     }
                 }
             }
        }
        //如果循环结束未触发return，直接在最后添加
        list.push({isSub:true})
    }

    $scope.saveMenus = function () {
        var menuList = $scope.tmp.menuList;
        var menuTree = [];

        if (menuList && menuList.length) {
            // list to tree
            var lastTop;
            _.each(menuList, function (it) {
                if (!it.isSub) {
                    lastTop = _.clone(it);
                    menuTree.push(lastTop);
                } else {
                    if (!lastTop)
                        return;
                    if (!lastTop.list) {
                        lastTop.list = [];
                    }

                    lastTop.list.push(it);
                }
            });
        }
        console.log(menuTree);
        var menus = JSON.stringify(menuTree);

        uiTips.loading();
        $http.post('/a/admin/user/role/menus/update', {
            menus: menus,
            id: $scope.tmp.targetOne.id
        }).success(function (data) {
            uiTips.unloading();

            $scope.ctrl.isShowMenus = false;
            $scope.tmp.targetOne.menus = menus;
        });
    };
});