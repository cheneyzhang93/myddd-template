package com.acme.shop.infrastructure.points;

import com.acme.shop.domain.points.PointsAccount;
import com.acme.shop.domain.points.PointsAccountRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 积分账户仓储实现。 */
@Repository
public class MybatisPointsAccountRepository implements PointsAccountRepository {

    private final PointsAccountMapper accounts;

    public MybatisPointsAccountRepository(PointsAccountMapper accounts) {
        this.accounts = accounts;
    }

    @Override
    public void save(PointsAccount account) {
        PointsAccountPO po = toPo(account);
        PointsAccountPO existing = this.accounts.selectById(account.getId());
        if (existing == null) {
            this.accounts.insert(po);
            return;
        }
        po.setVersion(existing.getVersion());
        if (this.accounts.updateById(po) == 0) {
            throw new BizException(CommonErrorCode.CONFLICT, account.getId());
        }
    }

    @Override
    public Optional<PointsAccount> findByBuyerId(long buyerId) {
        PointsAccountPO po = this.accounts.selectOne(new LambdaQueryWrapper<PointsAccountPO>()
                .eq(PointsAccountPO::getBuyerId, buyerId));
        return Optional.ofNullable(po).map(MybatisPointsAccountRepository::toDomain);
    }

    private static PointsAccountPO toPo(PointsAccount account) {
        PointsAccountPO po = new PointsAccountPO();
        po.setId(account.getId());
        po.setBuyerId(account.buyerId());
        po.setBalance(account.balance());
        return po;
    }

    private static PointsAccount toDomain(PointsAccountPO po) {
        return PointsAccount.reconstitute(po.getId(), po.getBuyerId(), po.getBalance());
    }
}
