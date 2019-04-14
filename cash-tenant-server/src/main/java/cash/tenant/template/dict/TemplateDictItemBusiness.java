package cash.tenant.template.dict;

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
 * 字典类型数据项
 * @author 马毅
 */
@Business(name = "TemplateDictItemBusiness")
public class TemplateDictItemBusiness {

	public static final String CASH_TENANT_ADD = "TemplateDictItem.add";
	public static final String CASH_TENANT_REMOVE = "TemplateDictItem.remove";
	public static final String CASH_TENANT_MODIFY = "TemplateDictItem.modify";
	public static final String CASH_TENANT_QUERY = "TemplateDictItem.query";
	public static final String CASH_TENANT_COUNT = "TemplateDictItem.count";

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String dictId = StringUtils.trimNull(request.getData("dictId"));
		String itemKey = StringUtils.trimNull(request.getData("itemKey"));
		String itemName = StringUtils.trimNull(request.getData("itemName"));
		String itemValue = StringUtils.trimNull(request.getData("itemValue"));
		String extralField1 = StringUtils.trimNull(request.getData("extralField1"));
		String extralField2 = StringUtils.trimNull(request.getData("extralField2"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		if(StringUtils.isEmpty(dictId)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else if(StringUtils.isEmpty(itemKey)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码不能为空！");
		}else if(itemKey.length()>40){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码不能超过40个字符！");
		}else if(StringUtils.isEmpty(itemName)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项名称不能为空！");
		}else if(itemName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项名称不能超过60个字符！");
		}else if(StringUtils.isEmpty(itemValue)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项值不能为空！");
		}else if(itemValue.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项值不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(extralField1) && extralField1.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备用字段1不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(extralField2) && extralField2.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备用字段2不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备注不能超过300个字符！");
		}else{
			//获取类型数据
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "id", dictId);
			Map<String, Object> bean = this.getSqlRunner().query(TemplateDictTypeBusiness.CASH_TENANT_QUERY, "default", paramsMap);
			if(bean!=null){
				String dictCode = StringUtils.trimNull(bean.get("dictCode"));
				//验证数据项编码是否存在
				paramsMap = new HashMap<String, Object>();
				SqlUtils.addConditionEquals(paramsMap, "dictId", dictId);
				SqlUtils.addConditionEquals(paramsMap, "itemKey", itemKey);
				Map<String, Object> existBean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
				if(existBean==null){
					BusinessUtils.setId(request.getData());
					request.putData("dictCode", dictCode);
					request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
					request.putData("createTime", DateUtils.unixTime());
					request.putData("lastTime", DateUtils.unixTime());
					int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
					response.setData(num);
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码已经存在！");
				}
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "字典类型数据可能已经被删除！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		//准备数据
		String id = StringUtils.trimNull(request.getData("id"));
		String itemKey = StringUtils.trimNull(request.getData("itemKey"));
		String itemName = StringUtils.trimNull(request.getData("itemName"));
		String itemValue = StringUtils.trimNull(request.getData("itemValue"));
		String extralField1 = StringUtils.trimNull(request.getData("extralField1"));
		String extralField2 = StringUtils.trimNull(request.getData("extralField2"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		Map<String, Object> paramMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(paramMap, "id", id);
		Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", paramMap);
		//验证数据
		if(StringUtils.isEmpty(id)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}else if(oldBean==null){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据已经被删除，请刷新后重试！");
		}else if(StringUtils.isEmpty(itemKey)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码不能为空！");
		}else if(itemKey.length()>40){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码不能超过40个字符！");
		}else if(StringUtils.isEmpty(itemName)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项名称不能为空！");
		}else if(itemName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项名称不能超过60个字符！");
		}else if(StringUtils.isEmpty(itemValue)){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项值不能为空！");
		}else if(itemValue.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项值不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(extralField1) && extralField1.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备用字段1不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(extralField2) && extralField2.length()>1000){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备用字段2不能超过1000个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "备注不能超过300个字符！");
		}else{
			//验证数据项编码是否存在
			paramMap = new HashMap<String, Object>();
			SqlUtils.addConditionNotEquals(paramMap, "id", id);
			SqlUtils.addConditionEquals(paramMap, "dictId", oldBean.get("dictId"));
			SqlUtils.addConditionEquals(paramMap, "itemKey", itemKey);
			Map<String, Object> existBean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramMap);
			if(existBean==null){
				request.putData("dictId", oldBean.get("dictId"));
				request.putData("dictCode", oldBean.get("dictCode"));
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
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "数据项编码已经存在！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse remove(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.removeData(BusinessConst.DATA_ID));
		// 获取旧数据
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
	
}
