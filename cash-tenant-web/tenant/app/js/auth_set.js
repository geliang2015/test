;(function(undefined) {
    "use strict"
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
        columnsInit:function(){
            return  [
	            {
	                field: 'optionDel',
	                valign:"middle",
	                checkbox: true
	            },
            	{
            		field: 'status',
                    title: '序号',
                    align:"center",
                    valign:"middle",
                    width:40,
                    formatter: function (value, row, index) {
                        return index + 1;
                    }
                },
                {
                	field:"authCode",
                	title:"编码",
                	valign:"middle",
                	sortable: true
                },
                {
                	field:"authTitle",
                	title:"标题",
                	valign:"middle",
                	sortable:true
                },
                {
                	field:"authBody",
                	title:"描述",
                	valign:"middle"
                },
                {
                	field:"requires",
                	title:"是否必填",
                	valign:"middle",
                	formatter: function (value, row, index) {
                		if(value==1){
                			value = "是";
                		}else{
                			value = "否";
                		}
                        return value;
                    }
                },
                {
                	field: "rowSort", 
                	title: "排序号",
                	valign:"middle",
                	width:80
                },
	            {
	                field: 'status',
	                title: '状态',
	                align:'center',
	                valign:"middle",
	                width:120,
	                formatter: function (value, row, index) {
	                    if(value == '1'){
	                        return [
	                            '<span class="sys-type-bg pro-status-green">启用</span>'
	                        ];
	                    }
	                    return [
	                        '<span class="sys-type-bg pro-status-red">停用</span>'
	                    ];
	                }
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
                height: $(window).height()-2,
                striped: 'true',
                classes: 'table table-hover qxttable',
                sidePagination: 'server',
                queryParamsType: "page",
                queryParams: function queryParams(params) {//设置查询参数
                	var args = {};
                    tableConfig.presentPageNumber = params.pageNumber;
                    args.page = params.pageNumber - 1;//params.pageNumber
                    args.pageSize = params.pageSize;//params.pageSize
                    args.isPage = 1;
                    var paramStr = JSON.stringify(args);
                    var param = {
                        params: paramStr
                    };
                    return param;
                },
                onLoadSuccess:function(){
                	$(".bs-checkbox").css({'text-align':'center','vertical-align':'middle'});
                	//获取用户已经拥有的认证项
                	$$.ajax({
            	        url:"/rest/BaseAppBusiness/listAuthCodeByAppCode",
            	        async:false,
            	        data : {
            	            appCode : appCode
            	        },
            	        success : function(data){
            	            if(data && data.length>0){
            	            	var authCodeArray = [];
            	            	for(var i=0;i<data.length;i++){
            	            		authCodeArray.push(data[i].authCode);
            	            	}
            	            	$("#dataTable").bootstrapTable("checkBy", {field:"authCode", values:authCodeArray});
            	            }
            	        }
            	    });
                }
            });
        },
        refresh: function () {
        	$("#dataTable").bootstrapTable('refresh',{url:$$.restTenantServerUrl + '/rest/TemplateAuthBusiness/listAll'+$$.restTenantVersion,});
        }
    }
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

function getSelectData(){
	var rows= $("#dataTable").bootstrapTable('getSelections');
    return rows;
}

var appCode;
function initData(appCodeTemp){
	appCode = appCodeTemp;
	//渲染选择数据表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    tableConfig.refresh();
}

$(function () {
	//动态设置表格高度
	$(window).resize(function () {
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height()-2
        });
    });
});

