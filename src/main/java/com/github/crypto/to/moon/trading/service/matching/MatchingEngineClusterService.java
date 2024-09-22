package com.github.crypto.to.moon.trading.service.matching;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;

public class MatchingEngineClusterService implements ClusteredService {

    private final RingBuffer<MatchingEvent> ringBuffer;

    public MatchingEngineClusterService(Disruptor<MatchingEvent> disruptor) {
        this.ringBuffer = disruptor.getRingBuffer();
    }

    @Override
    public void onStart(Cluster cluster, Image snapshotImage) {

    }

    @Override
    public void onSessionOpen(ClientSession session, long timestamp) {

    }

    @Override
    public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {

    }

    @Override
    public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {

        // 反序列化订单
        byte[] data = new byte[length];
        buffer.getBytes(offset, data);
//        Order order = Serializer.deserialize(data, OrderMessage.class);


        // 发布到 Disruptor
        long sequence = ringBuffer.next();
        try {
            MatchingEvent event = ringBuffer.get(sequence);
            event.setOrder(null);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    @Override
    public void onTimerEvent(long correlationId, long timestamp) {

    }

    @Override
    public void onTakeSnapshot(ExclusivePublication snapshotPublication) {

    }

    @Override
    public void onRoleChange(Cluster.Role newRole) {

    }

    @Override
    public void onTerminate(Cluster cluster) {

    }

//    // 发送撮合结果回订单模块
//    public void sendMatchingResult(MatchingResult result) {
//        // 序列化
//        byte[] data = Serializer.serialize(result);
//        UnsafeBuffer buffer = new UnsafeBuffer(data);
//
////         获取对应的 ClientSession
//        ClientSession session = cluster.clientSessions().get(result.getOrder().getSessionId());
//        if (session != null) {
//            while (session.offer(buffer, 0, buffer.capacity()) < 0) {
//                cluster.idle();
//            }
//        }
//    }
}
