package com.github.crypto.to.moon.trading.service.order.persist;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;
import com.github.crypto.to.moon.trading.service.order.EventWrapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Component
public class OrderPersistenceDispatcher {

    private static final int ringBufferSize = 1 << 14;

    RingBuffer<EventWrapper> ringBuffer;

    Disruptor<EventWrapper> orderPersistenceDisruptor;

    public OrderPersistenceDispatcher() {

        this.orderPersistenceDisruptor = new Disruptor<>(
                EventWrapper::new,
                ringBufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );
        this.ringBuffer = orderPersistenceDisruptor.getRingBuffer();
        orderPersistenceDisruptor.start();
        orderPersistenceDisruptor.handleEventsWith(new OrderPersistenceHandler());

    }

    public void publishOrderEvent(EventWrapper eventWrapper) {
        while (!ringBuffer.tryPublishEvent((event, sequence) -> event.setEventMessage(eventWrapper.getEventMessage()))) {
            log.warn("Ring buffer is full, retrying...");
            LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }
    }
}
