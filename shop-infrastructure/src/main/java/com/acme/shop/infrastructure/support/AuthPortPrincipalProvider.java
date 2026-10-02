package com.acme.shop.infrastructure.support;

import org.deep361.myddd.kernel.port.auth.AuthPort;
import org.deep361.myddd.persistence.mybatis.PrincipalProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 审计字段的"当前操作人"来源：把鉴权端口接到持久化层。
 *
 * <p>框架的 {@code AuditMetaObjectHandler} 缺省用 {@code system}（{@code PrincipalProvider.none()}），
 * 本 bean 顶掉它——于是 {@code creator/updater} 记的是真实登录名，而不是全表一致的 system。
 *
 * <p>用 {@link ObjectProvider} 而不是直接注入：鉴权能力可被关闭（{@code myddd.auth.enabled=false}），
 * 那时审计仍要能写库，退回缺省主体即可。
 */
@Component
public class AuthPortPrincipalProvider implements PrincipalProvider {

    private final ObjectProvider<AuthPort> authPorts;

    public AuthPortPrincipalProvider(ObjectProvider<AuthPort> authPorts) {
        this.authPorts = authPorts;
    }

    @Override
    public Optional<String> currentPrincipal() {
        AuthPort auth = this.authPorts.getIfAvailable();
        return auth == null ? Optional.empty() : auth.currentUser().map(user -> user.username());
    }
}
