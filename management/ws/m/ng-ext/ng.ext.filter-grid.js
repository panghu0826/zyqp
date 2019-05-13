(function(global){
	var FormError = function(name){
		this.$name = name;
		this.$valid = true;
		this.$invalid = false;

		this.flags = {};
	};

	FormError.prototype.set = function(key, flag){
		this.flags[key] = flag;
		var values = _.values(this.flags);
		this.$valid = _.every(values, function(it){
			return it;
		});

		this.$invalid = !this.$valid;
	};

	// easy template
	var Template = global.Template;
	var templateTpl = '' + 
'<#macro filterGridTpl data>' + 
'<table id="${data.tblId}" class="table table-bordered table-striped" ng-form="${data.formName}">' + 
'	<thead>' + 
'	<tr class="grid-header">' + 
'		<#list data.columns as column>' + 
'		<#if (column.choose)>' + 
'		<th width="5%">' + 
'			<#if (column.multiple)>' + 
'			<input type="checkbox" class="grid-choose-all" />' + 
'			</#if>' + 
'		</th>' + 
'		<#else>' + 
'		<th <#if (column.isSortable)>class="grid-sorter ng-ui-sort-all"</#if> ' + 
'			data-column="${column.name}" width="${column.width}">' + 
'			${column.label}' + 
'		</th>					' + 
'		</#if>' + 
'		</#list>' + 
'	</tr>' + 
'	</thead>' + 
'' + 
'	<tbody>' + 
'	<#list data.list as one>' + 
'	<tr class="grid-tr <#if (one_index % 2 == 0)>even<#else>odd</#if>" data-index="${one_index}">' + 
'		<#list data.columns as column>' + 
'		<td>' + 
'			<#if (column.buts)>' + 
'				<#list column.buts as but>' + 
'				<button class="grid-but" data-index="${one_index} ${column_index} ${but_index}">${but.label}</button>' + 
'				</#list>' + 
'			<#elseif (column.choose)>' + 
'				<#if (column.multiple)>' + 
'				<input type="checkbox"<#if (one.isChecked)> checked="true"</#if> class="grid-choose-one" name="grid-choose-${data.gridCountNum}-${column_index}" />' + 
'				<#else>' + 
'				<input type="radio"<#if (one.isChecked)> checked="true"</#if> class="grid-choose-one" name="grid-choose-${data.gridCountNum}-${column_index}" />' + 
'				</#if>' + 
'			<#elseif (column.link)>' + 
'				<a href="javascript:void();" class="grid-link" data-index="${one_index} ${column_index} 0">${one[column.name]}</a>' + 
'			<#elseif (column.bind)>' + 
'				<input type="text" class="grid-bind-input grid-bind-input-${column_index}" ' + 
'					data-index="${one_index} ${column_index} 0" ui-valid="${column.valid}" ' + 
'					style="${column.style}" value="${one[column.name]}" />' + 
'			<#elseif (column.options)>' + 
'				<select class="grid-bind-input grid-bind-input-${column_index}" ' + 
'					data-index="${one_index} ${column_index} 0" ui-valid="${column.valid}" ' + 
'					style="${column.style}">' + 
'					<#list column.options as option>' + 
'					<option value="${option.value}" title="${option.title}" <#if (one[column.name] == option.value)>selected</#if>>${option.title}</option>' + 
'					</#list>' + 
'				</select>' + 
'			<#else>' + 
'				${one[column.name]}' + 
'			</#if>' + 
'		</td>' + 
'		</#list>' + 
'	</tr>				' + 
'	</#list>' + 
'	</tbody>' + 
'</table>' + 
'</#macro>';

	var moduleName = 'ng.ext.filter-grid';
	var md = angular.module(moduleName, ['ng.service']);

	md.directive('extFilterGrid', ['$parse', 'safeApply', 'uiTips', 'uiValid', function($parse, safeApply, uiTips, uiValid){
		var cc = 0;
		
		return {
			scope: {
				list: '=', 
				columns: '='
			}, 
			priority: 1000, 
			restrict: 'A',

			// begin link ***
			link: function($scope, el, attrs){
				var gridCountNum = cc++;
				var formName = 'gridForm' + gridCountNum;
				var scopeP = $scope.$parent;
				// you can check valid use $scope.gridForm0.$valid;
				var form = scopeP[formName] = new FormError(formName);

				var opts = $scope.$eval(attrs.extFilterGrid) || {};
				var tblId = opts.tblId || ('tbl' + gridCountNum);
				// event
				if(opts.fn){
					el.delegate('.grid-but,.grid-link', 'click', function(e) {
						e.preventDefault();
						e.stopPropagation();

						var target = $(e.target);
						var arr = target.attr('data-index').split(' ');
						// arrIndex -> [oneIndex, columnIndex, butIndex]
						var arrIndex = [];
						var i = 0;
						for(; i < 3; i++){
							arrIndex.push(arr[i] ? parseInt(arr[i]) : 0);
						}

						var targetFn = $parse(opts.fn)(scopeP);
						if(targetFn){
							safeApply(scopeP, function(){
								var resultFn = targetFn.apply(scopeP, arrIndex);
								if(resultFn){
									var itemIndex = arrIndex[0];
									var itemDom = el.findAll('.grid-tr').filter(function(){
										return $(this).attr('data-index') == itemIndex;
									});
									resultFn(itemDom);
								}
							});
						}
					});
				}

				// sort, less than 100 performance ok
				if(_.any($scope.columns, function(it){
					return it.isSortable;
				})){
					var resetSortedClass = function(element, suf1, suf2, addedSuf3){
						var pre = 'ng-ui-sort-';
						element.removeClass(pre + suf1).removeClass(pre + suf2).addClass(pre + addedSuf3);
					};

					var sortInner = function(val1, val2, isUp){
						if(!_.isString(val1))
							val1 = '' + val1;
						if(!_.isString(val2))
							val2 = '' + val2;

						var result = val1.localeCompare(val2);
						return isUp ? result : 0 - result;
					};

					el.delegate('.grid-sorter', 'click', function(e) {
						var td = $(e.target);
						var isUp = !td.is('.ng-ui-sort-up');
						var sortColumn = td.attr('data-column');

						resetSortedClass(td, 'all', isUp ? 'down' : 'up', isUp ? 'up' : 'down');
						resetSortedClass(td.siblings('td.grid-sorter'), 'up', 'down', 'all');

						// sort model and then generate dom
						var list2compare = [];
						_.each($scope.list, function(it, i){
							list2compare.push({index: i, value: it[sortColumn]});
						});

						list2compare.sort(function(a, b){
							return sortInner(a.value, b.value, isUp);
						});

						var trList = el.findAll('.grid-tr'); 
						var sortedTrList = [];
						_.each(list2compare, function(it){
							sortedTrList.push(trList.filter('[data-index=' + (it.index || '') + ']'));
						});
						el.findAll('table').append(sortedTrList);
					});
				}

				// choose (radio/checkbox) notice that only one column usually the first
				var chooseColumn = _.findWhere($scope.columns, {choose: true});
				if(chooseColumn){
					el.delegate('.grid-choose-one', 'click', function(e) {
						var input = $(e.target);
						var isChecked = input.is(':checked');

						var index = input.closest('.grid-tr').attr('data-index') || '0';
			//			var columnIndex = input.attr('name').split('-')[3];

						safeApply(scopeP, function(){
							if(chooseColumn.multiple){
								$scope.list[index].isChecked = isChecked;
							}else{
								_.each($scope.list, function(one, i){
									one.isChecked = index == i;
								});
							}
						});

						if(chooseColumn.multiple){
							var allInput = el.findAll('.grid-choose-all');
							if(_.every($scope.list, function(one){
								return one.isChecked;
							})){
								allInput.attr('checked', true);
							}else{
								allInput.removeAttr('checked');
							}
						}
					});
					el.delegate('.grid-choose-all', 'click', function(e) {
						var input = $(e.target);
						var isChecked = input.is(':checked');
						var sub = el.findAll('.grid-choose-one');
						if(isChecked)
							sub.attr('checked', true);
						else
							sub.removeAttr('checked');

						safeApply(scopeP, function(){
							_.each($scope.list, function(one){
								one.isChecked = isChecked;
							});
						});
					});
				}

				// bind and valid
				if(_.any($scope.columns, function(it){
					return it.bind || it.options;
				})){
					el.delegate('.grid-bind-input', 'change', function(e) {
						var input = $(e.target);
						var val = input.val().trim();

						var arr = input.attr('data-index').split(' ');
						var index = arr[0] || 0;
						var columnIndex = arr[1] || 0;

						var targetColumn = $scope.columns[columnIndex];
						var one = $scope.list[index];

						safeApply(scopeP, function(){
							var flag = true;
							if(targetColumn.valid){
								var validKey = '' + columnIndex + '-' + index;

								var rules = targetColumn.valid;
								var result = uiValid.check(val, rules, scopeP, null, one);
								flag = result.flag;
								if(!flag){
									uiTips.on(input, result.msg);
									flag = false;
									
									// not apply
									form.set(validKey, false);
								}else{
									uiTips.off(input);

									form.set(validKey, true);
								}
							}

							if(flag){
								one[targetColumn.name] = val;
							}
						}); // apply end
					});
				}

				// filter
				if(opts.filter){
					var columns = opts.filter.split(' ');
					if(columns.contains('*'))
						columns = _.pluck($scope.columns, 'name');
					
					var getFilterIndex = function(val){
						var indexLl = [];
						var reg = new RegExp('^.*' + val + '.*$');
						_.each($scope.list, function(one, index){
							if(_.any(columns, function(column){
									return one[column] && ('' + one[column]).match(reg);
								})){
								indexLl.push(index);
							}
						});
						return indexLl;
					};
					$('<input class="grid-filter" />').keyup(function(e){
						var trs = el.findAll('.grid-tr');
						var val = $(this).val().trim();
						if(!val){
							trs.show();
							return;
						}

						var indexLl = getFilterIndex(val);
						trs.each(function(){
							var index = $(this).attr('data-index') || '0';
							if(indexLl.contains(parseInt(index, 10)))
								$(this).show();
							else
								$(this).hide();
						});
					}).prependTo(el);

					if(opts.filterLabel)
						$(opts.filterLabel).prependTo(el);
				}

				// not true -> equals
				$scope.$watch('list', function(list){
					// clear first
					el.findAll('table').remove();

					var data = {columns: $scope.columns, list: list, 
						tblId: tblId, gridCountNum: gridCountNum, formName: formName};

					var tpl = Consts.format(templateTpl, data);
					el.append(tpl);

					// filter again
					if(opts.filter){
						var filterInput = el.findAll('.grid-filter');
						var filterInputVal = filterInput.val().trim();
						if(filterInputVal)
							filterInput.trigger('keyup');
					}
				});
			} // end link
		}; // end return
	}]); // end directive
})(this);