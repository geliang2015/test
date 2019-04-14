package cash.tenant.template.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cash.tenant.auth.BaseTenantAuth;
import cash.tenant.bean.CashConst;
import jrain.fw.business.base.Business;
import jrain.fw.business.base.BusinessMethod;
import jrain.fw.business.bean.BusinessConst;
import jrain.fw.business.bean.BusinessRequest;
import jrain.fw.business.bean.BusinessResponse;
import jrain.fw.business.utils.BusinessUtils;
import jrain.fw.dao.bean.PageObj;
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.dao.utils.SqlUtils;
import jrain.fw.utils.FwUtils;
import jrain.utils.collection.CollectionUtils;
import jrain.utils.date.DateUtils;
import jrain.utils.lang.StringUtils;

/**
 * 菜单
 * @author 马毅
 */
@Business(name = "TemplateMenuBusiness")
public class TemplateMenuBusiness {

	public static final String CASH_TENANT_ADD = "TemplateMenu.add";
	public static final String CASH_TENANT_REMOVE = "TemplateMenu.remove";
	public static final String CASH_TENANT_MODIFY = "TemplateMenu.modify";
	public static final String CASH_TENANT_QUERY = "TemplateMenu.query";
	public static final String CASH_TENANT_COUNT = "TemplateMenu.count";

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String menuCode = StringUtils.trimNull(request.getData("menuCode"));
		String menuName = StringUtils.trimNull(request.getData("menuName"));
		String menuPath = StringUtils.trimNull(request.getData("menuPath"));
		String menuIcon = StringUtils.trimNull(request.getData("menuIcon"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		if(StringUtils.isEmpty(menuCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单编码不能为空！");
		}else if(menuCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(menuName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单名称不能为空！");
		}else if(menuName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单名称不能超过60个字符！");
		}else if(!StringUtils.isEmpty(menuPath) && menuPath.length()>200){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单路径长度不能超过200个字符！");
		}else if(!StringUtils.isEmpty(menuIcon) && menuIcon.length()>100){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单图标长度不能超过100个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "备注长度不能超过300个字符！");
		}else{
			//验证菜单编码是否存在
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "menuCode", menuCode);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				BusinessUtils.setId(request.getData());
				request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("createTime", DateUtils.unixTime());
				request.putData("lastTime", DateUtils.unixTime());
				int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
				if(num>0){
					//修改pid对应的记录为非叶子节点
					paramsMap = new HashMap<String,Object>();
					SqlUtils.addConditionEquals(paramsMap, "id", request.getData("pid"));
					SqlUtils.addConditionNotEquals(paramsMap, "isLeaf", 2);
					paramsMap.put("isLeaf", 2);
					getSqlRunner().update(CASH_TENANT_MODIFY, "default", paramsMap);
				}
				response.setData(num);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "菜单编码已经存在！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		String menuCode = StringUtils.trimNull(request.getData("menuCode"));
		String menuName = StringUtils.trimNull(request.getData("menuName"));
		String menuPath = StringUtils.trimNull(request.getData("menuPath"));
		String menuIcon = StringUtils.trimNull(request.getData("menuIcon"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		//获取被修改的数据
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramsMap, "id", id);
		Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
		if(oldBean==null){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据已经被删除，请刷新后重试！");
		}else if(StringUtils.isEmpty(menuCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单编码不能为空！");
		}else if(menuCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(menuName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单名称不能为空！");
		}else if(menuName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单名称不能超过60个字符！");
		}else if(!StringUtils.isEmpty(menuPath) && menuPath.length()>200){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单路径长度不能超过200个字符！");
		}else if(!StringUtils.isEmpty(menuIcon) && menuIcon.length()>100){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "菜单路径长度不能超过100个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "备注长度不能超过300个字符！");
		}else{
			//验证菜单编码是否存在
			paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionNotEquals(paramsMap, "id", id);
			SqlUtils.addConditionEquals(paramsMap, "menuCode", menuCode);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				SqlUtils.addConditionEquals(request.getData(), "id", id);
				request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("lastTime", DateUtils.unixTime());
				int num = getSqlRunner().update(CASH_TENANT_MODIFY, "default", request.getData());
				response.setData(num);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "菜单编码已经存在！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		List<String> ids = CollectionUtils.valueOfList(id);
		int num = 0;
		if (ids.size()>0){
			//验证是否有子节点
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionIn(paramsMap, "pid", ids);
			Map<String, Object> son = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(son==null){
				SqlUtils.addConditionIn(request.getData(), "id", ids);
				num = this.getSqlRunner().delete(CASH_TENANT_REMOVE, "default", request.getData());
				if(num>0){
					//验证父节点是否有子节点
					String pid = StringUtils.trimNull(request.getData("pid"));
					if(!StringUtils.isEmpty(pid)){
						paramsMap = new HashMap<String, Object>();
						SqlUtils.addConditionEquals(paramsMap, "pid", pid);
						son = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
						if(son==null){//没有子节点了，修改当前父节点为叶子节点
							Map<String, Object> updateParams = new HashMap<String, Object>();
							SqlUtils.addConditionEquals(updateParams, "id", pid);
							updateParams.put("isLeaf", 1);
							this.getSqlRunner().update(CASH_TENANT_MODIFY, "default", updateParams);
						}
					}
				}
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "请先删除子菜单！");
			}
		}
		response.setData(num);
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse query(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		SqlUtils.addConditionEquals(request.getData(), "id", id);
		Map<String, Object> bean = getSqlRunner().query(CASH_TENANT_QUERY, "default", request.getData());
		response.setData(bean);
		return response;
	}

	@BusinessMethod
	public BusinessResponse count(BusinessRequest request){
		BusinessResponse response = new BusinessResponse();
		Map<String, Object> bean = getSqlRunner().query(CASH_TENANT_COUNT, "default", request.getData());
		Number count = null;
		if (bean != null) {
			count = (Number) bean.get("count");
		}
		response.setData(count == null ? 0 : count.intValue());
		return response;
	}

	@BusinessMethod
	public BusinessResponse list(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		PageObj<List<Map<String, Object>>> bean = getSqlRunner().listPage(CASH_TENANT_QUERY, "default", request.getData());
		List<Map<String, Object>> list = bean.getRows();
		response.setData(list);
		response.setTotal(bean.getTotal());
		return response;
	}
	
	/**
	 * 获取所有的菜单
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse listAll(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		List<Map<String, Object>> list = getSqlRunner().list(CASH_TENANT_QUERY, "default", request.getData());
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("id", 0);
		root.put("pid", -1);
		root.put("menuCode", "root");
		root.put("menuName", "根菜单");
		list.add(0, root);
		response.setData(list);
		response.setTotal(list.size());
		return response;
	}
	
	/**
	 * 复制菜单-将所有菜单复制给指定的系统租户
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse copyMenuData(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		if(StringUtils.isEmpty(appCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "租户编码不能为空！");
			return response;
		}
		//获取当前系统所有的菜单数据
		Map<String, Object> menuParamsMap = new HashMap<String, Object>();
		SqlUtils.setSqlOrder(menuParamsMap, "rowSort asc");
		List<Map<String, Object>> allMenuList = getSqlRunner().list(CASH_TENANT_QUERY, "default", menuParamsMap);
		//简单处理数据为map，便于通过pid获取子菜单列表
		Map<String,List<Map<String, Object>>> allMenuMapByPid = new HashMap<String,List<Map<String, Object>>>();
		if(allMenuList!=null && allMenuList.size()>0){
			for(int i=0;i<allMenuList.size();i++){
				Map<String, Object> memuMap = allMenuList.get(i);
				String pid = StringUtils.trimNull(memuMap.get("pid"),"");
				List<Map<String, Object>> sonMenuList = allMenuMapByPid.get(pid);
				if(sonMenuList==null){
					sonMenuList = new ArrayList<Map<String, Object>>();
				}
				sonMenuList.add(memuMap);
				allMenuMapByPid.put(pid, sonMenuList);
			}
		}
		Map<String, Object> menuParam = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(menuParam, "appCode", appCode);
		//获取目标租户菜单
		List<Map<String,Object>> menuList = this.getSqlRunner().list(CashConst.BASE_MENU_QUERY, "default", menuParam);
		Map<String, String> allMenuMap = new HashMap<String, String>();
		if(menuList!=null && menuList.size()>0){
			for(int i=0;i<menuList.size();i++){
				Map<String, Object> menuMap = menuList.get(i);
				if(menuMap!=null && menuMap.size()>0){
					String menuId = StringUtils.trimNull(menuMap.get("id"));
					String menuCode = StringUtils.trimNull(menuMap.get("menuCode"));
					allMenuMap.put(menuCode, menuId);
				}
			}
		}
		//清除目标租户菜单
		getSqlRunner().delete(CashConst.BASE_MENU_REMOVE, "default", menuParam);
		this.copyMenu(allMenuMapByPid, "0", "0", appCode, allMenuMap);
		return response;
	}
	
	private void copyMenu(Map<String,List<Map<String, Object>>> allMenuMapByPid,
			String pid, String newPid, String appCode, Map<String, String> allMenuMap){
		
		if(allMenuMapByPid!=null && allMenuMapByPid.size()>0){
			List<Map<String, Object>> sonList = allMenuMapByPid.get(pid);
			if(sonList!=null && sonList.size()>0){
				for(int i=0;i<sonList.size();i++){
					Map<String, Object> menuMap = sonList.get(i);
					if(menuMap!=null && menuMap.size()>0){
						String menuCode = StringUtils.trimNull(menuMap.get("menuCode"));
						String menuId = allMenuMap.get(menuCode);
						if(StringUtils.isEmpty(menuId)){
							menuId = BusinessUtils.getId()+"";
						}
						String oldId = StringUtils.trimNull(menuMap.get("id"));
						menuMap.put("id", menuId);
						menuMap.put("pid", newPid);
						menuMap.put("appCode", appCode);
						//保存数据
						int num = getSqlRunner().insert(CashConst.BASE_MENU_ADD, "default", menuMap);
						if(num>0){
							this.copyMenu(allMenuMapByPid, oldId, menuId, appCode, allMenuMap);
						}
					}
				}
			}
		}
	}
	
}
