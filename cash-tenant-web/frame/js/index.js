/**
 * 检查用户是否登录及处理
 */
function checkLoginStatusDeal(){
	var storage = window.localStorage;
	if(storage){
		var v = storage.getItem($$.sysFlag+"hgtg-token");
		if(v){
			$("#loginUser").html(storage.getItem($$.sysFlag+"hgtg-userName")+"，您好！");
		}else{
			$$.goUrl("/login/login.html");
		}
	}
}

/**
 * 成功设置顶部菜单后处理
 */
function dealAfterSetMenuData(){
	//定义点击切换
	$(".q-first-nav li").click(function (){
		checkLoginStatusDeal();
		var selectTopMenu = $(this);
        $(".q-first-nav li[menuCode='"+selectTopMenu.attr("menuCode")+"']").addClass('li-active').siblings().removeClass('li-active');
    	$(".q-second-nav li[menuCode='"+selectTopMenu.attr("menuCode")+"']").addClass('li-active').siblings().removeClass('li-active');
    	$("#J_iframe").attr("src","../"+selectTopMenu.attr("menuUrl"));
    });
	$(".q-first-nav li")[0].click();
	
}

/**
 * 设置远程顶部菜单数据
 */
function setRemoteMenuData(){
	//获取顶部菜单
	$$.ajax({
        url:"/rest/IndexBusiness/getUserTopMenu",
		async:false,
        data : {},
        success : function(data){
        	var topMenuHtml = "";
            if(data && data.length>0){
            	for(var i=0;i<data.length;i++){
            		var menuData = data[i];
            		var menuId = menuData.menuId;
            		var menuCode = menuData.menuCode;
            		var menuName = menuData.menuName;
            		var menuUrl = menuData.menuUrl;
            		topMenuHtml += "<li menuId='"+menuId+"' menuCode='"+menuCode+"' menuName='"+menuName+"' menuUrl='"+menuUrl+"'><p>"+menuName+"</p></li>";
            	}
            }
            $("#top-menu").html(topMenuHtml);
            var storage = window.localStorage;
			if(storage){
				storage.setItem($$.sysFlag+"hgtg-topMenuHtml",topMenuHtml);
			}
        }
   	});
}

/**
 * 初始化顶部菜单
 */
function initMenu(){
	var storage = window.localStorage;
	if(storage){
		var topMenuHtml = storage.getItem($$.sysFlag+"hgtg-topMenuHtml");
		if(topMenuHtml){
			$("#top-menu").html(topMenuHtml);
		}else{
			setRemoteMenuData();
		}
	}else{
		setRemoteMenuData();
	}
	dealAfterSetMenuData();
}

function openUpdatePwdWindow(){
	var openWin;
    layer.open({
        maxmin:true,
        type: 2,
        title: "修改密码",
        area: ['550px', '300px'],
        content: "update_pwd.html",
        btn: ['确定','关闭'],
        btnAlign: 'c',
        success: function (layero, index) {
            openWin = window[layero.find('iframe')[0]['name']];
        },
        yes: function (index, layero) {
            var validFormRes = openWin.submitForm();
            if(validFormRes){
            	layer.close(index);
            	layer.msg("修改密码成功！",{time:1000});
            }
        },
        cancel: function (index) {
            layer.close(index);
        }
    });
}

/**
 * 退出成功，清除本地缓存信息
 */
function loginOutSuccessDeal(){
	var storage = window.localStorage;
	if(storage){
		var userCode = storage.getItem($$.sysFlag+"hgtg-userCode");
		storage.clear();
		if(userCode){
			storage.setItem($$.sysFlag+"hgtg-userCode",userCode);
		}
	}
}

function logout(){
	$$.ajax({
        url:"/rest/IndexBusiness/loginOut",
        data : {},
        success : function(data){
        	loginOutSuccessDeal();
            $$.goUrl("/login/login.html");
        }
    });
}

$(function (){
	
	checkLoginStatusDeal();
	
	//初始化菜单
	initMenu();
	
	//修改密码
    $("#update-pwd").on('click',function () {
    	openUpdatePwdWindow();
    });
	
	//退出登录
    $("#logout").on('click',function () {
        logout();
    });
    
    $("#content-main").height($(window).height());
    
    //动态设置表格高度
	$(window).resize(function () {
        $("#content-main").height($(window).height());
    });
    
});
