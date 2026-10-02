package ${package}.application;

import ${package}.domain.Sample;
import ${package}.domain.SampleCreatedEvent;
import ${package}.domain.SampleRepository;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.ddd.event.DomainEvent;
import org.deep361.myddd.kernel.ddd.event.DomainEventPublisher;
import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 用例层测试：不启容器，用真实聚合 + 假端口。 */
class SampleAppServiceTest {

    private final Map<Long, Sample> store = new HashMap<>();
    private final List<DomainEvent> published = new ArrayList<>();

    private final SampleAppService service = new SampleAppService(
            new SampleRepository() {
                @Override
                public void save(Sample sample) {
                    SampleAppServiceTest.this.store.put(sample.getId(), sample);
                }

                @Override
                public Optional<Sample> findById(long id) {
                    return Optional.ofNullable(SampleAppServiceTest.this.store.get(id));
                }
            },
            new DomainEventPublisher() {
                @Override
                public void publish(DomainEvent event, String aggregateType, String aggregateId) {
                    SampleAppServiceTest.this.published.add(event);
                }
            },
            new IdGenerator() {
                private long next = 1000L;

                @Override
                public long nextId() {
                    return ++this.next;
                }
            });

    @Test
    void createsSavesAndPublishes() {
        SampleViewDTO created = this.service.create("  第一个样本  ");

        assertThat(created.id()).isEqualTo(1001L);
        assertThat(created.name()).isEqualTo("第一个样本");
        assertThat(this.store).containsKey(1001L);
        assertThat(this.published)
                .singleElement()
                .isInstanceOf(SampleCreatedEvent.class);
    }

    @Test
    void readsBackStoredSample() {
        this.service.create("样本");

        assertThat(this.service.get(1001L).name()).isEqualTo("样本");
    }

    @Test
    void unknownIdIsNotFound() {
        assertThatThrownBy(() -> this.service.get(404L))
                .isInstanceOf(BizException.class);
    }
}
