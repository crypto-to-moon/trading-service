package com.github.crypto.to.moon.trading.service.matching;

import com.lmax.disruptor.EventHandler;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchingHandler implements EventHandler<MatchingEvent> {
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    @Override
    public void onEvent(MatchingEvent event, long sequence, boolean endOfBatch) throws Exception {
        Order order = event.getOrder();
        String symbol = order.getSymbol();

        // 获取对应交易对的订单簿
        OrderBook orderBook = orderBooks.computeIfAbsent(symbol, s -> new OrderBook(symbol));

        // 执行撮合
        List<Trade> trades = orderBook.matchOrders();
        event.setTrades(trades);
        event.setUpdatedOrder(order);
    }
}

