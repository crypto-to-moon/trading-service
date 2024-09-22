package com.github.crypto.to.moon.trading.service.order;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;
import com.github.crypto.to.moon.trading.service.trading.Trading;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * {@link OrderDispatcher} is responsible for dispatching orders to the matching engine.
 * {@link OrderEventHandler}
 */
@Slf4j
@Component
public class OrderDispatcher {

    private final OrderEventHandler eventHandler;

    private static final int ringBufferSize = 1 << 14;

    RingBuffer<EventWrapper> ringBuffer;

    Disruptor<EventWrapper> disruptor;

    public OrderDispatcher(OrderEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        this.disruptor = new Disruptor<>(
                EventWrapper::new,
                ringBufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );
        this.ringBuffer = disruptor.getRingBuffer();
        disruptor.start();

        disruptor.handleEventsWith(eventHandler);

    }

    public void publishOrder(Trading.EventMessage eventMessage) {
        while (!ringBuffer.tryPublishEvent((event, sequence) -> event.setEventMessage(eventMessage))) {
            log.warn("Ring buffer is full, retrying...");
            LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }
    }

}
