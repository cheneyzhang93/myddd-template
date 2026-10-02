package com.acme.shop.domain.order;

import org.deep361.myddd.kernel.common.page.PageQuery;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.deep361.myddd.kernel.ddd.repository.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 订单仓储端口。{@code Repository} 是空标记接口——查询形状由业务自定，框架不替你猜。
 */
public interface OrderRepository extends Repository<Order, Long> {

    void save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findByBuyer(long buyerId);

    PageResult<Order> page(PageQuery query);

    /** 超时未支付的订单（供定时关单用，按创建时间升序，最多 {@code limit} 条）。 */
    List<Order> findUnpaidCreatedBefore(Instant cutoff, int limit);
}
