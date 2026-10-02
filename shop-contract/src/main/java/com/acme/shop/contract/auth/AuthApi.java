package com.acme.shop.contract.auth;

import com.acme.shop.contract.auth.vo.AuthUserVO;
import com.acme.shop.contract.auth.vo.LoginForm;
import org.deep361.myddd.kernel.common.response.Response;

/** 登录与会话对外契约。 */
public interface AuthApi {

    /** 登录：成功后由 Sa-Token 签发会话，token 在响应头 {@code satoken} 中返回。 */
    Response<Void> login(LoginForm form);

    /** 登出。 */
    Response<Void> logout();

    /** 当前登录用户。 */
    Response<AuthUserVO> currentUser();
}
