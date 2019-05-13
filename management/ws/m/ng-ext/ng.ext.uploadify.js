(function(global){
	var moduleName = 'ng.ext.uploadify';
	var md = angular.module(moduleName, []);

	md.directive('extUploadify', ['$parse', function($parse){
		return {
			restrict: 'A',

			// begin link ***
			link: function(scope, el, attrs){
				var opts = scope.$eval(attrs.extUploadify) || {};
				console.log('Init uploadify : ');
				console.log(JSON.stringify(opts));

				if(!opts.uploader){
					console.log('Parameter required : uploader!');
					return;
				}
				// get absolute url path
				if(!opts.uploader.startsWith('/'))
					opts.uploader = Consts.getAppPath(opts.uploader);

				// default parameters
				var props = {auto: true, multi: false, width: 80, height: 25,
					buttonText: '选择文件', fileObjName: 'file', debug: false, preventCaching: false};
				props.swf = Consts.getAppPath('ng-ext/uploadify/uploadify.swf');

				// 重新设置formData
				props.onUploadStart = function(file){
					if(opts.paramsModel){
						var formData = $parse(opts.paramsModel)(scope);
						el.uploadify('settings', 'formData', formData);
					}
				};

				props.onUploadError = function(file, errorCode, errorMsg, errorString){
					console.log('Upload error : ' + errorCode);
					console.log(errorMsg);
					console.log(errorString);
				};

				props.onUploadSuccess = function(file, data, response){
					console.log(file);
					console.log(data);
					if(opts.fnSuccess){
						var jsonObj = '';
						if(data){
							try{
								jsonObj = JSON.parse(data);
							}catch(e){
								console.error(e);
							}
						}

						var getter = $parse(opts.fnSuccess);
						var fnTarget = getter(scope);
						if(fnTarget){
							scope.$apply(function(){
								fnTarget(file, jsonObj, response);
							});
						}
					}
				};

				angular.extend(props, opts);

				console.log(JSON.stringify(props));
				el.uploadify(props);
			} // end link
		};
	}]);
})(this);