(function (global) {
    var ua = navigator.userAgent;
    var isWx = ua.indexOf('MicroMessenger') != -1;

    // refer http://mp.weixin.qq.com/wiki/7/aaa137b55fb2e0456bf8dd9148dd613f.html
    if (!isWx) {
        var wx = {
            config: function (prop) {
                this.prop = prop;
            },

            error: function (fn) {

            },

            ready: function (fn) {
                if (window.$)
                    $(fn);
                else
                    fn();
            },

            closeWindow: function () {
                window.close();
            },

            scanQRCode: function (prop) {
                prop.success({resultStr: 'xxx'});
            },

            chooseWXPay: function (prop) {
                console.log(prop);
                prop.success({});
            },

            chooseImage: function (prop) {
                prop.success({localIds: ['/comm/images/test-crop-mtv.jpg', '/comm/images/test-crop.jpg']});
            },

            uploadImage: function (prop) {
                prop.success({serverId: 'nv26I701LmGxvoxkQk8az6qoRJ8tI5P4kprBbra9XmmhXoFocapNxyL39-aD2x2G'});
            },

            downloadImage: function (prop) {
                prop.success({localId: '/wx/ind/img/userheader.png'});
            },

            getLocation: function (prop) {
                prop.success({latitude: 90, longitude: 90});
            },

            onMenuShareTimeline: function (prop) {
                this.shareTimelineInfo = prop;
                console.log(prop);
            },

            onMenuShareAppMessage: function (prop) {
                this.shareAppInfo = prop;
                console.log(prop);
            },

            triggerShareTimeline: function () {
                if (this.shareTimelineInfo && this.shareTimelineInfo.success) {
                    this.shareTimelineInfo.success();
                }
            },

            triggerShareApp: function () {
                if (this.shareAppInfo && this.shareAppInfo.success) {
                    this.shareAppInfo.success();
                }
            },

            previewImage: function (prop) {
                console.log(prop);
                window.open(prop.current);
            },

            addCard: function (prop) {
                var cardList = prop.cardList || [];
                prop.success({cardList: cardList});
            },

            showMenuItems: function (prop) {
                console.log(prop);
            },

            hideMenuItems: function (prop) {
                console.log(prop);
            },

            getLatestAddress: function (prop) {
                console.log(prop);
                if (prop.success)
                    prop.success({});
            },

            editAddress: function (prop) {
                console.log(prop);
                if (prop.success)
                    prop.success({
                        err_msg: 'edit_address:ok', userName: 'kerry', telNumber: '18620304494',
                        proviceFirstStageName: '广东省', addressCitySecondStageName: '深圳市',
                        addressCountiesThirdStageName: '龙华新区', addressDetailInfo: 'XX', addressPostalCode: '580000'
                    });
            },

            dump: ''
        };

        global.wx = wx;

        var WeixinJSBridge = {
            invoke: function (method, props, fn) {
                console.log(method);
                console.log(props);

                var fnRaw = wx[method];
                if (!fnRaw)
                    return;

                props.success = fn;
                fnRaw(props);
            }
        };

        global.WeixinJSBridge = WeixinJSBridge;
    }


})(this);
