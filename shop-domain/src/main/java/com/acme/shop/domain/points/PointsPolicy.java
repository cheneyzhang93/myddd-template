package com.acme.shop.domain.points;

import org.deep361.myddd.kernel.ddd.service.DomainService;

/** 积分策略：1 元 = 1 积分，不足一元不送。领域服务无状态，所以是纯函数。 */
public final class PointsPolicy implements DomainService {

    private static final long CENTS_PER_POINT = 100L;

    public long pointsFor(long totalCents) {
        if (totalCents < 0) {
            throw new IllegalArgumentException("订单金额不能为负：" + totalCents);
        }
        return totalCents / CENTS_PER_POINT;
    }
}
