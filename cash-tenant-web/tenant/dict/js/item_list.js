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
                {field:"dictCode",title:"类型编码",valign:"middle",sortable: true},
                {field:"itemKey",title:"数据项编码",valign:"middle",sortable:true},
                {field:"itemName",title:"数据项名称",valign:"middle"},
                {field:"itemValue",title:"数据值",class:'w100',valign:"middle"},
                {field:"extralField1",title:"备用1",class:'w100',valign:"middle"},
                {field:"extralField2",title:"备用2",class:'w100',valign:"middle"},
                {field:"remarks",title:"备注",class:'w100',valign:"middle"},
                {field: "rowSort", title: "排序号",valign:"middle",width:80},
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
	                        '<li><a href="javascript:updateData(\'' + row.id + '\',\'' + row.dictId + '\',\'' + row.dictCode + '\')" title="编辑">',
	                        '<i class="glyphicon glyphicon-pencil"></i><em> 编辑</em></a></li>',
	                        '<li><a href="javascript:void(0)" onclick="deleteData(\''+row.id+'\');">',
	                        '<i class="glyphicon glyphicon-trash"></i> 删除</a></li>',
	                        '</ul></div>'
	                    ].join('');
	                }

	            }
	        ]
        },
        gridInit:function(columns){
            return $("#dataTable").bootstrapTable({
                method:"post",
                contentType:"application/x-www-form-urlencoded",
                url:$$.restTenantServerUrl + '/rest/TemplateDictItemBusiness/list'+$$.restTenantVersion,
                dataField: 'data',
                totalField:'total',
                columns: columns,
                undefinedText:'-',
                height:$(window).height() - 88,
                striped: 'true',
                classes: 'table table-hover qxttable',
                pagination: 'true',
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
                    10,20, 50, 100
                ],
                queryParams: function queryParams(params) {//设置查询参数
	                var args={};
	                args.c_dictId_1=pageParams.dictId;
	                var itemKey = $("#itemKey").val();
	                var itemName = $("#itemName").val();
	                if(itemKey!==""){
	                	args.c_itemKey_7 = itemKey;
	                }
	                if(itemName!==""){
	                	args.c_itemName_7 = itemName;
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
 * 新增
 */
function addData(dictId,dictCode) {
	openOperateWindow("item_add.html","add","",dictId,dictCode);
}

/**
 * 修改
 */
function updateData(id,dictId,dictCode) {
    openOperateWindow("item_add.html","update",id,dictId,dictCode);
}

/**
 * 打开操作窗口
 */
function openOperateWindow(url,type,id,dictId,dictCode){
	var title = "";
	if(type=="add"){
		title = "新建数据项";
	}else{
		title = "修改数据项";
	}
	var openWin;
    layer.open({
    	maxmin:true,
        type: 2,
        title: title,
        area: ['800px', '450px'],
        content: url,
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
            openWin.initData(type,id,dictId,dictCode);
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
	layer.confirm('您确定要删除选中数据吗？',{icon: 3, title: '提示', area: '300px', btnAlign: 'c'}, function (index) {
    	var indexLoad = layer.load(2);
		var indexTips = layer.tips('正在努力删除数据，请等待...','#del-btn',{time:30*60*1000});
    	layer.close(index);
    	$$.ajax({
        	url: "/rest/TemplateDictItemBusiness/remove",
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
            	url: "/rest/TemplateDictItemBusiness/remove",
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

var pageParams;
$(function () {
	
	pageParams = $$.getQueryString();
    
    var returnParams = "";
    if(pageParams.queryDictCode){
    	returnParams += "&queryDictCode="+pageParams.queryDictCode;
    }
    if(pageParams.queryDictName){
    	returnParams += "&queryDictName="+pageParams.queryDictName;
    }
    if(pageParams.pageNumber){
    	returnParams += "&pageNumber="+pageParams.pageNumber;
    }
	
    $('#itemKey').focus();
    
  //渲染表格
    var columns = tableConfig.columnsInit();
    var table = tableConfig.gridInit(columns);
    
    //重置表单
    $("#searchReset").on("click",function(){
        $('#itemKey').val('');
        $('#itemName').val('');
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
    	addData(pageParams.dictId,decodeURI(decodeURI(pageParams.dictCode)));
    });
    
    //批量删除
    $("#del-btn").click(function(){
		batchDeleteData();
    });
    
    $("#return-btn").click(function () {
        $$.goUrl("/tenant/dict/type_list.html?"+returnParams);
    });
    
    //动态设置表格高度
	$(window).resize(function () {
        $('#dataTable').bootstrapTable('resetView', {
            height: $(window).height() - 88
        });
    });
    
});
