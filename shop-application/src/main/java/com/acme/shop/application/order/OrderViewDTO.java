package com.acme.shop.application.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderItem;

import java.util.List;

/** 订单视图：应用层出参，与对外契约（contract 的 *VO）分开，两边各自演进。 */
public record OrderViewDTO(long orderId, long buyerId, String status, long totalCents, List<LineViewDTO> lines) {

    public record LineViewDTO(long skuId, int quantity, long unitPriceCents, long lineCents) {
    }

    public static OrderViewDTO of(Order order) {
        return new OrderViewDTO(order.getId(), order.buyerId(), order.status().name(),
                order.pricing().totalCents(),
                order.items().stream()
                        .map(item -> new LineViewDTO(item.skuId(), item.quantity(),
                                item.unitPriceCents(), item.lineCents()))
                        .toList());
    }

    /** 只用于测试与文档：领域行的显式转换入口，避免调用方摸聚合内部。 */
    public static LineViewDTO ofLine(OrderItem item) {
        return new LineViewDTO(item.skuId(), item.quantity(), item.unitPriceCents(), item.lineCents());
    }
}
