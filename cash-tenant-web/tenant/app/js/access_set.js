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
    	appId: '',
    	columnsInit:function(){
            return  [
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
                	field:"accessKey",
                	title:"accessKey",
                	valign:"middle"
                },
                {
                	field:"accessPublic",
                	title:"accessPublic",
                	valign:"middle"
                },
                {
                	field:"accessSecret",
                	title:"accessSecret",
                	valign:"middle"
                },
                {
                	field:"remarks",
                	title:"备注",
                	valign:"middle",
                	width:120,
                	formatter: function (value, row, index) {
                		if(value===""){
                			value = "设置备注";
                		}
                		return '<a href="javascript:void(0);" title="设置备注" onclick="setRemarks(\''+row.id+'\')">'+value+'</a>';
                    }
                },
                {
                	title:"创建时间",
                	field:"createTime",
                	align:'center',
	                valign: 'middle',
	                width:150,
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
	                field: 'status',
	                title: '状态',
	                align:'center',
	                valign:"middle",
	                sortable: true,
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
	            },
	            {
	                field: 'optionEdit',
	                title: '操作',
	                width: 120,
	                align: 'center',
	                valign:'middle',
	                formatter: function (value, row, index) {
	                	if(row.status==1){
	                		return [
		                        '<a id="'+row.id+'" class="opt-icon" href="javascript:void(0)" title="禁用" onclick="updateStatus(\''+row.id+'\',\'2\')">',
		                        '<i class="glyphicon glyphicon-ban-circle">禁用</i></a>',
		                        ' <a id="'+row.id+'001" class="opt-icon" href="javascript:void(0)" title="删除" onclick="deleteData(\''+row.id+'\')">',
		                        '<i class="glyphicon glyphicon-remove">删除</i></a>'
		                    ].join('');
	                	}else{
	                		return [
		                        '<a id="'+row.id+'" class="opt-icon" href="javascript:void(0)" title="启用" onclick="updateStatus(\''+row.id+'\',\'1\')">',
		                        '<i class="glyphicon glyphicon-ok">启用</i></div>',
		                        ' <a id="'+row.id+'001" class="opt-icon" href="javascript:void(0)" title="删除" onclick="deleteData(\''+row.id+'\')">',
		                        '<i class="glyphicon glyphicon-remove">删除</i></a>'
		                    ].join('');
	                	}
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
                	args.appId = tableConfig.appId;
                    tableConfig.presentPageNumber = params.pageNumber;
                    args.page = params.pageNumber - 1;//params.pageNumber
                    args.pageSize = params.pageSize;//params.pageSize
                    args.isPage = 1;
                    return args;
                },
                onLoadSuccess:function(){
                	$(".bs-checkbox").css({'text-align':'center','vertical-align':'middle'});
                }
            });
        },
        refresh: function () {
        	$("#dataTable").bootstrapTable('refresh',{url:$$.restTenantServerUrl + '/rest/BaseAppCipherBusiness/list'+$$.restTenantVersion,});
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


function updateStatus(id,status){
	var tip = "";
	if(status==1){
		tip = "启用";
	}else{
		tip = "禁用";
	}
	layer.confirm('您确定要'+tip+'当前数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.tips('正在努力执行中，请等待...','#'+id,{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/BaseAppCipherBusiness/modify",
        	data: {
            	id: id,
            	status: status
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

function setRemarks(id){
	var openWin;
	var title = "设置密钥备注";
    layer.open({
    	maxmin:true,
        type: 2,
        title: title,
        area: ['800px', '250px'],
        content: "access_remark.html",
        btn: ['保存','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(id);
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

function deleteData(id){
	layer.confirm('您确定要删除当前数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.tips('正在努力删除数据，请等待...','#'+id+"001",{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/BaseAppCipherBusiness/remove",
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

function createAccess(){
	layer.confirm('您确定要创建新的密钥吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.msg('正在努力执行中，请等待...',{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/BaseAppCipherBusiness/add",
        	data: {
        		appId: tableConfig.appId
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

function initData(appId){
	tableConfig.appId = appId;
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

