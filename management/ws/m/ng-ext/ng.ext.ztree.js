/**
 * Created by Administrator on 2016/10/12.
 */
(function(global){
    var moduleName = 'ng.ext.ztree';
    var md = angular.module(moduleName, []);

    md.directive('extZtree', ['$timeout', function($timeout){
        return {
            restrict: 'A',
            link: function($scope, el, attrs) {
                $timeout(function(){
                    $.fn.zTree.init(el, $scope[attrs.setting]);
                })
            }
        }
    }]);
})(this);