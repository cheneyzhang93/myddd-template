package com.acme.shop.application.order;

import com.acme.shop.application.support.InMemoryOrderRepository;
import com.acme.shop.application.support.RecordingEventPublisher;
import com.acme.shop.application.support.SequenceIdGenerator;
import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderPaidEvent;
import com.acme.shop.domain.order.OrderStatus;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderAppService：建单、支付、查询的用例编排")
class OrderAppServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final OrderAppService service =
            new OrderAppService(this.orders, this.events, new SequenceIdGenerator());

    private static CreateOrderDTO command(long buyerId, CreateOrderDTO.CreateLineDTO... lines) {
        return new CreateOrderDTO(buyerId, List.of(lines), "token-" + buyerId);
    }

    @Test
    @DisplayName("建单：金额由领域聚合算，应用层只搬运")
    void createsOrderAndPublishesNothingYet() {
        OrderViewDTO view = this.service.create(
                command(7L, new CreateOrderDTO.CreateLineDTO(11L, 2, 1_500L)));

        assertThat(view.totalCents()).isEqualTo(3_000L);
        assertThat(view.status()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(this.orders.findById(view.orderId())).isPresent();
        assertThat(this.events.events()).isEmpty();
    }

    @Test
    @DisplayName("建单：空行是参数错误，不进领域")
    void rejectsEmptyLines() {
        assertThatThrownBy(() -> this.service.create(command(7L)))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(CommonErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("支付：状态推进 + 发布 OrderPaidEvent，事件坐标是聚合根")
    void paysAndPublishesEvent() {
        OrderViewDTO created = this.service.create(
                command(7L, new CreateOrderDTO.CreateLineDTO(11L, 1, 12_345L)));

        OrderViewDTO paid = this.service.pay(created.orderId());

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID.name());
        assertThat(this.events.events()).singleElement().isInstanceOf(OrderPaidEvent.class);
        RecordingEventPublisher.Publication only = this.events.publications().get(0);
        assertThat(only.aggregateType()).isEqualTo(Order.class.getName());
        assertThat(only.aggregateId()).isEqualTo(String.valueOf(created.orderId()));
        assertThat(((OrderPaidEvent) only.event()).totalCents()).isEqualTo(12_345L);
    }

    @Test
    @DisplayName("支付不存在的订单：404 语义的 NOT_FOUND")
    void payUnknownOrder() {
        assertThatThrownBy(() -> this.service.pay(999_999L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(CommonErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("分页：领域对象在应用层边界转成 DTO")
    void pagesOrders() {
        this.service.create(command(1L, new CreateOrderDTO.CreateLineDTO(1L, 1, 100L)));
        this.service.create(command(2L, new CreateOrderDTO.CreateLineDTO(1L, 1, 200L)));

        PageResult<OrderViewDTO> page = this.service.page(1, 10);

        assertThat(page.total()).isEqualTo(2L);
        assertThat(page.items()).hasSize(2);
        assertThat(page.pages()).isEqualTo(1L);
    }
}
