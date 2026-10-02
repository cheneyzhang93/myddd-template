package com.acme.shop.application.points;

import com.acme.shop.application.support.InMemoryPointsAccountRepository;
import com.acme.shop.domain.points.PointsAccount;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PointsAppService：查询与兑换")
class PointsAppServiceTest {

    private final InMemoryPointsAccountRepository accounts = new InMemoryPointsAccountRepository();
    private final PointsAppService service = new PointsAppService(this.accounts);

    @Test
    @DisplayName("没有账户时余额读作 0，不抛异常")
    void readsZeroForUnknownBuyer() {
        assertThat(service.findByBuyer(404L)).isEqualTo(new PointsViewDTO(404L, 0L));
    }

    @Test
    @DisplayName("兑换扣减余额")
    void redeems() {
        PointsAccount account = PointsAccount.open(1L, 8L);
        account.grant(500L);
        this.accounts.save(account);

        assertThat(service.redeem(8L, 200L).balance()).isEqualTo(300L);
    }

    @Test
    @DisplayName("余额不足：领域抛的 ILLEGAL_STATE 原样穿出应用层")
    void rejectsShortage() {
        this.accounts.save(PointsAccount.reconstitute(1L, 8L, 10L));

        assertThatThrownBy(() -> service.redeem(8L, 20L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(CommonErrorCode.ILLEGAL_STATE);
    }
}
