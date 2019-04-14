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
    	pid:0,
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
	                field: 'menuCode',
	                title: '菜单编码',
	                valign:'middle',
	                sortable : true
	            },
	            {
	                field: 'menuName',
	                title: '菜单名称',
	                valign:'middle',
	                sortable : true
	            },
	            {
	                field: 'menuIcon',
	                title: '菜单图标',
	                valign:'middle'
	            },
	            {
	                field: 'menuPath',
	                title: '菜单路径',
	                valign:'middle'
	            },
	            {
	                field: 'remarks',
	                title: '备注',
	                valign:'middle'
	            },
	            {
	                field: 'rowSort',
	                title: '排序号',
	                width: 80,
	                valign:'middle',
	                sortable : true
	            },
	            {
	                field: 'optionDel',
	                valign:'middle',
	                checkbox: true
	            },
	            {
	                field: 'optionEdit',
	                title: '编辑',
	                width: 60,
	                align: 'center',
	                valign:'middle',
	                formatter: function (value, row, index) {
	                    return [
	                        '<a class="opt-icon" href="javascript:void(0)" title="编辑" onclick="updateData(\''+row.id+'\',\''+row.pid+'\',\''+row.isLeaf+'\')">',
	                        '<i class="glyphicon glyphicon-pencil"></i>',
	                        '</a>'
	                    ].join('');
	                }
	            }
            ]
        },
        gridInit:function(columns){
            return $("#dataTable").bootstrapTable({
                method: "post",
                contentType: "application/x-www-form-urlencoded",
                dataField: 'data',
                totalField:'total',
                columns: columns,
                undefinedText: '-',
                striped: 'true',
                classes: 'table table-hover qxttable',
                pagination: 'true',
                clickToSelect: true,
                singleSelect: false,
                height:$(window).height() - 88,
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
                    var menuCode = $("#menuCode").val();
                    var menuName = $("#menuName").val();
                    var args = {};
                    args.c_pid_1 = tableConfig.pid;
                    if(menuCode!==""){
                    	args.c_menuCode_7 = menuCode;
                    }
                    if(menuName!==""){
                    	args.c_menuName_7 = menuName;
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
            })
        },
        refresh: function () {
        	var url = $$.restTenantServerUrl + '/rest/TemplateMenuBusiness/list'+$$.restTenantVersion;
            $("#dataTable").bootstrapTable('refresh',{url:url});
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

/*树=================begin===========================*/
var source;
function initMenuTree(){
	//数据源
	source = {
	    type: 'post',
	    root: 'data',
	    dataType: "json",
	    dataFields: [{
	        name: "id",
	        type: "string"
		},{
	        name: "pid",
	        type: "string"
		},{
	        name: "menuCode",
	        type: "string"
		},{
	        name: "menuName",
	        type: "string"
		},{
	        name: "menuPath",
	        type: "string"
		},{
			name: "level",
	        type: "string"
		},{
			name: "isLeaf",
	        type: "string"
		},{
            name: "status",
            type: "string"
        },{
            name: "rowSort",
            type: "number"
        },{
            name: "remarks",
            type: "string"
        }],
		hierarchy: {
		    keyDataField: { name: 'id' },
	        parentDataField: { name: 'pid' }
		},
		id: "id"
	};
	
	//数据适配器
	var dataAdapter = new $.jqx.dataAdapter(source, {
		beforeSend: function(jqXHR, settings){
			var storage = window.localStorage;
			if(storage){
				var token = storage.getItem($$.sysFlag+"hgtg-token");
				if(token){
					jqXHR.setRequestHeader("token",token);
				}
			}
		},
	    loadComplete: function () {
	    	//指定排序列
	    	$("#categoryTable").jqxTreeGrid('sortBy', 'rowSort', 'asc');
	    	//展开树
	    	$("#categoryTable").jqxTreeGrid('expandAll');
	    	//展开指定节点
	    	//$("#categoryTable").jqxTreeGrid('expandRow', tableConfig.pid);
	    	//选中指定节点，触发选中事件
	    	$("#categoryTable").jqxTreeGrid('selectRow', tableConfig.pid);
	    },
	    downloadComplete: function(edata, textStatus, jqXHR){
	    	if(edata && edata.code && edata.code==403){
	    		$$.goUrl("/login/login.html");
	    	}
	    }
	});
	
	//表格
    $("#categoryTable").jqxTreeGrid({
        source: dataAdapter,
        width: '100%',
        altRows: true,
        height: $(window).height()-40,
        selectionMode: "singleRow",
        showToolbar: false,
        theme: 'bootstrap',
        columns: [
	        {
	            text: "菜单",
	            align: "center",
	            dataField: "menuName"
	        }
        ]

    });
     
    //选中行事件
    $("#categoryTable").on('rowSelect', function (event) {
      	var args = event.args;
      	tableConfig.pid = args.row.id;
      	tableConfig.resetPageNumber();
      	tableConfig.refresh();
  	});
    
    //初始化菜单树
	loadMenuTreeData();
}

function loadMenuTreeData(){
	source.url = $$.restTenantServerUrl + '/rest/TemplateMenuBusiness/listAll'+$$.restTenantVersion;
	$('#categoryTable').jqxTreeGrid('updateBoundData');
}

/*部门树=================end===========================*/

/**
 * 新增
 */
function addData() {
	openOperateWindow("menu_add.html","add","",tableConfig.pid,1);
}

/**
 * 修改菜单
 */
function updateData(id,pid,isLeaf) {
    openOperateWindow("menu_add.html","update",id,pid,isLeaf);
}

/**
 * 打开操作窗口
 */
function openOperateWindow(url,type,id,pid,isLeaf){
	var title = "";
	if(type=="add"){
		title = "新建菜单";
	}else{
		title = "修改菜单";
	}
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: title,
        area: ['800px', '380px'],
        content: url,
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(type,id,pid,isLeaf);
        },
        yes: function (index, layero) {
            var validFormRes = openWin.submitForm();
            if(validFormRes){
            	layer.close(index);
            	layer.msg(title+"成功！",{time:1000});
            	loadMenuTreeData();
            }
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

/**
 * 打开复制菜单窗口
 */
function copyMenuWindow(){
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: "同步菜单-选择目标租户",
        area: ['900px', '500px'],
        content: "menu_copy.html",
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initAppCode();
        },
        yes: function (index, layero) {
        	var selectData = openWin.getSelectData();
        	if(selectData){
        		//复制菜单数据
            	var indexLoad = layer.load(2);
    			var indexTips = layer.tips('正在努力同步菜单数据，请等待...','#copy-btn',{time:30*60*1000});
            	layer.close(index);
            	$$.ajax({
                	url: "/rest/TemplateMenuBusiness/copyMenuData",
                	data: {
                		appCode: selectData.appCode
                	},
                	success: function(data) {
                		layer.close(indexLoad);
    		        	layer.close(indexTips);
    		        	layer.tips('同步成功！','#copy-btn',{time:1000});
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
        	}
        },
        cancel: function (index) {
            layer.close(index);
        }
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
        	var pid = "";
        	for (var i = 0; i < rowDatas.length; i++) {
            	rowIds.push(rowDatas[i].id);
            	if(pid===""){
            		pid = rowDatas[i].pid;
            	}
        	}
        	layer.close(index);
        	$$.ajax({
            	url: "/rest/TemplateMenuBusiness/remove",
            	data: {
                	id : rowIds.join(','),
                	pid: pid
            	},
            	success: function(data) {
            		layer.close(indexLoad);
		        	layer.close(indexTips);
            		loadMenuTreeData();
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
	
	//右侧表格
    $('#menuCode').focus();
	
	//渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
	
    //tree
    initMenuTree();
    
    //重置
    $("#searchReset").on("click",function(){
        $('#menuName').val('');
        $('#menuCode').val('');
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
    
    //复制菜单
    $("#copy-btn").on('click',function () {
    	copyMenuWindow();
    });
    
    //新建
    $("#add-btn").on('click',function () {
        addData();
    });
    
    //批量删除
    $("#del-btn").click(function(){
        batchDeleteData();
    });
    
    
    //动态设置表格高度
	$(window).resize(function () {
        $("#categoryTable").jqxTreeGrid({
            height: $(window).height() - 40
        });
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height() - 88
        });
    });
    
});

