package cash.tenant.template.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import jrain.utils.cipher.AesUtils;
import jrain.utils.collection.CollectionUtils;
import jrain.utils.date.DateUtils;
import jrain.utils.lang.StringUtils;

/**
 * 用户
 * @author 马毅
 */
@Business(name = "TemplateUserBusiness")
public class TemplateUserBusiness {

	public static final String CASH_TENANT_ADD = "TemplateUser.add";
	public static final String CASH_TENANT_REMOVE = "TemplateUser.remove";
	public static final String CASH_TENANT_MODIFY = "TemplateUser.modify";
	public static final String CASH_TENANT_QUERY = "TemplateUser.query";
	public static final String CASH_TENANT_COUNT = "TemplateUser.count";

	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse add(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String userCode = StringUtils.trimNull(request.getData("userCode"));
		String userName = StringUtils.trimNull(request.getData("userName"));
		String pwd = StringUtils.trimNull(request.getData("pwd"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		if(StringUtils.isEmpty(userCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户编码不能为空！");
		}else if(userCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(userName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户姓名不能为空！");
		}else if(userName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户姓名不能超过60个字符！");
		}else if(StringUtils.isEmpty(pwd)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "密码不能为空！");
		}else if(pwd.length()<6 || pwd.length()>16){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "密码长度必须是6至16个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "备注长度不能超过300个字符！");
		}else{
			//验证用户编码是否存在
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionEquals(paramsMap, "userCode", userCode);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				//密码加密
				Random random = new Random();
				int salt = 100000 + random.nextInt(999999);
				String tenantPwdKey =CashConst.TENANTPWDKEY;
				pwd = AesUtils.encrypt(pwd + salt, tenantPwdKey);
				request.putData("pwd", pwd);
				request.putData("salt", salt);
				BusinessUtils.setId(request.getData());
				request.putData("createUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
				request.putData("createTime", DateUtils.unixTime());
				request.putData("lastTime", DateUtils.unixTime());
				int num = this.getSqlRunner().insert(CASH_TENANT_ADD, "default", request.getData());
				response.setData(num);
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "用户编码已经存在！");
			}
		}
		return response;
	}

	@BusinessMethod
	public BusinessResponse modify(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		String userCode = StringUtils.trimNull(request.getData("userCode"));
		String userName = StringUtils.trimNull(request.getData("userName"));
		String pwd = StringUtils.trimNull(request.getData("pwd"));
		String remarks = StringUtils.trimNull(request.getData("remarks"));
		//获取被修改的数据
		Map<String, Object> queryParamsMap = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(queryParamsMap, "id", id);
		Map<String, Object> oldBean = getSqlRunner().query(CASH_TENANT_QUERY, "default", queryParamsMap);
		if(oldBean==null){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "数据已经被删除，请刷新后重试！");
		}else if(StringUtils.isEmpty(userCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户编码不能为空！");
		}else if(userCode.length()>20){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户编码不能超过20个字符！");
		}else if(StringUtils.isEmpty(userName)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户姓名不能为空！");
		}else if(userName.length()>60){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户姓名不能超过60个字符！");
		}else if(!StringUtils.isEmpty(pwd) && (pwd.length()<6 || pwd.length()>16)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "密码长度必须是6至16个字符！");
		}else if(!StringUtils.isEmpty(remarks) && remarks.length()>300){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "备注长度不能超过300个字符！");
		}else{
			//验证用户编码是否可修改：管理员编码谁都不能改
			String oldUserCode = StringUtils.trimNull(oldBean.get("userCode"));
			if(CashConst.ADMIN_USER_CODE.equals(oldUserCode) && !oldUserCode.equals(userCode)){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "您没有修改管理员编码的权限！");
			}else{
				//不是管理员，就不能修改管理员密码
				String loginUserCode = StringUtils.trimNull(BaseTenantAuth.getPresentLoginUserCode(request));
				if(!CashConst.ADMIN_USER_CODE.equals(loginUserCode) && CashConst.ADMIN_USER_CODE.equals(oldUserCode) && !StringUtils.isEmpty(pwd)){
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "您没有修改管理员密码的权限！");
				}else{
					//验证用户编码是否存在
					Map<String, Object> paramsMap = new HashMap<String, Object>();
					SqlUtils.addConditionNotEquals(paramsMap, "id", id);
					SqlUtils.addConditionEquals(paramsMap, "userCode", userCode);
					Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
					if(bean==null){
						if(!StringUtils.isEmpty(pwd)){//修改密码
							//密码加密
							Random random = new Random();
							int salt = 100000 + random.nextInt(999999);
							String tenantPwdKey =CashConst.TENANTPWDKEY;
							pwd = AesUtils.encrypt(pwd + salt, tenantPwdKey);
							request.putData("pwd", pwd);
							request.putData("salt", salt);
						}else{//不修改密码
							request.removeData("pwd");
						}
						SqlUtils.addConditionEquals(request.getData(), "id", id);
						request.putData("lastUser", BaseTenantAuth.getPresentLoginUserCode(request));
						request.putData("lastTime", DateUtils.unixTime());
						int num = getSqlRunner().update(CASH_TENANT_MODIFY, "default", request.getData());
						response.setData(num);
					}else{
						response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "用户编码已经存在！");
					}
				}
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
			//验证删除数据是否有管理员
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			SqlUtils.addConditionIn(paramsMap, "id", ids);
			SqlUtils.addConditionEquals(paramsMap, "userCode", CashConst.ADMIN_USER_CODE);
			Map<String, Object> bean = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", paramsMap);
			if(bean==null){
				SqlUtils.addConditionIn(request.getData(), "id", ids);
				num = getSqlRunner().delete(CASH_TENANT_REMOVE, "default", request.getData());
			}else{
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "您没有删除管理员的权限！");
			}
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW, "警告：非法操作！");
		}
		response.setData(num);
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
	public BusinessResponse query(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String id = StringUtils.trimNull(request.getData("id"));
		SqlUtils.addConditionEquals(request.getData(), BusinessConst.DATA_ID, id);
		Map<String, Object> bean = getSqlRunner().query(CASH_TENANT_QUERY, "default", request.getData());
		if(bean!=null && bean.size()>0){
			bean.put("pwd", "");
			bean.put("salt", "");
		}
		response.setData(bean);
		return response;
	}

	@BusinessMethod
	public BusinessResponse list(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		SqlUtils.setSqlOrder(request.getData(), "rowSort asc");
		PageObj<List<Map<String, Object>>> bean = getSqlRunner().listPage(CASH_TENANT_QUERY, "default", request.getData());
		List<Map<String, Object>> list = bean.getRows();
		if(list!=null && list.size()>0){
			for(int i=0;i<list.size();i++){
				Map<String, Object> map = list.get(i);
				if(map!=null && map.size()>0){
					map.put("pwd", "");
					map.put("salt", "");
				}
			}
		}
		response.setData(list);
		response.setTotal(bean.getTotal());
		return response;
	}
	
	@BusinessMethod
	public BusinessResponse updatePwd(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		//获取登录用户信息
		String loginUserId = StringUtils.trimNull(BaseTenantAuth.getPresentLoginUserCode(request));
		Map<String, Object> params = new HashMap<String, Object>();
		SqlUtils.addConditionEquals(params, "id", loginUserId);
		Map<String, Object> loginUser = this.getSqlRunner().query(CASH_TENANT_QUERY, "default", params);
		if(loginUser!=null){
			String pwd = StringUtils.trimNull(loginUser.get("pwd"));
			String salt = StringUtils.trimNull(loginUser.get("salt"));
			String oldPwd = StringUtils.trimNull(request.getData("oldPwd"));
			String tenantPwdKey =CashConst.TENANTPWDKEY;
			String oldPwdSalt = AesUtils.encrypt(oldPwd + salt, tenantPwdKey);
			String newPwd = StringUtils.trimNull(request.getData("newPwd"));
			String newPwdSalt = AesUtils.encrypt(newPwd + salt, tenantPwdKey);
			String confirmPwd = StringUtils.trimNull(request.getData("confirmPwd"));
			if(StringUtils.isEmpty(pwd)){
				response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"初始密码为空，请联系管理员！");
			}else{
				if(pwd.equals(oldPwdSalt)){
					if(!StringUtils.isEmpty(newPwd)){
						if(newPwd.length()<6 || newPwd.length()>16){
							response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"密码必须是6到16个字符之间！");
						}else{
							if(newPwd.equals(confirmPwd)){
								if(pwd.equals(newPwdSalt)){
									response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"新密码与原密码一致！");
								}else{
									Map<String, Object> updateData = new HashMap<String, Object>();
									SqlUtils.addConditionEquals(updateData, BusinessConst.DATA_ID, loginUserId);
									updateData.put("pwd", newPwdSalt);
									int num = this.getSqlRunner().update(CASH_TENANT_MODIFY, "default", updateData);
									response.setData(num);
								}
							}else{
								response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"两次密码不一致！");
							}
						}
					}else{
						response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"新密码不能为空！");
					}
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"原密码错误！");
				}
			}
		}else{
			response.setCodeAndMsg(BusinessConst.CODE_UNKNOW,"登录失效或者数据已经被删除，请重新登录后再试！");
		}
		return response;
	}
	
}
