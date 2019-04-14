package cash.tenant.appCipher;

import java.security.KeyPair;
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
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.dao.utils.SqlUtils;
import jrain.fw.utils.FwUtils;
import jrain.ts.TableStoreUtils;
import jrain.utils.cipher.RsaUtils;
import jrain.utils.date.DateUtils;
import jrain.utils.lang.LangUtils;
import jrain.utils.lang.StringUtils;

@Business(name = "BaseAppCipherBusiness")
public class BaseAppCipherBusiness { 

	public static final String CASH_TENANT_ADD = "BaseAppCipher.add";
	public static final String CASH_TENANT_REMOVE = "BaseAppCipher.remove";
	public static final String CASH_TENANT_MODIFY = "BaseAppCipher.modify";
	public static final String CASH_TENANT_QUERY = "BaseAppCipher.query";
	public static final String CASH_TENANT_COUNT = "BaseAppCipher.count";

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appId = StringUtils.trimNull(request.getData("appId"));
		if(StringUtils.isEmpty(appId)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "警告，非法操作！");
		}else{
			//获取租户信息
			Map<String, Object> paramMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramMap, "id", appId);
			Map<String, Object> bean = this.getSqlRunner().query(BaseAppBusiness.CASH_TENANT_QUERY, "default", paramMap);
			if(bean==null){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "租户数据可能已经被删除，请刷新后重试！");
			}else{
				String appCode = StringUtils.trimNull(bean.get("appCode"));
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
				} else {
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "创建失败，请稍后重试！");
				}
			}
		}
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		//验证修改数据
		String id = StringUtils.trimNull(request.getData("id"));
		String status = StringUtils.trimNull(request.getData("status"));
		if(StringUtils.isEmpty(id) || StringUtils.isEmpty(status)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "警告，非法操作！");
		}else{
			if("1".equals(status) || "2".equals(status)){
				Map<String, Object> paramsMap = new HashMap<String, Object>();
				SqlUtils.addConditionEquals(paramsMap, "id", id);
				Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
				if(oldBean!=null){
					paramsMap.put("status", status);
					int num = this.getSqlRunner().update(CASH_TENANT_MODIFY, "default", paramsMap);
					if(num>0){
						String appId = StringUtils.trimNull(oldBean.get("appId"));
						String appCode = StringUtils.trimNull(oldBean.get("appCode"));
						String accessKey = StringUtils.trimNull(oldBean.get("accessKey"));
						String accessPublic = StringUtils.trimNull(oldBean.get("accessPublic"));
						String pk = appId+"_"+accessKey;
						if("1".equals(status)){
							//同步到tablestore
							Map<String, String> saveDataMap = new HashMap<String, String>();
							saveDataMap.put("appId",  appId);
							saveDataMap.put("appCode", appCode);
							saveDataMap.put("accessPublic", accessPublic);
							TableStoreUtils.getTableStoreService().put("base_app_chiper", pk, saveDataMap);
						}else{
							TableStoreUtils.getTableStoreService().remove("base_app_chiper", pk);
						}
					}
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "被修改数据不存在，请刷新后核对！");
				}
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "警告，非法操作！");
			}
		}
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.removeData("id"));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramsMap, "id", id);
		Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
		if(oldBean!=null){
			int num = this.getSqlRunner().delete(CASH_TENANT_REMOVE, "default", paramsMap);
			if(num>0){
				String appId = StringUtils.trimNull(oldBean.get("appId"));
				String accessKey = StringUtils.trimNull(oldBean.get("accessKey"));
				String pk = appId+"_"+accessKey;
				TableStoreUtils.getTableStoreService().remove("base_app_chiper", pk);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据可能已经被删除，请刷新后核对！");
			}
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据可能已经被删除，请刷新后核对！");
		}
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse list(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String appId = StringUtils.trimNull(request.getData("appId"));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramsMap, "appId", appId);
		SqlUtils.setSqlOrder(paramsMap, "createTime desc");
		List<Map<String, Object>> list = getSqlRunner().list(CASH_TENANT_QUERY, "default", paramsMap);
		response.setData(list);
		response.setTotal(list.size());
		return response;
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
	
	@BusinessMethod
	public BusinessResponse saveRemarks(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramsMap, "id", id);
		paramsMap.put("remarks", remarks);
		this.getSqlRunner().update(CASH_TENANT_MODIFY, "default", paramsMap);
		return response;
	}
	
	
}
