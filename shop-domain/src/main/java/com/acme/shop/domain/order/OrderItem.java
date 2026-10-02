package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.ddd.model.Entity;

/** 订单行。属于 {@link Order} 聚合内部，不单独持久化身份之外的东西。 */
public final class OrderItem extends Entity<Long> {

    private final long skuId;
    private final int quantity;
    private final long unitPriceCents;

    public OrderItem(Long id, long skuId, int quantity, long unitPriceCents) {
        super(id);
        if (quantity <= 0) {
            throw new IllegalArgumentException("订单行数量必须为正：" + quantity);
        }
        if (skuId <= 0) {
            throw new IllegalArgumentException("skuId 必须为正：" + skuId);
        }
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("单价不能为负：" + unitPriceCents);
        }
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
    }

    public long skuId() {
        return skuId;
    }

    public int quantity() {
        return quantity;
    }

    public long unitPriceCents() {
        return unitPriceCents;
    }

    public long lineCents() {
        return Math.multiplyExact(unitPriceCents, quantity);
    }
}
