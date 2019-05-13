$(function() {

    //图表配置
    var setOption = function(opts) {
        var product = opts;
        var chartsListLen = config.chartsList[0].list.length;

        var chartsOption = {
            animation: false,
            backgroundColor : '#fff',
            grid: {
                containLabel: true,
                top: '15%',
                left: 0,
                right: 0,
                bottom: 0
            },
            tooltip: {
                show: false,
                showContent: false,
                formatter: '数据日期：{b}<br><span style="color:#ff8989;">累计收益</span>：{c0}%<br><span style="color:#9dcdff;">沪深300收益</span>：{c1}%',
                trigger: 'axis',
                textStyle: {
                    fontSize:12
                },
                backgroundColor: 'rgba(50,50,50,0.6)',
                axisPointer: {
                    lineStyle: {
                        color:'#999'
                    }
                }
            },
            legend: {
                show: true,
                selectedMode: false,
                x: 'left',
                y: 'top',
                itemWidth: 10,
                itemHeight: 10,
                itemGap: 30,
                data: [
                    {
                        name: product.name,
                        icon: 'image://data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeAQMAAAAB/jzhAAAAA1BMVEXmQ0M+lJ0kAAAAC0lEQVQI12MYlAAAAJYAATSJejMAAAAASUVORK5CYII=',
                        textStyle: {
                            color: '#555',
                            width: 10,
                            height: 10
                        }
                    },
                    {
                        name: product.nameHS,
                        icon: 'image://data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeAQMAAAAB/jzhAAAAA1BMVEVDoeYr08SmAAAAC0lEQVQI12MYlAAAAJYAATSJejMAAAAASUVORK5CYII=',
                        textStyle: {
                            color: '#555',
                        }
                    }
                ],
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: product.dateList,
                axisLabel: {
                    //margin: 10,
                    textStyle :{
                        align: 'left',
                        fontSize: 8,
                        color: '#666'
                    },
                    interval: 0,
                    formatter: function(value, ind) {
                        var len = chartsListLen;
                        if(ind === 0) {
                            return value;
                        }
                        if(ind == parseInt(len/2) && (len % 2 !== 0)) {
                            return value;
                        }
                        if(ind == len-1) {
                            return value;
                        }
                    }
                },
                axisTick: {
                    show: false,
                },
                axisLine: {
                    lineStyle: {
                        width: 0.5,
                        color: '#ccc',
                    }
                },
                splitLine: {
                    show: false,
                }
            },
            yAxis: {
                type: 'value',
                position: 'right',
                boundaryGap: false,
                axisLabel: {
                    inside: false,
                    //formatter: '{value}%',
                    formatter: function(value) {
                        return value + '%\n';
                        //return val + '%\n\n';
                    },
                    textStyle: {
                        fontSize: 8
                    }
                },
                axisTick: {
                    length:0
                },
                axisLine: {
                    show: false,
                    lineStyle:{
                        width:1,color:'#666'
                    }
                },
                splitLine: {
                    show: true
                },
                splitNumber: 2
            },
            series: [
                {
                    smooth: true,
                    name: product.name,
                    type: 'line',
                    lineStyle:{normal:{color:'#E64343',width:2}},
                    data: product.dataList,
                    symbol: 'none'
                },
                {
                    smooth: true,
                    name: product.nameHS,
                    type: 'line',
                    lineStyle:{normal:{color:'#43A1E6',width:2}},
                    data: product.dataListHS,
                    symbol: 'none'
                }
            ]
        };
        return chartsOption;
    };

    //处理数据
    var renderData = function() {
        var dateList = [];
        var timeList = [];
        var dataList = [];
        var dataListHS = [];
        var chartsListLen = config.chartsList[0].list.length
        dateList = [0];
        dataList = [0];
        dataListHS = [0];
        for(var i = 0 ; i < chartsListLen; i++) {
            dateList[i] = {
                value: config.chartsList[0].list[i].date,
                textStyle: {
                    align: i == 0 ? 'left' : (i == chartsListLen - 1) ? 'right' : 'center'
                }
            };
            timeList[i] = config.chartsList[0].list[i].time;
            dataList[i+1] = config.chartsList[0].list[i].percent;
            dataListHS[i+1] = config.chartsList[1].list[i].percent;
        }

        var product = {
            name: config.chartsList[0].name + ': ' + dataList.slice(-1) + '%',
            nameHS: '沪深300' + ': ' + dataListHS.slice(-1) + '%',
            symbol: config.chartsList[0].symbol,
            dateList: dateList,
            timeList: timeList,
            dataList: dataList,
            dataListHS: dataListHS
        };

        var myChart = echarts.init(document.getElementById('charts'));
        var option = setOption(product);
        myChart.setOption(option);
        config.chartsLoading = false;;
        $('.chartsLoading').hide();
    };

    //切换
    var switchGroupTab = function() {
        $('.chartsNav a').eq(config.chartsNavType-1).addClass('cur');
        $('.chartsNav').on('click', 'a', function(e) {
            var $target = $(e.target);
            var index = $target.index();

            if(config.chartsLoading) return false;
            $target.addClass('cur').siblings('a').removeClass('cur');
            config.chartsNavType = index + 1;

            if(config.chartsListObj[config.chartsNavType]) {
                config.chartsList = config.chartsListObj[config.chartsNavType];
                renderData();
            } else {
                ajax();
            }
        });
    };


    //请求数据
    var ajax = function() {
        if(config.chartsLoading) return false;
        config.chartsLoading = true;
        $('.chartsLoading').show();

        $.ajax({
            type: 'GET',
            url: config.url,
            data: {
                id: config.id,
                type: config.chartsNavType //1:近3个月 2:近一年 3:全部
            },
            dataType: 'json',
            success: function(data) {
                if(data.statusCode) return false;
                config.chartsListObj[config.chartsNavType] = data.data;
                config.chartsList = data.data;
                renderData();
            },
            error: function() {
                config.chartsLoading = false;
            }
        })
    };

    //
    var init = function() {
        if(config.chartsListObj[config.chartsNavType]) {
            renderData();
        } else {
            ajax();
        }
        switchGroupTab();
    };

    init();
});