package com.acme.shop.config;

import cn.dev33.satoken.stp.StpInterface;
import com.acme.shop.auth.ShopCredentialVerifier;
import com.acme.shop.auth.ShopPermissions;
import com.acme.shop.auth.ShopStpInterface;
import org.deep361.myddd.kernel.port.auth.CredentialVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 鉴权接线：框架要的两个 bean——{@link CredentialVerifier}（登录时验凭据）与
 * {@link StpInterface}（每次鉴权取权限）——都在这里落到业务的用户表上。
 *
 * <p>少给任何一个的症状都很明确：没有校验器则登录必失败（provider 会打 WARN），
 * 权限源数量不对则启动期守卫直接拒绝启动。
 */
@Configuration(proxyBeanMethods = false)
public class ShopAuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ShopPermissions shopPermissions(JdbcTemplate jdbc) {
        return new ShopPermissions(jdbc);
    }

    @Bean
    public CredentialVerifier credentialVerifier(JdbcTemplate jdbc, PasswordEncoder passwordEncoder,
            ShopPermissions permissions) {
        return new ShopCredentialVerifier(jdbc, passwordEncoder, permissions);
    }

    @Bean
    public StpInterface stpInterface(ShopPermissions permissions) {
        return new ShopStpInterface(permissions);
    }
}
