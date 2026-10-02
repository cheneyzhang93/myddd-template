package com.acme.shop.application.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderItem;
import com.acme.shop.domain.order.OrderRepository;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.common.page.PageQuery;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.deep361.myddd.kernel.ddd.event.DomainEventPublisher;
import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单用例。事务边界在这一层，不在领域层也不在 Controller。
 *
 * <p>聚合的事件由 {@link DomainEventPublisher#publish(org.deep361.myddd.kernel.ddd.event.DomainEventSource)}
 * 在保存之后发布：缺省阶段 AFTER_COMMIT 的监听器只有在事务真提交后才跑，所以"下单失败却加了积分"
 * 这条路在结构上不存在。
 */
@Service
public class OrderAppService {

    private final OrderRepository orders;
    private final DomainEventPublisher events;
    private final IdGenerator idGenerator;

    public OrderAppService(OrderRepository orders, DomainEventPublisher events, IdGenerator idGenerator) {
        this.orders = orders;
        this.events = events;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public OrderViewDTO create(CreateOrderDTO command) {
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException(CommonErrorCode.INVALID_PARAMETER);
        }
        List<OrderItem> items = command.lines().stream()
                .map(line -> new OrderItem(idGenerator.nextId(), line.skuId(), line.quantity(),
                        line.unitPriceCents()))
                .toList();
        Order order = Order.create(idGenerator.nextId(), command.buyerId(), items);
        this.orders.save(order);
        this.events.publish(order);
        return OrderViewDTO.of(order);
    }

    @Transactional
    public OrderViewDTO pay(long orderId) {
        Order order = require(orderId);
        order.pay();
        this.orders.save(order);
        this.events.publish(order);
        return OrderViewDTO.of(order);
    }

    @Transactional(readOnly = true)
    public OrderViewDTO find(long orderId) {
        return OrderViewDTO.of(require(orderId));
    }

    @Transactional(readOnly = true)
    public PageResult<OrderViewDTO> page(long pageNum, long pageSize) {
        return this.orders.page(PageQuery.of(pageNum, pageSize)).map(OrderViewDTO::of);
    }

    private Order require(long orderId) {
        return this.orders.findById(orderId)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, orderId));
    }
}
