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
	            	title: '序号',
	                field: 'id',
	                width:40,
	                align:'center',
	                valign:'middle',
	                formatter: function (value, row, index) {
	                    return index + 1;
	                }
	            },
	            {
	            	title: '用户编码',
	                field: 'userCode',
	                valign:'middle'
	            },
	            {
	            	title: '用户名称',
	                field: 'userName',
	                valign:'middle'
	            },
	            {
	            	title: '最新登录时间',
	                field: 'loginTime',
	                align:'center',
	                valign: 'middle',
	                width:120,
	                sortable : true,
	                formatter: function (value, row, index) {
	                	if(value>0){
	                		value = format(new Date(value * 1000),"yyyy-MM-dd HH:mm:ss");
	                	}else{
	                		value = "";
	                	}
	                    return value;
	                }
	            },
	            {
	            	title: '最新登录IP',
	                field: 'loginIp',
	                valign:'middle'
	            },
	            {
	                field: 'remarks',
	                title: '备注',
	                valign:'middle'
	            },
	            {
	            	title: '状态',
	                field: 'status',
	                width: 120,
	                align: 'center',
	                valign:'middle',
	                sortable : true,
	                formatter: function (value, row, index) {
	                    if(value == 1){
	                        return [
	                            '<span class="sys-type-bg pro-status-green">启用</span>'
	                        ];
	                    }
	                    return [
	                        '<span class="sys-type-bg pro-status-red">停用</span>'
	                    ];
	                }
	            },
	            {
	                checkbox: true,
	                valign:'middle'
	            },
	            {
	                field: 'option',
	                title: '操作',
	                width: 80,
	                align: 'center',
	                valign:'middle',
	                formatter: function (value, row, index) {
	                    return [
	                        '<div class="opt-dropdown tb-opt-div"><a class="tb-opt-icon" href="javascript:void(0)">操作 <i class="glyphicon glyphicon-triangle-bottom"></i></a>',
	                        '<ul class="opt-dropdown-menu opt-dropdown-menu-bottom">',
	                        '<li><a href="javascript:void(0)" onclick="updateData(\''+row.id+'\');" title="编辑">',
	                        '<i class="glyphicon glyphicon-pencil"></i><em>编辑</em></a></li>',
	                        '<li><a href="javascript:void(0)" onclick="deleteData(\''+row.id+'\',\''+row.userCode+'\');">',
	                        '<i class="glyphicon glyphicon-trash"></i> 删除</a></li>',
	                        '</ul></div>'
	                    ].join('');
	                }
	            }
            ];
        },
        gridInit:function(columns){
            return $("#dataTable").bootstrapTable({
            	method: "post",
                contentType: "application/x-www-form-urlencoded",
                url: $$.restTenantServerUrl + '/rest/TemplateUserBusiness/list'+$$.restTenantVersion,
                dataField: 'data',
                totalField:'total',
                columns: columns,
                undefinedText: '-',
                striped: 'true',
                classes: 'table table-hover qxttable',
                pagination: 'true',
                height:$(window).height() - 88,
                clickToSelect: true,
                singleSelect: false,
                sidePagination: "server",
                queryParamsType: "page",
                sortSide: 'client',
                sortable: true,
                pageNumber: 1,
                pageSize: 10,
                silentSort: true,
                pageList: [
                    10, 20, 50, 100
                ],
                queryParams: function queryParams(params) {//设置查询参数
                    var args = {};
                    var userCode = $("#userCode").val();
                    var userName = $("#userName").val();
                    if(userCode!==""){
                    	args.c_userCode_7 = userCode;
                    }
                    if(userName!==""){
                    	args.c_userName_7 = userName;
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
                }
            });
        },
        refresh: function () {
            $("#dataTable").bootstrapTable('refresh');
        },
        resetPageNumber:function(){
        	var options = $("#dataTable").bootstrapTable("getOptions");
        	options.pageNumber = 1;
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

/**
 * 新增角色
 */
function addData() {
	openOperateWindow("user_add.html","add","");
}

/**
 * 修改角色
 */
function updateData(id) {
    openOperateWindow("user_add.html","update",id);
}

/**
 * 打开操作窗口
 */
function openOperateWindow(url,type,id){
	var title = "";
	if(type=="add"){
		title = "新建用户";
	}else{
		title = "修改用户";
	}
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: title,
        area: ['800px', '420px'],
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
	layer.confirm('您确定要删除当前数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.tips('正在努力删除数据，请等待...','#del-btn',{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/TemplateUserBusiness/remove",
        	data: {
            	id : id
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
        layer.confirm('确定要删除选中数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
        	var indexLoad = layer.load(2);
			var indexTips = layer.tips('正在努力删除数据，请等待...','#del-btn',{time:30*60*1000});
        	var rowDatas= $("#dataTable").bootstrapTable('getSelections');
        	var rowIds = [];
        	for (var i = 0; i < rowDatas.length; i++) {
            	rowIds.push(rowDatas[i].id);
        	}
        	layer.close(index);
        	$$.ajax({
            	url: "/rest/TemplateUserBusiness/remove",
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

$(function () {
	
    $('#userCode').focus();
    
    //渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    
    //重置表单
    $("#searchReset").on("click",function(){
    	$('#userCode').val('');
        $('#userName').val('');
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