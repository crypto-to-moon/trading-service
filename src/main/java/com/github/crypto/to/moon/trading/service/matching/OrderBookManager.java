package com.github.crypto.to.moon.trading.service.matching;


import com.github.crypto.to.moon.trading.service.order.Order;

import java.util.concurrent.ConcurrentHashMap;

public class OrderBookManager {

    // 维护每个交易对的订单簿
    private ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    public void addOrder(Order order) {
        // 获取对应交易对的订单簿
        OrderBook orderBook = orderBooks.computeIfAbsent(order.getSymbol(), k -> new OrderBook(order.getSymbol()));

        // 添加订单到订单簿
        orderBook.addOrder(order);
    }

    // 可以添加更多方法，例如撤单、查询订单等
}

