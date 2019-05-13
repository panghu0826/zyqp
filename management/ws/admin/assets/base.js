// localstorage
(function (global) {
    var TIME_SUF = '__SAVED_TIME';

    var Store = function (isSession, refreshInterval) {
        this.storage = isSession ? window.sessionStorage : window.localStorage;
        // default 1 hour
        this.refreshInterval = refreshInterval || 1000 * 60 * 60;
        if (!this.storage)
            console.log('Web local storage api not support!');
    };

    var extend = {
        clear: function () {
            if (!this.storage)
                return;

            this.storage.clear();
        },

        get: function (key) {
            if (!this.storage)
                return;

            var str = this.storage[key];
            if (!str || 'undefined' == str || 'null' == str)
                return null;

            // check if expired
            var timeSaved = this.storage[key + TIME_SUF];
            if (!timeSaved)
                return null;

            if ((new Date().getTime() - parseInt(timeSaved)) > this.refreshInterval) {
                this.storage.removeItem(key);
                this.storage.removeItem(key + TIME_SUF);
                return null;
            }

            return JSON.parse(str);
        },

        set: function (key, val) {
            if (!this.storage)
                return;

            this.storage[key] = JSON.stringify(val);
            this.storage[key + TIME_SUF] = '' + new Date().getTime();
        },

        remove: function (key) {
            if (!this.storage)
                return;

            this.storage.removeItem(key);
            this.storage.removeItem(key + TIME_SUF);
        }
    };

    for (key in extend) {
        Store.prototype[key] = extend[key];
    }

    global.Store = Store;
})(window);

$(function () {
    // render menu by role on server side
    $.get('/a/admin/user/get-login-user', function (data) {
        if (data.needLogin) {
            document.location.href = '/a/m/logout';
            return;
        }

        if (data.menus) {
            var sidebarMenu = Consts.format($('#menu-tpl').html(), {list: data.menus});
            $('#sidebar').html(sidebarMenu);

            // sidebar events
            $('.menu-title').hover(function (e) {
                var m = $(e.target);
                if (!m.is('.menu-title')) {
                    m = m.closest('.menu-title');
                }

                m.addClass('hover');
            }, function () {
                $('.menu-title').removeClass('hover');
            });

            $('.menu-leaf').click(function (e) {
                // key1:1,key2:2
                var paramsStr = $(this).attr('data-params');
                var store = new Store(true);
                if (paramsStr) {
                    var a = {};
                    _.each(paramsStr.split(','), function (it) {
                        var arr = it.split('=');
                        if (arr.length == 2) {
                            a[arr[0]] = arr[1];
                        }
                    });
                    console.log('page params - ' + JSON.stringify(a));
                    store.set('page-params', a);
                } else {
                    store.remove('page-params');
                }
                return true;
            });

            $('.menu-title').click(function (e) {
                e.stopPropagation();
                var m = $(e.target);
                if (!m.is('.menu-title')) {
                    m = m.closest('.menu-title');
                }

                var ul = m.parent().find('ul');
                var isShow = ul.data('isShow');
                if (!isShow) {
                    ul.data('isShow', 1);
                    ul.stop().slideDown('fast');
                } else {
                    ul.data('isShow', 0);
                    ul.stop().slideUp('fast');
                }
            });
        }

        if (data.user) {
            $('.user-name').text(data.user.username);
            $('.user-headimgurl').attr('src', data.user.headimgurl);
        }
    });

    // nav bar events
    $('#link-logout').click(function (e) {
        document.location.href = '/a/m/logout';
    });
    $(".icon-home").click(function(e){
        document.location.href = '#/page/tj_user_passwd-change';
    })
});

(function (win) {
    var Store = win.Store;

    var Page = {};
    win.Page = Page;

    Page.pageLoaded = [];
    Page.renderContent = function (page) {
        $('#content-page').remove();

        $.ajax({
            type: 'get',
            cache: false,
            url: 'pages/' + page + '.html',
            data: '',
            dataType: 'text',
            success: function (content) {
                var htmlInner = '<div id="content-page">' + content + '</div>';
                $('#main').html(htmlInner);

                angular.bootstrap($('#content-page'), ['module_' + page]);
                $.dialog.unloading();
            },

            error: function () {
                $.dialog.unloading();
            }
        });
    };

    Page.open = function (page, params) {
        if (params) {
            var store = new Store(true);
            store.set('page-params', params);
        }

        $.dialog.loading();
        if (this.pageLoaded.contains(page)) {
            this.renderContent(page);
        } else {
            var that = this;
            // debug use suf TODO
            var suf = false ? '' : '?_=' + new Date().getTime();
            $LAB.script('pages/' + page + '.js' + suf).wait(function () {
                that.pageLoaded.push(page);
                that.renderContent(page);
            });
        }
    };

    Page.go = function (hash, params) {
        var store = new Store(true);
        if (params) {
            store.set('page-params', params);
        } else {
            store.remove('page-params');
        }
        document.location.hash = '#' + hash;
    };

    Page.params = function () {
        var store = new Store(true);
        return store.get('page-params') || {};
    };

    Page.goToPrint = function (tpl, data) {
        var store = new Store(true);
        store.set('tpl', tpl);
        store.set('tplData', data);
        window.open('/admin/print-tpl.html');
    };
})(this);

(function () {
    var routes = {
        '/page/:page': function (page) {
            page = page.replace(/\_/g, '/');
            Page.open(page);
        },

        '/page/:page/:params': function (page, paramsStr) {
            var p = {};
            _.each(paramsStr.split(','), function (it) {
                var arr = it.split('_');
                p[arr[0]] = arr[1];
            });
            page = page.replace(/\_/g, '/');
            Page.open(page, p);
        }
    };

    var router = Router(routes);
    router.init();
})();

(function (win) {
    // base module
    var md = angular.module('base', ['ng.ui']);
    md.factory('Page', function () {
        return win.Page;
    });
    md.factory('BitTransfer', function () {
        return {
            // 位转化为2,4,8...的list
            bitToList: function (j) {
                var max = 30;
                var list = [];
                for (var i = 0; i < max; i++) {
                    var it = Math.pow(2, i);
                    if (it > j)
                        break;

                    if ((it & j ) == it) {
                        list.push('' + it);
                    }
                }
                return list;
            }
        };
    });
    md.factory('respInterceptor', ['$q', 'uiTips', function ($q, uiTips) {
        var respInterceptor = {
            responseError: function (response) {
                uiTips.unloading();
                return $q.reject(response);
            },

            response: function (response) {
//                    console.log(response);
                uiTips.unloading();
                if (response.data) {
                    if (response.data.needLogin) {
                        document.location.href = '/a/m/logout';
                    } else if (response.data.error) {
                        uiTips.alert(response.data.error);
                        return $q.reject(response);
                    }
                }
                return response || $q.when(response);
            }
        };
        return respInterceptor;
    }
    ])
    ;
    md.config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.common['X-Requested-With'] = 'ng';
        $httpProvider.interceptors.push('respInterceptor');
    }]);
    md.run(['uiValid', function (uiValid) {
        uiValid.regPat('email', /^(\w)+(\.\w+)*@(\w)+((\.\w+)+)$/, '请录入有效的邮箱地址');
        uiValid.regPat('tel', /^\d{11}$/, '请录入11位手机号码');
        uiValid.regPat('idcard', /^[\dxX]{15,18}$/, '请录入正确的身份证号码');
        uiValid.regPat('mobileAndTel', /^(\(\d{3,4}\)|\d{3,4}-|\s)?\d{7,14}(-\d{1,8})?$/, '请录入11位手机号码或者电话号码如020-1234567');
        uiValid.regPat('fee', /^\d+(\.\d+)?$/, '请输入有效费率,如:1.2');
        uiValid.regPat('number', /^\d+(\.\d+)?$/, '请输入有效费率,如:1.2');
        uiValid.regPat('float', /^\d+(\.\d+)?$/, '请输入有效数字,如:1.2');
        uiValid.regPat('income', /^\d+(\.\d+)?%$/, '请输入有效收益率,如:39%');
        uiValid.regPat('price', /^(([1-9][0-9]*)|(([0]\.\d{1,4}|[1-9][0-9]*\.\d{1,4})))$/, '请输入有效价格,如:26.44,最多四位小数');

    }]);

    md.directive('userRoleLabel', ['$http', 'uiTips', function ($http) {
        return {
            restrict: 'A',
            scope: {
                info: '='
            },

            template: $('#tpl-directive-user-role-label').html(),

            link: function ($scope, el, attrs) {
                $scope.targetIndex = $scope.info.type == 'role' ? 0 : 1;

                var role = $scope.info.role || 0;
                var label = $scope.info.label || 0;
                var getRoleOrLabel = function (role, isSet, isRole) {
                    var targetList = isRole ? $scope.roleList : $scope.labelList;
                    var arr = [];
                    _.each(targetList, function (it) {
                        if ((it.code & role) == it.code) {
                            arr.push(it.name);
                            if (isSet) {
                                it.isChecked = true;
                            }
                        } else {
                            if (isSet) {
                                it.isChecked = false;
                            }
                        }
                    });
                    return arr.join('');
                };

                var store = new Store(true);
                var roleList = store.get('roleList');

                var labelList = store.get('labelList');
                if (!roleList || !labelList) {
                    $http.get('/a/admin/user/get-role-label-list').success(function (data) {
                        if (data.roleList) {
                            $scope.roleList = data.roleList;
                            store.set('roleList', data.roleList);
                        }
                        if (data.labelList) {
                            $scope.labelList = data.labelList;
                            store.set('labelList', data.labelList);
                        }

                        getRoleOrLabel(role, true, true);
                        getRoleOrLabel(label, true, false);
                    });
                } else {
                    $scope.roleList = roleList;
                    $scope.labelList = labelList;
                    getRoleOrLabel(role, true, true);
                    getRoleOrLabel(label, true, false);
                }

                $scope.$watch(function () {
                    var r = {role: 0, label: 0, roleNameList: [], labelNameList: []};

                    _.each($scope.roleList, function (it) {
                        if (it.isChecked) {
                            r.role += it.code;
                            r.roleNameList.push(it.name);
                        }
                    });
                    _.each($scope.labelList, function (it) {
                        if (it.isChecked) {
                            r.label += it.code;
                            r.labelNameList.push(it.name);
                        }
                    });

                    return r;
                }, function (info) {
                    $scope.info.role = info.role;
                    $scope.info.label = info.label;
                    $scope.info.roleNameList = info.roleNameList;
                    $scope.info.labelNameList = info.labelNameList;
                }, true);
            }
        };
    }]);
})(this);

(function (win) {
    var Map = {
        map: null,
        localSearch: null,
        cb: null,
        showZoom: 12
    };
    win.Map = Map;

    Map.hide = function () {
       // $('#map-container').css({top: '-2000px'});
    };

    Map.show = function (lat, lng, zoom, container) {
        zoom = zoom || this.showZoom;
        this.init(container);

        var point = new BMap.Point(lat, lng);
        this.map.centerAndZoom(point, zoom);

        this.showZoom = zoom;

        //$(container).css({top: '50%', 'margin-top': '-20px'});
    };

    Map.search = function (keyword) {
        this.localSearch.search(keyword);
    };

    Map.init = function (container) {
        //if (this.map)
          //  return;

        this.create(container || "map-container");
    };

    Map.test = function(container){
        var map = new BMap.Map(container);    // 创建Map实例
        map.centerAndZoom(new BMap.Point(116.404, 39.915), 11);  // 初始化地图,设置中心点坐标和地图级别
        map.addControl(new BMap.MapTypeControl());   //添加地图类型控件
        map.setCurrentCity("北京");          // 设置地图显示的城市 此项是必须设置的
        map.enableScrollWheelZoom(true);
        $(container).css({top: '50%', 'margin-top': '-20px'});
    }

    Map.create = function(container){
        var map = new BMap.Map(container);          // 创建地图实例
        map.enableScrollWheelZoom();    //启用滚轮放大缩小，默认禁用
        map.enableContinuousZoom();    //启用地图惯性拖拽，默认禁用
        map.addControl(new BMap.NavigationControl());  //添加默认缩放平移控件
        map.addControl(new BMap.OverviewMapControl()); //添加默认缩略地图控件
        map.addControl(new BMap.OverviewMapControl({isOpen: true, anchor: BMAP_ANCHOR_BOTTOM_RIGHT}));   //右下角，打开

        var localSearch = new BMap.LocalSearch(map);
        localSearch.enableAutoViewport(); //允许自动调节窗体大小

        var that = this;
        localSearch.setSearchCompleteCallback(function (searchResult) {
            var poi = searchResult.getPoi(0);
            map.centerAndZoom(poi.point, that.showZoom + 1);

            var marker = new BMap.Marker(poi.point);
            map.addOverlay(marker);

            var content = (poi.city || '') + '<br />' + (poi.title || '') + '<br />' + (poi.address || '');
            var infoWindow = new BMap.InfoWindow("<p style='font-size:13px;'>" + content + "</p>");
            map.openInfoWindow(infoWindow, poi.point);

            if (that.cb)
                that.cb(poi);
        });

        this.map = map;
        this.localSearch = localSearch;
    }
})(this);