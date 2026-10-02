package com.acme.shop.contract.points.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 积分兑换入参。 */
public record PointsRedeemForm(@NotNull @Positive long buyerId, @NotNull @Positive long points) {
}
