/**
 * 更改登录窗口大小
 */
function changeHight() {
	var vh = $(window).height();
    $(".index_login").height(vh);
}

/**
 * 设置登录状态
 */
function setLoginStatus() {
	$(".re_password .rem_btn").click(function(){
        if ($(this).find("img").css("margin-top") == "0px") {
            $(this).find("img").css("margin-top", "-30px");
            $('#rememberMe').val('false');
        }else{
            $(this).find("img").css("margin-top", "0px");
            $('#rememberMe').val('true');
        }
    });
}

/**
 * 数据验证
 */
function validateLoginForm(){
	$("#loginForm").validate({
        rules: {
        	userName: {
                required: true,
                maxlength:30
            },
            password: {
                required: true,
                maxlength:30
            }
        },
        errorPlacement: function(error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "span",
        errorClass: "errortips",
        success: 'valid',
        messages: {
        	userName: {
                required: "(请输入用户名)",
                maxlength:"(最多输入{0}个字符)"
            },
            password: {
                required: "(请输入密码)",
                maxlength:"(最多输入{0}个字符)"
            }
        }
    });
}

/**
 * 登录成功后处理-存储用户登录信息
 * @param {Object} userName
 * @param {Object} uuid
 */
function loginSuccessDeal(data,userCode,pwd){
	var storage = window.localStorage;
	if(storage){
		storage.clear();
		storage.setItem($$.sysFlag+"hgtg-token",data.token);
		storage.setItem($$.sysFlag+"hgtg-userName",data.userName);
		var rememberMe = $("#rememberMe").val();
		if(rememberMe=="true"){
			storage.setItem($$.sysFlag+"hgtg-userCode",userCode);
		}
	}else{
		$$.alert("浏览器版本过低!");
	}
}

/**
 * 执行登录
 */
function loginSystem(){
	if($('#loginForm').valid()) {
		var userCode = $("#userCode").val();
		var password = $("#password").val();
		$$.ajax({
	        url:"/rest/LoginBusiness/login",
	        data:{"userCode":userCode,"password":password},
	        success : function(data){
	        	if(data.result=="success"){
	        		loginSuccessDeal(data,userCode,password);
		            firstPageUrl = "/frame/index.html";
		            $$.goUrl(firstPageUrl);
	        	}
	        }
	    });
    }
}

$(function () {
    changeHight();
    setLoginStatus();
    validateLoginForm();
    
    //设置登录信息
    var storage = window.localStorage;
	if(storage){
		var userCode = storage.getItem($$.sysFlag+"hgtg-userCode");
		if(userCode){
			$("#userCode").val(userCode);
		}
	}
    
    //提交登录
    $("#subBtn").on('click',function () {
        loginSystem("");
    });
    
    $('.entrer-login').bind('keypress',function(e){
        if(e.keyCode == 13){
        	loginSystem("");
        }
    });
    
    $(window).resize(function () {
	    changeHight();
	});
    
});
