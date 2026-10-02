package com.acme.shop.infrastructure.support;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * 发号器：直接复用 MyBatis-Plus 的雪花生成器，不引入额外中间件。
 *
 * <p>框架只提供 {@link IdGenerator} 端口、不提供实现——这是刻意的：发号策略（雪花 / 号段 / UUID）
 * 与部署拓扑绑定，业务自己的实现只要打上一个 {@code @Component} 就顶掉这一份。
 */
@Component
public class MpIdGenerator implements IdGenerator {

    @Override
    public long nextId() {
        return IdWorker.getId();
    }
}
