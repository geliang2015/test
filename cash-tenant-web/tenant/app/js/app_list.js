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
	            	title: '租户id',
	                field: 'id',
	                valign:'middle'
	            },
	            {
	            	title: '租户编码',
	                field: 'appCode',
	                valign:'middle'
	            },
	            {
	            	title: '租户名称',
	                field: 'appName',
	                valign:'middle'
	            },
	            {
	            	title: '联系电话',
	                field: 'mobile',
	                valign:'middle'
	            },
	            {
	            	title: '加密密钥',
	                field: 'chiper',
	                valign:'middle'
	            },
	            {
	                field: 'total',
	                title: '累计充值金额(元)',
	                valign:'middle'
	            },
	            {
	                field: 'cash',
	                title: '账户余额(元)',
	                valign:'middle',
                	formatter: function (value, row, index) {
	                    return '<a title="立即充值" style="cursor:pointer;cursor:hand;" href="javascript:void(0)" onclick="recharge(\''+row.appCode+'\',\''+row.appName+'\')"><div>'+value+'</div></a>';
	                }
	            },
	            {
	                field: 'remarks',
	                title: '备注',
	                valign:'middle'
	            },
	            {
	                field: 'rowSort',
	                title: '排序号',
	                valign:'middle',
	                width: 80,
	                sortable : true
	            },
	            {
	            	title: '状态',
	                field: 'status',
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
	                        '<li><a href="javascript:void(0)" onclick="deleteData(\''+row.id+'\');">',
	                        '<i class="glyphicon glyphicon-trash"></i> 删除</a></li>',
	                        '<li><a href="javascript:void(0)" onclick="openAuthSet(\''+row.appCode+'\',\''+row.appName+'\');">',
	                        '<i class="glyphicon glyphicon-cog"></i> 认证</a></li>',
	                        '<li><a href="javascript:void(0)" onclick="accessSet(\''+row.id+'\',\''+row.appCode+'\',\''+row.appName+'\');">',
	                        '<i class="glyphicon glyphicon-cog"></i> 密钥</a></li>',
	                        '<li><a href="javascript:void(0)" onclick="accountInRecord(\''+row.appCode+'\',\''+row.appName+'\');">',
	                        '<i class="glyphicon glyphicon-cog"></i> 充值记录</a></li>',
	                        '<li><a href="javascript:void(0)" onclick="accountOutRecord(\''+row.appCode+'\',\''+row.appName+'\');">',
	                        '<i class="glyphicon glyphicon-cog"></i> 消费记录</a></li>',
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
                url: $$.restTenantServerUrl + '/rest/BaseAppBusiness/list'+$$.restTenantVersion,
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
                    var appCode = $("#appCode").val();
                    var appName = $("#appName").val();
                    if(appCode!==""){
                    	args.c_appCode_7 = appCode;
                    }
                    if(appName!==""){
                    	args.c_appName_7 = appName;
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
 * 充值
 * @param appCode
 * @returns
 */
function recharge(appCode,appName){
	var notSubmit = true;
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "租户充值-"+appName+"("+appCode+")",
        area: ['800px', '300px'],
        content: "app_recharge.html",
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(appCode);
        },
        yes: function (pIndex, layero) {
        	//获取充值金额
        	var obj = openWin.getCash();
        	if(obj.check){
        		layer.confirm('您确定要给租户'+appName+'('+appCode+')充值'+obj.cash+'元吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
            		layer.close(index);
            		if(notSubmit){
                		notSubmit = false;
                		var validFormRes = openWin.submitForm();
                        if(validFormRes){
                        	layer.close(pIndex);
                        	layer.msg("充值成功！",{time:1000});
                        	tableConfig.refresh();
                        }else{
                        	notSubmit = true;
                        }
                	}
                });
        	}
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

/**
 * 新增
 */
function addData() {
	openOperateWindow("app_add.html","add","");
}

/**
 * 修改
 */
function updateData(id) {
    openOperateWindow("app_add.html","update",id);
}

/**
 * 打开操作窗口
 */
function openOperateWindow(url,type,id){
	var title = "";
	if(type=="add"){
		title = "新建租户";
	}else{
		title = "修改租户";
	}
	var notSubmit = true;
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
        	if(notSubmit){
        		notSubmit = false;
        		var validFormRes = openWin.submitForm();
                if(validFormRes){
                	layer.close(index);
                	layer.msg(title+"成功！",{time:1000});
                	tableConfig.refresh();
                }else{
                	notSubmit = true;
                }
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
        	url: "/rest/BaseAppBusiness/remove",
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
            	url: "/rest/BaseAppBusiness/remove",
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

function openAuthSet(appCode,appName){
	var openWin;
	var notSubmit = true;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "认证设置-"+appName+"("+appCode+")",
        area: ['90%', '90%'],
        content: "auth_set.html",
        btn: ['保存','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(appCode);
        },
        yes: function (index, layero) {
        	var selectData = openWin.getSelectData();
        	if(selectData && notSubmit){
        		notSubmit = false;
        		var indexLoad = layer.load(2);
        		var msgIndex = layer.msg('正在努力保存数据，请等待...',{time:30*60*1000});
        		var authCodeArray = [];
            	for(var i=0;i<selectData.length;i++){
            		authCodeArray.push(selectData[i].authCode);
            	}
        		//保存数据
        		$$.ajax({
        	        url:"/rest/BaseAppBusiness/saveAppCodeAuth",
        	        data : {
        	        	appCode: appCode,
        	        	authCode : authCodeArray.join(',')
        	        },
        	        success : function(data){
        	        	layer.close(indexLoad);
        	        	layer.close(msgIndex);
        	        	layer.msg('数据保存成功',{time:2*1000});
        	        	layer.close(index);
        	        },
                	serror : function(){
                		notSubmit = true;
                		layer.close(indexLoad);
                		layer.close(msgIndex);
                		layer.msg('数据保存失败，请稍后重试！',{time:2*1000});
                	},
                	error: function(){
                		notSubmit = true;
                		layer.close(indexLoad);
                		layer.close(msgIndex);
                		layer.msg('数据保存失败，请稍后重试！',{time:2*1000});
                	}
        	    });
        	}
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

function accessSet(appId,appCode,appName){
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "密钥设置-"+appName+"("+appCode+")",
        area: ['90%', '90%'],
        content: "access_set.html",
        btn: ['生成密钥','&nbsp;&nbsp;关&nbsp;&nbsp;&nbsp;&nbsp;闭&nbsp;&nbsp;'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(appId);
        },
        yes: function (index, layero) {
        	openWin.createAccess();
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

function accountOutRecord(appCode,appName){
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "扣费记录-"+appName+"("+appCode+")",
        area: ['90%', '90%'],
        content: "account_out_list.html",
        btn: ['关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(appCode);
        },
        yes: function (index, layero) {
        	layer.close(index);
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

function accountInRecord(appCode,appName){
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "充值记录-"+appName+"("+appCode+")",
        area: ['90%', '90%'],
        content: "account_in_list.html",
        btn: ['关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(appCode);
        },
        yes: function (index, layero) {
        	layer.close(index);
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

$(function () {
	
    $('#appCode').focus();
    
    //渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    
    //重置表单
    $("#searchReset").on("click",function(){
    	$('#appCode').val('');
        $('#appName').val('');
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