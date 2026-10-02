package com.acme.shop.application.points;

import com.acme.shop.application.support.InMemoryPointsAccountRepository;
import com.acme.shop.application.support.SequenceIdGenerator;
import com.acme.shop.domain.order.OrderPaidEvent;
import com.acme.shop.domain.points.PointsPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PointsGrantListener：跨聚合的唯一耦合点")
class PointsGrantListenerTest {

    private final InMemoryPointsAccountRepository accounts = new InMemoryPointsAccountRepository();
    private final PointsGrantListener listener =
            new PointsGrantListener(this.accounts, new PointsPolicy(), new SequenceIdGenerator());

    @Test
    @DisplayName("首笔支付自动开户并按 1 元 = 1 分累加")
    void grantsOnFirstPaidOrder() {
        this.listener.onOrderPaid(new OrderPaidEvent(1L, 8L, 12_345L));

        assertThat(this.accounts.size()).isEqualTo(1);
        assertThat(this.accounts.findByBuyerId(8L)).get()
                .satisfies(account -> assertThat(account.balance()).isEqualTo(123L));
    }

    @Test
    @DisplayName("第二笔复用同一账户，不再开户")
    void accumulatesOnSameAccount() {
        this.listener.onOrderPaid(new OrderPaidEvent(1L, 8L, 10_000L));
        this.listener.onOrderPaid(new OrderPaidEvent(2L, 8L, 5_000L));

        assertThat(this.accounts.size()).isEqualTo(1);
        assertThat(this.accounts.findByBuyerId(8L)).get()
                .satisfies(account -> assertThat(account.balance()).isEqualTo(150L));
    }

    @Test
    @DisplayName("不足 1 元的订单不产生账户，也不写库")
    void skipsSubPointAmount() {
        this.listener.onOrderPaid(new OrderPaidEvent(1L, 8L, 99L));

        assertThat(this.accounts.size()).isZero();
    }
}
