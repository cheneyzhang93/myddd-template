package com.acme.shop.domain.points;

import org.deep361.myddd.kernel.ddd.repository.Repository;

import java.util.Optional;

/** 积分账户仓储端口：按买家取账户，账户不存在时由应用层开户。 */
public interface PointsAccountRepository extends Repository<PointsAccount, Long> {

    void save(PointsAccount account);

    Optional<PointsAccount> findByBuyerId(long buyerId);
}
