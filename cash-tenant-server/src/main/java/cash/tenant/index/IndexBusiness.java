package cash.tenant.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cash.tenant.auth.BaseTenantAuth;
import cash.tenant.bean.CashConst;
import jrain.fw.business.base.Business;
import jrain.fw.business.base.BusinessMethod;
import jrain.fw.business.bean.BusinessRequest;
import jrain.fw.business.bean.BusinessResponse;
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.utils.FwUtils;
import jrain.utils.lang.StringUtils;

/**
 * 首页
 * @author 马毅
 */
@Business(name="IndexBusiness")
public class IndexBusiness {
	
	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}
	
	/**
	 * 获取用户顶部菜单
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse getUserTopMenu(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		List<Map<String, Object>> topMenuList = new ArrayList<Map<String, Object>>();
		Map<String, Object> appMenuMap = new HashMap<String, Object>();
		appMenuMap.put("menuId", "111111");
		appMenuMap.put("menuCode", "appMenu");
		appMenuMap.put("menuName", "租户管理");
		appMenuMap.put("menuUrl", "/tenant/app/app_list.html");
		topMenuList.add(appMenuMap);
		Map<String, Object> dictMenuMap = new HashMap<String, Object>();
		dictMenuMap.put("menuId", "222222");
		dictMenuMap.put("menuCode", "dictMenu");
		dictMenuMap.put("menuName", "字典管理");
		dictMenuMap.put("menuUrl", "/tenant/dict/type_list.html");
		topMenuList.add(dictMenuMap);
		Map<String, Object> menuMenuMap = new HashMap<String, Object>();
		menuMenuMap.put("menuId", "333333");
		menuMenuMap.put("menuCode", "menuMenu");
		menuMenuMap.put("menuName", "菜单管理");
		menuMenuMap.put("menuUrl", "/tenant/menu/menu_list.html");
		topMenuList.add(menuMenuMap);
		Map<String, Object> menuAuthMap = new HashMap<String, Object>();
		menuAuthMap.put("menuId", "444444");
		menuAuthMap.put("menuCode", "authMenu");
		menuAuthMap.put("menuName", "认证管理");
		menuAuthMap.put("menuUrl", "/tenant/auth/auth_list.html");
		topMenuList.add(menuAuthMap);
		String loginUserCode = StringUtils.trimNull(BaseTenantAuth.getPresentLoginUserCode(request));
		if(CashConst.ADMIN_USER_CODE.equals(loginUserCode)){
			Map<String, Object> userMenuMap = new HashMap<String, Object>();
			userMenuMap.put("menuId", "555555");
			userMenuMap.put("menuCode", "userMenu");
			userMenuMap.put("menuName", "用户管理");
			userMenuMap.put("menuUrl", "/tenant/user/user_list.html");
			topMenuList.add(userMenuMap);
		}
		response.setData(topMenuList);
		return response;
	}

	/**
	 * 退出登录
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse loginOut(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		BaseTenantAuth.removePresentLoginUser(request);
		return response;
	}
	
}
