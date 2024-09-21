package org.example.market;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;
import org.example.aeronCluster.snapshot.Serializer;
import org.example.order.Trade;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class MarketDataFeed {


    private static final MarketDataFeed instance = new MarketDataFeed();

    // 使用 Aeron 发布市场数据
    private final Aeron aeron;
    private final Publication publication;

    // 定义发布市场数据的频道和流 ID
    private static final String CHANNEL = "aeron:udp?endpoint=224.0.1.1:40456"; // 多播地址，可根据需要调整
    private static final int STREAM_ID = 1001;

    private MarketDataFeed() {
        // 启动嵌入式的 MediaDriver
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnShutdown(true));

        // 创建 Aeron 实例
        aeron = Aeron.connect(new Aeron.Context()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName()));

        // 创建 Publication，用于发布市场数据
        publication = aeron.addPublication(CHANNEL, STREAM_ID);
    }

    public static MarketDataFeed getInstance() {
        return instance;
    }

    public void publishTrade(Trade trade) {
        // 序列化 Trade 对象
        byte[] tradeBytes = Serializer.serializeTrade(trade);
        UnsafeBuffer buffer = new UnsafeBuffer(tradeBytes);

        // 发送数据
        while (publication.offer(buffer, 0, buffer.capacity()) < 0L) {
            // 重试，或者根据需要调整策略
            // 可以添加适当的等待或退避策略，避免空循环占用 CPU
            log.error("Offer failed, retrying...");
            LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }
    }

    // 在应用关闭时，清理资源
    public void close() {
        publication.close();
        aeron.close();
        // 如果启动了嵌入式的 MediaDriver，需要关闭
        // mediaDriver.close(); // 需要保留对 mediaDriver 的引用
    }

}
