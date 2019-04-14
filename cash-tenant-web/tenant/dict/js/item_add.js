/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(type,id,dictId,dictCode){
	$("#dictCode-show").css("cssText", "background-color:#F3F2F0 !important;");
	$("#b_dictId").val(dictId);
	$("#b_dictCode").val(dictCode);
	if(type=="update"){
		$("#b_url").val("/rest/TemplateDictItemBusiness/modify");
		$$.ajax({
	        url:"/rest/TemplateDictItemBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	        	$("#b_id").val(data.id);
	            $("#dictCode-show").val(data.dictCode);
	            $("#b_itemKey").val(data.itemKey);
	            $("#b_itemName").val(data.itemName);
	            $("#b_itemValue").val(data.itemValue);
	            $("#b_extralField1").val(data.extralField1);
	            $("#b_extralField2").val(data.extralField2);
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
		$("#dictCode-show").val(dictCode);
		$("#b_url").val("/rest/TemplateDictItemBusiness/add");
	}
}

/**
 * 校验表单
 */
function validateForm(){
	return $("#dataForm").valid();
}

/**
 * 添加表单校验
 */
function addFormValidate() {
    $("#dataForm").validate({
        rules: {
            b_itemKey: {
                required: true,
                rangelength: [1,30]
            },
            b_itemName: {
                required: true,
                rangelength: [1,30]
            },
            b_itemValue: {
                required: true,
                rangelength: [1,1000]
            },
            b_extralField1: {
                rangelength: [1,1000]
            },
            b_extralField2: {
                rangelength: [1,1000]
            },
            b_rowSort:{
                required: true,
                range: [1,9999999]
            },
            b_remarks:{
                maxlength: 250
            }
        },
        errorPlacement: function(error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "span",
        errorClass: "errortips",
        success: 'valid',
        messages: {
        	b_itemKey: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_itemName: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_itemValue: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_extralField1: {
                rangelength: "(必须是{0}到{1}个字符)"
            },
            b_extralField2: {
                rangelength: "(必须是{0}到{1}个字符)"
            },
            b_rowSort: {
                required: "(必填)",
                range:"(必须在{0}和{1}之间)"
            },
            b_remarks: {
                maxlength:"(不能超过250个字符)"
            }
        }
    });
}

/**
 * 执行数据保存
 */
function submitForm() {
	var validFormRes = false;
    $$.form({
    	fname:"#dataForm",
        async:false,
        able:function(){
            validFormRes = false;
        },
        checkForm:validateForm,
        success:function () {
            validFormRes = true;
        }
    });
    return validFormRes;
}

$(function(){
	$('#b_itemKey').focus();
	addFormValidate();
});
