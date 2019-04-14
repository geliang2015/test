/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(type,id){
	if(type=="update"){
		$("#b_url").val("/rest/TemplateDictTypeBusiness/modify");
		$$.ajax({
	        url:"/rest/TemplateDictTypeBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	        	$("#b_id").val(data.id);
	            $("#b_dictCode").val(data.dictCode);
	            $("#b_dictName").val(data.dictName);
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
		$("#b_url").val("/rest/TemplateDictTypeBusiness/add");
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
            b_typeCode: {
                required: true,
                rangelength: [1,30]
            },
            b_typeName: {
                required: true,
                rangelength: [1,30]
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
        	b_typeCode: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_typeName: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_rowSort: {
                required: "(必填)",
                range:"(必须在{0}和{1}之间)"
            },
            b_remarks: {
                maxlength:"(不能超过250个字符)"
            }
        },
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
	$('#b_typeCode').focus();
	addFormValidate();
});
