(function(global){
	var Template = global.Template;

	var templateTpl = '' + 
'<ul class="ng-ui-table-tree">' + 
'	<#if (data.opts.choose)>' + 
'	<li class="ext-tree-choose-header">' + 
'		<span class="leaf-control expand collapse level0"></span>' + 
'		<input type="checkbox" class="ext-tree-choose-all" />' + 
'	</li>' + 
'	</#if>' + 
'<#list data.list as one>' + 
'	<li class="ext-tree-choose-item <#if (one.level != 1)>ng-hide</#if>" data-index="${one_index}" data-level="${one.level}">' + 
'		<span class="leaf-control expand level${one.level} <#if (one.leaf)>leaf</#if>"></span>' + 
'		<#if (data.opts.choose)>' + 
'		<input type="checkbox"<#if (one.isChecked)> checked="true"</#if> class="ext-tree-choose-one" />' + 
'		</#if>' + 
'		<#if (data.opts.fn)>' + 
'		<a href="javascript:void();" class="ext-tree-link">${one.label}</a>' + 
'		<#else>' + 
'		<label>${one.label}</label>' + 
'		</#if>' + 
'	</li>' + 
'</#list>' + 
'</ul>';

	var templateTplTable = '' + 
'<table class="table table-bordered table-striped ng-ui-table-tree">' + 
'	<thead class="ext-tree-choose-header">' + 
'	<tr>' + 
'		<th>' + 
'		<#if (data.opts.choose)>' + 
'		<span class="leaf-control expand collapse level0"></span>' + 
'		<input type="checkbox" class="ext-tree-choose-all" />' + 
'		</#if>' + 
'		</th>' + 
'		<#list data.columns as column>' + 
'		<th<#if (column.width)> width="${column.width}"</#if>>' + 
'		${column.label}' + 
'		</th>' + 
'		</#list>' + 
'	</tr>' + 
'	</thead>' + 
'' + 
'	<tbody>' + 
'	<#list data.list as one>' + 
'	<tr class="ext-tree-choose-item" data-index="${one_index}" data-level="${one.level}">' + 
'		<td>' + 
'		<span class="leaf-control expand collapse level${one.level} <#if (one.leaf)>leaf</#if>"></span>' + 
'		<#if (data.opts.choose)>' + 
'		<input type="checkbox"<#if (one.isChecked)> checked="true"</#if> class="ext-tree-choose-one" />' + 
'		</#if>' + 
'		<#if (data.opts.fn)>' + 
'		<a href="javascript:void();" class="ext-tree-link">${one.label}</a>' + 
'		<#else>' + 
'		<label>${one.label}</label>' + 
'		</#if>' + 
'		</td>' + 
'' + 
'		<#list data.columns as column>' + 
'		<td>' + 
'		<#if (column.buttons)>' + 
'			<#list column.buttons as but>' + 
'				<button class="${but.className} ext-but" data-index="${but_index}">${but.label}</button>' + 
'			</#list>' + 			
'		<#else>' + 
'		${one[column.name]}' + 
'		</#if>' + 
'		</td>' + 
'		</#list>' + 
'	</tr>' + 
'	</#list>' + 
'	</tbody>' + 
'</table>';

	var moduleName = 'ng.ext.tree';
	var md = angular.module(moduleName, []);

	// *** *** *** *** *** *** *** *** *** ***
	// *** *** *** *** *** *** *** *** *** ***
	md.directive('extTree', ['$parse', function($parse){
		return {
			scope: {
				list: '='
			}, 

			restrict: 'A',

			// begin link ***
			link: function($scope, el, attrs){
				var scopeP = $scope.$parent;

				var opts = $scope.$eval(attrs.extTree) || {};
				// event
				if(opts.fn){
					el.delegate('.ext-tree-link,.ext-but', 'click', function(e) {
						e.preventDefault();
						e.stopPropagation();

						var target = $(e.target);
						var index = target.closest('li,tr').attr('data-index') || '0';
						var butIndex = target.is('.ext-but') ? (target.attr('data-index') || '0') : null;
						var one = $scope.list[index];
//						if(!one.leaf){
//							target.prev('input').trigger('click');
//							return;
//						}

						el.findAll('.link-choosed').removeClass('link-choosed');
						target.addClass('link-choosed');

						var targetFn = $parse(opts.fn)(scopeP);
						if(targetFn){
							scopeP.$apply(function(){
								targetFn.call(scopeP, index, butIndex);
							});
						}
					});
				}

				if(opts.choose){
					el.delegate('.ext-tree-choose-one', 'click', function(e) {
						var input = $(e.target);
						var isChecked = input.is(':checked');

						var index = input.closest('li,tr').attr('data-index') || '0';;
						scopeP.$apply(function(){
							$scope.list[index].isChecked = isChecked;
						});

						var allInput = el.findAll('.ext-tree-choose-all');
						if(_.every($scope.list, function(one){
							return one.isChecked;
						})){
							allInput.attr('checked', true);
						}else{
							allInput.removeAttr('checked');
						}
					});

					el.delegate('.ext-tree-choose-all', 'click', function(e) {
						var input = $(e.target);
						var isChecked = input.is(':checked');
						var sub = el.findAll('.ext-tree-choose-one');
						if(isChecked)
							sub.attr('checked', true);
						else
							sub.removeAttr('checked');

						scopeP.$apply(function(){
							_.each($scope.list, function(one){
								one.isChecked = isChecked;
							});
						});
					});
				}

				var getLevel = function(item){
					return parseInt(item.attr('data-level') || '1');
				};

				// expand collapse
				el.delegate('.leaf-control', 'click', function(e) {
					var span = $(e.target);

					var item = span.closest('li,tr');
					var allLl = el.findAll('.ext-tree-choose-item');

					var level = getLevel(item);
					var thisIndex = allLl.index(item);

					var nextSameLevelLl = allLl.filter(':gt(' + thisIndex + ')').filter(function(){
						return getLevel($(this)) <= level;
					});

					var childrenLl;
					if(nextSameLevelLl.length){
						var nextSameLevelItem = nextSameLevelLl.eq(0);
						var nextIndex = allLl.index(nextSameLevelItem);
						childrenLl = allLl.slice(thisIndex + 1, nextIndex);
					}else{
						childrenLl = allLl.slice(thisIndex + 1);
					}

					if(childrenLl && childrenLl.length){
						var isCollapse = span.is('.collapse');
						if(isCollapse){
							span.removeClass('collapse');
							childrenLl.addClass('hide-level' + level);
						}else{
							span.addClass('collapse');
							childrenLl.removeClass('hide-level' + level);

							// only show next level children
							childrenLl.filter(function(){
								var levelInner = getLevel($(this));
								return levelInner === level + 1;
							}).removeClass('ng-hide');
						}
					}
				});

				var filter = function(list){
					_.each(list, function(one, i){
						if(i == list.length - 1){
							one.leaf = true;
							return;
						}

						var next = list[i + 1];
						if(next.level <= one.level)
							one.leaf = true;
					});
				};

				// not true -> equals
				$scope.$watch('list', function(list){
					// clear first
					el.findAll('ul').remove();
					el.findAll('table').remove();

					filter(list);
					var data = {list: list, opts: opts};
					var targetTpl = templateTpl;
					if(opts.columns){
						var columns = $parse(opts.columns)(scopeP);
						if(columns){
							data.columns = columns;
							targetTpl = templateTplTable;
						}
					}
					var tpl = Consts.format(targetTpl, data);
					el.append(tpl);
				});
			} // end link
		}; // end return
	}]); // end directive
})(this);
