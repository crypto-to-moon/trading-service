package com.github.crypto.to.moon.trading.service.matching.market;

import lombok.Data;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;

import java.util.List;

@Data
public class MatchingResult {
    private Order order;
    private List<Trade> trades;

    public MatchingResult(Order order, List<Trade> trades) {
        this.order = order;
        this.trades = trades;
    }
}
