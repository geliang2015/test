/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(type,id){
	if(type=="update"){
		$("#b_url").val("/rest/TemplateAuthBusiness/modify");
		$$.ajax({
	        url:"/rest/TemplateAuthBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	        	$("#b_id").val(data.id);
	            $("#b_authCode").val(data.authCode);
	            $("#b_authTitle").val(data.authTitle);
	            $("#b_authBody").val(data.authBody);
	            $("#b_requires").val(data.requires);
	            $("#b_rowSort").val(data.rowSort);
	            var status = data.status;
	            if(status==1){
	            	$('#in-use').attr('checked', 'checked');
	            }else{
	            	$('#not-in-use').attr('checked', 'checked');
	            }
	        }
	    });
	}else{
		$("#b_url").val("/rest/TemplateAuthBusiness/add");
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
        	b_authCode: {
                required: true,
                rangelength: [1,20]
            },
            b_authTitle: {
                required: true,
                rangelength: [1,60]
            },
            b_authBody: {
                rangelength: [1,200]
            },
            b_rowSort:{
                required: true,
                range: [1,9999999]
            }
        },
        errorPlacement: function(error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "span",
        errorClass: "errortips",
        success: 'valid',
        messages: {
        	b_authCode: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_authTitle: {
                required: "(必填)",
                rangelength:"(必须是{0}到{1}个字符)"
            },
            b_authBody: {
                rangelength:"(必须是{0}到{1}个字符)"
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
	$('#b_authCode').focus();
	addFormValidate();
});
