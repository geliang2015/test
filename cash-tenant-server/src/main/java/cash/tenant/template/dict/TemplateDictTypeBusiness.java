package cash.tenant.template.dict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cash.tenant.app.BaseAppBusiness;
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
import jrain.ts.TableStoreService;
import jrain.ts.TableStoreUtils;
import jrain.utils.collection.CollectionUtils;
import jrain.utils.date.DateUtils;
import jrain.utils.lang.StringUtils;

/**
 * 字典类型
 * @author 马毅
 */
@Business(name = "TemplateDictTypeBusiness")
public class TemplateDictTypeBusiness{

	public static final String CASH_TENANT_ADD = "TemplateDictType.add";
	public static final String CASH_TENANT_REMOVE = "TemplateDictType.remove";
	public static final String CASH_TENANT_MODIFY = "TemplateDictType.modify";
	public static final String CASH_TENANT_QUERY = "TemplateDictType.query";
	public static final String CASH_TENANT_COUNT = "TemplateDictType.count";

	public SqlRunner getSqlRunner(){
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request){
		BusinessResponse response = new BusinessResponse();
		String dictCode = StringUtils.trimNull(request.getData("dictCode"));
		String dictName = StringUtils.trimNull(request.getData("dictName"));
		if(StringUtils.isEmpty(dictCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型编码不能为空！");
		}else if(dictCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(dictName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型名称不能为空！");
		}else if(dictName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型名称不能超过60个字符！");
		}else{
			//验证类型编码是否存在
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "dictCode", dictCode);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				BusinessUtils.setId(request.getData());
				request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("createTime", DateUtils.unixTime());
				request.putData("lastTime", DateUtils.unixTime());
				int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
				response.setData(num);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "类型编码已经存在！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request){
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		String dictCode = StringUtils.trimNull(request.getData("dictCode"));
		String dictName = StringUtils.trimNull(request.getData("dictName"));
		if(StringUtils.isEmpty(id)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else{
			//获取被修改的数据
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "id", id);
			Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(oldBean==null){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据已经被删除，请刷新后重试！");
			}else if(StringUtils.isEmpty(dictCode)){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型编码不能为空！");
			}else if(dictCode.length()>20){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型编码不能超过20个字符！");
			}else if(StringUtils.isEmpty(dictName)){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型名称不能为空！");
			}else if(dictName.length()>60){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "类型名称不能超过60个字符！");
			}else{
				//验证类型编码是否存在
				paramsMap = new HashMap<String, Object>();
				SqlUtils.addConditionNotEquals(paramsMap, "id", id);
				SqlUtils.addConditionEquals(paramsMap, "dictCode", dictCode);
				Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
				if(bean==null){
					SqlUtils.addConditionEquals(request.getData(), "id", id);
					request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("lastTime", DateUtils.unixTime());
					int num = getSqlRunner().update(CASH_TENANT_MODIFY, "default", request.getData());
					if(num>0){
						//修改后处理
						String preDictCode = StringUtils.trimNull(oldBean.get("dictCode"),"");
						if(!preDictCode.equals(dictCode)){//类别编码变更
							//修改数据项的类别编码
							Map<String, Object> updateItemMap = new HashMap<String, Object>();
							SqlUtils.addConditionEquals(updateItemMap, "dictId", id);
							updateItemMap.put("dictCode", dictCode);
							this.getSqlRunner().update(TemplateDictItemBusiness.CASH_TENANT_MODIFY, "default", updateItemMap);
						}
					}
					response.setData(num);
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "类型编码已经存在！");
				}
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request){
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.removeData(BusinessConst.DATA_ID));
		// 获取旧数据
		List<String> ids = CollectionUtils.valueOfList(id);
		int num = 0;
		if (ids.size()>0) {
			//验证是否有子节点
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionIn(paramsMap, "dictId", ids);
			Map<String, Object> existsBean = this.getSqlRunner().query(TemplateDictItemBusiness.CASH_TENANT_QUERY, "default", paramsMap);
			if(existsBean==null){
				SqlUtils.addConditionIn(request.getData(), "id", ids);
				num = getSqlRunner().delete(CASH_TENANT_REMOVE, "default", request.getData());
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "请先删除类型下的数据项！");
			}
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}
		response.setData(num);
		return response;
	}

	@BusinessMethod
	public BusinessResponse count(BusinessRequest request) {
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
	public BusinessResponse query(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		SqlUtils.addConditionEquals(request.getData(), "id", id);
		Map<String, Object> bean = getSqlRunner().query(CASH_TENANT_QUERY, "default", request.getData());
		response.setData(bean);
		return response;
	}

	@BusinessMethod
	public BusinessResponse list(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		PageObj<List<Map<String, Object>>> bean = getSqlRunner().listPage(CASH_TENANT_QUERY, "default", request.getData());
		response.setData(bean.getRows());
		response.setTotal(bean.getTotal());
		return response;
	}
	
	/**
	 * 同步模板字典数据到租户
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse synchronizeDictData(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String loginUserCode = StringUtils.trimNull(BaseTenantAuth.getPresentLoginUserCode(request));
		List<String> ids = CollectionUtils.valueOfList(StringUtils.trimNull(request.getData("id")));
		if(!"admin".equals(loginUserCode)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "您没有此权限！");
		}else if(ids!=null && ids.size()>0){
			//获取所有租户
			Map<String, Object> queryMap = new HashMap<String, Object>();
			List<Map<String, Object>> appList = this.getSqlRunner().list(BaseAppBusiness.CASH_TENANT_QUERY, "default", queryMap);
			if(appList!=null && appList.size()>0){//有租户
				//获取类型数据
				Map<String, Object> dicParamsMap = new HashMap<String, Object>();
				SqlUtils.addConditionIn(dicParamsMap, "id", ids);
				SqlUtils.setSqlOrder(dicParamsMap, "rowSort asc");
				List<Map<String, Object>> allDicTypeList = this.getSqlRunner().list(CASH_TENANT_QUERY, "default", dicParamsMap);
				//获取明细数据
				Map<String, Object> dicItemParamsMap = new HashMap<String, Object>();
				SqlUtils.addConditionIn(dicItemParamsMap, "dictId", ids);
				List<Map<String, Object>> allDicItemList = this.getSqlRunner().list(TemplateDictItemBusiness.CASH_TENANT_QUERY, "default", dicItemParamsMap);
				//将数据组装成类型对应的明细数据
				Map<String, List<Map<String, Object>>> allDicItemMap = new HashMap<String, List<Map<String, Object>>>();
				if(allDicItemList!=null && allDicItemList.size()>0){
					for(int i=0;i<allDicItemList.size();i++){
						Map<String, Object> dicItemMap = allDicItemList.get(i);
						if(dicItemMap!=null && dicItemMap.size()>0){
							String dictId = StringUtils.trimNull(dicItemMap.get("dictId"));
							if(!StringUtils.isEmpty(dictId)){
								List<Map<String, Object>> dictItemList = allDicItemMap.get(dictId);
								if(dictItemList==null){
									dictItemList = new ArrayList<Map<String, Object>>();
									allDicItemMap.put(dictId, dictItemList);
								}
								dictItemList.add(dicItemMap);
							}
						}
					}
				}
				//获取所有租户的字典类型数据,对应字典明细数据
				List<String> dictCodeList = new ArrayList<String>();
				if(allDicTypeList!=null && allDicTypeList.size()>0){
					for(int i=0;i<allDicTypeList.size();i++){
						Map<String, Object> dictTypeMap = allDicTypeList.get(i);
						if(dictTypeMap!=null && dictTypeMap.size()>0){
							String dictCode = StringUtils.trimNull(dictTypeMap.get("dictCode"));
							dictCodeList.add(dictCode);
						}
					}
				}
				List<Map<String, Object>> appDicTypeList = null;
				List<Map<String, Object>> appDicItemList = null;
				if(dictCodeList!=null && dictCodeList.size()>0){
					Map<String, Object> appDictParamsMap = new HashMap<String, Object>();
					SqlUtils.addConditionIn(appDictParamsMap, "dictCode", dictCodeList);
					appDicTypeList = this.getSqlRunner().list(CashConst.BASE_DICT_TYPE_QUERY, "default", appDictParamsMap);
					appDicItemList = this.getSqlRunner().list(CashConst.BASE_DICT_ITEM_QUERY, "default", appDictParamsMap);
				}
				Map<String, Map<String, Object>> appDicTypeMap = new HashMap<String, Map<String, Object>>();
				Map<String, Map<String, Object>> appDicItemMap = new HashMap<String, Map<String, Object>>();
				if(appDicTypeList!=null && appDicTypeList.size()>0){
					for(int i=0;i<appDicTypeList.size();i++){
						Map<String, Object> dictTypeMap = appDicTypeList.get(i);
						if(dictTypeMap!=null && dictTypeMap.size()>0){
							String appCode = StringUtils.trimNull(dictTypeMap.get("appCode"));
							String dictCode = StringUtils.trimNull(dictTypeMap.get("dictCode"));
							appDicTypeMap.put(appCode+dictCode, dictTypeMap);
						}
					}
				}
				if(appDicItemList!=null && appDicItemList.size()>0){
					for(int i=0;i<appDicItemList.size();i++){
						Map<String, Object> dictItemMap = appDicItemList.get(i);
						if(dictItemMap!=null && dictItemMap.size()>0){
							String appCode = StringUtils.trimNull(dictItemMap.get("appCode"));
							String dictCode = StringUtils.trimNull(dictItemMap.get("dictCode"));
							String itemKey = StringUtils.trimNull(dictItemMap.get("itemKey"));
							appDicItemMap.put(appCode+dictCode+itemKey, dictItemMap);
						}
					}
				}
				//开始同步：新增租户不存在的字典数据
				if(allDicTypeList!=null && allDicTypeList.size()>0){
					for(int i=0;i<allDicTypeList.size();i++){
						Map<String, Object> dictTypeMap = allDicTypeList.get(i);
						if(dictTypeMap!=null && dictTypeMap.size()>0){
							String oldId = StringUtils.trimNull(dictTypeMap.get("id"));
							String dictCode = StringUtils.trimNull(dictTypeMap.get("dictCode"));
							for(int j=0;j<appList.size();j++){
								Map<String, Object> appMap = appList.get(j);
								if(appMap!=null && appMap.size()>0){
									String appCode = StringUtils.trimNull(appMap.get("appCode"));
									Map<String, Object> dictTypeMapTemp = appDicTypeMap.get(appCode+dictCode);
									String dictTypeId = null;
									if(dictTypeMapTemp==null || dictTypeMapTemp.size()<1){
										dictTypeId = BusinessUtils.getId()+"";
										dictTypeMap.put("id", dictTypeId);
										dictTypeMap.put("appCode", appCode);
										//新增
										this.getSqlRunner().insert(CashConst.BASE_DICT_TYPE_ADD, "default", dictTypeMap);
									}else{
										dictTypeId = StringUtils.trimNull(dictTypeMapTemp.get("id"));
									}
									List<Map<String, Object>> dicItemList = allDicItemMap.get(oldId);
									if(dicItemList!=null && dicItemList.size()>0){
										for(int k=0;k<dicItemList.size();k++){
											Map<String, Object> dictItemMap = dicItemList.get(k);
											if(dictItemMap!=null && dictItemMap.size()>0){
												String itemKey = StringUtils.trimNull(dictItemMap.get("itemKey"));
												Map<String, Object> dictItemMapTemp = appDicItemMap.get(appCode+dictCode+itemKey);
												if(dictItemMapTemp==null || dictItemMapTemp.size()<1){
													dictItemMap.put("id", BusinessUtils.getId()+"");
													dictItemMap.put("dictId", dictTypeId);
													dictItemMap.put("appCode", appCode);
													this.getSqlRunner().insert(CashConst.BASE_DICT_ITEM_ADD, "default", dictItemMap);
												}
											}
										}
									}
								}
							}
						}
					}
					synchronizeDictDataToTableStore();
				}
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "请选择目标租户！");
			}
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}
		return response;
	}
	
	/**
	 * 同步租户字典数据到tableStore
	 * @return
	 */
	private boolean synchronizeDictDataToTableStore(){
		//获取所有租户的字典类型数据
		Map<String, Object> appDictTypeParamsMap = new HashMap<String, Object>();
		SqlUtils.setSqlOrder(appDictTypeParamsMap, "appCode asc,rowSort asc");
		List<Map<String, Object>> appDicTypeList = this.getSqlRunner().list(CashConst.BASE_DICT_TYPE_QUERY, "default", appDictTypeParamsMap);
		//获取所有租户的字典明细数据
		Map<String, Object> appDictItemParamsMap = new HashMap<String, Object>();
		SqlUtils.setSqlOrder(appDictItemParamsMap, "appCode asc,dictCode asc,rowSort asc");
		List<Map<String, Object>> appDicItemList = this.getSqlRunner().list(CashConst.BASE_DICT_ITEM_QUERY, "default", appDictItemParamsMap);
		//简单组装明细数据，便于取值存储
		Map<String, List<Map<String, Object>>> allAppDictItemMap = new HashMap<String, List<Map<String, Object>>>();
		if(appDicItemList!=null && appDicItemList.size()>0){
			for(int i=0;i<appDicItemList.size();i++){
				Map<String, Object> appDictItemMap = appDicItemList.get(i);
				if(appDictItemMap!=null && appDictItemMap.size()>0){
					String appCode = StringUtils.trimNull(appDictItemMap.get("appCode"));
					String dictCode = StringUtils.trimNull(appDictItemMap.get("dictCode"));
					String key = appCode+dictCode;
					List<Map<String, Object>> itemList = allAppDictItemMap.get(key);
					if(itemList==null){
						itemList = new ArrayList<Map<String, Object>>();
						allAppDictItemMap.put(key, itemList);
					}
					itemList.add(appDictItemMap);
				}
			}
		}
		//开始存储数据
		if(appDicTypeList!=null && appDicTypeList.size()>0){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String tableName = "dict_type_item";
			for(int i=0;i<appDicTypeList.size();i++){
				Map<String, Object> dictTypeMap = appDicTypeList.get(i);
				if(dictTypeMap!=null && dictTypeMap.size()>0){
					String appCode = StringUtils.trimNull(dictTypeMap.get("appCode"));
					String dictCode = StringUtils.trimNull(dictTypeMap.get("dictCode"));
					String primary = appCode+dictCode;
					List<Map<String, Object>> itemList = allAppDictItemMap.get(primary);
					if(itemList==null){
						itemList = new ArrayList<Map<String, Object>>();
					}
					Map<String, String> saveDataMap = new HashMap<String, String>();
					for(Map.Entry<String, Object> entry:dictTypeMap.entrySet()){
						String key = entry.getKey();
						String value = StringUtils.trimNull(entry.getValue());
						saveDataMap.put(key, value);
					}
					String itemListStr = FwUtils.getJsonService().toJson(itemList);
					saveDataMap.put("itemValue", itemListStr);
					String pk = appCode+"_"+dictCode;
					ts.put(tableName, pk, saveDataMap);
				}
			}
		}
		return true;
	}
	
}
