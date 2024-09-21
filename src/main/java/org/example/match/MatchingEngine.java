package org.example.match;

import org.example.account.AccountService;
import org.example.market.MarketDataFeed;
import org.example.order.Order;
import org.example.order.Trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class MatchingEngine {

    private static MatchingEngine instance = new MatchingEngine();

    // 使用价格为键的 TreeMap，买单按价格从高到低排序，卖单从低到高排序
    private TreeMap<BigDecimal, List<Order>> buyOrders = new TreeMap<>(Comparator.reverseOrder());
    private TreeMap<BigDecimal, List<Order>> sellOrders = new TreeMap<>();

    public static MatchingEngine getInstance() {
        return instance;
    }

    public synchronized void addOrder(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            addBuyOrder(order);
        } else {
            addSellOrder(order);
        }

        matchOrders();
    }

    private void addBuyOrder(Order order) {
        buyOrders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }

    private void addSellOrder(Order order) {
        sellOrders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }

    private void matchOrders() {
        // 撮合逻辑
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            BigDecimal highestBuyPrice = buyOrders.firstKey();
            BigDecimal lowestSellPrice = sellOrders.firstKey();

            if (highestBuyPrice.compareTo(lowestSellPrice) >= 0) {
                // 有成交
                List<Order> buyList = buyOrders.get(highestBuyPrice);
                List<Order> sellList = sellOrders.get(lowestSellPrice);

                Order buyOrder = buyList.get(0);
                Order sellOrder = sellList.get(0);

                BigDecimal tradePrice = sellOrder.getPrice();
                BigDecimal tradeQuantity = buyOrder.getRemainingAmount().min(sellOrder.getRemainingAmount());

                // 更新订单剩余数量
                buyOrder.setRemainingAmount(buyOrder.getRemainingAmount().subtract(tradeQuantity));
                sellOrder.setRemainingAmount(sellOrder.getRemainingAmount().subtract(tradeQuantity));

                // 生成成交记录
                Trade trade = new Trade(buyOrder.getOrderId(), sellOrder.getOrderId(), tradePrice, tradeQuantity, System.currentTimeMillis(), buyOrder.getSymbol());
                MarketDataFeed.getInstance().publishTrade(trade);

                // 更新账户余额
                AccountService.getInstance().updateBalancesAfterTrade(buyOrder.getUserId(), sellOrder.getUserId(), trade);

                // 移除已完成的订单
                if (buyOrder.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
                    buyList.remove(0);
                    if (buyList.isEmpty()) {
                        buyOrders.remove(highestBuyPrice);
                    }
                }
                if (sellOrder.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
                    sellList.remove(0);
                    if (sellList.isEmpty()) {
                        sellOrders.remove(lowestSellPrice);
                    }
                }
            } else {
                break; // 无法成交
            }
        }
    }
}
