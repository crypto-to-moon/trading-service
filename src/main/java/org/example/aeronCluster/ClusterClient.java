package org.example.aeronCluster;

import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.samples.cluster.ClusterConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import org.example.aeronCluster.snapshot.Serializer;
import org.example.aeronCluster.utils.AeronCommon;
import org.example.order.Order;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ClusterClient implements Agent {
    // 3s 发送一次心跳
    final static long keepAliveInterval = 3000;
    private final ExpandableDirectByteBuffer sendBuffer = new ExpandableDirectByteBuffer();
    private final IdleStrategy idleStrategy = new SleepingIdleStrategy();
    AeronCluster aeronCluster;
    long keepAliveDeadlineMs = 0;

    @Getter
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private final EgressListener egressListener;

    public ClusterClient(EgressListener egressListener) throws UnknownHostException {
        this.egressListener = egressListener;
        this.aeronCluster = init(); // 初始化 AeronCluster
    }

    private AeronCluster init() throws UnknownHostException {
        try {
            log.info("Initializing ClusterClient");

            String hostname = System.getenv("HOST_NAME");
            int nodeId = Integer.parseInt(System.getenv("nodeId"));
            String ingressEndpoints = AeronCommon.ingressEndpoints(List.of("node0", "node1", "node2"));
            log.info("[ClientConfig] ingressEndpoints: {}", ingressEndpoints);

            MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                    .threadingMode(ThreadingMode.SHARED)
                    .dirDeleteOnShutdown(true));

            AeronCluster aeronCluster = AeronCluster.connect(
                    new AeronCluster.Context()
                            .egressListener(egressListener)
                            .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                            .ingressChannel("aeron:udp")
                            .egressChannel(AeronCommon.udpChannel(nodeId, hostname, AeronCommon.CLIENT_RESPONSE_PORT_OFFSET))
                            .ingressEndpoints(ingressEndpoints));

            isInitialized.set(true);
            log.info("[ClusterClient] Client initialized successfully");
            return aeronCluster;

        } catch (Exception e) {
            log.error("Failed to initialize ClusterClient", e);
            throw new RuntimeException(e);
        }
    }

    public boolean send(Long key, String value) {
        int length = RaftDataEncoderAndDecoder.encoder(sendBuffer, key, value);
        log.info("[ClientAgent] send :[key:" + key + ",value:" + value + "]");

        while (aeronCluster.offer(sendBuffer, 0, length) < 0) {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
        return true;
    }

    @Override
    public void onStart() {
        keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
        log.info("[ClientAgent] onStart keepAliveDeadlineMs: {}", keepAliveDeadlineMs);
    }

    @Override
    public int doWork() throws Exception {
//        long currentTime = System.currentTimeMillis();
//        int workCount = 0;
//        if (this.isInit && keepAliveDeadlineMs < currentTime) {
//            aeronCluster.sendKeepAlive();
//            keepAliveDeadlineMs = System.currentTimeMillis() + keepAliveInterval;
//            workCount++;
//        }
//        if (!this.isInit) {
//            this.aeronCluster = this.init();
//            workCount += 1;
//        }
//        if (aeronCluster.egressSubscription().isConnected()) {
//            workCount += this.aeronCluster.pollEgress();
//        }
//        return workCount;
        int workCount = 0;
        if (isInitialized.get()) {
            workCount += aeronCluster.pollEgress();
        }
        return workCount;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "ClientAgent";
    }


    public boolean sendOrder(Order order) {
        // 将订单序列化为字节数组
        byte[] orderBytes = Serializer.serializeOrder(order);
        int length = orderBytes.length;
        sendBuffer.putBytes(0, orderBytes);

        log.info("[ClusterClient] Sending order: {}", order);

        while (aeronCluster.offer(sendBuffer, 0, length) < 0) {
            idleStrategy.idle(aeronCluster.pollEgress());
        }
        return true;
    }

}
