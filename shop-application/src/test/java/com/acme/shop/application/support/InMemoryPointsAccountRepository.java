package com.acme.shop.application.support;

import com.acme.shop.domain.points.PointsAccount;
import com.acme.shop.domain.points.PointsAccountRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** 内存积分仓储替身。 */
public class InMemoryPointsAccountRepository implements PointsAccountRepository {

    private final Map<Long, PointsAccount> byId = new LinkedHashMap<>();

    @Override
    public void save(PointsAccount account) {
        this.byId.put(account.getId(), account);
    }

    @Override
    public Optional<PointsAccount> findByBuyerId(long buyerId) {
        return this.byId.values().stream().filter(account -> account.buyerId() == buyerId).findFirst();
    }

    /** 只用于断言"账户被新建了几次"。 */
    public int size() {
        return this.byId.size();
    }
}
