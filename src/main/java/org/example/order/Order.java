package org.example.order;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Order implements Serializable {
    private String orderId;
    private Long userId;
    private String symbol;
    private Side side;
    private OrderType type;
    private BigDecimal price;
    private BigDecimal amount; // 初始下单数量
    private BigDecimal remainingAmount; // 剩余未成交数量
    private OrderStatus status;
    private Long createTime;
    private String clientOrderId;


    public enum Side { BUY, SELL }
    public enum OrderType { MARKET, LIMIT }
    public enum OrderStatus { NEW, PARTIALLY_FILLED, FILLED, CANCELED }

    // Constructor, getters, and setters

    // Constructor
    public Order() {
        this.createTime = System.currentTimeMillis();
        this.status = OrderStatus.NEW;
        this.remainingAmount = this.amount; // 在设置 amount 后需要更新 remainingAmount
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.remainingAmount = amount;
    }

    // Getters and setters
    // ...

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
}
