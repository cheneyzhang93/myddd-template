package com.acme.shop.contract.order.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** 建单入参。 */
public record OrderCreateForm(
        @NotNull @Positive long buyerId,
        @NotEmpty List<@Valid @NotNull OrderLineForm> lines,
        @NotNull String clientToken) {

    /** 订单行入参。 */
    public record OrderLineForm(
            @Positive long skuId,
            @Positive int quantity,
            @Positive long unitPriceCents) {
    }
}
