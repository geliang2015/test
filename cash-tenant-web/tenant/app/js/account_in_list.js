;(function(undefined) {
    var _global;
    function result(args,fn){
        var argsArr = Array.prototype.slice.call(args);
        if(argsArr.length > 0){
            return argsArr.reduce(fn);
        } else {
            return 0;
        }
    }
    var tableConfig = {
    		presentPageNumber:null,
    		columnsInit:function(){
            return [
	            {
	            	title: '序号',
	                field: 'id',
	                align:'center',
	                valign: 'middle',
	                width:'60',
	                formatter: function (value, row, index) {
	                    return index + 1;
	                }
	            },
	            {
	            	title: '时间',
	                field: 'time',
	                valign: 'middle',
	                sortable : true
	            },
	            {
	            	title: '本次充值金额',
	                field: 'money',
	                valign: 'middle',
	                sortable : true
	            },
	            {
	            	title: '操作员',
	                field: 'createUser',
	                valign: 'middle',
	                sortable : true
	            }
	        ]
        },
        gridInit:function(columns){
            return $("#dataTable").bootstrapTable({
            	method: 'post',
            	contentType: "application/x-www-form-urlencoded",
            	dataField: 'data',
                totalField:'total',
                columns: columns,
                height: $(window).height()-95,
                striped: 'true',
                contentType: "application/x-www-form-urlencoded",
                classes: 'table table-hover qxttable',
                sidePagination: 'server',
                queryParamsType: "page",
                queryParams: function queryParams(params) {//设置查询参数
                    var args = {};
                    args.appCode = tableConfig.appCode;
                    var startTime = $("#startTime").val();
                    var endTime = $("#endTime").val();
                    if(startTime!==""){
                    	args.startTime = startTime;
                    }
                    if(endTime!==""){
                    	args.endTime = endTime;
                    }
                    tableConfig.presentPageNumber = params.pageNumber;
                    args.page = params.pageNumber - 1;//params.pageNumber
                    args.pageSize = params.pageSize;//params.pageSize
                    args.isPage = 1;
                    return args;
                },
                onClickRow: function (row, $element, field) {
                    $("#dataTable").bootstrapTable("uncheckAll");
                },
                onLoadSuccess:function(){
                	$(".bs-checkbox").css({'text-align':'center','vertical-align':'middle'});
                },
                onPostBody:function () {
                	var options = $("#dataTable").bootstrapTable("getOptions");
                	if(tableConfig.presentPageNumber){
                		var recordSize = $("#dataTable").bootstrapTable("getData").length;
                		if(recordSize<1 && tableConfig.presentPageNumber>1){
                			options.pageNumber = tableConfig.presentPageNumber-1;
                			tableConfig.refresh();
                		}
                	}
                    $(".opt-dropdown").hover(function(){
                        $(this).find(".opt-dropdown-menu").show();
                    },function(){
                        $(this).find(".opt-dropdown-menu").hide();
                    });
                },
                onClickRow: function (row, $element, field) {
                    $("#dataTable").bootstrapTable("uncheckAll");
                }
            });
        },
        refresh: function (type) {
        	$("#dataTable").bootstrapTable('refresh',{url:$$.restTenantServerUrl + '/rest/BaseAppBusiness/accountInList'+$$.restTenantVersion});
        },
        resetPageNumber:function(){
        	var options = $("#dataTable").bootstrapTable("getOptions");
        	options.pageNumber = 1;
        }
    };

    // 将插件对象暴露给全局对象
    _global = (function(){ return this || (0, eval)('this'); }());
    if (typeof module !== "undefined" && module.exports) {
        module.exports = tableConfig;
    } else if (typeof define === "function" && define.amd) {
        define(function(){return tableConfig;});
    } else {
        !('plugin' in _global) && (_global.tableConfig = tableConfig);
    }
}());

/**
 * 初始化日期
 * @returns
 */
function initDate(){
	var endTime = {
        elem: '#endTime',
        type: 'datetime',
        format:"yyyy-MM-dd HH:mm:ss",
        btns: ['confirm'],
        done: function (value, date, endDate) {
        	
        }
    };
	laydate.render(endTime);
	var startTime = {
        elem: '#startTime',
        type: 'datetime',
        format:"yyyy-MM-dd HH:mm:ss",
        btns: ['confirm'],
        done: function (value, date, endDate) {
        	
        }
    };
	laydate.render(startTime);
}

function setTime(){
	var date = new Date();
	date.setDate(date.getDate()+1);
	$("#endTime").val(format(date,"yyyy-MM-dd")+" 00:00:00");
	date.setMonth(date.getMonth()-1);
	$("#startTime").val(format(date,"yyyy-MM-dd")+" 00:00:00");
}

function resetTime(type){
	var startTime = new Date($("#startTime").val());
	var endTime = new Date($("#endTime").val());
	if(type=="qyr"){
		$("#endTime").val(format(startTime,"yyyy-MM-dd hh:mm:ss"));
		startTime.setDate(startTime.getDate()-1);
		$("#startTime").val(format(startTime,"yyyy-MM-dd hh:mm:ss"));
	}else if(type=="dqr"){
		var date = new Date();
		$("#startTime").val(format(date,"yyyy-MM-dd")+" 00:00:00");
		date.setDate(date.getDate()+1);
		$("#endTime").val(format(date,"yyyy-MM-dd")+" 00:00:00");
	}else if(type=="hyr"){
		$("#startTime").val(format(endTime,"yyyy-MM-dd hh:mm:ss"));
		endTime.setDate(endTime.getDate()+1);
		$("#endTime").val(format(endTime,"yyyy-MM-dd hh:mm:ss"));
	}
}

function initData(appCode){
	
	tableConfig.appCode = appCode;
	
	initDate();
    setTime();
    
    //渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    
    tableConfig.resetPageNumber();
    tableConfig.refresh();
    
    //重置表单
    $("#searchReset").on("click",function(){
    	setTime();
        tableConfig.resetPageNumber();
        tableConfig.refresh();
    });
    
    //按钮搜索
    $("#serachBtn").on("click",function(){
    	tableConfig.resetPageNumber();
        tableConfig.refresh();
    });
    
    //按钮搜索
    $(".btn-query-data").on("click",function(){
    	var type = $(this).attr("type");
    	resetTime(type);
    	tableConfig.resetPageNumber();
        tableConfig.refresh();
    });
}

$(function () {
	
    //动态设置表格高度
	$(window).resize(function () {
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height() - 95
        });
    });
    
});