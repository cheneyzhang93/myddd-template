package com.acme.shop.domain.points;

import org.deep361.myddd.kernel.ddd.context.Domain;

/** 积分领域模块入口。监听订单事件的 {@code PointsGrantListener} 落在应用层同名切片里。 */
@Domain(value = "points",
        scanPackages = {"com.acme.shop.domain.points", "com.acme.shop.application.points"})
public final class PointsDomain {

    private PointsDomain() {
    }
}
