package org.example.order;

import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.cluster.service.Cluster;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.example.aeronCluster.ClusterClient;
import org.example.aeronCluster.snapshot.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class ClientService implements EgressListener {

    private ClusterClient clusterClient;

    // 存储订单的 Map，key 为 orderId，value 为 OrderResponse
    private final ConcurrentHashMap<String, CompletableFuture<OrderController.OrderResponse>> pendingOrders = new ConcurrentHashMap<>();

    public ClientService() {

    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) throws UnknownHostException {
        log.info("[ClientService] onApplicationEvent: {}", event);
        // 将自身作为 EgressListener 传递给 ClusterClient
        this.clusterClient = new ClusterClient(this);

        // 启动 ClusterClient 的 AgentRunner
        AgentRunner agentRunner = new AgentRunner(
                new SleepingIdleStrategy(),
                Throwable::printStackTrace,
                null,
                clusterClient);
        AgentRunner.startOnThread(agentRunner);
    }


    public OrderController.OrderResponse sendOrder(OrderRequest orderRequest) throws InterruptedException, ExecutionException {
        // 生成订单 ID
        String orderId = UUID.randomUUID().toString();

        // 创建订单对象
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(orderRequest.getUserId());
        order.setSymbol(orderRequest.getSymbol());
        order.setType(Order.OrderType.valueOf(orderRequest.getOrderType()));
        order.setSide(Order.Side.valueOf(orderRequest.getSide()));
        order.setPrice(orderRequest.getPrice());
        order.setAmount(orderRequest.getQuantity());
        order.setClientOrderId(orderRequest.getClientOrderId());

        // 创建一个 CompletableFuture 来等待订单响应
        CompletableFuture<OrderController.OrderResponse> future = new CompletableFuture<>();
        pendingOrders.put(orderId, future);

        // 发送订单
        boolean success = clusterClient.sendOrder(order);
        OrderController.OrderResponse response = new OrderController.OrderResponse();
        response.setOrderId(orderId);
        response.setClientOrderId(orderRequest.getClientOrderId());
        if (success) {
            response.setStatus("NEW");
            response.setMessage("Order placed successfully");
        } else {
            response.setStatus("FAILED");
            response.setMessage("Failed to place order");
        }


        // 等待订单响应（可以设置超时时间）
        return future.get();
    }

    // 供 ClusterClient 的 EgressListener 调用，更新订单状态
    public void onOrderResponse(Order order) {
        CompletableFuture<OrderController.OrderResponse> future = pendingOrders.remove(order.getOrderId());
        if (future != null) {
            OrderController.OrderResponse response = new OrderController.OrderResponse();
            response.setOrderId(order.getOrderId());
            future.complete(response);
        }
    }

    @Override
    public void onSessionEvent(long correlationId, long clusterSessionId, long leadershipTermId, int leaderMemberId, EventCode code, String detail) {
        log.info("[ClientService] onSessionEvent: correlationId={}, clusterSessionId={}, code={}, detail={}",
                correlationId, clusterSessionId, code, detail);
    }

    @Override
    public void onMessage(long clusterSessionId, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
        byte[] data = new byte[length];
        buffer.getBytes(offset, data);
        Order order = Deserializer.deserializeOrder(data);

        if (order != null) {
            log.info("[ClientService] Received order response: {}", order);
            // 处理订单响应
            CompletableFuture<OrderController.OrderResponse> future = pendingOrders.remove(order.getOrderId());
            if (future != null) {
                OrderController.OrderResponse response = new OrderController.OrderResponse();
                response.setOrderId(order.getOrderId());
                future.complete(response);
            }
        } else {
            log.error("[ClientService] Failed to deserialize order response");
        }
    }

    @Override
    public void onNewLeader(long clusterSessionId, long leadershipTermId, int leaderMemberId, String ingressEndpoints) {
        log.info("[ClientService] onNewLeader: clusterSessionId={}, leaderMemberId={}, ingressEndpoints={}",
                clusterSessionId, leaderMemberId, ingressEndpoints);
    }

    public boolean isInit() {
        return this.clusterClient.getIsInitialized().get();
    }
}
