package com.acme.shop.config;

import com.acme.shop.domain.points.PointsPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域组件的装配：领域层零 Spring 依赖（架构规则 1），所以它的 bean 只能在外面声明。
 *
 * <p>这是六层结构的固定代价，也是它的收益：领域包里没有一行注解，搬到别的框架照样能用。
 */
@Configuration(proxyBeanMethods = false)
public class ShopDomainConfiguration {

    @Bean
    public PointsPolicy pointsPolicy() {
        return new PointsPolicy();
    }
}
