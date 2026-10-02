package com.acme.shop.infrastructure.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderItem;
import com.acme.shop.domain.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("聚合 ⇄ PO 往返：状态与金额不能在路上丢")
class OrderPoConverterTest {

    @Test
    @DisplayName("往返保真")
    void roundTrips() {
        Order order = Order.create(42L, 8L, List.of(
                new OrderItem(1L, 101L, 2, 1_500L),
                new OrderItem(2L, 102L, 1, 999L)));

        OrderPO po = OrderPoConverter.toPo(order);
        List<OrderItemPO> itemPos = OrderPoConverter.toItemPoList(order);
        Order restored = OrderPoConverter.toDomain(po, itemPos);

        assertThat(po.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(po.getTotalCents()).isEqualTo(3_999L);
        assertThat(restored.getId()).isEqualTo(order.getId());
        assertThat(restored.buyerId()).isEqualTo(order.buyerId());
        assertThat(restored.status()).isEqualTo(order.status());
        assertThat(restored.pricing()).isEqualTo(order.pricing());
        assertThat(restored.items()).isEqualTo(order.items());
    }

    @Test
    @DisplayName("订单行带上父 id，重建才有归属")
    void itemRowsCarryOrderId() {
        Order order = Order.create(42L, 8L, List.of(new OrderItem(1L, 101L, 2, 1_500L)));

        assertThat(OrderPoConverter.toItemPoList(order)).singleElement()
                .satisfies(item -> assertThat(item.getOrderId()).isEqualTo(42L));
    }
}
