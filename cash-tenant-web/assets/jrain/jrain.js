 (function($){
    //首先备份下jquery的ajax方法
    var _ajax=$.ajax;
    //重写jquery的ajax方法
    $.ajax=function(opt){
    	//备份opt中error和success方法
        var fn = {
            error:function(XMLHttpRequest, textStatus, errorThrown){},
            success:function(data, textStatus){},
            beforeSend:function(XHR){}
        }
        if(opt.error){
            fn.error=opt.error;
        }
        if(opt.success){
            fn.success=opt.success;
        }
        if(opt.beforeSend){
            fn.beforeSend=opt.beforeSend;
        }
        //扩展增强处理
        var _opt = $.extend(opt,{
        	error:function(XMLHttpRequest, textStatus, errorThrown){
                //错误方法增强处理
                fn.error(XMLHttpRequest, textStatus, errorThrown);
            },
            success:function(data, textStatus){
                //成功回调方法增强处理
                if(data && data.code && data.code==403){
                	$$.goUrl("/login/login.html");
                }else{
                	fn.success(data, textStatus);
                }
            },
            beforeSend:function(XHR){
            	var storage = window.localStorage;
				if(storage){
					var token = storage.getItem($$.sysFlag+"hgtg-token");
					if(token){
						XHR.setRequestHeader("token",token);
					}
				}
				fn.beforeSend(XHR);
            }
        });
        return _ajax(_opt);
    };
})(jQuery);

//===================================================================================================
// 基础消息 106.15.58.111
//===================================================================================================
(function(){
	var $$ = {
		base : sysBasicsConfig.base,
		restTenantServerUrl : sysBasicsConfig.restTenantServerUrl,
		restTenantVersion : sysBasicsConfig.restTenantVersion,
        sysFlag : sysBasicsConfig.sysFlag,
		loadJs : function (url, callback){
			url = $$.base + url;
			var done = false;
			var script = document.createElement('script');
			script.type = 'text/javascript';
			script.language = 'javascript';
			script.src = url;
			script.onload = script.onreadystatechange = function(){
				if (!done && (!script.readyState || script.readyState == 'loaded' || script.readyState == 'complete')){
					done = true;
					script.onload = script.onreadystatechange = null;
					if (callback){
						callback.call(script);
					}
				}
			}
			document.getElementsByTagName("head")[0].appendChild(script);
		},
		loadCss : function (url, callback){
			url = $$.base + url;
			var link = document.createElement('link');
			link.rel = 'stylesheet';
			link.type = 'text/css';
			link.media = 'screen';
			link.href = url;
			document.getElementsByTagName('head')[0].appendChild(link);
			if (callback){
				callback.call(link);
			}
		},
		goUrl : function(url){
			if(url.indexOf("login.html")!=-1){
				url = $$.base + url;
				window.top.location.href = url;
			}else{
				url = $$.base + url;
				window.location.href = url; 
			}
		}
	};
	window.$$ = $$; 
	$.fn.serializeObject = function(){
		var o = {};
		var a = this.serializeArray();
		$.each(a, function() {
			var tname = this.name;
			if(tname && tname.length > 2 && tname.substr(0,2) == "b_"){
				o[tname.substr(2)] = this.value || '';	 
			}
			if(tname == 'url'){
				o.url = this.value;
			}
            if(tname == 'extenddata'){
				if(this.value.length>1 ){
					var args=$$.getQueryStringUrl("?"+this.value);
					for(var key in args){
						o[key]=args[key];
					}
                }
            }

		});
        console.log("==extenddata==o:=="+"?"+JSON.stringify(o));
		return o;
	};
    $$.getQueryStringUrl=function(UrlStr){
        var o = {};
        var params = UrlStr.match(new RegExp("[\?\&][^\?\&]+=[^\?\&]+","g"));
        if(!params){
            return o;
        }
        for(var i = 0; i < params.length; i++){
            var tempParam = params[i].substring(1);
            if(tempParam){
                var startIndex = tempParam.indexOf("=");
                if(startIndex > -1){
                    o[tempParam.substring(0,startIndex)] = tempParam.substring(startIndex+1);
                }
            }
        }
        return o;
    };

	$$.getQueryString=function(){
		var o = {};
		var url = window.location.href;
		if(url.indexOf("?")!=-1){
			url = url.substring(url.indexOf("?"));
		}
		var params = url.match(new RegExp("[\?\&][^\?\&]+=[^\?\&]+","g"));
	    if(!params){
	        return o;
	    }
	    for(var i = 0; i < params.length; i++){
	       var tempParam = params[i].substring(1);
	       if(tempParam){
		        var startIndex = tempParam.indexOf("=");
		        if(startIndex > -1){
		        	o[tempParam.substring(0,startIndex)] = tempParam.substring(startIndex+1);
		        }
	     	}
	    }
	    return o;
	}

})();
//===================================================================================================
// ajax处理
//===================================================================================================
(function(){
	if(!$$){
		$$ = {};
	}
	$$.error = function(msg,fn){
		$$.msg(msg,"error",fn);
	}; 
	
	$$.alert = function(msg,fn){
		$$.msg(msg,"info",fn); 
	};
    $$.alertOp = function(msg,op,fn){
        layer.alert(msg,op,fn);
    };
	$$.msg = function(msg,type,fn){
		type = type || 'info';
		if(type == 'error'){
			layer.alert(msg,{icon:2,closeBtn:0},fn);
		}else{
		    layer.alert(msg,{icon:1,closeBtn:0},fn);
		}
		//fn.call();
		//$.messager.alert($$.ajax.defaults.title, msg,type,fn);
	};

	/**********************************************************
    * ajax 请求
    * service      : 服务名称 
	* data         : 请求数据
	* eflag        : true,false  请求失败是否弹出alert提示，默认true
	* seflag       : ture,false  数据错误是否弹出alert提示, 默认true
	* beforeSend   : 请求前执行的函数
	* success      :  成功回调函数
	* error        : 请求失败函数
	* serror       : 请求成功，内容设备
	**********************************************************/
    $$.ajax = function(options){
        options = options || {};
        //合并参数
        var opts = $.extend({},$$.ajax.defaults,options);
        //处理REST URL
        opts.url =  $$.restTenantServerUrl +opts.url+$$.restTenantVersion;
        $.ajax({
            type: opts.type,
            url: opts.url,
            data: opts.data,
            async :  opts.async,
            beforeSend: function(XMLHttpRequest) {
                if(opts.beforeSend){
                    return opts.beforeSend.call(this);
                }
                return true;
            },
            error: function() {
                if(opts.showResult){
                    opts.showResult.call(this,"不能连接到服务器",opts);
                }
                if(opts.eflag){
                   $$.ajax.defaults.ajaxError();
                }
                if(opts.error){
                    opts.error.call(this,opts);
                }
            },
            success: function(rs) {
                if(opts.showResult){
                    opts.showResult.call(this,rs,opts);
                }
                if(rs && (rs.code ==  200)){
                    if(opts.sflag){
                    	$$.alert("操作成功!");
                    }
                    if(opts.success){
                    	if(rs.rows){
                            opts.success.call(this,rs.rows,opts);
						}else{
                            opts.success.call(this,rs.data,opts);
						}
                    }
                }else{
                    if(opts.seflag){
                 		$$.ajax.defaults.ajaxSError(rs);       
                    }
                    if(opts.serror){
                        opts.serror.call(this,rs,opts);
                    }
                }
            }
        });
    };

    $$.remove = function(options){
		$.messager.confirm($$.ajax.defaults.title, $$.ajax.defaults.removeMsg,
		function(bs) {
			if (bs) {
        		$$.ajax(options);            
			}
		});
	};

	$$.submit = function(options){
		$.messager.confirm($$.ajax.defaults.title, $$.defaults.submitMsg,
		function(bs) {
			if (bs) {
        		$$.ajax(options);            
			}
		});
	};
 
    $$.ajax.defaults = {
		 type       : "POST",
		 async      : true,
		 eflag      : true,
		 sflag      : false,
		 seflag     : true,
		 data       : {},
		 title      : '系统消息',
		 errorMsg   : '不能连接服务器，请检查连接地址',
		 removeMsg  : '您确认要删除吗？',
		 submitMsg  : '你确认要提交吗？',
		 beforeSend : null,		 
		 success    : null,
		 showResult : null,
		 error      : null,
		 serror     : null
	};
	$$.ajax.defaults.ajaxError = function(){
		$$.alert($$.ajax.defaults.errorMsg);
		//$.messager.alert($$.ajax.defaults.title, $$.ajax.defaults.errorMsg, 'error');
	};
	$$.ajax.defaults.ajaxSError = function(rs){
		$$.error(rs.msg || "");
	};
})();

//===================================================================================================
// 表单处理
//=================================================================================================== 
(function($){
	if(!$$){
		$$ = {};
	}
	
	function innerAble(opts){
		if(opts.able){
			opts.able.call(opts);	
		}else{
			/*
		 	$('.easyui-linkbutton').linkbutton('enable');
	    	$('.easyui-linkbutton').removeAttr("disabled");
	    	*/
		}
	}
	
	function innerDisAble(opts){
		if(opts.disAble){
			opts.disAble.call(opts);
		}else{
		    //	 $('.easyui-linkbutton').linkbutton('disable');
			//  $('.easyui-linkbutton').attr("disabled","disabled");
		}
	}

	function innnerCheckForm(opts){

			var b =true; // $(opts.fname).form("validate");
			if(b && opts.checkForm){
				b = opts.checkForm.call(opts);
			}
			return b;
        /**/
		return true;
	}
 
    $$.form = function(options)  {

    	options = options || {};	
		opts = $.extend({},$$.form.defaults,options);

		innerDisAble(opts);
		var b = innnerCheckForm(opts);
		if(!b){
			innerAble(opts);
		}
		var formData = $(opts.fname).serializeObject();
		var url = formData.url;
		delete formData.url;
 		if(b){
 			$$.ajax({
 				url : url,
		        data: formData,
				async:opts.async,
				suiteCode:opts.suiteCode,
                showResult:options.showResult,
				error: function(request) {
		            innerAble(opts);
		        },
		        success :function(data,op){
					innerAble(opts);
					if(opts.success){
						opts.success.call(this,data,opts);
					} 
				},
				serror: function(){
					innerAble(opts);
				}
	  		});
 		}
		
    };
 
    $$.form.defaults = $.extend({},$$.ajax.defaults,{
		 fname: '#theform',
		 able : null,
		 async:true,
		 disAble : null,
		 checkForm : function(){
		 	return true;
		 }		 
	});
})(jQuery);

