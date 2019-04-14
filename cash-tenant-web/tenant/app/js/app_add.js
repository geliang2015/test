//编码验证
jQuery.validator.addMethod("isAppCode", function(value, element) {  
    var length = value.length;  
    var reg = /^([A-Z]|[a-z]){5}$/;
    return this.optional(element) || (reg.test(value));  
}, "必须由5位字母组成");

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
    	$("#b_appCode").attr("disabled", "disabled");
    	$("#b_appCode").css("cssText", "background-color:#F3F2F0 !important;");
		$("#b_url").val("/rest/BaseAppBusiness/modify");
		$$.ajax({
	        url:"/rest/BaseAppBusiness/query",
	        async:false,
	        data : {
	            id : id
	        },
	        success : function(data){
	            $("#b_id").val(data.id);
	            $("#b_appCode").val(data.appCode);
	            $("#b_appName").val(data.appName);
	            $("#b_mobile").val(data.mobile);
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
		$("#b_url").val("/rest/BaseAppBusiness/add");
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
        	b_appCode: {
                required: true,
                isAppCode: true
            },
            b_appName: {
                required: true,
                rangelength: [1,60]
            },
            b_mobile: {
            	required: true,
            	isMobile: true,
            },
            b_rowSort: {
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
            b_appCode: {
            	required: "(必填)",
            	isAppCode:"(租户编码必须由5位字母组成)"
            },
            b_appName: {
            	required: "(必填)",
                rangelength:"(租户名称必须是{0}到{1}个字符)"
            },
            b_mobile: {
            	required: "(必填)",
            	isMobile: "请填写正确的手机号码！",
            },
            b_rowSort: {
                required: "(必填)",
                range:"(排序值必须在{0}和{1}之间)"
            },
            b_remarks: {
                maxlength:"(备注不能超过250个字符)"
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
	$('#b_appCode').focus();
	addFormValidate();
})