package com.acme.shop.application.support;

import org.deep361.myddd.kernel.ddd.event.DomainEvent;
import org.deep361.myddd.kernel.ddd.event.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

/** 记录型发布器替身：只记下坐标与事件，不做任何投递。 */
public class RecordingEventPublisher implements DomainEventPublisher {

    public record Publication(DomainEvent event, String aggregateType, String aggregateId) {
    }

    private final List<Publication> publications = new ArrayList<>();

    @Override
    public void publish(DomainEvent event, String aggregateType, String aggregateId) {
        this.publications.add(new Publication(event, aggregateType, aggregateId));
    }

    public List<Publication> publications() {
        return List.copyOf(this.publications);
    }

    public List<DomainEvent> events() {
        return this.publications.stream().map(Publication::event).toList();
    }
}
