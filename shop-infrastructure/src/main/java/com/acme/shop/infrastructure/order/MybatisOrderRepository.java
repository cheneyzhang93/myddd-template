package com.acme.shop.infrastructure.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderRepository;
import com.acme.shop.domain.order.OrderStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.common.page.PageQuery;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单仓储的 MyBatis-Plus 实现——领域端口的适配端。
 *
 * <p>三处刻意的选择：
 * <ul>
 *   <li>id 由应用层发号（{@code IdGenerator}），PO 用 {@code IdType.INPUT}，不让 MP 生成第二个 id；</li>
 *   <li>聚合不带版本字段，因此乐观锁在"写前重读当前行版本"这一档工作（要跨请求防并发覆盖，
 *       就把 version 随聚合读出并在 {@code reconstitute} 时带进领域对象）；</li>
 *   <li>订单行的写入只在首次保存时发生——行的增删改属于建单这一条用例，不藏在"保存聚合"的魔法里。</li>
 * </ul>
 */
@Repository
public class MybatisOrderRepository implements OrderRepository {

    private final OrderMapper orders;
    private final OrderItemMapper orderItems;

    public MybatisOrderRepository(OrderMapper orders, OrderItemMapper orderItems) {
        this.orders = orders;
        this.orderItems = orderItems;
    }

    @Override
    @Transactional
    public void save(Order order) {
        OrderPO po = OrderPoConverter.toPo(order);
        OrderPO existing = this.orders.selectById(order.getId());
        if (existing == null) {
            this.orders.insert(po);
            for (OrderItemPO item : OrderPoConverter.toItemPoList(order)) {
                this.orderItems.insert(item);
            }
            return;
        }
        po.setVersion(existing.getVersion());
        if (this.orders.updateById(po) == 0) {
            throw new BizException(CommonErrorCode.CONFLICT, order.getId());
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderPO po = this.orders.selectById(id);
        return po == null ? Optional.empty() : Optional.of(load(po));
    }

    @Override
    public List<Order> findByBuyer(long buyerId) {
        List<OrderPO> pos = this.orders.selectList(
                new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getBuyerId, buyerId)
                        .orderByDesc(OrderPO::getId));
        return attachItems(pos);
    }

    @Override
    public PageResult<Order> page(PageQuery query) {
        Page<OrderPO> page = new Page<>(query.getPageNum(), query.getPageSize());
        this.orders.selectPage(page, new LambdaQueryWrapper<OrderPO>().orderByDesc(OrderPO::getId));
        return PageResult.of(attachItems(page.getRecords()), page.getTotal(),
                query.getPageNum(), query.getPageSize());
    }

    @Override
    public List<Order> findUnpaidCreatedBefore(Instant cutoff, int limit) {
        Page<OrderPO> page = new Page<>(1, limit, false);
        this.orders.selectPage(page, new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getStatus, OrderStatus.CREATED.name())
                .lt(OrderPO::getCreateTime, cutoff)
                .orderByAsc(OrderPO::getCreateTime));
        return attachItems(page.getRecords());
    }

    private Order load(OrderPO po) {
        List<OrderItemPO> items = itemsOf(List.of(po.getId())).getOrDefault(po.getId(), List.of());
        return OrderPoConverter.toDomain(po, items);
    }

    /** 一次 in 查询取回整页的订单行，避免 N+1。 */
    private List<Order> attachItems(List<OrderPO> pos) {
        if (pos.isEmpty()) {
            return List.of();
        }
        Map<Long, List<OrderItemPO>> itemsByOrderId = itemsOf(pos.stream().map(OrderPO::getId).toList());
        return pos.stream()
                .map(po -> OrderPoConverter.toDomain(po, itemsByOrderId.getOrDefault(po.getId(), List.of())))
                .toList();
    }

    private Map<Long, List<OrderItemPO>> itemsOf(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return this.orderItems.selectList(
                        new LambdaQueryWrapper<OrderItemPO>().in(OrderItemPO::getOrderId, orderIds)
                                .orderByAsc(OrderItemPO::getId))
                .stream()
                .collect(Collectors.groupingBy(OrderItemPO::getOrderId));
    }
}
