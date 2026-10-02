package com.acme.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 应用入口。
 *
 * <p>这一类只有两件事：定位基础包（{@code com.acme.shop}）和打开缓存。框架能力全部由 starter
 * 自动装配决定，不需要 {@code @EnableMyddd} 之类的开关——脚手架刻意不造"总开关"，
 * 每个能力都有自己的 {@code myddd.*.enabled}。
 *
 * <p>基础包同时是三件事的锚点：组件扫描、{@code @Domain} 扫描、架构守卫的 {@code basePackage}。
 * 改它必须三处一起改，这也是本样例把 {@code MydddArchitectureSpec.verify("com.acme.shop")}
 * 写成一条测试的原因。
 */
@EnableCaching
@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
