package ${package}.application;

import ${package}.domain.Sample;
import ${package}.domain.SampleRepository;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.ddd.event.DomainEventPublisher;
import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.springframework.stereotype.Service;

/**
 * 用例编排：取号 → 建聚合 → 存 → 发事件 → 转 DTO。
 *
 * <p>事件在保存之后发，缺省阶段 AFTER_COMMIT 的监听器只有事务真提交才跑。接了数据库后请给方法加
 * {@code @Transactional}（需要 spring-tx，骨架里没有数据源所以不带依赖）：加了事务之后顺序不能反，
 * 先 publish 后 save 会让回滚的事务发出不可撤回的事件。
 */
@Service
public class SampleAppService {

    private final SampleRepository samples;
    private final DomainEventPublisher events;
    private final IdGenerator idGenerator;

    public SampleAppService(SampleRepository samples, DomainEventPublisher events, IdGenerator idGenerator) {
        this.samples = samples;
        this.events = events;
        this.idGenerator = idGenerator;
    }

    public SampleViewDTO create(String name) {
        Sample sample = Sample.create(this.idGenerator.nextId(), name);
        this.samples.save(sample);
        this.events.publish(sample);
        return SampleViewDTO.of(sample);
    }

    public SampleViewDTO get(long id) {
        return this.samples.findById(id)
                .map(SampleViewDTO::of)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, id));
    }
}
