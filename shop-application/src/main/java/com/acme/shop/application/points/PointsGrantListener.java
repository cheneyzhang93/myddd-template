package com.acme.shop.application.points;

import com.acme.shop.domain.order.OrderPaidEvent;
import com.acme.shop.domain.points.PointsAccount;
import com.acme.shop.domain.points.PointsAccountRepository;
import com.acme.shop.domain.points.PointsPolicy;
import org.deep361.myddd.kernel.ddd.event.DomainEventListener;
import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单支付成功后加积分——跨聚合的唯一耦合点。
 *
 * <p>监听器所在包必须被某个 {@code @Domain} 模块覆盖（这里由 {@code points} 模块的 scanPackages 给出），
 * 方法签名另有四条硬校验：非 static、非 private、返回 void、恰好一个 {@code DomainEvent} 子类型参数。
 * 违约是启动失败，不是运行期才发现。
 */
@Component
public class PointsGrantListener {

    private static final Logger log = LoggerFactory.getLogger(PointsGrantListener.class);

    private final PointsAccountRepository accounts;
    private final PointsPolicy policy;
    private final IdGenerator idGenerator;

    public PointsGrantListener(PointsAccountRepository accounts, PointsPolicy policy, IdGenerator idGenerator) {
        this.accounts = accounts;
        this.policy = policy;
        this.idGenerator = idGenerator;
    }

    @DomainEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderPaid(OrderPaidEvent event) {
        long points = this.policy.pointsFor(event.totalCents());
        if (points == 0) {
            log.debug("订单 {} 金额 {} 分不足 1 元，不加积分", event.orderId(), event.totalCents());
            return;
        }
        PointsAccount account = this.accounts.findByBuyerId(event.buyerId())
                .orElseGet(() -> PointsAccount.open(this.idGenerator.nextId(), event.buyerId()));
        account.grant(points);
        this.accounts.save(account);
        log.info("订单 {} 支付成功，买家 {} 加 {} 分，余额 {}", event.orderId(), event.buyerId(), points,
                account.balance());
    }
}
