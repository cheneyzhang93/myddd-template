package com.acme.shop.domain.points;

import org.deep361.myddd.kernel.ddd.model.AggregateRoot;

/**
 * 积分账户聚合根。它不持有任何订单引用：加积分只由 {@code OrderPaidEvent} 驱动。
 */
public final class PointsAccount extends AggregateRoot<Long> {

    private final long buyerId;

    private long balance;

    private PointsAccount(Long id, long buyerId, long balance) {
        super(id);
        this.buyerId = buyerId;
        this.balance = balance;
    }

    public static PointsAccount open(Long id, long buyerId) {
        if (buyerId <= 0) {
            throw new IllegalArgumentException("buyerId 必须为正：" + buyerId);
        }
        return new PointsAccount(id, buyerId, 0L);
    }

    public static PointsAccount reconstitute(Long id, long buyerId, long balance) {
        return new PointsAccount(id, buyerId, balance);
    }

    public void grant(long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("赠送积分必须为正：" + points);
        }
        this.balance = Math.addExact(this.balance, points);
    }

    public void redeem(long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("兑换积分必须为正：" + points);
        }
        if (this.balance < points) {
            throw new org.deep361.myddd.kernel.common.error.BizException(
                    org.deep361.myddd.kernel.common.error.CommonErrorCode.ILLEGAL_STATE,
                    "积分不足", "需要 " + points + "，现有 " + this.balance);
        }
        this.balance = Math.subtractExact(this.balance, points);
    }

    public long buyerId() {
        return buyerId;
    }

    public long balance() {
        return balance;
    }
}
