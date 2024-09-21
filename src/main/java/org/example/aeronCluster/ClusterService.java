package org.example.aeronCluster;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.example.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import org.example.aeronCluster.snapshot.Deserializer;
import org.example.aeronCluster.snapshot.Serializer;
import org.example.nacos.NacosService;
import org.example.order.ClientService;
import org.example.order.Order;
import org.example.order.OrderBookManager;
import org.example.order.OrderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class ClusterService implements ClusteredService {
    private final IdleStrategy idleStrategy = new SleepingIdleStrategy();

    private Cluster cluster;

    @Autowired
    private NacosService nacosService;

    List<RaftDataEncoderAndDecoder.RaftData> clusterRaftData = new ArrayList<>();
    private OrderBookManager orderBookManager = new OrderBookManager();

    @Override
    public void onStart(Cluster cluster, Image snapshotImage) {
        this.cluster = cluster;
        printInfo("onStart", "");
        if (snapshotImage != null) {
            printInfo("onStart-snapshot", "");
        }
        ExpandableArrayBuffer snapshotBuffer = new ExpandableArrayBuffer();
        AtomicInteger snapshotBufferOffset = new AtomicInteger(0);
        if (snapshotImage != null && !snapshotImage.isEndOfStream()) {
            snapshotImage.poll((buffer, offset, length, header) -> {
                if (length > 0) {
                    buffer.getBytes(offset, snapshotBuffer, snapshotBufferOffset.get(), length);
                    snapshotBufferOffset.getAndAdd(length);
                }
            }, Integer.MAX_VALUE);
            byte[] bytes = new byte[snapshotBufferOffset.get()];
            snapshotBuffer.getBytes(0, bytes);
            this.clusterRaftData = Deserializer.deserializeFromBytes(bytes);
        }
    }

    @Override
    public void onSessionOpen(ClientSession session, long timestamp) {
        printInfo("onSessionOpen", session.toString());
    }

    @Override
    public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
        printInfo("onSessionClose", closeReason.toString());
    }

    @Override
    public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
        log.info("ClusterService on SessionMessage");
        byte[] data = new byte[length];
        buffer.getBytes(offset, data);
        Order order = Deserializer.deserializeOrder(data);

        if (order != null) {
            log.info("[ClusterService] Received order: {}", order);

            // 验证订单
            boolean valid = OrderValidator.validate(order);
            if (valid) {
                // 添加订单到订单簿
                orderBookManager.addOrder(order);

                // 更新订单状态并回复客户端
                order.setStatus(Order.OrderStatus.NEW);
                sendOrderResponse(session, order);
            } else {
                // 订单验证失败，回复错误信息
                order.setStatus(Order.OrderStatus.CANCELED);
                sendOrderResponse(session, order);
            }
            // Send response back to client
            byte[] responseBytes = Serializer.serializeOrder(order);
            UnsafeBuffer responseBuffer = new UnsafeBuffer(responseBytes);

            while (session.offer(responseBuffer, 0, responseBuffer.capacity()) < 0) {
                cluster.idleStrategy().idle();
            }
        } else {
            log.error("[ClusterService] Failed to deserialize order");
        }
    }

    private void sendOrderResponse(ClientSession session, Order order) {
        byte[] orderBytes = Serializer.serializeOrder(order);
        UnsafeBuffer buffer = new UnsafeBuffer(orderBytes);

        while (session.offer(buffer, 0, buffer.capacity()) < 0) {
            idleStrategy.idle();
        }
    }

    @Override
    public void onTimerEvent(long correlationId, long timestamp) {
        printInfo("onTimerEvent", "");

    }

    @Override
    public void onTakeSnapshot(ExclusivePublication snapshotPublication) {
        printInfo("onTakeSnapshot", "start take snapshot");
        byte[] bytes = Serializer.serializeToBytes(clusterRaftData);
        ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
        buffer.putBytes(0, bytes);

        while (snapshotPublication.offer(buffer, 0, bytes.length) < 0) {
            idleStrategy.idle();
        }
        printInfo("onTakeSnapshot", "end take snapshot" + " size:" + clusterRaftData.size());

    }

    @Override
    public void onRoleChange(Cluster.Role newRole) {
        log.info("[ClusterService] Role changed to: {}", newRole);
        printInfo("onRoleChange:", newRole.toString());
        nacosService.updateRole(newRole == Cluster.Role.LEADER);
    }

    @Override
    public void onTerminate(Cluster cluster) {

        printInfo("onTerminate", "");

    }

    @Override
    public void onNewLeadershipTermEvent(long leadershipTermId, long logPosition, long timestamp, long termBaseLogPosition, int leaderMemberId, int logSessionId, TimeUnit timeUnit, int appVersion) {
        printInfo("onNewLeadershipTermEvent", "leadershipTermId = " + leadershipTermId + ", logPosition = " + logPosition + ", timestamp = " + timestamp + ", termBaseLogPosition = " + termBaseLogPosition + ", leaderMemberId = " + leaderMemberId + ", logSessionId = " + logSessionId + ", timeUnit = " + timeUnit + ", appVersion = " + appVersion);
    }

    @Override
    public int doBackgroundWork(long nowNs) {
        return 0;
    }

    public void printInfo(String method, String msg) {
        log.info("[" + method + "] " + msg);
    }

    public List<RaftDataEncoderAndDecoder.RaftData> getClusterData() {
        return clusterRaftData;
    }

}
