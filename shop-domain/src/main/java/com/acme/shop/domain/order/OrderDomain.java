package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.ddd.context.Domain;

/**
 * 订单领域模块的入口声明。{@code scanPackages} 显式列出两层：领域事件监听器是应用层的 bean，
 * 而框架要求监听器所在包必须被某个 {@code @Domain} 模块覆盖（否则启动期直接拒绝注册）。
 */
@Domain(value = "order", scanPackages = {"com.acme.shop.domain.order", "com.acme.shop.application.order"})
public final class OrderDomain {

    private OrderDomain() {
    }
}
