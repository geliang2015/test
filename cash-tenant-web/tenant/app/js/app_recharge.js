/**
 * 初始化数据
 * @param {Object} type
 * @param {Object} id
 */
function initData(appCode){
	$("cashShow").attr("disabled", "disabled");
	$("#cashShow").css("cssText", "background-color:#F3F2F0 !important;");
	$("#b_url").val("/rest/BaseAppBusiness/recharge");
	$$.ajax({
        url:"/rest/BaseAppBusiness/queryAccount",
        async:false,
        data : {
        	appCode: appCode
        },
        success : function(data){
            $("#b_appCode").val(appCode);
            $("#cashShow").val(data.cash);
            $("#b_version").val(data.version);
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
        	b_cash: {
                required: true,
                digits: true,
                range:[1,100000]
            }
        },
        errorPlacement: function(error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "span",
        errorClass: "errortips",
        success: 'valid',
        messages: {
        	b_cash: {
                required: "(必填)",
                digits: "(必须是不小于0的整数)",
                range:"(必须在{0}和{1}之间)"
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

function getCash(){
	var obj = {};
	if($("#dataForm").valid()){
		obj.check = true;
		obj.cash = $("#b_cash").val();
	}else{
		obj.check = false;
	}
	return obj;
}

$(function(){
	$('#b_cash').focus();
	addFormValidate();
})