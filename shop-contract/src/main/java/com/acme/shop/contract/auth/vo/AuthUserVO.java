package com.acme.shop.contract.auth.vo;

import java.util.Set;

/** 当前登录用户出参。 */
public record AuthUserVO(String userId, String username, Set<String> roles, Set<String> permissions) {
}
