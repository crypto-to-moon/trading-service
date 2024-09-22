package com.github.crypto.to.moon.trading.service.matching;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

@Component
public class MatchingDispatcher {

    private Disruptor<MatchingEvent> matchingDisruptor;
    private RingBuffer<MatchingEvent> ringBuffer;

    private static final int ringBufferSize = 1 << 14;

    public MatchingDispatcher() {
        Disruptor<MatchingEvent> matchingDisruptor = new Disruptor<>(
                MatchingEvent::new,
                ringBufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        this.matchingDisruptor = matchingDisruptor;
        this.ringBuffer = matchingDisruptor.getRingBuffer();

        matchingDisruptor.handleEventsWith(new MatchingHandler());

        matchingDisruptor.start();
    }

    public void publishMatching(MatchingEvent matching) {
        boolean b = ringBuffer.tryPublishEvent((event, sequence) -> event.setOrder(matching.getOrder()));
        if (!b) {
            // 如果 ring buffer 已满，等待 100 毫秒
            LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }
    }
}
