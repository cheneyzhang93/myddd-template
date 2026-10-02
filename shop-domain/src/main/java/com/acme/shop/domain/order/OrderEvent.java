package com.acme.shop.domain.order;

/** 订单流转事件（状态机的事件轴，与领域事件 {@link OrderPaidEvent} 不是一回事）。 */
public enum OrderEvent {

    PAY,
    SHIP,
    COMPLETE,
    CANCEL
}
