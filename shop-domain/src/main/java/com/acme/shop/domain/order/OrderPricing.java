package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.ddd.model.AbstractValueObject;

/** 订单价格：以"分"为唯一金额单位，杜绝浮点。equals/hashCode/toString 由 AbstractValueObject 按字段反射给出。 */
public final class OrderPricing extends AbstractValueObject {

    private final long totalCents;

    public OrderPricing(long totalCents) {
        if (totalCents < 0) {
            throw new IllegalArgumentException("订单金额不能为负：" + totalCents);
        }
        this.totalCents = totalCents;
    }

    public long totalCents() {
        return totalCents;
    }

    public OrderPricing plus(OrderPricing other) {
        return new OrderPricing(Math.addExact(this.totalCents, other.totalCents));
    }
}
