package com.github.crypto.to.moon.trading.service.matching;

import lombok.Data;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;

import java.util.List;

@Data
public class MatchingEvent {
    private Order order;
    private List<Trade> trades; // 撮合产生的成交记录
    private Order updatedOrder; // 更新后的订单状态

    // Getters and Setters
}
