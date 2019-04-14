package cash.tenant.template.auth;

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
 * 认证项
 * @author 马毅
 */
@Business(name = "TemplateAuthBusiness")
public class TemplateAuthBusiness {

	public static final String CASH_TENANT_ADD = "TemplateAuth.add";
	public static final String CASH_TENANT_REMOVE = "TemplateAuth.remove";
	public static final String CASH_TENANT_MODIFY = "TemplateAuth.modify";
	public static final String CASH_TENANT_QUERY = "TemplateAuth.query";
	public static final String CASH_TENANT_COUNT = "TemplateAuth.count";

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String authCode = StringUtils.trimNull(request.getData("authCode"));
		String authTitle = StringUtils.trimNull(request.getData("authTitle"));
		String authBody = StringUtils.trimNull(request.getData("authBody"));
		String requires = StringUtils.trimNull(request.getData("requires"));
		String rowSort = StringUtils.trimNull(request.getData("rowSort"));
		String status = StringUtils.trimNull(request.getData("status"));
		if(StringUtils.isEmpty(authCode)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码不能为空！");
		}else if(authCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(authTitle)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "标题不能为空！");
		}else if(authTitle.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "标题不能超过60个字符！");
		}else if(!StringUtils.isEmpty(authBody) && authBody.length()>200){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "描述不能超过200个字符！");
		}else if(!"1".equals(requires) && !"2".equals(requires)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else if(StringUtils.isEmpty(rowSort)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序不能为空！");
		}else{
			Long px = null;
			try {
				px = Long.parseLong(rowSort);
			} catch (Exception e) {
				px = null;
			}
			if(px==null){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序必须是数字！");
			}else if(px<-9999999 || px>9999999){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序必须在-9999999与9999999之间！");
			}else if(!"1".equals(status) && !"2".equals(status)){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
			}else{
				//验证数据项编码是否存在
				Map<String, Object> paramMap = new HashMap<String, Object>();
				SqlUtils.addConditionEquals(paramMap, "authCode", authCode);
				Map<String, Object> existBean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramMap);
				if(existBean==null){
					BusinessUtils.setId(request.getData());
					request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("createTime", DateUtils.unixTime());
					request.putData("lastTime", DateUtils.unixTime());
					int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
					response.setData(num);
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码已经存在！");
				}
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		//准备数据
		String id = StringUtils.trimNull(request.getData("id"));
		String authCode = StringUtils.trimNull(request.getData("authCode"));
		String authTitle = StringUtils.trimNull(request.getData("authTitle"));
		String authBody = StringUtils.trimNull(request.getData("authBody"));
		String requires = StringUtils.trimNull(request.getData("requires"));
		String rowSort = StringUtils.trimNull(request.getData("rowSort"));
		String status = StringUtils.trimNull(request.getData("status"));
		//验证数据
		if(StringUtils.isEmpty(authCode)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码不能为空！");
		}else if(authCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(authTitle)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "标题不能为空！");
		}else if(authTitle.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "标题不能超过60个字符！");
		}else if(!StringUtils.isEmpty(authBody) && authBody.length()>200){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "描述不能超过200个字符！");
		}else if(!"1".equals(requires) && !"2".equals(requires)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else if(StringUtils.isEmpty(rowSort)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序不能为空！");
		}else{
			Long px = null;
			try {
				px = Long.parseLong(rowSort);
			} catch (Exception e) {
				px = null;
			}
			if(px==null){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序必须是数字！");
			}else if(px<-9999999 || px>9999999){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "排序必须在-9999999与9999999之间！");
			}else if(!"1".equals(status) && !"2".equals(status)){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
			}else{
				//验证数据项编码是否存在
				Map<String, Object> paramMap = new HashMap<String, Object>();
				SqlUtils.addConditionNotEquals(paramMap, "id", id);
				SqlUtils.addConditionEquals(paramMap, "authCode", authCode);
				Map<String, Object> existBean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramMap);
				if(existBean==null){
					request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("lastTime", DateUtils.unixTime());
					SqlUtils.addConditionEquals(request.getData(), "id", request.getData("id"));
					int num = this.getSqlRunner().update(CASH_TENANT_MODIFY, "default", request.getData());
					if(num>0){
						response.setData(num);
					}else{
						response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "被修改数据不存在，请刷新后核对！");
					}
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "编码已经存在！");
				}
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.removeData(BusinessConst.DATA_ID));
		List<String> ids = CollectionUtils.valueOfList(id);
		int num = 0;
		if (ids.size() > 0) {
			SqlUtils.addConditionIn(request.getData(), "id", ids);
			num = this.getSqlRunner().delete(CASH_TENANT_REMOVE, "default", request.getData());
			response.setData(num);
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}
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
	
	@BusinessMethod
	public BusinessResponse listAll(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		List<Map<String, Object>> list = getSqlRunner().list(CASH_TENANT_QUERY, "default", request.getData());
		response.setData(list);
		response.setTotal(list.size());
		return response;
	}
	
}
