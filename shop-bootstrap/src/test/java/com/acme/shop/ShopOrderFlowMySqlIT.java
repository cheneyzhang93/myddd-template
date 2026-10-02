package com.acme.shop;

import com.acme.shop.application.order.CreateOrderDTO;
import com.acme.shop.application.order.OrderAppService;
import com.acme.shop.application.order.OrderViewDTO;
import com.acme.shop.application.points.PointsAppService;
import com.acme.shop.application.points.PointsViewDTO;
import com.acme.shop.domain.order.OrderStatus;
import org.deep361.myddd.testing.container.MydddMysqlContainer;
import org.deep361.myddd.testing.integration.MydddIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 同一套用例在真实 MySQL 上再跑一遍（Provider 之外的另一半：数据库方言）。
 *
 * <p>本机没有 Docker 时整类干净跳过——这是脚手架的验收底线：{@code clean verify} 不许因为
 * "缺一个容器运行时"而变红。有 Docker 的环境（CI、devbox）自动生效，无需改代码。
 *
 * <p>{@code spring.sql.init.mode=always} 只在测试里打开：MySQL profile 缺省是 never，
 * 迁移脚本归业务自己的 Flyway/Liquibase，样例不把建表当成应用能力。
 */
@MydddIntegrationTest
@ImportTestcontainers(MydddMysqlContainer.class)
@TestPropertySource(properties = "spring.sql.init.mode=always")
class ShopOrderFlowMySqlIT {

    @Autowired
    private OrderAppService orders;

    @Autowired
    private PointsAppService points;

    @Test
    @DisplayName("建单 → 支付 → 积分：MySQL 方言下的 datetime(3) 与乐观锁更新都走通")
    void createPayAndGrantPoints() {
        OrderViewDTO created = this.orders.create(new CreateOrderDTO(1L,
                List.of(new CreateOrderDTO.CreateLineDTO(101L, 2, 1_500L)),
                UUID.randomUUID().toString()));
        assertThat(created.status()).isEqualTo(OrderStatus.CREATED.name());

        OrderViewDTO paid = this.orders.pay(created.orderId());
        assertThat(paid.status()).isEqualTo(OrderStatus.PAID.name());
        assertThat(paid.totalCents()).isEqualTo(3_000L);

        PointsViewDTO balance = this.points.findByBuyer(1L);
        assertThat(balance.balance()).isEqualTo(30L);
    }
}
