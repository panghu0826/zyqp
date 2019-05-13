var md = angular.module('module_mj/paixing/add', ['base']);
md.controller('MainCtrl', function ($scope, $window, $http, $timeout, uiTips, uiValid, uiLog, Page, BitTransfer) {
    var params = Page.params();
    var id = params.id;
    $scope.ctrl = {};
    $scope.one = {player1:{},player2:{},player3:{},player4:{}, cardsDown:[], remainCardlist:[],preSetRemainCard:[]};
    $scope.ctrl.allCards = []

    $scope.initCard = function(card) {
        $scope.ctrl.allCards.push(card);
        $scope.ctrl.allCards.push(card);
        $scope.ctrl.allCards.push(card);
        $scope.ctrl.allCards.push(card);
    }

    for (var i = 1; i <= 9; i++) {
        $scope.initCard({code:i,name:i+"万"})
    }
    for (var i = 1; i <= 9; i++) {
        $scope.initCard({code:16+i,name:i+"条"})
    }
    for (var i = 1; i <= 9; i++) {
        $scope.initCard({code:32+i,name:i+"筒"})
    }
    $scope.initCard({code:69,name:"红中"})

    _.each($scope.ctrl.allCards, function(item){
        $scope.one.remainCardlist.push(item);
    })

    $scope.updateRemainListStr = function() {
        $scope.one.remainCardlistStr = _.map($scope.one.remainCardlist,function(it){return it.name}).join(" ")
    }

    $scope.changeSingleCard = function(model, modelName){
        $scope.ctrl.cards = []
        _.each($scope.one.remainCardlist, function(it){
            $scope.ctrl.cards.push(it);
        })
        if(model[modelName]) $scope.ctrl.cards.push(model[modelName]);
        $scope.ctrl.editModel = model;
        $scope.ctrl.editModelName = modelName;
        $scope.ctrl.showSingleEditDialog = true;
    }

    $scope.changeMultiCard = function(model, modelName){
        var multiCards = model[modelName] || [];
        multiCards = _.map(multiCards, function(item){return item.name})
        $scope.ctrl.multiCards = multiCards.join(" ")
        $scope.ctrl.editModel = model;
        $scope.ctrl.editModelName = modelName;
        $scope.ctrl.showMultiEditDialog = true;
        $scope.ctrl.tmpRemainList = _.map($scope.one.remainCardlist, function(it){return it;});
    }

    $scope.add2MultiTmp = function(tmp) {
        $scope.ctrl.tmpRemainList.remove(tmp);
        $scope.ctrl.multiCards += " " + tmp.name;
    }

    $scope.saveMultiCardChange = function() {
        var arr = $scope.ctrl.multiCards.split(" ");
        var model = $scope.ctrl.editModel;
        var modelName = $scope.ctrl.editModelName
        model[modelName] = model[modelName] || []
        _.each(model[modelName], function(it){
           $scope.one.remainCardlist.push(it);
        });
        model[modelName] = []
        var tmp = _.each(arr, function(name) {
            var card = _.find($scope.one.remainCardlist, function(item){return item.name == name});
            if(card) {
                model[modelName].push({code:card.code,name:card.name});
                $scope.one.remainCardlist.remove(card);
            }
        }
        )
        $scope.ctrl.showMultiEditDialog = false;
        $scope.updateRemainListStr();
    }

    $scope.saveSingleCardChange = function() {
        var singleCard = $scope.ctrl.singleCard
        if(singleCard){
            var card = _.find($scope.ctrl.cards, function(ele){
                return ele.code == parseInt(singleCard)
             });
            $scope.ctrl.cards.remove(card)
            $scope.ctrl.editModel[$scope.ctrl.editModelName] = card;
        }
        $scope.one.remainCardlist = [];
        _.each($scope.ctrl.cards, function(it){
            $scope.one.remainCardlist.push(it);
        })
        $scope.ctrl.showSingleEditDialog = false;
        $scope.updateRemainListStr();
    }

    $scope.save = function(){
        var one = $scope.one;
        $http.post("/a/mj/paixing/save", one).success(function (data) {
            var data = data.data;
            if (data.id) {
                $scope.one.id = data.id;
                uiTips.alert("保存成功");
            } else {
                uiTips.alert(data.msg);
            }
        });
    }

    $scope.load = function(){
        $http.get("/a/mj/paixing/detail/"+id, {}).success(function (data) {
            data = data.data;
            if (data.id) {
                $scope.one = data.data;
                $scope.one.id = data.id;
            } else {
                uiTips.alert(data.msg);
            }
        });
    }

    if(id) {
        $scope.load(id);
    }

    $scope.updateRemainListStr();


});