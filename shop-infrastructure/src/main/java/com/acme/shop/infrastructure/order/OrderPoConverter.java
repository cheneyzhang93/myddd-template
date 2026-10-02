package com.acme.shop.infrastructure.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderItem;
import com.acme.shop.domain.order.OrderPricing;
import com.acme.shop.domain.order.OrderStatus;

import java.util.List;

/**
 * 聚合 ⇄ 持久化对象的转换，手写。
 *
 * <p>这里不用 MapStruct，原因不是"忘了接注解处理器"，而是两条硬事实：
 * <ul>
 *   <li>领域对象是富对象——{@code Order} 的构造器是 private、只能走 {@code create}/{@code reconstitute}
 *       静态工厂，访问器是 {@code buyerId()} 这类流式方法而非 JavaBean getter；MapStruct 既造不出它，
 *       也读不到它的属性。</li>
 *   <li>状态是枚举、金额是值对象，转换本身是"语义映射"而不是"字段搬运"，自动生成会把非法值也搬进来。</li>
 * </ul>
 * MapStruct 在脚手架里的正确用武之地是"两侧都是 record / JavaBean"的传输对象转换，见
 * {@code shop-api} 的 {@code OrderVoMapper}。
 */
final class OrderPoConverter {

    private OrderPoConverter() {
    }

    static OrderPO toPo(Order order) {
        OrderPO po = new OrderPO();
        po.setId(order.getId());
        po.setBuyerId(order.buyerId());
        po.setStatus(order.status().name());
        po.setTotalCents(order.pricing().totalCents());
        return po;
    }

    static List<OrderItemPO> toItemPoList(Order order) {
        return order.items().stream().map(item -> toItemPo(order.getId(), item)).toList();
    }

    static OrderItemPO toItemPo(Long orderId, OrderItem item) {
        OrderItemPO po = new OrderItemPO();
        po.setId(item.getId());
        po.setOrderId(orderId);
        po.setSkuId(item.skuId());
        po.setQuantity(item.quantity());
        po.setUnitPriceCents(item.unitPriceCents());
        return po;
    }

    static Order toDomain(OrderPO po, List<OrderItemPO> itemPos) {
        List<OrderItem> items = itemPos.stream()
                .map(item -> new OrderItem(item.getId(), item.getSkuId(), item.getQuantity(),
                        item.getUnitPriceCents()))
                .toList();
        return Order.reconstitute(po.getId(), po.getBuyerId(), items,
                OrderStatus.valueOf(po.getStatus()), new OrderPricing(po.getTotalCents()));
    }
}
