package cash.tenant.login;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cash.tenant.auth.BaseTenantAuth;
import cash.tenant.bean.CashConst;
import jrain.fw.business.base.Business;
import jrain.fw.business.base.BusinessMethod;
import jrain.fw.business.bean.BusinessConst;
import jrain.fw.business.bean.BusinessRequest;
import jrain.fw.business.bean.BusinessResponse;
import jrain.fw.dao.runner.SqlRunner;
import jrain.fw.utils.FwUtils;
import jrain.utils.cipher.AesUtils;
import jrain.utils.lang.LangUtils;
import jrain.utils.lang.StringUtils;

/**
 * 登录
 * @author 马毅
 */
@Business(name="LoginBusiness")
public class LoginBusiness {
	
	public SqlRunner getSqlRunner() {
		return FwUtils.getSqlRunnerManger().getSqlRunner(CashConst.TENANT);
	}

	@BusinessMethod
	public BusinessResponse login(BusinessRequest request) {
		BusinessResponse response = new BusinessResponse();
		String userCode = StringUtils.trimNull(request.getData("userCode"));
		String pwd = StringUtils.trimNull(request.getData("password"));
		Map<String, String> resMap = new HashMap<String, String>();
		if(StringUtils.isEmpty(userCode)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "登录账号不能为空！");
		}else if(StringUtils.isEmpty(pwd)){
			response.setCodeAndMsg(BusinessConst.CODE_PARAM, "密码不能为空！");
		}else{
			String sql = "select * from template_user where user_code=?";
			Map<String, Object> loginUser = this.getSqlRunner().queryBySql(request.getData(), sql, userCode);
			if(loginUser==null){
				response.setCodeAndMsg(BusinessConst.CODE_PARAM, "用户名不存在！");
			}else{
				String userPwd = StringUtils.trimNull(loginUser.get("pwd"));
				String salt = StringUtils.trimNull(loginUser.get("salt"));
				String tenantPwdKey = CashConst.TENANTPWDKEY;
				pwd = AesUtils.encrypt(pwd + salt, tenantPwdKey);
				if(pwd.equals(userPwd)){
					String token = LangUtils.randomUUID();
					resMap.put("result", "success");
					resMap.put("token", token);
					resMap.put("userName", StringUtils.trimNull(loginUser.get("userName")));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					loginUser.put("activeTime", sdf.format(new Date()));//记录用户活跃时间，为系统超时准备时间
					BaseTenantAuth.getLoginUserAll().put(token, loginUser);
					response.setData(resMap);
				}else{
					response.setCodeAndMsg(BusinessConst.CODE_PARAM, "登录密码错误！");
				}
			}
		}
		return response;
	}
	
}
