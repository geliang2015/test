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
    		presentPageNumber:null,
    		columnsInit:function(){
            return  [
            	{
	                field: 'radio',
	                radio: true,
	                align: 'center',
	                valign: 'middle'
	            },
                {
                    title: '租户编码',
                    field: 'appCode',
                    sortable : true

                },
                {
                    title: '租户名称',
                    field: 'appName',
                    sortable : true
                },
                {
	                field: 'rowSort',
	                title: '排序号'
	            },
                {
                    field: 'status',
                    title: '状态',
                    formatter: function (value, row, index) {
                        if(value == '1'){
                            return [
                                '<span class="sys-type-bg pro-status-green">正常</span>'
                            ];
                        }
                        return [
                            '<span class="sys-type-bg pro-status-red">停用</span>'
                        ];
                    }
                }]
        },
        gridInit:function(columns){
            return $("#dataTable").bootstrapTable({
                method:"post",
                contentType:"application/x-www-form-urlencoded",
                dataField: 'data',
                totalField:'total',
                columns: columns,
                striped: 'true',
                classes: 'table table-hover qxttable',
                pagination: 'true',
                height:$(window).height()-15,
                clickToSelect: true,
                singleSelect:false,
                sidePagination: "server",
                queryParamsType : "page",
                sortSide:'client',
                sortable:true,
                pageNumber: 1,
                pageSize:10,
                idField:'id',
                silentSort: true,
                pageList: [
                	10, 20, 50, 100
                ],
                queryParams: function queryParams(params) {//设置查询参数
                    var args={};
                    var appCode = $("#appCode").val();
                    if(appCode!==""){
                        args.c_appCode_7 = appCode;
                    }
                    tableConfig.presentPageNumber = params.pageNumber;
                    args.page=params.pageNumber-1;
                    args.pageSize=params.pageSize;
                    args.isPage=1;
                    return args;
                },
                onClickRow:function(row, $element,field){
                    $("#dataTable").bootstrapTable("uncheckAll");
                },
                onPostBody: function () {
                	var options = $("#dataTable").bootstrapTable("getOptions");
                	if(tableConfig.presentPageNumber){
                		var recordSize = $("#dataTable").bootstrapTable("getData").length;
                		if(recordSize<1 && tableConfig.presentPageNumber>1){
                			options.pageNumber = tableConfig.presentPageNumber-1;
                			tableConfig.refresh();
                		}
                	}
                }
            });
        },
        refresh: function () {
        	var url = $$.restTenantServerUrl + '/rest/BaseAppBusiness/list'+$$.restTenantVersion;
            $("#dataTable").bootstrapTable('refresh',{url:url});
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

function initAppCode(){
	//渲染选择数据表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    tableConfig.refresh();
    $("#serachBtn").click(function(){
    	tableConfig.resetPageNumber();
        tableConfig.refresh();
    });
    
    //回车搜索
    $('.entrer-query').bind('keypress',function(e){
        if(e.keyCode == 13){
        	tableConfig.resetPageNumber();
            tableConfig.refresh();
        }
    });
    
    $(window).resize(function () {
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height()-15
        });
    });
    
}

/**
 * 获取选择数据
 * @returns
 */
function getSelectData(){
    var len = $("#dataTable input:checked").length;
    if (len == 0) {
        //未选中数据提示框
        layer.msg('请选择数据', {time:1000});
        return false;
    } else {
        var rows= $("#dataTable").bootstrapTable('getSelections');
        var rowIds = [];
        var ret={};
        return rows[0];
    }
}


