package com.acme.shop.application.order;

import com.acme.shop.application.support.InMemoryOrderRepository;
import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderItem;
import com.acme.shop.domain.order.OrderStatus;
import org.deep361.myddd.kernel.port.job.JobContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderTimeoutJob：调度端口与业务解耦")
class OrderTimeoutJobTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final OrderTimeoutJob job = new OrderTimeoutJob(this.orders);

    private static Order createdOrder(long id) {
        return Order.create(id, 8L, List.of(new OrderItem(id + 1000, 11L, 1, 500L)));
    }

    @Test
    @DisplayName("到期的未支付订单被关闭")
    void closesTimedOutOrders() {
        Order first = createdOrder(1L);
        Order second = createdOrder(2L);
        this.orders.whenUnpaidCreatedBefore(List.of(first, second));

        this.job.execute(JobContext.of("shopOrderTimeoutClose"));

        assertThat(first.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(second.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(this.orders.findById(1L)).isPresent();
    }

    @Test
    @DisplayName("参数不是数字时按缺省窗口跑，任务不失败")
    void toleratesGarbageParam() {
        Order order = createdOrder(3L);
        this.orders.whenUnpaidCreatedBefore(List.of(order));

        this.job.execute(new JobContext("shopOrderTimeoutClose", "thirty-minutes"));

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("没有到期订单时零写入")
    void doesNothingWhenEmpty() {
        this.orders.whenUnpaidCreatedBefore(List.of());

        this.job.execute(JobContext.of("shopOrderTimeoutClose"));

        assertThat(this.orders.page(org.deep361.myddd.kernel.common.page.PageQuery.of(1, 10)).total()).isZero();
    }
}
