package com.acme.shop;

import org.deep361.myddd.archunit.MydddArchitectureSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 架构守卫：把框架的 7 条红线（展开为 11 条规则）钉在这个工程上。
 *
 * <p>接线只有一行——这是脚手架最该被消费的地方：规则由 {@code myddd-archunit} 维护，
 * 业务只负责给出自己的基础包。基础包写错的症状是"永远绿"（选择集为空 + {@code allowEmptyShould}），
 * 因此这一条测试必须与真实的顶层包名一起改。
 */
class ShopArchitectureTest {

    @Test
    @DisplayName("com.acme.shop 满足 myddd 全部架构规则")
    void satisfiesMydddArchitecture() {
        MydddArchitectureSpec.verify("com.acme.shop");
    }
}
