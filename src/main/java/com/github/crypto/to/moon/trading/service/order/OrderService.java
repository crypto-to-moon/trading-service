package com.github.crypto.to.moon.trading.service.order;

import com.github.crypto.to.moon.trading.service.account.InsufficientBalanceException;
import com.github.crypto.to.moon.trading.service.trading.Trading;
import com.github.crypto.to.moon.trading.service.util.BigDecimalProtoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    OrderDispatcher orderDispatcher;

    public Order createOrder(OrderRequest orderRequest) throws InvalidOrderException, InsufficientBalanceException {
        // 验证订单参数
        validateOrderRequest(orderRequest);

        // 创建订单
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(orderRequest.getUserId());
        order.setSymbol(orderRequest.getSymbol());
        order.setSide(Order.Side.valueOf(orderRequest.getSide().toUpperCase()));
        order.setType(Order.OrderType.valueOf(orderRequest.getOrderType().toUpperCase()));
        order.setPrice(orderRequest.getPrice());
        order.setAmount(orderRequest.getAmount());
        order.setRemainingAmount(orderRequest.getAmount());
        order.setStatus(Order.OrderStatus.NEW);
        order.setCreateTime(System.currentTimeMillis());

        Trading.OrderRequest orderMessage = convertToOrderMessage(order);
        Trading.EventMessage eventMessage = Trading.EventMessage.newBuilder()
                .setOrderRequest(orderMessage)
                .setEventType(Trading.EventType.PLACE_ORDER)
                .build();
        orderDispatcher.publishOrder(eventMessage);

        return order;
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


    private void validateOrderRequest(OrderRequest orderRequest) throws InvalidOrderException {
        // 验证订单参数合法性
        if (orderRequest.getUserId() == null || orderRequest.getUserId() <= 0) {
            throw new InvalidOrderException("用户ID无效");
        }
        if (orderRequest.getSymbol() == null || orderRequest.getSymbol().isEmpty()) {
            throw new InvalidOrderException("交易对不能为空");
        }
        if (orderRequest.getSide() == null || (!orderRequest.getSide().equalsIgnoreCase("BUY") && !orderRequest.getSide().equalsIgnoreCase("SELL"))) {
            throw new InvalidOrderException("订单方向无效");
        }
        if (orderRequest.getOrderType() == null || (!orderRequest.getOrderType().equalsIgnoreCase("LIMIT") && !orderRequest.getOrderType().equalsIgnoreCase("MARKET"))) {
            throw new InvalidOrderException("订单类型无效");
        }
        if (orderRequest.getAmount().doubleValue() <= 0) {
            throw new InvalidOrderException("订单数量必须大于0");
        }
        if (orderRequest.getOrderType().equalsIgnoreCase("LIMIT") && orderRequest.getPrice().doubleValue() <= 0) {
            throw new InvalidOrderException("限价订单价格必须大于0");
        }
    }

    public void updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
//        Order order = orderRepository.findById(orderId);
//        if (order != null) {
//            order.setStatus(newStatus);
//            // TODO send to disruptor
//            // 记录状态变更日志
//            logOrderStatusChange(order);
//        }
    }

    private void logOrderStatusChange(Order order) {
        // 记录订单状态变更日志
        // 可以将日志写入数据库、日志文件或发送到监控系统
        System.out.println("订单状态更新：orderId=" + order.getOrderId() + ", newStatus=" + order.getStatus());
    }
}

