package ${package}.domain;

import org.deep361.myddd.kernel.ddd.event.AbstractDomainEvent;

/** 样本已创建。跨聚合的联动只靠它，不靠互相持有引用。 */
public final class SampleCreatedEvent extends AbstractDomainEvent {

    private final long sampleId;
    private final String name;

    public SampleCreatedEvent(long sampleId, String name) {
        super();
        this.sampleId = sampleId;
        this.name = name;
    }

    public SampleCreatedEvent(String eventId, java.time.Instant occurredAt, long sampleId, String name) {
        super(eventId, occurredAt);
        this.sampleId = sampleId;
        this.name = name;
    }

    public long sampleId() {
        return sampleId;
    }

    public String name() {
        return name;
    }
}
