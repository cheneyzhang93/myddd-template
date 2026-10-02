package com.acme.shop.application.points;

import com.acme.shop.domain.points.PointsAccount;

/** 积分视图：应用层出参。 */
public record PointsViewDTO(long buyerId, long balance) {

    public static PointsViewDTO of(PointsAccount account) {
        return new PointsViewDTO(account.buyerId(), account.balance());
    }
}
