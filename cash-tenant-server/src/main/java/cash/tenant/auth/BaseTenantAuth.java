package cash.tenant.auth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cash.tenant.bean.CashConst;
import jrain.fw.business.bean.BusinessConst;
import jrain.fw.business.bean.BusinessRequest;
import jrain.fw.business.bean.BusinessResponse;
import jrain.fw.business.bi.BusinessInterceptor;
import jrain.fw.business.bi.BusinessInterceptorBefore;
import jrain.fw.cfg.base.CfgService;
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.utils.FwUtils;
import jrain.utils.collection.CollectionUtils;
import jrain.utils.lang.LangUtils;
import jrain.utils.lang.StringUtils;

/**
 * 系统登录认证
 * @author 马毅
 */
@BusinessInterceptor(name = "BaseTenantAuth")
public class BaseTenantAuth implements BusinessInterceptorBefore{

	private List<String> noList = new ArrayList<>();
	private final int sessionTimeout;//单位分钟
	private static final Map<String, Map<String,Object>> loginUserAll = new HashMap<String, Map<String,Object>>();

	public SqlRunner getSqlRunner(){
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	public BaseTenantAuth(){
		CfgService cfg = FwUtils.getCfgService();
		String noStr = StringUtils.trimNull(cfg.getCfgValue("auth", "auth.no"));
		noList = CollectionUtils.valueOfList(noStr);
		sessionTimeout = LangUtils.parseInt(cfg.getCfgValue("auth", "session.timeout"), 30 * 24 * 60);
	}
	
	@Override
	public BusinessResponse before(BusinessRequest request) {
		// 如果是非认证服务->直接跳过
		if(noList.contains(request.getKey())){
			return new BusinessResponse();
		}else{
			BusinessResponse response = this.check(request);
			if(response.getCode()==BusinessConst.CODE_SUCCESS){
				return new BusinessResponse();
			}else{
				return response;
			}
		}
	}

	public BusinessResponse check(BusinessRequest request){
		BusinessResponse response = new BusinessResponse();
		String token = StringUtils.trimNull(request.getHeader("token"));
		Map<String, Object> loginUserMap = loginUserAll.get(token);
		if(StringUtils.isEmpty(loginUserMap)){
			response.setCode(BusinessConst.CODE_AUTH);
			response.setMsg("权限认证失败");
		}else{
			String activeTime = StringUtils.trimNull(loginUserMap.get("activeTime"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date activeDate = null;
			try{
				activeDate = sdf.parse(activeTime);
			} catch(ParseException e){
				activeDate = null;
			}
			//超过设定时间没有操作，视为登录超时，用户需重新登录系统
			if(activeDate==null || new Date().getTime()-activeDate.getTime()>sessionTimeout*60l*1000){
				loginUserAll.remove(token);
				response.setCode(BusinessConst.CODE_AUTH);
				response.setMsg("登录超时，请重新登录！");
			}else{
				//记录活跃时间
				loginUserMap.put("activeTime", sdf.format(new Date()));
			}
		}
		return response;
	}
	
	public static boolean setPresentLoginUser(String token,Map<String, Object> loginUser){
		boolean res = false;
		if(!StringUtils.isEmpty(token) && loginUser!=null && loginUser.size()>0){
			loginUserAll.put(token, loginUser);
			res = true;
		}
		return res;
	}
	
	public static Map<String, Object> getPresentLoginUser(BusinessRequest request){
		String token = StringUtils.trimNull(request.getHeader("token"));
		return loginUserAll.get(token);
	}
	
	public static Object getPresentLoginUserId(BusinessRequest request){
		String token = StringUtils.trimNull(request.getHeader("token"));
		Map<String, Object> loginUser = loginUserAll.get(token);
		Object userId = null;
		if(loginUser!=null){
			userId = loginUser.get("id");
		}
		return userId;
	}
	
	public static Object getPresentLoginUserCode(BusinessRequest request){
		String token = StringUtils.trimNull(request.getHeader("token"));
		Map<String, Object> loginUser = loginUserAll.get(token);
		Object userCode = null;
		if(loginUser!=null){
			userCode = loginUser.get("userCode");
		}
		return userCode;
	}
	
	public static Map<String, Object> removePresentLoginUser(BusinessRequest request){
		String token = StringUtils.trimNull(request.getHeader("token"));
		return loginUserAll.remove(token);
	}

	public static Map<String, Map<String, Object>> getLoginUserAll() {
		return loginUserAll;
	}

}
