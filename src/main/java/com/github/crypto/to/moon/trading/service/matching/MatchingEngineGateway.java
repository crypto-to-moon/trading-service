package com.github.crypto.to.moon.trading.service.matching;

import com.google.protobuf.InvalidProtocolBufferException;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import com.github.crypto.to.moon.trading.service.aeronCluster.snapshot.Serializer;
import com.github.crypto.to.moon.trading.service.matching.market.MatchingResult;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.trading.Trading;
import com.github.crypto.to.moon.trading.service.util.BigDecimalProtoUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MatchingEngineGateway {

    private static MatchingEngineGateway instance;
    private final AeronCluster aeronCluster;
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();

    private MatchingEngineGateway() {
        // 初始化 AeronCluster
        aeronCluster = AeronCluster.connect(
                new AeronCluster.Context()
                        .egressListener(egressListener)
                // 其他配置...
        );
    }

    public static MatchingEngineGateway getInstance() {
        if (instance == null) {
            synchronized (MatchingEngineGateway.class) {
                if (instance == null) {
                    instance = new MatchingEngineGateway();
                }
            }
        }
        return instance;
    }

    public void sendOrder(Order order) {
        // 序列化订单
        Trading.OrderRequest orderMessage = convertToOrderMessage(order);
        UnsafeBuffer buffer = new UnsafeBuffer(orderMessage.toByteArray());

        // 发送订单
        while (aeronCluster.offer(buffer, 0, buffer.capacity()) < 0) {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
    }

    private Trading.OrderRequest convertToOrderMessage(Order order) {
        return Trading.OrderRequest.newBuilder()
                .setOrderId(order.getOrderId())
                .setUserId(order.getUserId())
                .setSymbol(order.getSymbol())
                .setType(Trading.OrderType.valueOf(order.getType().name()))
                .setPrice(BigDecimalProtoUtils.bigDecimalToBytes(order.getPrice()))
                .setAmount(BigDecimalProtoUtils.bigDecimalToBytes(order.getAmount()))
                .build();
    }

    // 接收撮合结果
    private final EgressListener egressListener = new EgressListener() {
        @Override
        public void onMessage(long clusterSessionId, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
            byte[] data = new byte[length];
            buffer.getBytes(offset, data);
            MatchingResult result = null;
            try {
                Trading.EventMessage eventMessage = Serializer.deserialize(data);
                Trading.MatchingResult matchingResult = eventMessage.getMatchingResult();
                log.info("Received matching result: {}", matchingResult);

            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            // 将撮合结果发布到订单模块的处理流程中，例如通过 Disruptor 或直接调用方法
            processMatchingResult(result);
        }

        // 其他回调方法...
    };

    public void processMatchingResult(MatchingResult result) {
        // 处理撮合结果，更新订单状态，发布账户信息等

    }

    public void sendMatchingResult(MatchingResult result) {


    }
}

