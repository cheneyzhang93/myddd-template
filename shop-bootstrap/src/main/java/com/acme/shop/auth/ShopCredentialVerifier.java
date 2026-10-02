package com.acme.shop.auth;

import org.deep361.myddd.kernel.port.auth.AuthCredentials;
import org.deep361.myddd.kernel.port.auth.AuthUser;
import org.deep361.myddd.kernel.port.auth.CredentialVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * 凭据校验器：业务侧唯一的"密码对不对"判定。端口在框架，实现在业务——框架不替你猜用户表。
 *
 * <p>三条纪律：
 * <ul>
 *   <li>口令比较只走 {@link PasswordEncoder#matches}，永不解密、永不比明文；</li>
 *   <li>用户不存在与口令错返回同一个结果（都是 {@code null}），不给枚举用户名的机会；</li>
 *   <li>返回 {@code null} 而不是抛异常——HTTP 语义（401）由 provider 与全局异常处理器统一决定。</li>
 * </ul>
 */
public class ShopCredentialVerifier implements CredentialVerifier {

    private static final Logger log = LoggerFactory.getLogger(ShopCredentialVerifier.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final ShopPermissions permissions;

    public ShopCredentialVerifier(JdbcTemplate jdbc, PasswordEncoder passwordEncoder,
            ShopPermissions permissions) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.permissions = permissions;
    }

    @Override
    public AuthUser verify(AuthCredentials credentials) {
        List<Account> found = this.jdbc.query(
                "select id, password_hash from shop_user where username = ?",
                (rs, rowNum) -> new Account(rs.getLong("id"), rs.getString("password_hash")),
                credentials.username());
        if (found.size() > 1) {
            log.warn("用户名 {} 命中 {} 行，登录拒绝", credentials.username(), found.size());
            return null;
        }
        if (found.isEmpty()
                || !this.passwordEncoder.matches(credentials.password(), found.get(0).passwordHash())) {
            return null;
        }
        Account account = found.get(0);
        String userId = String.valueOf(account.id());
        return new AuthUser(userId, credentials.username(),
                this.permissions.rolesOf(account.id()), this.permissions.permissionsOf(account.id()));
    }

    /** 只取登录需要的两列，避免把整行搬进内存。 */
    private record Account(long id, String passwordHash) {
    }
}
