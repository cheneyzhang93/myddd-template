package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.statemachine.StateMachine;
import org.deep361.myddd.kernel.statemachine.StateMachineDefinition;

/**
 * 订单状态机的唯一真源。定义在 build 期做全图校验（初始态非终态、无重复(源,事件)、全可达、终态无出边），
 * 所以这里不写任何防御性断言。
 */
public final class OrderStateMachine {

    private static final StateMachineDefinition<OrderStatus, OrderEvent> DEFINITION =
            StateMachineDefinition.<OrderStatus, OrderEvent>builder("order")
                    .initial(OrderStatus.CREATED)
                    .terminal(OrderStatus.COMPLETED)
                    .terminal(OrderStatus.CANCELLED)
                    .transition(OrderStatus.CREATED, OrderEvent.PAY, OrderStatus.PAID)
                    .transition(OrderStatus.CREATED, OrderEvent.CANCEL, OrderStatus.CANCELLED)
                    .transition(OrderStatus.PAID, OrderEvent.SHIP, OrderStatus.SHIPPED)
                    .transition(OrderStatus.PAID, OrderEvent.CANCEL, OrderStatus.CANCELLED)
                    .transition(OrderStatus.SHIPPED, OrderEvent.COMPLETE, OrderStatus.COMPLETED)
                    .build();

    private OrderStateMachine() {
    }

    public static StateMachine<OrderStatus, OrderEvent> forOrder(Order order) {
        return StateMachine.of(DEFINITION, order::status, order::changeStatus);
    }
}
