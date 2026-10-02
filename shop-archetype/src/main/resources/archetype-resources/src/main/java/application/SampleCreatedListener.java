package ${package}.application;

import ${package}.domain.SampleCreatedEvent;
import org.deep361.myddd.kernel.ddd.event.DomainEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 领域事件监听器示例。签名有四条启动期硬校验：非 static、非 private、返回 void、恰好一个
 * {@code DomainEvent} 子类型参数；包必须被 {@code SampleDomain} 的 scanPackages 覆盖。
 */
@Component
public class SampleCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(SampleCreatedListener.class);

    @DomainEventListener
    public void onSampleCreated(SampleCreatedEvent event) {
        log.info("样本已创建：id={} name={}", event.sampleId(), event.name());
    }
}
