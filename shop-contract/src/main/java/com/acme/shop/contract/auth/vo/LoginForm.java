package com.acme.shop.contract.auth.vo;

import jakarta.validation.constraints.NotBlank;

/** 登录入参。 */
public record LoginForm(@NotBlank String username, @NotBlank String password) {

    @Override
    public String toString() {
        return "LoginForm[username=" + this.username + ", password=***]";
    }
}
