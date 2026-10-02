package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.statemachine.StateMachineException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static Order newOrder() {
        return Order.create(1L, 42L, List.of(new OrderItem(11L, 900L, 2, 1990L)));
    }

    @Test
    void created_order_totals_line_amounts_in_cents() {
        Order order = newOrder();
        assertThat(order.pricing()).isEqualTo(new OrderPricing(3980L));
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void paying_registers_exactly_one_domain_event_and_reads_back_on_drain() {
        Order order = newOrder();
        order.pay();

        assertThat(order.domainEvents()).hasSize(1);
        OrderPaidEvent event = (OrderPaidEvent) order.domainEvents().get(0);
        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.buyerId()).isEqualTo(42L);
        assertThat(event.totalCents()).isEqualTo(3980L);
        assertThat(event.eventId()).isNotBlank();

        assertThat(order.drainEvents()).hasSize(1);
        assertThat(order.drainEvents()).isEmpty();
    }

    @Test
    void illegal_transitions_are_rejected_by_the_state_machine() {
        Order order = newOrder();
        assertThatThrownBy(() -> order.ship()).isInstanceOf(StateMachineException.class);
        assertThatThrownBy(() -> order.complete()).isInstanceOf(StateMachineException.class);
    }

    @Test
    void happy_path_walks_the_whole_graph() {
        Order order = newOrder();
        assertThatCode(() -> {
            order.pay();
            order.ship();
            order.complete();
        }).doesNotThrowAnyException();
        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void terminal_states_have_no_outgoing_transition() {
        Order completed = newOrder();
        completed.pay();
        completed.ship();
        completed.complete();
        assertThatThrownBy(completed::cancel).isInstanceOf(StateMachineException.class);

        Order cancelled = newOrder();
        cancelled.cancel();
        assertThatThrownBy(cancelled::pay).isInstanceOf(StateMachineException.class);
    }

    @Test
    void an_empty_order_is_not_a_legal_aggregate() {
        assertThatThrownBy(() -> Order.create(1L, 42L, List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderItem(1L, 900L, 0, 100L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identity_equals_ignores_mutable_state() {
        Order first = newOrder();
        Order second = newOrder();
        second.pay();
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(Order.create(2L, 42L, List.of(new OrderItem(11L, 900L, 2, 1990L))));
    }

}
