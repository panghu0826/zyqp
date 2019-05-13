var md = angular.module('module_user/list', ['base', 'ng.ext.uploadify']);
md.controller('MainCtrl', function ($scope, $http, uiTips, uiValid) {
    $scope.ctrl = {};
    $scope.query = {};
    $scope.tmp = {editOne: {}, roleLabelChoose: {}};
    var statusLabels = {1: '', 0: '屏蔽'};
    $scope.getStatusLabel = function (status) {
        return statusLabels[status];
    };

    $scope.queryLl = function (pageNum, x, y) {
        pageNum = pageNum || 1;

        var p = _.clone($scope.query);
        p.needQueryRoleList = x;
        p.needQueryLabelList = y;

        uiTips.loading();
        $http.get('/a/admin/user/list/' + pageNum, {params: p}).success(function (data) {
            uiTips.unloading();
            $scope.tmp.isAllChecked = false;

            $scope.ll = data.pager.ll;
            $scope.pager = {pageNum: pageNum, pageSize: data.pager.pageSize, totalCount: data.pager.totalCount};

            if (data.roleList) {
                roleList = data.roleList;
                store.set('roleList', data.roleList);

                $scope.tmp.roleList = roleList
            }
            if (data.labelList) {
                labelList = data.labelList;
                store.set('labelList', data.labelList);

                $scope.tmp.labelList = labelList
            }
        });
    };

    var store = new Store(true);
    var roleList = store.get('roleList');
    var labelList = store.get('labelList');
    var needQueryRoleList = !roleList;
    var needQueryLabelList = !labelList;

    $scope.tmp.roleList = roleList
    $scope.tmp.labelList = labelList
    $scope.queryLl(1, needQueryRoleList, needQueryLabelList);

    $scope.getRoleName = function (role, type) {
        var targetList = 'role' == type ? roleList : labelList;
        var arr = _.filter(targetList, function (it) {
            return (it.code & role) == it.code;
        });
        return _.pluck(arr, 'name').join("\r\n");
    };

    $scope.changeStatus = function (one, status) {
        var cb = function () {
            uiTips.loading();
            $http.get('/a/admin/user/status/' + one.id, {params: {status: status}}).success(function (data) {
                one.status = status;
            });
        };
        if (0 == status) {
            uiTips.confirm('确定要屏蔽吗？', function () {
                cb();
            }, null);
        } else {
            cb();
        }
    };

    // 截图或图文图片上传后调用
    $scope.uploadSuccess = function (file, json, response) {
        $scope.tmp.editOne.headimgurl = json.url;
    };

    $scope.uploadImport = function (file, json, response) {
        if (json.flag) {
            $scope.ctrl.isShowImport = false;
            $scope.queryLl();
        } else {
            uiTips.alert(json.error);
        }
    };

    $scope.edit = function (one) {
        $scope.tmp.editOne = _.clone(one);
        $scope.ctrl.isShowAdd = true;
    };

    $scope.save = function () {
        if (!uiValid.checkForm($scope.tmp.addForm) || !$scope.tmp.addForm.$valid) {
            uiTips.alert('请正确录入信息！');
            return;
        }

        var one = _.clone($scope.tmp.editOne);
        uiTips.loading();
        $http.post('/a/admin/user/save', one).success(function (data) {
            $scope.ctrl.isShowAdd = false;
            $scope.queryLl();
        });
    };

    $scope.setRole = function (one, type) {
        var choosedList = _.where($scope.ll, {isChecked: true});
        if (!one && !choosedList.length) {
            uiTips.alert('请选择要操作的记录！');
            return;
        }

        var usernameBatch = _.pluck(choosedList, 'username').join(',');

        var x = $scope.tmp.roleLabelChoose;
        x.title = one ? one.username : usernameBatch;
        x.type = type;

        if (one) {
            $scope.tmp.editOne = _.clone(one);
            x.role = one.role;
            x.label = one.label;
        }

        $scope.ctrl.isShowSetRole = true;
    };

    $scope.setRoleSave = function () {
        var role = $scope.tmp.roleLabelChoose.role || 0;
        var label = $scope.tmp.roleLabelChoose.label || 0;

        var userId = $scope.tmp.editOne.id;
        var isBatch = !userId;
        // 批量处理的情况
        if (isBatch) {
            var choosedList = _.where($scope.ll, {isChecked: true});
            userId = _.pluck(choosedList, 'id').join(',');
        }

        uiTips.loading();
        $http.post('/a/admin/user/role/update', {
            role: role,
            label: label,
            userId: userId
        }).success(function (data) {
            $scope.ctrl.isShowSetRole = false;
            if (isBatch) {
                $scope.queryLl();
            } else {
                _.each($scope.ll, function (it) {
                    if (it.id == userId) {
                        it.role = role;
                        it.label = label;
                    }
                });
            }
        });
    };

    $scope.setPwd = function (one) {
        var randPwd = '';
        _.times(6, function () {
            randPwd += _.random(0, 9);
        });
        uiTips.prompt('请输入修改后的密码：', function (val) {
            uiTips.loading();
            $http.post('/a/admin/user/pwd', {id: one.id, pwd: val}).success(function (data) {
                if (flag) {
                    uiTips.alert('修改成功！');
                }
            });
        }, randPwd);
    };

    // 全选
    $scope.$watch('tmp.isAllChecked', function (isChecked) {
        _.each($scope.ll, function (it) {
            it.isChecked = isChecked;
        });
    });

    //测试
    /**
    var obj = {"id":"6","location":"深圳市市民中心","users":[{"姓名":"1212","公司":"231231233","职位":"","手机":""}],"appVer":"2.0","tssign":1466159160101,"appInfo":"tjq_web"}
    $http.post('http://dev.gxtai9.com/a/tj/activity/sign_up', obj).success(function (data) {
    });
     **/
});