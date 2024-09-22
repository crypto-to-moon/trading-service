package com.github.crypto.to.moon.trading.service.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private Long userId;
    private String symbol;       // 交易对，例如 "BTC/USD"
    private String orderType;    // 订单类型，例如 "LIMIT" 或 "MARKET"
    private String side;         // 买卖方向，例如 "BUY" 或 "SELL"
    private BigDecimal price;    // 下单价格，对于市价单可以为空
    private BigDecimal amount; // 下单数量
    private String clientOrderId; // 客户端订单 ID
}
