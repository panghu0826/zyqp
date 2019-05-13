// JavaScript Document
/*
 * wen
 * copyright wen
 * email:yellowwen@126.com
 */
(function($){
	$.fn.wen_plug_award2 = function(options){
		var defaultVal = {
			star:"#gameBtn",		//启动ID
			star_class:"",			//启动class
			styleName:"hold",		//高亮样式
			speed:300,          	//初始速度
			speedQuick:150,   		//加速
			quicks:5,            	//走多少格开始加速
			row:3,					//表格有多少行
			col:3,					//表格有多少列
			endIndex:0,        		//决定在哪一格减速
			endCycle:5,        		//快速转多少圈后减速
			timeout:20000,		//超时
			data_url:"",			//请求数据地址
			errgrid:"2",			//指定超时或出错时停在第几格
			star_fn:"",
			end_fn:""
		};
		var obj = $.extend(defaultVal,options);
		return this.each(function(){
			var $this = $(this),
				gamebox = this,
				arr = GetSide(obj.row, obj.col),  	//初始化数组
				index = 0,           				//当前亮区位置(从哪一格开始)
				prevIndex = 0,        				//前一位置(用于清除高亮样式)
				flag = false,         				//结束转动标志 
				quick = 0,            				//走了多少格
				cycle = 0,							//转了多少圈
				Time,           		 			//定义对象
				old_time,
				new_time,
				cjdata = {"grid":"2","txt":"本次没中奖！感谢您的参与！明天再来!"};			 			//中奖数据
				
			$(obj.star).click(function () {
				var elm = $(this);
				if($(obj.star).hasClass("off")){
					return false; 
				}else{
					$(obj.star).addClass("off");
				}
				if(typeof obj.star_fn == "function"){
					obj.star_fn(elm);
				}
				get_cjdata();
				old_time = new Date().getTime();
				Time = setInterval(Star, obj.speed);
			});
			
			function resetval(){
				var index = 0,           				//当前亮区位置(从哪一格开始)
				prevIndex = 0,        				//前一位置(用于清除高亮样式)
				flag = false,         				//结束转动标志 
				quick = 0,            				//走了多少格
				cycle = 0,						//转了多少圈
				cjdata = {};
				
			}
			
			function Star(num) {
				new_time = new Date().getTime();
				$(gamebox.rows[arr[index][0]].cells[arr[index][1]]).addClass(obj.styleName);
				if (index > 0) {
					prevIndex = index - 1;
				}else {
					prevIndex = arr.length - 1;
				}
				$(gamebox.rows[arr[prevIndex][0]].cells[arr[prevIndex][1]]).removeClass(obj.styleName);
				index++;
				quick++;
				
				
		 		//慢转1圈后停止
				if (flag == true && index == cjdata.grid && cycle == 1) {
					quick = 0;
					clearInterval(Time);
					if(typeof obj.end_fn == "function"){
						obj.end_fn(obj.star,cjdata);
						/*if(cjdata.gamekey > 0){
							$(obj.star).removeClass("off " + obj.star_class);
							resetval();
						}*/
					}
				}
				
				if (index >= arr.length) {
					index = 0;
					cycle++;
				}
		
		
				//跑马灯变速
				if (flag == false) {
					//走多少格开始加速
					if (quick == obj.quicks) {
						clearInterval(Time);
						Time = setInterval(Star, obj.speedQuick);
					}
					//跑指定圈并且数据请求回来后减速
					if (cycle >= obj.endCycle && index == obj.endIndex) {
						clearInterval(Time);
						flag = true;         			//触发结束
						cycle = 0;						//重置圈数，用于慢转1圈后停止
						//cjdata.grid = obj.errgrid;		//在指定格停止
						Time = setInterval(Star, obj.speed);
						return;
					}
					//超时
					if(obj.timeout <= new_time - old_time){
						clearInterval(Time);
						flag = true;         			//触发结束
						cycle = 0;	//重置圈数，用于慢转1圈后停止
						cjdata.grid = obj.errgrid;		//在指定格停止
						Time = setInterval(Star, obj.speed);
					}
				}
				
			}
			function get_cjdata(){
				// (!!(obj.data_url).match(/^http:\/\/aaa\.com|^http:\/\/www\.aaa\.com/))可以在这里检测域名
				if(obj.data_url.indexOf("?") > -1){
					obj.data_url = obj.data_url+"&"+(+new Date());//"&"+(+new Date())防止从缓存取数据
				}else{
					obj.data_url = obj.data_url+"?"+(+new Date());//"?"+(+new Date())防止从缓存取数据
				}
				$.ajax({
					type: 'GET', 
					url: obj.data_url, 
					dataType: 'json', 
					success: function(json){
						var nJson = {"grid":"2","txt":"本次没中奖！感谢您的参与！明天再来!"};
            var str = json.is08 ? "红包将会在24小时内发送给您!":"";
						if(json.amount===1){
								nJson.grid = "3";
								nJson.txt = "恭喜您中了1元红包，感谢您的参与！"+str;
						}else if(json.amount===2){
								nJson.grid = "4";
								nJson.txt = "恭喜您中了2元红包，感谢您的参与！"+str;
						}else if(json.amount===3){
								nJson.grid = "7";
								nJson.txt = "恭喜您中了3元红包，感谢您的参与！"+str;
						}else if(json.amount===4){
								nJson.grid = "8";
								nJson.txt = "恭喜您中了4元红包，感谢您的参与！"+str;
						}else if(json.amount===5){
								nJson.grid = "1";
								nJson.txt = "恭喜您中了5元红包，感谢您的参与！"+str;
						}else{
								nJson.grid = "2";
								nJson.txt = "本次没中奖！感谢您的参与！明天再来!";
						}
						cjdata = nJson;
					}, 
					error: function(xhr, errorType, error){
						var nJson = {"grid":"2","txt":"本次没中奖！感谢您的参与！明天再来!"};
						cjdata = nJson;
					}
				});
			}
		})
	}
	
	//获取数组最外圈传入参数行、列数
	function GetSide(m, n) {
		//初始化数组
		var arr = [];
		for (var i = 0; i < m; i++) {
			arr.push([]);
			for (var j = 0; j < n; j++) {
				arr[i][j] = i * n + j;
			}
		}
		//获取数组最外圈
		var resultArr = [];
		var tempX = 0,
		tempY = 0,
		direction = "Along",
		count = 0;
		while (tempX >= 0 && tempX < n && tempY >= 0 && tempY < m && count < m * n) {
			count++;
			resultArr.push([tempY, tempX]);
			if (direction == "Along") {
				if (tempX == n - 1){
					tempY++;
				}else{
					tempX++;
				}
				if (tempX == n - 1 && tempY == m - 1){
					direction = "Inverse";
				}
			}else {
				if (tempX == 0){
					tempY--;
				}else{
					tempX--;
				}
				if (tempX == 0 && tempY == 0){
					break;
				}
			}
		}
		return resultArr;
	}

})(jQuery);

//中奖名单滚动
(function ($) {
    $.fn.extend({
        Scroll: function (opt, callback) {
            if (!opt) {
                var opt = {};
            }
            var timerID;
            var _this_ul = this.find("ul").eq(0);
            var _this_li = _this_ul.find("li").eq(0);
            var lineH = _this_li.outerHeight(true);//获取容器高度
            var _this_li_l = _this_ul.find("li").length;
            var _this_li_h = lineH*_this_li_l;
            _this_ul.find("li").clone().appendTo(_this_ul);
            var line = opt.line ? parseInt(opt.line, 10) : parseInt(_this_li.height() / lineH, 10);
            var speed = opt.speed ? parseInt(opt.speed, 10) : 10; //滚动速度，数值越大，速度越慢（毫秒）
            var timer = opt.timer ? parseInt(opt.timer, 10) : 10; //滚动的时间间隔（毫秒)
            if (line == 0) {
                line = 1;
            }
            //var upheight = 0 - line * lineH;
            var upheight = 0;

            //滚动函数
            var scrollTop = function () {
                if (!_this_ul.is(":animated")) {
                    _this_ul.animate({
                        top: upheight--
                    }, speed, function () {
                        if(-upheight >= _this_li_h){
                            _this_ul.find("li:lt(_this_li_l)").appendTo(_this_ul);
                            upheight = 0;
                            _this_ul.css({ top: 0 });
                        }
                    });
                }
            }
            $this = $(this);
            $this.hover(function(){
                window.clearInterval(timerID);
            },function(){
                if (timer){
                    timerID = window.setInterval(scrollTop, timer);
                }
            });
            //自动播放
            if (timer){
                timerID = window.setInterval(scrollTop, timer);
            }
        }
    });
})(jQuery);