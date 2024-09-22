package com.github.crypto.to.moon.trading.service.matching.market;

import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import com.github.crypto.to.moon.trading.service.aeronCluster.snapshot.Deserializer;
import com.github.crypto.to.moon.trading.service.order.Trade;

public class MarketDataSubscriber {

    private final Aeron aeron;
    private final Subscription subscription;

    // 与发布端使用相同的频道和流 ID
    private static final String CHANNEL = "aeron:udp?endpoint=224.0.1.1:40456";
    private static final int STREAM_ID = 1001;

    public MarketDataSubscriber() {
        // 创建 Aeron 实例
        aeron = Aeron.connect(new Aeron.Context());

        // 创建 Subscription
        subscription = aeron.addSubscription(CHANNEL, STREAM_ID);
    }

    public void start() {
        final IdleStrategy idleStrategy = new BackoffIdleStrategy();
        final FragmentHandler fragmentHandler = new FragmentAssembler(this::onFragment);

        while (true) {
            final int fragments = subscription.poll(fragmentHandler, 10);
            idleStrategy.idle(fragments);
        }
    }

    private void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
        byte[] data = new byte[length];
        buffer.getBytes(offset, data);
        Trade trade = Deserializer.deserializeTrade(data);
        if (trade != null) {
            System.out.println("Received trade: " + trade);
        } else {
            System.err.println("Failed to deserialize trade");
        }
    }

    public void close() {
        subscription.close();
        aeron.close();
    }
}
