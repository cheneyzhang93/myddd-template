package com.acme.shop.auth;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Set;

/** 角色与权限的取数：一份 SQL 同时供登录快照与每次鉴权复用。 */
public class ShopPermissions {

    private static final String ROLES = "select role_code from shop_user_role where user_id = ?";
    private static final String PERMISSIONS = "select permission_code from shop_user_permission where user_id = ?";

    private final JdbcTemplate jdbc;

    public ShopPermissions(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Set<String> rolesOf(long userId) {
        return Set.copyOf(this.jdbc.queryForList(ROLES, String.class, userId));
    }

    public Set<String> permissionsOf(long userId) {
        return Set.copyOf(this.jdbc.queryForList(PERMISSIONS, String.class, userId));
    }

    /** Sa-Token 的 loginId 是 Object（本样例里是用户 id 的字符串形态）。 */
    public Set<String> rolesOf(Object loginId) {
        return rolesOf(Long.parseLong(String.valueOf(loginId)));
    }

    public Set<String> permissionsOf(Object loginId) {
        return permissionsOf(Long.parseLong(String.valueOf(loginId)));
    }
}
