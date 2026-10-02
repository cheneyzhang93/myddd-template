package com.acme.shop.application.points;

import com.acme.shop.domain.points.PointsAccount;
import com.acme.shop.domain.points.PointsAccountRepository;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分用例：查询走缓存，兑换走事务并驱逐缓存。
 *
 * <p>缓存开关由消费方负责（{@code @EnableCaching} 在启动类上），框架不替应用决定；
 * 缓存介质由 {@code myddd.cache.type} 决定，换 redis 时这一层零改动。
 */
@Service
public class PointsAppService {

    static final String CACHE_NAME = "shop-points";

    private final PointsAccountRepository accounts;

    public PointsAppService(PointsAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#buyerId")
    @Transactional(readOnly = true)
    public PointsViewDTO findByBuyer(long buyerId) {
        return this.accounts.findByBuyerId(buyerId)
                .map(PointsViewDTO::of)
                .orElseGet(() -> new PointsViewDTO(buyerId, 0L));
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#buyerId")
    @Transactional
    public PointsViewDTO redeem(long buyerId, long points) {
        PointsAccount account = this.accounts.findByBuyerId(buyerId)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, buyerId));
        account.redeem(points);
        this.accounts.save(account);
        return PointsViewDTO.of(account);
    }
}
