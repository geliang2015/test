/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(type,id,pid,isLeaf){
	if(type=="update"){
		$("#b_url").val("/rest/TemplateMenuBusiness/modify");
		$$.ajax({
	        url:"/rest/TemplateMenuBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	            $("#b_id").val(data.id);
	            $("#b_pid").val(data.pid);
	            $("#b_isLeaf").val(data.isLeaf);
	            $("#b_menuCode").val(data.menuCode);
	            $("#b_menuName").val(data.menuName);
	            $("#b_menuIcon").val(data.menuIcon);
	            $("#b_menuPath").val(data.menuPath);
	            $("#b_rowSort").val(data.rowSort);
	            var status = data.status;
	            if(status==1){
	            	$('#in-use').attr('checked', 'checked');
	            }else{
	            	$('#not-in-use').attr('checked', 'checked');
	            }
	            $("#b_remarks").val(data.remarks);
	        }
	    });
	}else{
		$("#b_pid").val(pid);
		$("#b_isLeaf").val(isLeaf);
		$("#b_url").val("/rest/TemplateMenuBusiness/add");
	}
}

/**
 * 校验表单
 */
function validForm(){
	return $("#dataForm").valid();
}

/**
 * 添加表单校验
 */
function dataFormValid() {
    $("#dataForm").validate({
        rules: {
        	b_menuCode: {
                required: true,
                maxlength: 20
            },
            b_menuName: {
                required: true,
                maxlength: 60
            },
            b_menuIcon: {
                maxlength: 100
            },
            b_menuPath: {
                maxlength: 200
            },
            b_rowSort:{
                required: true,
                range: [1,9999999]
            },
            b_remarks:{
                maxlength: 300
            }
        },
        errorPlacement: function(error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "span",
        errorClass: "errortips",
        success: 'valid',
        messages: {
            b_menuCode: {
                required: "(必填)",
                maxlength:"(菜单编码不能超过{0}个字符)"
            },
            b_menuName: {
                required: "(必填)",
                maxlength:"(菜单名称不能超过{0}个字符)"
            },
            b_menuIcon: {
                maxlength:"(菜单图标不能超过{0}个字符)"
            },
            b_menuPath: {
                maxlength:"(菜单路径不能超过{0}个字符)"
            },
            b_rowSort: {
                required: "(必填)",
                range:"(排序值必须在{0}和{1}之间)"
            },
            b_remarks: {
                maxlength:"(备注不能超过{0}个字符)"
            }
        },
    });
}

/**
 * 执行数据保存
 */
function submitForm(){
	var validFormRes = false;
    $$.form({
    	fname:"#dataForm",
        async:false,
        able:function(){
            validFormRes = false;
        },
        checkForm:validForm,
        success:function () {
            validFormRes = true;
        }
    });
    return validFormRes;
}

$(function(){
	$('#b_menuCode').focus();
    dataFormValid();
})