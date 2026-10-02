package com.acme.shop.domain.order;

import java.util.List;

/**
 * 订单聚合根。状态流转一律过 {@link OrderStateMachine}，不在方法体里写 if-else 状态判断。
 *
 * <p>构造只有两个入口：{@link #create} 建单、{@link #reconstitute} 从存储重建。没有无参构造与
 * setter——{@code Entity} 的 id 是 final，重建必须走构造器，这也是 MapStruct 只负责
 * "领域对象 → PO" 而反向手写的原因。
 */
public class Order extends org.deep361.myddd.kernel.ddd.model.AggregateRoot<Long> {

    private final long buyerId;
    private final List<OrderItem> items;

    private OrderStatus status;
    private OrderPricing pricing;

    private Order(Long id, long buyerId, List<OrderItem> items, OrderStatus status, OrderPricing pricing) {
        super(id);
        this.buyerId = buyerId;
        this.items = List.copyOf(items);
        this.status = status;
        this.pricing = pricing;
    }

    public static Order create(Long id, long buyerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单至少要有一行");
        }
        if (buyerId <= 0) {
            throw new IllegalArgumentException("buyerId 必须为正：" + buyerId);
        }
        return new Order(id, buyerId, items, OrderStatus.CREATED, sumOf(items));
    }

    public static Order reconstitute(Long id, long buyerId, List<OrderItem> items,
            OrderStatus status, OrderPricing pricing) {
        return new Order(id, buyerId, items, status, pricing);
    }

    public void pay() {
        OrderStateMachine.forOrder(this).fire(OrderEvent.PAY);
        registerEvent(new OrderPaidEvent(getId(), buyerId, pricing.totalCents()));
    }

    public void ship() {
        OrderStateMachine.forOrder(this).fire(OrderEvent.SHIP);
    }

    public void complete() {
        OrderStateMachine.forOrder(this).fire(OrderEvent.COMPLETE);
    }

    public void cancel() {
        OrderStateMachine.forOrder(this).fire(OrderEvent.CANCEL);
    }

    public long buyerId() {
        return buyerId;
    }

    public List<OrderItem> items() {
        return items;
    }

    public OrderStatus status() {
        return status;
    }

    void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public OrderPricing pricing() {
        return pricing;
    }

    private static OrderPricing sumOf(List<OrderItem> items) {
        OrderPricing total = new OrderPricing(0L);
        for (OrderItem item : items) {
            total = total.plus(new OrderPricing(item.lineCents()));
        }
        return total;
    }
}
