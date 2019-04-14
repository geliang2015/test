/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(id){
	$("#b_url").val("/rest/BaseAppCipherBusiness/saveRemarks");
	$("#b_id").val(id);
	$$.ajax({
        url:"/rest/BaseAppCipherBusiness/query",
        async:false,
        data : {
            id : id
        },
        success : function(data){
            $("#b_remarks").val(data.remarks);
        }
    });
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
	$('#b_remarks').focus();
	addFormValidate();
});
