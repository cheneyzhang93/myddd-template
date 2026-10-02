package com.acme.shop.domain.points;

import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointsAccountTest {

    @Test
    void grant_accumulates_and_redeem_never_goes_negative() {
        PointsAccount account = PointsAccount.open(7L, 42L);
        account.grant(30);
        account.grant(20);
        assertThat(account.balance()).isEqualTo(50);

        account.redeem(49);
        assertThat(account.balance()).isEqualTo(1);
    }

    @Test
    void insufficient_balance_is_a_business_failure_with_the_illegal_state_code() {
        PointsAccount account = PointsAccount.open(7L, 42L);
        assertThatThrownBy(() -> account.redeem(1))
                .isInstanceOf(BizException.class)
                .extracting(failure -> ((BizException) failure).getErrorCode())
                .isEqualTo(CommonErrorCode.ILLEGAL_STATE);
    }

    @Test
    void non_positive_amounts_are_rejected_on_both_sides() {
        PointsAccount account = PointsAccount.open(7L, 42L);
        assertThatThrownBy(() -> account.grant(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.redeem(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PointsAccount.open(7L, 0L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void policy_grants_one_point_per_yuan_and_rounds_down() {
        PointsPolicy policy = new PointsPolicy();
        assertThat(policy.pointsFor(0L)).isZero();
        assertThat(policy.pointsFor(99L)).isZero();
        assertThat(policy.pointsFor(100L)).isEqualTo(1);
        assertThat(policy.pointsFor(3980L)).isEqualTo(39);
        assertThatThrownBy(() -> policy.pointsFor(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_grant_registers_no_domain_event_by_design() {
        PointsAccount account = PointsAccount.open(7L, 42L);
        account.grant(10);
        assertThat(account.domainEvents()).isEmpty();
        assertThat(account.getId()).isEqualTo(7L);
        assertThat(account.buyerId()).isEqualTo(42L);
    }

    @Test
    void value_objects_compare_by_fields_not_by_identity() {
        assertThat(new PointsSnapshot(1L, 10L)).isEqualTo(new PointsSnapshot(1L, 10L));
        assertThat(new PointsSnapshot(1L, 10L)).isNotEqualTo(new PointsSnapshot(1L, 11L));
        assertThat(new PointsSnapshot(1L, 10L).toString()).contains("balance=10");
    }

    /** 值对象基类的样例：两个 final 字段，全部语义由反射给出。 */
    private static final class PointsSnapshot extends org.deep361.myddd.kernel.ddd.model.AbstractValueObject {

        private final long accountId;
        private final long balance;

        private PointsSnapshot(long accountId, long balance) {
            this.accountId = accountId;
            this.balance = balance;
        }
    }
}
