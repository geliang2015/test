package cash.tenant.app;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.ColumnValue;
import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.GetRangeRequest;
import com.alicloud.openservices.tablestore.model.GetRangeResponse;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.RangeRowQueryCriteria;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.RowUpdateChange;
import com.alicloud.openservices.tablestore.model.UpdateRowRequest;

import cash.auth.base.FeeConst;
import cash.tenant.auth.BaseTenantAuth;
import cash.tenant.bean.CashConst;
import cash.tenant.template.auth.TemplateAuthBusiness;
import cash.tenant.template.dict.TemplateDictItemBusiness;
import cash.tenant.template.dict.TemplateDictTypeBusiness;
import cash.tenant.template.menu.TemplateMenuBusiness;
import jrain.fw.business.base.Business;
import jrain.fw.business.base.BusinessMethod;
import jrain.fw.business.bean.BusinessConst;
import jrain.fw.business.bean.BusinessRequest;
import jrain.fw.business.bean.BusinessResponse;
import jrain.fw.business.utils.BusinessUtils;
import jrain.fw.dao.bean.PageObj;
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.dao.utils.SqlUtils;
import jrain.fw.log.base.Logger;
import jrain.fw.log.base.LoggerFactory;
import jrain.fw.utils.FwUtils;
import jrain.ts.DataBean;
import jrain.ts.TableStoreService;
import jrain.ts.TableStoreUtils;
import jrain.utils.cipher.AesUtils;
import jrain.utils.cipher.RsaUtils;
import jrain.utils.collection.CollectionUtils;
import jrain.utils.date.DateUtils;
import jrain.utils.lang.LangUtils;
import jrain.utils.lang.StringUtils;

@Business(name = "BaseAppBusiness")
public class BaseAppBusiness { 

	public static final String CASH_TENANT_ADD = "BaseApp.add";
	public static final String CASH_TENANT_REMOVE = "BaseApp.remove";
	public static final String CASH_TENANT_MODIFY = "BaseApp.modify";
	public static final String CASH_TENANT_QUERY = "BaseApp.query";
	public static final String CASH_TENANT_COUNT = "BaseApp.count";
	
	private static final Logger logger = LoggerFactory.getLogger(BaseAppBusiness.class);

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		String appName = StringUtils.trimNull(request.getData("appName"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		if(StringUtils.isEmpty(appCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "租户编码不能为空！");
		}else if(appCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "租户编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(appName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "租户名称不能为空！");
		}else if(appName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "租户名称不能超过60个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备注不能超过300个字符！");
		}else{
			//验证租户编码是否存在
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "appCode", appCode);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				long id = BusinessUtils.getId();
				request.putData("id", id);
				String chiper = AesUtils.getAesKeyStr();
				request.putData("chiper", chiper);
				request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("createTime", DateUtils.unixTime());
				request.putData("lastTime", DateUtils.unixTime());
				int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
				if (num>0){
					//添加超级管理员
					this.addAppTenantSuperUser(request, appCode, chiper);
					//初始化系统菜单
					this.initAppTenantMenu(request, appCode);
					// 初始化系统配置
					this.initAppTenantDic(request, appCode);
					//
					this.initAppCipher(request, id+"", appCode);
					//
					this.initBaseAuth(request, id+"", appCode);
				}
				response.setData(num);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "租户编码已经存在！");
			}
		}
		return response;
	}
	
	/**
	 * 创建租户的超级管理员
	 * @param request
	 * @param appCode
	 * @return
	 */
	private boolean addAppTenantSuperUser(BusinessRequest request, String appCode, String chiper) {
		//清除租户可能存在的用户
		String deleteSql = " delete from base_user where app_code=? ";
		this.getSqlRunner().execSql(new HashMap<String, Object>(), deleteSql, appCode);
		//创建租户
		StringBuilder sb = new StringBuilder();
		sb.append("insert into base_user(");
		sb.append("id,app_code,user_code,user_name,pwd,salt,can_remove,status,row_sort,create_user,create_time,last_user,last_time");
		sb.append(") values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
		long id = BusinessUtils.getId();
		String userCode = appCode;
		String userName = "超级管理员";
		String pwd = "123456";
		Random random = new Random();
		int salt = 100000 + random.nextInt(999999);
		pwd = AesUtils.encrypt(pwd + salt, chiper);
		int canRemove = 2;
		int status = 1;
		int rowSort = 1000;
		Object createUser = BaseTenantAuth.getPresentLoginUserCode(request);
		Object createTime = DateUtils.unixTime();
		Object lastUser = createUser;
		Object lastTime = createTime;
		String sql = sb.toString();
		Map<String, Object> paramData = new HashMap<String, Object>();
		int num = this.getSqlRunner().execSql(paramData, sql, id, appCode, userCode, userName, pwd,salt,
				canRemove, status, rowSort, createUser, createTime, lastUser, lastTime);
		if (num > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 初始化租户系统菜单
	 * @param request
	 * @param appCode
	 * @return
	 */
	private boolean initAppTenantMenu(BusinessRequest request, String appCode) {
		if (!StringUtils.isEmpty(appCode)) {
			// 获取系统所有的菜单数据
			Map<String, Object> menuParamsMap = new HashMap<String, Object>();
			SqlUtils.setSqlOrder(menuParamsMap, "rowSort asc");
			List<Map<String, Object>> allMenuList = this.getSqlRunner().list(TemplateMenuBusiness.CASH_TENANT_QUERY, "default", menuParamsMap);
			// 简单处理数据为map，便于通过pid获取子菜单列表
			Map<String, List<Map<String, Object>>> allMenuMapByPid = new HashMap<String, List<Map<String, Object>>>();
			if (allMenuList != null && allMenuList.size() > 0) {
				for (int i = 0; i < allMenuList.size(); i++) {
					Map<String, Object> memuMap = allMenuList.get(i);
					String pid = StringUtils.trimNull(memuMap.get("pid"), "");
					List<Map<String, Object>> sonMenuList = allMenuMapByPid.get(pid);
					if (sonMenuList == null) {
						sonMenuList = new ArrayList<Map<String, Object>>();
					}
					sonMenuList.add(memuMap);
					allMenuMapByPid.put(pid, sonMenuList);
				}
			}
			// 清除可能存在的菜单数据
			Map<String, Object> delParamsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(delParamsMap, "appCode", appCode);
			this.getSqlRunner().delete(CashConst.BASE_MENU_REMOVE, "default", delParamsMap);
			// 新建租户菜单
			this.copyMenu(allMenuMapByPid, "0", "0", appCode);
		}
		return true;
	}

	/**
	 * 保存新租户菜单
	 * @param allMenuMapByPid
	 * @param pid
	 * @param newPid
	 * @param appCode
	 * @param tenantCode
	 */
	private void copyMenu(Map<String, List<Map<String, Object>>> allMenuMapByPid,
			String pid, String newPid,String appCode) {

		if (allMenuMapByPid != null && allMenuMapByPid.size() > 0) {
			List<Map<String, Object>> sonList = allMenuMapByPid.get(pid);
			if (sonList != null && sonList.size() > 0) {
				for (int i = 0; i < sonList.size(); i++) {
					Map<String, Object> menuMap = sonList.get(i);
					if (menuMap != null && menuMap.size() > 0) {
						String id = BusinessUtils.getId() + "";
						String oldId = StringUtils.trimNull(menuMap.get("id"));
						menuMap.put("id", id);
						menuMap.put("pid", newPid);
						menuMap.put("appCode", appCode);
						// 保存数据
						int num = this.getSqlRunner().insert(CashConst.BASE_MENU_ADD, "default", menuMap);
						if (num > 0) {
							this.copyMenu(allMenuMapByPid, oldId, id, appCode);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 初始化租户系统字典
	 * @param request
	 * @param appCode
	 * @param tenantCode
	 * @return
	 */
	private boolean initAppTenantDic(BusinessRequest request, String appCode) {
		if (!StringUtils.isEmpty(appCode)) {
			// 获取系统字典类别数据数据
			Map<String, Object> dicParamsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(dicParamsMap, "appCode", appCode);
			SqlUtils.setSqlOrder(dicParamsMap, "rowSort asc");
			List<Map<String, Object>> allDicTypeList = this.getSqlRunner().list(TemplateDictTypeBusiness.CASH_TENANT_QUERY,"default", dicParamsMap);
			// 获取明细数据
			List<Map<String, Object>> allDicItemList = this.getSqlRunner().list(TemplateDictItemBusiness.CASH_TENANT_QUERY,"default", dicParamsMap);
			Map<String, List<Map<String, Object>>> allDicItemMap = new HashMap<String, List<Map<String, Object>>>();
			if (allDicItemList != null && allDicItemList.size() > 0) {
				for (int i = 0; i < allDicItemList.size(); i++) {
					Map<String, Object> dicItemMap = allDicItemList.get(i);
					if (dicItemMap != null && dicItemMap.size() > 0) {
						String dictId = StringUtils.trimNull(dicItemMap.get("dictId"));
						if (!StringUtils.isEmpty(dictId)) {
							List<Map<String, Object>> dictItemList = allDicItemMap.get(dictId);
							if (dictItemList == null) {
								dictItemList = new ArrayList<Map<String, Object>>();
								allDicItemMap.put(dictId, dictItemList);
							}
							dictItemList.add(dicItemMap);
						}
					}
				}
			}
			//清除可能存在的数据
			Map<String, Object> delParamsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(delParamsMap, "appCode", appCode);
			this.getSqlRunner().delete(CashConst.BASE_DICT_ITEM_REMOVE, "default", delParamsMap);
			this.getSqlRunner().delete(CashConst.BASE_DICT_TYPE_REMOVE, "default", delParamsMap);

			//初始化到当前租户
			if (allDicTypeList != null && allDicTypeList.size() > 0) {
				for (int i = 0; i < allDicTypeList.size(); i++) {
					Map<String, Object> dictTypeMap = allDicTypeList.get(i);
					if (dictTypeMap != null && dictTypeMap.size() > 0) {
						String id = BusinessUtils.getId() + "";
						String oldId = StringUtils.trimNull(dictTypeMap.get("id"));
						dictTypeMap.put("id", id);
						dictTypeMap.put("appCode", appCode);
						int num = this.getSqlRunner().insert(CashConst.BASE_DICT_TYPE_ADD, "default", dictTypeMap);
						if (num > 0) {
							List<Map<String, Object>> dicItemList = allDicItemMap.get(oldId);
							if (dicItemList != null && dicItemList.size() > 0) {
								for (int j = 0; j < dicItemList.size(); j++) {
									Map<String, Object> dictItemMap = dicItemList.get(j);
									if (dictItemMap != null && dictItemMap.size() > 0) {
										dictItemMap.put("id", BusinessUtils.getId() + "");
										dictItemMap.put("dictId", id);
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
		return true;
	}
	
	private boolean initAppCipher(BusinessRequest request,String appId,String appCode){
		long id = BusinessUtils.getId();
		KeyPair keyPair = RsaUtils.getKeyPair();
		String accessKey = LangUtils.randomUUID().replace("-", "");
		String accessPublic = RsaUtils.getPublicKeyStr(keyPair);
		String accessSecret = RsaUtils.getPrivateKeyStr(keyPair);
		int status = 1;
		int rowSort = 1000;
		Object createUser = BaseTenantAuth.getPresentLoginUserCode(request);
		Object createTime = DateUtils.unixTime();
		Object lastUser = createUser;
		Object lastTime = createTime;
		String sql = "insert into base_app_cipher(id,app_id,app_code,access_key,access_public,access_secret,status,row_sort,create_user,create_time,last_user,last_time) values(?,?,?,?,?,?,?,?,?,?,?,?)";
		Map<String, Object> paramData = new HashMap<String, Object>();
		int num = this.getSqlRunner().execSql(paramData, sql, id, appId, appCode, accessKey, 
				accessPublic,accessSecret,status, rowSort, createUser, createTime, lastUser, lastTime);
		if (num > 0) {
			//同步到tablestore
			String pk = appId+"_"+accessKey;
			Map<String, String> saveDataMap = new HashMap<String, String>();
			saveDataMap.put("appId",  appId);
			saveDataMap.put("appCode", appCode);
			saveDataMap.put("accessPublic", accessPublic);
			TableStoreUtils.getTableStoreService().put("base_app_chiper", pk, saveDataMap);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean initBaseAuth(BusinessRequest request,String appId,String appCode){
		if(!StringUtils.isEmpty(appCode)){
			String sql = "select * from template_auth";
			List<Map<String, Object>> list = this.getSqlRunner().listBySql(request.getData(), sql);
			if(list!=null && list.size()>0){
				for(int i=0;i<list.size();i++){
					Map<String, Object> map = list.get(i);
					if(map!=null && map.size()>0){
						long id = BusinessUtils.getId();
						String authCode = StringUtils.trimNull(map.get("authCode"));
						String authTitle = StringUtils.trimNull(map.get("authTitle"));
						String authBody = StringUtils.trimNull(map.get("authBody"));
						String requires = StringUtils.trimNull(map.get("requires"));
						String status = StringUtils.trimNull(map.get("status"));
						String rowSort = StringUtils.trimNull(map.get("rowSort"));
						Object createUser = BaseTenantAuth.getPresentLoginUserCode(request);
						Object createTime = DateUtils.unixTime();
						Object lastUser = createUser;
						Object lastTime = createTime;
						String insertSql = "insert into base_auth(id,app_code,auth_code,auth_title,auth_body,requires,status,row_sort,create_user,create_time,last_user,last_time) values(?,?,?,?,?,?,?,?,?,?,?,?)";
						Map<String, Object> paramData = new HashMap<String, Object>();
						this.getSqlRunner().execSql(paramData, insertSql, id, appCode, authCode, authTitle, 
								authBody,requires,status, rowSort, createUser, createTime, lastUser, lastTime);
					}
				}
			}
			return true;
		}else{
			return false;
		}
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		//验证修改数据
		String id = StringUtils.trimNull(request.getData("id"));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramsMap, "id", id);
		Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
		if(oldBean!=null){
			request.removeData("chiper");
			String appCode = StringUtils.trimNull(oldBean.get("appCode"));
			request.putData("appCode", appCode);
			request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
			request.putData("lastTime", DateUtils.unixTime());
			SqlUtils.addConditionEquals(request.getData(), BusinessConst.DATA_ID, request.getData(BusinessConst.DATA_ID));
			int num = getSqlRunner().update(CASH_TENANT_MODIFY, "default", request.getData());
			response.setData(num);
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "被修改数据不存在，请刷新后核对！");
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.removeData("id"));
		// 获取旧数据
		List<String> ids = CollectionUtils.valueOfList(id);
		int num = 0;
		if (ids.size() > 0) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			SqlUtils.addConditionIn(paramMap, "id", ids);
			//获取删除的数据
			List<Map<String, Object>> appList = this.getSqlRunner().list(CASH_TENANT_QUERY, "default", paramMap);
			if(appList!=null && appList.size()>0){
				List<String> appCodes = new ArrayList<String>();
				for(int i=0;i<appList.size();i++){
					Map<String, Object> appMap = appList.get(i);
					if(appMap!=null && appMap.size()>0){
						String appCode = StringUtils.trimNull(appMap.get("appCode"));
						appCodes.add(appCode);
					}
				}
				num = getSqlRunner().delete(CASH_TENANT_REMOVE, "default", paramMap);
				if(num>0){
					//删除当前租户的超级管理员
					String sql = "delete from base_user where app_code=? and user_code=? ";
					paramMap = new HashMap<String, Object>();
					for(int i=0;i<appCodes.size();i++){
						this.getSqlRunner().execSql(paramMap, sql, appCodes.get(i), appCodes.get(i));
					}
					paramMap = new HashMap<String, Object>();
					SqlUtils.addConditionIn(paramMap, "appCode", appCodes);
					//删除租户系统菜单
					this.getSqlRunner().delete(CashConst.BASE_MENU_REMOVE, "default", paramMap);
					//删除租户系统配置
					List<Map<String, Object>> dictTypeList = this.getSqlRunner().list(CashConst.BASE_DICT_TYPE_QUERY, "default", paramMap);
					TableStoreService ts = TableStoreUtils.getTableStoreService();
					String tableName = "dict_type_item";
					if(dictTypeList!=null && dictTypeList.size()>0){
						for(int i=0;i<dictTypeList.size();i++){
							Map<String, Object> dictMap = dictTypeList.get(i);
							if(dictMap!=null && dictMap.size()>0){
								String appCode = StringUtils.trimNull(dictMap.get("appCode"));
								String dictCode = StringUtils.trimNull(dictMap.get("dictCode"));
								ts.remove(tableName, appCode+"_"+dictCode);
							}
						}
					}
					this.getSqlRunner().delete(CashConst.BASE_DICT_ITEM_REMOVE, "default", paramMap);
					this.getSqlRunner().delete(CashConst.BASE_DICT_TYPE_REMOVE, "default", paramMap);
					//
					sql = "delete from base_app_cipher where app_code=? ";
					String cipherSql = "select * from base_app_cipher where app_code=? ";
					paramMap = new HashMap<String, Object>();
					for(int i=0;i<appList.size();i++){
						Map<String, Object> appMap = appList.get(i);
						if(appMap!=null && appMap.size()>0){
							String appCode = StringUtils.trimNull(appMap.get("appCode"));
							String appId = StringUtils.trimNull(appMap.get("id"));
							Map<String, Object> cipherMap = this.getSqlRunner().queryBySql(paramMap, cipherSql, appCode);
							this.getSqlRunner().execSql(paramMap, sql, appCode);
							if(cipherMap!=null && cipherMap.size()>0){
								String accessKey = StringUtils.trimNull(cipherMap.get("accessKey"));
								String pk = appId+"_"+accessKey;
								TableStoreUtils.getTableStoreService().remove("base_app_chiper", pk);
							}
						}
					}
					//
					sql = "delete from base_auth where app_code=? ";
					paramMap = new HashMap<String, Object>();
					for(int i=0;i<appCodes.size();i++){
						this.getSqlRunner().execSql(paramMap, sql, appCodes.get(i));
					}
					//
					tableName = "acct_base";
					for(int i=0;i<appCodes.size();i++){
						ts.remove(tableName, appCodes.get(i));
					}
				}
			}
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
	
	@SuppressWarnings("unused")
	private void kouFeiData(){
		String sql = "SELECT item_key,item_value FROM base_dict_item WHERE app_code='zhcxs' AND dict_code='MoneyItem' ";
		List<Map<String, Object>> list = this.getSqlRunner().listBySql(new HashMap<>(), sql);
		Map<String, String> all = new HashMap<String, String>();
		if(list!=null && list.size()>0){
			for(int i=0;i<list.size();i++){
				Map<String, Object> temp = list.get(i);
				if(temp!=null && temp.size()>0){
					String itemKey = StringUtils.trimNull(temp.get("itemKey"));
					String itemValue = StringUtils.trimNull(temp.get("itemValue"));
					all.put(itemKey, itemValue);
				}
			}
		}
		TableStoreService ts = TableStoreUtils.getTableStoreService();
		String startPk = "zhcxs_";
		String endPk = "zhcxs_zzzzzzzzzz";
		String tableName = "acct_out";
		List<Map<String, String>> listAll = new ArrayList<Map<String, String>>();
		this.getTableStoreData(ts, tableName, startPk, endPk, listAll);
		if(listAll!=null && listAll.size()>0){
			for(int i=0;i<listAll.size();i++){
				Map<String, String> temp = listAll.get(i);
				if(temp!=null && temp.size()>0){
					String pk = temp.get("pk");
					String feeType = temp.get("feeType");
					String money = all.get(feeType);
					if(!StringUtils.isEmpty(money)){
						Map<String, String> saveMap = new HashMap<String,String>();
						saveMap.put("money", money);
						ts.put(tableName, pk, saveMap);
						System.out.println(saveMap);
					}
				}
			}
		}
	}

	@BusinessMethod
	public BusinessResponse query(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		SqlUtils.addConditionEquals(request.getData(), BusinessConst.DATA_ID, id);
		Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", request.getData());
		response.setData(bean);
		return response;
	}
	
	/**
	 * 同步数据
	 */
	@SuppressWarnings("unused")
	private void synchronize(){
		String endPoint = "https://cash.cn-qingdao.ots.aliyuncs.com";
		String accessId = "LTAIMsry4u7OvOdp";
		String accessKey = "K1VeaIbE71D81z09qwYwZwQEueGHO6";
		String instanceName = "cash";
		SyncClient queryClient = new SyncClient(endPoint, accessId, accessKey, instanceName);
		queryClient.listTable();
		
		endPoint = "https://cash-saas.cn-qingdao.ots.aliyuncs.com";
		accessId = "LTAIMsry4u7OvOdp";
		accessKey = "K1VeaIbE71D81z09qwYwZwQEueGHO6";
		instanceName = "cash-saas";
		SyncClient saveClient = new SyncClient(endPoint, accessId, accessKey, instanceName);
		saveClient.listTable();
		synchronizeTableStoreData(queryClient, saveClient, "auth_ud_base", "548629632828575744", "zzzzzzzzzzzzzzzzzzzzzzzzz");
		queryClient.shutdown();
		saveClient.shutdown();
	}
	
	private void synchronizeTableStoreData(SyncClient queryClient,SyncClient saveClient,String tableName,String startPk,String endPk){
		List<Map<String, String>> list = this.listTableStoreData(queryClient, tableName, startPk, endPk);
		if(list!=null && list.size()>0){
			for(int i=0;i<list.size();i++){
				Map<String, String> map = list.get(i);
				if(map!=null && map.size()>0){
					String userId = map.get("pk");
					startPk = userId;
					Map<String, String> saveMap = new HashMap<String, String>();
					saveMap.put("image", StringUtils.trimNull(map.get("draw")));
					saveMap.put("value", StringUtils.trimNull(map.get("value")));
					saveMap.put("createTime", DateUtils.unixTime()+"");
					String pk = "cnjfd"+"_"+userId;
					this.putTableStore(saveClient, tableName, pk, saveMap);
					if(saveMap.get("image").length()>500){
						logger.error("同步数据："+pk+"===="+saveMap.get("image").substring(0,500));
					}else{
						logger.error("同步数据："+pk+"===="+saveMap.get("image"));
					}
				}
			}
			if(list.size()>1){
				this.synchronizeTableStoreData(queryClient, saveClient, tableName, startPk, endPk);
			}else{
				logger.error("同步数据结束");
			}
		}
	}
	
	private List<Map<String, String>> listTableStoreData(SyncClient queryClient,String tableName,String startPk,String endPk){
		RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(tableName);
		PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
		primaryKeyBuilder.addPrimaryKeyColumn("pk", PrimaryKeyValue.fromString(startPk));
		rangeRowQueryCriteria.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());
		primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
		primaryKeyBuilder.addPrimaryKeyColumn("pk", PrimaryKeyValue.fromString(endPk));
		rangeRowQueryCriteria.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());
		rangeRowQueryCriteria.setDirection(Direction.FORWARD);
		rangeRowQueryCriteria.setMaxVersions(1);
		rangeRowQueryCriteria.setLimit(10);
		List<Map<String, String>> rsList = new ArrayList<Map<String, String>>();
		GetRangeResponse getRangeResponse = queryClient.getRange(new GetRangeRequest(rangeRowQueryCriteria));
		for (Row row : getRangeResponse.getRows()) {
			if (row != null) {
				Column[] columns = row.getColumns();
				Map<String, String> rsMap = new HashMap<>();
				for (Column column : columns) {
					rsMap.put(column.getName(), getValue(column.getValue()));
				}
				rsMap.put("pk", row.getPrimaryKey().getPrimaryKeyColumn("pk").getValue().asString());
				rsList.add(rsMap);
			}
		}
		return rsList;
	}
	
	private boolean putTableStore(SyncClient saveClient,String tableName,String pk,Map<String, String> saveMap){
		PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
		primaryKeyBuilder.addPrimaryKeyColumn("pk", PrimaryKeyValue.fromString(pk));
		PrimaryKey primaryKey = primaryKeyBuilder.build();
		RowUpdateChange rowUpdateChange = new RowUpdateChange(tableName, primaryKey);
		Set<String> columns = saveMap.keySet();
		for (String column : columns) {
			String columnValue = saveMap.get(column);
			ColumnValue value = ColumnValue.fromString(columnValue);
			rowUpdateChange.put(new Column(column, value, System.currentTimeMillis()));
		}
		saveClient.updateRow(new UpdateRowRequest(rowUpdateChange));
		return true;
	}
	
	public String getValue(ColumnValue value) {
		if (value == null) {
			return "";
		}
		String str = value.toString();
		if ("null".equals(str)) {
			return "";
		}
		return str;
	}

	@BusinessMethod
	public BusinessResponse list(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		PageObj<List<Map<String, Object>>> bean = getSqlRunner().listPage(CASH_TENANT_QUERY, "default", request.getData());
		List<Map<String, Object>> list = bean.getRows();
		if(list!=null && list.size()>0){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String tableName = "acct_base";
			for(int i=0;i<list.size();i++){
				Map<String, Object> map = list.get(i);
				if(map!=null){
					String appCode = StringUtils.trimNull(map.get("appCode"));
					Map<String, String> account = ts.get(tableName, appCode);
					String cash = null;
					String total = null;
					if(account!=null && account.size()>0){
						cash = StringUtils.trimNull(account.get("cash"));
						try {
							Double.parseDouble(cash);
						} catch (Exception e) {
							cash = null;
						}
						total = StringUtils.trimNull(account.get("total"));
						try {
							Double.parseDouble(total);
						} catch (Exception e) {
							total = null;
						}
					}
					if(StringUtils.isEmpty(cash)){
						cash = "0";
					}
					if(StringUtils.isEmpty(total)){
						total = "0";
					}
					map.put("cash", cash);
					map.put("total", total);
				}
			}
		}
		response.setData(bean.getRows());
		response.setTotal(bean.getTotal());
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse listAuthCodeByAppCode(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if(!StringUtils.isEmpty(appCode)){
			String sql = "SELECT auth_code FROM base_auth WHERE app_code=? ORDER BY row_sort";
			list = this.getSqlRunner().listBySql(request.getData(), sql, appCode);
		}
		response.setData(list);
		response.setTotal(list.size());
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse saveAppCodeAuth(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		String authCodes = StringUtils.trimNull(request.getData("authCode"));
		List<String> authCodeList = CollectionUtils.valueOfList(authCodes);
		if(!StringUtils.isEmpty(appCode)){
			//清除租户认证数据
			Map<String, Object> paramMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramMap, "appCode", appCode);
			String sql = " delete from base_auth where app_code=? ";
			this.getSqlRunner().execSql(paramMap, sql, appCode);
			//保存新的数据
			if(authCodeList!=null && authCodeList.size()>0){
				//获取认证数据
				paramMap = new HashMap<String, Object>();
				SqlUtils.addConditionIn(paramMap, "authCode", authCodeList);
				List<Map<String, Object>> list = this.getSqlRunner().list(TemplateAuthBusiness.CASH_TENANT_QUERY, "default", paramMap);
				if(list!=null && list.size()>0){
					for(int i=0;i<list.size();i++){
						Map<String, Object> map = list.get(i);
						if(map!=null && map.size()>0){
							long id = BusinessUtils.getId();
							String authCode = StringUtils.trimNull(map.get("authCode"));
							String authTitle = StringUtils.trimNull(map.get("authTitle"));
							String authBody = StringUtils.trimNull(map.get("authBody"));
							String requires = StringUtils.trimNull(map.get("requires"));
							String status = StringUtils.trimNull(map.get("status"));
							String rowSort = StringUtils.trimNull(map.get("rowSort"));
							Object createUser = BaseTenantAuth.getPresentLoginUserCode(request);
							Object createTime = DateUtils.unixTime();
							Object lastUser = createUser;
							Object lastTime = createTime;
							String insertSql = "insert into base_auth(id,app_code,auth_code,auth_title,auth_body,requires,status,row_sort,create_user,create_time,last_user,last_time) values(?,?,?,?,?,?,?,?,?,?,?,?)";
							Map<String, Object> paramData = new HashMap<String, Object>();
							this.getSqlRunner().execSql(paramData, insertSql, id, appCode, authCode, authTitle, 
									authBody,requires,status, rowSort, createUser, createTime, lastUser, lastTime);
						}
					}
				}
			}
		}
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse queryAccount(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		TableStoreService ts = TableStoreUtils.getTableStoreService();
		String tableName = "acct_base";
		Map<String, String> account = ts.get(tableName, appCode);
		if(account==null || account.size()<1){
			account = new HashMap<String, String>();
			account.put("appCode", appCode);
			account.put("total", "0");
			account.put("cash", "0");
			account.put("version", "");
		}
		response.setData(account);
		return response;
	}
	
	/**
	 * 账户充值
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse recharge(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		String cash = StringUtils.trimNull(request.getData("cash"));
		String version = StringUtils.trimNull(request.getData("version"));
		if(StringUtils.isEmpty(appCode)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramMap, "appCode", appCode);
			Map<String, Object> bean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramMap);
			if(bean==null){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "当前充值租户可能已经被删除，请刷新后重试！");
			}else if(StringUtils.isEmpty(cash)){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "充值金额不能为空！");
			}else{
				Long je = null;
				try {
					je = Long.parseLong(cash);
				} catch (Exception e) {
					je = null;
				}
				if(je==null){
					response.setCodeAndMsg(BusinessConst.CODE_PARAM, "充值金额必须是整数！");
				}else if(je<1 || je>100000){
					response.setCodeAndMsg(BusinessConst.CODE_PARAM, "单次充值金额必须在1与100000之间！");
				}else{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					String time = sdf.format(new Date());
					String appName = StringUtils.trimNull(bean.get("appName"));
					//获取账户数据
					TableStoreService ts = TableStoreUtils.getTableStoreService();
					String tableName = "acct_base";
					Map<String, String> account = ts.get(tableName, appCode);
					if(account!=null && account.size()>0){
						try {
							String oldVersion = StringUtils.trimNull(account.get("version"));
							if(oldVersion.equals(version)){
								//更新账户数据
								Map<String, String> saveMap = new HashMap<String, String>();
								saveMap.put("appCode", appCode);
								saveMap.put("appName", appName);
								saveMap.put("version", BusinessUtils.getId()+"");
								saveMap.put("lastUser", BaseTenantAuth.getPresentLoginUserCode(request)+"");
								saveMap.put("lastTime", time);
								ts.put(tableName, appCode, saveMap);
								//添加充值记录
								String pk = appCode+"_"+time+"_"+FwUtils.getIdService().nextId();
								Map<String, String> saveRecordMap = new HashMap<String, String>();
								saveRecordMap.put("appCode", appCode);
								saveRecordMap.put("money", cash);
								saveRecordMap.put("createUser", BaseTenantAuth.getPresentLoginUserCode(request)+"");
								saveRecordMap.put("createTime", time);
								ts.put("acct_in", pk, saveRecordMap);
							}else{
								response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据已经被修改，请重新打修改页面！");
							}
						} catch (Exception e) {
							response.setCodeAndMsg(BusinessConst.CODE_PARAM, "充值时系统发生异常，请联系管理员！");
						}
					}else{
						if("".equals(version)){
							//更新账户数据
							Map<String, String> saveMap = new HashMap<String, String>();
							saveMap.put("appCode", appCode);
							saveMap.put("appName", appName);
							saveMap.put("version", BusinessUtils.getId()+"");
							saveMap.put("lastUser", BaseTenantAuth.getPresentLoginUserCode(request)+"");
							saveMap.put("lastTime", time);
							ts.put(tableName, appCode, saveMap);
							//添加充值记录
							String pk = appCode+"_"+time;
							Map<String, String> saveRecordMap = new HashMap<String, String>();
							saveRecordMap.put("appCode", appCode);
							saveRecordMap.put("money", cash);
							saveRecordMap.put("createUser", BaseTenantAuth.getPresentLoginUserCode(request)+"");
							saveRecordMap.put("createTime", time);
							ts.put("acct_in", pk, saveRecordMap);
						}else{
							response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
						}
					}
					//核算账户金额-累计充值，账户余额
					//获取累计充值金额
					BigDecimal inSum = this.getAccountInAll(appCode);
					if(inSum==null){
						inSum = new BigDecimal("0");
					}
					//获取消费金额
					BigDecimal outSum = this.getAccountOutAll(appCode);
					if(outSum==null){
						outSum = new BigDecimal("0");
					}
					//账户余额
					BigDecimal zhye = inSum.subtract(outSum);
					Map<String, String> saveMap = new HashMap<String, String>();
					saveMap.put("total", LangUtils.formatNumber(inSum,"#.###"));
					saveMap.put("cash", LangUtils.formatNumber(zhye,"#.###"));
					ts.put(tableName, appCode, saveMap);
				}
			}
		}
		return response;
	}
	
	/**
	 * 获取租户累计充值金额
	 * @param appCode
	 * @return
	 */
	private BigDecimal getAccountInAll(String appCode){
		BigDecimal total = new BigDecimal("0");
		if(!StringUtils.isEmpty(appCode)){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String tableName = "acct_in";
			String startPk = appCode;
			String endPk = appCode+"_zzzzzzzzzzzzzz";
			List<Map<String, String>> listAll = new ArrayList<Map<String, String>>();
			this.getTableStoreData(ts, tableName, startPk, endPk, listAll);
			if(listAll!=null && listAll.size()>0){
				for(int i=0;i<listAll.size();i++){
					Map<String, String> map = listAll.get(i);
					if(map!=null && map.size()>0){
						String moneyStr = map.get("money"); 
						BigDecimal money = null;
						if(!StringUtils.isEmpty(moneyStr)){
							try {
								money = new BigDecimal(moneyStr);
							} catch (Exception e) {
								money = null;
							}
						}
						if(money==null){
							money = new BigDecimal("0");
						}
						total = total.add(money);
					}
				}
			}
		}
		return total;
	}
	
	/**
	 * 获取租户累计消费金额
	 * @param appCode
	 * @return
	 */
	private BigDecimal getAccountOutAll(String appCode){
		BigDecimal total = new BigDecimal("0");
		if(!StringUtils.isEmpty(appCode)){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String tableName = "acct_day";
			String startPk = appCode;
			String endPk = appCode+"_zzzzzzzzzzzzzz";
			List<Map<String, String>> listAll = new ArrayList<Map<String, String>>();
			this.getTableStoreData(ts, tableName, startPk, endPk, listAll);
			if(listAll!=null && listAll.size()>0){
				for(int i=0;i<listAll.size();i++){
					Map<String, String> map = listAll.get(i);
					if(map!=null && map.size()>0){
						String cashStr = map.get("cash"); 
						BigDecimal cash = null;
						if(!StringUtils.isEmpty(cashStr)){
							try {
								cash = new BigDecimal(cashStr);
							} catch (Exception e) {
								cash = null;
							}
						}
						if(cash==null){
							cash = new BigDecimal("0");
						}
						total = total.add(cash);
					}
				}
			}
		}
		return total;
	}
	
	/**
	 * 消费记录
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse accountOutList(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		String startTime = StringUtils.trimNull(request.getData("startTime"));
		String endTime = StringUtils.trimNull(request.getData("endTime"));
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date startDate = sdf1.parse(startTime);
			Date endDate = sdf1.parse(endTime);
			if(startDate.getTime()>endDate.getTime()){
				startTime = sdf2.format(endDate);
				endTime = sdf2.format(startDate);
			}else{
				startTime = sdf2.format(startDate);
				endTime = sdf2.format(endDate);
			}
		} catch (Exception e) {
			startTime = null;
			endTime = null;
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if(!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String startPk = appCode+"_"+startTime;
			String endPk = appCode+"_"+endTime+"_zzzzzzzzzz";
			String tableName = "acct_out";
			this.getTableStoreData(ts, tableName, startPk, endPk, list);
		}
		BigDecimal sum = new BigDecimal("0");
		for(int i=0;i<list.size();i++){
			Map<String, String> map = list.get(i);
			if(map!=null && map.size()>0){
				String feeType = map.get("feeType");
				map.put("feeTypeDesc", FeeConst.getTypeDesc(feeType, ""));
				try {
					String time = map.get("pk").replace(appCode+"_", "");
					if(time.indexOf("_")!=-1){
						time = time.substring(0, time.indexOf("_"));
					}
					map.put("time", sdf1.format(sdf2.parse(time)));
					sum = sum.add(new BigDecimal(map.get("money")));
				} catch (Exception e) {
				}
			}
		}
		Map<String, String> sumMap = new HashMap<String, String>();
		sumMap.put("time", "消费合计");
		sumMap.put("money", LangUtils.formatNumber(sum,"#.###"));
		sumMap.put("feeTypeDesc", "");
		sumMap.put("mobile", "");
		sumMap.put("optName", "");
		list.add(0, sumMap);
		response.setData(list);
		return response;
	}
	
	/**
	 * 充值记录
	 * @param request
	 * @return
	 */
	@BusinessMethod
	public BusinessResponse accountInList(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appCode = StringUtils.trimNull(request.getData("appCode"));
		String startTime = StringUtils.trimNull(request.getData("startTime"));
		String endTime = StringUtils.trimNull(request.getData("endTime"));
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date startDate = sdf1.parse(startTime);
			Date endDate = sdf1.parse(endTime);
			if(startDate.getTime()>endDate.getTime()){
				startTime = sdf2.format(endDate);
				endTime = sdf2.format(startDate);
			}else{
				startTime = sdf2.format(startDate);
				endTime = sdf2.format(endDate);
			}
		} catch (Exception e) {
			startTime = null;
			endTime = null;
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if(!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
			TableStoreService ts = TableStoreUtils.getTableStoreService();
			String startPk = appCode+"_"+startTime;
			String endPk = appCode+"_"+endTime+"_zzzzzzzzzz";
			String tableName = "acct_in";
			this.getTableStoreData(ts, tableName, startPk, endPk, list);
		}
		BigDecimal sum = new BigDecimal("0");
		for(int i=0;i<list.size();i++){
			Map<String, String> map = list.get(i);
			if(map!=null && map.size()>0){
				try {
					String time = map.get("pk").replace(appCode+"_", "");
					if(time.indexOf("_")!=-1){
						time = time.substring(0, time.indexOf("_"));
					}
					map.put("time", sdf1.format(sdf2.parse(time)));
					sum = sum.add(new BigDecimal(map.get("money")));
				} catch (Exception e) {
				}
			}
		}
		Map<String, String> sumMap = new HashMap<String, String>();
		sumMap.put("time", "充值合计");
		sumMap.put("money", LangUtils.formatNumber(sum,"#.###"));
		sumMap.put("createUser", "");
		list.add(0, sumMap);
		response.setData(list);
		return response;
	}
	
	private void getTableStoreData(TableStoreService ts,String tableName,String startPk,String endPk,List<Map<String, String>> listAll){
		DataBean dataBean = ts.listData1(tableName, startPk, endPk);
		if(dataBean!=null){
			List<Map<String, String>> list = dataBean.getData();
			String nextPk = dataBean.getNextKey();
			if(list!=null && list.size()>0){
				listAll.addAll(list);
				if(!StringUtils.isEmpty(nextPk)){
					this.getTableStoreData(ts, tableName, nextPk, endPk, listAll);
				}
			}
		}
	}
	
}
