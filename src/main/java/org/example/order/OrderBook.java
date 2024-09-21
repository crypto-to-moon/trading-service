package org.example.order;

import org.example.account.AccountService;
import org.example.market.MarketDataFeed;

import java.math.BigDecimal;
import java.util.*;

import static org.example.order.Order.OrderType.MARKET;
import static org.example.order.Order.Side.BUY;

public class OrderBook {

    private String symbol;

    // 买单和卖单队列
    private TreeMap<BigDecimal, List<Order>> buyOrders = new TreeMap<>(Comparator.reverseOrder());
    private TreeMap<BigDecimal, List<Order>> sellOrders = new TreeMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public synchronized void addOrder(Order order) {
        if (order.getSide() == BUY ) {
            addBuyOrder(order);
        } else {
            addSellOrder(order);
        }

        // 进行撮合
        matchOrders();
    }

    private void addBuyOrder(Order order) {
        if (order.getType() == MARKET ) {
            // 市价买单，价格设为最大值
            order.setPrice(new BigDecimal("99999999"));
        }
        buyOrders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }

    private void addSellOrder(Order order) {
        if (order.getType() == MARKET ) {
            // 市价卖单，价格设为最小值
            order.setPrice(BigDecimal.ZERO);
        }
        sellOrders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }

    private void matchOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            BigDecimal highestBuyPrice = buyOrders.firstKey();
            BigDecimal lowestSellPrice = sellOrders.firstKey();

            if (highestBuyPrice.compareTo(lowestSellPrice) >= 0) {
                List<Order> buyList = buyOrders.get(highestBuyPrice);
                List<Order> sellList = sellOrders.get(lowestSellPrice);

                Order buyOrder = buyList.get(0);
                Order sellOrder = sellList.get(0);

                BigDecimal tradePrice = sellOrder.getType() == Order.OrderType.MARKET ? highestBuyPrice : lowestSellPrice;
                BigDecimal tradeQuantity = buyOrder.getRemainingAmount().min(sellOrder.getRemainingAmount());

                // 更新订单剩余数量
                buyOrder.setRemainingAmount(buyOrder.getRemainingAmount().subtract(tradeQuantity));
                sellOrder.setRemainingAmount(sellOrder.getRemainingAmount().subtract(tradeQuantity));

                // 更新订单状态
                updateOrderStatus(buyOrder);
                updateOrderStatus(sellOrder);

                // 生成成交记录
                Trade trade = new Trade();
                trade.setSymbol(buyOrder.getSymbol());
                trade.setBuyOrderId(buyOrder.getOrderId());
                trade.setSellOrderId(sellOrder.getOrderId());
                trade.setPrice(tradePrice);
                trade.setQuantity(tradeQuantity);
                trade.setTimestamp(System.currentTimeMillis());

                // 发布成交数据
                MarketDataFeed.getInstance().publishTrade(trade);

                // 更新账户信息
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

    private void updateOrderStatus(Order order) {
        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            order.updateStatus(Order.OrderStatus.FILLED);
        } else if (order.getRemainingAmount().compareTo(order.getAmount()) < 0) {
            order.updateStatus(Order.OrderStatus.PARTIALLY_FILLED);
        }
    }

}
