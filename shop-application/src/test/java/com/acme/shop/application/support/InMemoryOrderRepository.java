package com.acme.shop.application.support;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderRepository;
import com.acme.shop.domain.order.OrderStatus;
import org.deep361.myddd.kernel.common.page.PageQuery;
import org.deep361.myddd.kernel.common.page.PageResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 内存订单仓储：应用层单测用的替身，不代表生产实现（生产实现见 shop-infrastructure）。 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new LinkedHashMap<>();
    private List<Order> unpaidCreatedBefore = List.of();

    public void whenUnpaidCreatedBefore(List<Order> orders) {
        this.unpaidCreatedBefore = List.copyOf(orders);
    }

    @Override
    public void save(Order order) {
        this.store.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<Order> findByBuyer(long buyerId) {
        return this.store.values().stream().filter(order -> order.buyerId() == buyerId).toList();
    }

    @Override
    public PageResult<Order> page(PageQuery query) {
        List<Order> all = new ArrayList<>(this.store.values());
        all.sort(Comparator.comparing(Order::getId));
        int from = (int) Math.min(query.offset(), all.size());
        int to = (int) Math.min(from + query.getPageSize(), all.size());
        return PageResult.of(all.subList(from, to), all.size(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public List<Order> findUnpaidCreatedBefore(Instant cutoff, int limit) {
        return this.unpaidCreatedBefore.stream()
                .filter(order -> order.status() == OrderStatus.CREATED)
                .limit(limit)
                .toList();
    }
}
