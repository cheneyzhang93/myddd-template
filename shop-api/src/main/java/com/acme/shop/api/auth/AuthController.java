package com.acme.shop.api.auth;

import com.acme.shop.contract.auth.AuthApi;
import com.acme.shop.contract.auth.vo.AuthUserVO;
import com.acme.shop.contract.auth.vo.LoginForm;
import jakarta.validation.Valid;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.common.response.Response;
import org.deep361.myddd.kernel.port.auth.AuthCredentials;
import org.deep361.myddd.kernel.port.auth.AuthPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录 Controller：只调端口，不出现任何 Sa-Token 类型。
 *
 * <p>token 由 provider 写进响应头（satoken 默认 {@code is-write-header=true}，头名 {@code satoken}），
 * 前端之后带这个头回来即可——所以"换鉴权 provider"不会波及契约，也不会波及这一层。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthPort auth;
    private final AuthVoMapper mapper;

    public AuthController(AuthPort auth, AuthVoMapper mapper) {
        this.auth = auth;
        this.mapper = mapper;
    }

    @Override
    @PostMapping("/login")
    public Response<Void> login(@Valid @RequestBody LoginForm form) {
        this.auth.login(new AuthCredentials(form.username(), form.password()));
        return Response.ok();
    }

    @Override
    @PostMapping("/logout")
    public Response<Void> logout() {
        this.auth.logout();
        return Response.ok();
    }

    @Override
    @GetMapping("/current-user")
    public Response<AuthUserVO> currentUser() {
        return Response.ok(this.auth.currentUser()
                .map(this.mapper::toVo)
                .orElseThrow(() -> new BizException(CommonErrorCode.UNAUTHORIZED)));
    }
}
