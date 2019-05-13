/**
 * Created by panjintao on 2016/11/9.
 */
(function (global) {
    var moduleName = 'ng.ext.uploadifive';
    var md = angular.module(moduleName, []);

    md.directive('extUploadifive', ['$parse', function ($parse) {
        return {
            restrict: 'A',

            // begin link ***
            link: function (scope, el, attrs) {
                var opts = scope.$eval(attrs.extUploadifive) || {};
                console.log('Init uploadifive : ');
                console.log(JSON.stringify(opts));

                if (!opts.uploader) {
                    console.log('Parameter required : uploader!');
                    return;
                }
                // get absolute url path
                if (!opts.uploader.startsWith('/'))
                    opts.uploader = Consts.getAppPath(opts.uploader);

                opts.uploadScript = opts.uploader;  //uploadifive 上传

                // default parameters
                var props = {
                    auto: true, multi: false, width: 80, height: 25,
                    buttonText: '选择文件', fileObjName: 'file', debug: false, queueSizeLimit: 1
                    //queueSizeLimit 上传队列文件数
                };

                // 重新设置formData
                props.onUpload = function (file) {
                    if (opts.paramsModel) {
                        var formData = $parse(opts.paramsModel)(scope);

                        if (formData != undefined) {
                            el.settings.formData = formData;   //动态更改formData的值
                            el.uploadifive('upload');
                        }
                    }
                };

                props.onFallback = function () {
                    alert("该浏览器无法使用!");
                };


                props.onError = function (file, errorCode, errorMsg, errorString) {
                    console.log('Upload error : ' + errorCode);
                    console.log(errorMsg);
                    console.log(errorString);
                };

                props.onUploadComplete = function (file, data, response) {
                    console.log(file);
                    console.log(data);
                    if (opts.fnSuccess) {
                        var jsonObj = '';
                        if (data) {
                            try {
                                jsonObj = JSON.parse(data);
                            } catch (e) {
                                console.error(e);
                            }
                        }

                        var getter = $parse(opts.fnSuccess);
                        var fnTarget = getter(scope);
                        if (fnTarget) {
                            scope.$apply(function () {
                                fnTarget(file, jsonObj, response);
                            });
                        }
                    }
                };

                angular.extend(props, opts);

                console.log(JSON.stringify(props));
                el.uploadifive(props);
            } // end link
        };
    }]);
})(this);