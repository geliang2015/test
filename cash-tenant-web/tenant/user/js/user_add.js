//编码验证
jQuery.validator.addMethod("isMobile", function(value, element) {  
    var length = value.length;
    var reg = /^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$/;
    return this.optional(element) || (reg.test(value));  
}, "请填写正确的手机号码");

/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(type,id){
	if(type=="update"){
		$("#pwd-label").html("密码：");
		$("#b_url").val("/rest/TemplateUserBusiness/modify");
		$$.ajax({
	        url:"/rest/TemplateUserBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	            $("#b_id").val(data.id);
	            $("#b_userCode").val(data.userCode);
	            $("#b_userName").val(data.userName);
	            var status = data.status;
	            if(status==1){
	            	$('#in-use').attr('checked', 'checked');
	            }else{
	            	$('#not-in-use').attr('checked', 'checked');
	            }
	            $("#b_rowSort").val(data.rowSort);
	            $("#b_remarks").val(data.remarks);
	        }
	    });
	}else{
		$("#b_url").val("/rest/TemplateUserBusiness/add");
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
        	b_userCode: {
                required: true,
                rangelength: [1,20]
            },
            b_userName: {
                required: true,
                rangelength: [1,60]
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
            b_userCode: {
            	required: "(必填)",
                rangelength:"(用户编码必须是{0}到{1}个字符)"
            },
            b_userName: {
            	required: "(必填)",
                rangelength:"(用户名称必须是{0}到{1}个字符)"
            },
            b_remarks: {
                maxlength:"(备注不能超过300个字符)"
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
	$('#b_userCode').focus();
	addFormValidate();
})