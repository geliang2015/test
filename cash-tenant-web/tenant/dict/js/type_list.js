var queryObj = new Object();
;(function (undefined) {
    "use strict"
    var _global;

    function result(args, fn) {
        var argsArr = Array.prototype.slice.call(args);
        if (argsArr.length > 0) {
            return argsArr.reduce(fn);
        } else {
            return 0;
        }
    }

    var tableConfig = {
    	presentPageNumber:null,
    	columnsInit: function () {
            return [
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
                {field: "dictCode", title: "类别编码", valign:"middle",sortable: true},
                {field: "dictName", title: "类别名称",valign:"middle"},
                {field: "remarks", title: "备注",valign:"middle",width:300},
                {field: "rowSort", title: "排序号",valign:"middle",width:80},
                {
                    field: 'status',
                    title: '状态',
                    align:'center',
                    valign:'middle',
                    width:120,
                    formatter: function (value, row, index) {
                        if (value == '1') {
                            return [
                                '<span class="sys-type-bg pro-status-green">启用</span>'
                            ];
                        }
                        return [
                            '<span class="sys-type-bg pro-status-red">禁用</span>'
                        ];
                    }
                },
                {
                    field: 'optionDel',
                    valign:"middle",
                    checkbox: true
                }, 
                {
                    field: 'option',
                    title: '操作',
                    width: 80,
                    align: 'center',
                    valign:"middle",
                    formatter: function (value, row, index) {
                        return [
                            '<div class="opt-dropdown tb-opt-div"><a class="tb-opt-icon" href="javascript:void(0)">操作 <i class="glyphicon glyphicon-triangle-bottom"></i></a>',
                            '<ul class="opt-dropdown-menu opt-dropdown-menu-bottom">',
                            '<li><a href="javascript:updateData(\'' + row.id + '\')">',
                            '<i class="glyphicon glyphicon-pencil"></i><em> 编辑</em></a></li>',
                            '<li><a href="javascript:void(0)" onclick="deleteData(\''+row.id+'\');">',
	                        '<i class="glyphicon glyphicon-trash"></i> 删除</a></li>',
	                        '<li><a href="javascript:showData(\'' + row.id + '\',\'' + row.dictCode + '\')"><i class="glyphicon glyphicon-th-list"></i><em> 数据项</em></a></li>',
	                        '</ul></div>'
                        ].join('');
                    }
                }
            ];
        },
        gridInit: function (columns) {
            return $("#dataTable").bootstrapTable({
                method: "post",
                contentType: "application/x-www-form-urlencoded",
                dataField: 'data',
                totalField:'total',
                columns: columns,
                undefinedText: '-',
                height:$(window).height() - 88,
                striped: 'true',
                classes: 'table table-hover qxttable',
                pagination: 'true',
                clickToSelect: true,
                singleSelect: false,
                sidePagination: "server",
                queryParamsType: "page",
                sortSide: 'client',
                sortable: true,
                pageNumber: 1,
                pageSize: 10,
                idField: 'id',
                silentSort: true,
                pageList: [
                    10, 20, 50, 100
                ],
                queryParams: function queryParams(params) {//设置查询参数
                    var args = {};
	                var dictCode = $("#dictCode").val();
	                var dictName = $("#dictName").val();
	                if(dictCode!==""){
	                	args.c_dictCode_7 = dictCode;
	                }
	                if(dictName!==""){
	                	args.c_dictName_7 = dictName;
	                }
	                tableConfig.presentPageNumber = params.pageNumber;
	                queryObj.dictCode = dictCode;
	                queryObj.dictName = dictName;
	                queryObj.pageNumber = params.pageNumber;
                    args.page = params.pageNumber - 1;
                    args.pageSize = params.pageSize;
                    args.isPage = 1;
                    return args;
                },
                onClickRow: function (row, $element, field) {
                    $("#dataTable").bootstrapTable("uncheckAll");
                },
                onLoadSuccess:function(){
                	$(".bs-checkbox").css({'text-align':'center','vertical-align':'middle'});
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
                	$(".opt-dropdown").hover(function(){
                        $(this).find(".opt-dropdown-menu").show();
                    },function(){
                        $(this).find(".opt-dropdown-menu").hide();
                    });
                }
            });
        },
        refresh: function () {
        	var url = $$.restTenantServerUrl + '/rest/TemplateDictTypeBusiness/list'+$$.restTenantVersion;
            $("#dataTable").bootstrapTable('refresh',{url: url});
        },
        resetPageNumber:function(){
        	var options = $("#dataTable").bootstrapTable("getOptions");
        	options.pageNumber = 1;
        }
    };

    // 将插件对象暴露给全局对象
    _global = (function () {
        return this || (0, eval)('this');
    }());
    if (typeof module !== "undefined" && module.exports) {
        module.exports = tableConfig;
    } else if (typeof define === "function" && define.amd) {
        define(function () {
            return tableConfig;
        });
    } else {
        !('plugin' in _global) && (_global.tableConfig = tableConfig);
    }
}());

/**
 * 新增
 */
function addData() {
	openOperateWindow("type_add.html","add","");
}

/**
 * 修改
 */
function updateData(id) {
    openOperateWindow("type_add.html","update",id);
}

/**
 * 打开操作窗口
 */
function openOperateWindow(url,type,id){
	var title = "";
	if(type=="add"){
		title = "新建类别";
	}else{
		title = "修改类别";
	}
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: title,
        area: ['800px', '350px'],
        content: url,
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(type,id);
        },
        yes: function (index, layero) {
            var validFormRes = openWin.submitForm();
            if(validFormRes){
            	layer.close(index);
            	layer.msg(title+"成功！",{time:1000});
            	tableConfig.refresh();
            }
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

/**
 * 删除数据
 */
function deleteData(id){
	layer.confirm('您确定要删除数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.tips('正在努力删除数据，请等待...','#del-btn',{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/TemplateDictTypeBusiness/remove",
        	data: {
            	id: id
        	},
        	success: function(data) {
        		layer.close(indexLoad);
	        	layer.close(indexTips);
        		tableConfig.refresh();
        	},
        	serror : function(){
        		layer.close(indexLoad);
	        	layer.close(indexTips);
        	},
        	error: function(){
        		layer.close(indexLoad);
	        	layer.close(indexTips);
        	}
    	});
    });
}

/**
 * 批量删除数据
 */
function batchDeleteData(){
	var len = $("#dataTable input:checked").length;
    if (len == 0) {
        layer.msg('请选择数据',{time:1000});
    } else {
        layer.confirm('您确定要删除选中数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
        	var indexLoad = layer.load(2);
			var indexTips = layer.tips('正在努力删除数据，请等待...','#del-btn',{time:30*60*1000});
        	var rowDatas= $("#dataTable").bootstrapTable('getSelections');
        	var rowIds = [];
        	for (var i = 0; i < rowDatas.length; i++) {
            	rowIds.push(rowDatas[i].id);
        	}
        	layer.close(index);
        	$$.ajax({
            	url: "/rest/TemplateDictTypeBusiness/remove",
            	data: {
                	id : rowIds.join(',')
            	},
            	success: function(data) {
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            		tableConfig.refresh();
            	},
            	serror : function(){
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            	},
            	error: function(){
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            	}
        	});
        });
    }
}

function synchronizeData(){
	var len = $("#dataTable input:checked").length;
    if (len == 0) {
        layer.msg('请选择目标数据',{time:1000});
    } else {
        layer.confirm('您确定要同步选中数据(包括类别下的数据项)到所有租户码？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
        	var indexLoad = layer.load(2);
			var indexTips = layer.tips('正在努力同步数据，请等待...','#synchronize-btn',{time:30*60*1000});
        	var rowDatas= $("#dataTable").bootstrapTable('getSelections');
        	var rowIds = [];
        	for (var i = 0; i < rowDatas.length; i++) {
            	rowIds.push(rowDatas[i].id);
        	}
        	layer.close(index);
        	$$.ajax({
            	url: "/rest/TemplateDictTypeBusiness/synchronizeDictData",
            	data: {
                	id : rowIds.join(',')
            	},
            	success: function(data) {
            		layer.close(indexLoad);
		        	layer.close(indexTips);
		        	layer.tips('同步数据成功！','#synchronize-btn',{time:2000});
            	},
            	serror : function(){
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            	},
            	error: function(){
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            	}
        	});
        });
    }
}

function showData(id, dictCode) {
	var queryDictCode = queryObj.dictCode;
	var queryDictName = queryObj.dictName;
	var pageNumber = queryObj.pageNumber;
	var params = "dictId="+id+"&dictCode="+dictCode;
    if(queryDictCode!==""){
   	 	params += "&queryDictCode="+queryDictCode;
    }
    if(queryDictName!==""){
   	 	params += "&queryDictName="+queryDictName;
    }
    if(pageNumber!==""){
		params += "&pageNumber="+pageNumber;
	}
    $$.goUrl("/tenant/dict/item_list.html?"+encodeURI(encodeURI(params)));
}

$(function () {
	
	var pageParams = $$.getQueryString();
	if(pageParams.queryDictCode){
		$("#dictCode").val(decodeURI(decodeURI(pageParams.queryDictCode)));
	}
	if(pageParams.queryDictName){
		$("#dictName").val(decodeURI(decodeURI(pageParams.queryDictName)));
	}
	
    $('#dictCode').focus();
    
    //渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    
    if(pageParams.pageNumber){
    	var options = $("#dataTable").bootstrapTable("getOptions");
    	options.pageNumber = parseInt(pageParams.pageNumber);
	}
    
    tableConfig.refresh();
    
    //重置表单
    $("#searchReset").on("click",function(){
        $('#dictCode').val('');
        $('#dictName').val('');
        tableConfig.resetPageNumber();
        tableConfig.refresh();
    });
    
    //按钮搜索
    $("#serachBtn").on("click",function(){
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
    
    //同步数据
    $("#synchronize-btn").click(function(){
    	synchronizeData();
    });
    
    //新建
    $("#add-btn").click(function(){
    	addData();
    });
    
    //批量删除
    $("#del-btn").click(function(){
		batchDeleteData();
    });
    
    //动态设置表格高度
	$(window).resize(function () {
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height() - 88
        });
    });
    
});