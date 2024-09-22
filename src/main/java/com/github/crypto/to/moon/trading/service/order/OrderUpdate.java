package com.github.crypto.to.moon.trading.service.order;

import lombok.Data;

@Data
public class OrderUpdate {
    private String orderId;
    private Order.OrderStatus newStatus;
    private Trade trade;

    // Getters and Setters
}
