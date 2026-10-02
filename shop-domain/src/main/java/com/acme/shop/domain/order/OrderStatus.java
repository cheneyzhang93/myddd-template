package com.acme.shop.domain.order;

/** 订单状态。流转合法性只由 {@link OrderStateMachine} 定义，不在这里编码。 */
public enum OrderStatus {

    CREATED,
    PAID,
    SHIPPED,
    COMPLETED,
    CANCELLED
}
