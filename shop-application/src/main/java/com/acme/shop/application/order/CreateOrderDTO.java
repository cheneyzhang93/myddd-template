package com.acme.shop.application.order;

import java.util.List;

/** 建单用例的入参。DTO 只允许出现在应用层（架构规则 6c）。 */
public record CreateOrderDTO(long buyerId, List<CreateLineDTO> lines, String clientToken) {

    public record CreateLineDTO(long skuId, int quantity, long unitPriceCents) {
    }
}
