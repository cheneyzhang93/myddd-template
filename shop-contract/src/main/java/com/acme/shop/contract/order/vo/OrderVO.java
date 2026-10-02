package com.acme.shop.contract.order.vo;

import java.util.List;

/** 订单出参。 */
public record OrderVO(long orderId, long buyerId, String status, long totalCents, List<OrderLineVO> lines) {

    /** 订单行出参。 */
    public record OrderLineVO(long skuId, int quantity, long unitPriceCents, long lineCents) {
    }
}
