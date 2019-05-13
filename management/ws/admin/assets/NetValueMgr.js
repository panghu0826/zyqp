/**
 * Created by jaime on 2016/6/20.
 */

var NetValueMgr = function($scope, $http, uiTips, uiValid){
    $scope.netValueList = []
    $scope.netValueTable = new Table($scope.netValueList)

    $scope.saveNetValue = function(){
        var one = _.clone($scope.tmp.netValue);
        if (!one.netValueTime){
            uiTips.alert('日期不能为空！');
            return;
        }
        if (!one.netValue){
            uiTips.alert('净值不能为空！');
            return;
        }
        if(!/^\d+(\.\d+)?$/.test(one.netValue)) {
            uiTips.alert("净值只能是数值");
            return;
        }
        if (!one.totalValue){
            uiTips.alert('累计净值不能为空！');
            return;
        }
        if(!/^\d+(\.\d+)?$/.test(one.totalValue)) {
            uiTips.alert("累计净值只能是数值");
            return;
        }
        var list = $scope.netValueList
        $scope.ctrl.showAddNetValue = false;
        if(!one.id) {
            list.push(one)
        } else {
            for(var i = 0; i <= list.length;i++) {
                if(list[i].id == one.id) {
                    list[i] = one;
                    break;
                }
            }
        }
    }

    $scope.delNetValue = function(one){
        if(one.id) {
            uiTips.loading();
            $http.get('/a/admin/taiji/product/del_net_value/'+one.id,{}).success(function (data) {
                if(data.statusCode == 0) {
                    $scope.netValueTable.del(one)
                }
            });
        } else {
            $scope.netValueTable.del(one)
        }
    }

    $scope.editNetValue = function(one){
        $scope.tmp.netValue = _.clone(one);
        $scope.tmp.netValue.isEdit = true
        $scope.ctrl.showAddNetValue = true;
    }

    $scope.importNetValue = function() {
        if(!$scope.product.id) {
            uiTips.alert("当前产品未保存,不能导入")
            return
        }
        $scope.ctrl.showImportNetValue = true;
    }

    $scope.uploadNetValueResult = function (file, json, response) {
        if (json.flag) {
            $scope.ctrl.showImportNetValue = false;
            uiTips.alert("导入完成,请重新刷新本页面");
            $scope.loadProductNetValueList($scope.product.id)
        } else {
            uiTips.alert("导入失败");
        }
    }

    $scope.loadProductNetValueList = function(pageNum){
        uiTips.loading();
        $http.get('/a/admin/taiji/product/net_value_list/'+pageNum,{params:{productId:$scope.product.id}}).success(function (data) {
            if(data.statusCode == 0) {
                data = data.data
                $scope.netValueList = data.pager.ll;
                $scope.netValueTable = new Table($scope.netValueList)
                $scope.netvaluePager = {pageNum: pageNum, pageSize: data.pager.pageSize, totalCount: data.pager.totalCount};
            }
        });
    }

    $scope.paramsModel = {productId:$scope.product.id};
}