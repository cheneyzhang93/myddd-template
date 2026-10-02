package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.ddd.event.AbstractDomainEvent;

/** 订单已支付。积分侧监听它来加积分（跨聚合只通过事件，不互相持有引用）。 */
public final class OrderPaidEvent extends AbstractDomainEvent {

    private final long orderId;
    private final long buyerId;
    private final long totalCents;

    public OrderPaidEvent(long orderId, long buyerId, long totalCents) {
        super();
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.totalCents = totalCents;
    }

    public OrderPaidEvent(String eventId, java.time.Instant occurredAt, long orderId, long buyerId, long totalCents) {
        super(eventId, occurredAt);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.totalCents = totalCents;
    }

    public long orderId() {
        return orderId;
    }

    public long buyerId() {
        return buyerId;
    }

    public long totalCents() {
        return totalCents;
    }
}
