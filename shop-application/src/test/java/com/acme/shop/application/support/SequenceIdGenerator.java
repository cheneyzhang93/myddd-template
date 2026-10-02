package com.acme.shop.application.support;

import org.deep361.myddd.kernel.ddd.model.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/** 递增 id 替身：单测要的是可预测，不是雪花。 */
public class SequenceIdGenerator implements IdGenerator {

    private final AtomicLong sequence = new AtomicLong(1000L);

    @Override
    public long nextId() {
        return this.sequence.incrementAndGet();
    }
}
