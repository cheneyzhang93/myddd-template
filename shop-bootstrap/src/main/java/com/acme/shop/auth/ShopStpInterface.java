package com.acme.shop.auth;

import cn.dev33.satoken.stp.StpInterface;

import java.util.List;

/**
 * Sa-Token 的权限数据源：把 {@code @RequiresPermission} 的检查接到业务自己的角色/权限表上。
 *
 * <p>provider 侧要求"恰好一个 {@code StpInterface} bean"（框架里有启动期守卫，两个就拒绝启动），
 * 所以这张表是唯一的权限出口。换成 Spring Security 时本类整体删除，改成一堆
 * {@code GrantedAuthority}——业务代码与契约不受影响。
 */
public class ShopStpInterface implements StpInterface {

    private final ShopPermissions permissions;

    public ShopStpInterface(ShopPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return this.permissions.permissionsOf(loginId).stream().sorted().toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return this.permissions.rolesOf(loginId).stream().sorted().toList();
    }
}
